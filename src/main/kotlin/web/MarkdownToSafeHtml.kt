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
        val normalizedMarkdown = markdownText
            .replace(Regex("(?m)^--\\s*$"), "---")
            .replace("<", "&lt;")
        return htmlRenderer.render(markdownParser.parse(normalizedMarkdown))
    }
}
