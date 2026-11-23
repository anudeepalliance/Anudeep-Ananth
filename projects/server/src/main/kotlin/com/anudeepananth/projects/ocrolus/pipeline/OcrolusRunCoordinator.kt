package com.anudeepananth.projects.ocrolus.pipeline

import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusModelOption
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.ocrolus.data.OcrolusRunStorage
import com.anudeepananth.projects.ocrolus.model.AssignmentModelCaller
import com.anudeepananth.projects.ocrolus.notification.OcrolusRunEventNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

/** How many free-model runs visitors may start per day (UTC), to protect the free OpenRouter quota. */
const val MAXIMUM_PUBLIC_RUNS_PER_DAY = 10

private const val MAXIMUM_RUN_DURATION_MINUTES = 20
private const val MAXIMUM_RUN_DURATION_MILLISECONDS = MAXIMUM_RUN_DURATION_MINUTES * 60 * 1000L
private const val RUN_ID_LENGTH = 8
private const val RUN_ID_ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789"

/** What happened when someone asked to start a run. */
sealed interface OcrolusStartRunOutcome {
    data class Started(val runId: String) : OcrolusStartRunOutcome
    data class AnotherRunIsActive(val activeRunId: String) : OcrolusStartRunOutcome
    data object DailyLimitReached : OcrolusStartRunOutcome
}

/**
 * Starts runs in the background and keeps track of the one in progress.
 * Only one run may be active at a time, so visitors who arrive mid-run watch it instead of starting another.
 */
class OcrolusRunCoordinator(
    private val runStorage: OcrolusRunStorage,
    private val modelCaller: AssignmentModelCaller,
    private val runEventNotifier: OcrolusRunEventNotifier,
    private val backgroundScope: CoroutineScope,
) {
    private val logger = LoggerFactory.getLogger("OcrolusRunCoordinator")
    private val startLock = Mutex()
    private val progressTrackersByRunId = ConcurrentHashMap<String, OcrolusRunProgressTracker>()

    @Volatile
    var activeRunId: String? = null
        private set

    /** Live updates for a run that is in progress on this server, or null when the run is not active here. */
    fun liveRunUpdates(runId: String): StateFlow<OcrolusAssignmentRun>? = progressTrackersByRunId[runId]?.liveRun

    /** How many more free runs visitors may start today. */
    suspend fun countPublicRunsRemainingToday(): Int {
        val startOfTodayEpochMilliseconds =
            LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        return (MAXIMUM_PUBLIC_RUNS_PER_DAY - runStorage.countFreeModelRunsStartedSince(startOfTodayEpochMilliseconds)).coerceAtLeast(
            0
        )
    }

    /** Starts a run with [chosenModel] unless another run is active or today's free-run limit is used up. */
    suspend fun startRun(chosenModel: OcrolusModelOption): OcrolusStartRunOutcome = startLock.withLock {
        activeRunId?.let {
            return OcrolusStartRunOutcome.AnotherRunIsActive(it)
        }
        if (chosenModel.isFree && countPublicRunsRemainingToday() == 0) return OcrolusStartRunOutcome.DailyLimitReached

        val newRun = OcrolusAssignmentRun(
            runId = generateRunId(),
            assignmentName = OCROLUS_ASSIGNMENT_NAME,
            status = OcrolusRunStatus.RUNNING,
            modelId = chosenModel.modelId,
            modelDisplayName = chosenModel.displayName,
            isFreeModel = chosenModel.isFree,
            startedAtEpochMilliseconds = System.currentTimeMillis(),
            steps = buildPendingRunSteps(),
        )
        runStorage.saveRun(newRun)
        val progressTracker = OcrolusRunProgressTracker(newRun, runStorage)
        progressTrackersByRunId[newRun.runId] = progressTracker
        activeRunId = newRun.runId

        backgroundScope.launch {
            performRunToTheEnd(chosenModel, progressTracker)
        }
        OcrolusStartRunOutcome.Started(newRun.runId)
    }

    private suspend fun performRunToTheEnd(
        chosenModel: OcrolusModelOption,
        progressTracker: OcrolusRunProgressTracker
    ) {
        runEventNotifier.sendRunEventNotification(progressTracker.currentRun)
        try {
            val hasFinishedInTime = withTimeoutOrNull(MAXIMUM_RUN_DURATION_MILLISECONDS) {
                OcrolusAssignmentRunPipeline(chosenModel, modelCaller, runStorage, progressTracker).performAllSteps()
                true
            } ?: false
            if (hasFinishedInTime) {
                finishRun(progressTracker, OcrolusRunStatus.COMPLETED, errorMessage = null)
            } else {
                finishRun(
                    progressTracker,
                    OcrolusRunStatus.FAILED,
                    "The run took longer than $MAXIMUM_RUN_DURATION_MINUTES minutes and was stopped."
                )
            }
        } catch (exception: Exception) {
            logger.error("Run ${progressTracker.currentRun.runId} failed: ${exception.message}", exception)
            finishRun(progressTracker, OcrolusRunStatus.FAILED, exception.message ?: "Unknown error")
        } finally {
            activeRunId = null
            progressTrackersByRunId.remove(progressTracker.currentRun.runId)
        }
    }

    /** Records the final status, ticks the last "save and notify" step and sends the Discord update. */
    private suspend fun finishRun(
        progressTracker: OcrolusRunProgressTracker,
        finalStatus: OcrolusRunStatus,
        errorMessage: String?
    ) {
        val completedAtEpochMilliseconds = System.currentTimeMillis()
        if (finalStatus == OcrolusRunStatus.COMPLETED) {
            progressTracker.markStepStatus(
                OcrolusStepKeys.SAVE_AND_NOTIFY,
                com.anudeepananth.projects.ocrolus.OcrolusStepStatus.RUNNING
            )
            progressTracker.reportProgress(
                OcrolusStepKeys.SAVE_AND_NOTIFY,
                "Saved to MongoDB; summary sent to Discord through n8n"
            )
            progressTracker.markStepStatus(
                OcrolusStepKeys.SAVE_AND_NOTIFY,
                com.anudeepananth.projects.ocrolus.OcrolusStepStatus.COMPLETED
            )
        }
        progressTracker.updateRunAndPersist { run ->
            run.copy(
                status = finalStatus,
                errorMessage = errorMessage,
                completedAtEpochMilliseconds = completedAtEpochMilliseconds,
                durationMilliseconds = completedAtEpochMilliseconds - run.startedAtEpochMilliseconds,
            )
        }
        runEventNotifier.sendRunEventNotification(progressTracker.currentRun)
    }

    private fun generateRunId(): String = (1..RUN_ID_LENGTH).map {
        RUN_ID_ALPHABET.random()
    }.joinToString("")
}
