# Reaptive project KOOG conventions

How KOOG is used in this specific codebase. Follow these patterns when creating new KOOG pipelines.

Source: `server/src/main/kotlin/llms/koog/` and
`server/src/main/kotlin/marketing/peekoPals/aiCharacters/createStoryArcsWithKoog/`

---

## Dependency

```kotlin
// server/build.gradle.kts
implementation("ai.koog:koog-agents:0.7.3")
```

Single dependency — `koog-agents` bundles core, tools, prompt DSL, features, snapshot, and
OpenTelemetry modules.

---

## LLM providers

MiniMax is the primary LLM, accessed via KOOG's `OpenAILLMClient` with a custom `baseUrl`:

```kotlin
private val miniMaxClient by lazy {
    OpenAILLMClient(
        apiKey = MINIMAX_TOKEN_PLAN_KEY,
        settings = OpenAIClientSettings(baseUrl = "https://api.minimax.io")
    )
}

internal val minMaxKoogExecutor: MultiLLMPromptExecutor by lazy {
    ThinkStrippingMultiLLMPromptExecutor(mapOf(LLMProvider.OpenAI to miniMaxClient))
}
```

**Important**: MiniMax emits `<think>...</think>` reasoning tags.
`ThinkStrippingMultiLLMPromptExecutor` strips these at the executor boundary. Always use
`minMaxKoogExecutor` or `defaultKoogExecutor` (same thing) for MiniMax calls.

### LLModel capabilities

When defining an `LLModel` for any OpenAI-compatible provider, always declare both capabilities:

```kotlin
val llmModel = LLModel(
    provider = LLMProvider.OpenAI,
    id = KOOG_MINIMAX_MODEL, // "MiniMax-M2.7-highspeed"
    capabilities = listOf(
        LLMCapability.Completion,
        LLMCapability.OpenAIEndpoint.Completions,
    ),
)
```

Add `LLMCapability.Tools` when the agent uses tools:

```kotlin
capabilities = listOf(
    LLMCapability.Completion,
    LLMCapability.OpenAIEndpoint.Completions,
    LLMCapability.Tools,
)
```

---

## Two execution modes

### 1. SkillRunner — for skill-based sub-agent calls

`SkillRunner` (in `llms/koog/SkillRunner.kt`) loads a skill markdown file from the classpath and
runs it as the system prompt of a fresh `AIAgent`.

**Prose output** (intermediate text):

```kotlin
val output = SkillRunner.run(
    skillFileName = "peekopals-story-continuity.md",
    userInput = userPrompt,
)
```

**Structured JSON output** (typed Kotlin object):

```kotlin
val result = SkillRunner.runJsonStructured(
    skillFileName = "peekopals-daily-story-arc.md",
    userInput = userPrompt,
    serializer = StoriesJsonResponse.serializer(),
)
```

Skill files live in `server/src/main/kotlin/marketing/peekoPals/peekopals-skills/`,
`server/src/main/kotlin/marketing/astroNavya/skills/`, or `server/src/main/kotlin/marketing/skills/`
and are packaged into the JAR by `processResources`.

### 2. Graph-based pipeline — for multi-phase orchestration

Use `AIAgent` with a `strategy` (Koog graph) for pipelines with multiple phases. See
StoryArcPipeline.kt for the canonical example.

---

## Graph pipeline anatomy

Every pipeline follows this 5-file pattern:

| File                       | Role                                                                                                     |
|----------------------------|----------------------------------------------------------------------------------------------------------|
| `XxxPipeline.kt`           | Entry point, AIAgent construction, graph strategy (`buildXxxStrategy()`), helper functions               |
| `XxxPipelineModels.kt`     | `XxxPipelineState` accumulator, phase I/O types, terminal `XxxPipelineResult` sealed class               |
| `XxxPipelineSkills.kt`     | `object` with one `suspend fun runX(...)` per phase. Private `*JsonResponse` DTOs for LLM-facing schemas |
| `XxxPipelineDataAccess.kt` | MongoDB reads/writes. Pure Kotlin, pre-graph where possible                                              |
| `RunXxxPipelineRoute.kt`   | Ktor POST route that triggers the pipeline. Responds 200 immediately, runs in background                 |

### State accumulator pattern

One `@Serializable data class` threaded through every node. Nullable fields populated as phases
complete:

```kotlin
@Serializable
data class StoryArcPipelineState(
    val recentStoryArcs: List<StoryArc>,
    val storyContinuity: StoryContinuitySummary? = null,
    val characterStoryArcs: CharacterStoryArcs,
    val storyReview: StoryReview? = null,
    val postRotation: PeekoPalsStoryPostRotation? = null,
    val postAllocation: PostAllocation? = null,
)
```

### Graph strategy

```kotlin
internal fun buildStoryArcPipelineStrategy(): AIAgentGraphStrategy<State, Result> =
    strategy("peekopals-nightly-story-arc") {

        val nodeContinuity by node<State, State> { state ->
            val result = Skills.runPhaseX(state.xxx)
            state.copy(xxx = result)
        }

        val nodeStories by node<State, State> { state ->
            val prev = state.xxx ?: error("Phase 2 requires Phase 1")
            val result = Skills.runPhaseY(prev)
            state.copy(yyy = result)
        }

        // Linear edges
        edge(nodeStart forwardTo nodeContinuity)
        edge(nodeContinuity forwardTo nodeStories)
        edge(nodeStories forwardTo nodeFinish transformed { state ->
            Result.Success(...) as Result
        })
    }
```

### Agent construction with features

```kotlin
val agent = AIAgent(
    promptExecutor = minMaxKoogExecutor,
    llmModel = llmModel,
    strategy = buildXxxStrategy(),
    systemPrompt = null,
    maxIterations = 20,
) {
    install(Persistence) {
        storage = JVMFilePersistenceStorageProvider(checkpointDir)
        enableAutomaticPersistence = true
    }
    install(Tracing) {
        addMessageProcessor(TraceFeatureMessageLogWriter(tracingLogger))
    }
    // Optional Langfuse
    if (System.getenv("LANGFUSE_PUBLIC_KEY") != null &&
        System.getenv("LANGFUSE_SECRET_KEY") != null
    ) {
        install(OpenTelemetry) {
            setServiceInfo("pipeline-name", "0.1.0")
            addLangfuseExporter()
        }
    }
}
```

### Deterministic session IDs

Session IDs are derived from the pipeline's logical key (date, post ID, etc.) so that:

- Two runs targeting the same key share checkpoints (crash recovery)
- Different keys get independent checkpoint chains

```kotlin
fun sessionIdFor(targetDateMillis: Long): String =
    "peekopals-nightly-${formatEpochAsUtcDate(targetDateMillis)}"
```

---

## Structured output pattern

### Private JsonResponse DTOs

Each phase that returns structured JSON has a private `*JsonResponse` data class annotated with
`@Serializable` and `@LLMDescription`:

```kotlin
@Serializable
@LLMDescription("Phase 3 output: pass/fail review of the daily story arcs.")
private data class ReviewJsonResponse(
    @property:LLMDescription("true if all quality checks pass.")
    val passed: Boolean,
    @property:LLMDescription("If passed is false, a list of failure reasons.")
    val issues: List<String> = emptyList(),
)
```

Key rules:

- `@LLMDescription` on the class = overall description for the LLM
- `@property:LLMDescription` on each field = field-level description (used in the auto-generated
  JSON schema)
- These DTOs are **private** to the Skills file — public methods map to shared domain types before
  returning
- Koog auto-generates a JSON schema from the `KSerializer` and appends it to the system prompt

### Semantic retry with withSemanticRetry

Beyond JSON parse failures (handled by Koog's `StructureFixingParser`), use `withSemanticRetry` for
domain-level validation:

```kotlin
val parsed = withSemanticRetry(
    phase = "Phase 2 (runDailyStoryArc)",
    maxAttempts = KOOG_FAILURE_MAX_ATTEMPTS, // 5
    buildUserPrompt = { previousFailure -> appendFailureFeedback(basePrompt, previousFailure) },
) { userPrompt ->
    val response = SkillRunner.runJsonStructured(
        skillFileName = "peekopals-daily-story-arc.md",
        userInput = userPrompt,
        serializer = StoriesJsonResponse.serializer(),
    )
    // Domain-level validation — throws if violated, error message fed back to LLM
    require(response.characters.size == 6) { "Must return exactly 6 characters" }
    response
}
```

The retry loop:

1. Builds the user prompt (base prompt + optional failure feedback)
2. Calls `SkillRunner.runJsonStructured` (Koog handles JSON parse retries internally)
3. Runs `require(...)` checks — if they throw, the error message is appended to the next attempt's
   prompt
4. Up to `KOOG_FAILURE_MAX_ATTEMPTS` total attempts

---

## Tool creation pattern

Use `ToolSet` with `@Tool` and `@LLMDescription` annotations:

```kotlin
@LLMDescription("Tools for searching the web")
object WebSearchToolSet : ToolSet {

    @Tool(customName = "search_web")
    @LLMDescription("Search the internet for current information.")
    suspend fun searchWeb(
        @LLMDescription("The search query string")
        query: String
    ): String {
        ...
    }
}
```

Register tools in a `ToolRegistry`:

```kotlin
val toolRegistry = ToolRegistry {
    tools(WebSearchToolSet)
}
```

Pass to the agent:

```kotlin
val agent = AIAgent(
    promptExecutor = executor,
    systemPrompt = "...",
    llmModel = llmModel,
    toolRegistry = toolRegistry,
    maxIterations = 10,
)
```

---

## MiniMax-specific gotchas

- **`<think>` tags**: Always use `minMaxKoogExecutor` (strips reasoning tags automatically)
- **`n=1` only**: MiniMax does not support `LLMCapability.MultipleChoices`
- **Temperature range**: Must be (0.0, 1.0] — values outside this range will error
- **No image/audio input**: MiniMax doesn't support multimodal via the OpenAI-compatible API
- **Ignored parameters**: `presence_penalty`, `frequency_penalty`, `logit_bias` are silently ignored

---

## Naming conventions

- Pipeline entry: `runXxxPipeline()` suspend function
- Strategy builder: `buildXxxPipelineStrategy()` internal function
- Session ID: `xxxSessionIdFor(key)` function
- Skills object: `XxxPipelineSkills` object
- Data access object: `XxxPipelineDataAccess` object
- State class: `XxxPipelineState` data class
- Result class: `XxxPipelineResult` sealed class
- Phase nodes: `nodePhaseX` named with `by node<State, State> { ... }`
- Skill files: `kebab-case.md` in `peekopals-skills/`, `skills/`, or `astroNavya/skills/`
