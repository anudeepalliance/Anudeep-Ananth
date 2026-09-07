package agents.firecrawl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicSocialProfileUrlsTest {

    @Test
    fun `extracts linkedin twitter and instagram profiles from firecrawl links`() {
        val socialProfileUrls = PublicSocialProfileUrls.extractFromMarkdownAndLinks(
            markdown = "AI vehicle inspection. [Home](https://inspektlabs.com/)",
            discoveredLinks = listOf(
                "https://inspektlabs.com/about-us",
                "https://www.linkedin.com/company/inspektlabs",
                "https://twitter.com/InspektlabsAI",
                "https://www.instagram.com/inspektlabs",
                "https://www.linkedin.com/shareArticle?url=https://inspektlabs.com/",
            ),
        )

        assertEquals(
            listOf(
                "https://linkedin.com/company/inspektlabs",
                "https://twitter.com/InspektlabsAI",
                "https://instagram.com/inspektlabs",
            ),
            socialProfileUrls,
        )
    }

    @Test
    fun `extracts social profiles from markdown when the links list is empty`() {
        val socialProfileUrls = PublicSocialProfileUrls.extractFromMarkdownAndLinks(
            markdown = "Follow us on [LinkedIn](https://www.linkedin.com/company/inspektlabs) and " +
                "https://twitter.com/InspektlabsAI.",
            discoveredLinks = emptyList(),
        )

        assertTrue(socialProfileUrls.contains("https://linkedin.com/company/inspektlabs"))
        assertTrue(socialProfileUrls.contains("https://twitter.com/InspektlabsAI"))
    }

    @Test
    fun `drops share login and job board urls`() {
        assertFalse(PublicSocialProfileUrls.isPublicSocialProfileUrl("https://linkedin.com/shareArticle"))
        assertFalse(PublicSocialProfileUrls.isPublicSocialProfileUrl("https://linkedin.com/jobs/view/123"))
        assertFalse(PublicSocialProfileUrls.isPublicSocialProfileUrl("https://twitter.com/intent/tweet"))
        assertFalse(PublicSocialProfileUrls.isPublicSocialProfileUrl("https://youtube.com/watch?v=abc"))
        assertTrue(PublicSocialProfileUrls.isPublicSocialProfileUrl("https://linkedin.com/in/anudeep-ananth"))
        assertTrue(PublicSocialProfileUrls.isPublicSocialProfileUrl("https://youtube.com/@inspektlabs"))
    }
}
