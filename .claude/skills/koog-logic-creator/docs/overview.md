# KOOG overview

Source: https://docs.koog.ai/ | https://github.com/JetBrains/koog

---

## What is KOOG

KOOG is JetBrains' **Kotlin Multiplatform** framework for building and running AI agents in
idiomatic Kotlin. It provides a complete toolkit for creating agents that can use tools, follow
strategies, interact with LLMs, and orchestrate complex workflows.

**Status:** Alpha **Platforms:** JVM, JS, WasmJS, Android, iOS **Requirements:** JDK 17+, Kotlin
2.3.x

---

## Key capabilities

- **Multiple agent types**: Basic (single run), functional (imperative loop), graph-based (DAG),
  planner (GOAP)
- **Type-safe tool system**: `@Tool` annotations, `ToolSet` classes, `ToolRegistry`
- **Multiple LLM providers**: OpenAI, Anthropic, Google/Gemini, OpenRouter, DeepSeek, Ollama, AWS
  Bedrock, MistralAI
- **Structured output**: Type-safe JSON responses via `kotlinx.serialization` with auto-retry
- **MCP integration**: First-class Model Context Protocol client and server support
- **Features (plugins)**: Persistence/checkpointing, tracing, OpenTelemetry, memory (short/long-term), event handling,
  token counting
- **RAG**: Built-in retrieval-augmented generation with embeddings and vector storage
- **Agent-to-Agent (A2A)**: Protocol support for multi-agent communication
- **Framework integration**: Spring Boot starter, Ktor module

---

## Gradle setup

### Single umbrella dependency (recommended)

```kotlin
dependencies {
    implementation("ai.koog:koog-agents:0.7.3")
}
```

The `koog-agents` artifact transitively includes all core modules: agents-core, agents-tools, prompt
executors (OpenAI, Anthropic, Google, etc.), agents-mcp, embeddings, RAG, and more.

### Individual modules (when you need specific features only)

```kotlin
dependencies {
    // Core
    implementation("ai.koog:agents-core:0.7.3")
    implementation("ai.koog:agents-tools:0.7.3")

    // Specific LLM client
    implementation("ai.koog:prompt-executor-openai-client:0.7.3")

    // Features
    implementation("ai.koog:agents-features-snapshot:0.7.3")     // Persistence
    implementation("ai.koog:agents-features-trace:0.7.3")        // Tracing
    implementation("ai.koog:agents-features-opentelemetry:0.7.3") // OpenTelemetry
    implementation("ai.koog:agents-features-memory:0.7.3")       // Memory
    implementation("ai.koog:agents-features-sql:0.7.3")          // SQL persistence

    // MCP
    implementation("ai.koog:agents-mcp:0.7.3")        // MCP client
    implementation("ai.koog:agents-mcp-server:0.7.3")  // MCP server

    // RAG
    implementation("ai.koog:rag-base:0.7.3")
    implementation("ai.koog:embeddings-llm:0.7.3")

    // Framework integration
    implementation("ai.koog:koog-ktor:0.7.3")
    implementation("ai.koog:koog-spring-boot-starter:0.7.3")
}
```

---

## Module structure

### Core agent modules (`agents/`)

| Module              | Purpose                                                                                               |
|---------------------|-------------------------------------------------------------------------------------------------------|
| `agents-core`       | Central abstractions: `AIAgent`, strategies, graph/node DSL                                           |
| `agents-tools`      | Tool infrastructure: `Tool<TArgs, TResult>`, `SimpleTool`, `ToolRegistry`, `@Tool`, `@LLMDescription` |
| `agents-ext`        | Extension tools: `AskUser`, `SayToUser`, `ExitTool`, file/search/shell tools                          |
| `agents-mcp`        | MCP client integration                                                                                |
| `agents-mcp-server` | MCP server integration                                                                                |
| `agents-test`       | Testing utilities and mocking framework                                                               |
| `agents-utils`      | Shared utilities                                                                                      |

### Feature modules (`agents/agents-features/`)

| Module                            | Purpose                                                            |
|-----------------------------------|--------------------------------------------------------------------|
| `agents-features-event-handler`   | Event lifecycle hooks (onToolCallStarting, onAgentCompleted, etc.) |
| `agents-features-memory`          | Short-term agent memory                                            |
| `agents-features-longterm-memory` | Persistent memory with embeddings                                  |
| `agents-features-opentelemetry`   | OpenTelemetry tracing (Langfuse, W&B Weave)                        |
| `agents-features-trace`           | Execution tracing                                                  |
| `agents-features-tokenizer`       | Token counting                                                     |
| `agents-features-snapshot`        | Agent state persistence/recovery (file, JDBC, SQL providers)       |
| `agents-features-sql`             | SQL persistence provider                                           |
| `agents-features-a2a-*`           | Agent-to-Agent protocol (core, server, client)                     |

### Prompt/LLM modules (`prompt/`)

| Module                              | Purpose                                          |
|-------------------------------------|--------------------------------------------------|
| `prompt-model`                      | Message types, prompt model                      |
| `prompt-llm`                        | `LLModel`, `LLMProvider`, `LLMCapability`        |
| `prompt-executor-model`             | `PromptExecutor` interface                       |
| `prompt-executor-openai-client`     | OpenAI/compatible client                         |
| `prompt-executor-anthropic-client`  | Anthropic client                                 |
| `prompt-executor-google-client`     | Google/Gemini client                             |
| `prompt-executor-ollama-client`     | Ollama client                                    |
| `prompt-executor-openrouter-client` | OpenRouter client                                |
| `prompt-executor-deepseek-client`   | DeepSeek client                                  |
| `prompt-executor-bedrock-client`    | AWS Bedrock client                               |
| `prompt-executor-mistralai-client`  | MistralAI client                                 |
| `prompt-executor-llms-all`          | Factory functions (`simpleOpenAIExecutor`, etc.) |
| `prompt-structure`                  | Structured output support                        |

### Other modules

| Module                              | Purpose             |
|-------------------------------------|---------------------|
| `embeddings-base`, `embeddings-llm` | Embedding support   |
| `rag-base`, `rag-vector`            | RAG support         |
| `koog-ktor`                         | Ktor integration    |
| `koog-spring-boot-starter`          | Spring Boot starter |

---

## Quick start: simplest agent

```kotlin
import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.simpleOpenAIExecutor
import ai.koog.prompt.executor.clients.openai.OpenAIModels

val executor = simpleOpenAIExecutor(apiKey = "sk-...")
val agent = AIAgent(
    promptExecutor = executor,
    llmModel = OpenAIModels.Chat.GPT4o,
    systemPrompt = "You are a helpful assistant.",
)

val result = agent.run("What is the capital of France?")
println(result) // "The capital of France is Paris."

executor.close()
```

---

## Quick start: agent with tools

```kotlin
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

@LLMDescription("Calculator tools")
class CalculatorTools : ToolSet {
    @Tool
    @LLMDescription("Adds two numbers")
    fun plus(
        @LLMDescription("First number") a: Float,
        @LLMDescription("Second number") b: Float,
    ): String = (a + b).toString()
}

val registry = ToolRegistry {
    tools(CalculatorTools())
}

val agent = AIAgent(
    promptExecutor = executor,
    llmModel = OpenAIModels.Chat.GPT4o,
    systemPrompt = "You are a calculator assistant.",
    toolRegistry = registry,
)

val result = agent.run("What is 2.5 + 3.7?")
```

---

## Quick start: graph-based agent

```kotlin
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.extension.*

val myStrategy = strategy<String, String>("my_strategy") {
    val nodeCallLLM by nodeLLMRequestMultiple()
    val nodeExecuteTool by nodeExecuteMultipleTools(parallelTools = true)
    val nodeSendToolResult by nodeLLMSendMultipleToolResults()

    edge(nodeStart forwardTo nodeCallLLM)
    edge(nodeCallLLM forwardTo nodeExecuteTool onMultipleToolCalls { true })
    edge(nodeCallLLM forwardTo nodeFinish onMultipleAssistantMessages { true }
            transformed { it.joinToString("\n") { msg -> msg.content } })
    edge(nodeExecuteTool forwardTo nodeSendToolResult)
    edge(nodeSendToolResult forwardTo nodeExecuteTool onMultipleToolCalls { true })
    edge(nodeSendToolResult forwardTo nodeFinish onMultipleAssistantMessages { true }
            transformed { it.joinToString("\n") { msg -> msg.content } })
}

val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    strategy = myStrategy,
    toolRegistry = registry,
    systemPrompt = "You are a helpful assistant.",
)
```

---

## Four agent types

| Type            | Use case                                   | How to create                                                         |
|-----------------|--------------------------------------------|-----------------------------------------------------------------------|
| **Basic**       | Simple prompt-response with optional tools | `AIAgent(executor, model, systemPrompt)` — uses `singleRunStrategy()` |
| **Functional**  | Imperative control flow, custom tool loops | `AIAgent(executor, model, strategy = functionalStrategy { ... })`     |
| **Graph-based** | Multi-phase pipelines, complex workflows   | `AIAgent(executor, model, strategy = strategy("name") { ... })`       |
| **Planner**     | Goal-oriented with GOAP planning           | `AIAgent(executor, model, strategy = plannerStrategy)`                |

---

## Prompt executors (LLM providers)

Factory functions for quick setup:

```kotlin
simpleOpenAIExecutor(apiToken)          // OpenAI
simpleAnthropicExecutor(apiKey)         // Anthropic
simpleGoogleAIExecutor(apiKey)          // Google/Gemini
simpleOpenRouterExecutor(apiKey)        // OpenRouter
simpleOllamaAIExecutor(baseUrl)         // Ollama (local)
simpleMistralAIExecutor(apiKey)         // MistralAI
simpleAzureOpenAIExecutor(...)          // Azure OpenAI
```

For custom/OpenAI-compatible providers (like MiniMax):

```kotlin
val client = OpenAILLMClient(
    apiKey = "your-key",
    settings = OpenAIClientSettings(baseUrl = "https://api.minimax.io"),
)
val executor = MultiLLMPromptExecutor(mapOf(LLMProvider.OpenAI to client))
```

**Important**: Executors are `Closeable` — use `.use { }` or call `.close()` when done.

---

## Model constants

```kotlin
// OpenAI
OpenAIModels.Chat.GPT4o
OpenAIModels.Chat.GPT4oMini
OpenAIModels.Chat.O3

// Custom model (for non-standard providers)
LLModel(
    provider = LLMProvider.OpenAI,
    id = "MiniMax-M2.7-highspeed",
    capabilities = listOf(
        LLMCapability.Completion,
        LLMCapability.OpenAIEndpoint.Completions,
    ),
)
```

### LLMCapability reference

| Capability                                 | When needed                                  |
|--------------------------------------------|----------------------------------------------|
| `LLMCapability.Completion`                 | Always (basic chat completion)               |
| `LLMCapability.OpenAIEndpoint.Completions` | OpenAI-compatible endpoints                  |
| `LLMCapability.Tools`                      | When agent uses tools                        |
| `LLMCapability.MultipleChoices`            | When requesting multiple completions (n > 1) |
