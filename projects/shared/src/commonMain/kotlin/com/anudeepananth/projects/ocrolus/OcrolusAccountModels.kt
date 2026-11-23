package com.anudeepananth.projects.ocrolus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The three add-on products an Ocrolus customer can buy on top of the core document-capture product.
 *
 * @property displayName the product name as shown to people
 * @property promptDescriptor the only way prompts may describe the product (there is no feature list)
 * @property promptTopic the subject a customer note must mention to count as a signal for this product
 */
@Serializable
enum class OcrolusAddOnProduct(val displayName: String, val promptDescriptor: String, val promptTopic: String) {
    FRAUD_SHIELD("Fraud Shield", "fraud add-on", "fraud"),
    CASHFLOW_INSIGHTS("CashFlow Insights", "cash-flow analytics add-on", "cash-flow analysis"),
    INCOME_VERIFY("Income Verify", "income verification add-on", "income verification"),
}

/**
 * One customer account from the assignment dataset, exactly as Ocrolus supplied it.
 *
 * @property accountId the dataset id such as `A001`; also the MongoDB document id
 * @property monthsSinceLastQuarterlyBusinessReview months since the customer last had a review meeting
 * @property customerSuccessManagerNote free-text note written by the account's Customer Success Manager
 */
@Serializable
data class OcrolusAccount(
    @SerialName("_id") val accountId: String,
    val accountName: String,
    val vertical: String,
    val annualRecurringRevenueUsd: Double,
    val monthlyProcessingVolume: Double,
    val hasAdoptedFraudShield: Boolean,
    val hasAdoptedCashFlowInsights: Boolean,
    val hasAdoptedIncomeVerify: Boolean,
    val monthsSinceLastQuarterlyBusinessReview: Int,
    val netPromoterScore: Int,
    val renewalDaysOut: Int,
    val customerSuccessManagerNote: String,
) {
    /** Whether this account already owns [product]. */
    fun hasAdopted(product: OcrolusAddOnProduct): Boolean = when (product) {
        OcrolusAddOnProduct.FRAUD_SHIELD -> hasAdoptedFraudShield
        OcrolusAddOnProduct.CASHFLOW_INSIGHTS -> hasAdoptedCashFlowInsights
        OcrolusAddOnProduct.INCOME_VERIFY -> hasAdoptedIncomeVerify
    }
}

/**
 * An account plus everything a run worked out about it: the model's judgements and the computed scores.
 *
 * @property fraudShieldNoteScore the model's 0-10 buying-signal score, or null when the product is already adopted
 * @property noteScoreEvidenceQuote the words from the note the model quoted to justify a score of 4 or more
 * @property fraudShieldPriority 0-100 priority computed in code (intent x health x size); 0 means excluded
 * @property bestProductLabel the product with the highest priority, or "Tie" / "None"
 * @property overallRank position in the campaign ranking for the chosen product, or null when not eligible
 */
@Serializable
data class OcrolusScoredAccount(
    val account: OcrolusAccount,
    val fraudShieldNoteScore: Int? = null,
    val cashFlowInsightsNoteScore: Int? = null,
    val incomeVerifyNoteScore: Int? = null,
    val noteScoreEvidenceQuote: String = "",
    val isFlaggedAsRisk: Boolean = false,
    val riskEvidenceQuote: String = "",
    val fraudShieldPriority: Double = 0.0,
    val cashFlowInsightsPriority: Double = 0.0,
    val incomeVerifyPriority: Double = 0.0,
    val bestProductLabel: String = "None",
    val anomalyScore: Int? = null,
    val anomalyReason: String? = null,
    val isFlaggedForManualReview: Boolean = false,
    val overallRank: Int? = null,
) {
    /** The model's note score for [product]; null when the account already owns it. */
    fun noteScoreFor(product: OcrolusAddOnProduct): Int? = when (product) {
        OcrolusAddOnProduct.FRAUD_SHIELD -> fraudShieldNoteScore
        OcrolusAddOnProduct.CASHFLOW_INSIGHTS -> cashFlowInsightsNoteScore
        OcrolusAddOnProduct.INCOME_VERIFY -> incomeVerifyNoteScore
    }

    /** The computed 0-100 priority for [product]. */
    fun priorityFor(product: OcrolusAddOnProduct): Double = when (product) {
        OcrolusAddOnProduct.FRAUD_SHIELD -> fraudShieldPriority
        OcrolusAddOnProduct.CASHFLOW_INSIGHTS -> cashFlowInsightsPriority
        OcrolusAddOnProduct.INCOME_VERIFY -> incomeVerifyPriority
    }
}
