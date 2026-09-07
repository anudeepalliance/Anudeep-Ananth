package com

import io.ktor.client.request.get
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerTest {

    @Test
    fun `homepage renders portfolio shell from knowledge pack`() = testApplication {
        application {
            configureRouting()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        val html = response.bodyAsText()
        assertTrue(html.contains("Anudeep Ananth"))
        assertTrue(html.contains("Job Matching"))
        assertTrue(html.contains("job description") || html.contains("Job description"))
        assertTrue(html.contains("Job Matching"))
        assertTrue(html.contains("anudeeps3@gmail.com"))
        assertTrue(html.contains("/static/css/lab.css"))
        assertTrue(html.contains("active-agent-id"))
        assertTrue(html.contains("hx-indicator"))
        assertTrue(!html.contains("opacity:0") || html.contains("htmx-indicator"))
        // First-byte page body must stay visible; only indicators may use display:none via CSS class.
        assertTrue(!html.contains("style=\"opacity:0"))
        assertTrue(html.contains("lang=\"en\""))
        assertTrue(html.contains("<title>Anudeep Ananth — B2B SaaS marketing &amp; live AI agents</title>") || html.contains("<title>Anudeep Ananth — B2B SaaS marketing & live AI agents</title>"))
        assertTrue(html.contains("og:image"))
        assertTrue(html.contains("/static/images/og-cover.jpg"))
        assertTrue(html.contains("og:image:width"))
        assertTrue(html.contains("1200"))
        assertTrue(html.contains("twitter:image"))
        assertTrue(html.contains("application/ld+json"))
        assertTrue(html.contains("rel=\"me\""))
        assertTrue(html.contains("/llms.txt"))
        assertTrue(html.contains("<main"))
        assertTrue(html.contains("aria-label=\"Primary\""))
        assertTrue(html.contains("<footer"))
        assertTrue(html.contains("theme-color"))
    }

    @Test
    fun `job match htmx rejects blank JD with retry friendly html`() = testApplication {
        application {
            configureRouting()
        }
        val response = client.submitForm(
            url = "/htmx/job-match",
            formParameters = parameters {
                append("jobDescription", "")
            },
        )
        assertEquals(HttpStatusCode.OK, response.status)
        val html = response.bodyAsText()
        assertTrue(html.contains("Paste a job description") || html.contains("snag") || html.contains("Retry"))
    }

    @Test
    fun `llms txt robots and sitemap are crawlable plaintext`() = testApplication {
        application {
            configureRouting()
        }
        val llmsTxt = client.get("/llms.txt")
        assertEquals(HttpStatusCode.OK, llmsTxt.status)
        assertTrue(llmsTxt.bodyAsText().contains("Anudeep Ananth"))
        assertTrue(llmsTxt.bodyAsText().contains("Job Matching"))

        val llmsFullTxt = client.get("/llms-full.txt")
        assertEquals(HttpStatusCode.OK, llmsFullTxt.status)
        assertTrue(llmsFullTxt.bodyAsText().contains("DigitalOcean"))

        val robotsTxt = client.get("/robots.txt")
        assertEquals(HttpStatusCode.OK, robotsTxt.status)
        assertTrue(robotsTxt.bodyAsText().contains("Sitemap:"))
        assertTrue(robotsTxt.bodyAsText().contains("GPTBot"))

        val sitemapXml = client.get("/sitemap.xml")
        assertEquals(HttpStatusCode.OK, sitemapXml.status)
        assertTrue(sitemapXml.bodyAsText().contains("llms.txt"))
        assertTrue(sitemapXml.bodyAsText().contains("lastmod"))
        assertTrue(sitemapXml.bodyAsText().contains("og-cover.jpg"))
    }

    @Test
    fun `agent chat htmx rejects blank message with retry friendly html`() = testApplication {
        application {
            configureRouting()
        }
        val response = client.submitForm(
            url = "/htmx/agent-chat",
            formParameters = parameters {
                append("agentId", "market_research")
                append("message", "")
            },
        )
        assertEquals(HttpStatusCode.OK, response.status)
        val html = response.bodyAsText()
        assertTrue(html.contains("Type a message") || html.contains("Retry"))
    }
}
