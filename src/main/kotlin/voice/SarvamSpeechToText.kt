package voice

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import util.ConfidentialObjects
import util.usage.UsageKind
import util.usage.persistAgentUsageLogToMongo

private val sarvamLogger = LoggerFactory.getLogger("SarvamSpeechToText")

private const val SARVAM_SPEECH_TO_TEXT_URL = "https://api.sarvam.ai/speech-to-text"

private val sharedVoiceHttpClient by lazy {
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
    }
}

@Serializable
private data class SarvamSpeechToTextResponse(
    val transcript: String? = null,
)

/**
 * English speech-to-text via Sarvam. Logs usage; returns null on failure so callers can fall back to text.
 */
suspend fun transcribeEnglishAudioWithSarvam(
    audioBytes: ByteArray,
    fileName: String = "voice.webm",
    contentType: String = "audio/webm",
    agentId: String? = null,
    clientIpAddress: String? = null,
    sessionId: String? = null,
): String? {
    val startedAtMs = System.currentTimeMillis()
    if (audioBytes.isEmpty()) {
        return null
    }
    return try {
        val response = sharedVoiceHttpClient.post(SARVAM_SPEECH_TO_TEXT_URL) {
            header("api-subscription-key", ConfidentialObjects.SARVAM_API_KEY)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "file",
                            audioBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            },
                        )
                        append("model", "saaras:v3")
                        append("language_code", "en-IN")
                    },
                ),
            )
        }
        if (!response.status.isSuccess()) {
            persistFailedVoiceUsage(
                kind = UsageKind.STT,
                startedAtMs = startedAtMs,
                agentId = agentId,
                errorMessage = "Sarvam HTTP ${response.status}",
                clientIpAddress = clientIpAddress,
                sessionId = sessionId,
            )
            return null
        }
        val transcript = response.body<SarvamSpeechToTextResponse>().transcript?.trim().orEmpty()
        persistAgentUsageLogToMongo(
            kind = UsageKind.STT,
            ok = transcript.isNotBlank(),
            durationMs = System.currentTimeMillis() - startedAtMs,
            agentId = agentId,
            inputChars = transcript.length,
            errorMessage = if (transcript.isBlank()) "blank transcript" else null,
            clientIpAddress = clientIpAddress,
            sessionId = sessionId,
        )
        transcript.takeIf { it.isNotBlank() }
    } catch (exception: Exception) {
        sarvamLogger.error("Sarvam STT failed: ${exception.message}", exception)
        persistFailedVoiceUsage(
            kind = UsageKind.STT,
            startedAtMs = startedAtMs,
            agentId = agentId,
            errorMessage = exception.message,
            clientIpAddress = clientIpAddress,
            sessionId = sessionId,
        )
        null
    }
}

internal suspend fun persistFailedVoiceUsage(
    kind: String,
    startedAtMs: Long,
    agentId: String?,
    errorMessage: String?,
    clientIpAddress: String?,
    sessionId: String?,
) {
    persistAgentUsageLogToMongo(
        kind = kind,
        ok = false,
        durationMs = System.currentTimeMillis() - startedAtMs,
        agentId = agentId,
        errorMessage = errorMessage,
        clientIpAddress = clientIpAddress,
        sessionId = sessionId,
    )
}
