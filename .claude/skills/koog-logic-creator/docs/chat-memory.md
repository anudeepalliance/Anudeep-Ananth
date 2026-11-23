# KOOG Chat Memory

Source:

- https://docs.koog.ai/features/chat-memory/
- https://docs.koog.ai/features/chat-memory/chat-agent-with-memory/
- https://docs.koog.ai/features/chat-memory/chat-backend-with-memory/

Module: `ai.koog:agents-features-memory` (available from koog 0.7.0+)

---

## 1. Overview

`ChatMemory` is a KOOG feature that gives an agent multi-turn conversation continuity. On every
`agent.run(input, sessionId)` call it transparently:

1. **Loads** the prior conversation for that session id before the LLM sees the prompt
2. **Appends** the new user input and the assistant's reply to that history
3. **Stores** the updated history after the run completes

You never have to hand-assemble prompt history — you just pass a stable session id and the feature
keeps each conversation coherent across runs, restarts, and requests.

### Key capabilities

- Automatic load/store keyed by session id
- Pluggable storage backend via `ChatHistoryProvider`
- Built-in preprocessors for window-size capping and message filtering
- Custom preprocessors for transformations (PII redaction, summarization, etc.)

### Minimal install

```kotlin
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.memory.feature.ChatMemory
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor

val agent = AIAgent(
    promptExecutor = simpleOpenAIExecutor(System.getenv("OPENAI_API_KEY")),
    llmModel = OpenAIModels.Chat.GPT4oMini,
    systemPrompt = "You are a helpful assistant."
) {
    install(ChatMemory) {
        windowSize(20)
    }
}
```

### ChatMemory vs. Persistence

These features solve different problems and are easy to confuse. They compose cleanly when you need
both.

| Aspect           | `ChatMemory`                                       | `Persistence`                     |
|------------------|----------------------------------------------------|-----------------------------------|
| What it saves    | Conversation messages                              | Agent graph execution state       |
| When it saves    | After `agent.run()` completes                      | After each graph node             |
| On crash         | In-progress run is lost; prior conversation intact | Resumes from the last checkpoint  |
| Primary use case | Multi-turn chat continuity                         | Long-running tool-using pipelines |

Use `ChatMemory` for a chat product. Use `Persistence` when a single agent run is long and expensive
enough that losing it mid-way hurts. Use both when a chat agent also runs multi-step tool chains per
turn.

---

## 2. Session IDs

The session id is the second argument to `agent.run(input, sessionId)`. It is the only thing that
scopes conversation histories — same id shares history, different ids are fully isolated.

```kotlin
// First run — nothing loaded (empty history), then the exchange is saved
agent.run("What is the capital of France?", "session-1")

// Second run on same session id — prior exchange is loaded before the LLM sees the new input
agent.run("And what about Germany?", "session-1")
```

### Choosing a session id

Pick something stable and meaningful for the conversation's lifetime:

| Product shape                 | Good session id                        |
|-------------------------------|----------------------------------------|
| One-to-one chatbot            | User id                                |
| Threaded chat                 | Thread uuid                            |
| User × multiple personas      | `"${userId}-${characterId}"` composite |
| Anonymous short-lived session | Random uuid stored in a cookie         |

**For Peekopals:** a user can chat with any of 6 AI characters and each (user, character) pair is
its own ongoing conversation — so the natural session id is `"${userId}-${characterId}"`. A single
user id or character id alone would bleed history across pairs.

---

## 3. `ChatHistoryProvider`

`ChatHistoryProvider` is the storage interface. The feature ships with `InMemoryChatHistoryProvider`
(thread-safe but non-persistent — fine for tests and local dev, never for production).

### The interface

```kotlin
interface ChatHistoryProvider {
    suspend fun store(conversationId: String, messages: List<Message>)
    suspend fun load(conversationId: String): List<Message>
}
```

### Custom provider skeleton

```kotlin
import ai.koog.agents.features.memory.providers.ChatHistoryProvider
import ai.koog.prompt.message.Message

class MyDatabaseChatHistoryProvider(
    private val db: Database,
) : ChatHistoryProvider {

    override suspend fun store(conversationId: String, messages: List<Message>) {
        db.saveMessages(conversationId, messages)
    }

    override suspend fun load(conversationId: String): List<Message> {
        return db.loadMessages(conversationId) ?: emptyList()
    }
}
```

Wire it in at install time:

```kotlin
install(ChatMemory) {
    chatHistoryProvider = MyDatabaseChatHistoryProvider(db)
    windowSize(50)
}
```

### Peekopals: Mongo-backed provider

The Peekopals server already uses MongoDB for other reads/writes (see the `XxxPipelineDataAccess.kt`
files referenced in [reaptive-conventions.md](reaptive-conventions.md)). A Mongo-backed provider is
the natural fit: one `chat_history` collection, documents keyed by `conversationId` holding a list
of serialized `Message`s. Reuse the existing Mongo client and serialization patterns — don't invent
a new data access layer.

---

## 4. Preprocessors

Preprocessors transform the message list at both load time (before the LLM sees it) and store time (before it hits the
provider). They run in the order you register them, and that order matters.

### Built-in preprocessors

| Method                         | Class                        | Behavior                                       |
|--------------------------------|------------------------------|------------------------------------------------|
| `windowSize(n)`                | `WindowSizePreProcessor`     | Keep only the last `n` messages                |
| `filterMessages { predicate }` | `FilterMessagesPreProcessor` | Keep messages where the predicate returns true |

### Configuration combining both

```kotlin
install(ChatMemory) {
    chatHistoryProvider = MyDatabaseChatHistoryProvider(db)
    windowSize(20)
    filterMessages { it is Message.User || it is Message.Assistant }
}
```

### Custom preprocessor

Implement `ChatMemoryPreProcessor` and register it:

```kotlin
import ai.koog.agents.features.memory.preprocessor.ChatMemoryPreProcessor
import ai.koog.prompt.message.Message

class RedactEmailsPreProcessor : ChatMemoryPreProcessor {
    private val emailRegex = Regex("""[\w.+-]+@[\w.-]+""")

    override fun preprocess(messages: List<Message>): List<Message> =
        messages.map { msg ->
            when (msg) {
                is Message.User -> Message.User(msg.content.replace(emailRegex, "[REDACTED]"))
                else -> msg
            }
        }
}

install(ChatMemory) {
    addPreProcessor(RedactEmailsPreProcessor())
    windowSize(50)
}
```

### Order matters

Preprocessors compose sequentially, so `filter-then-window` and `window-then-filter` give different
results.

- **Filter first, then window:** drop system messages, then keep the last 20 remaining — you always
  get 20 user/assistant turns
- **Window first, then filter:** keep the last 20 raw messages, then drop systems — you may end up
  with fewer than 20 effective turns if some of the last 20 were systems

Decide what you actually want and register preprocessors in that order.

---

## 5. Chat Agent pattern (CLI / simple loop)

A minimal terminal chat agent — useful for prototyping a character's voice before wiring it into the
server:

```kotlin
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.memory.feature.ChatMemory
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor

suspend fun main() {
    val sessionId = "my-conversation"

    simpleOpenAIExecutor(System.getenv("OPENAI_API_KEY")).use { executor ->
        val agent = AIAgent(
            promptExecutor = executor,
            llmModel = OpenAIModels.Chat.GPT4oMini,
            systemPrompt = "You are a helpful assistant."
        ) {
            install(ChatMemory) {
                windowSize(20)
            }
        }

        while (true) {
            print("You: ")
            val input = readln().trim()
            if (input == "/bye") break
            if (input.isEmpty()) continue

            val reply = agent.run(input, sessionId)
            println("Assistant: $reply\n")
        }
    }
}
```

Every `agent.run(input, sessionId)` call in the loop:

1. Loads the prior messages for `sessionId` from the provider
2. Runs them through any preprocessors
3. Appends the new user input
4. Calls the LLM
5. Stores the updated history

Nothing in the loop itself has to track history — the feature does it.

### Choosing a window size

A `windowSize(20)` default works for casual chat. For longer, more reference-heavy conversations,
40–50 is reasonable. Above that, you're paying real token cost on every turn and risking
context-window overflow — at which point consider summarization via a custom preprocessor.

---

## 6. Chat Backend pattern

For a real chat product, the agent is constructed once at server startup and reused across HTTP
requests. The request carries the session id; the agent handles the rest.

### Architecture

```
HTTP request { userId, characterId, message }
        │
        ▼
  Ktor route  ──▶  sessionId = "${userId}-${characterId}"
        │
        ▼
  agent.run(message, sessionId)   ← ChatMemory loads, appends, saves transparently
        │
        ▼
  Response { reply }
```

### Agent construction (once, at startup)

```kotlin
val chatAgent = AIAgent(
    promptExecutor = executor,
    llmModel = OpenAIModels.Chat.GPT4oMini,
    systemPrompt = characterSystemPrompt,
) {
    install(ChatMemory) {
        chatHistoryProvider = MongoChatHistoryProvider(mongoDb)
        windowSize(50)
    }
}
```

### Peekopals Ktor route

Follow the existing project pattern from `RunXxxPipelineRoute.kt`
(see [reaptive-conventions.md](reaptive-conventions.md)). The route is a thin adapter — it builds
the session id, calls the agent, and returns the reply. All memory persistence happens inside
`install(ChatMemory)`.

```kotlin
fun Route.chatRoute(chatAgent: AIAgent<String, String>) {
    post(ApiRoutes.CHAT) {
        val body = call.receive<ChatRequest>()
        val sessionId = "${body.userId}-${body.characterId}"
        val reply = chatAgent.run(body.message, sessionId)
        call.respond(ChatResponse(reply = reply))
    }
}
```

Because each character has its own system prompt and ideally its own agent instance (or a single
agent with character injection into the prompt), you have two sensible shapes:

- **One agent per character** — held in a map `Map<CharacterId, AIAgent<String, String>>`, system
  prompt baked into each. Clean separation, slightly more startup memory.
- **Single agent with templated system prompt** — one `AIAgent` constructed without a fixed system
  prompt; the character's system prompt is prepended at request time. Simpler lifecycle, but you
  lose the "system prompt is stored in history" guarantee.

Either way, session id remains `"${userId}-${characterId}"` so histories stay isolated per (user,
character) pair.

---

## 7. Best Practices

- **Always set `windowSize`.** Unbounded history eventually overflows the LLM's context window and
  blows up token cost on every turn.
- **Choose preprocessor order deliberately.** Filter-then-window vs window-then-filter give
  different results — know which one you want.
- **Use meaningful session ids.** User ids for one-to-one bots, composite
  `"${userId}-${characterId}"` for multi-persona products, thread uuids for threaded chat.
- **Never ship `InMemoryChatHistoryProvider` to production.** It evaporates on restart and doesn't
  scale across server instances. Use a persistent provider (Mongo, Postgres, Redis) for anything
  real.
- **Combine with `Persistence` when turns are expensive.** If a single turn runs a long tool-calling
  pipeline, pair `ChatMemory` (conversation continuity) with `Persistence` (crash recovery inside a
  turn).
- **Redact sensitive data at store time.** If user input may contain PII, add a custom preprocessor (like the
  `RedactEmailsPreProcessor` example) so secrets don't end up in your history store.

---

## 8. Quick-reference imports

```kotlin
// Core feature
import ai.koog.agents.features.memory.feature.ChatMemory

// Providers
import ai.koog.agents.features.memory.providers.ChatHistoryProvider
import ai.koog.agents.features.memory.providers.InMemoryChatHistoryProvider

// Preprocessors
import ai.koog.agents.features.memory.preprocessor.ChatMemoryPreProcessor

// Messages
import ai.koog.prompt.message.Message
```
