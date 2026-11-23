package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anudeepananth.projects.design.ActivityFeedLine
import com.anudeepananth.projects.design.ActivityTone
import com.anudeepananth.projects.design.AddSpace
import com.anudeepananth.projects.design.BodyText
import com.anudeepananth.projects.design.ContentCard
import com.anudeepananth.projects.design.EmphasisCard
import com.anudeepananth.projects.design.EyebrowText
import com.anudeepananth.projects.design.HeadingText
import com.anudeepananth.projects.design.LabelText
import com.anudeepananth.projects.design.LivePill
import com.anudeepananth.projects.design.LoadingIndicator
import com.anudeepananth.projects.design.LocalSiteColours
import com.anudeepananth.projects.design.LocalWidthClass
import com.anudeepananth.projects.design.MessageWithAction
import com.anudeepananth.projects.design.NoticeBanner
import com.anudeepananth.projects.design.NoticeTone
import com.anudeepananth.projects.design.PrimaryButton
import com.anudeepananth.projects.design.ProgressStatus
import com.anudeepananth.projects.design.SecondaryButton
import com.anudeepananth.projects.design.SecondaryText
import com.anudeepananth.projects.design.StatTile
import com.anudeepananth.projects.design.StatusChip
import com.anudeepananth.projects.design.StepProgressBar
import com.anudeepananth.projects.design.SubheadingText
import com.anudeepananth.projects.design.TimelineStep
import com.anudeepananth.projects.design.WidthClass
import com.anudeepananth.projects.design.WrappingRow
import com.anudeepananth.projects.design.liveRunTimelineColumnWidth
import com.anudeepananth.projects.design.spacingDefault
import com.anudeepananth.projects.design.spacingMedium
import com.anudeepananth.projects.design.spacingSmall
import com.anudeepananth.projects.design.spacingTiny
import com.anudeepananth.projects.design.spacingXLarge
import com.anudeepananth.projects.design.spacingXSmall
import com.anudeepananth.projects.design.spacingXXLarge
import com.anudeepananth.projects.navigation.AppRoutePaths
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.ocrolus.OcrolusRunStep
import com.anudeepananth.projects.ocrolus.OcrolusStepStatus
import com.anudeepananth.projects.ocrolus.formatting.formatDurationAsMinutesAndSeconds
import com.anudeepananth.projects.ocrolus.formatting.isStillInProgress
import com.anudeepananth.projects.ocrolus.formatting.toProgressStatus
import com.anudeepananth.projects.ocrolus.formatting.toSentenceCaseLabel
import com.anudeepananth.projects.platform.readCurrentEpochMillisecondsFromBrowserClock
import kotlinx.coroutines.delay

private const val ELAPSED_TIMER_TICK_MILLISECONDS = 1000L
private const val MAXIMUM_VISIBLE_PROGRESS_MESSAGES = 4

/**
 * The live view of one run: a step timeline, an elapsed timer, a model call counter, and results that
 * appear as each step finishes. Shows a summary when the run completes and the error when it fails.
 * Every new copy of the run is passed to [onWatchedRunUpdated], so the page around it can react to the run's status.
 */
@Composable
fun OcrolusLiveRunSection(
    runId: String,
    wasAlreadyInProgressWhenPageOpened: Boolean,
    onWatchedRunUpdated: (OcrolusAssignmentRun) -> Unit,
    onTryAgain: () -> Unit,
    onNavigateToPath: (String) -> Unit,
) {
    val liveRunWatcher = remember(runId) {
        OcrolusLiveRunWatcher(runId)
    }
    LaunchedEffect(liveRunWatcher) {
        liveRunWatcher.followRunOnServerUntilFinished()
    }
    val run = liveRunWatcher.latestRun
    LaunchedEffect(run) {
        run?.let(onWatchedRunUpdated)
    }
    val loadFailureMessage = liveRunWatcher.loadFailureMessage

    Column(modifier = Modifier.fillMaxWidth()) {
        when {
            run != null -> LiveRunContent(run, wasAlreadyInProgressWhenPageOpened, onTryAgain, onNavigateToPath)
            loadFailureMessage != null -> MessageWithAction(
                title = "The run could not be loaded",
                body = loadFailureMessage,
                actionLabel = "Back to the assignment",
                onAction = onTryAgain,
            )

            else -> LoadingIndicator("Connecting to the run…")
        }
    }
}

@Composable
private fun LiveRunContent(
    run: OcrolusAssignmentRun,
    wasAlreadyInProgressWhenPageOpened: Boolean,
    onTryAgain: () -> Unit,
    onNavigateToPath: (String) -> Unit,
) {
    val widthClass = LocalWidthClass.current
    Column(modifier = Modifier.fillMaxWidth()) {
        if (wasAlreadyInProgressWhenPageOpened && run.status.isStillInProgress()) {
            NoticeBanner(
                text = "Someone already started a run, so you are watching it live. Only one run can be in progress at a time.",
                tone = NoticeTone.IN_PROGRESS,
            )
            AddSpace(spacingDefault)
        }
        LiveRunFactsCard(run)
        AddSpace(spacingXLarge)

        when (run.status) {
            OcrolusRunStatus.COMPLETED -> {
                RunCompletedSummaryCard(run, onNavigateToPath)
                AddSpace(spacingXLarge)
            }

            OcrolusRunStatus.FAILED -> {
                NoticeBanner(
                    text = "The run failed. ${run.errorMessage ?: "No reason was given."}",
                    tone = NoticeTone.PROBLEM,
                    actionLabel = "Try again",
                    onAction = onTryAgain,
                )
                AddSpace(spacingXLarge)
            }

            else -> Unit
        }

        if (widthClass == WidthClass.EXPANDED) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacingXXLarge), verticalAlignment = Alignment.Top) {
                RunStepTimelineCard(run.steps, Modifier.width(liveRunTimelineColumnWidth))
                LiveRunResults(run, Modifier.weight(1f))
            }
        } else {
            RunStepTimelineCard(run.steps, Modifier.fillMaxWidth())
            AddSpace(spacingXLarge)
            LiveRunResults(run, Modifier.fillMaxWidth())
        }
    }
}

/** The strip of run facts above the timeline: status, model, elapsed time and model calls so far. */
@Composable
private fun LiveRunFactsCard(run: OcrolusAssignmentRun) {
    val elapsedMilliseconds by produceState(
        initialValue = elapsedMillisecondsFor(run),
        run.status,
        run.durationMilliseconds,
        run.startedAtEpochMilliseconds
    ) {
        value = elapsedMillisecondsFor(run)
        while (run.status.isStillInProgress()) {
            delay(ELAPSED_TIMER_TICK_MILLISECONDS)
            value = elapsedMillisecondsFor(run)
        }
    }
    val completedStepCount = run.steps.count {
        it.status == OcrolusStepStatus.COMPLETED
    }
    ContentCard(modifier = Modifier.fillMaxWidth()) {
        WrappingRow(horizontalSpacing = spacingMedium) {
            StatusChip(run.status.toProgressStatus(), run.status.toSentenceCaseLabel())
            LabelText(run.modelDisplayName)
        }
        AddSpace(spacingXLarge)
        WrappingRow(horizontalSpacing = spacingXXLarge) {
            StatTile(
                formatDurationAsMinutesAndSeconds(elapsedMilliseconds),
                if (run.status.isStillInProgress()) "Elapsed" else "Time taken"
            )
            StatTile(run.usage.modelCallCount.toString(), "Model calls")
            StatTile("$completedStepCount/${run.steps.size}", "Steps done")
        }
        AddSpace(spacingXLarge)
        StepProgressBar(
            stepStatuses = run.steps.map {
                it.status.toProgressStatus()
            },
        )
    }
}

/** How long a run has taken so far, or its final duration once it has finished. */
private fun elapsedMillisecondsFor(run: OcrolusAssignmentRun): Long {
    val endEpochMilliseconds = run.completedAtEpochMilliseconds ?: readCurrentEpochMillisecondsFromBrowserClock()
    return run.durationMilliseconds ?: (endEpochMilliseconds - run.startedAtEpochMilliseconds)
}

@Composable
private fun RunCompletedSummaryCard(run: OcrolusAssignmentRun, onNavigateToPath: (String) -> Unit) {
    val siteColours = LocalSiteColours.current
    EmphasisCard(modifier = Modifier.fillMaxWidth()) {
        EyebrowText("Run complete", color = siteColours.onEmphasisBlockMuted)
        AddSpace(spacingSmall)
        HeadingText(
            text = run.result.chosenProduct?.let {
                "${it.displayName} is the product to cross-sell"
            } ?: "The assignment is finished",
            color = siteColours.onEmphasisBlock,
        )
        AddSpace(spacingSmall)
        BodyText(
            text = "${run.modelDisplayName} finished all ${run.steps.size} steps in ${
                formatDurationAsMinutesAndSeconds(
                    run.durationMilliseconds ?: 0
                )
            } using ${run.usage.modelCallCount} model calls. The run is saved, with every prompt and reply.",
            color = siteColours.onEmphasisBlock,
        )
        AddSpace(spacingDefault)
        SecondaryButton(
            text = "Open the full result",
            onClick = {
                onNavigateToPath(AppRoutePaths.ocrolusRunResult(run.runId))
            },
        )
    }
}

/**
 * The live pipeline tracker: a header with a "Live" pill, the current step number and a slim progress bar,
 * then every step. The step running now is highlighted with a spinning marker, its own ticking timer and a
 * feed of its latest progress messages; finished steps go quiet and the next step reads "Up next".
 * On phones the page scrolls to the running step each time a new step starts.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RunStepTimelineCard(steps: List<OcrolusRunStep>, modifier: Modifier) {
    val runningStepIndex = steps.indexOfFirst {
        it.status == OcrolusStepStatus.RUNNING
    }
    val upNextStepIndex = if (runningStepIndex >= 0) runningStepIndex + 1 else -1
    val completedStepCount = steps.count {
        it.status == OcrolusStepStatus.COMPLETED
    }
    val runningStepBringIntoViewRequester = remember {
        BringIntoViewRequester()
    }
    val isCompact = LocalWidthClass.current == WidthClass.COMPACT
    var lastRunningStepIndexScrolledTo by remember {
        mutableIntStateOf(-1)
    }
    LaunchedEffect(runningStepIndex) {
        // Only follow a step that starts while the visitor watches; opening the page does not jump.
        if (isCompact && runningStepIndex >= 0 && lastRunningStepIndexScrolledTo >= 0) {
            runningStepBringIntoViewRequester.bringIntoView()
        }
        lastRunningStepIndexScrolledTo = runningStepIndex
    }
    ContentCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EyebrowText("Pipeline")
            AddSpace(spacingSmall)
            if (runningStepIndex >= 0) {
                LivePill()
            }
            Box(modifier = Modifier.weight(1f))
            LabelText(
                text = when {
                    runningStepIndex >= 0 -> "Step ${runningStepIndex + 1} of ${steps.size}"
                    completedStepCount == steps.size -> "All ${steps.size} steps done"
                    else -> "$completedStepCount of ${steps.size} done"
                },
            )
        }
        AddSpace(spacingMedium)
        StepProgressBar(
            stepStatuses = steps.map {
                it.status.toProgressStatus()
            },
        )
        AddSpace(spacingXLarge)
        steps.forEachIndexed { stepIndex, step ->
            val isRunningStep = stepIndex == runningStepIndex
            TimelineStep(
                status = step.status.toProgressStatus(),
                isLastStep = stepIndex == steps.lastIndex,
                isHighlighted = isRunningStep,
                isConnectorFlowing = stepIndex == runningStepIndex - 1,
                modifier = if (isRunningStep) Modifier.bringIntoViewRequester(runningStepBringIntoViewRequester) else Modifier,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val titleColor = when (step.status) {
                        OcrolusStepStatus.RUNNING, OcrolusStepStatus.FAILED -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    if (isRunningStep) {
                        HeadingText(step.title, modifier = Modifier.weight(1f, fill = false), color = titleColor)
                    } else {
                        SubheadingText(step.title, modifier = Modifier.weight(1f, fill = false), color = titleColor)
                    }
                    AddSpace(spacingSmall)
                    StepPerformerTag(step.isPerformedByModel)
                }
                RunStepDetail(step, isUpNext = stepIndex == upNextStepIndex)
            }
        }
    }
}

/** How a progress message should look in the feed: a retry after a model problem, a switch to another model, or plain progress. */
private fun activityToneOf(progressMessageText: String): ActivityTone {
    val lowercaseText = progressMessageText.lowercase()
    return when {
        lowercaseText.contains("did not answer") || lowercaseText.contains("failed") || lowercaseText.contains("error") || lowercaseText.contains(
            "retry"
        ) -> ActivityTone.RETRYING

        lowercaseText.startsWith("trying") || lowercaseText.startsWith("switching") -> ActivityTone.SWITCHING
        else -> ActivityTone.PROGRESS
    }
}

/**
 * The small print under a step's title: how long a finished step took, "Up next" for the next step,
 * or, for the running step, a ticking timer and its latest messages, newest first.
 */
@Composable
private fun RunStepDetail(step: OcrolusRunStep, isUpNext: Boolean) {
    val stepStartedAt = step.startedAtEpochMilliseconds
    val stepCompletedAt = step.completedAtEpochMilliseconds
    Column {
        when (step.status) {
            OcrolusStepStatus.COMPLETED -> if (stepStartedAt != null && stepCompletedAt != null) {
                AddSpace(spacingTiny)
                SecondaryText(
                    "Done in ${formatDurationAsMinutesAndSeconds(stepCompletedAt - stepStartedAt)}",
                    color = LocalSiteColours.current.captionText
                )
            }

            OcrolusStepStatus.RUNNING, OcrolusStepStatus.FAILED -> {
                if (step.status == OcrolusStepStatus.RUNNING && stepStartedAt != null) {
                    AddSpace(spacingTiny)
                    RunningStepTimer(stepStartedAt)
                }
                val newestFirstMessages = step.progressMessages.reversed()
                val visibleMessages = newestFirstMessages.take(MAXIMUM_VISIBLE_PROGRESS_MESSAGES)
                visibleMessages.forEachIndexed { messageIndex, progressMessage ->
                    key(progressMessage.atEpochMilliseconds, progressMessage.text) {
                        ActivityFeedLine(
                            text = progressMessage.text,
                            tone = if (step.status == OcrolusStepStatus.FAILED) ActivityTone.RETRYING else activityToneOf(
                                progressMessage.text
                            ),
                            isLatest = messageIndex == 0,
                        )
                    }
                }
                val hiddenEarlierMessageCount = newestFirstMessages.size - visibleMessages.size
                if (hiddenEarlierMessageCount > 0) {
                    AddSpace(spacingXSmall)
                    SecondaryText("+$hiddenEarlierMessageCount earlier", color = LocalSiteColours.current.captionText)
                }
            }

            OcrolusStepStatus.PENDING -> if (isUpNext) {
                AddSpace(spacingTiny)
                SecondaryText("Up next", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

/** "Running for 1m 12s", counting up every second from when the step started. */
@Composable
private fun RunningStepTimer(stepStartedAtEpochMilliseconds: Long) {
    val runningMilliseconds by produceState(
        initialValue = readCurrentEpochMillisecondsFromBrowserClock() - stepStartedAtEpochMilliseconds,
        stepStartedAtEpochMilliseconds
    ) {
        while (true) {
            value = readCurrentEpochMillisecondsFromBrowserClock() - stepStartedAtEpochMilliseconds
            delay(ELAPSED_TIMER_TICK_MILLISECONDS)
        }
    }
    LabelText(
        "Running for ${formatDurationAsMinutesAndSeconds(runningMilliseconds)}",
        color = LocalSiteColours.current.runningContent
    )
}

/** The right-hand side of the live view: each result block appears as soon as the run has produced it. */
@Composable
private fun LiveRunResults(run: OcrolusAssignmentRun, modifier: Modifier) {
    val result = run.result
    val chosenProduct = result.chosenProduct
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacingXLarge)) {
        if (chosenProduct == null) {
            ContentCard(modifier = Modifier.fillMaxWidth()) {
                EyebrowText("Results")
                AddSpace(spacingSmall)
                BodyText(
                    text = if (run.status.isStillInProgress()) {
                        "Results will appear here as each step finishes: first the product pick, then the top 15 accounts, then the three emails."
                    } else {
                        "This run did not get far enough to produce results."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }
        ChosenProductCard(result, chosenProduct)
        if (result.productRationale.isNotBlank()) {
            ContentCard(modifier = Modifier.fillMaxWidth()) {
                EyebrowText("Why this product")
                AddSpace(spacingSmall)
                BodyText(result.productRationale)
            }
        }
        if (result.topAccountIds.isNotEmpty()) {
            Column {
                HeadingText("Top ${result.topAccountIds.size} accounts")
                AddSpace(spacingDefault)
                TopAccountsTable(result, chosenProduct)
            }
        }
        if (result.outreachMessages.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(spacingDefault)) {
                HeadingText("Outreach emails for the top ${result.outreachMessages.size}")
                result.outreachMessages.forEach { outreachMessage ->
                    OutreachEmailCard(outreachMessage)
                }
            }
        }
    }
}
