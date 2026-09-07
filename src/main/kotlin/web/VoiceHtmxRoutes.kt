package web

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.utils.io.toByteArray
import voice.synthesizeEnglishSpeechWithElevenLabs
import voice.transcribeEnglishAudioWithSarvam

/**
 * Voice STT/TTS endpoints used by the homepage mic/play controls.
 */
fun Routing.voiceHtmxRoutes() {
    post("/htmx/voice/stt") {
        val multipart = call.receiveMultipart()
        var audioBytes: ByteArray? = null
        var fileName = "voice.webm"
        var contentType = "audio/webm"
        var agentId: String? = null
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    fileName = part.originalFileName ?: fileName
                    contentType = part.contentType?.toString() ?: contentType
                    audioBytes = part.provider().toByteArray()
                }
                is PartData.FormItem -> {
                    if (part.name == "agentId") {
                        agentId = part.value
                    }
                }
                else -> Unit
            }
            part.dispose()
        }
        val transcript = audioBytes?.let { bytes ->
            transcribeEnglishAudioWithSarvam(
                audioBytes = bytes,
                fileName = fileName,
                contentType = contentType,
                agentId = agentId,
                clientIpAddress = call.request.local.remoteAddress,
            )
        }
        if (transcript.isNullOrBlank()) {
            call.respondText(
                "Voice input failed — type instead.",
                ContentType.Text.Plain,
                HttpStatusCode.BadRequest,
            )
        } else {
            call.respondText(transcript, ContentType.Text.Plain)
        }
    }

    post("/htmx/voice/tts") {
        val parameters = call.receiveParameters()
        val textToSpeak = parameters["text"].orEmpty()
        val agentId = parameters["agentId"]
        val audioBytes = synthesizeEnglishSpeechWithElevenLabs(
            text = textToSpeak,
            agentId = agentId,
            clientIpAddress = call.request.local.remoteAddress,
        )
        if (audioBytes == null) {
            call.respondText(
                "Spoken reply unavailable — read the text instead.",
                ContentType.Text.Plain,
                HttpStatusCode.BadRequest,
            )
        } else {
            call.respond(ByteArrayContent(audioBytes, ContentType.Audio.MPEG))
        }
    }
}
