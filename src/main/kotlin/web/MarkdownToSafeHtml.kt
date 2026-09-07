package web

import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

/**
 * Turns agent reply markdown into HTML. Angle brackets are escaped first so a model cannot inject tags.
 */
object MarkdownToSafeHtml {
    private val markdownExtensions = listOf(
        TablesExtension.create(),
        AutolinkExtension.create(),
    )
    private val markdownParser = Parser.builder()
        .extensions(markdownExtensions)
        .build()
    private val htmlRenderer = HtmlRenderer.builder()
        .extensions(markdownExtensions)
        .escapeHtml(true)
        .softbreak("<br />")
        .build()

    fun render(markdownText: String): String {
        val normalizedMarkdown = isolateMarkdownPipeTables(markdownText)
            .replace(Regex("(?m)^--\\s*$"), "---")
            .replace("<", "&lt;")
        return htmlRenderer.render(markdownParser.parse(normalizedMarkdown))
    }
}

private val markdownPipeTableRowPattern = Regex("""^\s*\|.+\|\s*$""")
private val markdownPipeTableSeparatorPattern = Regex(
    """^\s*\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)+\|?\s*$""",
)

/**
 * Models often put a pipe table on the next line after a label, with no blank line.
 * The parser then treats those pipes as plain text. This lifts the table onto its own block.
 */
internal fun isolateMarkdownPipeTables(markdownText: String): String {
    val sourceLines = markdownText.lines()
    val normalizedLines = mutableListOf<String>()
    var lineIndex = 0
    while (lineIndex < sourceLines.size) {
        val currentLine = sourceLines[lineIndex]
        val nextLine = sourceLines.getOrNull(lineIndex + 1)
        val isPipeTableStart = looksLikeMarkdownPipeTableRow(currentLine) &&
            nextLine != null &&
            looksLikeMarkdownPipeTableSeparator(nextLine)
        if (isPipeTableStart) {
            if (normalizedLines.isNotEmpty() && normalizedLines.last().isNotBlank()) {
                normalizedLines.add("")
            }
            while (lineIndex < sourceLines.size && looksLikeMarkdownPipeTableRow(sourceLines[lineIndex])) {
                normalizedLines.add(sourceLines[lineIndex].trim())
                lineIndex += 1
            }
            if (lineIndex < sourceLines.size && sourceLines[lineIndex].isNotBlank()) {
                normalizedLines.add("")
            }
            continue
        }
        normalizedLines.add(currentLine)
        lineIndex += 1
    }
    return normalizedLines.joinToString("\n")
}

private fun looksLikeMarkdownPipeTableRow(line: String): Boolean {
    return markdownPipeTableRowPattern.matches(line) || looksLikeMarkdownPipeTableSeparator(line)
}

private fun looksLikeMarkdownPipeTableSeparator(line: String): Boolean {
    return markdownPipeTableSeparatorPattern.matches(line)
}
