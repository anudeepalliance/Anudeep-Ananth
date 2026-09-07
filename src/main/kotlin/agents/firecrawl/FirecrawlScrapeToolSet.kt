package agents.firecrawl

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

/**
 * Koog tool set that scrapes a public URL via Firecrawl and returns markdown.
 */
class FirecrawlScrapeToolSet(
    private val firecrawlScrapeClient: FirecrawlScrapeClient = FirecrawlScrapeClient(),
) : ToolSet {
    @Tool
    @LLMDescription(
        "Scrape a public web page URL and return main-content markdown. " +
            "Use for job posts, company pages, or research links the user pastes.",
    )
    suspend fun scrapePublicPageToMarkdown(
        @LLMDescription("Absolute http(s) URL to scrape.")
        pageUrl: String,
    ): String {
        return firecrawlScrapeClient.scrapeUrlToMarkdownOrErrorMessage(pageUrl = pageUrl.trim())
    }
}

/**
 * Builds a [ToolRegistry] exposing Firecrawl scrape for portfolio agents.
 */
fun buildFirecrawlScrapeToolRegistry(
    firecrawlScrapeToolSet: FirecrawlScrapeToolSet = FirecrawlScrapeToolSet(),
): ToolRegistry =
    ToolRegistry {
        tools(firecrawlScrapeToolSet)
    }
