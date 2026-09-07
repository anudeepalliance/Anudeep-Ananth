package agents.firecrawl

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

/**
 * Koog tool set that scrapes a public URL via Firecrawl and returns markdown plus discovered links.
 */
class FirecrawlScrapeToolSet(
    private val firecrawlScrapeClient: FirecrawlScrapeClient = FirecrawlScrapeClient(),
) : ToolSet {
    @Tool
    @LLMDescription(
        "Scrape a public web page URL and return markdown plus outbound links, including social " +
            "profile URLs (LinkedIn, X/Twitter, Instagram, Facebook, YouTube). Use this whenever the " +
            "user pastes a company site, job post, or social URL, then call it again on each social " +
            "profile you need to review.",
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
