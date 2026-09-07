package web.seo

import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import knowledge.ContactDetails

/**
 * Crawl surfaces: robots.txt and sitemap.xml.
 *
 * LLM crawlers are listed with an explicit Allow so training/answer bots
 * that check their own user-agent still treat the site as open.
 */
fun Routing.seoDiscoveryRoutes() {
    get("/robots.txt") {
        call.respondText(buildRobotsTxt(), ContentType.Text.Plain)
    }

    get("/sitemap.xml") {
        call.respondText(buildSitemapXml(), ContentType.Application.Xml)
    }
}

internal fun buildRobotsTxt(): String {
    val siteUrl = ContactDetails.PORTFOLIO_URL
    val llmCrawlerUserAgents = listOf(
        "GPTBot",
        "ChatGPT-User",
        "Google-Extended",
        "ClaudeBot",
        "anthropic-ai",
        "PerplexityBot",
        "Applebot-Extended",
        "CCBot",
    )
    return buildString {
        appendLine("User-agent: *")
        appendLine("Allow: /")
        appendLine()
        llmCrawlerUserAgents.forEach { userAgent ->
            appendLine("User-agent: $userAgent")
            appendLine("Allow: /")
            appendLine()
        }
        appendLine("Sitemap: $siteUrl/sitemap.xml")
        appendLine()
        appendLine("# LLM-readable overview (llmstxt.org)")
        appendLine("# $siteUrl/llms.txt")
        appendLine("# $siteUrl/llms-full.txt")
    }
}

internal fun buildSitemapXml(): String {
    val siteUrl = ContactDetails.PORTFOLIO_URL
    val lastmod = PortfolioSeo.SITEMAP_LASTMOD
    val ogImageUrl = PortfolioSeo.ogImageUrl()
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
                xmlns:image="http://www.google.com/schemas/sitemap-image/1.1">
          <url>
            <loc>$siteUrl/</loc>
            <lastmod>$lastmod</lastmod>
            <changefreq>weekly</changefreq>
            <priority>1.0</priority>
            <image:image>
              <image:loc>$ogImageUrl</image:loc>
              <image:title>${ContactDetails.FULL_NAME}</image:title>
              <image:caption>${escapeXml(PortfolioSeo.OG_IMAGE_ALT)}</image:caption>
            </image:image>
          </url>
          <url>
            <loc>$siteUrl/llms.txt</loc>
            <lastmod>$lastmod</lastmod>
            <changefreq>weekly</changefreq>
            <priority>0.8</priority>
          </url>
          <url>
            <loc>$siteUrl/llms-full.txt</loc>
            <lastmod>$lastmod</lastmod>
            <changefreq>weekly</changefreq>
            <priority>0.6</priority>
          </url>
        </urlset>
    """.trimIndent()
}

private fun escapeXml(value: String): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
