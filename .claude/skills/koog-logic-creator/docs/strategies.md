# KOOG strategies

Source: https://docs.koog.ai/agents/ | https://docs.koog.ai/predefined-agent-strategies/ | https://docs.koog.ai/agents/functional-agents/ | https://docs.koog.ai/agents/planner-agents/

---

## Overview

A strategy defines how an agent processes input. KOOG provides four strategy types:

| Strategy                      | Use case                                   | Persistence | Visualization |
|-------------------------------|--------------------------------------------|-------------|---------------|
| **Basic (singleRunStrategy)** | Simple prompt-response with tools          | No          | No            |
| **Graph-based**               | Multi-phase pipelines, complex workflows   | Yes         | Yes           |
| **Functional**                | Imperative control flow, quick prototyping | No          | No            |
| **Planner (GOAP / LLM)**      | Goal-oriented task decomposition           | Varies      | Varies        |

---

## 1. Basic agents (singleRunStrategy)

The default when no explicit strategy is provided. Handles the standard LLM -> tools -> response
loop automatically.

```kotlin
val agent = AIAgent(
    promptExecutor = executor,
    llmModel = OpenAIModels.Chat.GPT4o,
    systemPrompt = "You are a helpful assistant.",
    toolRegistry = toolRegistry,
    maxIterations = 10,
)
val result = agent.run("What's 2 + 3?")
```

### Tool calling modes

The `singleRunStrategy` supports three tool-calling modes:

```kotlin
import ai.koog.agents.core.agent.entity.ToolCalls

// Sequential tool calls (default)
singleRunStrategy(toolCalls = ToolCalls.SEQUENTIAL)

// Parallel tool calls
singleRunStrategy(toolCalls = ToolCalls.PARALLEL)

// One tool call per step
singleRunStrategy(toolCalls = ToolCalls.SINGLE_RUN_SEQUENTIAL)
```

---

## 2. Graph-based strategies

Model agent behavior as a directed graph. Nodes are actions, edges are data flow paths.

See [graph-based-agents.md](graph-based-agents.md) for full documentation.

```kotlin
val strategy = strategy<String, String>("my-strategy") {
    val nodeA by nodeLLMRequest()
    val nodeB by nodeExecuteTool()

    edge(nodeStart forwardTo nodeA)
    edge(nodeA forwardTo nodeB onToolCall { true })
    edge(nodeA forwardTo nodeFinish onAssistantMessage { true })
    edge(nodeB forwardTo nodeFinish)
}
```

---

## 3. Functional strategies

Implement agent logic as a Kotlin lambda with access to the agent context. Good for quick
prototyping, but lacks persistence and visualization.

```kotlin
import ai.koog.agents.core.agent.entity.functionalStrategy

val strategy = functionalStrategy<String, String> { input ->
    // Send input to LLM
    var responses = requestLLMMultiple(input)

    // Tool-calling loop
    while (responses.containsToolCalls()) {
        val pendingCalls = extractToolCalls(responses)
        val results = executeMultipleTools(pendingCalls)
        responses = sendMultipleToolResults(results)
    }

    // Return final text response
    responses.single().asAssistantMessage().content
}

val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    strategy = strategy,
    toolRegistry = toolRegistry,
    systemPrompt = "You are a helpful assistant.",
)
```

### Functional context methods

Inside `functionalStrategy { }`, you have access to:

| Method                             | Description                           |
|------------------------------------|---------------------------------------|
| `requestLLM(message)`              | Send message, get single response     |
| `requestLLMMultiple(message)`      | Send message, get multiple responses  |
| `requestLLMWithoutTools(message)`  | LLM request with tools disabled       |
| `requestLLMForceOneTool(tool)`     | Force LLM to use a specific tool      |
| `requestLLMStructured<T>()`        | Get typed structured output           |
| `requestLLMStreaming()`            | Get streaming response                |
| `extractToolCalls(responses)`      | Extract tool calls from LLM responses |
| `executeMultipleTools(tools)`      | Execute multiple tool calls           |
| `executeTool(tool)`                | Execute a single tool call            |
| `sendMultipleToolResults(results)` | Send tool results back to LLM         |
| `sendToolResult(result)`           | Send a single tool result             |
| `compressHistory()`                | Compress conversation history         |
| `latestTokenUsage()`               | Get token count from last request     |
| `responses.containsToolCalls()`    | Check if responses contain tool calls |
| `response.asAssistantMessage()`    | Cast response to assistant message    |

### Session access

For direct prompt manipulation:

```kotlin
functionalStrategy<String, String> { input ->
    llm.writeSession {
        // Modify prompt, make requests, etc.
        val response = requestLLM(input)
        response.content
    }
}
```

---

## 4. Predefined strategies

### chatAgentStrategy

Conversational agent that processes user input via LLM, calls tools when needed, and provides
responses. Best for interactive chat applications.

```kotlin
import ai.koog.agents.core.agent.entity.chatAgentStrategy

val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    strategy = chatAgentStrategy(),
    systemPrompt = "You are a friendly assistant.",
    toolRegistry = toolRegistry,
)
```

### reActStrategy

Alternates between reasoning and execution phases. The agent reasons about the task, takes an action (tool call),
observes the result, then reasons again.

```kotlin
import ai.koog.agents.core.agent.entity.reActStrategy

val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    strategy = reActStrategy(
        reasoningInterval = 1,  // Reason every N steps
        name = "re_act",
    ),
    systemPrompt = "You are a research assistant.",
    toolRegistry = toolRegistry,
)
```

---

## 5. Planner strategies

### LLM-based planners

Decompose complex tasks into sub-steps using LLM planning:

- `SimpleLLMPlanner` — basic LLM-driven task decomposition
- `SimpleLLMWithCriticPlanner` — includes a critic that reviews and refines the plan

### GOAP agents (Goal-Oriented Action Planning)

Use A* search to find optimal action sequences:

```kotlin
// Define goals, actions with preconditions/effects/costs, and states
val goapAgent = GOAPAgent(
    goals = listOf(cleanRoomGoal),
    actions = listOf(pickUpAction, putDownAction, sweepAction),
    initialState = mapOf("dirty" to true, "holding" to false),
)
```

GOAP agents work best for well-defined environments with:

- Clear goal states
- Finite, enumerable actions
- Known preconditions and effects
- Quantifiable costs

---

## Choosing a strategy

| Scenario                                                   | Recommended strategy                                     |
|------------------------------------------------------------|----------------------------------------------------------|
| Simple Q&A / chat with tools                               | `singleRunStrategy()` (default) or `chatAgentStrategy()` |
| Multi-phase pipeline (content generation, data processing) | Graph strategy with `strategy("name") { ... }`           |
| Quick prototype with custom tool logic                     | `functionalStrategy { ... }`                             |
| Reasoning-heavy tasks (research, analysis)                 | `reActStrategy()`                                        |
| Complex task decomposition                                 | Planner strategy                                         |
| Game AI, robotics planning                                 | GOAP agent                                               |
