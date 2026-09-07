package voice

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import util.ConfidentialObjects
import util.usage.UsageKind
import util.usage.persistAgentUsageLogToMongo

private val sarvamLogger = LoggerFactory.getLogger("SarvamSpeechToText")

private const val SARVAM_SPEECH_TO_TEXT_URL = "https://api.sarvam.ai/speech-to-text"
private const val SARVAM_STT_MODEL = "saaras:v3"
private const val SARVAM_STT_MODE = "transcribe"
private const val SARVAM_LANGUAGE_CODE = "en-IN"

private val sarvamJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private val sharedVoiceHttpClient by lazy {
    HttpClient(CIO) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 45_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 45_000
        }
        engine {
            requestTimeout = 45_000
        }
    }
}

@Serializable
private data class SarvamSpeechToTextResponse(
    val transcript: String? = null,
)

/**
 * English speech-to-text via Sarvam Saaras v3. Logs usage; returns null on failure so callers can fall back to typing.
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
    val sarvamContentType = normalizeSarvamAudioContentType(contentType)
    val sarvamFileName = fileNameForSarvamContentType(
        contentType = sarvamContentType,
        originalFileName = fileName,
    )
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
                                append(HttpHeaders.ContentType, sarvamContentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$sarvamFileName\"")
                            },
                        )
                        append("model", SARVAM_STT_MODEL)
                        append("mode", SARVAM_STT_MODE)
                        append("language_code", SARVAM_LANGUAGE_CODE)
                    },
                ),
            )
        }
        val responseText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            sarvamLogger.warn("Sarvam HTTP ${response.status} body=${responseText.take(400)}")
            persistFailedVoiceUsage(
                kind = UsageKind.STT,
                startedAtMs = startedAtMs,
                agentId = agentId,
                errorMessage = "Sarvam HTTP ${response.status}: ${responseText.take(180)}",
                clientIpAddress = clientIpAddress,
                sessionId = sessionId,
            )
            return null
        }
        val transcript = sarvamJson.decodeFromString(
            SarvamSpeechToTextResponse.serializer(),
            responseText,
        ).transcript?.trim().orEmpty()
        persistAgentUsageLogToMongo(
            kind = UsageKind.STT,
            ok = transcript.isNotBlank(),
            durationMs = System.currentTimeMillis() - startedAtMs,
            agentId = agentId,
            inputChars = transcript.length,
            visitorEnteredQueryText = transcript,
            errorMessage = if (transcript.isBlank()) "blank transcript" else null,
            clientIpAddress = clientIpAddress,
            sessionId = sessionId,
        )
        transcript.takeIf { transcriptText ->
            transcriptText.isNotBlank()
        }
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

/**
 * Sarvam rejects browser mime strings like `audio/webm;codecs=opus`. Keep only the type/subtype.
 */
internal fun normalizeSarvamAudioContentType(rawContentType: String): String {
    val baseType = rawContentType.substringBefore(';').trim().lowercase()
    return when {
        baseType.contains("webm") -> "audio/webm"
        baseType.contains("ogg") -> "audio/ogg"
        baseType == "audio/opus" -> "audio/opus"
        baseType.contains("mp4") || baseType.contains("m4a") -> "audio/mp4"
        baseType.contains("mpeg") || baseType.contains("mp3") -> "audio/mpeg"
        baseType.contains("wav") || baseType.contains("wave") -> "audio/wav"
        baseType.contains("aac") -> "audio/aac"
        baseType.isBlank() -> "audio/webm"
        else -> baseType
    }
}

internal fun fileNameForSarvamContentType(
    contentType: String,
    originalFileName: String,
): String {
    val extension = when {
        contentType.contains("wav") -> "wav"
        contentType.contains("mpeg") || contentType.contains("mp3") -> "mp3"
        contentType.contains("mp4") || contentType.contains("m4a") -> "m4a"
        contentType.contains("ogg") -> "ogg"
        contentType.contains("opus") && !contentType.contains("webm") -> "opus"
        else -> "webm"
    }
    val baseName = originalFileName.substringBeforeLast('.', "voice").ifBlank {
        "voice"
    }
    return "$baseName.$extension"
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
