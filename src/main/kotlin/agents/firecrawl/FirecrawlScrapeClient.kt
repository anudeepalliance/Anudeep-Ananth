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

@Serializable
private data class FirecrawlScrapeRequestBody(
    val url: String,
    val formats: List<String> = listOf("markdown"),
    val onlyMainContent: Boolean = true,
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
    val metadata: FirecrawlScrapeMetadataBody? = null,
)

@Serializable
private data class FirecrawlScrapeMetadataBody(
    val title: String? = null,
    val sourceURL: String? = null,
)

/**
 * Thin HTTP client for Firecrawl `/v1/scrape`. Returns markdown main content or an error string.
 */
class FirecrawlScrapeClient(
    private val httpClient: HttpClient = defaultFirecrawlHttpClient(),
    private val apiKey: String = ConfidentialObjects.FIRECRAWL_API_KEY,
) {
    /**
     * Scrapes [pageUrl] and returns truncated markdown suitable for LLM grounding.
     */
    suspend fun scrapeUrlToMarkdownOrErrorMessage(
        pageUrl: String,
        markdownCharacterCap: Int = DEFAULT_MARKDOWN_CHAR_CAP,
    ): String {
        return try {
            val responseBody = httpClient.post(FIRECRAWL_SCRAPE_URL) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(FirecrawlScrapeRequestBody(url = pageUrl))
            }.body<FirecrawlScrapeResponseBody>()

            if (!responseBody.success) {
                return "Firecrawl scrape failed: ${responseBody.error ?: "unknown error"}"
            }

            val markdown = responseBody.data?.markdown?.trim().orEmpty()
            if (markdown.isBlank()) {
                return "Firecrawl scrape returned empty markdown for $pageUrl"
            }

            val title = responseBody.data?.metadata?.title
            val sourceUrl = responseBody.data?.metadata?.sourceURL ?: pageUrl
            val header = buildString {
                appendLine("Scraped page: $sourceUrl")
                if (!title.isNullOrBlank()) {
                    appendLine("Title: $title")
                }
                appendLine()
            }
            val cappedMarkdown = if (markdown.length <= markdownCharacterCap) {
                markdown
            } else {
                markdown.take(markdownCharacterCap) + "\n\n[truncated]"
            }
            header + cappedMarkdown
        } catch (exception: Exception) {
            firecrawlLogger.warn("Firecrawl scrape failed for $pageUrl: ${exception.message}")
            "Firecrawl scrape error for $pageUrl: ${exception.message}"
        }
    }
}

private fun defaultFirecrawlHttpClient(): HttpClient =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
    }
