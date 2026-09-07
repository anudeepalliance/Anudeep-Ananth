package agents.minimax

private val WELL_FORMED_THINK_TAG_REGEX = Regex("<think>[\\s\\S]*?</think>")
private val UNCLOSED_THINK_TAG_REGEX = Regex("<think>[\\s\\S]*$")

/**
 * Removes MiniMax `<think>` reasoning blocks (closed or unclosed) from assistant text.
 */
fun stripMiniMaxReasoningTags(rawAssistantText: String): String {
    val withoutWellFormedBlocks = rawAssistantText.replace(WELL_FORMED_THINK_TAG_REGEX, "")
    val withoutUnclosedBlocks = withoutWellFormedBlocks.replace(UNCLOSED_THINK_TAG_REGEX, "")
    return if (withoutUnclosedBlocks.isBlank() && rawAssistantText.isNotBlank()) {
        rawAssistantText.trim()
    } else {
        withoutUnclosedBlocks.trim()
    }
}
