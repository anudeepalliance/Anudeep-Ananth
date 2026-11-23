# KOOG LLM providers

Source: https://docs.koog.ai/llm-providers/ | https://docs.koog.ai/model-capabilities/ | https://docs.koog.ai/llm-parameters/

---

## Supported providers

| Provider            | Client class          | Factory function                   | Tools | Streaming | Embeddings |
|---------------------|-----------------------|------------------------------------|-------|-----------|------------|
| OpenAI              | `OpenAILLMClient`     | `simpleOpenAIExecutor(apiKey)`     | Yes   | Yes       | Yes        |
| Anthropic           | `AnthropicLLMClient`  | `simpleAnthropicExecutor(apiKey)`  | Yes   | Yes       | No         |
| Google/Gemini       | `GoogleLLMClient`     | `simpleGoogleAIExecutor(apiKey)`   | Yes   | Yes       | Yes        |
| DeepSeek            | `DeepSeekLLMClient`   | `simpleDeepSeekExecutor(apiKey)`   | Yes   | Yes       | No         |
| OpenRouter          | `OpenRouterLLMClient` | `simpleOpenRouterExecutor(apiKey)` | Yes   | Yes       | No         |
| AWS Bedrock         | `BedrockLLMClient`    | `simpleBedrockExecutor(apiKey)`    | Yes   | Yes       | Yes        |
| MistralAI           | `MistralAILLMClient`  | `simpleMistralAIExecutor(apiKey)`  | Yes   | Yes       | Yes        |
| Alibaba (DashScope) | `DashScopeLLMClient`  | —                                  | Yes   | Yes       | No         |
| Ollama              | `OllamaClient`        | `simpleOllamaAIExecutor(baseUrl)`  | Yes   | Yes       | Yes        |

---

## Quick setup with factory functions

```kotlin
// OpenAI
val executor = simpleOpenAIExecutor(System.getenv("OPENAI_API_KEY"))

// Anthropic
val executor = simpleAnthropicExecutor(System.getenv("ANTHROPIC_API_KEY"))

// Google/Gemini
val executor = simpleGoogleAIExecutor(System.getenv("GOOGLE_AI_API_KEY"))

// Ollama (local)
val executor = simpleOllamaAIExecutor("http://localhost:11434")

// Azure OpenAI
val executor = simpleAzureOpenAIExecutor(
    resourceName = "my-resource",
    deploymentName = "gpt-4o",
    apiVersion = "2024-02-15-preview",
    apiToken = System.getenv("AZURE_OPENAI_API_KEY"),
)
```

---

## Custom / OpenAI-compatible providers

For any provider that exposes an OpenAI-compatible API (MiniMax, Together, Groq, etc.):

```kotlin
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor

val client = OpenAILLMClient(
    apiKey = "your-api-key",
    settings = OpenAIClientSettings(baseUrl = "https://api.provider.com"),
)

val executor = MultiLLMPromptExecutor(mapOf(LLMProvider.OpenAI to client))
```

---

## Model constants

### OpenAI

```kotlin
OpenAIModels.Chat.GPT4o
OpenAIModels.Chat.GPT4oMini
OpenAIModels.Chat.O3
OpenAIModels.Chat.GPT5
```

### Ollama

```kotlin
OllamaModels.Meta.LLAMA_3_2
```

### Custom model definition

```kotlin
val myModel = LLModel(
    provider = LLMProvider.OpenAI,
    id = "model-name-here",
    capabilities = listOf(
        LLMCapability.Completion,
        LLMCapability.OpenAIEndpoint.Completions,
    ),
)
```

---

## LLMCapability reference

Capabilities determine what features the model supports. Declare them when creating `LLModel`:

| Capability                                 | When to include                      |
|--------------------------------------------|--------------------------------------|
| `LLMCapability.Completion`                 | Always (basic chat completion)       |
| `LLMCapability.OpenAIEndpoint.Completions` | OpenAI-compatible chat endpoints     |
| `LLMCapability.Tools`                      | When the agent uses tools            |
| `LLMCapability.MultipleChoices`            | When requesting n > 1 completions    |
| `LLMCapability.Schema`                     | When using structured output schemas |
| `LLMCapability.Temperature`                | When setting temperature             |
| `LLMCapability.Streaming`                  | When using streaming responses       |
| `LLMCapability.Embeddings`                 | When generating embeddings           |

### Example with tools

```kotlin
val modelWithTools = LLModel(
    provider = LLMProvider.OpenAI,
    id = "MiniMax-M2.7-highspeed",
    capabilities = listOf(
        LLMCapability.Completion,
        LLMCapability.OpenAIEndpoint.Completions,
        LLMCapability.Tools,
    ),
)
```

---

## LLMProvider enum

Used to route requests in `MultiLLMPromptExecutor`:

| Value                   | Use with                                            |
|-------------------------|-----------------------------------------------------|
| `LLMProvider.OpenAI`    | OpenAI, Azure OpenAI, and any OpenAI-compatible API |
| `LLMProvider.Anthropic` | Anthropic Claude                                    |
| `LLMProvider.Google`    | Google Gemini                                       |
| `LLMProvider.Ollama`    | Ollama (local)                                      |

---

## Executor lifecycle

**Important**: Executors are `Closeable`. Always close them when done:

```kotlin
// Option 1: use block
simpleOpenAIExecutor(apiKey).use { executor ->
    val agent = AIAgent(promptExecutor = executor, ...)
    agent.run("...")
}

// Option 2: explicit close
val executor = simpleOpenAIExecutor(apiKey)
try {
    val agent = AIAgent(promptExecutor = executor, ...)
    agent.run("...")
} finally {
    executor.close()
}

// Option 3: lazy singleton (for server-lifetime executors)
val executor by lazy { MultiLLMPromptExecutor(mapOf(...)) }
// Close on shutdown
```

---

## Content moderation

Built-in moderation support via OpenAI and Ollama:

```kotlin
// OpenAI moderation
val moderationResult = executor.moderate(
    text = "Some text to check",
    model = OpenAIModels.Moderation.Omni,
)

if (moderationResult.isHarmful) {
    println("Violated categories: ${moderationResult.violatedCategories}")
}
```

18 moderation categories supported including: hate, harassment, self-harm, sexual content, violence,
etc.
