package web.seo

import knowledge.AgentRosterEntries
import knowledge.AnudeepProfile
import knowledge.ContactDetails
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Homepage search, social, and structured-data copy.
 *
 * Titles stay in the 50–60 character window search results typically show.
 * Descriptions stay in the 150–160 character window so they are not truncated.
 */
object PortfolioSeo {
    const val PAGE_TITLE = "Anudeep Ananth — B2B SaaS marketing & live AI agents"

    const val META_DESCRIPTION =
        "Senior B2B SaaS marketer for lifecycle and demand gen. Paste a role for a live fit score, " +
            "then chat with custom marketing agents Anudeep built and uses."

    const val OG_TITLE = PAGE_TITLE
    const val OG_DESCRIPTION = META_DESCRIPTION
    const val OG_TYPE = "profile"
    const val OG_LOCALE = "en_IN"
    const val SITE_NAME = ContactDetails.FULL_NAME

    const val OG_IMAGE_PATH = "/static/images/og-cover.jpg"
    const val OG_IMAGE_WIDTH = 1200
    const val OG_IMAGE_HEIGHT = 630
    const val OG_IMAGE_ALT = "Portrait of Anudeep Ananth, lifecycle marketing manager"
    const val OG_IMAGE_MIME_TYPE = "image/jpeg"

    const val PERSON_IMAGE_PATH = "/static/images/anudeep-hero.jpg"
    const val PERSON_IMAGE_WIDTH = 1551
    const val PERSON_IMAGE_HEIGHT = 1551

    const val JOB_TITLE = "Lifecycle Marketing Manager"
    const val EMPLOYER_NAME = "DigitalOcean"
    const val EMPLOYER_URL = "https://www.digitalocean.com"

    const val THEME_COLOR = "#ffcc00"
    const val COLOR_SCHEME = "light"
    const val LANGUAGE_CODE = "en"
    const val ROBOTS_CONTENT = "index, follow, max-image-preview:large, max-snippet:-1, max-video-preview:-1"
    const val SITEMAP_LASTMOD = "2026-09-07"

    const val KEYWORDS =
        "Anudeep Ananth, lifecycle marketing, demand generation, B2B SaaS, DigitalOcean, " +
            "growth marketing, marketing ops, AI marketing agents, Job Matching"

    val knowsAboutTopics = listOf(
        "Lifecycle marketing",
        "Demand generation",
        "B2B SaaS marketing",
        "Community-led growth",
        "Marketing operations",
        "Custom AI agents",
    )

    fun homepageUrl(): String = "${ContactDetails.PORTFOLIO_URL}/"

    fun absoluteUrl(path: String): String {
        val siteUrl = ContactDetails.PORTFOLIO_URL.trimEnd('/')
        return if (path.startsWith("/")) "$siteUrl$path" else "$siteUrl/$path"
    }

    fun ogImageUrl(): String = absoluteUrl(OG_IMAGE_PATH)

    fun personImageUrl(): String = absoluteUrl(PERSON_IMAGE_PATH)
}

/**
 * JSON-LD graph for the homepage: Person, WebSite, ProfilePage, breadcrumbs, and the agent list.
 */
fun buildPortfolioJsonLdScript(): String {
    val homepageUrl = PortfolioSeo.homepageUrl()
    val personId = "$homepageUrl#person"
    val websiteId = "$homepageUrl#website"
    val profilePageId = "$homepageUrl#profilepage"
    val breadcrumbId = "$homepageUrl#breadcrumb"
    val agentListId = "$homepageUrl#agents"
    val jsonLdJson = Json { prettyPrint = true }
    val graph = buildJsonObject {
        put("@context", "https://schema.org")
        putJsonArray("@graph") {
            addJsonObject {
                put("@type", "Person")
                put("@id", personId)
                put("name", ContactDetails.FULL_NAME)
                put("givenName", "Anudeep")
                put("familyName", "Ananth")
                put("url", homepageUrl)
                put("email", "mailto:${ContactDetails.EMAIL}")
                put("telephone", ContactDetails.PHONE_DISPLAY)
                put("jobTitle", PortfolioSeo.JOB_TITLE)
                put("description", AnudeepProfile.CAREER_SUMMARY)
                putJsonObject("image") {
                    put("@type", "ImageObject")
                    put("url", PortfolioSeo.personImageUrl())
                    put("contentUrl", PortfolioSeo.personImageUrl())
                    put("width", PortfolioSeo.PERSON_IMAGE_WIDTH)
                    put("height", PortfolioSeo.PERSON_IMAGE_HEIGHT)
                    put("caption", PortfolioSeo.OG_IMAGE_ALT)
                }
                putJsonArray("sameAs") {
                    add(JsonPrimitive(ContactDetails.LINKEDIN_URL))
                }
                putJsonObject("worksFor") {
                    put("@type", "Organization")
                    put("name", PortfolioSeo.EMPLOYER_NAME)
                    put("url", PortfolioSeo.EMPLOYER_URL)
                }
                putJsonObject("homeLocation") {
                    put("@type", "Place")
                    put("name", AnudeepProfile.LOCATION_SUMMARY)
                    putJsonObject("address") {
                        put("@type", "PostalAddress")
                        put("addressCountry", "IN")
                    }
                }
                putJsonArray("knowsAbout") {
                    PortfolioSeo.knowsAboutTopics.forEach { topic ->
                        add(JsonPrimitive(topic))
                    }
                }
                putJsonObject("contactPoint") {
                    put("@type", "ContactPoint")
                    put("contactType", "recruiting")
                    put("email", ContactDetails.EMAIL)
                    put("telephone", ContactDetails.PHONE_DISPLAY)
                    put("availableLanguage", "English")
                }
                put("knowsLanguage", "English")
            }
            addJsonObject {
                put("@type", "WebSite")
                put("@id", websiteId)
                put("name", PortfolioSeo.SITE_NAME)
                put("url", homepageUrl)
                put("description", PortfolioSeo.META_DESCRIPTION)
                put("inLanguage", PortfolioSeo.LANGUAGE_CODE)
                putJsonObject("publisher") {
                    put("@id", personId)
                }
                putJsonObject("author") {
                    put("@id", personId)
                }
            }
            addJsonObject {
                put("@type", "ProfilePage")
                put("@id", profilePageId)
                put("url", homepageUrl)
                put("name", PortfolioSeo.PAGE_TITLE)
                put("description", PortfolioSeo.META_DESCRIPTION)
                put("inLanguage", PortfolioSeo.LANGUAGE_CODE)
                putJsonObject("isPartOf") {
                    put("@id", websiteId)
                }
                putJsonObject("mainEntity") {
                    put("@id", personId)
                }
                putJsonObject("about") {
                    put("@id", personId)
                }
                putJsonObject("primaryImageOfPage") {
                    put("@type", "ImageObject")
                    put("url", PortfolioSeo.ogImageUrl())
                    put("width", PortfolioSeo.OG_IMAGE_WIDTH)
                    put("height", PortfolioSeo.OG_IMAGE_HEIGHT)
                }
                putJsonObject("breadcrumb") {
                    put("@id", breadcrumbId)
                }
            }
            addJsonObject {
                put("@type", "BreadcrumbList")
                put("@id", breadcrumbId)
                putJsonArray("itemListElement") {
                    addJsonObject {
                        put("@type", "ListItem")
                        put("position", 1)
                        put("name", "Home")
                        put("item", homepageUrl)
                    }
                }
            }
            addJsonObject {
                put("@type", "ItemList")
                put("@id", agentListId)
                put("name", "Custom marketing agents")
                put("description", "Live agents Anudeep built and uses in marketing work.")
                put("numberOfItems", AgentRosterEntries.agents.size)
                putJsonArray("itemListElement") {
                    AgentRosterEntries.agents.forEachIndexed { index, agent ->
                        addJsonObject {
                            put("@type", "ListItem")
                            put("position", index + 1)
                            put("name", agent.displayName)
                            put("description", agent.shortDescription)
                            put("url", "$homepageUrl#agents")
                        }
                    }
                }
            }
        }
    }
    return jsonLdJson.encodeToString(graph)
}
