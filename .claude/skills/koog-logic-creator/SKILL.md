---
name: koog-logic-creator
description: Expert Koog 1.x framework guidance for Kotlin ai.koog agents, graph DSL, tools, structured output, providers, memory/RAG, tracing, persistence, testing, migration, and project conventions.
metadata:
  tags: koog, jetbrains, ai-agents, kotlin, graph-dsl, tools, structured-output, koog-1
---

## When to use

Use this skill whenever you are:

- Creating a new KOOG-based pipeline or agent
- Writing or modifying code that imports `ai.koog.*` packages
- Defining graph strategies with nodes and edges
- Creating tools with `@Tool` and `@LLMDescription` annotations
- Working with structured output (`executeStructured`, `runJsonStructured`)
- Configuring KOOG features (Persistence, Tracing, OpenTelemetry)
- Implementing chat history / conversation memory (ChatMemory feature, session IDs, custom
  ChatHistoryProvider)
- Implementing cross-session RAG / vector-based memory (LongTermMemory feature, retrieval +
  ingestion pipelines, custom vector storage)
- Setting up LLM providers or prompt executors
- Migrating code from Koog 0.x or 1.0 preview APIs to Koog 1.x stable APIs
- Debugging KOOG-related issues

## KOOG at a glance

Koog is JetBrains' Kotlin Multiplatform framework for building AI agents. Key facts:

- **Official URL (JetBrains repo):** https://github.com/JetBrains/koog
- **Official releases (JetBrains):** https://github.com/JetBrains/koog/releases
- **Docs:** https://docs.koog.ai/ | **API reference:** https://api.koog.ai/ | **Issue
  tracker:** https://youtrack.jetbrains.com/issues/KG
- **License:** Apache License 2.0 | **Default branch:** `develop` | **Homepage:** https://docs.koog.ai
- **Latest stable release:** `1.0.0` (2026-05-21) — first stable release; stable/beta module split;
  graph DSL names finalized; deprecated APIs removed; Java interop redesigned; HTTP transport
  decoupled from Ktor; OpenTelemetry expanded to Kotlin Multiplatform; Anthropic prompt caching
  added.
- **Dependency:** `implementation("ai.koog:koog-agents:1.0.0")` for the stable aggregate artifact.
- **Requirements:** JDK 17+, Kotlin 2.2.0+ in the official quickstart; this PeekoPals checkout
  currently pins Kotlin 2.3.10 and Koog 1.0.0.
- **Supported targets:** JVM, JS, WasmJS, Android, iOS. Note that 1.0 release notes call out
  OpenTelemetry KMP support but not WasmJS for that feature yet.
- **Four agent types:** Basic, Functional, Graph-based, Planner (GOAP/LLM)
- **LLM providers:** OpenAI/Azure OpenAI, Anthropic, Google, DeepSeek, OpenRouter, Amazon Bedrock,
  Mistral, Alibaba/DashScope via OpenAI-compatible client, and Ollama.
- **This PeekoPals checkout uses:** MiniMax via OpenAI-compatible Koog clients, `koog-agents:1.0.0`,
  MCP via `agents-mcp:1.0.0-beta-preview7`, graph-based pipelines, ChatMemory providers, structured
  output, and Koog-backed content generation.

Always check https://github.com/JetBrains/koog/releases and the local version catalog/build files
before upgrading or giving exact API guidance. Koog moved quickly before 1.0, and this skill's
bundled docs are snapshots.

## Koog 1.0 migration notes

Use these rules when touching current Koog code:

- Prefer 1.0 stable APIs. Treat old 0.x examples as suspicious until verified against official
  docs/API reference.
- All deprecated APIs were removed in 1.0. If code references deprecated 0.x shims, replace them
  instead of suppressing warnings.
- Use `Persistence*` spellings, not old `Persistency*` spellings.
- Old pipeline types such as `AIAgentPipeline` / `AIAgentPipelineImpl` are gone. Current public
  surfaces use `AIAgentPipelineAPI`, `AIAgentGraphPipeline`, and `AIAgentPlannerPipeline`.
- The graph DSL names are settled: `String`-input nodes keep names like `nodeLLMRequest`,
  `nodeLLMRequestStructured`, and `nodeLLMRequestStreaming`; `Message.User`-input variants use
  `nodeLLMSendMessage*`.
- `nodeExecuteTools` returns `ReceivedToolResults`; wire it into `nodeLLMSendMessage` /
  `nodeLLMSendToolResults*` instead of assuming the old auto-writeback behavior.
- `ai.koog.prompt.dsl.Prompt` moved to `ai.koog.prompt.Prompt`; keep the `prompt { ... }` builder
  import from `ai.koog.prompt.dsl`.
- `AgentMemory` was removed in favor of `LongTermMemory`. For fact/history compression, use current
  LongTermMemory and history compression docs; treat `docs/agent-memory.md` as legacy reference
  only.
- Planner APIs moved to a dedicated planner module. Do not assume GOAP/LLM planner classes are
  present from only `koog-agents` unless the local build includes the planner artifact.
- LLM clients now use `KoogHttpClient.Factory` instead of assuming Ktor's `HttpClient` directly. In
  this repo, inspect the MiniMax/OpenAI-compatible helper before changing transport code.

## How to use

Read individual docs files for detailed explanations and code examples:

### Official KOOG documentation

The files below are local snapshots. Use them for orientation, then verify exact signatures
against https://docs.koog.ai/, https://api.koog.ai/, and https://github.com/JetBrains/koog/releases
for Koog 1.x work.

- [docs/overview.md](docs/overview.md) - KOOG overview, module structure, quick start, agent types,
  LLM providers
- [docs/graph-based-agents.md](docs/graph-based-agents.md) - Graph strategies, nodes, edges,
  pipeline patterns, parallel execution, subgraphs
- [docs/strategies.md](docs/strategies.md) - All strategy types: basic, graph, functional, planner,
  predefined strategies
- [docs/tools.md](docs/tools.md) - Creating tools (@Tool, ToolSet, SimpleTool), tool registry,
  agents-as-tools, built-in tools
- [docs/structured-output.md](docs/structured-output.md) - Typed JSON responses, @LLMDescription
  schemas, StructureFixingParser, three API layers
- [docs/features.md](docs/features.md) - Persistence/checkpointing, Tracing, OpenTelemetry (Langfuse/Weave/Datadog),
  event handlers, memory, chat memory
- [docs/chat-memory.md](docs/chat-memory.md) - Chat memory deep dive: session IDs,
  ChatHistoryProvider implementations, preprocessors, chat agent (CLI) and chat backend (Ktor)
  patterns, Peekopals-specific guidance
- [docs/agent-memory.md](docs/agent-memory.md) - Legacy AgentMemory reference only; do not use for
  new Koog 1.x work unless maintaining old pinned code.
- [docs/long-term-memory.md](docs/long-term-memory.md) - Long-term memory (RAG) deep dive: retrieval
  pipeline (query extractors, search strategies, prompt augmenters), ingestion pipeline (extraction
  strategies, timing), operational modes, custom vector storage, manual access via
  withLongTermMemory
- [docs/prompt-dsl.md](docs/prompt-dsl.md) - Prompt creation DSL, message types, LLM parameters,
  executors, failure handling, caching, history compression
- [docs/llm-providers.md](docs/llm-providers.md) - Provider setup, model constants, LLMCapability,
  custom/OpenAI-compatible providers
- [docs/mcp-and-integrations.md](docs/mcp-and-integrations.md) - MCP client/server, Ktor plugin,
  Spring Boot, A2A protocol
- [docs/testing.md](docs/testing.md) - Mock executors, tool mocking, graph structure assertions

### This project's conventions

- [docs/reaptive-conventions.md](docs/reaptive-conventions.md) - Older project convention snapshot.
  Reuse only the general patterns that match the current checkout after inspecting local code.

Before giving project-specific advice, inspect the active checkout:

- version catalog: `gradle/libs.versions.toml`
- Koog helpers: `server/src/main/kotlin/**/koog/**`
- runtime skill/resource loading: `SkillRunner.kt`
- package-local `CLAUDE.md` / `AGENTS.md` notes

## Key patterns in this project

### For simple LLM calls (no graph)

Use `KoogAgent` or `SkillRunner`:

```kotlin
// Direct LLM call
val response = KoogAgent.run("Your prompt", systemPrompt = "You are...")

// Skill-based call (markdown file as system prompt)
val output = SkillRunner.run("skill-file.md", userInput = "...")

// Structured JSON output
val result = SkillRunner.runJsonStructured("skill.md", userInput, MyType.serializer())
```

### For multi-phase pipelines (graph)

Follow the local feature/pipeline pattern already present in the active checkout. A common shape is:

1. `XxxPipeline.kt` - entry point + graph strategy
2. `XxxPipelineModels.kt` - state accumulator + result sealed class
3. `XxxPipelineSkills.kt` - one `suspend fun` per phase
4. `XxxPipelineDataAccess.kt` - MongoDB reads/writes
5. `RunXxxPipelineRoute.kt` - Ktor POST route

### For structured output with validation

Always pair `SkillRunner.runJsonStructured` with `withSemanticRetry`:

```kotlin
val parsed = withSemanticRetry(
    phase = "Phase N (description)",
    maxAttempts = KOOG_FAILURE_MAX_ATTEMPTS,
    buildUserPrompt = { prev -> appendFailureFeedback(basePrompt, prev) },
) { userPrompt ->
    val response = SkillRunner.runJsonStructured(
        skillFileName = "my-skill.md",
        userInput = userPrompt,
        serializer = MyJsonResponse.serializer(),
    )
    require(response.field.isNotBlank()) { "field must not be blank" }
    response
}
```

## Quick reference: common imports

```kotlin
// Core agent
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*

// Tools
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

// Prompts
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.StructureFixingParser
import ai.koog.prompt.executor.model.executeStructured

// LLM providers
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

// Features
import ai.koog.agents.snapshot.feature.Persistence
import ai.koog.agents.snapshot.providers.file.JVMFilePersistenceStorageProvider
import ai.koog.agents.features.tracing.feature.Tracing
import ai.koog.agents.features.tracing.writer.TraceFeatureMessageLogWriter
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry

// Messages
import ai.koog.prompt.message.Message
```

When imports do not resolve, do not guess from this quick reference. Check the installed Koog
version, then use the matching API reference or the code already compiling in the project.
