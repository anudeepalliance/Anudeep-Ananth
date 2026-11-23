# KOOG graph-based agents

Source: https://docs.koog.ai/agents/graph-based-agents/ | https://docs.koog.ai/custom-strategy-graphs/ | https://docs.koog.ai/nodes-and-components/ | https://docs.koog.ai/custom-nodes/ | https://docs.koog.ai/data-transfer-between-nodes/ | https://docs.koog.ai/subgraphs-overview/ | https://docs.koog.ai/parallel-node-execution/

---

## Overview

Graph-based agents model behavior as a **directed graph** (state machine). Nodes represent actions (LLM calls, tool
execution, data transformation). Edges represent data flow with optional
conditions.

Advantages over functional agents:

- Easy to visualize and debug
- State persistence (checkpoint after each node)
- Composable (subgraphs)
- Parallel execution support
- Deterministic replay from checkpoints

---

## Strategy DSL

```kotlin
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.extension.*

val myStrategy = strategy<InputType, OutputType>("strategy-name") {
    // Define nodes
    val nodeA by node<InputType, IntermediateType> { input -> ... }
    val nodeB by node<IntermediateType, OutputType> { intermediate -> ... }

    // Define edges
    edge(nodeStart forwardTo nodeA)
    edge(nodeA forwardTo nodeB)
    edge(nodeB forwardTo nodeFinish)
}
```

### Type parameters

- `InputType` — the type passed to `agent.run(input)`
- `OutputType` — the type returned by `agent.run()`
- `nodeStart` — built-in start node, emits the agent input
- `nodeFinish` — built-in finish node, receives the final output

---

## Nodes

### Custom nodes

```kotlin
val myNode by node<InputType, OutputType> { input ->
    // Transform input to output
    val result = processData(input)
    result
}
```

### Pre-defined LLM nodes

```kotlin
// Send message to LLM, get single response
val nodeSendInput by nodeLLMRequest()

// Send message to LLM, get multiple responses
val nodeSendInputMultiple by nodeLLMRequestMultiple()

// Request structured output from LLM
val nodeStructured by nodeLLMRequestStructured<MyDataClass>()

// Send tool result back to LLM
val nodeSendToolResult by nodeLLMSendToolResult()
val nodeSendMultipleToolResults by nodeLLMSendMultipleToolResults()

// Compress conversation history
val nodeCompress by nodeLLMCompressHistory<ReceivedToolResult>()

// Append content to prompt
val nodeAppend by nodeAppendPrompt { /* build messages */ }

// Force LLM to only call tools (no text response)
val nodeForceTools by nodeLLMRequestOnlyCallingTools()

// Force LLM to use a specific tool
val nodeForceOneTool by nodeLLMRequestForceOneTool(specificTool)
```

### Pre-defined tool nodes

```kotlin
// Execute a single tool call
val nodeExecuteTool by nodeExecuteTool()

// Execute multiple tool calls (optionally parallel)
val nodeExecuteTools by nodeExecuteMultipleTools(parallelTools = true)
```

### Utility nodes

```kotlin
// Pass-through (no-op)
val nodeNoop by nodeDoNothing()
```

---

## Edges

Edges connect nodes and control data flow. Every node must have at least one outgoing edge (except
`nodeFinish`).

### Basic edges

```kotlin
// Simple forward
edge(nodeA forwardTo nodeB)

// Forward with data transformation
edge(nodeA forwardTo nodeB transformed { output -> transformedOutput })

// Terminal edge (to finish node)
edge(nodeB forwardTo nodeFinish)
edge(nodeB forwardTo nodeFinish transformed { output -> finalResult })
```

### Conditional edges

```kotlin
// Route based on tool call response
edge(nodeLLM forwardTo nodeExecuteTool onToolCall { true })
edge(nodeLLM forwardTo nodeFinish onAssistantMessage { true })

// Route based on multiple tool calls
edge(nodeLLM forwardTo nodeExecuteTools onMultipleToolCalls { true })
edge(nodeLLM forwardTo nodeFinish onMultipleAssistantMessages { true }
        transformed { messages -> messages.joinToString("\n") { it.content } })

// Custom condition
edge(nodeA forwardTo nodeB onCondition { data -> data.isValid })
edge(nodeA forwardTo nodeC onCondition { data -> !data.isValid })

// When no tools were called
edge(nodeLLM forwardTo nodeFinish onToolNotCalled)

// Type-based routing
edge(nodeA forwardTo nodeB onIsInstance (Message.Assistant::class) transformed { it.content })
```

---

## Standard tool-calling graph pattern

The most common pattern for an agent that can call tools:

```kotlin
val strategy = strategy<String, String>("tool-calling-agent") {
    val nodeCallLLM by nodeLLMRequestMultiple()
    val nodeExecuteTool by nodeExecuteMultipleTools(parallelTools = true)
    val nodeSendToolResult by nodeLLMSendMultipleToolResults()

    edge(nodeStart forwardTo nodeCallLLM)

    // If LLM calls tools, execute them
    edge(nodeCallLLM forwardTo nodeExecuteTool onMultipleToolCalls { true })

    // If LLM responds with text, we're done
    edge(nodeCallLLM forwardTo nodeFinish onMultipleAssistantMessages { true }
            transformed { it.joinToString("\n") { msg -> msg.content } })

    // Send tool results back to LLM
    edge(nodeExecuteTool forwardTo nodeSendToolResult)

    // Loop: LLM may call more tools or respond
    edge(nodeSendToolResult forwardTo nodeExecuteTool onMultipleToolCalls { true })
    edge(nodeSendToolResult forwardTo nodeFinish onMultipleAssistantMessages { true }
            transformed { it.joinToString("\n") { msg -> msg.content } })
}
```

---

## Pipeline pattern (linear graph)

For multi-phase pipelines where each node transforms state:

```kotlin
val pipelineStrategy = strategy<PipelineState, PipelineResult>("my-pipeline") {

    val nodePhase1 by node<PipelineState, PipelineState> { state ->
        val result = runPhase1(state.input)
        state.copy(phase1Output = result)
    }

    val nodePhase2 by node<PipelineState, PipelineState> { state ->
        val phase1 = state.phase1Output ?: error("Phase 2 requires Phase 1")
        val result = runPhase2(phase1)
        state.copy(phase2Output = result)
    }

    val nodePhase3 by node<PipelineState, PipelineState> { state ->
        val phase2 = state.phase2Output ?: error("Phase 3 requires Phase 2")
        val result = runPhase3(phase2)
        state.copy(phase3Output = result)
    }

    // Linear edges
    edge(nodeStart forwardTo nodePhase1)
    edge(nodePhase1 forwardTo nodePhase2)
    edge(nodePhase2 forwardTo nodePhase3)
    edge(nodePhase3 forwardTo nodeFinish transformed { state ->
        PipelineResult.Success(summary = "Done: ${state.phase3Output}")
    })
}
```

---

## Data transfer between nodes

### Via edge transformation

Data flows through edges. Use `transformed` to convert types between nodes:

```kotlin
edge(nodeA forwardTo nodeB transformed { aOutput -> convertToB(aOutput) })
```

### Via AIAgentStorage (key-value store)

For sharing data between non-adjacent nodes:

```kotlin
val nodeProducer by node<String, String> { input ->
    // Store data in agent storage
    storage["my_key"] = someData
    input
}

val nodeConsumer by node<String, String> { input ->
    // Retrieve data from storage
    val data = storage["my_key"] as? MyType
    // ... use data
    input
}
```

---

## Subgraphs

Encapsulate reusable graph patterns into subgraphs:

```kotlin
val subgraph = subgraph<SubInput, SubOutput>("my-subgraph") {
    val nodeA by node<SubInput, SubOutput> { input -> process(input) }

    edge(nodeStart forwardTo nodeA)
    edge(nodeA forwardTo nodeFinish)
}

val mainStrategy = strategy<Input, Output>("main") {
    val nodeSub by subgraph // Use the subgraph as a node

    edge(nodeStart forwardTo nodeSub)
    edge(nodeSub forwardTo nodeFinish)
}
```

### Predefined subgraphs

- **Task Execution Subgraph** — performs a task with tools, returns structured result
- **Task Execution with Verification** — includes a quality verification step returning
  `CriticResult`

---

## Parallel node execution

Execute multiple nodes concurrently with merge strategies:

```kotlin
val nodeParallel by parallelNode(
    nodes = listOf(nodeA, nodeB, nodeC),
    mergeStrategy = MergeStrategy.WaitAll { results ->
        // Combine results from all parallel nodes
        results.joinToString(", ")
    }
)
```

Merge strategies:

- `WaitAll` — wait for all nodes to complete, combine results
- `WaitFirst` — return the first result, cancel others

---

## Conditional branching

```kotlin
val strategy = strategy<State, Result>("branching") {
    val nodeCheck by node<State, State> { state ->
        // Evaluate condition
        state
    }

    val nodeBranchA by node<State, State> { state -> handleA(state) }
    val nodeBranchB by node<State, State> { state -> handleB(state) }

    edge(nodeStart forwardTo nodeCheck)
    edge(nodeCheck forwardTo nodeBranchA onCondition { state -> state.shouldTakeA })
    edge(nodeCheck forwardTo nodeBranchB onCondition { state -> !state.shouldTakeA })
    edge(nodeBranchA forwardTo nodeFinish transformed { ... })
    edge(nodeBranchB forwardTo nodeFinish transformed { ... })
}
```

---

## Agent construction with graph strategy

```kotlin
val agent = AIAgent(
    promptExecutor = executor,
    llmModel = model,
    strategy = myStrategy,
    systemPrompt = "You are a helpful assistant.",
    maxIterations = 20,
    toolRegistry = toolRegistry,
) {
    // Install features
    install(Persistence) { ... }
    install(Tracing) { ... }
}

// Run the agent
val result = agent.run(input, sessionId = "unique-session-id")
```
