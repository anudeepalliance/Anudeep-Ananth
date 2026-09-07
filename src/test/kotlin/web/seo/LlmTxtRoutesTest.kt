package web.seo

import knowledge.ContactDetails
import kotlin.test.Test
import kotlin.test.assertTrue

class LlmTxtRoutesTest {

    @Test
    fun `llms txt overview names anudeep and job matching`() {
        val overviewText = buildLlmsTxtOverview()
        assertTrue(overviewText.contains("# ${ContactDetails.FULL_NAME}"))
        assertTrue(overviewText.contains("Job Matching"))
        assertTrue(overviewText.contains(ContactDetails.EMAIL))
        assertTrue(overviewText.contains("/llms-full.txt"))
    }

    @Test
    fun `llms full txt includes case studies and targeting rules`() {
        val fullText = buildLlmsFullTxt()
        assertTrue(fullText.contains("CASE STUDY") || fullText.contains("Case studies"))
        assertTrue(fullText.contains("HARD SKIP") || fullText.contains("Hard skips"))
        assertTrue(fullText.contains("DigitalOcean"))
    }
}
