package voice

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import util.ConfidentialObjects
import util.usage.UsageKind
import util.usage.persistAgentUsageLogToMongo

private val elevenLabsLogger = LoggerFactory.getLogger("ElevenLabsTextToSpeech")

/** Raj — Indian male English, used for spoken agent replies. */
private const val INDIAN_MALE_ENGLISH_VOICE_ID = "uavKGt8JpB2lo1bcty9J"
private const val ELEVEN_LABS_MODEL_ID = "eleven_multilingual_v2"

private val elevenLabsHttpClient by lazy { HttpClient(CIO) }

@Serializable
private data class ElevenLabsSpeechRequestBody(
    val text: String,
    @SerialName("model_id") val modelId: String,
)

/**
 * Speaks [text] via ElevenLabs. Returns MP3 bytes or null so the UI can stay text-only.
 */
suspend fun synthesizeEnglishSpeechWithElevenLabs(
    text: String,
    voiceId: String = INDIAN_MALE_ENGLISH_VOICE_ID,
    agentId: String? = null,
    clientIpAddress: String? = null,
    sessionId: String? = null,
): ByteArray? {
    val startedAtMs = System.currentTimeMillis()
    val trimmedText = text.trim()
    if (trimmedText.isBlank()) {
        return null
    }
    return try {
        val response = elevenLabsHttpClient.post(
            "https://api.elevenlabs.io/v1/text-to-speech/$voiceId?output_format=mp3_44100_128",
        ) {
            header("xi-api-key", ConfidentialObjects.ELEVEN_LABS_API_KEY)
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    ElevenLabsSpeechRequestBody(
                        text = trimmedText.take(2500),
                        modelId = ELEVEN_LABS_MODEL_ID,
                    ),
                ),
            )
        }
        if (!response.status.isSuccess()) {
            persistFailedVoiceUsage(
                kind = UsageKind.TTS,
                startedAtMs = startedAtMs,
                agentId = agentId,
                errorMessage = "ElevenLabs HTTP ${response.status}",
                clientIpAddress = clientIpAddress,
                sessionId = sessionId,
            )
            return null
        }
        val audioBytes = response.bodyAsBytes()
        persistAgentUsageLogToMongo(
            kind = UsageKind.TTS,
            ok = audioBytes.isNotEmpty(),
            durationMs = System.currentTimeMillis() - startedAtMs,
            agentId = agentId,
            inputChars = trimmedText.length,
            clientIpAddress = clientIpAddress,
            sessionId = sessionId,
        )
        audioBytes.takeIf { it.isNotEmpty() }
    } catch (exception: Exception) {
        elevenLabsLogger.error("ElevenLabs TTS failed: ${exception.message}", exception)
        persistFailedVoiceUsage(
            kind = UsageKind.TTS,
            startedAtMs = startedAtMs,
            agentId = agentId,
            errorMessage = exception.message,
            clientIpAddress = clientIpAddress,
            sessionId = sessionId,
        )
        null
    }
}
