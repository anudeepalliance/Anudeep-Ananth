package web.seo

import knowledge.ContactDetails
import kotlin.test.Test
import kotlin.test.assertTrue

class LlmTxtRoutesTest {

    @Test
    fun `llms txt overview names anudeep and job matching`() {
        val overviewText = buildLlmsTxtOverview()
        assertTrue(overviewText.contains("# ${ContactDetails.FULL_NAME}"))
        assertTrue(overviewText.contains("Job Matching") || overviewText.contains("published experience"))
        assertTrue(overviewText.contains(ContactDetails.EMAIL))
        assertTrue(overviewText.contains("/llms-full.txt"))
        assertTrue(overviewText.contains("DigitalOcean"))
        assertTrue(overviewText.contains("Firecrawl"))
        assertTrue(overviewText.contains("https://www.jetbrains.com/koog/"))
        assertTrue(overviewText.contains("−40% CPL") || overviewText.contains("-40% CPL"))
        assertTrue(overviewText.contains("Experience (newest first)"))
        assertTrue(overviewText.contains("Remote from India"))
    }

    @Test
    fun `llms full txt includes case studies and targeting rules`() {
        val fullText = buildLlmsFullTxt()
        assertTrue(fullText.contains("CASE STUDY") || fullText.contains("Case studies"))
        assertTrue(fullText.contains("HARD SKIP") || fullText.contains("Hard skips"))
        assertTrue(fullText.contains("DigitalOcean"))
        assertTrue(fullText.contains("Elucidata"))
        assertTrue(fullText.contains("=== PROFILE ==="))
    }
}
