package voice

import kotlin.test.Test
import kotlin.test.assertEquals

class SarvamSpeechToTextTest {

    @Test
    fun `strips browser codec suffix so Sarvam accepts the file type`() {
        assertEquals("audio/webm", normalizeSarvamAudioContentType("audio/webm;codecs=opus"))
        assertEquals("audio/webm", normalizeSarvamAudioContentType("audio/webm"))
        assertEquals("audio/mp4", normalizeSarvamAudioContentType("audio/mp4;codecs=mp4a.40.2"))
        assertEquals("audio/ogg", normalizeSarvamAudioContentType("audio/ogg;codecs=opus"))
        assertEquals("audio/wav", normalizeSarvamAudioContentType("audio/wav"))
    }

    @Test
    fun `picks a matching filename extension for the cleaned content type`() {
        assertEquals(
            "voice.webm",
            fileNameForSarvamContentType(contentType = "audio/webm", originalFileName = "voice.webm"),
        )
        assertEquals(
            "voice.m4a",
            fileNameForSarvamContentType(contentType = "audio/mp4", originalFileName = "voice.webm"),
        )
    }
}
