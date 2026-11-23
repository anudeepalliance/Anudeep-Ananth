# KOOG tools

Source: https://docs.koog.ai/tools-overview/ | https://docs.koog.ai/annotation-based-tools/ | https://docs.koog.ai/class-based-tools/ | https://docs.koog.ai/built-in-tools/

---

## Tool system overview

Tools give agents the ability to interact with external systems, perform calculations, access
databases, call APIs, etc. The LLM decides when to call a tool based on the tool's name and
description.

Three ways to create tools:

1. **Annotation-based** (`@Tool` + `@LLMDescription` on `ToolSet`) — concise, recommended for most
   cases
2. **Class-based** (`SimpleTool<Args>` or `Tool<Args, Result>`) — full control over serialization
   and execution
3. **Built-in tools** — pre-built utilities (AskUser, SayToUser, ExitTool, file tools)

---

## 1. Annotation-based tools (recommended)

Use `ToolSet` interface with `@Tool` and `@LLMDescription` annotations:

```kotlin
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

@LLMDescription("Tools for performing math operations")
class MathTools : ToolSet {

    @Tool
    @LLMDescription("Adds two numbers and returns the result")
    fun add(
        @LLMDescription("First number") a: Float,
        @LLMDescription("Second number") b: Float,
    ): String = (a + b).toString()

    @Tool(customName = "multiply_numbers")
    @LLMDescription("Multiplies two numbers and returns the result")
    fun multiply(
        @LLMDescription("First number") a: Float,
        @LLMDescription("Second number") b: Float,
    ): String = (a * b).toString()
}
```

### Key rules for annotation-based tools

- The class must implement `ToolSet`
- Each tool method must be annotated with `@Tool`
- `@LLMDescription` on the class describes the toolset to the LLM
- `@LLMDescription` on each method describes what the tool does
- `@LLMDescription` on each parameter describes the parameter
- Return type should be `String` (or types that convert to String)
- `@Tool(customName = "...")` overrides the function name as the tool name
- Methods can be `suspend` for async operations
- Tools can be `object` (singleton) or `class` (instance)

### Java equivalent

```java

@LLMDescription("Tools for performing math operations")
public static class MathTools implements ToolSet {
    @Tool
    @LLMDescription("Adds two numbers and returns the result")
    public int add(int a, int b) {
        return a + b;
    }
}
```

---

## 2. Class-based tools

### SimpleTool (returns String)

For tools that return a plain string result:

```kotlin
import ai.koog.agents.core.tools.SimpleTool
import kotlinx.serialization.Serializable

object LessThanTool : SimpleTool<LessThanTool.Args>(
    argsSerializer = Args.serializer(),
    name = "less_than",
    description = "Asks if a number is less than the given value",
) {
    @Serializable
    data class Args(val value: Int)

    override suspend fun execute(args: Args): String {
        return if (secretNumber < args.value) "YES" else "NO"
    }
}
```

### Tool (custom result type)

For tools that need typed results:

```kotlin
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable

object WeatherTool : Tool<WeatherTool.Args, WeatherTool.Result>(
    argsSerializer = Args.serializer(),
) {
    @Serializable
    data class Args(val city: String)

    @Serializable
    data class Result(val temperature: Float, val conditions: String)

    override val descriptor = ToolDescriptor(
        name = "get_weather",
        description = "Get current weather for a city",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "city",
                description = "The city name",
                type = ToolParameterType.String,
            )
        ),
    )

    override suspend fun execute(args: Args): Result {
        // Call weather API...
        return Result(temperature = 72.0f, conditions = "Sunny")
    }
}
```

---

## 3. Built-in tools

| Tool                | Name                 | Description                                     |
|---------------------|----------------------|-------------------------------------------------|
| `SayToUser`         | `__say_to_user__`    | Send a message to the user (prints to stdout)   |
| `AskUser`           | `__ask_user__`       | Ask the user for input (reads from stdin)       |
| `ExitTool`          | `__exit__`           | Signal the agent to finish its execution        |
| `ReadFileTool`      | `__read_file__`      | Read text file content with optional line range |
| `EditFileTool`      | `__edit_file__`      | Single targeted text replacement in a file      |
| `WriteFileTool`     | `__write_file__`     | Write text content to a file                    |
| `ListDirectoryTool` | `__list_directory__` | Hierarchical directory listing                  |

Built-in tools live in the `agents-ext` module.

---

## Tool registry

Register tools and pass them to the agent:

```kotlin
import ai.koog.agents.core.tools.ToolRegistry

val toolRegistry = ToolRegistry {
    // Annotation-based ToolSet
    tools(MathTools())

    // Individual class-based tools
    tool(LessThanTool)
    tool(WeatherTool)

    // Built-in tools
    tool(AskUser)
    tool(SayToUser)

    // List of tools
    tools(listOf(LessThanTool, WeatherTool))
}
```

### Merging registries

```kotlin
val combined = firstRegistry + secondRegistry
```

### Passing to the agent

```kotlin
val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    systemPrompt = "You are a helpful assistant.",
    toolRegistry = toolRegistry,
    maxIterations = 10,
)
```

---

## Agents as tools

Convert any agent into a tool that can be used by other agents:

```kotlin
val analysisAgentTool = analysisAgentService.createAgentTool(
    agentName = "analyzeTransactions",
    agentDescription = "Performs financial transaction analysis",
    inputDescription = "Transaction analysis request",
    inputType = typeToken<String>(),
)
```

This enables hierarchical multi-agent systems where a top-level agent delegates sub-tasks to
specialized agents via the tool system.

---

## Calling tools programmatically

Within `AIAgentLLMWriteSession`:

```kotlin
llm.writeSession {
    // By tool reference
    val result = callTool(WeatherTool, WeatherTool.Args("London"))

    // By tool name
    val result2 = callTool("get_weather", """{"city": "London"}""")

    // Raw string result
    val raw = callToolRaw("get_weather", """{"city": "London"}""")
}
```

---

## Tool descriptor schemer

Converts `ToolDescriptor` into JSON Schema for specific LLM providers. Built-in implementations:

- `OpenAICompatibleToolDescriptorSchemer` — for OpenAI and compatible APIs
- `OllamaToolDescriptorSchemer` — for Ollama

Custom implementations can be created by implementing `ToolDescriptorSchemer`.
