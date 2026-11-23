package com.anudeepananth.projects.ocrolus

import kotlinx.serialization.Serializable

/** Addresses of the server endpoints, shared so the web app and server can never disagree. */
object OcrolusApiPaths {
    const val ASSIGNMENT_OVERVIEW = "/api/ocrolus/assignment"
    const val RUNS = "/api/ocrolus/runs"
    const val ADMIN_PASSCODE_HEADER = "X-Admin-Passcode"

    fun run(runId: String): String = "$RUNS/$runId"
    fun runEvents(runId: String): String = "$RUNS/$runId/events"
    fun runModelCalls(runId: String): String = "$RUNS/$runId/model-calls"
}

/** Which company's service a model is called through. */
@Serializable
enum class OcrolusModelProvider { OPENROUTER, MINIMAX }

/**
 * An AI model a visitor can pick for a run.
 *
 * @property isFree true when visitors may run it without the admin passcode: OpenRouter's free models,
 * and MiniMax, which Anudeep's MiniMax token plan already pays for
 */
@Serializable
data class OcrolusModelOption(
    val modelId: String,
    val displayName: String,
    val isFree: Boolean,
    val provider: OcrolusModelProvider = OcrolusModelProvider.OPENROUTER,
)

/** A pipeline step as described on the assignment page before any run starts. */
@Serializable
data class OcrolusPipelineStepDescription(
    val stepKey: String,
    val title: String,
    val isPerformedByModel: Boolean,
    val description: String,
)

/**
 * Everything the assignment page needs before a run starts.
 *
 * @property activeRunId id of a run in progress right now, or null; only one run may be active at a time
 * @property featuredRunId id of the reference run to point visitors to, or null when none is marked
 * @property publicRunsRemainingToday how many more free runs visitors may start today
 */
@Serializable
data class OcrolusAssignmentOverview(
    val assignmentName: String,
    val pipelineSteps: List<OcrolusPipelineStepDescription>,
    val availableModels: List<OcrolusModelOption>,
    val activeRunId: String?,
    val featuredRunId: String?,
    val publicRunsRemainingToday: Int,
)

/** Request to start a run. [adminPasscode] is only needed for paid models. */
@Serializable
data class OcrolusStartRunRequest(val modelId: String, val adminPasscode: String? = null)

/** Reply to a successful start request. */
@Serializable
data class OcrolusStartRunResponse(val runId: String)

/** Error reply. [activeRunId] is set when the request was refused because another run is in progress. */
@Serializable
data class OcrolusApiError(val message: String, val activeRunId: String? = null)

/** Admin edit of a saved run; null fields are left unchanged. */
@Serializable
data class OcrolusUpdateRunRequest(
    val displayLabel: String? = null,
    val presenterNotes: String? = null,
    val isFeatured: Boolean? = null,
)
