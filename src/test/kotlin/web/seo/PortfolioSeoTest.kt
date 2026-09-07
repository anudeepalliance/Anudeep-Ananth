package web.seo

import knowledge.ContactDetails
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PortfolioSeoTest {

    @Test
    fun `title and description stay in typical search snippet windows`() {
        assertTrue(PortfolioSeo.PAGE_TITLE.length in 50..60, "title length ${PortfolioSeo.PAGE_TITLE.length}")
        assertTrue(
            PortfolioSeo.META_DESCRIPTION.length in 150..160,
            "description length ${PortfolioSeo.META_DESCRIPTION.length}",
        )
        assertTrue(PortfolioSeo.PAGE_TITLE.startsWith(ContactDetails.FULL_NAME))
        assertTrue(PortfolioSeo.META_DESCRIPTION.contains("lifecycle"))
        assertTrue(PortfolioSeo.META_DESCRIPTION.contains("demand gen"))
        assertTrue(PortfolioSeo.ogImageUrl().startsWith("https://"))
        assertTrue(PortfolioSeo.ogImageUrl().endsWith(PortfolioSeo.OG_IMAGE_PATH))
        assertEquals(1200, PortfolioSeo.OG_IMAGE_WIDTH)
        assertEquals(630, PortfolioSeo.OG_IMAGE_HEIGHT)
    }

    @Test
    fun `json-ld graph includes person website profile page and agents`() {
        val jsonLd = Json.parseToJsonElement(buildPortfolioJsonLdScript()).jsonObject
        val graph = jsonLd["@graph"]!!.jsonArray
        val types = graph.map { graphNode ->
            graphNode.jsonObject["@type"]!!.jsonPrimitive.content
        }
        assertTrue(types.contains("Person"))
        assertTrue(types.contains("WebSite"))
        assertTrue(types.contains("ProfilePage"))
        assertTrue(types.contains("BreadcrumbList"))
        assertTrue(types.contains("ItemList"))
        val person = graph.first { graphNode ->
            graphNode.jsonObject["@type"]!!.jsonPrimitive.content == "Person"
        }.jsonObject
        assertEquals(ContactDetails.FULL_NAME, person["name"]!!.jsonPrimitive.content)
        assertEquals("Lifecycle Marketing Manager", person["jobTitle"]!!.jsonPrimitive.content)
        assertTrue(person["sameAs"]!!.jsonArray.any { sameAsUrl ->
            sameAsUrl.jsonPrimitive.content == ContactDetails.LINKEDIN_URL
        })
        assertEquals("DigitalOcean", person["worksFor"]!!.jsonObject["name"]!!.jsonPrimitive.content)
    }

    @Test
    fun `robots txt allows llm crawlers and points at the sitemap`() {
        val robotsText = buildRobotsTxt()
        assertTrue(robotsText.contains("User-agent: *"))
        assertTrue(robotsText.contains("Allow: /"))
        assertTrue(robotsText.contains("User-agent: GPTBot"))
        assertTrue(robotsText.contains("User-agent: Google-Extended"))
        assertTrue(robotsText.contains("User-agent: ClaudeBot"))
        assertTrue(robotsText.contains("User-agent: PerplexityBot"))
        assertTrue(robotsText.contains("User-agent: Applebot-Extended"))
        assertTrue(robotsText.contains("Sitemap: ${ContactDetails.PORTFOLIO_URL}/sitemap.xml"))
        assertTrue(robotsText.contains("/llms.txt"))
    }

    @Test
    fun `sitemap lists homepage llms files lastmod and og image`() {
        val sitemapXml = buildSitemapXml()
        assertTrue(sitemapXml.contains("<loc>${ContactDetails.PORTFOLIO_URL}/</loc>"))
        assertTrue(sitemapXml.contains("<loc>${ContactDetails.PORTFOLIO_URL}/llms.txt</loc>"))
        assertTrue(sitemapXml.contains("<loc>${ContactDetails.PORTFOLIO_URL}/llms-full.txt</loc>"))
        assertTrue(sitemapXml.contains("<lastmod>${PortfolioSeo.SITEMAP_LASTMOD}</lastmod>"))
        assertTrue(sitemapXml.contains(PortfolioSeo.ogImageUrl()))
        assertTrue(!sitemapXml.contains("#job-matching"))
    }
}
