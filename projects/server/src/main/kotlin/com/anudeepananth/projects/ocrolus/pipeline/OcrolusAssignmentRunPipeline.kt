package com.anudeepananth.projects.ocrolus.pipeline

import com.anudeepananth.projects.ocrolus.OcrolusAccount
import com.anudeepananth.projects.ocrolus.OcrolusAddOnProduct
import com.anudeepananth.projects.ocrolus.OcrolusAiCorrectionSummary
import com.anudeepananth.projects.ocrolus.OcrolusMeasurementPlan
import com.anudeepananth.projects.ocrolus.OcrolusModelOption
import com.anudeepananth.projects.ocrolus.OcrolusOutreachMessage
import com.anudeepananth.projects.ocrolus.OcrolusScoredAccount
import com.anudeepananth.projects.ocrolus.OcrolusStepStatus
import com.anudeepananth.projects.ocrolus.data.OcrolusRunStorage
import com.anudeepananth.projects.ocrolus.model.AssignmentModelCaller
import com.anudeepananth.projects.ocrolus.model.extractJsonObjectFromModelText
import com.anudeepananth.projects.ocrolus.scoring.CAMPAIGN_TOP_ACCOUNT_COUNT
import com.anudeepananth.projects.ocrolus.scoring.OUTREACH_ACCOUNT_COUNT
import com.anudeepananth.projects.ocrolus.scoring.OcrolusPriorityScoring
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** How many accounts go into one note-scoring or risk-flag request. Small batches sent at the same time finish much faster on free models. */
private const val ACCOUNTS_PER_MODEL_CALL = 10

private const val SCORING_TEMPERATURE = 0.2
private const val WRITING_TEMPERATURE = 0.6

/** The assignment brief's hard cap on each outreach email. */
private const val MAXIMUM_OUTREACH_EMAIL_WORD_COUNT = 150

/** A note score at or above this needs a quote from the note to back it up (rule 4 of the scoring prompt). */
private const val LOWEST_NOTE_SCORE_THAT_NEEDS_A_QUOTE = 4
private const val NOTE_SCORE_WHEN_QUOTE_IS_MISSING = 3

/** A note score at or above this counts as a clear buying signal when explaining the product pick. */
private const val CLEAR_BUYING_SIGNAL_NOTE_SCORE = 6

/**
 * Performs the whole assignment once: loads the accounts, has the model judge the notes,
 * computes the scores in code, has the model write and review the outreach, and records every step.
 *
 * The maths lives in [OcrolusPriorityScoring]; the model only does the judgement and writing steps.
 */
class OcrolusAssignmentRunPipeline(
    chosenModel: OcrolusModelOption,
    modelCaller: AssignmentModelCaller,
    private val runStorage: OcrolusRunStorage,
    private val progressTracker: OcrolusRunProgressTracker,
) {
    private val modelRequester = OcrolusValidatedModelRequester(chosenModel, modelCaller, runStorage, progressTracker)

    /** Runs every step in order. Throws if a step cannot be completed; the caller marks the run failed. */
    suspend fun performAllSteps() {
        val accounts = runStep(OcrolusStepKeys.LOAD_ACCOUNTS) { stepKey ->
            val loadedAccounts = runStorage.fetchAllAccounts()
            check(loadedAccounts.isNotEmpty()) {
                "No accounts found in MongoDB. Run the seeding script first."
            }
            progressTracker.reportProgress(stepKey, "Loaded ${loadedAccounts.size} accounts from MongoDB")
            loadedAccounts
        }

        val accountsWithNoteScores = runStep(OcrolusStepKeys.SCORE_CSM_NOTES) { stepKey ->
            scoreCustomerSuccessManagerNotes(stepKey, accounts)
        }

        val accountsWithRiskFlags = runStep(OcrolusStepKeys.FLAG_AT_RISK_ACCOUNTS) { stepKey ->
            flagAtRiskAccounts(stepKey, accountsWithNoteScores)
        }

        val scoringOutcome = runStep(OcrolusStepKeys.COMPUTE_PRIORITIES) { stepKey ->
            val outcome = OcrolusPriorityScoring.scoreAllAccountsAndPickProduct(accountsWithRiskFlags)
            val excludedAccountCount = outcome.scoredAccounts.count { scoredAccount ->
                OcrolusAddOnProduct.entries.all {
                    scoredAccount.priorityFor(it) == 0.0
                }
            }
            progressTracker.reportProgress(
                stepKey,
                "Priority = 100 x intent x health x size, computed for 3 products x ${outcome.scoredAccounts.size} accounts"
            )
            progressTracker.reportProgress(stepKey, "$excludedAccountCount accounts are excluded from every product")
            progressTracker.updateResult {
                it.copy(
                    scoredAccounts = outcome.scoredAccounts,
                    fraudShieldPriorityTotal = outcome.priorityTotalsByProduct.getValue(OcrolusAddOnProduct.FRAUD_SHIELD),
                    cashFlowInsightsPriorityTotal = outcome.priorityTotalsByProduct.getValue(OcrolusAddOnProduct.CASHFLOW_INSIGHTS),
                    incomeVerifyPriorityTotal = outcome.priorityTotalsByProduct.getValue(OcrolusAddOnProduct.INCOME_VERIFY),
                )
            }
            outcome
        }
        val chosenProduct = scoringOutcome.chosenProduct

        runStep(OcrolusStepKeys.PICK_PRODUCT) { stepKey ->
            progressTracker.reportProgress(
                stepKey,
                "Highest total priority: ${chosenProduct.displayName} (${
                    scoringOutcome.priorityTotalsByProduct.getValue(chosenProduct)
                })"
            )
            progressTracker.updateResult {
                it.copy(chosenProduct = chosenProduct)
            }
            val rationale = modelRequester.requestValidatedReply(
                stepKey = stepKey,
                promptText = loadPromptTemplateFromResourcesAndFill(
                    OcrolusPromptFiles.PRODUCT_RATIONALE,
                    mapOf(
                        "COMPUTED_FACTS" to buildComputedFactsForRationale(
                            scoringOutcome.scoredAccounts,
                            chosenProduct,
                            scoringOutcome.priorityTotalsByProduct
                        )
                    ),
                ),
                temperature = SCORING_TEMPERATURE,
            ) { replyText ->
                require(replyText.contains(chosenProduct.displayName)) {
                    "The rationale must name the chosen product, ${chosenProduct.displayName}."
                }
                replyText.trim()
            }
            progressTracker.updateResult {
                it.copy(productRationale = rationale)
            }
        }

        runStep(OcrolusStepKeys.RANK_TOP_ACCOUNTS) { stepKey ->
            progressTracker.updateResult {
                it.copy(topAccountIds = scoringOutcome.topAccountIds)
            }
            val accountNamesById = accounts.associate {
                it.accountId to it.accountName
            }
            progressTracker.reportProgress(
                stepKey,
                "Top 3: " + scoringOutcome.topAccountIds.take(OUTREACH_ACCOUNT_COUNT).joinToString {
                    accountNamesById.getValue(it)
                },
            )
            progressTracker.reportProgress(
                stepKey,
                "Ranked ${scoringOutcome.topAccountIds.size} accounts for the campaign; ties broken by volume"
            )
        }

        val accountsWithAnomalyScores = runStep(OcrolusStepKeys.CHECK_ANOMALIES) { stepKey ->
            checkAccountsForAnomalies(stepKey, scoringOutcome.scoredAccounts)
        }

        val outreachAccounts = scoringOutcome.topAccountIds.take(OUTREACH_ACCOUNT_COUNT).map { accountId ->
            accountsWithAnomalyScores.first {
                it.account.accountId == accountId
            }
        }
        val productPlaceholders = mapOf(
            "CHOSEN_PRODUCT" to chosenProduct.displayName,
            "PRODUCT_DESCRIPTOR" to chosenProduct.promptDescriptor,
            "PRODUCT_TOPIC" to chosenProduct.promptTopic,
        )
        val accountDataBlock = buildAccountDataBlockForOutreach(outreachAccounts, chosenProduct, accounts.size)

        val strategyText = runStep(OcrolusStepKeys.OUTREACH_STRATEGY) { stepKey ->
            val strategy = modelRequester.requestValidatedReply(
                stepKey = stepKey,
                promptText = loadPromptTemplateFromResourcesAndFill(
                    OcrolusPromptFiles.OUTREACH_STRATEGY,
                    productPlaceholders + ("ACCOUNT_DATA" to accountDataBlock)
                ),
                temperature = WRITING_TEMPERATURE,
            ) { replyText ->
                outreachAccounts.forEach {
                    require(replyText.contains(it.account.accountId) || replyText.contains(it.account.accountName)) {
                        "The strategy is missing account ${it.account.accountId} (${it.account.accountName})."
                    }
                }
                replyText.trim()
            }
            progressTracker.reportProgress(
                stepKey,
                "Strategy written for ${outreachAccounts.size} accounts before any email copy"
            )
            progressTracker.updateResult {
                it.copy(outreachStrategyText = strategy)
            }
            strategy
        }

        val firstDraftsText = runStep(OcrolusStepKeys.OUTREACH_DRAFT) { stepKey ->
            val drafts = modelRequester.requestValidatedReply(
                stepKey = stepKey,
                promptText = loadPromptTemplateFromResourcesAndFill(
                    OcrolusPromptFiles.OUTREACH_DRAFT,
                    productPlaceholders + mapOf("STRATEGY" to strategyText, "ACCOUNT_DATA" to accountDataBlock),
                ),
                temperature = WRITING_TEMPERATURE,
            ) { replyText ->
                require(Regex("SUBJECT", RegexOption.IGNORE_CASE).findAll(replyText).count() >= outreachAccounts.size) {
                    "Expected ${outreachAccounts.size} emails, each with a SUBJECT and BODY."
                }
                replyText.trim()
            }
            progressTracker.reportProgress(
                stepKey,
                "First drafts written, each with a source note and assumptions list"
            )
            progressTracker.updateResult {
                it.copy(outreachFirstDraftsText = drafts)
            }
            drafts
        }

        val outreachMessages = runStep(OcrolusStepKeys.OUTREACH_REVIEW) { stepKey ->
            reviewOutreachDrafts(
                stepKey,
                outreachAccounts,
                productPlaceholders,
                accountDataBlock,
                strategyText,
                firstDraftsText
            )
        }

        runStep(OcrolusStepKeys.SUMMARISE_AI_CORRECTIONS) { stepKey ->
            val changeLogsText = outreachMessages.joinToString("\n\n") { outreachMessage ->
                "${outreachMessage.accountId} ${outreachMessage.accountName}:\n" + outreachMessage.changeLog.joinToString(
                    "\n"
                ) {
                    "- Pass ${it.passNumber}: \"${it.before}\" -> \"${it.after}\" (${it.reason})"
                }.ifBlank {
                    "- No changes"
                }
            }
            val correctionSummary = modelRequester.requestValidatedReply(
                stepKey = stepKey,
                promptText = loadPromptTemplateFromResourcesAndFill(
                    OcrolusPromptFiles.AI_CORRECTIONS_SUMMARY,
                    mapOf("DRAFTS" to firstDraftsText, "CHANGE_LOGS" to changeLogsText),
                ),
                temperature = SCORING_TEMPERATURE,
            ) { replyText ->
                val summary =
                    modelReplyJson.decodeFromString<OcrolusAiCorrectionSummary>(extractJsonObjectFromModelText(replyText))
                require(summary.error.isNotBlank()) {
                    "The \"error\" field is empty."
                }
                summary
            }
            progressTracker.updateResult {
                it.copy(aiCorrectionSummary = correctionSummary)
            }
        }

        runStep(OcrolusStepKeys.MEASUREMENT_PLAN) { stepKey ->
            val comparisonGroupSize = accountsWithAnomalyScores.count {
                (it.overallRank ?: 0) > CAMPAIGN_TOP_ACCOUNT_COUNT
            }
            val campaignFacts = listOf(
                "- Product: ${chosenProduct.displayName}",
                "- Contacted accounts: the top ${scoringOutcome.topAccountIds.size} by priority score",
                "- Personalised emails went to the top ${outreachAccounts.size}; the email asks for a 20-minute call with the account's CSM",
                "- Comparison group: the $comparisonGroupSize eligible accounts ranked below the top ${scoringOutcome.topAccountIds.size}, which get no outreach",
                "- The dataset has NPS and days-to-renewal for every account",
            ).joinToString("\n")
            val measurementPlan = modelRequester.requestValidatedReply(
                stepKey = stepKey,
                promptText = loadPromptTemplateFromResourcesAndFill(
                    OcrolusPromptFiles.MEASUREMENT_PLAN,
                    mapOf("CAMPAIGN_FACTS" to campaignFacts, "CHOSEN_PRODUCT" to chosenProduct.displayName),
                ),
                temperature = WRITING_TEMPERATURE,
            ) { replyText ->
                val plan =
                    modelReplyJson.decodeFromString<OcrolusMeasurementPlan>(extractJsonObjectFromModelText(replyText))
                val cleanedPlan = OcrolusMeasurementPlan(
                    overview = plan.overview.trim(),
                    measuredItems = plan.measuredItems.map {
                        it.trim().trimStart('-', '*', '•', ' ')
                    }.filter {
                        it.isNotBlank()
                    },
                )
                require(countSentences(cleanedPlan.overview) == 1) {
                    "\"overview\" must be exactly one sentence."
                }
                require(cleanedPlan.measuredItems.size in 2..4 && cleanedPlan.measuredItems.all { countSentences(it) == 1 }) {
                    "\"measuredItems\" must hold 2 to 4 entries of exactly one sentence each."
                }
                cleanedPlan
            }
            progressTracker.updateResult {
                it.copy(measurementPlan = measurementPlan)
            }
        }
    }

    /** Marks a step running, does the work, then marks it completed, or failed if the work throws. */
    private suspend fun <StepOutput> runStep(stepKey: String, work: suspend (String) -> StepOutput): StepOutput {
        progressTracker.markStepStatus(stepKey, OcrolusStepStatus.RUNNING)
        try {
            val stepOutput = work(stepKey)
            progressTracker.markStepStatus(stepKey, OcrolusStepStatus.COMPLETED)
            return stepOutput
        } catch (exception: Exception) {
            progressTracker.markStepStatus(stepKey, OcrolusStepStatus.FAILED)
            throw exception
        }
    }

    private suspend fun scoreCustomerSuccessManagerNotes(
        stepKey: String,
        accounts: List<OcrolusAccount>
    ): List<OcrolusScoredAccount> {
        val accountBatches = accounts.chunked(ACCOUNTS_PER_MODEL_CALL)
        return mapBatchesAtTheSameTime(accountBatches) { batchIndex, accountBatch ->
            val accountRows = accountBatch.joinToString("\n") {
                "${it.accountId} | ${yesOrNo(it.hasAdoptedFraudShield)} | ${yesOrNo(it.hasAdoptedCashFlowInsights)} | ${
                    yesOrNo(
                        it.hasAdoptedIncomeVerify
                    )
                } | ${it.customerSuccessManagerNote}"
            }
            val replyRowsByAccountId = modelRequester.requestValidatedReply(
                stepKey = stepKey,
                promptText = loadPromptTemplateFromResourcesAndFill(
                    OcrolusPromptFiles.CSM_NOTE_SCORING,
                    mapOf("ACCOUNT_ROWS" to accountRows)
                ),
                temperature = SCORING_TEMPERATURE,
            ) { replyText ->
                val reply = modelReplyJson.decodeFromString<NoteScoringReply>(extractJsonObjectFromModelText(replyText))
                requireEveryAccountExactlyOnce(accountBatch, reply.accounts.map { it.accountId })
                reply.accounts.forEach { replyRow ->
                    listOf(
                        replyRow.fraudShieldScore,
                        replyRow.cashFlowInsightsScore,
                        replyRow.incomeVerifyScore
                    ).forEach { score ->
                        require(score == null || score in 0..10) {
                            "Account ${replyRow.accountId} has score $score. Scores must be whole numbers from 0 to 10."
                        }
                    }
                }
                reply.accounts.associateBy {
                    it.accountId
                }
            }
            val scoredBatch = accountBatch.map { account ->
                applyNoteScoreRulesInCode(stepKey, account, replyRowsByAccountId.getValue(account.accountId))
            }
            progressTracker.reportProgress(
                stepKey,
                "Batch ${batchIndex + 1} of ${accountBatches.size}: scored ${accountBatch.size} notes"
            )
            scoredBatch
        }
    }

    /**
     * Enforces in code what the scoring prompt asks of the model, so a sloppy reply cannot skew the ranking:
     * an adopted product never gets a score, a missing score counts as neutral (1), and a score of 4 or more
     * is lowered to 3 unless the model's quote really appears in the note.
     */
    private suspend fun applyNoteScoreRulesInCode(
        stepKey: String,
        account: OcrolusAccount,
        replyRow: NoteScoreReplyRow
    ): OcrolusScoredAccount {
        val isQuoteFoundInNote = isQuoteGroundedInNote(replyRow.evidenceQuote, account.customerSuccessManagerNote)
        suspend fun scoreAfterRules(product: OcrolusAddOnProduct, modelScore: Int?): Int? {
            if (account.hasAdopted(product)) return null
            val score = modelScore ?: 1
            if (score >= LOWEST_NOTE_SCORE_THAT_NEEDS_A_QUOTE && !isQuoteFoundInNote) {
                progressTracker.reportProgress(
                    stepKey,
                    "Guard: lowered ${account.accountId} ${product.displayName} from $score to $NOTE_SCORE_WHEN_QUOTE_IS_MISSING (no matching quote from the note)",
                )
                return NOTE_SCORE_WHEN_QUOTE_IS_MISSING
            }
            return score
        }
        return OcrolusScoredAccount(
            account = account,
            fraudShieldNoteScore = scoreAfterRules(OcrolusAddOnProduct.FRAUD_SHIELD, replyRow.fraudShieldScore),
            cashFlowInsightsNoteScore = scoreAfterRules(
                OcrolusAddOnProduct.CASHFLOW_INSIGHTS,
                replyRow.cashFlowInsightsScore
            ),
            incomeVerifyNoteScore = scoreAfterRules(OcrolusAddOnProduct.INCOME_VERIFY, replyRow.incomeVerifyScore),
            noteScoreEvidenceQuote = replyRow.evidenceQuote,
        )
    }

    private suspend fun flagAtRiskAccounts(
        stepKey: String,
        accountsWithNoteScores: List<OcrolusScoredAccount>
    ): List<OcrolusScoredAccount> {
        val accountBatches = accountsWithNoteScores.chunked(ACCOUNTS_PER_MODEL_CALL)
        val accountsWithRiskFlags = mapBatchesAtTheSameTime(accountBatches) { batchIndex, accountBatch ->
            val accountRows = accountBatch.joinToString("\n") {
                "${it.account.accountId} | ${it.account.customerSuccessManagerNote}"
            }
            val replyRowsByAccountId = modelRequester.requestValidatedReply(
                stepKey = stepKey,
                promptText = loadPromptTemplateFromResourcesAndFill(
                    OcrolusPromptFiles.RISK_FLAG,
                    mapOf("ACCOUNT_ROWS" to accountRows)
                ),
                temperature = SCORING_TEMPERATURE,
            ) { replyText ->
                val reply = modelReplyJson.decodeFromString<RiskFlagReply>(extractJsonObjectFromModelText(replyText))
                requireEveryAccountExactlyOnce(accountBatch.map { it.account }, reply.accounts.map { it.accountId })
                reply.accounts.associateBy {
                    it.accountId
                }
            }
            val flaggedBatch = accountBatch.map { scoredAccount ->
                val replyRow = replyRowsByAccountId.getValue(scoredAccount.account.accountId)
                val isQuoteFoundInNote =
                    isQuoteGroundedInNote(replyRow.evidenceQuote, scoredAccount.account.customerSuccessManagerNote)
                if (replyRow.isFlaggedAsRisk && !isQuoteFoundInNote) {
                    progressTracker.reportProgress(
                        stepKey,
                        "Guard: ${scoredAccount.account.accountId} risk flag dropped (no matching quote from the note)"
                    )
                }
                scoredAccount.copy(
                    isFlaggedAsRisk = replyRow.isFlaggedAsRisk && isQuoteFoundInNote,
                    riskEvidenceQuote = if (replyRow.isFlaggedAsRisk) replyRow.evidenceQuote else "",
                )
            }
            progressTracker.reportProgress(
                stepKey,
                "Batch ${batchIndex + 1} of ${accountBatches.size}: checked ${accountBatch.size} notes"
            )
            flaggedBatch
        }
        progressTracker.reportProgress(
            stepKey,
            "${accountsWithRiskFlags.count { it.isFlaggedAsRisk }} accounts flagged as at risk and excluded"
        )
        return accountsWithRiskFlags
    }

    private suspend fun checkAccountsForAnomalies(
        stepKey: String,
        scoredAccounts: List<OcrolusScoredAccount>
    ): List<OcrolusScoredAccount> {
        val accountRows = scoredAccounts.joinToString("\n") { scoredAccount ->
            val account = scoredAccount.account
            buildJsonObject {
                put("account_id", account.accountId)
                put("vertical", account.vertical)
                put("arr_usd", account.annualRecurringRevenueUsd)
                put("monthly_processing_volume", account.monthlyProcessingVolume)
                put("fraud_shield_adopted", yesOrNo(account.hasAdoptedFraudShield))
                put("cashflow_insights_adopted", yesOrNo(account.hasAdoptedCashFlowInsights))
                put("income_verify_adopted", yesOrNo(account.hasAdoptedIncomeVerify))
                put("months_since_last_qbr", account.monthsSinceLastQuarterlyBusinessReview)
                put("nps_score", account.netPromoterScore)
                put("renewal_days_out", account.renewalDaysOut)
                put("csm_note", account.customerSuccessManagerNote)
                put("csm_score_fraud_shield", scoredAccount.fraudShieldNoteScore?.toString() ?: "-")
                put("csm_score_cashflow_insights", scoredAccount.cashFlowInsightsNoteScore?.toString() ?: "-")
                put("csm_score_income_verify", scoredAccount.incomeVerifyNoteScore?.toString() ?: "-")
                put("flagged_as_risk", yesOrNo(scoredAccount.isFlaggedAsRisk))
                put("priority_fraud_shield", scoredAccount.fraudShieldPriority)
                put("priority_cashflow_insights", scoredAccount.cashFlowInsightsPriority)
                put("priority_income_verify", scoredAccount.incomeVerifyPriority)
            }.toString()
        }
        val replyRowsByAccountId = modelRequester.requestValidatedReply(
            stepKey = stepKey,
            promptText = loadPromptTemplateFromResourcesAndFill(
                OcrolusPromptFiles.ANOMALY_CHECK,
                mapOf("ACCOUNT_ROWS" to accountRows)
            ),
            temperature = SCORING_TEMPERATURE,
        ) { replyText ->
            val reply = modelReplyJson.decodeFromString<AnomalyReply>(extractJsonObjectFromModelText(replyText))
            requireEveryAccountExactlyOnce(scoredAccounts.map { it.account }, reply.accounts.map { it.accountId })
            reply.accounts.associateBy {
                it.accountId
            }
        }
        val accountsWithAnomalyScores = scoredAccounts.map { scoredAccount ->
            val replyRow = replyRowsByAccountId.getValue(scoredAccount.account.accountId)
            val anomalyScore = (replyRow.anomalyScore ?: 0).coerceIn(0, 10)
            scoredAccount.copy(
                anomalyScore = anomalyScore,
                anomalyReason = replyRow.anomalyReason,
                isFlaggedForManualReview = OcrolusPriorityScoring.isAnomalyScoreHighEnoughForManualReview(anomalyScore),
            )
        }
        progressTracker.updateResult {
            it.copy(scoredAccounts = accountsWithAnomalyScores)
        }
        val flaggedAccountsInTopList = accountsWithAnomalyScores.count {
            it.isFlaggedForManualReview && (it.overallRank ?: Int.MAX_VALUE) <= CAMPAIGN_TOP_ACCOUNT_COUNT
        }
        progressTracker.reportProgress(
            stepKey,
            "${accountsWithAnomalyScores.count { it.isFlaggedForManualReview }} accounts flagged for manual review, $flaggedAccountsInTopList of them in the top $CAMPAIGN_TOP_ACCOUNT_COUNT",
        )
        return accountsWithAnomalyScores
    }

    private suspend fun reviewOutreachDrafts(
        stepKey: String,
        outreachAccounts: List<OcrolusScoredAccount>,
        productPlaceholders: Map<String, String>,
        accountDataBlock: String,
        strategyText: String,
        firstDraftsText: String,
    ): List<OcrolusOutreachMessage> {
        val reviewReply = modelRequester.requestValidatedReply(
            stepKey = stepKey,
            promptText = loadPromptTemplateFromResourcesAndFill(
                OcrolusPromptFiles.OUTREACH_REVIEW,
                productPlaceholders + mapOf(
                    "ACCOUNT_DATA" to accountDataBlock,
                    "STRATEGY" to strategyText,
                    "DRAFTS" to firstDraftsText
                ),
            ),
            temperature = SCORING_TEMPERATURE,
        ) { replyText ->
            val reply = modelReplyJson.decodeFromString<OutreachReviewReply>(extractJsonObjectFromModelText(replyText))
            requireEveryAccountExactlyOnce(outreachAccounts.map { it.account }, reply.emails.map { it.accountId })
            reply.emails.forEach { email ->
                require(email.finalSubject.isNotBlank() && email.finalBody.isNotBlank()) {
                    "The email for ${email.accountId} has an empty subject or body."
                }
                val wordCount = countWords(email.finalBody)
                require(wordCount <= MAXIMUM_OUTREACH_EMAIL_WORD_COUNT) {
                    "The email for ${email.accountId} has $wordCount words. The hard cap is $MAXIMUM_OUTREACH_EMAIL_WORD_COUNT."
                }
            }
            reply
        }
        val outreachMessages = outreachAccounts.map { scoredAccount ->
            val email = reviewReply.emails.first {
                it.accountId == scoredAccount.account.accountId
            }
            OcrolusOutreachMessage(
                accountId = email.accountId,
                accountName = scoredAccount.account.accountName,
                finalSubject = email.finalSubject.trim(),
                finalBody = email.finalBody.trim(),
                wordCount = countWords(email.finalBody),
                claimsChecked = email.claimsChecked,
                claimsFixed = email.claimsFixed,
                salesinessScoreFirst = email.salesinessScoreFirst,
                salesinessScoreFinal = email.salesinessScoreFinal,
                spamScoreFirst = email.spamScoreFirst,
                spamScoreFinal = email.spamScoreFinal,
                verdict = email.verdict,
                changeLog = email.changeLog,
                flaggedForManualReview = email.flaggedForManualReview,
                readiness = email.readiness,
            )
        }
        progressTracker.reportProgress(
            stepKey,
            "Five-pass review done: ${outreachMessages.sumOf { it.changeLog.size }} changes logged, word counts ${outreachMessages.joinToString { it.wordCount.toString() }}",
        )
        progressTracker.updateResult {
            it.copy(outreachMessages = outreachMessages, outreachStrategyUpdates = reviewReply.strategyUpdates)
        }
        return outreachMessages
    }
}

private fun yesOrNo(value: Boolean): String = if (value) "Y" else "N"

/** Sends every batch to the model at once instead of one after another, and returns the results in batch order. */
private suspend fun <BatchItem, BatchResult> mapBatchesAtTheSameTime(
    batches: List<List<BatchItem>>,
    workOnOneBatch: suspend (Int, List<BatchItem>) -> List<BatchResult>,
): List<BatchResult> = coroutineScope {
    batches.mapIndexed { batchIndex, batch ->
        async {
            workOnOneBatch(batchIndex, batch)
        }
    }.awaitAll().flatten()
}

private fun requireEveryAccountExactlyOnce(expectedAccounts: List<OcrolusAccount>, replyAccountIds: List<String>) {
    val expectedAccountIds = expectedAccounts.map {
        it.accountId
    }
    val missingAccountIds = expectedAccountIds - replyAccountIds.toSet()
    val unexpectedAccountIds = replyAccountIds - expectedAccountIds.toSet()
    require(missingAccountIds.isEmpty() && unexpectedAccountIds.isEmpty() && replyAccountIds.size == replyAccountIds.toSet().size) {
        "Every account must appear exactly once. Missing: ${missingAccountIds.take(10)}. Unexpected or repeated: ${
            unexpectedAccountIds.take(
                10
            )
        }."
    }
}

/**
 * True when most of the quote's words really appear in the note. Used to stop a model from
 * justifying a score with words the Customer Success Manager never wrote.
 */
internal fun isQuoteGroundedInNote(quote: String, note: String): Boolean {
    val quoteWords = splitIntoLowercaseWords(quote)
    if (quoteWords.isEmpty()) return false
    val noteWords = splitIntoLowercaseWords(note).toSet()
    val matchingWordCount = quoteWords.count {
        it in noteWords
    }
    return matchingWordCount.toDouble() / quoteWords.size >= 0.7
}

private fun splitIntoLowercaseWords(text: String): List<String> =
    text.lowercase().split(Regex("[^a-z0-9]+")).filter {
        it.isNotBlank()
    }

internal fun countWords(text: String): Int = text.trim().split(Regex("\\s+")).count {
    it.isNotBlank()
}

internal fun countSentences(text: String): Int = text.trim().split(Regex("(?<=[.!?])\\s+")).count {
    it.isNotBlank()
}

/** The plain numbers the rationale prompt is allowed to use, all computed in code. */
private fun buildComputedFactsForRationale(
    scoredAccounts: List<OcrolusScoredAccount>,
    chosenProduct: OcrolusAddOnProduct,
    priorityTotalsByProduct: Map<OcrolusAddOnProduct, Double>,
): String {
    val totalVolume = scoredAccounts.sumOf {
        it.account.monthlyProcessingVolume
    }
    val perProductLines = OcrolusAddOnProduct.entries.joinToString("\n") { product ->
        val accountsWithoutProduct = scoredAccounts.filter {
            !it.account.hasAdopted(product)
        }
        val adoptedCount = scoredAccounts.size - accountsWithoutProduct.size
        val volumeShareOfNonAdopters =
            Math.round(100 * accountsWithoutProduct.sumOf { it.account.monthlyProcessingVolume } / totalVolume)
        val clearSignalCount = scoredAccounts.count {
            it.priorityFor(product) > 0 && (it.noteScoreFor(product) ?: 0) >= CLEAR_BUYING_SIGNAL_NOTE_SCORE
        }
        "- ${product.displayName}: adopted by $adoptedCount of ${scoredAccounts.size} accounts (${Math.round(100.0 * adoptedCount / scoredAccounts.size)}%); " +
                "${accountsWithoutProduct.size} accounts lack it and they hold $volumeShareOfNonAdopters% of monthly volume; " +
                "$clearSignalCount eligible accounts show a clear signal (note score $CLEAR_BUYING_SIGNAL_NOTE_SCORE or higher); " +
                "total priority score ${priorityTotalsByProduct.getValue(product)}"
    }
    return "Chosen product: ${chosenProduct.displayName} (highest total priority score)\n$perProductLines"
}

/** The account facts given to the three outreach prompts, in the same layout as Anudeep's original prompts. */
private fun buildAccountDataBlockForOutreach(
    outreachAccounts: List<OcrolusScoredAccount>,
    chosenProduct: OcrolusAddOnProduct,
    totalAccountCount: Int,
): String = outreachAccounts.joinToString("\n\n") { scoredAccount ->
    val account = scoredAccount.account
    val adoptedProductNames = OcrolusAddOnProduct.entries.filter {
        account.hasAdopted(it)
    }.joinToString {
        it.displayName
    }.ifBlank {
        "none"
    }
    val notAdoptedProductNames = OcrolusAddOnProduct.entries.filter {
        !account.hasAdopted(it)
    }.joinToString {
        it.displayName
    }
    """
    Account ${account.accountId}: ${account.accountName}
    - Vertical: ${account.vertical}
    - ARR: $${"%,.0f".format(account.annualRecurringRevenueUsd)}
    - Monthly document-processing volume: ${"%,.0f".format(account.monthlyProcessingVolume)}
    - Add-ons adopted: $adoptedProductNames
    - Add-ons not adopted: $notAdoptedProductNames
    - Months since last QBR: ${account.monthsSinceLastQuarterlyBusinessReview}
    - NPS: ${account.netPromoterScore}
    - Days to renewal: ${account.renewalDaysOut}
    - CSM note: "${account.customerSuccessManagerNote}"
    - Priority rank for ${chosenProduct.displayName}: ${scoredAccount.overallRank} of $totalAccountCount
    """.trimIndent()
}
