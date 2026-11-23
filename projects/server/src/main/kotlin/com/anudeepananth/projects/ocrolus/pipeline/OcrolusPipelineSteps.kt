package com.anudeepananth.projects.ocrolus.pipeline

import com.anudeepananth.projects.ocrolus.OcrolusPipelineStepDescription
import com.anudeepananth.projects.ocrolus.OcrolusRunStep

/** The assignment's name as shown on every page and saved on every run. */
const val OCROLUS_ASSIGNMENT_NAME = "Ocrolus GTM Engineer take-home"

/** Stable ids of the pipeline steps, in the order they run. */
object OcrolusStepKeys {
    const val LOAD_ACCOUNTS = "load-accounts"
    const val SCORE_CSM_NOTES = "score-csm-notes"
    const val FLAG_AT_RISK_ACCOUNTS = "flag-at-risk-accounts"
    const val COMPUTE_PRIORITIES = "compute-priorities"
    const val PICK_PRODUCT = "pick-product"
    const val RANK_TOP_ACCOUNTS = "rank-top-accounts"
    const val CHECK_ANOMALIES = "check-anomalies"
    const val OUTREACH_STRATEGY = "outreach-strategy"
    const val OUTREACH_DRAFT = "outreach-draft"
    const val OUTREACH_REVIEW = "outreach-review"
    const val SUMMARISE_AI_CORRECTIONS = "summarise-ai-corrections"
    const val MEASUREMENT_PLAN = "measurement-plan"
    const val SAVE_AND_NOTIFY = "save-and-notify"
}

/** Every pipeline step with the plain-words description shown on the assignment page. */
val ocrolusPipelineStepDescriptions = listOf(
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.LOAD_ACCOUNTS,
        "Load the 50 accounts",
        false,
        "Reads the customer accounts from MongoDB."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.SCORE_CSM_NOTES,
        "Score the CSM notes",
        true,
        "The model reads each note and scores the buying signal 0-10 for every add-on the account lacks, quoting its evidence."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.FLAG_AT_RISK_ACCOUNTS,
        "Flag at-risk accounts",
        true,
        "The model marks accounts that should not be pitched right now, quoting the note."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.COMPUTE_PRIORITIES,
        "Compute priorities",
        false,
        "Code multiplies intent, health and size into a 0-100 priority per product and applies the exclusions."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.PICK_PRODUCT,
        "Pick the product",
        true,
        "Code picks the add-on with the highest total priority; the model explains the pick using only the computed numbers."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.RANK_TOP_ACCOUNTS,
        "Rank the top 15",
        false,
        "Code ranks eligible accounts and breaks ties by volume."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.CHECK_ANOMALIES,
        "Check for anomalies",
        true,
        "The model looks for rows a person should review before outreach."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.OUTREACH_STRATEGY,
        "Plan the outreach",
        true,
        "The model writes a strategy for each of the top 3 accounts before any copy exists."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.OUTREACH_DRAFT,
        "Draft the emails",
        true,
        "The model drafts one email per account from the strategy."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.OUTREACH_REVIEW,
        "Review the emails",
        true,
        "A five-pass review: fact check, style, marketing, spam, style again. Every change is logged."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.SUMMARISE_AI_CORRECTIONS,
        "What the AI got wrong",
        true,
        "The model picks the most important error the review caught in the drafts."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.MEASUREMENT_PLAN,
        "Write the measurement plan",
        true,
        "The model answers Part 3 in three to five sentences."
    ),
    OcrolusPipelineStepDescription(
        OcrolusStepKeys.SAVE_AND_NOTIFY,
        "Save and notify",
        false,
        "Saves the run to MongoDB and posts the summary to Discord through n8n."
    ),
)

/** Fresh, all-pending steps for a new run. */
fun buildPendingRunSteps(): List<OcrolusRunStep> = ocrolusPipelineStepDescriptions.map {
    OcrolusRunStep(stepKey = it.stepKey, title = it.title, isPerformedByModel = it.isPerformedByModel)
}
