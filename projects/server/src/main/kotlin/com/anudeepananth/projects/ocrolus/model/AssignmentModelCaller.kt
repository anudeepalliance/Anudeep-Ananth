package com.anudeepananth.projects.ocrolus.model

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.params.LLMParams
import com.anudeepananth.projects.ocrolus.OcrolusModelOption
import com.anudeepananth.projects.ocrolus.OcrolusModelProvider
import com.anudeepananth.projects.util.ConfidentialObjects
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

/** What a model sent back for one prompt, with how many tokens it used. */
data class AssignmentModelReply(val text: String, val promptTokens: Long, val completionTokens: Long)

/** Sends one prompt to one AI model. An interface so tests can use canned replies instead of a real model. */
fun interface AssignmentModelCaller {
    suspend fun requestModelReply(
        model: OcrolusModelOption,
        promptText: String,
        temperature: Double
    ): AssignmentModelReply
}

private const val MINIMAX_OPENAI_COMPATIBLE_BASE_URL = "https://api.minimax.io"
private const val SINGLE_MODEL_CALL_TIMEOUT_MILLISECONDS = 150_000L

/** Asks OpenRouter for the shortest "thinking" phase; free models otherwise think for a minute or more per call. */
private val lowReasoningEffortRequestProperties = mapOf(
    "reasoning" to buildJsonObject {
        put("effort", "low")
    },
)
private val waitsBeforeRetryingAfterRateLimitMilliseconds = listOf(3_000L)

/**
 * Calls models through the Koog framework: OpenRouter for its free and paid models, and MiniMax's own
 * endpoint for MiniMax. Free OpenRouter models use the free-only key; paid ones use the paid key.
 * When OpenRouter says "too many requests" it waits and tries once more before giving up, so the run can move on to a backup model quickly.
 */
class KoogOpenRouterModelCaller : AssignmentModelCaller {
    private val logger = LoggerFactory.getLogger("KoogOpenRouterModelCaller")

    private val freeModelsClient by lazy {
        OpenRouterLLMClient(apiKey = ConfidentialObjects.OPEN_ROUTER_FREE_MODELS_ONLY)
    }
    private val paidModelsClient by lazy {
        OpenRouterLLMClient(apiKey = ConfidentialObjects.OPEN_ROUTER_PAID_MODELS_KEY)
    }

    /** MiniMax's own OpenAI-compatible endpoint, paid for by Anudeep's token plan. */
    private val miniMaxTokenPlanClient by lazy {
        OpenAILLMClient(
            apiKey = ConfidentialObjects.MINIMAX_TOKEN_PLAN_PP_PRIMARY,
            settings = OpenAIClientSettings(baseUrl = MINIMAX_OPENAI_COMPATIBLE_BASE_URL),
        )
    }

    override suspend fun requestModelReply(
        model: OcrolusModelOption,
        promptText: String,
        temperature: Double
    ): AssignmentModelReply {
        var lastException: Exception? = null
        (listOf(0L) + waitsBeforeRetryingAfterRateLimitMilliseconds).forEach { waitMilliseconds ->
            delay(waitMilliseconds)
            try {
                return sendPromptThroughKoog(model, promptText, temperature)
            } catch (exception: Exception) {
                lastException = exception
                val isRateLimited = exception.message.orEmpty().let {
                    it.contains("429") || it.contains("rate", ignoreCase = true)
                }
                if (!isRateLimited) throw exception
                logger.warn("OpenRouter rate-limited ${model.modelId}; will retry")
            }
        }
        throw checkNotNull(lastException)
    }

    private suspend fun sendPromptThroughKoog(
        model: OcrolusModelOption,
        promptText: String,
        temperature: Double
    ): AssignmentModelReply {
        val isMiniMax = model.provider == OcrolusModelProvider.MINIMAX
        // The low-reasoning request option is OpenRouter's; MiniMax gets a plain request.
        val koogPrompt = prompt(
            "ocrolus-assignment-step",
            LLMParams(
                temperature = temperature,
                additionalProperties = if (isMiniMax) null else lowReasoningEffortRequestProperties
            ),
        ) {
            user(promptText)
        }
        val koogModel = if (isMiniMax) {
            LLModel(
                provider = LLMProvider.OpenAI,
                id = model.modelId,
                capabilities = listOf(
                    LLMCapability.Completion,
                    LLMCapability.OpenAIEndpoint.Completions,
                    LLMCapability.Temperature
                ),
            )
        } else {
            LLModel(
                provider = LLMProvider.OpenRouter,
                id = model.modelId,
                capabilities = listOf(LLMCapability.Completion, LLMCapability.Temperature),
            )
        }
        val client = when {
            isMiniMax -> miniMaxTokenPlanClient
            model.isFree -> freeModelsClient
            else -> paidModelsClient
        }
        // A slow call must count as an ordinary failure (so a backup model is tried), not as the run being cancelled.
        val assistantMessage = withTimeoutOrNull(SINGLE_MODEL_CALL_TIMEOUT_MILLISECONDS) {
            client.execute(koogPrompt, koogModel, emptyList())
        } ?: throw IllegalStateException("No reply within ${SINGLE_MODEL_CALL_TIMEOUT_MILLISECONDS / 1000} seconds")
        return AssignmentModelReply(
            text = assistantMessage.parts.filterIsInstance<MessagePart.Text>().joinToString("") {
                it.text
            },
            promptTokens = assistantMessage.metaInfo.inputTokensCount?.toLong() ?: 0,
            completionTokens = assistantMessage.metaInfo.outputTokensCount?.toLong() ?: 0,
        )
    }
}
