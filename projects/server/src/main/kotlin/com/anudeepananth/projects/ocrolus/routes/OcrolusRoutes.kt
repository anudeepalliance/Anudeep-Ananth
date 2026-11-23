package com.anudeepananth.projects.ocrolus.routes

import com.anudeepananth.projects.ocrolus.OcrolusApiError
import com.anudeepananth.projects.ocrolus.OcrolusApiPaths
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentOverview
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.ocrolus.OcrolusRunSummary
import com.anudeepananth.projects.ocrolus.OcrolusStartRunRequest
import com.anudeepananth.projects.ocrolus.OcrolusStartRunResponse
import com.anudeepananth.projects.ocrolus.OcrolusUpdateRunRequest
import com.anudeepananth.projects.ocrolus.data.OcrolusRunStorage
import com.anudeepananth.projects.ocrolus.model.OcrolusModelCatalogue
import com.anudeepananth.projects.ocrolus.pipeline.OCROLUS_ASSIGNMENT_NAME
import com.anudeepananth.projects.ocrolus.pipeline.OcrolusRunCoordinator
import com.anudeepananth.projects.ocrolus.pipeline.OcrolusStartRunOutcome
import com.anudeepananth.projects.ocrolus.pipeline.ocrolusPipelineStepDescriptions
import com.anudeepananth.projects.util.ConfidentialObjects
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

private const val RUN_ID_PARAMETER = "runId"
private val runEventJson = Json {
    encodeDefaults = true
}

/** All `/api/ocrolus/...` endpoints: start a run, watch it live, and list, read, edit or delete saved runs. */
fun Route.ocrolusRoutes(runStorage: OcrolusRunStorage, runCoordinator: OcrolusRunCoordinator) {

    get(OcrolusApiPaths.ASSIGNMENT_OVERVIEW) {
        call.respond(
            OcrolusAssignmentOverview(
                assignmentName = OCROLUS_ASSIGNMENT_NAME,
                pipelineSteps = ocrolusPipelineStepDescriptions,
                availableModels = OcrolusModelCatalogue.modelsOfferedToVisitors(),
                activeRunId = runCoordinator.activeRunId,
                featuredRunId = runStorage.fetchFeaturedRunId(),
                publicRunsRemainingToday = runCoordinator.countPublicRunsRemainingToday(),
            ),
        )
    }

    post(OcrolusApiPaths.RUNS) {
        val startRunRequest = call.receive<OcrolusStartRunRequest>()
        val chosenModel = OcrolusModelCatalogue.findOfferedModel(startRunRequest.modelId)
        if (chosenModel == null) {
            call.respond(HttpStatusCode.BadRequest, OcrolusApiError("Unknown model."))
            return@post
        }
        if (!chosenModel.isFree && startRunRequest.adminPasscode != ConfidentialObjects.ADMIN_PASSCODE) {
            call.respond(HttpStatusCode.Forbidden, OcrolusApiError("Paid models need the admin passcode."))
            return@post
        }
        when (val startOutcome = runCoordinator.startRun(chosenModel)) {
            is OcrolusStartRunOutcome.Started -> call.respond(
                HttpStatusCode.Accepted,
                OcrolusStartRunResponse(startOutcome.runId)
            )

            is OcrolusStartRunOutcome.AnotherRunIsActive -> call.respond(
                HttpStatusCode.Conflict,
                OcrolusApiError(
                    "A run is already in progress. You can watch it live.",
                    activeRunId = startOutcome.activeRunId
                ),
            )

            OcrolusStartRunOutcome.DailyLimitReached -> call.respond(
                HttpStatusCode.TooManyRequests,
                OcrolusApiError("Today's limit of free runs has been reached. Please look at a completed run instead."),
            )
        }
    }

    get(OcrolusApiPaths.RUNS) {
        call.respond(
            runStorage.fetchRunListFieldsNewestFirst()
                .sortedByDescending {
                    it.isFeatured
                }
                .map {
                    it.toRunSummary()
                },
        )
    }

    get(OcrolusApiPaths.run("{$RUN_ID_PARAMETER}")) {
        val run = findRunOrRespondNotFound(call, runStorage, runCoordinator) ?: return@get
        call.respond(run)
    }

    get(OcrolusApiPaths.runModelCalls("{$RUN_ID_PARAMETER}")) {
        call.respond(runStorage.fetchModelCallLogsForRun(call.parameters[RUN_ID_PARAMETER].orEmpty()))
    }

    // Live progress: sends the whole run each time it changes, and stops once the run has finished.
    sse(OcrolusApiPaths.runEvents("{$RUN_ID_PARAMETER}")) {
        val runId = call.parameters[RUN_ID_PARAMETER].orEmpty()
        val liveRunUpdates = runCoordinator.liveRunUpdates(runId)
        if (liveRunUpdates == null) {
            runStorage.fetchRunById(runId)?.let {
                send(ServerSentEvent(data = runEventJson.encodeToString(OcrolusAssignmentRun.serializer(), it)))
            }
            return@sse
        }
        heartbeat {
            period = 15.seconds
        }
        var hasSentFinishedRun = false
        liveRunUpdates
            .takeWhile {
                !hasSentFinishedRun
            }
            .collect { run ->
                send(ServerSentEvent(data = runEventJson.encodeToString(OcrolusAssignmentRun.serializer(), run)))
                hasSentFinishedRun = run.status == OcrolusRunStatus.COMPLETED || run.status == OcrolusRunStatus.FAILED
                if (hasSentFinishedRun) throw FinishedRunSentSignal()
            }
    }

    patch(OcrolusApiPaths.run("{$RUN_ID_PARAMETER}")) {
        if (!hasValidAdminPasscode(call)) return@patch
        val updateRunRequest = call.receive<OcrolusUpdateRunRequest>()
        val wasFound = runStorage.updateRunEditableFields(
            runId = call.parameters[RUN_ID_PARAMETER].orEmpty(),
            displayLabel = updateRunRequest.displayLabel,
            presenterNotes = updateRunRequest.presenterNotes,
            isFeatured = updateRunRequest.isFeatured,
        )
        if (!wasFound) {
            call.respond(HttpStatusCode.NotFound, OcrolusApiError("No run with that id."))
            return@patch
        }
        call.respond(checkNotNull(runStorage.fetchRunById(call.parameters[RUN_ID_PARAMETER].orEmpty())))
    }

    // Anyone may delete a run (this is a public demo), except the one in progress and the featured
    // reference run, which needs the admin passcode so a visitor cannot remove it by accident.
    delete(OcrolusApiPaths.run("{$RUN_ID_PARAMETER}")) {
        val runId = call.parameters[RUN_ID_PARAMETER].orEmpty()
        if (runId == runCoordinator.activeRunId) {
            call.respond(HttpStatusCode.Conflict, OcrolusApiError("This run is still in progress."))
            return@delete
        }
        val isFeaturedRun = runStorage.fetchFeaturedRunId() == runId
        val hasAdminPasscode =
            call.request.headers[OcrolusApiPaths.ADMIN_PASSCODE_HEADER] == ConfidentialObjects.ADMIN_PASSCODE
        if (isFeaturedRun && !hasAdminPasscode) {
            call.respond(
                HttpStatusCode.Forbidden,
                OcrolusApiError("The reference run can only be deleted with the admin passcode.")
            )
            return@delete
        }
        if (runStorage.deleteRunAndItsModelCallLogs(runId)) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(HttpStatusCode.NotFound, OcrolusApiError("No run with that id."))
        }
    }
}

/** Ends the live stream cleanly after the finished run has been sent. */
private class FinishedRunSentSignal : kotlinx.coroutines.CancellationException("Finished run sent")

/** Returns the live in-memory run when it is in progress here (freshest), otherwise the saved one. */
private suspend fun findRunOrRespondNotFound(
    call: ApplicationCall,
    runStorage: OcrolusRunStorage,
    runCoordinator: OcrolusRunCoordinator,
): OcrolusAssignmentRun? {
    val runId = call.parameters[RUN_ID_PARAMETER].orEmpty()
    val run = runCoordinator.liveRunUpdates(runId)?.value ?: runStorage.fetchRunById(runId)
    if (run == null) call.respond(HttpStatusCode.NotFound, OcrolusApiError("No run with that id."))
    return run
}

private suspend fun hasValidAdminPasscode(call: ApplicationCall): Boolean {
    val isValid = call.request.headers[OcrolusApiPaths.ADMIN_PASSCODE_HEADER] == ConfidentialObjects.ADMIN_PASSCODE
    if (!isValid) call.respond(HttpStatusCode.Forbidden, OcrolusApiError("Wrong admin passcode."))
    return isValid
}

private fun OcrolusAssignmentRun.toRunSummary() = OcrolusRunSummary(
    runId = runId,
    assignmentName = assignmentName,
    status = status,
    modelDisplayName = modelDisplayName,
    isFreeModel = isFreeModel,
    startedAtEpochMilliseconds = startedAtEpochMilliseconds,
    completedAtEpochMilliseconds = completedAtEpochMilliseconds,
    durationMilliseconds = durationMilliseconds,
    isFeatured = isFeatured,
    displayLabel = displayLabel,
    chosenProductDisplayName = result.chosenProduct?.displayName,
)
