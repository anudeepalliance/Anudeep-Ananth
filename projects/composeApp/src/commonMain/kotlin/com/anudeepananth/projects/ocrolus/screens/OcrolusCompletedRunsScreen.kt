package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anudeepananth.projects.design.AddSpace
import com.anudeepananth.projects.design.BodyText
import com.anudeepananth.projects.design.ContentCard
import com.anudeepananth.projects.design.DataTable
import com.anudeepananth.projects.design.DataTableColumn
import com.anudeepananth.projects.design.DeleteIconButton
import com.anudeepananth.projects.design.HeroBackLink
import com.anudeepananth.projects.design.HeroBand
import com.anudeepananth.projects.design.LabelledValue
import com.anudeepananth.projects.design.LoadingIndicator
import com.anudeepananth.projects.design.LocalWidthClass
import com.anudeepananth.projects.design.MessageWithAction
import com.anudeepananth.projects.design.MonospaceText
import com.anudeepananth.projects.design.NoticeBanner
import com.anudeepananth.projects.design.NoticeTone
import com.anudeepananth.projects.design.PageBody
import com.anudeepananth.projects.design.SecondaryText
import com.anudeepananth.projects.design.StatusChip
import com.anudeepananth.projects.design.SubheadingText
import com.anudeepananth.projects.design.TagChip
import com.anudeepananth.projects.design.TagTone
import com.anudeepananth.projects.design.WidthClass
import com.anudeepananth.projects.design.WrappingRow
import com.anudeepananth.projects.design.spacingDefault
import com.anudeepananth.projects.design.spacingMedium
import com.anudeepananth.projects.design.spacingSmall
import com.anudeepananth.projects.design.spacingXSmall
import com.anudeepananth.projects.design.tableColumnWidthLarge
import com.anudeepananth.projects.design.tableColumnWidthMedium
import com.anudeepananth.projects.design.tableColumnWidthSmall
import com.anudeepananth.projects.navigation.AppRoutePaths
import com.anudeepananth.projects.ocrolus.OcrolusRunSummary
import com.anudeepananth.projects.ocrolus.api.OcrolusServerApi
import com.anudeepananth.projects.ocrolus.api.ServerReply
import com.anudeepananth.projects.ocrolus.formatting.formatDurationAsMinutesAndSeconds
import com.anudeepananth.projects.ocrolus.formatting.isStillInProgress
import com.anudeepananth.projects.ocrolus.formatting.toProgressStatus
import com.anudeepananth.projects.ocrolus.formatting.toSentenceCaseLabel
import com.anudeepananth.projects.platform.formatEpochMillisecondsAsViewerLocalDateAndTime
import kotlinx.coroutines.delay

private const val DELETION_CONFIRMATION_VISIBLE_MILLISECONDS = 4000L

/**
 * The list of saved runs: a table on wide windows and cards on narrow ones. Clicking a run opens it;
 * the bin button on a finished, non-featured run deletes it after a confirmation sheet.
 */
@Composable
fun OcrolusCompletedRunsScreen(onNavigateToPath: (String) -> Unit) {
    var runSummariesReply by remember {
        mutableStateOf<ServerReply<List<OcrolusRunSummary>>?>(null)
    }
    var reloadCount by remember {
        mutableIntStateOf(0)
    }
    var runSummaryPendingDeletion by remember {
        mutableStateOf<OcrolusRunSummary?>(null)
    }
    var deletionConfirmationMessage by remember {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(reloadCount) {
        runSummariesReply = OcrolusServerApi.fetchRunSummariesFromServer()
    }
    LaunchedEffect(deletionConfirmationMessage) {
        if (deletionConfirmationMessage != null) {
            delay(DELETION_CONFIRMATION_VISIBLE_MILLISECONDS)
            deletionConfirmationMessage = null
        }
    }
    val openRun = { runSummary: OcrolusRunSummary ->
        // A run that is still going has no result page yet; the assignment page shows it live.
        val destinationPath =
            if (runSummary.status.isStillInProgress()) AppRoutePaths.OCROLUS_ASSIGNMENT else AppRoutePaths.ocrolusRunResult(
                runSummary.runId
            )
        onNavigateToPath(destinationPath)
    }
    val askToDeleteRun = { runSummary: OcrolusRunSummary ->
        runSummaryPendingDeletion = runSummary
    }
    val currentRunSummariesReply = runSummariesReply

    Column(modifier = Modifier.fillMaxWidth()) {
        HeroBand(
            eyebrow = "Ocrolus take-home",
            title = "Completed runs",
            gradientWordCount = 1,
            lead = "Every run is saved with its full result and the exact prompts and replies, so you can compare models side by side. Times are in your time zone.",
            topContent = {
                HeroBackLink(
                    label = "Assignment",
                    onClick = {
                        onNavigateToPath(AppRoutePaths.OCROLUS_ASSIGNMENT)
                    },
                )
            },
        )
        PageBody {
            val currentDeletionConfirmationMessage = deletionConfirmationMessage
            if (currentDeletionConfirmationMessage != null) {
                NoticeBanner(currentDeletionConfirmationMessage, tone = NoticeTone.TIP)
                AddSpace(spacingDefault)
            }
            when (currentRunSummariesReply) {
                null -> LoadingIndicator("Loading the runs…")
                is ServerReply.Failure -> MessageWithAction(
                    title = "The runs could not be loaded",
                    body = currentRunSummariesReply.message,
                    actionLabel = "Try again",
                    onAction = {
                        runSummariesReply = null
                        reloadCount++
                    },
                )

                is ServerReply.Success -> when {
                    currentRunSummariesReply.value.isEmpty() -> MessageWithAction(
                        title = "No runs yet",
                        body = "Nobody has run the assignment yet. Start the first run and it will be saved here when it finishes.",
                        actionLabel = "Go to the assignment",
                        onAction = {
                            onNavigateToPath(AppRoutePaths.OCROLUS_ASSIGNMENT)
                        },
                    )

                    LocalWidthClass.current == WidthClass.EXPANDED -> RunSummariesTable(
                        currentRunSummariesReply.value,
                        openRun,
                        askToDeleteRun
                    )

                    else -> RunSummaryCards(currentRunSummariesReply.value, openRun, askToDeleteRun)
                }
            }
        }
    }

    val currentRunSummaryPendingDeletion = runSummaryPendingDeletion
    if (currentRunSummaryPendingDeletion != null) {
        OcrolusRunDeletionSheet(
            runId = currentRunSummaryPendingDeletion.runId,
            runDescription = "${currentRunSummaryPendingDeletion.modelDisplayName}, ${currentRunSummaryPendingDeletion.completionOrStartDateAndTime()}",
            adminPasscode = null,
            onRunDeleted = {
                val deletedRunId = currentRunSummaryPendingDeletion.runId
                val remainingRunSummaries = (runSummariesReply as? ServerReply.Success)?.value?.filterNot {
                    it.runId == deletedRunId
                }
                if (remainingRunSummaries != null) {
                    runSummariesReply = ServerReply.Success(remainingRunSummaries)
                }
                runSummaryPendingDeletion = null
                deletionConfirmationMessage = "Run deleted."
            },
            onDismiss = {
                runSummaryPendingDeletion = null
            },
        )
    }
}

/** The moment to show for a run: when it completed, or when it started if it has not finished. */
private fun OcrolusRunSummary.completionOrStartDateAndTime(): String {
    val completedAt = completedAtEpochMilliseconds
    return if (completedAt != null) {
        formatEpochMillisecondsAsViewerLocalDateAndTime(completedAt)
    } else {
        "Started ${formatEpochMillisecondsAsViewerLocalDateAndTime(startedAtEpochMilliseconds)}"
    }
}

private fun OcrolusRunSummary.timeTakenText(): String = durationMilliseconds?.let {
    formatDurationAsMinutesAndSeconds(it)
} ?: "–"

/** Running runs and the featured reference run cannot be deleted from the list. */
private fun OcrolusRunSummary.canBeDeletedFromList(): Boolean = !status.isStillInProgress() && !isFeatured

/** The run's name: the assignment name, the presenter's label when set, and a "Featured" badge. */
@Composable
private fun RunNameWithBadges(runSummary: OcrolusRunSummary) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubheadingText(runSummary.assignmentName, modifier = Modifier.weight(1f, fill = false))
            if (runSummary.isFeatured) {
                AddSpace(spacingSmall)
                TagChip("Featured", tone = TagTone.ACCENT)
            }
        }
        if (runSummary.displayLabel.isNotBlank()) {
            AddSpace(spacingXSmall)
            SecondaryText(runSummary.displayLabel)
        }
    }
}

/** The delete button for a run, or a short note saying why it cannot be deleted here. */
@Composable
private fun RunDeleteControl(runSummary: OcrolusRunSummary, onDeleteClick: (OcrolusRunSummary) -> Unit) {
    when {
        runSummary.canBeDeletedFromList() -> DeleteIconButton(
            accessibleLabel = "Delete this run",
            onClick = {
                onDeleteClick(runSummary)
            },
        )

        runSummary.isFeatured -> SecondaryText("Reference run")
        else -> Unit
    }
}

@Composable
private fun RunSummariesTable(
    runSummaries: List<OcrolusRunSummary>,
    onRunClick: (OcrolusRunSummary) -> Unit,
    onDeleteClick: (OcrolusRunSummary) -> Unit,
) {
    val runSummaryColumns = listOf<DataTableColumn<OcrolusRunSummary>>(
        DataTableColumn("Run", tableColumnWidthLarge) { runSummary ->
            RunNameWithBadges(runSummary)
        },
        DataTableColumn("Completed", tableColumnWidthLarge) { runSummary ->
            BodyText(runSummary.completionOrStartDateAndTime())
        },
        DataTableColumn("Model", tableColumnWidthMedium) { runSummary ->
            BodyText(runSummary.modelDisplayName)
        },
        DataTableColumn("Time taken", tableColumnWidthSmall) { runSummary ->
            MonospaceText(runSummary.timeTakenText())
        },
        DataTableColumn("Product chosen", tableColumnWidthMedium) { runSummary ->
            BodyText(runSummary.chosenProductDisplayName ?: "–")
        },
        DataTableColumn("Status", tableColumnWidthMedium) { runSummary ->
            StatusChip(runSummary.status.toProgressStatus(), runSummary.status.toSentenceCaseLabel())
        },
        DataTableColumn("", tableColumnWidthSmall) { runSummary ->
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                RunDeleteControl(runSummary, onDeleteClick)
            }
        },
    )
    DataTable(columns = runSummaryColumns, rows = runSummaries, onRowClick = onRunClick)
}

@Composable
private fun RunSummaryCards(
    runSummaries: List<OcrolusRunSummary>,
    onRunClick: (OcrolusRunSummary) -> Unit,
    onDeleteClick: (OcrolusRunSummary) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingDefault)) {
        runSummaries.forEach { runSummary ->
            ContentCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onRunClick(runSummary)
                },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(runSummary.status.toProgressStatus(), runSummary.status.toSentenceCaseLabel())
                    Box(modifier = Modifier.weight(1f))
                    RunDeleteControl(runSummary, onDeleteClick)
                }
                AddSpace(spacingMedium)
                RunNameWithBadges(runSummary)
                AddSpace(spacingXSmall)
                SecondaryText(
                    runSummary.completionOrStartDateAndTime(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AddSpace(spacingDefault)
                WrappingRow {
                    LabelledValue("Model", runSummary.modelDisplayName)
                    LabelledValue("Time taken", runSummary.timeTakenText())
                    LabelledValue("Product chosen", runSummary.chosenProductDisplayName ?: "–")
                }
            }
        }
    }
}
