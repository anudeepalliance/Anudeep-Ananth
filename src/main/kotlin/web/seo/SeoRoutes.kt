package web.seo

import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import knowledge.ContactDetails
import knowledge.HomepageSalesPitch

/**
 * Crawl surfaces: robots.txt and sitemap.xml.
 */
fun Routing.seoDiscoveryRoutes() {
    get("/robots.txt") {
        val robotsText = buildString {
            appendLine("User-agent: *")
            appendLine("Allow: /")
            appendLine()
            appendLine("Sitemap: ${ContactDetails.PORTFOLIO_URL}/sitemap.xml")
            appendLine()
            appendLine("# LLM-readable overview")
            appendLine("# ${ContactDetails.PORTFOLIO_URL}/llms.txt")
        }
        call.respondText(robotsText, ContentType.Text.Plain)
    }

    get("/sitemap.xml") {
        val sitemapXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
              <url>
                <loc>${ContactDetails.PORTFOLIO_URL}/</loc>
                <changefreq>weekly</changefreq>
                <priority>1.0</priority>
              </url>
              <url>
                <loc>${ContactDetails.PORTFOLIO_URL}/llms.txt</loc>
                <changefreq>weekly</changefreq>
                <priority>0.8</priority>
              </url>
              <url>
                <loc>${ContactDetails.PORTFOLIO_URL}/llms-full.txt</loc>
                <changefreq>weekly</changefreq>
                <priority>0.6</priority>
              </url>
            </urlset>
        """.trimIndent()
        call.respondText(sitemapXml, ContentType.Application.Xml)
    }
}

fun personAndWebsiteJsonLdScript(): String {
    val jsonLd = """
        {
          "@context": "https://schema.org",
          "@graph": [
            {
              "@type": "Person",
              "name": "${ContactDetails.FULL_NAME}",
              "url": "${ContactDetails.PORTFOLIO_URL}/",
              "email": "mailto:${ContactDetails.EMAIL}",
              "telephone": "${ContactDetails.PHONE_DISPLAY}",
              "jobTitle": "Lifecycle Marketing Manager",
              "description": ${HomepageSalesPitch.PARAGRAPH.take(220).jsonStringLiteral()},
              "sameAs": ["${ContactDetails.LINKEDIN_URL}"]
            },
            {
              "@type": "WebSite",
              "name": "${ContactDetails.FULL_NAME}",
              "url": "${ContactDetails.PORTFOLIO_URL}/"
            }
          ]
        }
    """.trimIndent()
    return jsonLd
}

private fun String.jsonStringLiteral(): String {
    return "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\""
}
