package agents.firecrawl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import util.ConfidentialObjects

private val firecrawlLogger = LoggerFactory.getLogger("FirecrawlScrapeClient")

private const val FIRECRAWL_SCRAPE_URL = "https://api.firecrawl.dev/v1/scrape"
private const val DEFAULT_MARKDOWN_CHAR_CAP = 12_000
private val dataImageMarkdownRegex = Regex("""!\[[^\]]*]\(data:[^)]+\)""")

/**
 * Scrapes a public URL to markdown plus outbound links.
 */
fun interface FirecrawlPageScraper {
    suspend fun scrapePublicPage(pageUrl: String): FirecrawlPageScrapeResult
}

@Serializable
private data class FirecrawlScrapeRequestBody(
    val url: String,
    val formats: List<String> = listOf("markdown", "links"),
    val onlyMainContent: Boolean = false,
)

@Serializable
private data class FirecrawlScrapeResponseBody(
    val success: Boolean = false,
    val data: FirecrawlScrapeDataBody? = null,
    val error: String? = null,
)

@Serializable
private data class FirecrawlScrapeDataBody(
    val markdown: String? = null,
    val links: List<String> = emptyList(),
    val metadata: FirecrawlScrapeMetadataBody? = null,
)

@Serializable
private data class FirecrawlScrapeMetadataBody(
    val title: String? = null,
    val sourceURL: String? = null,
)

/**
 * Thin HTTP client for Firecrawl `/v1/scrape`. Returns markdown, outbound links, or an error string.
 */
class FirecrawlScrapeClient(
    private val httpClient: HttpClient = defaultFirecrawlHttpClient(),
    private val apiKey: String = ConfidentialObjects.FIRECRAWL_API_KEY,
    private val markdownCharacterCap: Int = DEFAULT_MARKDOWN_CHAR_CAP,
) : FirecrawlPageScraper {
    /**
     * Scrapes [pageUrl] and returns a prompt block with title, social links, and markdown.
     */
    suspend fun scrapeUrlToMarkdownOrErrorMessage(pageUrl: String): String {
        return scrapePublicPage(pageUrl = pageUrl).formatAsPrefetchBlock()
    }

    override suspend fun scrapePublicPage(pageUrl: String): FirecrawlPageScrapeResult {
        val trimmedPageUrl = pageUrl.trim()
        return try {
            val responseBody = httpClient.post(FIRECRAWL_SCRAPE_URL) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(FirecrawlScrapeRequestBody(url = trimmedPageUrl))
            }.body<FirecrawlScrapeResponseBody>()

            if (!responseBody.success) {
                return FirecrawlPageScrapeResult(
                    pageUrl = trimmedPageUrl,
                    sourceUrl = trimmedPageUrl,
                    title = null,
                    markdown = "",
                    discoveredLinks = emptyList(),
                    errorMessage = "Firecrawl scrape failed: ${responseBody.error ?: "unknown error"}",
                )
            }

            val markdown = capMarkdown(
                markdown = stripInlineDataImages(responseBody.data?.markdown.orEmpty()),
                markdownCharacterCap = markdownCharacterCap,
            )
            val discoveredLinks = responseBody.data?.links.orEmpty().mapNotNull { rawLink ->
                PublicSocialProfileUrls.canonicalizeHttpUrlOrNull(rawLink) ?: rawLink.trim().takeIf { it.isNotBlank() }
            }.distinct()
            if (markdown.isBlank() && discoveredLinks.isEmpty()) {
                return FirecrawlPageScrapeResult(
                    pageUrl = trimmedPageUrl,
                    sourceUrl = trimmedPageUrl,
                    title = responseBody.data?.metadata?.title,
                    markdown = "",
                    discoveredLinks = emptyList(),
                    errorMessage = "Firecrawl scrape returned empty markdown for $trimmedPageUrl",
                )
            }

            FirecrawlPageScrapeResult(
                pageUrl = trimmedPageUrl,
                sourceUrl = responseBody.data?.metadata?.sourceURL ?: trimmedPageUrl,
                title = responseBody.data?.metadata?.title,
                markdown = markdown,
                discoveredLinks = discoveredLinks,
            )
        } catch (exception: Exception) {
            firecrawlLogger.warn("Firecrawl scrape failed for $trimmedPageUrl: ${exception.message}")
            FirecrawlPageScrapeResult(
                pageUrl = trimmedPageUrl,
                sourceUrl = trimmedPageUrl,
                title = null,
                markdown = "",
                discoveredLinks = emptyList(),
                errorMessage = "Firecrawl scrape error for $trimmedPageUrl: ${exception.message}",
            )
        }
    }
}

private fun stripInlineDataImages(markdown: String): String {
    return dataImageMarkdownRegex.replace(markdown, "").trim()
}

private fun capMarkdown(markdown: String, markdownCharacterCap: Int): String {
    return if (markdown.length <= markdownCharacterCap) {
        markdown
    } else {
        markdown.take(markdownCharacterCap) + "\n\n[truncated]"
    }
}

private fun defaultFirecrawlHttpClient(): HttpClient =
    HttpClient(CIO) {
        engine {
            requestTimeout = 45_000
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    // Firecrawl defaults onlyMainContent to true; false must be sent or footer social links vanish.
                    encodeDefaults = true
                },
            )
        }
    }
