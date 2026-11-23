package com.anudeepananth.projects.ocrolus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where a whole assignment run currently stands. */
@Serializable
enum class OcrolusRunStatus { QUEUED, RUNNING, COMPLETED, FAILED }

/** Where one step inside a run currently stands. */
@Serializable
enum class OcrolusStepStatus { PENDING, RUNNING, COMPLETED, FAILED }

/** One line of live progress shown under a step while it runs. */
@Serializable
data class OcrolusRunProgressMessage(val atEpochMilliseconds: Long, val text: String)

/**
 * One of the fixed pipeline steps, with its live state for a particular run.
 *
 * @property stepKey stable identifier such as `score-csm-notes`
 * @property isPerformedByModel true when an AI model does the work, false when plain code does
 */
@Serializable
data class OcrolusRunStep(
    val stepKey: String,
    val title: String,
    val isPerformedByModel: Boolean,
    val status: OcrolusStepStatus = OcrolusStepStatus.PENDING,
    val startedAtEpochMilliseconds: Long? = null,
    val completedAtEpochMilliseconds: Long? = null,
    val progressMessages: List<OcrolusRunProgressMessage> = emptyList(),
)

/** One edit the review step made to an email, and which of the five review passes asked for it. */
@Serializable
data class OcrolusOutreachChange(
    val before: String = "",
    val after: String = "",
    val passNumber: Int = 0,
    val reason: String = "",
)

/**
 * The final outreach email for one account, with the review step's scorecard.
 *
 * @property salesinessScoreFirst 1-10 score before revisions, where 1 is a helpful note and 10 a hard sell
 * @property spamScoreFinal 0-100 score after revisions; 80 or more passes
 * @property flaggedForManualReview things a person must confirm before the email is sent
 */
@Serializable
data class OcrolusOutreachMessage(
    val accountId: String,
    val accountName: String = "",
    val finalSubject: String = "",
    val finalBody: String = "",
    val wordCount: Int = 0,
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

/**
 * The assignment's Part 3 answer: how to tell whether the campaign worked.
 *
 * @property overview one sentence naming the main success measure, the comparison group and the time window
 * @property measuredItems one sentence per thing to measure (early signals, guardrail, caveat), shown as a list
 */
@Serializable
data class OcrolusMeasurementPlan(
    val overview: String = "",
    val measuredItems: List<String> = emptyList(),
)

/** The assignment's "one thing the AI got wrong" answer, taken from the review step's change logs. */
@Serializable
data class OcrolusAiCorrectionSummary(
    val error: String = "",
    val whyItMatters: String = "",
    val howItWasCaught: String = "",
    val fix: String = "",
)

/**
 * Everything a run produces. Fields fill in one by one as steps finish, so the live page can show partial results.
 *
 * @property topAccountIds the ranked top 15 account ids for the chosen product
 * @property outreachStrategyText the model's strategy for the top 3 accounts, as written
 * @property outreachFirstDraftsText the model's first email drafts, before the review step edited them
 */
@Serializable
data class OcrolusRunResult(
    val chosenProduct: OcrolusAddOnProduct? = null,
    val fraudShieldPriorityTotal: Double = 0.0,
    val cashFlowInsightsPriorityTotal: Double = 0.0,
    val incomeVerifyPriorityTotal: Double = 0.0,
    val productRationale: String = "",
    val scoredAccounts: List<OcrolusScoredAccount> = emptyList(),
    val topAccountIds: List<String> = emptyList(),
    val outreachStrategyText: String = "",
    val outreachFirstDraftsText: String = "",
    val outreachMessages: List<OcrolusOutreachMessage> = emptyList(),
    val outreachStrategyUpdates: String = "",
    val aiCorrectionSummary: OcrolusAiCorrectionSummary? = null,
    val measurementPlan: OcrolusMeasurementPlan? = null,
)

/** How much model work a run used. */
@Serializable
data class OcrolusRunUsage(
    val modelCallCount: Int = 0,
    val promptTokens: Long = 0,
    val completionTokens: Long = 0,
    val fallbackModelIdsUsed: List<String> = emptyList(),
)

/**
 * One complete attempt at the assignment by one AI model. Saved as one MongoDB document.
 *
 * @property runId short id used in the page address and as the MongoDB document id
 * @property isFeatured true for the reference run the assignment page points visitors to
 * @property displayLabel optional name the presenter gives the run
 * @property presenterNotes optional notes the presenter adds for the interview
 */
@Serializable
data class OcrolusAssignmentRun(
    @SerialName("_id") val runId: String,
    val assignmentName: String,
    val status: OcrolusRunStatus,
    val modelId: String,
    val modelDisplayName: String,
    val isFreeModel: Boolean,
    val startedAtEpochMilliseconds: Long,
    val completedAtEpochMilliseconds: Long? = null,
    val durationMilliseconds: Long? = null,
    val isFeatured: Boolean = false,
    val displayLabel: String = "",
    val presenterNotes: String = "",
    val steps: List<OcrolusRunStep> = emptyList(),
    val result: OcrolusRunResult = OcrolusRunResult(),
    val usage: OcrolusRunUsage = OcrolusRunUsage(),
    val errorMessage: String? = null,
)

/** The few fields the completed-runs list needs, so the list does not download every full run. */
@Serializable
data class OcrolusRunSummary(
    val runId: String,
    val assignmentName: String,
    val status: OcrolusRunStatus,
    val modelDisplayName: String,
    val isFreeModel: Boolean,
    val startedAtEpochMilliseconds: Long,
    val completedAtEpochMilliseconds: Long?,
    val durationMilliseconds: Long?,
    val isFeatured: Boolean,
    val displayLabel: String,
    val chosenProductDisplayName: String?,
)

/**
 * A saved record of one request to an AI model, so visitors can read the real prompt and reply.
 *
 * @property stepKey the pipeline step that made the call
 */
@Serializable
data class OcrolusModelCallLog(
    val runId: String,
    val stepKey: String,
    val modelId: String,
    val promptText: String,
    val responseText: String,
    val wasSuccessful: Boolean,
    val errorMessage: String? = null,
    val durationMilliseconds: Long,
    val promptTokens: Long = 0,
    val completionTokens: Long = 0,
    val createdAtEpochMilliseconds: Long,
)
