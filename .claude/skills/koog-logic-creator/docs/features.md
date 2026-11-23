# KOOG features (plugins)

Source: https://docs.koog.ai/features/ | https://docs.koog.ai/features/agent-persistence/ | https://docs.koog.ai/features/tracing/ | https://docs.koog.ai/features/open-telemetry/ | https://docs.koog.ai/features/agent-memory/ | https://docs.koog.ai/features/chat-memory/ | https://docs.koog.ai/features/agent-event-handlers/

---

## Overview

Features are plugin-like extensions that add capabilities to KOOG agents. They are installed at
agent construction time via the `installFeatures` lambda.

```kotlin
val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    strategy = myStrategy,
    systemPrompt = null,
) {
    install(Persistence) { ... }
    install(Tracing) { ... }
    install(OpenTelemetry) { ... }
}
```

---

## 1. Persistence (checkpointing)

Module: `agents-features-snapshot`

Saves agent state after each node so crashed pipelines can resume from the last completed node.

### Setup

```kotlin
import ai.koog.agents.snapshot.feature.Persistence
import ai.koog.agents.snapshot.providers.file.JVMFilePersistenceStorageProvider

install(Persistence) {
    storage = JVMFilePersistenceStorageProvider(checkpointDirectory)
    enableAutomaticPersistence = true
}
```

### Storage providers

| Provider                                  | Use case                                                          |
|-------------------------------------------|-------------------------------------------------------------------|
| `JVMFilePersistenceStorageProvider(path)` | Local disk (JVM). Creates checkpoint files in the given directory |
| `FilePersistenceStorageProvider(path)`    | Multiplatform file provider                                       |
| `InMemoryPersistenceStorageProvider`      | Testing only. Non-persistent                                      |
| `NoPersistenceStorageProvider`            | Disables persistence                                              |

### How it works

- Checkpoints capture: message history, current node, node input data, timestamps
- On resume, the agent restores from the latest checkpoint and replays from that node
- Session IDs determine which checkpoint chain to use — same session ID = same chain
- `enableAutomaticPersistence = true` writes a checkpoint after every node automatically

### Rollback support

For tools with side effects, use `RollbackToolRegistry` to define undo operations:

```kotlin
val rollbackRegistry = RollbackToolRegistry {
    rollback(DatabaseInsertTool) { args -> deleteFromDb(args.id) }
}
```

---

## 2. Tracing

Module: `agents-features-trace`

Intercepts and logs execution events within the agent graph.

### Setup

```kotlin
import ai.koog.agents.features.tracing.feature.Tracing
import ai.koog.agents.features.tracing.writer.TraceFeatureMessageLogWriter
import io.github.oshai.kotlinlogging.KotlinLogging

val traceLogger = KotlinLogging.logger("MyAgentTrace")

install(Tracing) {
    addMessageProcessor(TraceFeatureMessageLogWriter(traceLogger))
}
```

### Message processors

| Processor                              | Description                               |
|----------------------------------------|-------------------------------------------|
| `TraceFeatureMessageLogWriter(logger)` | Logs trace messages via KotlinLogging     |
| `TraceFeatureMessageFileWriter(path)`  | Writes trace messages to a file           |
| `TraceFeatureMessageRemoteWriter(url)` | Sends trace messages to a remote endpoint |

### Events traced

Strategy execution, LLM calls, LLM streaming, tool calls, node execution, edge traversal, error
events — 20+ event categories.

---

## 3. OpenTelemetry

Module: `agents-features-opentelemetry`

Comprehensive observability with hierarchical span structures.

### Setup

```kotlin
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry

install(OpenTelemetry) {
    setServiceInfo("my-agent-pipeline", "1.0.0")
    addLangfuseExporter()  // or addWeaveExporter(), addDatadogExporter()
}
```

### Span hierarchy

```
CreateAgentSpan
  -> InvokeAgentSpan
    -> StrategySpan
      -> NodeExecuteSpan (per node)
        -> InferenceSpan (LLM calls)
        -> ExecuteToolSpan (tool calls)
        -> McpClientSpan (MCP operations)
```

### Exporters

| Exporter      | Setup                                                                                     |
|---------------|-------------------------------------------------------------------------------------------|
| **Langfuse**  | `addLangfuseExporter()` — requires `LANGFUSE_PUBLIC_KEY` + `LANGFUSE_SECRET_KEY` env vars |
| **W&B Weave** | `addWeaveExporter()`                                                                      |
| **Datadog**   | `addDatadogExporter()`                                                                    |
| **Jaeger**    | `addOtlpHttpExporter(url)` or `addOtlpGrpcExporter(url)`                                  |
| **Logging**   | `addLoggingExporter()` (default, logs to stdout)                                          |

### MCP-specific telemetry

When using MCP tools, OpenTelemetry captures additional attributes: MCP server name, transport type,
tool schema, request/response payloads.

---

## 4. Event handlers

Module: `agents-features-event-handler`

React to agent lifecycle events.

### Setup

```kotlin
import ai.koog.agents.features.eventHandler.feature.handleEvents

// Extension function approach (recommended)
handleEvents {
    onToolCallStarting { ctx -> println("Tool starting: ${ctx.toolName}") }
    onToolCallFinished { ctx -> println("Tool finished: ${ctx.toolName}") }
    onAgentCompleted { ctx -> println("Agent completed: ${ctx.result}") }
    onAgentExecutionFailed { ctx -> println("Agent failed: ${ctx.throwable}") }
}

// Or install feature directly
install(EventHandler) {
    onToolCallStarting { ctx -> /* ... */ }
}
```

### Available events

- `onAgentStarted` / `onAgentCompleted` / `onAgentExecutionFailed`
- `onToolCallStarting` / `onToolCallFinished` / `onToolCallFailed`
- `onStrategyStarted` / `onStrategyCompleted`
- `onNodeStarted` / `onNodeCompleted`
- `onLLMRequestStarted` / `onLLMRequestCompleted`

Multiple handlers can be registered for the same event type.

---

## 5. Chat memory

Module: `agents-features-memory`

Gives an agent multi-turn conversation continuity: loads prior history before each `agent.run()` and
saves the updated history after. Conversations are keyed by a session id passed as the second
argument to `agent.run(input, sessionId)`.

```kotlin
import ai.koog.agents.features.memory.feature.ChatMemory

install(ChatMemory) {
    windowSize(20)
}
```

For the full picture — session ids, custom `ChatHistoryProvider` implementations, built-in and
custom preprocessors, chat agent (CLI) and chat backend (Ktor) patterns, and Peekopals-specific
guidance — see [chat-memory.md](chat-memory.md).

---

## 6. Agent memory (facts, concepts, subjects)

Module: `agents-features-memory`

Structured, hierarchical memory for storing **typed facts about the world** (user preferences,
environment, per-character relationship state) — distinct from `ChatMemory`, which stores raw
conversation history. Facts live under a Concept × Subject × Scope coordinate and can be
saved/loaded directly or via predefined graph nodes (including LLM-driven auto-detection).

```kotlin
import ai.koog.agents.local.memory.feature.AgentMemory

install(AgentMemory) {
    memoryProvider = memoryProvider
    agentName = "..."; featureName = "..."; organizationName = "..."; productName = "..."
}
```

For the full picture — Fact / Concept / Subject / Scope model, `LocalFileMemoryProvider` and custom
providers, encryption, predefined nodes (`nodeSaveToMemoryAutoDetectFacts` etc.), `withMemory { }`
DSL, and Peekopals-specific guidance — see [agent-memory.md](agent-memory.md).

---

## 7. Long-term memory (experimental)

Module: `agents-features-longterm-memory`

RAG-style cross-session memory: retrieves relevant prior context via vector similarity and injects
it into the prompt before LLM calls; ingests new conversation content into a vector store for future
retrieval. Two independent pipelines (retrieval + ingestion), three operational modes (Full
Automatic / Manual Only / Hybrid), pluggable storage (in-memory built-in, custom for vector DBs like
Mongo Atlas Vector Search). Requires `@OptIn(ExperimentalAgentsApi::class)`.

```kotlin
@OptIn(ExperimentalAgentsApi::class)
install(LongTermMemory) {
    retrieval {
        storage = myVectorDbStorage
        searchStrategy = SimilaritySearchStrategy(topK = 3, similarityThreshold = 0.7)
        promptAugmenter = SystemPromptAugmenter()
    }
}
```

For the full picture — retrieval pipeline (query extractors / search strategies / prompt
augmenters), ingestion pipeline (extraction strategies / timing), operational modes, custom
vector-DB storage, manual-mode access via `withLongTermMemory { }`, and Peekopals-specific
guidance — see [long-term-memory.md](long-term-memory.md).

---

## Custom features

Implement the feature interfaces:

```kotlin
class MyFeature : AIAgentGraphFeature, AIAgentFunctionalFeature {
    // Lifecycle interceptors for:
    // - Agent lifecycle (start, complete, fail)
    // - Strategy lifecycle
    // - LLM operations (request, response)
    // - Tool operations (call, result)
    // - Graph-specific events (node start, edge traversal)
}
```

Install custom features:

```kotlin
install(MyFeature) {
    configProperty = "value"
}
```
