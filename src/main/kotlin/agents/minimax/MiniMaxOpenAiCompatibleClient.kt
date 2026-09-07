package agents.minimax

import ai.koog.http.client.KoogHttpClient
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.base.models.OpenAIMessage
import ai.koog.prompt.executor.clients.openai.base.models.OpenAITool
import ai.koog.prompt.executor.clients.openai.base.models.OpenAIToolChoice
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * OpenAI-compatible MiniMax client that undoes Koog's double-encoded tool-call arguments.
 * MiniMax rejects the double-encoded shape on the follow-up request after a tool result.
 */
internal class MiniMaxOpenAiCompatibleClient(
    apiKey: String,
    settings: OpenAIClientSettings,
    httpClientFactory: KoogHttpClient.Factory = KtorKoogHttpClient.Factory(
        logger = KotlinLogging.logger("MiniMaxOpenAiCompatibleClient"),
    ),
) : OpenAILLMClient(
    apiKey = apiKey,
    settings = settings,
    httpClientFactory = httpClientFactory,
) {
    override fun serializeProviderChatRequest(
        messages: List<OpenAIMessage>,
        model: LLModel,
        tools: List<OpenAITool>?,
        toolChoice: OpenAIToolChoice?,
        params: LLMParams,
        stream: Boolean,
    ): String {
        val openAiRequestBody = super.serializeProviderChatRequest(
            messages = messages,
            model = model,
            tools = tools,
            toolChoice = toolChoice,
            params = params,
            stream = stream,
        )
        return normalizeDoubleEncodedToolArgumentsInMiniMaxOpenAiRequest(openAiRequestBody)
    }
}

internal fun normalizeDoubleEncodedToolArgumentsInMiniMaxOpenAiRequest(requestBody: String): String {
    val parsedRequestBody = runCatching {
        Json.parseToJsonElement(requestBody)
    }.getOrElse {
        return requestBody
    }
    return normalizeDoubleEncodedToolArgumentsInJsonElement(parsedRequestBody).toString()
}

private fun normalizeDoubleEncodedToolArgumentsInJsonElement(jsonElement: JsonElement): JsonElement =
    when (jsonElement) {
        is JsonArray -> JsonArray(jsonElement.map(::normalizeDoubleEncodedToolArgumentsInJsonElement))
        is JsonObject -> buildJsonObject {
            jsonElement.forEach { (key, value) ->
                val normalizedValue = if (key == "arguments") {
                    normalizeDoubleEncodedToolArgumentValue(value)
                } else {
                    normalizeDoubleEncodedToolArgumentsInJsonElement(value)
                }
                put(key, normalizedValue)
            }
        }

        else -> jsonElement
    }

private fun normalizeDoubleEncodedToolArgumentValue(jsonElement: JsonElement): JsonElement {
    if (jsonElement is JsonObject || jsonElement is JsonArray) {
        return normalizeDoubleEncodedToolArgumentsInJsonElement(jsonElement)
    }
    val content = jsonElement.jsonPrimitive.contentOrNull ?: return jsonElement
    val trimmedContent = content.trim()
    if (!trimmedContent.startsWith("\"") || !trimmedContent.endsWith("\"")) {
        return jsonElement
    }

    val decodedContent = runCatching {
        Json.decodeFromString<String>(trimmedContent)
    }.getOrNull() ?: return jsonElement

    val decodedTrimmedContent = decodedContent.trim()
    val isDecodedJsonContainer =
        (decodedTrimmedContent.startsWith("{") && decodedTrimmedContent.endsWith("}")) ||
            (decodedTrimmedContent.startsWith("[") && decodedTrimmedContent.endsWith("]"))

    return if (isDecodedJsonContainer) {
        JsonPrimitive(decodedContent)
    } else {
        jsonElement
    }
}
