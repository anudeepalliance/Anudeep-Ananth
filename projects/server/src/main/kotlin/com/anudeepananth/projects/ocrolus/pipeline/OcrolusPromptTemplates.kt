package com.anudeepananth.projects.ocrolus.pipeline

/** File names of the prompt templates under `resources/prompts/ocrolus/`. */
object OcrolusPromptFiles {
    const val CSM_NOTE_SCORING = "1.1-csm-note-scoring.md"
    const val RISK_FLAG = "1.2-risk-flag.md"
    const val ANOMALY_CHECK = "1.3-anomaly-check.md"
    const val OUTREACH_STRATEGY = "2.1-outreach-strategy.md"
    const val OUTREACH_DRAFT = "2.2-outreach-draft.md"
    const val OUTREACH_REVIEW = "2.3-outreach-review.md"
    const val PRODUCT_RATIONALE = "product-rationale.md"
    const val AI_CORRECTIONS_SUMMARY = "ai-corrections-summary.md"
    const val MEASUREMENT_PLAN = "measurement-plan.md"
}

/**
 * Reads a prompt template from the server's resource files and fills in its `{{PLACEHOLDER}}` slots.
 * Throws if a slot is left unfilled, so a typo can never send a half-built prompt to a model.
 */
fun loadPromptTemplateFromResourcesAndFill(promptFileName: String, placeholderValues: Map<String, String>): String {
    val resourcePath = "/prompts/ocrolus/$promptFileName"
    val templateText = checkNotNull(object {}.javaClass.getResource(resourcePath)) {
        "Missing prompt template $resourcePath"
    }.readText()
    val filledText = placeholderValues.entries.fold(templateText) { textSoFar, (placeholderName, value) ->
        textSoFar.replace("{{$placeholderName}}", value)
    }
    val unfilledPlaceholder = Regex("\\{\\{[A-Z_]+}}").find(
        placeholderValues.values.fold(filledText) { textSoFar, value ->
            textSoFar.replace(value, "")
        },
    )
    check(unfilledPlaceholder == null) {
        "Prompt $promptFileName still has unfilled placeholder ${unfilledPlaceholder?.value}"
    }
    return filledText
}
