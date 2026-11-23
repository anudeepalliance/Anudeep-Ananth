package com.anudeepananth.projects.ocrolus.pipeline

import com.anudeepananth.projects.ocrolus.OcrolusAccount
import com.anudeepananth.projects.ocrolus.OcrolusAddOnProduct
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusModelCallLog
import com.anudeepananth.projects.ocrolus.OcrolusModelOption
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.ocrolus.OcrolusStepStatus
import com.anudeepananth.projects.ocrolus.data.OcrolusRunStorage
import com.anudeepananth.projects.ocrolus.model.AssignmentModelCaller
import com.anudeepananth.projects.ocrolus.model.AssignmentModelReply
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Keeps runs and call logs in memory so the pipeline can be tested without MongoDB. */
private class InMemoryRunStorage(private val accounts: List<OcrolusAccount>) : OcrolusRunStorage {
    val savedRunsById = mutableMapOf<String, OcrolusAssignmentRun>()
    val savedModelCallLogs = mutableListOf<OcrolusModelCallLog>()

    override suspend fun fetchAllAccounts() = accounts
    override suspend fun saveRun(run: OcrolusAssignmentRun) {
        savedRunsById[run.runId] = run
    }

    override suspend fun fetchRunById(runId: String) = savedRunsById[runId]
    override suspend fun fetchAllRunsNewestFirst() = savedRunsById.values.toList()
    override suspend fun fetchRunListFieldsNewestFirst() = savedRunsById.values.toList()
    override suspend fun fetchFeaturedRunId() = savedRunsById.values.firstOrNull { it.isFeatured }?.runId
    override suspend fun countFreeModelRunsStartedSince(sinceEpochMilliseconds: Long) = 0
    override suspend fun updateRunEditableFields(
        runId: String,
        displayLabel: String?,
        presenterNotes: String?,
        isFeatured: Boolean?
    ) = true

    override suspend fun deleteRunAndItsModelCallLogs(runId: String) = savedRunsById.remove(runId) != null
    override suspend fun markInterruptedRunsAsFailed(errorMessage: String) = Unit
    override suspend fun saveModelCallLog(modelCallLog: OcrolusModelCallLog) {
        savedModelCallLogs += modelCallLog
    }

    override suspend fun fetchModelCallLogsForRun(runId: String) = savedModelCallLogs.filter {
        it.runId == runId
    }
}

/**
 * Stands in for a real AI model. It recognises each prompt by its opening words and returns a
 * well-formed reply, unless a test asks it to misbehave for a particular model or step.
 */
private class ScriptedModelCaller(
    private val modelIdsThatAlwaysFail: Set<String> = emptySet(),
    private var numberOfBrokenNoteScoringRepliesToSendFirst: Int = 0,
    private val shouldInventQuotes: Boolean = false,
) : AssignmentModelCaller {
    val modelIdsCalled = mutableListOf<String>()

    override suspend fun requestModelReply(
        model: OcrolusModelOption,
        promptText: String,
        temperature: Double
    ): AssignmentModelReply {
        modelIdsCalled += model.modelId
        if (model.modelId in modelIdsThatAlwaysFail) error("Status code: 429")
        return AssignmentModelReply(buildReplyFor(promptText), promptTokens = 100, completionTokens = 50)
    }

    private fun buildReplyFor(promptText: String): String = when {
        promptText.startsWith("You are scoring Customer Success Manager") -> {
            if (numberOfBrokenNoteScoringRepliesToSendFirst > 0) {
                numberOfBrokenNoteScoringRepliesToSendFirst--
                "Sorry, here are my thoughts in prose instead of JSON."
            } else {
                val rows = accountIdsInPrompt(promptText).joinToString(",") { accountId ->
                    val quote =
                        if (shouldInventQuotes) "customer begged for every product immediately" else "asked about fraud tooling"
                    val fraudScore = if (accountId == "T1" || accountId == "T2" || accountId == "T3") 8 else 1
                    """{"accountId":"$accountId","fraudShieldScore":$fraudScore,"cashFlowInsightsScore":1,"incomeVerifyScore":1,"evidenceQuote":"$quote"}"""
                }
                "```json\n{\"accounts\":[$rows]}\n```"
            }
        }

        promptText.startsWith("You are reviewing Customer Success Manager") -> {
            val rows = accountIdsInPrompt(promptText).joinToString(",") {
                """{"accountId":"$it","isFlaggedAsRisk":false,"evidenceQuote":""}"""
            }
            """{"accounts":[$rows]}"""
        }

        promptText.startsWith("You are quality-checking") -> {
            val rows = Regex("\"account_id\":\"(T\\d+)\"").findAll(promptText).map {
                """{"accountId":"${it.groupValues[1]}","anomalyScore":1,"anomalyReason":"-"}"""
            }.joinToString(",")
            """{"accounts":[$rows]}"""
        }

        promptText.startsWith("You are writing the product-pick rationale") -> "- Pick: Fraud Shield.\n- It has the highest total priority."
        promptText.startsWith("You are a growth marketing strategist") -> "Strategy for T1, T2 and T3: ask a question, do not pitch."
        promptText.startsWith("You are a copywriter") -> "SUBJECT: one\nBODY: ...\nSUBJECT: two\nBODY: ...\nSUBJECT: three\nBODY: ..."
        promptText.startsWith("You are the final reviewer") -> {
            val emails = listOf("T1", "T2", "T3").joinToString(",") {
                """{"accountId":"$it","finalSubject":"Following up","finalBody":"Hi [First name],\nYour team mentioned fraud.\nCould we talk for 20 minutes?\n[CSM name]","salesinessScoreFirst":4,"salesinessScoreFinal":2,"spamScoreFirst":90,"spamScoreFinal":95,"verdict":"PASS","changeLog":[{"before":"I know more","after":"Your team mentioned fraud","passNumber":1,"reason":"UNVERIFIABLE"}],"readiness":"READY FOR HUMAN REVIEW"}"""
            }
            """{"emails":[$emails],"strategyUpdates":""}"""
        }

        promptText.startsWith("You are documenting how an AI review step") ->
            """{"error":"The draft said \"I know more\"","whyItMatters":"It is made up.","howItWasCaught":"Pass 1 marked it UNVERIFIABLE.","fix":"Reworded."}"""

        promptText.startsWith("You are a growth marketer") ->
            """{"overview":"Adoption among contacted accounts against the comparison group over 90 days is the main measure.","measuredItems":["Read replies at two weeks.","Hold NPS steady as a guardrail.","Treat the result as directional with so few accounts."]}"""

        else -> error("The scripted model does not recognise this prompt: ${promptText.take(60)}")
    }

    private fun accountIdsInPrompt(promptText: String): List<String> =
        Regex("(?m)^(T\\d+) \\|").findAll(promptText).map {
            it.groupValues[1]
        }.toList()
}

class OcrolusAssignmentRunPipelineTest {

    private val freeModel =
        OcrolusModelOption("nvidia/nemotron-3.5-lightning:free", "Primary free model", isFree = true)

    private val testAccounts = (1..6).map { accountNumber ->
        OcrolusAccount(
            accountId = "T$accountNumber",
            accountName = "Test Account $accountNumber",
            vertical = "Mortgage",
            annualRecurringRevenueUsd = 100_000.0,
            monthlyProcessingVolume = 10_000.0 - accountNumber * 1_000,
            hasAdoptedFraudShield = false,
            hasAdoptedCashFlowInsights = false,
            hasAdoptedIncomeVerify = false,
            monthsSinceLastQuarterlyBusinessReview = 1,
            netPromoterScore = 60,
            renewalDaysOut = 180,
            customerSuccessManagerNote = "Ops team asked about fraud tooling in the last review.",
        )
    }

    private suspend fun performRun(
        modelCaller: AssignmentModelCaller,
        storage: InMemoryRunStorage
    ): OcrolusRunProgressTracker {
        val progressTracker = OcrolusRunProgressTracker(
            OcrolusAssignmentRun(
                runId = "testrun1",
                assignmentName = OCROLUS_ASSIGNMENT_NAME,
                status = OcrolusRunStatus.RUNNING,
                modelId = freeModel.modelId,
                modelDisplayName = freeModel.displayName,
                isFreeModel = true,
                startedAtEpochMilliseconds = 0,
                steps = buildPendingRunSteps(),
            ),
            storage,
        )
        OcrolusAssignmentRunPipeline(freeModel, modelCaller, storage, progressTracker).performAllSteps()
        return progressTracker
    }

    @Test
    fun `a well behaved model produces all three parts of the assignment`() = runTest {
        val storage = InMemoryRunStorage(testAccounts)
        val finishedRun = performRun(ScriptedModelCaller(), storage).currentRun

        assertEquals(OcrolusAddOnProduct.FRAUD_SHIELD, finishedRun.result.chosenProduct)
        assertEquals(listOf("T1", "T2", "T3"), finishedRun.result.topAccountIds.take(3))
        assertEquals(3, finishedRun.result.outreachMessages.size)
        assertTrue(finishedRun.result.outreachMessages.all { it.wordCount in 1..150 })
        assertTrue(finishedRun.result.productRationale.contains("Fraud Shield"))
        assertEquals(3, finishedRun.result.measurementPlan?.measuredItems?.size)
        assertEquals("Pass 1 marked it UNVERIFIABLE.", finishedRun.result.aiCorrectionSummary?.howItWasCaught)
        val unfinishedSteps = finishedRun.steps.filter {
            it.status != OcrolusStepStatus.COMPLETED && it.stepKey != OcrolusStepKeys.SAVE_AND_NOTIFY
        }
        assertTrue(unfinishedSteps.isEmpty(), "Unfinished steps: $unfinishedSteps")
        assertEquals(finishedRun.usage.modelCallCount, storage.savedModelCallLogs.size)
        assertEquals(finishedRun, storage.savedRunsById.getValue("testrun1"))
    }

    @Test
    fun `a reply that is not json is sent back once for repair`() = runTest {
        val storage = InMemoryRunStorage(testAccounts)
        val finishedRun =
            performRun(ScriptedModelCaller(numberOfBrokenNoteScoringRepliesToSendFirst = 1), storage).currentRun

        val noteScoringCalls = storage.savedModelCallLogs.filter {
            it.stepKey == OcrolusStepKeys.SCORE_CSM_NOTES
        }
        assertEquals(2, noteScoringCalls.size)
        assertTrue(noteScoringCalls.last().promptText.contains("YOUR PREVIOUS REPLY WAS REJECTED"))
        assertTrue(finishedRun.usage.fallbackModelIdsUsed.isEmpty())
    }

    @Test
    fun `when the chosen model is down a backup takes over and stays in use`() = runTest {
        val storage = InMemoryRunStorage(testAccounts)
        val modelCaller = ScriptedModelCaller(modelIdsThatAlwaysFail = setOf(freeModel.modelId))
        val finishedRun = performRun(modelCaller, storage).currentRun

        assertEquals(1, finishedRun.usage.fallbackModelIdsUsed.size)
        assertEquals(
            1,
            modelCaller.modelIdsCalled.count { it == freeModel.modelId },
            "The broken model must only be tried once"
        )
        assertEquals(3, finishedRun.result.outreachMessages.size)
    }

    @Test
    fun `a score backed by a quote that is not in the note is lowered in code`() = runTest {
        val storage = InMemoryRunStorage(testAccounts)
        val finishedRun = performRun(ScriptedModelCaller(shouldInventQuotes = true), storage).currentRun

        assertTrue(finishedRun.result.scoredAccounts.all { (it.fraudShieldNoteScore ?: 0) <= 3 })
        val guardMessages =
            finishedRun.steps.first { it.stepKey == OcrolusStepKeys.SCORE_CSM_NOTES }.progressMessages.filter {
                it.text.startsWith("Guard:")
            }
        assertEquals(3, guardMessages.size)
    }

    @Test
    fun `when no model gives a usable reply the step fails with a clear error`() = runTest {
        val storage = InMemoryRunStorage(testAccounts)
        val modelCaller = ScriptedModelCaller(numberOfBrokenNoteScoringRepliesToSendFirst = 100)
        val progressTrackerHolder = arrayOfNulls<OcrolusRunProgressTracker>(1)

        assertFailsWith<OcrolusModelStepFailedException> {
            progressTrackerHolder[0] = performRun(modelCaller, storage)
        }
        val failedStep = storage.savedRunsById.getValue("testrun1").steps.first {
            it.stepKey == OcrolusStepKeys.SCORE_CSM_NOTES
        }
        assertEquals(OcrolusStepStatus.FAILED, failedStep.status)
    }
}
