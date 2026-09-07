package agents.firecrawl

/**
 * One Firecrawl scrape: markdown, outbound links, and an error string when the scrape failed.
 *
 * @property pageUrl URL we asked Firecrawl to scrape.
 * @property sourceUrl Canonical URL Firecrawl reported, or [pageUrl] when missing.
 * @property title Page title from Firecrawl metadata, if any.
 * @property markdown Main-content markdown, possibly truncated.
 * @property discoveredLinks Outbound http(s) links Firecrawl returned for the page.
 * @property errorMessage Failure text when the scrape did not yield usable markdown; null on success.
 */
data class FirecrawlPageScrapeResult(
    val pageUrl: String,
    val sourceUrl: String,
    val title: String?,
    val markdown: String,
    val discoveredLinks: List<String>,
    val errorMessage: String? = null,
) {
    fun isSuccessful(): Boolean = errorMessage == null && markdown.isNotBlank()

    /**
     * Prompt block for agent grounding. Social profile URLs are listed before markdown so a
     * character cap cannot drop the handles.
     */
    fun formatAsPrefetchBlock(): String {
        return buildString {
            if (errorMessage != null) {
                appendLine("Scraped page: $pageUrl")
                appendLine(errorMessage)
                return@buildString
            }
            appendLine("Scraped page: $sourceUrl")
            if (!title.isNullOrBlank()) {
                appendLine("Title: $title")
            }
            val socialProfileUrls = PublicSocialProfileUrls.extractFromMarkdownAndLinks(
                markdown = markdown,
                discoveredLinks = discoveredLinks,
            )
            if (socialProfileUrls.isNotEmpty()) {
                appendLine("Discovered social profiles:")
                socialProfileUrls.forEach { socialProfileUrl ->
                    appendLine("- $socialProfileUrl")
                }
            }
            if (discoveredLinks.isNotEmpty() && socialProfileUrls.isEmpty()) {
                appendLine("Outbound links (first 20):")
                discoveredLinks.take(20).forEach { outboundLink ->
                    appendLine("- $outboundLink")
                }
            }
            appendLine()
            append(markdown)
        }.trimEnd()
    }
}
