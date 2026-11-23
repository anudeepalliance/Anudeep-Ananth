# KOOG Long-Term Memory (RAG)

Source: https://docs.koog.ai/features/long-term-memory/

Module: `agents-features-longterm-memory`

> **Experimental.** Requires `@OptIn(ExperimentalAgentsApi::class)` on every install/use site. The
> API may shift between releases.

---

## 1. Overview

Long-Term Memory (LTM) gives an agent **persistent, RAG-style memory across sessions**: relevant
prior context is retrieved and injected into the prompt before each LLM call, and new conversation
content is ingested into storage for future retrieval.

It is built around two **independent** pipelines:

| Pipeline      | What it does                                                                                | When it runs                                                 |
|---------------|---------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| **Retrieval** | Searches storage for context relevant to the current prompt and augments the prompt with it | Before LLM calls                                             |
| **Ingestion** | Extracts records from the conversation and persists them                                    | At configurable points (per-LLM-call or at agent completion) |

You can install only one, only the other, or both. They don't have to share a storage backend.

### LTM vs. ChatMemory vs. AgentMemory

| Feature                                            | What it stores                              | Retrieval style            | Use case                                                                        |
|----------------------------------------------------|---------------------------------------------|----------------------------|---------------------------------------------------------------------------------|
| `ChatMemory` ([chat-memory.md](chat-memory.md))    | Raw message history per session id          | Replay last N messages     | Multi-turn chat continuity                                                      |
| `AgentMemory` ([agent-memory.md](agent-memory.md)) | Typed Facts under Concept × Subject × Scope | Direct lookup by concept   | "Remember user prefers Kotlin" — explicit, structured                           |
| **`LongTermMemory`**                               | Free-form `MemoryRecord`s in a vector store | Semantic similarity search | "Three weeks ago they mentioned hating mornings — bring it up" — episodic / RAG |

**For Peekopals:** LTM is the right tool when a character needs to recall *episodes* across many
sessions — not the user's name (that's `AgentMemory`) and not the last 20 turns (that's
`ChatMemory`), but "the time they vented about their sister" surfacing when an emotionally adjacent
topic comes up later.

---

## 2. Operational Modes

Three deployment shapes, all configurable via the same `install(LongTermMemory) { … }` block:

| Mode                         | Retrieval                   | Ingestion              | Typical use                                                       |
|------------------------------|-----------------------------|------------------------|-------------------------------------------------------------------|
| **Full Automatic** (default) | Auto, before every LLM call | Auto, at chosen timing | Standard "give my chatbot a memory"                               |
| **Manual Only**              | Disabled                    | Disabled               | Pre-populated knowledge base, queried explicitly from nodes       |
| **Hybrid**                   | One auto, one manual        | One auto, one manual   | E.g. ingest automatically, retrieve manually inside a custom node |

Disable automatic behavior with the per-pipeline flags:

```kotlin
@OptIn(ExperimentalAgentsApi::class)
install(LongTermMemory) {
    retrieval {
        storage = myStorage
        enableAutomaticRetrieval = false
    }
    ingestion {
        storage = myStorage
        enableAutomaticIngestion = false
    }
}
```

---

## 3. Retrieval Pipeline

Retrieval has three replaceable stages: **Query Extractor → Search Strategy → Prompt Augmenter**.

### Query Extractor

Decides what text to use as the search query. Default uses the last user message.

```kotlin
@OptIn(ExperimentalAgentsApi::class)
install(LongTermMemory) {
    retrieval {
        storage = myStorage
        queryExtractor = QueryExtractor { prompt ->
            prompt.messages
                .filter { it.role == Message.Role.User }
                .takeLast(2)
                .joinToString(" ") { it.content }
                .ifEmpty { null }
        }
    }
}
```

Built-ins:

- `LastUserMessageQueryExtractor()` — default; uses the final user message
- `QueryExtractor { prompt -> … }` — custom lambda; return `null` to skip retrieval this turn

### Search Strategy

Decides how to find matching records.

```kotlin
@OptIn(ExperimentalAgentsApi::class)
install(LongTermMemory) {
    retrieval {
        storage = myVectorDbStorage
        searchStrategy = SimilaritySearchStrategy(topK = 3, similarityThreshold = 0.7)
    }
}
```

Built-ins:

- `SimilaritySearchStrategy(topK, similarityThreshold)` — semantic vector search; the default and
  the right choice for almost everything
- Custom lambda mapping `query -> SimilaritySearchRequest(...)` for hybrid retrieval, filters,
  namespace pinning, etc.

Tune `topK` and `similarityThreshold` together — too high a `topK` floods the prompt with marginal
hits, too high a `threshold` returns nothing on slightly-paraphrased queries.

### Prompt Augmenter

Decides how retrieved context enters the prompt.

```kotlin
@OptIn(ExperimentalAgentsApi::class)
install(LongTermMemory) {
    retrieval {
        storage = myStorage
        promptAugmenter = SystemPromptAugmenter()
    }
}
```

Built-ins:

- `SystemPromptAugmenter()` — appends as a system message; treats memory as context the model should
  use silently
- `UserPromptAugmenter()` — inserts before the last user message; treats memory as if the user
  reminded the model
- `PromptAugmenter { prompt, context -> … }` — full custom control

Choose `SystemPromptAugmenter` for character apps where you don't want the model to acknowledge "I'm
reading from memory" — it just knows.

---

## 4. Ingestion Pipeline

Ingestion has two stages: **Extraction Strategy → Timing**.

### Extraction Strategy

Transforms messages into `MemoryRecord`s before persisting.

```kotlin
@OptIn(ExperimentalAgentsApi::class)
install(LongTermMemory) {
    ingestion {
        storage = myVectorDbStorage
        extractionStrategy = FilteringExtractionStrategy(
            messageRolesToExtract = setOf(Message.Role.User, Message.Role.Assistant),
        )
    }
}
```

Built-ins:

- `FilteringExtractionStrategy(messageRolesToExtract)` — keep messages with the listed roles,
  persist as-is

Custom strategy (e.g. summarize before storing — saves tokens at retrieval and improves embedding
quality):

```kotlin
@OptIn(ExperimentalAgentsApi::class)
val summarizingExtractor = ExtractionStrategy { messages ->
        messages
            .filter { it.role == Message.Role.Assistant }
            .map { MemoryRecord(content = summarize(it.content)) }
    }
```

### Timing

When to flush messages to storage:

```kotlin
@OptIn(ExperimentalAgentsApi::class)
install(LongTermMemory) {
    ingestion {
        storage = myVectorDbStorage
        timing = IngestionTiming.ON_LLM_CALL
    }
}
```

| Value                                 | Behavior                                   | When to choose                                                                              |
|---------------------------------------|--------------------------------------------|---------------------------------------------------------------------------------------------|
| `IngestionTiming.ON_LLM_CALL`         | Persist before every LLM call within a run | Enables intra-session RAG — earlier turns become retrievable later in the same conversation |
| `IngestionTiming.ON_AGENT_COMPLETION` | Persist once when the agent finishes       | Lower write volume; cross-session-only retrieval (no intra-session RAG)                     |

For a chat product where one "agent run" = one user turn, both timings effectively mean "save after
each turn" but `ON_LLM_CALL` also captures intermediate tool-using subturns.

---

## 5. Storage Backends

Storage is the same component used by both pipelines (or you can pass different backends to
retrieval and ingestion).

### Built-in: `InMemoryRecordStorage`

```kotlin
val myStorage = InMemoryRecordStorage()
```

Substring matching only — no real vector embeddings. Useful for local dev and tests; do **not** ship
to production.

### Custom storage (vector DB)

Implement `SearchStorage` and/or `WriteStorage`:

```kotlin
class MyVectorDbStorage : SearchStorage<TextDocument, SearchRequest>, WriteStorage<TextDocument> {
    override suspend fun search(
        request: SearchRequest,
        namespace: String?,
    ): List<SearchResult<TextDocument>> {
        // Query your vector DB (Pinecone, Qdrant, Weaviate, pgvector, Mongo Atlas Vector Search, …)
    }

    override suspend fun add(
        records: List<TextDocument>,
        namespace: String?,
    ) {
        // Upsert into your vector DB
    }
}
```

**Peekopals fit:** the server already runs MongoDB. **MongoDB Atlas Vector Search** is the path of
least resistance — one collection per character (or one collection with a `characterId` field used
as namespace), embeddings generated via the existing LLM provider's embedding endpoint, written and
queried through this interface.

### Namespaces

Every storage call accepts an optional `namespace`. Use it to scope records — e.g. one namespace per
`(userId, characterId)` so memory never bleeds between characters or across users.

```kotlin
install(LongTermMemory) {
    retrieval {
        storage = myVectorDbStorage
        namespace = "${userId}-${characterId}"
        searchStrategy = SimilaritySearchStrategy(topK = 3, similarityThreshold = 0.7)
        promptAugmenter = SystemPromptAugmenter()
    }
}
```

If you need a per-request namespace (typical for multi-tenant chat backends), the cleaner shape is **manual mode** + a
custom node that reads the namespace off the request and calls
`withLongTermMemory { … }` directly.

---

## 6. Complete Setup Examples

### Retrieval-only (pre-populated knowledge base)

```kotlin
@OptIn(ExperimentalAgentsApi::class)
val agent = AIAgent(
        promptExecutor = executor,
        strategy = singleRunStrategy(),
        agentConfig = agentConfig,
        toolRegistry = ToolRegistry.EMPTY,
    ) {
        install(LongTermMemory) {
            retrieval {
                storage = myVectorDbStorage
                namespace = "my-collection"
                searchStrategy = SimilaritySearchStrategy(topK = 3, similarityThreshold = 0.7)
                promptAugmenter = SystemPromptAugmenter()
            }
        }
    }

agent.run("What did we discuss yesterday?")
```

Use this shape for: docs Q&A, character lore lookup, FAQ-style assistants where the corpus is loaded
out-of-band and the agent never writes back.

### Ingestion-only (build memory over time, no auto-retrieval yet)

```kotlin
@OptIn(ExperimentalAgentsApi::class)
install(LongTermMemory) {
    ingestion {
        storage = myVectorDbStorage
        namespace = "my-collection"
        extractionStrategy = FilteringExtractionStrategy(
            messageRolesToExtract = setOf(Message.Role.User, Message.Role.Assistant),
        )
        timing = IngestionTiming.ON_LLM_CALL
    }
}
```

Use this when you want to **start collecting** before you've decided on retrieval mechanics — useful
for warm-up periods.

### Both (full RAG loop)

Combine the two `install` blocks above pointing at the same storage. This is the default
chat-with-long-term-memory shape.

---

## 7. Manual Access from Strategy Nodes

For full control — e.g. retrieving with a request-derived namespace, or writing only on certain
conditions — disable automatic mode and use the `withLongTermMemory { }` DSL inside a node.

```kotlin
@OptIn(ExperimentalAgentsApi::class)
val myNode by node<String, Unit> {
    withLongTermMemory {
        // Manually add records
        val record = MemoryRecord(content = "important fact")
        ingestionStorage?.add(listOf(record), namespace = "my-namespace")

        // Manually search
        val request = SimilaritySearchRequest(queryText = input, limit = 5)
        val results = retrievalStorage?.search(request, namespace = "my-namespace")
    }
}
```

For direct feature access (e.g. passing the storage handle to a helper):

```kotlin
@OptIn(ExperimentalAgentsApi::class)
val myNode by node<String, Unit> {
    val memory = longTermMemory()
    val storage = memory.ingestionStorage
    // …
}
```

`ingestionStorage` and `retrievalStorage` are nullable because either pipeline may be uninstalled —
null-check before use.

---

## 8. Best Practices

1. **Pick one storage backend, namespace it well.** Per- (user, character) namespacing prevents
   cross-contamination without doubling your infrastructure.
2. **Don't ship `InMemoryRecordStorage`.** It does substring matching, not embeddings, and dies on
   restart.
3. **Tune `topK` and `similarityThreshold` together.** Start at `topK=3, threshold=0.7` and adjust
   based on prompt-window budget and observed recall.
4. **Prefer `SystemPromptAugmenter` for character apps.** The model uses memory silently rather than
   narrating "I remember you said…".
5. **Summarize before ingesting** when conversations are long. A custom `ExtractionStrategy` that
   runs `summarize()` shrinks vector counts and improves match quality.
6. **Use `ON_LLM_CALL` for intra-session RAG, `ON_AGENT_COMPLETION` to minimize writes.** Match to
   your turn shape.
7. **Combine with `ChatMemory` and `AgentMemory`.** They cover different timescales and shapes —
   short-term continuity, structured facts, episodic recall. A real chat product usually needs all
   three.
8. **Treat the API as experimental.** Pin your `koog-agents` version, watch the changelog before
   bumping.

---

## 9. Java configuration example

For server code that's still in Java:

```java
InMemoryRecordStorage myStorage = new InMemoryRecordStorage();

AIAgent agent = AIAgent.builder()
        .promptExecutor(executor)
        .llmModel(OpenAIModels.Chat.GPT4o)
        .systemPrompt("You are a helpful assistant.")
        .install(LongTermMemory.Feature, config -> {
            config.retrieval(
                    new LongTermMemory.RetrievalSettingsBuilder()
                            .withStorage(myStorage)
                            .withSearchStrategy(
                                    SearchStrategy.builder().similarity().withTopK(5).build()
                            )
                            .build()
            );
        })
        .build();

Object result = agent.run("What did we discuss yesterday?");
```

Peekopals' server is Kotlin throughout, so this is mostly reference material.

---

## 10. Quick-reference imports

```kotlin
import ai.koog.agents.features.longtermmemory.feature.LongTermMemory
import ai.koog.agents.features.longtermmemory.feature.longTermMemory
import ai.koog.agents.features.longtermmemory.feature.withLongTermMemory

// Retrieval
import ai.koog.agents.features.longtermmemory.retrieval.QueryExtractor
import ai.koog.agents.features.longtermmemory.retrieval.LastUserMessageQueryExtractor
import ai.koog.agents.features.longtermmemory.retrieval.SearchStrategy
import ai.koog.agents.features.longtermmemory.retrieval.SimilaritySearchStrategy
import ai.koog.agents.features.longtermmemory.retrieval.PromptAugmenter
import ai.koog.agents.features.longtermmemory.retrieval.SystemPromptAugmenter
import ai.koog.agents.features.longtermmemory.retrieval.UserPromptAugmenter

// Ingestion
import ai.koog.agents.features.longtermmemory.ingestion.ExtractionStrategy
import ai.koog.agents.features.longtermmemory.ingestion.FilteringExtractionStrategy
import ai.koog.agents.features.longtermmemory.ingestion.IngestionTiming

// Storage
import ai.koog.agents.features.longtermmemory.storage.MemoryRecord
import ai.koog.agents.features.longtermmemory.storage.InMemoryRecordStorage
import ai.koog.agents.features.longtermmemory.storage.SearchStorage
import ai.koog.agents.features.longtermmemory.storage.WriteStorage
import ai.koog.agents.features.longtermmemory.storage.SearchRequest
import ai.koog.agents.features.longtermmemory.storage.SimilaritySearchRequest
import ai.koog.agents.features.longtermmemory.storage.SearchResult

// Opt-in
import ai.koog.agents.core.annotations.ExperimentalAgentsApi
```
