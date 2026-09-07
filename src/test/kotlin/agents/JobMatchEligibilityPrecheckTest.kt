package agents

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JobMatchEligibilityPrecheckTest {

    @Test
    fun `hard-skip PMM JD is flagged soft-reject eligible`() {
        val jobDescriptionText = """
            Product Marketing Manager (PMM)
            Own messaging and launch narratives for our platform.
            Hybrid role based in San Francisco. 2-5 years experience.
        """.trimIndent()

        val result = JobMatchEligibilityPrecheck.evaluateJobDescriptionText(jobDescriptionText)

        assertTrue(result.isHardSkip)
        assertTrue(result.hardSkipReasons.any { it.contains("Product Marketing", ignoreCase = true) })
        assertTrue(
            result.matchedHardSkipSignalLabels.any { it.contains("PMM", ignoreCase = true) },
        )
    }

    @Test
    fun `hard-skip CRM manager onsite JD is flagged`() {
        val jobDescriptionText = """
            CRM Manager
            Own the CRM and lifecycle automations onsite in London.
            UK-only candidates. Relocation required.
        """.trimIndent()

        val result = JobMatchEligibilityPrecheck.evaluateJobDescriptionText(jobDescriptionText)

        assertTrue(result.isHardSkip)
        assertTrue(result.hardSkipReasons.any { it.contains("CRM", ignoreCase = true) })
        assertTrue(result.hardSkipReasons.any { it.contains("Onsite", ignoreCase = true) || it.contains("geo", ignoreCase = true) })
    }

    @Test
    fun `in-scope remote demand gen JD is not a hard skip`() {
        val jobDescriptionText = """
            Senior Demand Generation Manager
            B2B SaaS growth team. Fully remote (LinkedIn Remote).
            Own pipeline, paid + lifecycle experiments, and RevOps marketing partnership.
            8+ years preferred. Mid-senior role reporting to Head of Growth.
        """.trimIndent()

        val result = JobMatchEligibilityPrecheck.evaluateJobDescriptionText(jobDescriptionText)

        assertFalse(result.isHardSkip)
        assertTrue(result.hardSkipReasons.isEmpty())
    }

    @Test
    fun `remote-eligible hybrid wording does not hard-skip on location alone`() {
        val jobDescriptionText = """
            Lifecycle Marketing Director
            Hybrid optional, but LinkedIn Remote / fully remote candidates welcome worldwide.
            B2B SaaS. Own lifecycle and demand programs.
        """.trimIndent()

        val result = JobMatchEligibilityPrecheck.evaluateJobDescriptionText(jobDescriptionText)

        assertFalse(
            result.hardSkipReasons.any { it.contains("Onsite", ignoreCase = true) || it.contains("geo", ignoreCase = true) },
        )
    }
}
