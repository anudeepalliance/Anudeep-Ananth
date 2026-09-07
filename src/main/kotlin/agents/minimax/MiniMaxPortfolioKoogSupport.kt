package agents.minimax

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.Prompt
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import util.ConfidentialObjects
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.withTimeoutOrNull

private const val MINIMAX_OPENAI_COMPATIBLE_BASE_URL = "https://api.minimax.io"
private const val PORTFOLIO_AGENT_TIMEOUT_MS = 120_000L

enum class PortfolioMiniMaxModels(val modelName: String) {
    MINIMAX_M2_7_HIGHSPEED("MiniMax-M2.7-highspeed"),
}

private val miniMaxOpenAiCompatibleClient by lazy {
    MiniMaxOpenAiCompatibleClient(
        apiKey = ConfidentialObjects.MINIMAX_TOKEN_PLAN_PP_PRIMARY,
        settings = OpenAIClientSettings(baseUrl = MINIMAX_OPENAI_COMPATIBLE_BASE_URL),
    )
}

/**
 * MiniMax executor that strips leaked `<think>` blocks before callers parse the reply.
 */
internal val portfolioMiniMaxKoogExecutor: MultiLLMPromptExecutor by lazy {
    ThinkStrippingMultiLLMPromptExecutor(mapOf(LLMProvider.OpenAI to miniMaxOpenAiCompatibleClient))
}

fun portfolioMiniMaxKoogModel(
    modelId: String = PortfolioMiniMaxModels.MINIMAX_M2_7_HIGHSPEED.modelName,
): LLModel =
    LLModel(
        provider = LLMProvider.OpenAI,
        id = modelId,
        capabilities = listOf(
            LLMCapability.Completion,
            LLMCapability.OpenAIEndpoint.Completions,
            LLMCapability.Tools,
            LLMCapability.Schema.JSON.Standard,
        ),
    )

private class ThinkStrippingMultiLLMPromptExecutor(
    llmClients: Map<LLMProvider, LLMClient>,
) : MultiLLMPromptExecutor(llmClients) {
    override suspend fun execute(
        prompt: Prompt,
        model: LLModel,
        tools: List<ai.koog.agents.core.tools.ToolDescriptor>,
    ): Message.Assistant {
        val rawAssistantMessage = super.execute(prompt, model, tools)
        val cleanedParts = rawAssistantMessage.parts.mapNotNull { messagePart ->
            when (messagePart) {
                is ai.koog.prompt.message.MessagePart.Text -> {
                    val cleanedText = stripMiniMaxReasoningTags(messagePart.text)
                    cleanedText.takeIf { it.isNotBlank() }?.let {
                        ai.koog.prompt.message.MessagePart.Text(it, messagePart.cacheControl)
                    }
                }

                else -> messagePart
            }
        }
        return rawAssistantMessage.copy(parts = cleanedParts)
    }
}

/**
 * Runs one MiniMax [AIAgent] turn and returns cleaned assistant text.
 */
suspend fun runPortfolioMiniMaxAgentTurn(
    systemPrompt: String,
    userPrompt: String,
    toolRegistry: ToolRegistry? = null,
    modelId: String = PortfolioMiniMaxModels.MINIMAX_M2_7_HIGHSPEED.modelName,
    maxIterations: Int = 20,
): String {
    val agent = if (toolRegistry == null) {
        AIAgent(
            promptExecutor = portfolioMiniMaxKoogExecutor,
            systemPrompt = systemPrompt,
            llmModel = portfolioMiniMaxKoogModel(modelId),
        )
    } else {
        AIAgent(
            promptExecutor = portfolioMiniMaxKoogExecutor,
            systemPrompt = systemPrompt,
            llmModel = portfolioMiniMaxKoogModel(modelId),
            toolRegistry = toolRegistry,
            maxIterations = maxIterations,
        )
    }

    val rawResponse = withTimeoutOrNull(PORTFOLIO_AGENT_TIMEOUT_MS.milliseconds) {
        agent.run(userPrompt)
    } ?: "Agent timed out after ${PORTFOLIO_AGENT_TIMEOUT_MS / 1000}s"

    return stripMiniMaxReasoningTags(rawResponse)
}
