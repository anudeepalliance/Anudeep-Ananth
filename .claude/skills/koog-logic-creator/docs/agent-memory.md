# KOOG Agent Memory

Source: https://docs.koog.ai/features/agent-memory/

Module: `ai.koog:agents-features-memory`

---

## 1. Overview

`AgentMemory` is a structured, hierarchical memory system that lets an agent **store, retrieve, and
reuse facts across conversations**. Unlike `ChatMemory` (which persists raw message history per
session — see [chat-memory.md](chat-memory.md)), `AgentMemory` persists **typed pieces of
knowledge** organized by concept, subject, and scope.

Use it when the agent needs to remember things *about the world* — the user's name, their preferred
language, the operating system they're on, their writing style, their relationship with a
character — independent of any specific chat session.

### When to reach for `AgentMemory` vs. `ChatMemory`

| Need                                                                            | Feature       |
|---------------------------------------------------------------------------------|---------------|
| Continue a conversation across runs                                             | `ChatMemory`  |
| "Remember the user's name is John" forever                                      | `AgentMemory` |
| Cross-session preferences (theme, language, tone)                               | `AgentMemory` |
| Per-character relationship state (Peekopals: trust level, last topic, in-jokes) | `AgentMemory` |
| Multi-turn coherence within a single dialogue                                   | `ChatMemory`  |

They compose — a chat agent typically installs both.

---

## 2. Core Data Model

`AgentMemory` organizes information into four hierarchical primitives.

### Facts

A `Fact` is a piece of stored knowledge. Two flavors:

**`SingleFact`** — one value per concept:

```kotlin
val themeFact = SingleFact(
    concept = Concept("ide-theme", "User's preferred IDE theme", FactType.SINGLE),
    value = "Dark Theme",
    timestamp = Clock.System.now().toEpochMilliseconds(),
)
```

**`MultipleFacts`** — a list of values:

```kotlin
val languagesFact = MultipleFacts(
    concept = Concept("programming-languages", "Languages the user knows", FactType.MULTIPLE),
    values = listOf("Kotlin", "Java", "Python"),
    timestamp = Clock.System.now().toEpochMilliseconds(),
)
```

### Concepts

A `Concept` describes *what kind of knowledge* a fact holds:

- **keyword** — unique identifier (e.g. `"preferred-language"`)
- **description** — human-readable explanation; the LLM uses this when auto-detecting facts
- **factType** — `FactType.SINGLE` or `FactType.MULTIPLE`

Treat the description as a prompt — write it as a question or noun phrase the LLM can match against
conversation, e.g. `"What programming language is preferred by the user?"`.

### Subjects

A `MemorySubject` is *who or what* the fact is about. Built-in examples:

```kotlin
data object Machine : MemorySubject() {
    override val name: String = "machine"
    override val promptDescription: String =
        "Technical environment (installed tools, SDKs, OS, etc.)"
    override val priorityLevel: Int = 1
}

data object User : MemorySubject() {
    override val name: String = "user"
    override val promptDescription: String =
        "User information (preferences, history, contact details, etc.)"
    override val priorityLevel: Int = 1
}
```

`MemorySubject.Everything` is also available as a default catch-all.

Define custom subjects by extending `MemorySubject` — for Peekopals, candidates include `Character`
(per-AI-character state), `Relationship` (user-character bond), or `Conversation` (per-thread mood).

`priorityLevel` controls retrieval ordering when multiple subjects produce facts about the same
concept — higher priority wins.

### Scopes

`MemoryScope` controls *how broadly* a fact applies:

| Scope                           | Meaning                      |
|---------------------------------|------------------------------|
| `MemoryScope.Agent`             | This agent only              |
| `MemoryScope.Feature`           | All agents in this feature   |
| `MemoryScope.Product("my-app")` | All features in this product |
| `MemoryScope.CrossProduct`      | Across products              |

For Peekopals, `MemoryScope.Product("peekopals")` is the right default — facts about a user persist
whether they chat with character A or B.

---

## 3. Installing the Feature

```kotlin
import ai.koog.agents.local.memory.feature.AgentMemory

val agent = AIAgent(
    promptExecutor = executor,
    llmModel = OpenAIModels.Chat.GPT4oMini,
) {
    install(AgentMemory) {
        memoryProvider = memoryProvider           // see Section 4
        agentName = "character-chat-agent"
        featureName = "ai-companion"
        organizationName = "peekopals"
        productName = "peekopals"
    }
}
```

The four name fields are not cosmetic — they're how scopes resolve. Pick stable identifiers and
don't rename them after data has been written, or you'll orphan stored facts.

---

## 4. Storage Providers

### Built-in: `LocalFileMemoryProvider`

```kotlin
import ai.koog.agents.local.memory.providers.LocalFileMemoryProvider
import ai.koog.agents.local.memory.providers.LocalMemoryConfig
import ai.koog.agents.local.memory.storage.SimpleStorage
import kotlinx.io.files.Path

val memoryProvider = LocalFileMemoryProvider(
    config = LocalMemoryConfig("customer-support-memory"),
    storage = SimpleStorage(JVMFileSystemProvider.ReadWrite),
    fs = JVMFileSystemProvider.ReadWrite,
    root = Path("path/to/memory/root"),
)
```

Fine for local dev and single-server deployments. For Peekopals's distributed Ktor + Mongo backend,
you'll want a custom provider (next).

### Encryption

Wrap the storage layer to encrypt-at-rest:

```kotlin
val secureStorage = EncryptedStorage(
    fs = JVMFileSystemProvider.ReadWrite,
    encryption = Aes256GCMEncryptor("your-secret-key"),
)
```

Use whenever facts may contain PII or anything you wouldn't want leaking from a stolen disk.

### Custom provider (production / multi-instance)

Implement `AgentMemoryProvider`:

```kotlin
class MyCustomMemoryProvider : AgentMemoryProvider {
    override suspend fun save(fact: Fact, subject: MemorySubject, scope: MemoryScope) {
        // persist (fact, subject, scope) to your store
    }

    override suspend fun load(
        concept: Concept,
        subject: MemorySubject,
        scope: MemoryScope
    ): List<Fact> {
        // fetch facts matching this exact concept
    }

    override suspend fun loadAll(subject: MemorySubject, scope: MemoryScope): List<Fact> {
        // fetch all facts for this subject + scope
    }

    override suspend fun loadByDescription(
        description: String,
        subject: MemorySubject,
        scope: MemoryScope,
    ): List<Fact> {
        // fuzzy/semantic lookup by concept description
    }
}
```

**Peekopals fit:** a `MongoAgentMemoryProvider` with one `agent_memory` collection. Document key =
`(subject, scope, conceptKeyword)`, document body = serialized `Fact`. Reuse the existing Mongo
patterns from `XxxPipelineDataAccess.kt`. Index on `(subject, scope)` for `loadAll` performance.

`NoMemory` is the default no-op provider — useful when you want to install the feature for code
uniformity but disable persistence (e.g. in tests).

---

## 5. Direct Save / Load (outside the graph)

When you have a memory provider in hand, you can read and write directly without going through the
agent graph:

```kotlin
// Save
memoryProvider.save(
    fact = SingleFact(
        concept = Concept("greeting", "User's name", FactType.SINGLE),
        value = "John",
        timestamp = Clock.System.now().toEpochMilliseconds(),
    ),
    subject = MemorySubjects.User,
    scope = MemoryScope.Product("my-app"),
)

// Load
val greeting = memoryProvider.load(
    concept = Concept("greeting", "User's name", FactType.SINGLE),
    subject = MemorySubjects.User,
    scope = MemoryScope.Product("my-app"),
)

if (greeting.isNotEmpty()) {
    println("Welcome back, ${greeting.first()}")
} else {
    println("Information not found. First time here?")
}
```

Always wrap external calls in error handling — the underlying store may be unavailable:

```kotlin
try {
    memoryProvider.save(fact, subject, scope)
} catch (e: Exception) {
    logger.warn { "Couldn't save fact: ${e.message}" }
}
```

---

## 6. Predefined Memory Nodes

Use these inside graph strategies to wire memory into the pipeline. (See [graph-based-agents.md](graph-based-agents.md)
for the strategy/node DSL itself.)

| Node                              | Behavior                                                                                                              |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `nodeLoadAllFactsFromMemory`      | Loads every fact about the given subject(s) into the agent context                                                    |
| `nodeLoadFromMemory`              | Loads facts for one specific concept                                                                                  |
| `nodeSaveToMemory`                | Saves an explicitly built fact                                                                                        |
| `nodeSaveToMemoryAutoDetectFacts` | LLM scans the chat history, identifies fact-worthy info, picks concepts, and saves them — no manual fact construction |

### Example: load preferences then auto-extract new ones

```kotlin
val strategy = strategy("example-agent") {
    val detectFacts by nodeSaveToMemoryAutoDetectFacts<Unit>(
        subjects = listOf(MemorySubjects.User, MemorySubjects.Machine),
    )

    val loadPreferences by node<Unit, Unit> {
        withMemory {
            loadFactsToAgent(
                llm = llm,
                concept = Concept(
                    "user-preference",
                    "User's preferred programming language", FactType.SINGLE
                ),
                subjects = listOf(MemorySubjects.User),
            )
        }
    }

    edge(nodeStart forwardTo detectFacts)
    edge(detectFacts forwardTo loadPreferences)
    edge(loadPreferences forwardTo nodeFinish)
}
```

`nodeSaveToMemoryAutoDetectFacts` is powerful but expensive — it runs an LLM call to scan history.
Place it sparingly: typically once at the end of a conversation turn, not on every node.

---

## 7. Custom Memory Nodes

For finer control, build nodes with the `withMemory { }` DSL. It exposes `loadFactsToAgent` and
`saveFactsFromHistory`:

```kotlin
val loadProjectInfo by node<Unit, Unit> {
    withMemory {
        loadFactsToAgent(
            llm = llm,
            concept = Concept(
                "preferred-language",
                "What programming language is preferred?", FactType.SINGLE
            ),
        )
    }
}

val saveProjectInfo by node<Unit, Unit> {
    withMemory {
        saveFactsFromHistory(
            llm = llm,
            concept = Concept(
                "preferred-language",
                "What programming language is preferred?", FactType.SINGLE
            ),
            subject = MemorySubjects.User,
            scope = MemoryScope.Product("my-app"),
        )
    }
}
```

`saveFactsFromHistory` is the targeted version of auto-detection — same mechanism, but you tell it
exactly which concept to look for. Use this when you know the concepts in advance and want to avoid
the LLM-driven discovery cost of `nodeSaveToMemoryAutoDetectFacts`.

---

## 8. Edge Case Behavior

- **NoMemory provider** — default no-op so installing the feature is always safe
- **Priority-based loading** — when multiple subjects could supply a fact, higher `priorityLevel`
  wins
- **Scope filtering** — narrower scopes hide broader-scope facts unless you explicitly load wider
- **Timestamp tracking** — every fact carries a creation timestamp; use it for recency ordering
- **Type-aware handling** — `SingleFact` and `MultipleFacts` are stored and retrieved as their
  concrete types

---

## 9. Best Practices

1. **Start with `LocalFileMemoryProvider`, swap to a custom provider only when you have multiple
   instances.** Don't over-engineer storage upfront.
2. **Default to `SingleFact` until you genuinely need a list.** It's easier to reason about and
   easier to update.
3. **Treat `Concept.description` as an LLM prompt.** Write it as a clear question or descriptive
   noun phrase — the auto-detection node uses it directly.
4. **Group related facts under one subject.** Don't fragment user preferences across `User`,
   `UserPrefs`, and `UserSettings` — pick one and stick with it.
5. **Wrap save/load in try/catch.** Storage failures shouldn't crash a chat turn; log and degrade
   gracefully.
6. **Use `nodeSaveToMemoryAutoDetectFacts` sparingly.** It's an LLM call. Use it once per turn at
   most, or use the targeted `saveFactsFromHistory` when you know what to extract.
7. **Encrypt when storing PII.** `Aes256GCMEncryptor` with a properly managed key is the minimum bar
   for personal data.
8. **Pick stable name fields at install time.** `agentName`, `featureName`, `organizationName`,
   `productName` participate in scope keys — renaming orphans data.

---

## 10. Long-term memory (experimental)

Module: `agents-features-longterm-memory`

A separate, RAG-style memory system layered on top of `AgentMemory` for very large knowledge bases:

- **Retrieval** — query extractors → search strategies → prompt augmenters
- **Ingestion** — extraction strategies with timing options (`ON_LLM_CALL` or `ON_AGENT_COMPLETION`)
- **Modes** — Full Automatic / Manual Only / Hybrid (auto-extract, manual recall)

Treat as experimental for now; reach for `AgentMemory` + a custom provider first.

---

## 11. Quick-reference imports

```kotlin
// Feature
import ai.koog.agents.local.memory.feature.AgentMemory

// Model
import ai.koog.agents.local.memory.model.Concept
import ai.koog.agents.local.memory.model.Fact
import ai.koog.agents.local.memory.model.FactType
import ai.koog.agents.local.memory.model.SingleFact
import ai.koog.agents.local.memory.model.MultipleFacts
import ai.koog.agents.local.memory.model.MemorySubject
import ai.koog.agents.local.memory.model.MemorySubjects

// Scope
import ai.koog.agents.local.memory.config.MemoryScope

// Providers + storage
import ai.koog.agents.local.memory.providers.AgentMemoryProvider
import ai.koog.agents.local.memory.providers.LocalFileMemoryProvider
import ai.koog.agents.local.memory.providers.LocalMemoryConfig
import ai.koog.agents.local.memory.storage.SimpleStorage
import ai.koog.agents.local.memory.storage.EncryptedStorage
import ai.koog.agents.local.memory.storage.Aes256GCMEncryptor

// Predefined nodes
import ai.koog.agents.local.memory.feature.nodes.nodeLoadAllFactsFromMemory
import ai.koog.agents.local.memory.feature.nodes.nodeLoadFromMemory
import ai.koog.agents.local.memory.feature.nodes.nodeSaveToMemory
import ai.koog.agents.local.memory.feature.nodes.nodeSaveToMemoryAutoDetectFacts
```

## API package map

- `ai.koog.agents.local.memory.feature` — core `AgentMemory`
- `ai.koog.agents.local.memory.feature.nodes` — predefined memory nodes
- `ai.koog.agents.local.memory.config` — scope definitions
- `ai.koog.agents.local.memory.model` — Fact / Concept / Subject types
- `ai.koog.agents.local.memory.feature.history` — history compression
- `ai.koog.agents.local.memory.providers` — provider interface + `LocalFileMemoryProvider`
- `ai.koog.agents.local.memory.storage` — storage primitives + encryption
