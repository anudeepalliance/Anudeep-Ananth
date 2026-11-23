# KOOG MCP and integrations

Source: https://docs.koog.ai/model-context-protocol/ | https://docs.koog.ai/ktor-plugin/ | https://docs.koog.ai/spring-boot/ | https://docs.koog.ai/a2a-protocol-overview/

---

## Model Context Protocol (MCP)

KOOG has first-class MCP support for both client and server roles.

Module: `agents-mcp` (client), `agents-mcp-server` (server)

### MCP client integration

Three key components:

- `McpTool` — bridge between KOOG and MCP SDK
- `McpToolDescriptorParser` — converts MCP tool definitions to KOOG `ToolDescriptor` format
- `McpToolRegistryProvider` — establishes tool registries with MCP servers

### Transport options

**Stdio transport** (separate process):

```kotlin
val process = ProcessBuilder("npx", "@anthropic-ai/mcp-server-playwright").start()
val toolRegistry = McpToolRegistryProvider.defaultStdioTransport(process)
```

**SSE transport** (HTTP):

```kotlin
val toolRegistry = McpToolRegistryProvider.defaultSseTransport("http://localhost:8931")
```

### Complete MCP client example

```kotlin
import ai.koog.agents.mcp.McpToolRegistryProvider

// Start MCP server process
val process = ProcessBuilder("npx", "@anthropic-ai/mcp-server-playwright").start()

// Create tool registry from MCP tools
val mcpRegistry = McpToolRegistryProvider.defaultStdioTransport(process)

// Use MCP tools in a KOOG agent
val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    systemPrompt = "You are a browser automation assistant.",
    toolRegistry = mcpRegistry,
    maxIterations = 20,
)

val result = agent.run("Navigate to google.com and search for Koog framework")
```

---

## Ktor integration

Module: `koog-ktor`

Install the KOOG plugin in a Ktor application:

```kotlin
import ai.koog.ktor.Koog

install(Koog) {
    llm {
        openAI(apiKey = System.getenv("OPENAI_API_KEY") ?: "")
        anthropic(apiKey = System.getenv("ANTHROPIC_API_KEY") ?: "")
        ollama { baseUrl = "http://localhost:11434" }
        fallback {
            provider = LLMProvider.OpenAI
            model = OpenAIModels.Chat.GPT4_1
        }
    }
}
```

### Route helpers

```kotlin
routing {
    // AI agent endpoint
    post("/chat") {
        aiAgent {
            // Agent configuration
        }
    }

    // Direct LLM access
    post("/complete") {
        llm {
            // LLM request
        }
    }
}
```

Supports streaming, moderation, and MCP tools within routes.

---

## Spring Boot integration

Module: `koog-spring-boot-starter`

### Auto-configuration

Application properties:

```properties
ai.koog.openai.api-key=${OPENAI_API_KEY}
ai.koog.anthropic.api-key=${ANTHROPIC_API_KEY}
ai.koog.ollama.base-url=http://localhost:11434
ai.koog.openai.enabled=true
```

### Bean injection

```kotlin
@RestController
class ChatController(
    @Qualifier("openAIExecutor") private val executor: PromptExecutor,
) {
    @PostMapping("/chat")
    suspend fun chat(@RequestBody message: String): String {
        val agent = AIAgent(
            promptExecutor = executor,
            llmModel = OpenAIModels.Chat.GPT4o,
            systemPrompt = "You are a helpful assistant.",
        )
        return agent.run(message)
    }
}
```

Supports multi-provider fallback via `MultiLLMPromptExecutor` bean.

---

## Spring AI integration

Module: `koog-spring-ai-starter-model-chat`, `koog-spring-ai-starter-model-embedding`

Adapts Spring AI's `ChatModel` and `EmbeddingModel` to KOOG's `LLMClient`. Supports 13+ Spring AI
providers. Tools are managed by KOOG, not Spring AI.

---

## Agent-to-Agent (A2A) protocol

KOOG implements the A2A v0.3.0 protocol for agent-to-agent communication.

Modules: `agents-features-a2a-core`, `agents-features-a2a-server`, `agents-features-a2a-client`

### A2A server (expose KOOG agent)

```kotlin
val server = A2AServer(
    agentExecutor = myAgentExecutor,
    agentCard = AgentCard(name = "MyAgent", description = "..."),
    transport = HttpJSONRPCServerTransport(port = 8080),
)
server.start()
```

### A2A client (connect to external agent)

```kotlin
val client = A2AClient(
    transport = clientTransport,
    agentCardResolver = AgentCardResolver("http://agent-host:8080"),
)
client.connect()
val response = client.sendMessage("Hello from another agent")
```

---

## Agent Client Protocol (ACP)

For bidirectional client-agent communication (JVM-only).

Module: `agents-features-acp`

```kotlin
install(AcpAgent) {
    // Configure ACP support
}
```

Supports custom events, default notifications, and message conversion utilities.
