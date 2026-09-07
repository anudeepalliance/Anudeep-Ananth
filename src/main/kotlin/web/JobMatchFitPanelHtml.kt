package web

import agents.JobMatchEligibilityPrecheckResult
import agents.JobMatchFitAnalysis
import agents.JobMatchFitAnalysisOutcome
import kotlinx.html.ButtonType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.ul

/**
 * Renders the HTMX Job Matching fit panel (success or retryable error).
 */
fun renderJobMatchFitPanelHtml(outcome: JobMatchFitAnalysisOutcome): String {
    return when (outcome) {
        is JobMatchFitAnalysisOutcome.Success -> renderSuccessfulFitPanelHtml(
            fitAnalysis = outcome.fitAnalysis,
            eligibilityPrecheck = outcome.eligibilityPrecheck,
        )

        is JobMatchFitAnalysisOutcome.Failure -> renderFailedFitPanelHtml(
            errorMessage = outcome.errorMessage,
        )
    }
}

private fun renderSuccessfulFitPanelHtml(
    fitAnalysis: JobMatchFitAnalysis,
    eligibilityPrecheck: JobMatchEligibilityPrecheckResult,
): String {
    val scoreToneClass = when {
        fitAnalysis.softReject -> "fit-score is-soft-reject"
        fitAnalysis.fitScore >= 75 -> "fit-score is-strong"
        fitAnalysis.fitScore >= 55 -> "fit-score is-mid"
        else -> "fit-score is-low"
    }

    return createHTML().div(classes = "fit-result") {
        div(classes = "fit-result-head") {
            div(classes = scoreToneClass) {
                span(classes = "fit-score-label") { +"fit_score" }
                span(classes = "fit-score-value") { +fitAnalysis.fitScore.toString() }
            }
            if (fitAnalysis.softReject) {
                span(classes = "chip chip-danger") { +"soft reject" }
            } else if (eligibilityPrecheck.preferenceWarnings.isNotEmpty()) {
                span(classes = "chip chip-warn") { +"preference gaps" }
            } else {
                span(classes = "chip chip-ok") { +"in scope path" }
            }
        }

        p(classes = "fit-summary") { +fitAnalysis.summary }

        if (fitAnalysis.softRejectReasons.isNotEmpty()) {
            renderBulletBlock(
                title = "Hard-skip / soft-reject signals",
                items = fitAnalysis.softRejectReasons,
                listClass = "fit-list is-warn",
            )
        }

        val preferenceWarnings = (
            fitAnalysis.preferenceWarnings + eligibilityPrecheck.preferenceWarnings
            ).distinct()
        if (preferenceWarnings.isNotEmpty()) {
            renderBulletBlock(
                title = "Preference warnings",
                items = preferenceWarnings,
                listClass = "fit-list is-warn",
            )
        }

        if (fitAnalysis.strengths.isNotEmpty()) {
            renderBulletBlock(
                title = "Strengths",
                items = fitAnalysis.strengths,
                listClass = "fit-list is-ok",
            )
        }

        if (fitAnalysis.gaps.isNotEmpty()) {
            renderBulletBlock(
                title = "Gaps",
                items = fitAnalysis.gaps,
                listClass = "fit-list",
            )
        }

        if (fitAnalysis.talkingPoints.isNotEmpty()) {
            renderBulletBlock(
                title = "Talking points",
                items = fitAnalysis.talkingPoints,
                listClass = "fit-list is-cyan",
            )
        }
    }
}

private fun renderFailedFitPanelHtml(errorMessage: String): String {
    return createHTML().div(classes = "fit-result is-error") {
        h3 { +"Job Matching hit a snag" }
        p { +errorMessage }
        div(classes = "console-actions") {
            button(classes = "btn btn-primary") {
                type = ButtonType.button
                attributes["onclick"] =
                    "document.getElementById('job-matching-form')?.requestSubmit()"
                +"Retry fit analysis"
            }
            a(href = "#agents", classes = "btn btn-ghost") { +"Back to console" }
        }
    }
}

private fun kotlinx.html.FlowContent.renderBulletBlock(
    title: String,
    items: List<String>,
    listClass: String,
) {
    h4(classes = "fit-block-title") { +title }
    ul(classes = listClass) {
        items.forEach { item ->
            li { +item }
        }
    }
}
