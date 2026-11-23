package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import com.anudeepananth.projects.design.BulletedSentenceList
import com.anudeepananth.projects.ocrolus.OcrolusMeasurementPlan
import com.anudeepananth.projects.design.ContentCard
import com.anudeepananth.projects.design.CopyToClipboardButton
import com.anudeepananth.projects.design.DataTable
import com.anudeepananth.projects.design.DataTableColumn
import com.anudeepananth.projects.design.ExpandableSection
import com.anudeepananth.projects.design.EyebrowText
import com.anudeepananth.projects.design.HeadingText
import com.anudeepananth.projects.design.HeroBackLink
import com.anudeepananth.projects.design.HeroBand
import com.anudeepananth.projects.design.LabelText
import com.anudeepananth.projects.design.LabelledValue
import com.anudeepananth.projects.design.LeadBodyText
import com.anudeepananth.projects.design.LoadingIndicator
import com.anudeepananth.projects.design.LocalSiteColours
import com.anudeepananth.projects.design.LocalWidthClass
import com.anudeepananth.projects.design.MessageWithAction
import com.anudeepananth.projects.design.MonospaceBlock
import com.anudeepananth.projects.design.MonospaceText
import com.anudeepananth.projects.design.EmphasisCard
import com.anudeepananth.projects.design.NoticeBanner
import com.anudeepananth.projects.design.NoticeTone
import com.anudeepananth.projects.design.PageBody
import com.anudeepananth.projects.design.PillTabStrip
import com.anudeepananth.projects.design.PrimaryButton
import com.anudeepananth.projects.design.SecondaryButton
import com.anudeepananth.projects.design.SecondaryText
import com.anudeepananth.projects.design.SectionHeader
import com.anudeepananth.projects.design.StatPillChip
import com.anudeepananth.projects.design.StatPillTone
import com.anudeepananth.projects.design.StatusChip
import com.anudeepananth.projects.design.SubheadingText
import com.anudeepananth.projects.design.TagChip
import com.anudeepananth.projects.design.TagTone
import com.anudeepananth.projects.design.ThinDivider
import com.anudeepananth.projects.design.TintedBlock
import com.anudeepananth.projects.design.WidthClass
import com.anudeepananth.projects.design.WrappingRow
import com.anudeepananth.projects.design.readableTextMaximumWidth
import com.anudeepananth.projects.design.spacingDefault
import com.anudeepananth.projects.design.spacingMedium
import com.anudeepananth.projects.design.spacingSection
import com.anudeepananth.projects.design.spacingSmall
import com.anudeepananth.projects.design.spacingXLarge
import com.anudeepananth.projects.design.spacingXSmall
import com.anudeepananth.projects.design.spacingXXLarge
import com.anudeepananth.projects.design.tableColumnWidthLarge
import com.anudeepananth.projects.design.tableColumnWidthMedium
import com.anudeepananth.projects.design.tableColumnWidthSmall
import com.anudeepananth.projects.design.tableColumnWidthTiny
import com.anudeepananth.projects.design.tableColumnWidthXLarge
import com.anudeepananth.projects.navigation.AppRoutePaths
import com.anudeepananth.projects.ocrolus.OcrolusAddOnProduct
import com.anudeepananth.projects.ocrolus.OcrolusAiCorrectionSummary
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusOutreachMessage
import com.anudeepananth.projects.ocrolus.OcrolusRunResult
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.ocrolus.OcrolusScoredAccount
import com.anudeepananth.projects.ocrolus.api.HTTP_STATUS_NOT_FOUND
import com.anudeepananth.projects.ocrolus.api.OcrolusServerApi
import com.anudeepananth.projects.ocrolus.api.ServerReply
import com.anudeepananth.projects.ocrolus.formatting.formatDurationAsMinutesAndSeconds
import com.anudeepananth.projects.ocrolus.formatting.formatNumberWithOneDecimal
import com.anudeepananth.projects.ocrolus.formatting.formatOptionalScore
import com.anudeepananth.projects.ocrolus.formatting.formatWholeNumberWithThousandsSeparators
import com.anudeepananth.projects.ocrolus.formatting.isStillInProgress
import com.anudeepananth.projects.ocrolus.formatting.toProgressStatus
import com.anudeepananth.projects.ocrolus.formatting.toSentenceCaseLabel
import com.anudeepananth.projects.platform.formatEpochMillisecondsAsViewerLocalDateAndTime
import com.anudeepananth.projects.platform.readBrowserPageAddress

/** How many words of "Ocrolus GTM Engineer take-home" are drawn in the blue gradient: "Ocrolus GTM Engineer". */
private const val RUN_TITLE_GRADIENT_WORD_COUNT = 3

private const val PRIORITY_FORMULA_TEXT = "Priority = 100 × intent × health × size"

private val priorityFormulaExplanations = listOf(
    "Intent" to "the model's 0–10 score of the buying signal in the account's note, divided by 10.",
    "Health" to "60% net promoter score (capped at 70) plus 40% timing: a recent business review and a renewal 90 to 270 days out.",
    "Size" to "the account's monthly processing volume divided by the largest volume in the dataset.",
    "Excluded (priority 0)" to "accounts that already adopted the product, are flagged at risk, have a net promoter score below 35, renew within 60 days, or declined.",
)

/**
 * One tab of the result page.
 *
 * @property partNumber the number used in the page address, such as `?part=2`
 */
private data class RunResultPart(val partNumber: Int, val tabLabel: String)

private val runResultParts = listOf(
    RunResultPart(1, "Part 1: Product and ranking"),
    RunResultPart(2, "Part 2: Outreach"),
    RunResultPart(3, "Part 3: Measurement"),
    RunResultPart(4, "Under the hood"),
)

/**
 * The permanent page for one saved run: a header with the run's facts, then one tab per part of the
 * assignment plus "Under the hood" for the real prompts, then admin tools. The open tab is
 * [selectedPartNumber], kept in the address so links and refreshes reopen it. A run that is still
 * in progress has no result yet, so the visitor is sent to the live view instead.
 */
@Composable
fun OcrolusRunResultScreen(
    runId: String,
    selectedPartNumber: Int,
    onNavigateToPath: (String) -> Unit,
    onReplaceCurrentPathWith: (String) -> Unit,
    onBackToAllRuns: () -> Unit,
) {
    var runReply by remember(runId) {
        mutableStateOf<ServerReply<OcrolusAssignmentRun>?>(null)
    }
    var reloadCount by remember(runId) {
        mutableIntStateOf(0)
    }
    LaunchedEffect(runId, reloadCount) {
        val freshRunReply = OcrolusServerApi.fetchRunFromServer(runId)
        if (freshRunReply is ServerReply.Success && freshRunReply.value.status.isStillInProgress()) {
            onReplaceCurrentPathWith(AppRoutePaths.OCROLUS_ASSIGNMENT)
        } else {
            runReply = freshRunReply
        }
    }
    val currentRunReply = runReply
    val loadedRun = (currentRunReply as? ServerReply.Success)?.value

    Column(modifier = Modifier.fillMaxWidth()) {
        RunResultHero(loadedRun, onBackToAllRuns)
        PageBody {
            when {
                currentRunReply == null -> LoadingIndicator("Loading the run…")

                currentRunReply is ServerReply.Failure && currentRunReply.httpStatusCode == HTTP_STATUS_NOT_FOUND -> MessageWithAction(
                    title = "This run does not exist",
                    body = "There is no saved run at this address. It may have been deleted, or the link may be incomplete.",
                    actionLabel = "See the completed runs",
                    onAction = {
                        onNavigateToPath(AppRoutePaths.OCROLUS_COMPLETED_RUNS)
                    },
                )

                currentRunReply is ServerReply.Failure -> MessageWithAction(
                    title = "The run could not be loaded",
                    body = currentRunReply.message,
                    actionLabel = "Try again",
                    onAction = {
                        runReply = null
                        reloadCount++
                    },
                )

                currentRunReply is ServerReply.Success -> RunResultContent(
                    run = currentRunReply.value,
                    selectedPartNumber = selectedPartNumber.coerceIn(1, runResultParts.size),
                    onSelectPart = { partNumber ->
                        onReplaceCurrentPathWith(AppRoutePaths.ocrolusRunResult(runId, partNumber))
                    },
                    onRunEdited = { editedRun ->
                        runReply = ServerReply.Success(editedRun)
                    },
                    onRunDeleted = {
                        onNavigateToPath(AppRoutePaths.OCROLUS_COMPLETED_RUNS)
                    },
                )
            }
        }
    }
}

/** The tab strip, the open part's content with previous and next buttons, and the admin tools below. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RunResultContent(
    run: OcrolusAssignmentRun,
    selectedPartNumber: Int,
    onSelectPart: (Int) -> Unit,
    onRunEdited: (OcrolusAssignmentRun) -> Unit,
    onRunDeleted: () -> Unit,
) {
    val tabStripBringIntoViewRequester = remember {
        BringIntoViewRequester()
    }
    var hasShownFirstPart by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(selectedPartNumber) {
        // On the first show the page is already at the top; after that, bring the tabs back into view.
        if (hasShownFirstPart) tabStripBringIntoViewRequester.bringIntoView() else hasShownFirstPart = true
    }
    val result = run.result
    Column(modifier = Modifier.fillMaxWidth()) {
        if (run.status == OcrolusRunStatus.FAILED) {
            NoticeBanner(
                text = "This run failed before finishing. ${run.errorMessage ?: "No reason was given."} Whatever it produced before failing is shown below.",
                tone = NoticeTone.PROBLEM,
            )
            AddSpace(spacingXLarge)
        }
        PillTabStrip(
            tabLabels = runResultParts.map {
                it.tabLabel
            },
            selectedTabIndex = selectedPartNumber - 1,
            onTabSelected = { tabIndex ->
                onSelectPart(tabIndex + 1)
            },
            modifier = Modifier.bringIntoViewRequester(tabStripBringIntoViewRequester),
        )
        AddSpace(spacingXXLarge)
        when (selectedPartNumber) {
            1 -> {
                val chosenProduct = result.chosenProduct
                if (chosenProduct != null) PartOneSection(result, chosenProduct) else PartNotReachedMessage("Part 1")
            }

            2 -> if (result.outreachMessages.isNotEmpty() || result.outreachStrategyText.isNotBlank()) PartTwoSection(
                result
            ) else PartNotReachedMessage("Part 2")

            3 -> result.measurementPlan?.let { PartThreeSection(it) } ?: PartNotReachedMessage("Part 3")
            else -> OcrolusModelCallsSection(run)
        }
        AddSpace(spacingXXLarge)
        PreviousAndNextPartButtons(selectedPartNumber, onSelectPart)

        AddSpace(spacingSection)
        OcrolusRunAdminSection(run, onRunEdited, onRunDeleted)
    }
}

/** Shown in a tab when the run stopped before producing that part. */
@Composable
private fun PartNotReachedMessage(partLabel: String) {
    MessageWithAction(
        title = "$partLabel was not produced",
        body = "This run stopped before it reached $partLabel. The other tabs show what it did produce.",
    )
}

@Composable
private fun PartThreeSection(measurementPlan: OcrolusMeasurementPlan) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(eyebrow = "Part 3", title = "How success is measured")
        AddSpace(spacingXLarge)
        ContentCard(modifier = Modifier.fillMaxWidth()) {
            LeadBodyText(
                measurementPlan.overview,
                modifier = Modifier.widthIn(max = readableTextMaximumWidth),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (measurementPlan.measuredItems.isNotEmpty()) {
                AddSpace(spacingXLarge)
                EyebrowText("What gets measured")
                AddSpace(spacingMedium)
                BulletedSentenceList(
                    sentences = measurementPlan.measuredItems,
                    modifier = Modifier.widthIn(max = readableTextMaximumWidth),
                )
            }
        }
    }
}

/** "Previous part" and "Next part" buttons under each tab's content; the first tab has no previous, the last no next. */
@Composable
private fun PreviousAndNextPartButtons(selectedPartNumber: Int, onSelectPart: (Int) -> Unit) {
    val previousPart = runResultParts.firstOrNull {
        it.partNumber == selectedPartNumber - 1
    }
    val nextPart = runResultParts.firstOrNull {
        it.partNumber == selectedPartNumber + 1
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        ThinDivider()
        AddSpace(spacingXLarge)
        WrappingRow(horizontalSpacing = spacingMedium) {
            if (previousPart != null) {
                SecondaryButton(
                    text = "Previous part",
                    onClick = {
                        onSelectPart(previousPart.partNumber)
                    },
                )
            }
            if (nextPart != null) {
                PrimaryButton(
                    text = "Next part: ${nextPart.tabLabel.substringAfter(": ")}",
                    onClick = {
                        onSelectPart(nextPart.partNumber)
                    },
                )
            }
        }
    }
}

/** The dark band at the top of the result page: the back link, the run's name and its facts, on every tab. */
@Composable
private fun RunResultHero(run: OcrolusAssignmentRun?, onBackToAllRuns: () -> Unit) {
    val runTitle = run?.displayLabel?.ifBlank { null } ?: run?.assignmentName ?: "Run result"
    HeroBand(
        eyebrow = if (run?.isFeatured == true) "Reference run" else "Run result",
        title = runTitle,
        gradientWordCount = if (runTitle == run?.assignmentName) RUN_TITLE_GRADIENT_WORD_COUNT else 0,
        topContent = {
            HeroBackLink(label = "All runs", onClick = onBackToAllRuns)
        },
        bottomContent = if (run == null) {
            null
        } else {
            {
                WrappingRow(horizontalSpacing = spacingMedium) {
                    StatusChip(run.status.toProgressStatus(), run.status.toSentenceCaseLabel())
                    if (run.isFeatured) {
                        TagChip("Featured", tone = TagTone.ACCENT)
                    }
                    LabelText(run.modelDisplayName, color = MaterialTheme.colorScheme.onSurface)
                    LabelText(
                        (if (run.completedAtEpochMilliseconds != null) "Completed " else "Started ") +
                                formatEpochMillisecondsAsViewerLocalDateAndTime(
                                    run.completedAtEpochMilliseconds ?: run.startedAtEpochMilliseconds
                                ),
                    )
                }
                AddSpace(spacingXLarge)
                WrappingRow(horizontalSpacing = spacingMedium) {
                    StatPillChip(
                        value = run.durationMilliseconds?.let {
                            formatDurationAsMinutesAndSeconds(it)
                        } ?: "–",
                        label = "time taken",
                    )
                    StatPillChip(run.usage.modelCallCount.toString(), "model calls")
                    StatPillChip(
                        formatWholeNumberWithThousandsSeparators(run.usage.promptTokens),
                        "tokens in",
                        tone = StatPillTone.MINT
                    )
                    StatPillChip(
                        formatWholeNumberWithThousandsSeparators(run.usage.completionTokens),
                        "tokens out",
                        tone = StatPillTone.MINT
                    )
                }
                AddSpace(spacingXLarge)
                CopyToClipboardButton(textToCopy = readBrowserPageAddress(), label = "Copy link")
                if (run.usage.fallbackModelIdsUsed.isNotEmpty()) {
                    AddSpace(spacingDefault)
                    SecondaryText(
                        "Some calls fell back to other models when the chosen one failed: ${
                            run.usage.fallbackModelIdsUsed.joinToString(
                                ", "
                            )
                        }."
                    )
                }
                if (run.presenterNotes.isNotBlank()) {
                    AddSpace(spacingXLarge)
                    ContentCard(modifier = Modifier.fillMaxWidth()) {
                        EyebrowText("Presenter notes")
                        AddSpace(spacingSmall)
                        BodyText(run.presenterNotes)
                    }
                }
            }
        },
    )
}

/** Part 1: the product pick, why, the formula, the top accounts and the full scored list. */
@Composable
private fun PartOneSection(result: OcrolusRunResult, chosenProduct: OcrolusAddOnProduct) {
    val isExpandedWidth = LocalWidthClass.current == WidthClass.EXPANDED
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            eyebrow = "Part 1",
            title = "Cross-sell ${chosenProduct.displayName}",
            description = "The model read every CSM note, scored the buying signal per add-on and flagged at-risk accounts, quoting the note each time. Code then turned those scores into priorities, totalled them per product and ranked the accounts.",
        )
        AddSpace(spacingXLarge)
        if (isExpandedWidth) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacingDefault), verticalAlignment = Alignment.Top) {
                ChosenProductCard(result, chosenProduct, Modifier.weight(1f))
                ProductRationaleCard(result.productRationale, Modifier.weight(1f))
            }
        } else {
            ChosenProductCard(result, chosenProduct)
            AddSpace(spacingDefault)
            ProductRationaleCard(result.productRationale, Modifier.fillMaxWidth())
        }
        AddSpace(spacingDefault)
        PriorityFormulaCard()

        if (result.topAccountIds.isNotEmpty()) {
            AddSpace(spacingXLarge)
            HeadingText("Top ${result.topAccountIds.size} accounts to approach")
            AddSpace(spacingDefault)
            TopAccountsTable(result, chosenProduct)
        }
        if (result.scoredAccounts.isNotEmpty()) {
            AddSpace(spacingDefault)
            ExpandableSection(
                title = "All ${result.scoredAccounts.size} accounts, with the evidence",
                supportingText = "Note scores, the quoted evidence, risk flags, all three priorities and anomaly checks",
            ) {
                SecondaryText("This table is wide: scroll it sideways to see every column.")
                AddSpace(spacingMedium)
                AllScoredAccountsTable(result.scoredAccounts)
            }
        }
    }
}

@Composable
private fun ProductRationaleCard(productRationale: String, modifier: Modifier) {
    ContentCard(modifier = modifier) {
        EyebrowText("Why this product")
        AddSpace(spacingSmall)
        BodyText(productRationale.ifBlank { "No rationale was written for this run." })
    }
}

@Composable
private fun PriorityFormulaCard() {
    ContentCard(modifier = Modifier.fillMaxWidth()) {
        EyebrowText("The scoring logic")
        AddSpace(spacingMedium)
        MonospaceBlock(PRIORITY_FORMULA_TEXT)
        AddSpace(spacingDefault)
        Column(verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
            priorityFormulaExplanations.forEach { (termName, termExplanation) ->
                Column {
                    SubheadingText(termName)
                    BodyText(termExplanation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** The wide table of every account; it scrolls sideways on narrow windows rather than squashing. */
@Composable
private fun AllScoredAccountsTable(scoredAccounts: List<OcrolusScoredAccount>) {
    val accountsInRankOrder = scoredAccounts.sortedWith(
        compareBy<OcrolusScoredAccount> {
            it.overallRank ?: Int.MAX_VALUE
        }.thenBy {
            it.account.accountId
        },
    )
    val scoredAccountColumns = listOf<DataTableColumn<OcrolusScoredAccount>>(
        DataTableColumn("Rank", tableColumnWidthTiny) { scoredAccount ->
            MonospaceText(scoredAccount.overallRank?.toString() ?: "–")
        },
        DataTableColumn("Account", tableColumnWidthLarge) { scoredAccount ->
            Column {
                SubheadingText(scoredAccount.account.accountName)
                SecondaryText("${scoredAccount.account.accountId}, ${scoredAccount.account.vertical}")
            }
        },
        DataTableColumn("Fraud Shield note score", tableColumnWidthSmall) { scoredAccount ->
            MonospaceText(formatOptionalScore(scoredAccount.fraudShieldNoteScore))
        },
        DataTableColumn("CashFlow Insights note score", tableColumnWidthSmall) { scoredAccount ->
            MonospaceText(formatOptionalScore(scoredAccount.cashFlowInsightsNoteScore))
        },
        DataTableColumn("Income Verify note score", tableColumnWidthSmall) { scoredAccount ->
            MonospaceText(formatOptionalScore(scoredAccount.incomeVerifyNoteScore))
        },
        DataTableColumn("Evidence quote", tableColumnWidthXLarge) { scoredAccount ->
            SecondaryText(scoredAccount.noteScoreEvidenceQuote.ifBlank { "–" })
        },
        DataTableColumn("Risk flag", tableColumnWidthXLarge) { scoredAccount ->
            if (scoredAccount.isFlaggedAsRisk) {
                Column {
                    TagChip("At risk", tone = TagTone.WARNING)
                    if (scoredAccount.riskEvidenceQuote.isNotBlank()) {
                        AddSpace(spacingXSmall)
                        SecondaryText(scoredAccount.riskEvidenceQuote)
                    }
                }
            } else {
                SecondaryText("–")
            }
        },
        DataTableColumn("Fraud Shield priority", tableColumnWidthSmall) { scoredAccount ->
            MonospaceText(formatNumberWithOneDecimal(scoredAccount.fraudShieldPriority))
        },
        DataTableColumn("CashFlow Insights priority", tableColumnWidthSmall) { scoredAccount ->
            MonospaceText(formatNumberWithOneDecimal(scoredAccount.cashFlowInsightsPriority))
        },
        DataTableColumn("Income Verify priority", tableColumnWidthSmall) { scoredAccount ->
            MonospaceText(formatNumberWithOneDecimal(scoredAccount.incomeVerifyPriority))
        },
        DataTableColumn("Best product", tableColumnWidthMedium) { scoredAccount ->
            BodyText(scoredAccount.bestProductLabel)
        },
        DataTableColumn("Anomaly check", tableColumnWidthXLarge) { scoredAccount ->
            Column {
                BodyText(scoredAccount.anomalyScore?.let { "Score $it" } ?: "–")
                val anomalyReason = scoredAccount.anomalyReason
                if (!anomalyReason.isNullOrBlank()) {
                    SecondaryText(anomalyReason)
                }
                if (scoredAccount.isFlaggedForManualReview) {
                    AddSpace(spacingXSmall)
                    TagChip("Manual review", tone = TagTone.WARNING)
                }
            }
        },
    )
    DataTable(columns = scoredAccountColumns, rows = accountsInRankOrder)
}

/** Part 2: the three emails with their review scorecards, the strategy, the first drafts and the AI's mistake. */
@Composable
private fun PartTwoSection(result: OcrolusRunResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            eyebrow = "Part 2",
            title = "Outreach for the top ${result.outreachMessages.size.coerceAtLeast(1)} accounts",
            description = "Each email went through three separate prompts: a strategy written before any copy, a draft, and a five-pass review (fact check, style, marketing, spam, style again). The review fixes what it finds and logs every change, shown below.",
        )
        AddSpace(spacingXLarge)
        Column(verticalArrangement = Arrangement.spacedBy(spacingDefault)) {
            result.outreachMessages.forEach { outreachMessage ->
                OutreachEmailCard(outreachMessage) {
                    OutreachReviewScorecard(outreachMessage)
                }
            }
            if (result.outreachStrategyText.isNotBlank()) {
                ExpandableSection(
                    title = "Strategy",
                    supportingText = "What the model planned for each account before writing a word"
                ) {
                    BodyText(result.outreachStrategyText)
                    if (result.outreachStrategyUpdates.isNotBlank()) {
                        AddSpace(spacingDefault)
                        SubheadingText("Updates after the review")
                        AddSpace(spacingXSmall)
                        BodyText(result.outreachStrategyUpdates)
                    }
                }
            }
            if (result.outreachFirstDraftsText.isNotBlank()) {
                ExpandableSection(
                    title = "First drafts",
                    supportingText = "The emails as first written, before the review edited them"
                ) {
                    BodyText(result.outreachFirstDraftsText)
                }
            }
            val aiCorrectionSummary = result.aiCorrectionSummary
            if (aiCorrectionSummary != null) {
                AiCorrectionCard(aiCorrectionSummary)
            }
        }
    }
}

/** The review results under an email: scores before and after, the verdict, flagged items and the change log. */
@Composable
private fun OutreachReviewScorecard(outreachMessage: OcrolusOutreachMessage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AddSpace(spacingDefault)
        ThinDivider()
        AddSpace(spacingDefault)
        WrappingRow {
            LabelledValue(
                "Salesiness (1 helpful – 10 hard sell)",
                "From ${outreachMessage.salesinessScoreFirst} to ${outreachMessage.salesinessScoreFinal}"
            )
            LabelledValue(
                "Spam score (80+ passes)",
                "From ${outreachMessage.spamScoreFirst} to ${outreachMessage.spamScoreFinal}"
            )
            LabelledValue("Claims checked", "${outreachMessage.claimsChecked} (${outreachMessage.claimsFixed} fixed)")
        }
        if (outreachMessage.verdict.isNotBlank()) {
            AddSpace(spacingDefault)
            LabelText("Verdict")
            BodyText(outreachMessage.verdict)
        }
        if (outreachMessage.flaggedForManualReview.isNotEmpty()) {
            AddSpace(spacingDefault)
            TintedBlock {
                LabelText("A person should confirm before sending")
                outreachMessage.flaggedForManualReview.forEach { flaggedItem ->
                    AddSpace(spacingXSmall)
                    BodyText("– $flaggedItem")
                }
            }
        }
        if (outreachMessage.changeLog.isNotEmpty()) {
            AddSpace(spacingDefault)
            ExpandableSection(
                title = "Change log",
                supportingText = "${outreachMessage.changeLog.size} edits made by the review"
            ) {
                outreachMessage.changeLog.forEachIndexed { changeIndex, outreachChange ->
                    if (changeIndex > 0) {
                        AddSpace(spacingDefault)
                        ThinDivider()
                        AddSpace(spacingDefault)
                    }
                    LabelText("Review pass ${outreachChange.passNumber}")
                    AddSpace(spacingXSmall)
                    SecondaryText("Before")
                    BodyText(outreachChange.before.ifBlank { "–" })
                    AddSpace(spacingXSmall)
                    SecondaryText("After")
                    BodyText(outreachChange.after.ifBlank { "–" })
                    if (outreachChange.reason.isNotBlank()) {
                        AddSpace(spacingXSmall)
                        SecondaryText("Why: ${outreachChange.reason}")
                    }
                }
            }
        }
    }
}

@Composable
private fun AiCorrectionCard(aiCorrectionSummary: OcrolusAiCorrectionSummary) {
    val siteColours = LocalSiteColours.current
    EmphasisCard(modifier = Modifier.fillMaxWidth()) {
        EyebrowText("What the AI got wrong", color = siteColours.onEmphasisBlockMuted)
        AddSpace(spacingSmall)
        HeadingText(
            aiCorrectionSummary.error.ifBlank { "No error was recorded for this run." },
            color = siteColours.onEmphasisBlock
        )
        val correctionDetails = listOf(
            "Why it matters" to aiCorrectionSummary.whyItMatters,
            "How it was caught" to aiCorrectionSummary.howItWasCaught,
            "The fix" to aiCorrectionSummary.fix,
        ).filter { (_, detailText) ->
            detailText.isNotBlank()
        }
        correctionDetails.forEach { (detailLabel, detailText) ->
            AddSpace(spacingDefault)
            LabelText(detailLabel, color = siteColours.onEmphasisBlockMuted)
            BodyText(detailText, color = siteColours.onEmphasisBlock)
        }
    }
}
