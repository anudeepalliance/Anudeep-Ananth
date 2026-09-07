package util.usage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

/**
 * One billable-ish call to an agent, speech-to-text, or text-to-speech provider.
 *
 * @property kind One of [UsageKind.AGENT], [UsageKind.STT], [UsageKind.TTS].
 * @property agentId Agent id when [kind] is agent (or voice tied to an agent); otherwise null.
 * @property visitorEnteredQueryText Chat message, job description, or speech-to-text transcript the visitor submitted. Null for spoken-reply events.
 */
@Serializable
data class AgentUsageLogDocument(
    @SerialName("_id")
    val id: String = ObjectId().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val kind: String,
    val agentId: String? = null,
    val ok: Boolean,
    val durationMs: Long,
    val inputChars: Int? = null,
    val visitorEnteredQueryText: String? = null,
    val errorMessage: String? = null,
    val clientIpAddress: String? = null,
    val sessionId: String? = null,
)

/**
 * Allowed values for [AgentUsageLogDocument.kind].
 */
object UsageKind {
    const val AGENT = "agent"
    const val STT = "stt"
    const val TTS = "tts"
}
