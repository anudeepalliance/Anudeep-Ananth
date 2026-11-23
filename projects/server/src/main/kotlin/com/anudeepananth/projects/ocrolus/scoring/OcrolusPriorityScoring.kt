package com.anudeepananth.projects.ocrolus.scoring

import com.anudeepananth.projects.ocrolus.OcrolusAccount
import com.anudeepananth.projects.ocrolus.OcrolusAddOnProduct
import com.anudeepananth.projects.ocrolus.OcrolusScoredAccount
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** How many accounts the campaign contacts. */
const val CAMPAIGN_TOP_ACCOUNT_COUNT = 15

/** How many of the top accounts get a personalised outreach email. */
const val OUTREACH_ACCOUNT_COUNT = 3

private const val MINIMUM_NET_PROMOTER_SCORE_TO_PITCH = 35
private const val MINIMUM_RENEWAL_DAYS_OUT_TO_PITCH = 60
private const val NET_PROMOTER_SCORE_CAP = 70
private const val MANUAL_REVIEW_ANOMALY_SCORE_THRESHOLD = 5

/**
 * The outcome of scoring all accounts for all three products.
 *
 * @property scoredAccounts every account with priorities, best product and rank filled in
 * @property chosenProduct the product with the highest total priority across all accounts
 * @property topAccountIds the ranked top accounts for [chosenProduct]
 */
data class OcrolusScoringOutcome(
    val scoredAccounts: List<OcrolusScoredAccount>,
    val chosenProduct: OcrolusAddOnProduct,
    val priorityTotalsByProduct: Map<OcrolusAddOnProduct, Double>,
    val topAccountIds: List<String>,
)

/**
 * The campaign scoring maths, kept as plain code so every run gives the same numbers for the same inputs.
 * It mirrors the spreadsheet formulas in Anudeep's submission: priority = 100 x intent x health x size.
 */
object OcrolusPriorityScoring {

    /**
     * Priority (0-100, one decimal) of pitching [product] to [account].
     * Returns 0 when the account must not be pitched: it already owns the product, is flagged as at risk,
     * has a low satisfaction score, renews too soon, or the note score is 0 (declined) or missing.
     */
    fun computePriorityForProduct(
        account: OcrolusAccount,
        product: OcrolusAddOnProduct,
        noteScore: Int?,
        isFlaggedAsRisk: Boolean,
        largestMonthlyProcessingVolume: Double,
    ): Double {
        val isExcluded = account.hasAdopted(product) ||
                isFlaggedAsRisk ||
                account.netPromoterScore < MINIMUM_NET_PROMOTER_SCORE_TO_PITCH ||
                account.renewalDaysOut < MINIMUM_RENEWAL_DAYS_OUT_TO_PITCH ||
                noteScore == null ||
                noteScore == 0
        if (isExcluded) return 0.0

        val intent = noteScore / 10.0
        val satisfactionPart =
            minOf(account.netPromoterScore, NET_PROMOTER_SCORE_CAP).toDouble() / NET_PROMOTER_SCORE_CAP
        val recentReviewPart = when {
            account.monthsSinceLastQuarterlyBusinessReview <= 2 -> 1.0
            account.monthsSinceLastQuarterlyBusinessReview <= 4 -> 0.5
            else -> 0.0
        }
        val renewalTimingPart = if (account.renewalDaysOut in 90..270) 1.0 else 0.5
        val health = 0.6 * satisfactionPart + 0.4 * (recentReviewPart + renewalTimingPart) / 2
        val size = account.monthlyProcessingVolume / largestMonthlyProcessingVolume
        return roundToOneDecimalLikeSpreadsheet(100 * intent * health * size)
    }

    /**
     * Scores every account for every product, picks the winning product and ranks the campaign list.
     * Accounts in [accountsWithModelJudgements] need only the note scores and risk flag filled in.
     */
    fun scoreAllAccountsAndPickProduct(accountsWithModelJudgements: List<OcrolusScoredAccount>): OcrolusScoringOutcome {
        val largestMonthlyProcessingVolume = accountsWithModelJudgements.maxOf {
            it.account.monthlyProcessingVolume
        }
        val accountsWithPriorities = accountsWithModelJudgements.map { scoredAccount ->
            val prioritiesByProduct = OcrolusAddOnProduct.entries.associateWith { product ->
                computePriorityForProduct(
                    account = scoredAccount.account,
                    product = product,
                    noteScore = scoredAccount.noteScoreFor(product),
                    isFlaggedAsRisk = scoredAccount.isFlaggedAsRisk,
                    largestMonthlyProcessingVolume = largestMonthlyProcessingVolume,
                )
            }
            scoredAccount.copy(
                fraudShieldPriority = prioritiesByProduct.getValue(OcrolusAddOnProduct.FRAUD_SHIELD),
                cashFlowInsightsPriority = prioritiesByProduct.getValue(OcrolusAddOnProduct.CASHFLOW_INSIGHTS),
                incomeVerifyPriority = prioritiesByProduct.getValue(OcrolusAddOnProduct.INCOME_VERIFY),
                bestProductLabel = labelBestProduct(prioritiesByProduct),
            )
        }
        val priorityTotalsByProduct = OcrolusAddOnProduct.entries.associateWith { product ->
            roundToOneDecimalLikeSpreadsheet(
                accountsWithPriorities.sumOf {
                    it.priorityFor(product)
                },
            )
        }
        val chosenProduct = priorityTotalsByProduct.maxBy {
            it.value
        }.key
        val rankedAccounts = rankAccountsForChosenProduct(accountsWithPriorities, chosenProduct)
        val topAccountIds = rankedAccounts
            .filter {
                it.overallRank != null
            }
            .sortedWith(compareBy({ it.overallRank }, { it.account.accountId }))
            .take(CAMPAIGN_TOP_ACCOUNT_COUNT)
            .map {
                it.account.accountId
            }
        return OcrolusScoringOutcome(rankedAccounts, chosenProduct, priorityTotalsByProduct, topAccountIds)
    }

    /** True when the anomaly score says a person should look at the account before outreach. */
    fun isAnomalyScoreHighEnoughForManualReview(anomalyScore: Int?): Boolean =
        anomalyScore != null && anomalyScore >= MANUAL_REVIEW_ANOMALY_SCORE_THRESHOLD

    private fun labelBestProduct(prioritiesByProduct: Map<OcrolusAddOnProduct, Double>): String {
        val highestPriority = prioritiesByProduct.values.max()
        if (highestPriority == 0.0) return "None"
        val productsAtHighestPriority = prioritiesByProduct.filterValues {
            it == highestPriority
        }.keys
        return if (productsAtHighestPriority.size > 1) "Tie" else productsAtHighestPriority.single().displayName
    }

    /**
     * Rank 1 is the highest priority for [chosenProduct]; equal priorities are split by larger volume.
     * Accounts with priority 0, or whose best opportunity is clearly a different product, get no rank.
     */
    private fun rankAccountsForChosenProduct(
        accountsWithPriorities: List<OcrolusScoredAccount>,
        chosenProduct: OcrolusAddOnProduct,
    ): List<OcrolusScoredAccount> {
        val otherProductNames = OcrolusAddOnProduct.entries.filter {
            it != chosenProduct
        }.map {
            it.displayName
        }
        val eligibleAccounts = accountsWithPriorities.filter {
            it.priorityFor(chosenProduct) > 0 && it.bestProductLabel !in otherProductNames
        }
        return accountsWithPriorities.map { scoredAccount ->
            if (scoredAccount !in eligibleAccounts) return@map scoredAccount.copy(overallRank = null)
            val priority = scoredAccount.priorityFor(chosenProduct)
            val accountsRankedAbove = eligibleAccounts.count { otherAccount ->
                val otherPriority = otherAccount.priorityFor(chosenProduct)
                otherPriority > priority ||
                        (otherPriority == priority && otherAccount.account.monthlyProcessingVolume > scoredAccount.account.monthlyProcessingVolume)
            }
            scoredAccount.copy(overallRank = accountsRankedAbove + 1)
        }
    }

    /** Rounds half up on the 15-digit decimal value, which is how the spreadsheet's ROUND behaves. */
    private fun roundToOneDecimalLikeSpreadsheet(value: Double): Double =
        BigDecimal(value).round(MathContext(15)).setScale(1, RoundingMode.HALF_UP).toDouble()
}
