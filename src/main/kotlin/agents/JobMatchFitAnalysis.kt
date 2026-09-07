package agents

import kotlinx.serialization.Serializable

/**
 * Structured Job Matching fit card returned to HTMX and persisted usage callers.
 */
@Serializable
data class JobMatchFitAnalysis(
    val fitScore: Int,
    val strengths: List<String> = emptyList(),
    val gaps: List<String> = emptyList(),
    val softReject: Boolean = false,
    val softRejectReasons: List<String> = emptyList(),
    val preferenceWarnings: List<String> = emptyList(),
    val talkingPoints: List<String> = emptyList(),
    val summary: String = "",
)

/**
 * Outcome wrapper so routes can render errors without throwing past HTMX.
 */
sealed class JobMatchFitAnalysisOutcome {
    data class Success(
        val fitAnalysis: JobMatchFitAnalysis,
        val eligibilityPrecheck: JobMatchEligibilityPrecheckResult,
    ) : JobMatchFitAnalysisOutcome()

    data class Failure(
        val errorMessage: String,
        val eligibilityPrecheck: JobMatchEligibilityPrecheckResult? = null,
    ) : JobMatchFitAnalysisOutcome()
}

/**
 * Outcome wrapper for free-form agent chat turns.
 */
sealed class AgentChatTurnOutcome {
    data class Success(val replyText: String, val agentId: String) : AgentChatTurnOutcome()
    data class Failure(val errorMessage: String, val agentId: String) : AgentChatTurnOutcome()
}
