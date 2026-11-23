package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anudeepananth.projects.design.AddSpace
import com.anudeepananth.projects.design.BodyText
import com.anudeepananth.projects.design.ContentCard
import com.anudeepananth.projects.design.ExpandableSection
import com.anudeepananth.projects.design.HeadingText
import com.anudeepananth.projects.design.HeroBand
import com.anudeepananth.projects.design.LabelledTextField
import com.anudeepananth.projects.design.LoadingIndicator
import com.anudeepananth.projects.design.LocalWidthClass
import com.anudeepananth.projects.design.MessageWithAction
import com.anudeepananth.projects.design.NoticeBanner
import com.anudeepananth.projects.design.NoticeTone
import com.anudeepananth.projects.design.PageBody
import com.anudeepananth.projects.design.PrimaryButton
import com.anudeepananth.projects.design.SecondaryButton
import com.anudeepananth.projects.design.SecondaryText
import com.anudeepananth.projects.design.SectionHeader
import com.anudeepananth.projects.design.SelectableOptionRow
import com.anudeepananth.projects.design.StatNumberText
import com.anudeepananth.projects.design.StatPillChip
import com.anudeepananth.projects.design.StatPillTone
import com.anudeepananth.projects.design.SubheadingText
import com.anudeepananth.projects.design.TagChip
import com.anudeepananth.projects.design.TagTone
import com.anudeepananth.projects.design.ThinDivider
import com.anudeepananth.projects.design.WidthClass
import com.anudeepananth.projects.design.WrappingRow
import com.anudeepananth.projects.design.spacingDefault
import com.anudeepananth.projects.design.spacingMedium
import com.anudeepananth.projects.design.spacingSection
import com.anudeepananth.projects.design.spacingSmall
import com.anudeepananth.projects.design.spacingXLarge
import com.anudeepananth.projects.design.spacingXSmall
import com.anudeepananth.projects.design.spacingXXLarge
import com.anudeepananth.projects.design.tableColumnWidthTiny
import com.anudeepananth.projects.design.textFieldMaximumWidth
import com.anudeepananth.projects.navigation.AppRoutePaths
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentOverview
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.ocrolus.OcrolusStepStatus
import com.anudeepananth.projects.ocrolus.formatting.formatDurationAsMinutesAndSeconds
import com.anudeepananth.projects.ocrolus.OcrolusModelOption
import com.anudeepananth.projects.ocrolus.OcrolusPipelineStepDescription
import com.anudeepananth.projects.ocrolus.api.OcrolusServerApi
import com.anudeepananth.projects.ocrolus.api.ServerReply
import kotlinx.coroutines.launch

private const val DEFAULT_ASSIGNMENT_NAME = "Ocrolus GTM Engineer take-home"
private const val START_RUN_BUTTON_LABEL = "Start analysis"

/** How many words of "Ocrolus GTM Engineer take-home" are drawn in the blue gradient: "Ocrolus GTM Engineer". */
private const val HERO_TITLE_GRADIENT_WORD_COUNT = 3

/**
 * One part of the take-home, summarised in Anudeep's words.
 *
 * @property partLabel short label such as "Part 1"
 */
private data class AssignmentPartSummary(val partLabel: String, val title: String, val description: String)

private val assignmentPartSummaries = listOf(
    AssignmentPartSummary(
        partLabel = "Part 1",
        title = "Pick a product and rank the accounts",
        description = "Pick one add-on (Fraud Shield, CashFlow Insights or Income Verify) as the best wallet-share play across the 50 accounts, show the scoring logic, and rank the top 15 for a cross-sell campaign.",
    ),
    AssignmentPartSummary(
        partLabel = "Part 2",
        title = "Build the outreach with AI",
        description = "Use an LLM to write personalised outreach (150 words max) for the top 3 accounts, share the real prompts, and call out one thing the AI got wrong.",
    ),
    AssignmentPartSummary(
        partLabel = "Part 3",
        title = "Measure it",
        description = "In three to five sentences: how you would know the campaign worked, what to measure, and over what time window.",
    ),
)

/**
 * The Ocrolus assignment page. Before a run it explains the take-home and offers a start button;
 * once a run is started (or one is already in progress) the same page turns into the live view.
 */
@Composable
fun OcrolusAssignmentScreen(onNavigateToPath: (String) -> Unit) {
    var overviewReply by remember {
        mutableStateOf<ServerReply<OcrolusAssignmentOverview>?>(null)
    }
    var overviewReloadCount by remember {
        mutableIntStateOf(0)
    }
    var watchedRunId by remember {
        mutableStateOf<String?>(null)
    }
    var wasWatchedRunAlreadyInProgress by remember {
        mutableStateOf(false)
    }
    var latestWatchedRun by remember {
        mutableStateOf<OcrolusAssignmentRun?>(null)
    }
    LaunchedEffect(overviewReloadCount) {
        val freshOverviewReply = OcrolusServerApi.fetchAssignmentOverviewFromServer()
        overviewReply = freshOverviewReply
        val activeRunId = (freshOverviewReply as? ServerReply.Success)?.value?.activeRunId
        if (activeRunId != null && watchedRunId == null) {
            wasWatchedRunAlreadyInProgress = true
            watchedRunId = activeRunId
        }
    }

    val currentOverviewReply = overviewReply
    val loadedOverview = (currentOverviewReply as? ServerReply.Success)?.value
    val currentlyWatchedRunId = watchedRunId

    Column(modifier = Modifier.fillMaxWidth()) {
        AssignmentHero(
            assignmentName = loadedOverview?.assignmentName ?: DEFAULT_ASSIGNMENT_NAME,
            overview = loadedOverview,
            isWatchingRun = currentlyWatchedRunId != null,
            watchedRun = latestWatchedRun?.takeIf {
                it.runId == currentlyWatchedRunId
            },
            onNavigateToPath = onNavigateToPath,
        )
        PageBody {
            when {
                currentlyWatchedRunId != null -> OcrolusLiveRunSection(
                    runId = currentlyWatchedRunId,
                    wasAlreadyInProgressWhenPageOpened = wasWatchedRunAlreadyInProgress,
                    onWatchedRunUpdated = { updatedRun ->
                        latestWatchedRun = updatedRun
                    },
                    onTryAgain = {
                        watchedRunId = null
                        wasWatchedRunAlreadyInProgress = false
                        overviewReply = null
                        overviewReloadCount++
                    },
                    onNavigateToPath = onNavigateToPath,
                )

                currentOverviewReply == null -> LoadingIndicator("Loading the assignment…")

                currentOverviewReply is ServerReply.Failure -> MessageWithAction(
                    title = "The assignment could not be loaded",
                    body = currentOverviewReply.message,
                    actionLabel = "Try again",
                    onAction = {
                        overviewReply = null
                        overviewReloadCount++
                    },
                )

                currentOverviewReply is ServerReply.Success -> AssignmentBeforeRunContent(
                    overview = currentOverviewReply.value,
                    onRunStarted = { startedRunId ->
                        wasWatchedRunAlreadyInProgress = false
                        watchedRunId = startedRunId
                    },
                    onWatchActiveRun = { activeRunId ->
                        wasWatchedRunAlreadyInProgress = true
                        watchedRunId = activeRunId
                    },
                )
            }
        }
    }
}

/** The dark band at the top: the assignment name, a short introduction, headline numbers and the reference-run shortcut. */
@Composable
private fun AssignmentHero(
    assignmentName: String,
    overview: OcrolusAssignmentOverview?,
    isWatchingRun: Boolean,
    watchedRun: OcrolusAssignmentRun?,
    onNavigateToPath: (String) -> Unit,
) {
    val pipelineStepCount = overview?.pipelineSteps?.size
    val modelStepCount = overview?.pipelineSteps?.count {
        it.isPerformedByModel
    }
    HeroBand(
        eyebrow = when {
            !isWatchingRun -> "For the Ocrolus hiring team"
            watchedRun?.status == OcrolusRunStatus.COMPLETED -> "Run complete"
            watchedRun?.status == OcrolusRunStatus.FAILED -> "Run stopped"
            else -> "Live run"
        },
        title = assignmentName,
        gradientWordCount = HERO_TITLE_GRADIENT_WORD_COUNT,
        lead = if (isWatchingRun) {
            watchedRunHeroLead(watchedRun)
        } else {
            "Thank you for shortlisting this take-home for the GTM Engineer, Growth Marketing role. Anudeep built his submission by hand with an AI assistant. This page goes one step further: the same method, with his own prompts and his own scoring formula, now runs end to end on its own. Press start and watch it work, or open a saved run. Every prompt and every model reply is saved, so you can check the work yourself."
        },
        bottomContent = if (isWatchingRun || pipelineStepCount == null || modelStepCount == null) {
            null
        } else {
            {
                WrappingRow(horizontalSpacing = spacingMedium) {
                    StatPillChip("$pipelineStepCount", "pipeline steps")
                    StatPillChip("$modelStepCount", "done by AI")
                    StatPillChip("${pipelineStepCount - modelStepCount}", "done by code", tone = StatPillTone.MINT)
                    StatPillChip("50", "accounts scored", tone = StatPillTone.MINT)
                }
                val featuredRunId = overview.featuredRunId
                if (featuredRunId != null) {
                    AddSpace(spacingXLarge)
                    SecondaryText(
                        text = "Short on time? Anudeep saved a reference run before the interview. It shows the full result on one page.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AddSpace(spacingMedium)
                    SecondaryButton(
                        text = "Open the reference run",
                        onClick = {
                            onNavigateToPath(AppRoutePaths.ocrolusRunResult(featuredRunId))
                        },
                    )
                }
            }
        },
    )
}

/** The hero paragraph while a run is on screen: live wording while it works, then a short summary once it has finished or stopped. */
private fun watchedRunHeroLead(watchedRun: OcrolusAssignmentRun?): String = when (watchedRun?.status) {
    OcrolusRunStatus.COMPLETED -> {
        val durationText = formatDurationAsMinutesAndSeconds(watchedRun.durationMilliseconds ?: 0)
        "This run finished in $durationText using ${watchedRun.usage.modelCallCount} model calls. The full result is saved."
    }

    OcrolusRunStatus.FAILED -> {
        val stoppedStepNumber = watchedRun.steps.indexOfFirst {
            it.status == OcrolusStepStatus.FAILED || it.status == OcrolusStepStatus.RUNNING
        }.takeIf {
            it >= 0
        }?.plus(1) ?: (watchedRun.steps.count {
            it.status == OcrolusStepStatus.COMPLETED
        } + 1).coerceAtMost(watchedRun.steps.size)
        "This run stopped at step $stoppedStepNumber. Everything it produced is saved."
    }

    else -> "You are watching the pipeline work through the take-home, step by step. Results appear below as soon as each step finishes."
}

/** Everything shown before a run starts: the brief, the pipeline, and the start controls. */
@Composable
private fun AssignmentBeforeRunContent(
    overview: OcrolusAssignmentOverview,
    onRunStarted: (String) -> Unit,
    onWatchActiveRun: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val widthClass = LocalWidthClass.current
        SectionHeader(
            eyebrow = "The brief",
            title = "The brief, in short",
            description = "Summarised in Anudeep's words. The 50 accounts are the synthetic dataset Ocrolus sent. Nothing on this site uses real customer data.",
        )
        AddSpace(spacingXLarge)
        if (widthClass == WidthClass.EXPANDED) {
            // Same height for all three cards: the row is as tall as the tallest card, and each card fills it.
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(spacingDefault),
            ) {
                assignmentPartSummaries.forEach { assignmentPartSummary ->
                    AssignmentPartCard(assignmentPartSummary, Modifier.weight(1f).fillMaxHeight())
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(spacingDefault)) {
                assignmentPartSummaries.forEach { assignmentPartSummary ->
                    AssignmentPartCard(assignmentPartSummary, Modifier.fillMaxWidth())
                }
            }
        }

        AddSpace(spacingSection)
        val modelStepCount = overview.pipelineSteps.count {
            it.isPerformedByModel
        }
        SectionHeader(
            eyebrow = "The pipeline",
            title = "${overview.pipelineSteps.size} steps, from raw accounts to finished emails",
            description = "$modelStepCount steps use an AI model and ${overview.pipelineSteps.size - modelStepCount} are plain code. The model does what needs judgement: reading CSM notes, spotting risk, writing and reviewing the emails. The maths stays in code, using the same formula as Anudeep's submitted spreadsheet, and an automated test checks the code against that spreadsheet for all 50 accounts.",
        )
        AddSpace(spacingXLarge)
        PipelineStepsOverview(overview.pipelineSteps)

        AddSpace(spacingSection)
        SectionHeader(
            eyebrow = "Try it",
            title = "Run it yourself",
            description = "Pick a model and press start. A run makes about 20 model calls and takes roughly 5 to 15 minutes on free models. Each step shows up here as it happens, and the finished run is saved for you to come back to.",
        )
        AddSpace(spacingXLarge)
        StartRunControls(overview, onRunStarted, onWatchActiveRun)
    }
}

@Composable
private fun AssignmentPartCard(assignmentPartSummary: AssignmentPartSummary, modifier: Modifier) {
    ContentCard(modifier = modifier) {
        TagChip(assignmentPartSummary.partLabel, tone = TagTone.ACCENT)
        AddSpace(spacingDefault)
        HeadingText(assignmentPartSummary.title)
        AddSpace(spacingSmall)
        BodyText(assignmentPartSummary.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** The numbered list of pipeline steps, each tagged "AI" or "Code"; two columns on wide windows. */
@Composable
private fun PipelineStepsOverview(pipelineSteps: List<OcrolusPipelineStepDescription>) {
    val numberedPipelineSteps = pipelineSteps.mapIndexed { stepIndex, pipelineStep ->
        (stepIndex + 1) to pipelineStep
    }
    if (LocalWidthClass.current == WidthClass.EXPANDED) {
        val firstColumnStepCount = (numberedPipelineSteps.size + 1) / 2
        Row(horizontalArrangement = Arrangement.spacedBy(spacingDefault), verticalAlignment = Alignment.Top) {
            PipelineStepsCard(numberedPipelineSteps.take(firstColumnStepCount), Modifier.weight(1f))
            PipelineStepsCard(numberedPipelineSteps.drop(firstColumnStepCount), Modifier.weight(1f))
        }
    } else {
        PipelineStepsCard(numberedPipelineSteps, Modifier.fillMaxWidth())
    }
}

@Composable
private fun PipelineStepsCard(
    numberedPipelineSteps: List<Pair<Int, OcrolusPipelineStepDescription>>,
    modifier: Modifier
) {
    ContentCard(modifier = modifier) {
        numberedPipelineSteps.forEachIndexed { rowIndex, (stepNumber, pipelineStep) ->
            if (rowIndex > 0) {
                AddSpace(spacingDefault)
                ThinDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AddSpace(spacingDefault)
            }
            Row(verticalAlignment = Alignment.Top) {
                StatNumberText(
                    text = stepNumber.toString().padStart(2, '0'),
                    modifier = Modifier.widthIn(min = tableColumnWidthTiny),
                    color = if (pipelineStep.isPerformedByModel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SubheadingText(pipelineStep.title, modifier = Modifier.weight(1f, fill = false))
                        AddSpace(spacingSmall)
                        StepPerformerTag(pipelineStep.isPerformedByModel)
                    }
                    AddSpace(spacingXSmall)
                    SecondaryText(pipelineStep.description)
                }
            }
        }
    }
}

/** Tag saying whether a step is done by the AI model (violet) or by plain code (mint). */
@Composable
fun StepPerformerTag(isPerformedByModel: Boolean) {
    if (isPerformedByModel) {
        TagChip("AI", tone = TagTone.AI)
    } else {
        TagChip("Code", tone = TagTone.CODE)
    }
}

/** The model picker, the admin-only paid models, the runs-left note and the start button. */
@Composable
private fun StartRunControls(
    overview: OcrolusAssignmentOverview,
    onRunStarted: (String) -> Unit,
    onWatchActiveRun: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val freeModels = overview.availableModels.filter {
        it.isFree
    }
    val paidModels = overview.availableModels.filterNot {
        it.isFree
    }
    var selectedModelId by remember {
        mutableStateOf(freeModels.firstOrNull()?.modelId ?: paidModels.firstOrNull()?.modelId)
    }
    var adminPasscode by remember {
        mutableStateOf("")
    }
    var isStartRequestInFlight by remember {
        mutableStateOf(false)
    }
    var startFailure by remember {
        mutableStateOf<ServerReply.Failure?>(null)
    }
    val selectedModel = overview.availableModels.firstOrNull {
        it.modelId == selectedModelId
    }
    val hasFreeRunsLeft = overview.publicRunsRemainingToday > 0
    val isSelectedModelStartable = selectedModel != null && (!selectedModel.isFree || hasFreeRunsLeft)

    ContentCard(modifier = Modifier.fillMaxWidth()) {
        HeadingText("Choose a model")
        AddSpace(spacingXSmall)
        SecondaryText("Every model here costs you nothing. MiniMax M2.7 is the fastest and most reliable; it runs on Anudeep's MiniMax plan. The rest are free models on OpenRouter, which are slower and sometimes busy. When a model stalls or returns something unusable, the pipeline asks it to fix the reply or hands the step to a backup model, and logs that it did.")
        AddSpace(spacingDefault)
        ModelOptionList(freeModels, selectedModelId) { chosenModelId ->
            selectedModelId = chosenModelId
        }
        if (paidModels.isNotEmpty()) {
            AddSpace(spacingDefault)
            ExpandableSection(title = "Admin", supportingText = "Paid models; these need the admin passcode") {
                ModelOptionList(paidModels, selectedModelId) { chosenModelId ->
                    selectedModelId = chosenModelId
                }
                AddSpace(spacingDefault)
                LabelledTextField(
                    value = adminPasscode,
                    onValueChange = {
                        adminPasscode = it
                    },
                    label = "Admin passcode",
                    isSecret = true,
                )
            }
        }
        AddSpace(spacingXLarge)
        val currentStartFailure = startFailure
        if (currentStartFailure != null) {
            val conflictingActiveRunId = currentStartFailure.activeRunId
            NoticeBanner(
                text = currentStartFailure.message,
                tone = if (conflictingActiveRunId != null) NoticeTone.IN_PROGRESS else NoticeTone.PROBLEM,
                actionLabel = if (conflictingActiveRunId != null) "Watch it live" else null,
                onAction = if (conflictingActiveRunId != null) {
                    {
                        onWatchActiveRun(conflictingActiveRunId)
                    }
                } else {
                    null
                },
            )
            AddSpace(spacingDefault)
        }
        PrimaryButton(
            text = START_RUN_BUTTON_LABEL,
            onClick = {
                val modelToRun = selectedModel ?: return@PrimaryButton
                isStartRequestInFlight = true
                startFailure = null
                coroutineScope.launch {
                    val passcodeToSend = adminPasscode.takeIf {
                        !modelToRun.isFree && it.isNotBlank()
                    }
                    when (val startReply =
                        OcrolusServerApi.requestServerToStartRun(modelToRun.modelId, passcodeToSend)) {
                        is ServerReply.Success -> onRunStarted(startReply.value.runId)
                        is ServerReply.Failure -> startFailure = startReply
                    }
                    isStartRequestInFlight = false
                }
            },
            modifier = if (LocalWidthClass.current == WidthClass.COMPACT) Modifier.fillMaxWidth() else Modifier.widthIn(
                max = textFieldMaximumWidth
            ),
            isEnabled = isSelectedModelStartable,
            isLoading = isStartRequestInFlight,
        )
        AddSpace(spacingMedium)
        SecondaryText(
            text = when {
                !hasFreeRunsLeft -> "Today's free runs are used up. Please open a completed run instead. New free runs open up tomorrow."
                overview.publicRunsRemainingToday == 1 -> "1 free run left today. Only one run can be in progress at a time."
                else -> "${overview.publicRunsRemainingToday} free runs left today. Only one run can be in progress at a time."
            },
        )
    }
}

@Composable
private fun ModelOptionList(
    modelOptions: List<OcrolusModelOption>,
    selectedModelId: String?,
    onModelSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacingSmall)) {
        modelOptions.forEach { modelOption ->
            SelectableOptionRow(
                title = modelOption.displayName,
                isSelected = modelOption.modelId == selectedModelId,
                onSelect = {
                    onModelSelected(modelOption.modelId)
                },
                supportingText = modelOption.modelId,
            )
        }
    }
}
