# KOOG testing

Source: https://docs.koog.ai/testing/

---

## Overview

KOOG provides a comprehensive testing framework for mocking LLM responses, tool behavior, and
testing graph structure.

Module: `agents-test`

```kotlin
dependencies {
    testImplementation("ai.koog:agents-test:0.7.3")
}
```

---

## Mocking LLM responses

Use `getMockExecutor` to create a mock prompt executor with predefined responses:

```kotlin
import ai.koog.agents.testing.getMockExecutor

val mockExecutor = getMockExecutor(toolRegistry, eventHandler) {
    // Match by request content
    mockLLMAnswer("Hello!") onRequestContains "Hello"
    mockLLMAnswer("I don't know") onRequestContains "unknown"

    // Match by exact request
    mockLLMToolCall(CreateTool, CreateTool.Args("solve")) onRequestEquals "Solve task"

    // Default response (when no other match)
    mockLLMAnswer("Default response").asDefaultResponse
}
```

### Matching strategies

| Matcher                          | Description                          |
|----------------------------------|--------------------------------------|
| `onRequestContains "text"`       | Response content contains the string |
| `onRequestEquals "text"`         | Response content exactly matches     |
| `onRequestMatches { predicate }` | Custom lambda predicate              |
| `.asDefaultResponse`             | Used when no other matcher hits      |

### Response types

```kotlin
// Simple text response
mockLLMAnswer("The answer is 42")

// Tool call response
mockLLMToolCall(MyTool, MyTool.Args(param = "value"))

// Multiple responses in sequence
mockLLMAnswer("First") onRequestContains "start"
mockLLMAnswer("Second") onRequestContains "start" // second call
```

---

## Mocking tool behavior

```kotlin
// Always return a fixed value
mockTool(SearchTool) alwaysReturns "Found 3 results"

// Conditional returns based on arguments
mockTool(SearchTool) returns SearchTool.Result("Found") onArgumentsMatching { args ->
    args.query.contains("important")
}

// Return different values per call
mockTool(SearchTool) returnsSequentially listOf("Result 1", "Result 2", "Result 3")
```

---

## Testing graph structure

Use `testGraph` to validate the structure of a strategy graph:

```kotlin
testGraph("my-strategy-test") {
    // Assert subgraph exists by name
    val sub = assertSubgraphByName<String, String>("first")

    // Assert node exists by name
    val node = assertNodeByName<String, String>("my_node")

    // Assert edge structure
    assertEdges {
        startNode() alwaysGoesTo sub
        sub alwaysGoesTo nodeFinish()
    }

    // Assert reachability
    assertReachable(from = startNode(), to = nodeFinish())
}
```

### Edge assertions

```kotlin
assertEdges {
    // Node A always goes to Node B
    nodeA alwaysGoesTo nodeB

    // Node A goes to Node B on condition
    nodeA goesTo nodeB onCondition { true }

    // Node A goes to Node B on tool call
    nodeA goesTo nodeB onToolCall { true }
}
```

### Node behavior assertions

```kotlin
assertNodes {
    // Assert that asking the LLM with specific input produces expected output
    askLLM withInput "Hello" outputs assistantMessage("Hello!")
}
```

---

## Testing with withTesting ()

Enable testing mode in agent construction:

```kotlin
val agent = AIAgent(
    promptExecutor = mockExecutor,
    llmModel = testModel,
    strategy = myStrategy,
) {
    withTesting()
}

// Now run the agent and assert results
val result = agent.run("test input")
assertEquals("expected output", result)
```

---

## Integration testing pattern

For testing full pipelines:

```kotlin
@Test
fun `test pipeline produces valid output`() = runBlocking {
        val mockExecutor = getMockExecutor {
            mockLLMAnswer("""{"sentiment": "positive", "topics": ["kotlin"]}""")
                .asDefaultResponse
        }

        val agent = AIAgent(
            promptExecutor = mockExecutor,
            llmModel = testModel,
            strategy = myPipelineStrategy,
        ) {
            withTesting()
        }

        val result = agent.run(TestInput("Kotlin is great"))

        assertTrue(result is PipelineResult.Success)
        assertEquals("positive", (result as PipelineResult.Success).sentiment)
    }
```
