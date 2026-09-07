package knowledge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnudeepKnowledgePackTest {

    @Test
    fun `knowledge pack exposes expected core content counts`() {
        assertEquals(7, AnudeepKnowledgePack.timelineEntries.size)
        assertEquals(4, AnudeepKnowledgePack.caseStudyEntries.size)
        assertTrue(AnudeepKnowledgePack.achievementHighlights.isNotEmpty())
        assertEquals("anudeeps3@gmail.com", AnudeepKnowledgePack.contactDetails.EMAIL)
        assertEquals("soft_reject", AnudeepKnowledgePack.jobMatchingRules.HARD_SKIP_BEHAVIOR)
    }

    @Test
    fun `full agent grounding text includes profile rules and case studies`() {
        val groundingText = AnudeepKnowledgePack.formatFullAgentGroundingText()
        assertTrue(groundingText.contains("DigitalOcean"))
        assertTrue(groundingText.contains("HARD SKIP"))
        assertTrue(groundingText.contains("digitalocean-aiml-lifecycle"))
        assertTrue(groundingText.contains(ContactDetails.LINKEDIN_URL))
    }
}
