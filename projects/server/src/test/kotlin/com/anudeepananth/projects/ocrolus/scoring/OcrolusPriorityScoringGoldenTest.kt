package com.anudeepananth.projects.ocrolus.scoring

import com.anudeepananth.projects.ocrolus.OcrolusAccount
import com.anudeepananth.projects.ocrolus.OcrolusAddOnProduct
import com.anudeepananth.projects.ocrolus.OcrolusScoredAccount
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/** One row of Anudeep's scoring sheet: the inputs he used and the numbers his formulas produced. Ids and numbers only. */
@Serializable
private data class GoldenFixtureRow(
    val accountId: String,
    val monthlyProcessingVolume: Double,
    val hasAdoptedFraudShield: Boolean,
    val hasAdoptedCashFlowInsights: Boolean,
    val hasAdoptedIncomeVerify: Boolean,
    val monthsSinceLastQuarterlyBusinessReview: Int,
    val netPromoterScore: Int,
    val renewalDaysOut: Int,
    val fraudShieldNoteScore: Int?,
    val cashFlowInsightsNoteScore: Int?,
    val incomeVerifyNoteScore: Int?,
    val isFlaggedAsRisk: Boolean,
    val expectedFraudShieldPriority: Double,
    val expectedCashFlowInsightsPriority: Double,
    val expectedIncomeVerifyPriority: Double,
    val expectedBestProductLabel: String,
    val expectedOverallRank: Int?,
)

/** Proves the Kotlin scoring gives exactly the numbers in the hand-built spreadsheet. */
class OcrolusPriorityScoringGoldenTest {

    private val fixtureRows = Json.decodeFromString<List<GoldenFixtureRow>>(
        checkNotNull(javaClass.getResource("/ocrolus-scoring-golden-fixture.json")).readText(),
    )

    private val scoringOutcome = OcrolusPriorityScoring.scoreAllAccountsAndPickProduct(
        fixtureRows.map { fixtureRow ->
            OcrolusScoredAccount(
                account = OcrolusAccount(
                    accountId = fixtureRow.accountId,
                    accountName = fixtureRow.accountId,
                    vertical = "",
                    annualRecurringRevenueUsd = 0.0,
                    monthlyProcessingVolume = fixtureRow.monthlyProcessingVolume,
                    hasAdoptedFraudShield = fixtureRow.hasAdoptedFraudShield,
                    hasAdoptedCashFlowInsights = fixtureRow.hasAdoptedCashFlowInsights,
                    hasAdoptedIncomeVerify = fixtureRow.hasAdoptedIncomeVerify,
                    monthsSinceLastQuarterlyBusinessReview = fixtureRow.monthsSinceLastQuarterlyBusinessReview,
                    netPromoterScore = fixtureRow.netPromoterScore,
                    renewalDaysOut = fixtureRow.renewalDaysOut,
                    customerSuccessManagerNote = "",
                ),
                fraudShieldNoteScore = fixtureRow.fraudShieldNoteScore,
                cashFlowInsightsNoteScore = fixtureRow.cashFlowInsightsNoteScore,
                incomeVerifyNoteScore = fixtureRow.incomeVerifyNoteScore,
                isFlaggedAsRisk = fixtureRow.isFlaggedAsRisk,
            )
        },
    )

    @Test
    fun `every priority best product and rank matches the spreadsheet`() {
        assertEquals(50, fixtureRows.size)
        val scoredAccountsById = scoringOutcome.scoredAccounts.associateBy {
            it.account.accountId
        }
        fixtureRows.forEach { fixtureRow ->
            val scoredAccount = scoredAccountsById.getValue(fixtureRow.accountId)
            assertEquals(
                fixtureRow.expectedFraudShieldPriority,
                scoredAccount.fraudShieldPriority,
                "${fixtureRow.accountId} Fraud Shield"
            )
            assertEquals(
                fixtureRow.expectedCashFlowInsightsPriority,
                scoredAccount.cashFlowInsightsPriority,
                "${fixtureRow.accountId} CashFlow Insights"
            )
            assertEquals(
                fixtureRow.expectedIncomeVerifyPriority,
                scoredAccount.incomeVerifyPriority,
                "${fixtureRow.accountId} Income Verify"
            )
            assertEquals(
                fixtureRow.expectedBestProductLabel,
                scoredAccount.bestProductLabel,
                "${fixtureRow.accountId} best product"
            )
            assertEquals(fixtureRow.expectedOverallRank, scoredAccount.overallRank, "${fixtureRow.accountId} rank")
        }
    }

    @Test
    fun `fraud shield wins and the top fifteen come out in spreadsheet order`() {
        assertEquals(OcrolusAddOnProduct.FRAUD_SHIELD, scoringOutcome.chosenProduct)
        assertEquals(390.2, scoringOutcome.priorityTotalsByProduct.getValue(OcrolusAddOnProduct.FRAUD_SHIELD))
        val expectedTopAccountIds = fixtureRows
            .filter {
                it.expectedOverallRank != null && it.expectedOverallRank <= CAMPAIGN_TOP_ACCOUNT_COUNT
            }
            .sortedBy {
                it.expectedOverallRank
            }
            .map {
                it.accountId
            }
        assertEquals(expectedTopAccountIds, scoringOutcome.topAccountIds)
        assertEquals(listOf("A002", "A001", "A018"), scoringOutcome.topAccountIds.take(OUTREACH_ACCOUNT_COUNT))
    }
}
