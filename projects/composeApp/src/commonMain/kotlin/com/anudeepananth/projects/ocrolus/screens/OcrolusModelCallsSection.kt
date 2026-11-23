package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anudeepananth.projects.design.AddSpace
import com.anudeepananth.projects.design.BodyText
import com.anudeepananth.projects.design.CopyToClipboardButton
import com.anudeepananth.projects.design.ErrorText
import com.anudeepananth.projects.design.ExpandableSection
import com.anudeepananth.projects.design.MonospaceBlock
import com.anudeepananth.projects.design.NoticeBanner
import com.anudeepananth.projects.design.NoticeTone
import com.anudeepananth.projects.design.ProgressStatus
import com.anudeepananth.projects.design.SecondaryButton
import com.anudeepananth.projects.design.SectionHeader
import com.anudeepananth.projects.design.StatusChip
import com.anudeepananth.projects.design.SubheadingText
import com.anudeepananth.projects.design.spacingDefault
import com.anudeepananth.projects.design.spacingMedium
import com.anudeepananth.projects.design.spacingSmall
import com.anudeepananth.projects.design.spacingXLarge
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusModelCallLog
import com.anudeepananth.projects.ocrolus.api.OcrolusServerApi
import com.anudeepananth.projects.ocrolus.api.ServerReply
import com.anudeepananth.projects.ocrolus.formatting.formatDurationAsMinutesAndSeconds
import com.anudeepananth.projects.ocrolus.formatting.formatWholeNumberWithThousandsSeparators
import kotlinx.coroutines.launch

/**
 * "Show the prompts": fetches the run's saved model calls only when the visitor asks, then lists each
 * call with its real prompt and reply. They are fetched on demand because they are large.
 */
@Composable
fun OcrolusModelCallsSection(run: OcrolusAssignmentRun) {
    val coroutineScope = rememberCoroutineScope()
    var modelCallLogsReply by remember(run.runId) {
        mutableStateOf<ServerReply<List<OcrolusModelCallLog>>?>(null)
    }
    var isFetchInFlight by remember(run.runId) {
        mutableStateOf(false)
    }
    val stepTitlesByStepKey = run.steps.associate {
        it.stepKey to it.title
    }
    val currentModelCallLogsReply = modelCallLogsReply

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            eyebrow = "Under the hood",
            title = "Show the prompts",
            description = "Every prompt this run sent and every reply it got back, exactly as they were, including failed calls and repair attempts. Nothing is tidied up.",
        )
        AddSpace(spacingXLarge)
        if (currentModelCallLogsReply is ServerReply.Failure) {
            NoticeBanner(currentModelCallLogsReply.message, tone = NoticeTone.PROBLEM)
            AddSpace(spacingDefault)
        }
        if (currentModelCallLogsReply is ServerReply.Success) {
            if (currentModelCallLogsReply.value.isEmpty()) {
                BodyText("No model calls were saved for this run.")
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
                currentModelCallLogsReply.value.forEachIndexed { callIndex, modelCallLog ->
                    ModelCallExpandableCard(
                        callNumber = callIndex + 1,
                        stepTitle = stepTitlesByStepKey[modelCallLog.stepKey] ?: modelCallLog.stepKey,
                        modelCallLog = modelCallLog,
                    )
                }
            }
        } else {
            SecondaryButton(
                text = if (isFetchInFlight) "Loading the prompts…" else "Load ${run.usage.modelCallCount} prompts and replies",
                onClick = {
                    isFetchInFlight = true
                    coroutineScope.launch {
                        modelCallLogsReply = OcrolusServerApi.fetchModelCallLogsFromServer(run.runId)
                        isFetchInFlight = false
                    }
                },
                isEnabled = !isFetchInFlight,
            )
        }
    }
}

/** One model call: a summary line that opens to show the full prompt and reply, each with a copy button. */
@Composable
private fun ModelCallExpandableCard(callNumber: Int, stepTitle: String, modelCallLog: OcrolusModelCallLog) {
    val tokenSummary = "${formatWholeNumberWithThousandsSeparators(modelCallLog.promptTokens)} tokens in, ${
        formatWholeNumberWithThousandsSeparators(modelCallLog.completionTokens)
    } out"
    val outcomeSummary = if (modelCallLog.wasSuccessful) "succeeded" else "failed"
    ExpandableSection(
        title = "$callNumber. $stepTitle",
        supportingText = "${modelCallLog.modelId} · ${formatDurationAsMinutesAndSeconds(modelCallLog.durationMilliseconds)} · $tokenSummary · $outcomeSummary",
    ) {
        StatusChip(
            status = if (modelCallLog.wasSuccessful) ProgressStatus.DONE else ProgressStatus.FAILED,
            label = if (modelCallLog.wasSuccessful) "Succeeded" else "Failed",
        )
        val callErrorMessage = modelCallLog.errorMessage
        if (!callErrorMessage.isNullOrBlank()) {
            AddSpace(spacingSmall)
            ErrorText(callErrorMessage)
        }
        AddSpace(spacingDefault)
        PromptOrReplyBlock("Prompt", modelCallLog.promptText)
        AddSpace(spacingDefault)
        PromptOrReplyBlock("Reply", modelCallLog.responseText)
    }
}

@Composable
private fun PromptOrReplyBlock(blockTitle: String, blockText: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SubheadingText(blockTitle, modifier = Modifier.weight(1f))
            CopyToClipboardButton(textToCopy = blockText, label = "Copy ${blockTitle.lowercase()}")
        }
        AddSpace(spacingSmall)
        MonospaceBlock(blockText.ifBlank { "(empty)" })
    }
}
