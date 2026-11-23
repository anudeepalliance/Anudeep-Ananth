# KOOG prompt DSL

Source: https://docs.koog.ai/prompts/ | https://docs.koog.ai/prompts/prompt-creation/ | https://docs.koog.ai/prompts/prompt-executors/ | https://docs.koog.ai/prompts/handling-failures/ | https://docs.koog.ai/prompts/llm-response-caching/

---

## Overview

Prompts are instructions sent to LLMs. A prompt contains an ID, messages (system, user, assistant,
tool), and optional parameters (temperature, tool choice, etc.).

---

## Creating prompts

### Kotlin DSL

```kotlin
import ai.koog.prompt.dsl.prompt

val myPrompt = prompt("my-prompt-id") {
    system("You are a helpful assistant specialized in Kotlin programming.")
    user("Explain coroutines in simple terms.")
}
```

### With assistant messages (few-shot learning)

```kotlin
val fewShotPrompt = prompt("few-shot") {
    system("You are a sentiment analyzer. Reply with: positive, negative, or neutral.")
    user("I love this product!")
    assistant("positive")
    user("This is terrible.")
    assistant("negative")
    user("The weather is okay today.")
}
```

### With parameters

```kotlin
val paramPrompt = prompt("with-params") {
    system("You are a creative writer.")
    user("Write a haiku about Kotlin.")
    params {
        temperature = 0.9
        maxTokens = 100
    }
}
```

### Java builder

```java
var myPrompt = Prompt.builder("my-prompt-id")
        .system("You are a helpful assistant.")
        .user("What is Kotlin?")
        .build();
```

---

## Message types

| Type      | Method              | Description                          |
|-----------|---------------------|--------------------------------------|
| System    | `system("...")`     | Define LLM behavior and constraints  |
| User      | `user("...")`       | User input                           |
| Assistant | `assistant("...")`  | LLM response (for few-shot examples) |
| Tool      | `tool(id, content)` | Tool call results                    |

### Multimodal content

```kotlin
val multimodalPrompt = prompt("vision") {
    system("You are an image analyst.")
    user {
        text("What's in this image?")
        image(url = "https://example.com/image.jpg")
        // Or: image(base64 = "...", mediaType = "image/png")
    }
}
```

Supported media types: images, audio, video, documents (support varies by LLM provider).

---

## LLM parameters (LLMParams)

```kotlin
prompt("with-params") {
    system("...")
    user("...")
    params {
        temperature = 0.7          // Randomness (0.0 = deterministic, 1.0 = creative)
        maxTokens = 1000           // Response length limit
        numberOfChoices = 1        // Number of completions to generate

        toolChoice = ToolChoice.Auto       // Let LLM decide when to use tools
        // toolChoice = ToolChoice.Required // Force tool use
        // toolChoice = ToolChoice.None     // Disable tools
        // toolChoice = ToolChoice.Specific("tool_name") // Force specific tool

        schema = myJsonSchema      // Response structure (for structured output)
        speculation = "Expected format hint"  // Help LLM match expected format
    }
}
```

---

## Prompt executors

### SingleLLMPromptExecutor

For a single LLM provider:

```kotlin
import ai.koog.prompt.executor.llms.simpleOpenAIExecutor

val executor = simpleOpenAIExecutor(apiKey = "sk-...")
```

### MultiLLMPromptExecutor

Routes to different providers based on `LLMProvider`:

```kotlin
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient

val executor = MultiLLMPromptExecutor(
    mapOf(
        LLMProvider.OpenAI to openaiClient,
        LLMProvider.Anthropic to anthropicClient,
    )
)
```

### RoutingLLMPromptExecutor

Load balancing across multiple instances:

```kotlin
import ai.koog.prompt.executor.llms.RoutingLLMPromptExecutor
import ai.koog.prompt.executor.llms.RoundRobinRouter

val executor = RoutingLLMPromptExecutor(
    executors = listOf(executor1, executor2, executor3),
    router = RoundRobinRouter(),
)
```

---

## Executing prompts

### Basic execution

```kotlin
val responses: List<Message.Response> = executor.execute(
    prompt = myPrompt,
    model = llmModel,
    tools = emptyList(),
)
val text = responses.filterIsInstance<Message.Assistant>().first().content
```

### Structured execution

```kotlin
import ai.koog.prompt.executor.model.executeStructured

val result = executor.executeStructured(
    prompt = myPrompt,
    model = llmModel,
    serializer = MyDataClass.serializer(),
    fixingParser = StructureFixingParser(model = llmModel, retries = 3),
)
val data: MyDataClass = result.getOrThrow().data
```

---

## Failure handling

### Retry mechanisms

KOOG provides built-in retry for transient failures:

```kotlin
import ai.koog.prompt.executor.model.RetryConfig

val executor = simpleOpenAIExecutor(apiKey).withRetry(
    RetryConfig(
        maxRetries = 3,
        initialDelay = 1000L, // ms
        maxDelay = 10000L,    // ms
        backoffMultiplier = 2.0,
    )
)
```

### Timeout

```kotlin
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes

val result = withTimeoutOrNull(5.minutes) {
    agent.run(input)
} ?: "Agent timed out"
```

### Fallback between providers

With `MultiLLMPromptExecutor`, if one provider fails, you can implement fallback logic by catching
exceptions and retrying with a different model.

---

## LLM response caching

Cache responses to avoid redundant LLM calls:

```kotlin
import ai.koog.prompt.executor.cached.CachedPromptExecutor

val cachedExecutor = CachedPromptExecutor(
    delegate = baseExecutor,
    cache = FileCacheProvider(cacheDirectory),
    // Or: cache = RedisCacheProvider(redisClient),
)
```

---

## History compression

When conversations grow too long, compress history to fit the context window:

### Strategies

| Strategy                             | Description                                     |
|--------------------------------------|-------------------------------------------------|
| `WholeHistory`                       | Compress entire history into one TLDR (default) |
| `FromLastNMessages(n)`               | Keep last N messages, discard earlier           |
| `Chunked(size)`                      | Split into fixed-size chunks, compress each     |
| `RetrieveFactsFromHistory(concepts)` | Extract specific facts from history             |

### Usage in graph

```kotlin
val nodeCompress by nodeLLMCompressHistory<ReceivedToolResult>(
    strategy = HistoryCompressionStrategy.WholeHistory,
    preserveMemory = true,
)
```

### Usage in functional strategy

```kotlin
llm.writeSession {
    replaceHistoryWithTLDR(strategy = HistoryCompressionStrategy.Chunked(10))
}
```
