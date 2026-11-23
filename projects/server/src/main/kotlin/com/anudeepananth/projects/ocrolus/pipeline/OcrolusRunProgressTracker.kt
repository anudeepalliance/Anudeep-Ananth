package com.anudeepananth.projects.ocrolus.pipeline

import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusRunProgressMessage
import com.anudeepananth.projects.ocrolus.OcrolusRunResult
import com.anudeepananth.projects.ocrolus.OcrolusRunStep
import com.anudeepananth.projects.ocrolus.OcrolusRunUsage
import com.anudeepananth.projects.ocrolus.OcrolusStepStatus
import com.anudeepananth.projects.ocrolus.data.OcrolusRunStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Holds the latest state of one run while it executes. Every change is saved to storage and
 * pushed to [liveRun], which is what the live progress page listens to.
 */
class OcrolusRunProgressTracker(initialRun: OcrolusAssignmentRun, private val runStorage: OcrolusRunStorage) {
    val liveRun = MutableStateFlow(initialRun)
    private val updateLock = Mutex()

    val currentRun: OcrolusAssignmentRun
        get() = liveRun.value

    /** Applies [change] to the run, saves the new version and publishes it to live listeners. */
    suspend fun updateRunAndPersist(change: (OcrolusAssignmentRun) -> OcrolusAssignmentRun) {
        updateLock.withLock {
            val changedRun = change(liveRun.value)
            runStorage.saveRun(changedRun)
            liveRun.value = changedRun
        }
    }

    suspend fun updateResult(change: (OcrolusRunResult) -> OcrolusRunResult) {
        updateRunAndPersist {
            it.copy(result = change(it.result))
        }
    }

    suspend fun updateUsage(change: (OcrolusRunUsage) -> OcrolusRunUsage) {
        updateRunAndPersist {
            it.copy(usage = change(it.usage))
        }
    }

    suspend fun markStepStatus(stepKey: String, status: OcrolusStepStatus) {
        val nowEpochMilliseconds = System.currentTimeMillis()
        updateStep(stepKey) { step ->
            when (status) {
                OcrolusStepStatus.RUNNING -> step.copy(
                    status = status,
                    startedAtEpochMilliseconds = nowEpochMilliseconds
                )

                OcrolusStepStatus.COMPLETED, OcrolusStepStatus.FAILED -> step.copy(
                    status = status,
                    completedAtEpochMilliseconds = nowEpochMilliseconds
                )

                OcrolusStepStatus.PENDING -> step.copy(status = status)
            }
        }
    }

    /** Adds one line of live progress under the step. */
    suspend fun reportProgress(stepKey: String, text: String) {
        updateStep(stepKey) { step ->
            step.copy(
                progressMessages = step.progressMessages + OcrolusRunProgressMessage(
                    System.currentTimeMillis(),
                    text
                )
            )
        }
    }

    private suspend fun updateStep(stepKey: String, change: (OcrolusRunStep) -> OcrolusRunStep) {
        updateRunAndPersist { run ->
            run.copy(
                steps = run.steps.map { step ->
                    if (step.stepKey == stepKey) change(step) else step
                },
            )
        }
    }
}
