package com.anudeepananth.projects.ocrolus.pipeline

import com.anudeepananth.projects.ocrolus.OcrolusOutreachChange
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt

/** Forgiving JSON reader for model replies: unknown fields are ignored and small format slips are tolerated. */
internal val modelReplyJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
}

/**
 * Reads a whole number that a model may have written as `7`, `7.0`, `"7"`, `"-"` or `null`.
 * Anything that is not a number becomes null, so one sloppy cell never rejects a whole reply.
 */
internal object LenientWholeNumberSerializer : KSerializer<Int?> {
    override val descriptor = PrimitiveSerialDescriptor("LenientWholeNumber", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Int? {
        val jsonElement = (decoder as JsonDecoder).decodeJsonElement()
        return (jsonElement as? JsonPrimitive)?.content?.trim()?.toDoubleOrNull()?.roundToInt()
    }

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) encoder.encodeNull() else encoder.encodeInt(value)
    }
}

/** Reads a yes/no value that a model may have written as `true`, `"Y"`, `"yes"` or `1`. */
internal object LenientYesNoSerializer : KSerializer<Boolean> {
    override val descriptor = PrimitiveSerialDescriptor("LenientYesNo", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Boolean {
        val jsonElement = (decoder as JsonDecoder).decodeJsonElement()
        return (jsonElement as? JsonPrimitive)?.content?.trim()?.lowercase() in setOf("true", "y", "yes", "1")
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }
}

/** The model's note scores for one account; a null score means "already adopted". */
@Serializable
internal data class NoteScoreReplyRow(
    val accountId: String,
    @Serializable(with = LenientWholeNumberSerializer::class) val fraudShieldScore: Int? = null,
    @Serializable(with = LenientWholeNumberSerializer::class) val cashFlowInsightsScore: Int? = null,
    @Serializable(with = LenientWholeNumberSerializer::class) val incomeVerifyScore: Int? = null,
    val evidenceQuote: String = "",
)

@Serializable
internal data class NoteScoringReply(val accounts: List<NoteScoreReplyRow>)

@Serializable
internal data class RiskFlagReplyRow(
    val accountId: String,
    @Serializable(with = LenientYesNoSerializer::class) val isFlaggedAsRisk: Boolean = false,
    val evidenceQuote: String = "",
)

@Serializable
internal data class RiskFlagReply(val accounts: List<RiskFlagReplyRow>)

@Serializable
internal data class AnomalyReplyRow(
    val accountId: String,
    @Serializable(with = LenientWholeNumberSerializer::class) val anomalyScore: Int? = null,
    val anomalyReason: String = "-",
)

@Serializable
internal data class AnomalyReply(val accounts: List<AnomalyReplyRow>)

@Serializable
internal data class OutreachReviewReplyEmail(
    val accountId: String,
    val finalSubject: String = "",
    val finalBody: String = "",
    val claimsChecked: Int = 0,
    val claimsFixed: Int = 0,
    val salesinessScoreFirst: Int = 0,
    val salesinessScoreFinal: Int = 0,
    val spamScoreFirst: Int = 0,
    val spamScoreFinal: Int = 0,
    val verdict: String = "",
    val changeLog: List<OcrolusOutreachChange> = emptyList(),
    val flaggedForManualReview: List<String> = emptyList(),
    val readiness: String = "",
)

@Serializable
internal data class OutreachReviewReply(val emails: List<OutreachReviewReplyEmail>, val strategyUpdates: String = "")
