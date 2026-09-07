package agents

/**
 * Pulls the first balanced top-level JSON object from free-form LLM text.
 */
fun extractJsonObjectFromLlmTextOrNull(rawResponseText: String): String? {
    val firstOpenBraceIndex = rawResponseText.indexOf('{')
    if (firstOpenBraceIndex == -1) {
        return null
    }
    var depth = 0
    var isInsideString = false
    var isEscaped = false
    for (cursor in firstOpenBraceIndex until rawResponseText.length) {
        val character = rawResponseText[cursor]
        if (isEscaped) {
            isEscaped = false
            continue
        }
        if (character == '\\' && isInsideString) {
            isEscaped = true
            continue
        }
        if (character == '"') {
            isInsideString = !isInsideString
            continue
        }
        if (isInsideString) {
            continue
        }
        if (character == '{') {
            depth++
        } else if (character == '}') {
            depth--
            if (depth == 0) {
                return rawResponseText.substring(firstOpenBraceIndex, cursor + 1)
            }
        }
    }
    return null
}
