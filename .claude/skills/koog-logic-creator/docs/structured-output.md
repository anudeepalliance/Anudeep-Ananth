# KOOG structured output

Source: https://docs.koog.ai/structured-output/

---

## Overview

Structured output lets you get typed Kotlin objects from LLM responses instead of raw text. KOOG
auto-generates a JSON schema from your `@Serializable` data class and instructs the LLM to return
valid JSON matching that schema.

Four components:

1. **Data structure definition** — `@Serializable` classes with `@LLMDescription` annotations
2. **JSON schema generation** — automatic from `KSerializer`
3. **Structured LLM request** — `executeStructured<T>()`
4. **Response handling** — automatic parsing with retry via `StructureFixingParser`

---

## Defining structured output types

```kotlin
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@LLMDescription("Analysis result containing sentiment and key topics")
data class AnalysisResult(
    @property:LLMDescription("Overall sentiment: positive, negative, or neutral")
    val sentiment: String,

    @property:LLMDescription("List of key topics identified in the text")
    val topics: List<String>,

    @property:LLMDescription("Confidence score between 0.0 and 1.0")
    val confidence: Float,
)
```

### Annotation rules

- `@Serializable` — required for kotlinx.serialization
- `@LLMDescription` on the class — describes the overall output to the LLM
- `@property:LLMDescription` on each field — describes each field (used in the JSON schema)
- `@SerialName("custom_name")` — overrides the field name in JSON

### Supported types

- Primitives: `String`, `Int`, `Float`, `Double`, `Boolean`, `Long`
- Collections: `List<T>`, `Set<T>`, `Map<String, T>`
- Nested `@Serializable` data classes
- Enums (with `@SerialName` for custom values)
- Sealed classes (polymorphic, with discriminator)
- Nullable types (optional fields)

### Enum example

```kotlin
@Serializable
enum class Sentiment {
    @SerialName("positive")
    POSITIVE,
    @SerialName("negative")
    NEGATIVE,
    @SerialName("neutral")
    NEUTRAL,
}
```

### Sealed class example

```kotlin
@Serializable
sealed class Action {
    @Serializable
    @SerialName("search")
    data class Search(val query: String) : Action()

    @Serializable
    @SerialName("calculate")
    data class Calculate(val expression: String) : Action()
}
```

---

## Three API layers

### 1. Prompt executor level (lowest level)

Direct call to the executor with a prompt:

```kotlin
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.executor.model.StructureFixingParser

val myPrompt = prompt("analysis") {
    system("You are a text analysis expert.")
    user("Analyze this text: $inputText")
}

val result = executor.executeStructured(
    prompt = myPrompt,
    model = llmModel,
    serializer = AnalysisResult.serializer(),
    fixingParser = StructureFixingParser(model = llmModel, retries = 3),
)

val analysisResult: AnalysisResult = result.getOrThrow().data
```

### 2. Agent LLM context level

Within a write session inside a functional or custom strategy:

```kotlin
llm.writeSession {
    val result = requestLLMStructured<AnalysisResult>(
        fixingParser = StructureFixingParser(model = llmModel, retries = 3),
    )
    // result is AnalysisResult
}
```

### 3. Node level (graph strategies)

```kotlin
val nodeStructured by nodeLLMRequestStructured<AnalysisResult>(
    name = "analyze",
    fixingParser = StructureFixingParser(model = llmModel, retries = 3),
)
```

---

## StructureFixingParser

When the LLM returns invalid JSON, `StructureFixingParser` automatically retries by:

1. Sending the failed response + the parse error + the schema back to the LLM
2. Asking the LLM to fix the JSON
3. Retrying up to N times

```kotlin
val fixingParser = StructureFixingParser(
    model = llmModel,
    retries = 5, // max retry attempts
)
```

The fixing parser uses a specialized prompt with sections:

- **PROCESS** — instructions for how to fix the JSON
- **KEY PRINCIPLES** — guidelines for valid output
- **RESULT** — the failed output
- **DEFINITION** — the expected JSON schema

---

## JSON schema generators

KOOG generates JSON schemas from `KSerializer` descriptors. Built-in generators:

- `StandardJsonSchemaGenerator` — full JSON Schema draft 7
- `BasicJsonSchemaGenerator` — simplified schema
- Provider-specific generators for optimal compatibility

The schema is automatically appended to the system prompt when using `executeStructured`.

---

## Best practices

1. **Use `@property:LLMDescription`** on every field — explicit descriptions dramatically improve
   output quality
2. **Keep types simple** — flat data classes work better than deeply nested structures
3. **Use enums** for constrained string fields — the LLM gets the exact valid values
4. **Always use `StructureFixingParser`** — LLMs occasionally return invalid JSON
5. **Set reasonable retry counts** — 3-5 retries is usually sufficient
6. **Validate semantically** after parsing — JSON validity does not mean domain validity (use
   `withSemanticRetry` for domain-level checks)
7. **Wrap bare lists** — `executeStructured` needs a top-level object, not a bare `List<T>`. Use a
   wrapper data class:

```kotlin
@Serializable
data class ItemsResponse(val items: List<Item>)
```
