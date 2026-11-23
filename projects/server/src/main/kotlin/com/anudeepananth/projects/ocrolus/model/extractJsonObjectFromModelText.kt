package com.anudeepananth.projects.ocrolus.model

private val reasoningBlockPattern =
    Regex("<think>.*?</think>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))

/** Removes any "thinking out loud" block some models put before their real answer. */
fun stripModelReasoningBlocks(modelText: String): String = modelText.replace(reasoningBlockPattern, "").trim()

/**
 * Pulls the JSON object out of a model reply that may wrap it in code fences or extra words.
 * Returns the text from the first `{` to the last `}`; throws when the reply has no object at all.
 */
fun extractJsonObjectFromModelText(modelText: String): String {
    val cleanedText = stripModelReasoningBlocks(modelText)
    val firstBraceIndex = cleanedText.indexOf('{')
    val lastBraceIndex = cleanedText.lastIndexOf('}')
    require(firstBraceIndex >= 0 && lastBraceIndex > firstBraceIndex) {
        "The reply contains no JSON object."
    }
    return cleanedText.substring(firstBraceIndex, lastBraceIndex + 1)
}
