package agents

import agents.firecrawl.FirecrawlPageScrapeResult
import agents.firecrawl.FirecrawlPageScraper
import ai.koog.agents.core.tools.ToolRegistry
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PortfolioAgentRuntimeTest {

    @Test
    fun `soft-reject hard-skip JD forces softReject even if model scores high`() = runBlocking {
        val mockLlmTextRunner = PortfolioLlmTextRunner { _, _, _ ->
            """
            {
              "fitScore": 88,
              "strengths": ["Lifecycle depth", "AI agent shipping"],
              "gaps": ["Pure PMM messaging craft"],
              "softReject": false,
              "softRejectReasons": [],
              "preferenceWarnings": [],
              "talkingPoints": ["I can still transfer demand gen systems thinking"],
              "summary": "Model incorrectly treated this as a strong fit."
            }
            """.trimIndent()
        }

        val runtime = PortfolioAgentRuntime(
            llmTextRunner = mockLlmTextRunner,
            firecrawlScrapeClient = agents.firecrawl.FirecrawlScrapeClient(
                apiKey = "test-unused",
            ),
            firecrawlToolRegistry = ToolRegistry {},
        )

        val outcome = runtime.runJobMatchingFitAnalysis(
            jobDescriptionText = """
                Product Marketing Manager (PMM)
                Hybrid onsite in New York. 1-5 years experience.
            """.trimIndent(),
        )

        val success = assertIs<JobMatchFitAnalysisOutcome.Success>(outcome)
        assertTrue(success.eligibilityPrecheck.isHardSkip)
        assertTrue(success.fitAnalysis.softReject)
        assertTrue(success.fitAnalysis.fitScore <= 42)
        assertTrue(success.fitAnalysis.softRejectReasons.isNotEmpty())
        assertTrue(success.fitAnalysis.strengths.isNotEmpty())
    }

    @Test
    fun `happy-path in-scope JD keeps softReject false and preserves score`() = runBlocking {
        val mockLlmTextRunner = PortfolioLlmTextRunner { _, _, _ ->
            """
            {
              "fitScore": 84,
              "strengths": ["Demand gen ownership", "Lifecycle systems", "B2B SaaS"],
              "gaps": ["Would clarify paid budget ownership"],
              "softReject": false,
              "softRejectReasons": [],
              "preferenceWarnings": [],
              "talkingPoints": ["DigitalOcean lifecycle outcomes", "AI agent leverage"],
              "summary": "Strong mid-senior demand gen fit for remote B2B SaaS."
            }
            """.trimIndent()
        }

        val runtime = PortfolioAgentRuntime(
            llmTextRunner = mockLlmTextRunner,
            firecrawlToolRegistry = ToolRegistry {},
        )

        val outcome = runtime.runJobMatchingFitAnalysis(
            jobDescriptionText = """
                Senior Demand Generation Manager
                Fully remote B2B SaaS role. Own pipeline and growth experiments.
                Mid-senior seat reporting to Head of Growth.
            """.trimIndent(),
        )

        val success = assertIs<JobMatchFitAnalysisOutcome.Success>(outcome)
        assertFalse(success.eligibilityPrecheck.isHardSkip)
        assertFalse(success.fitAnalysis.softReject)
        assertEquals(84, success.fitAnalysis.fitScore)
        assertTrue(success.fitAnalysis.strengths.size >= 2)
        assertTrue(success.fitAnalysis.talkingPoints.isNotEmpty())
    }

    @Test
    fun `blank JD returns failure without calling LLM`() = runBlocking {
        var llmCallCount = 0
        val runtime = PortfolioAgentRuntime(
            llmTextRunner = PortfolioLlmTextRunner { _, _, _ ->
                llmCallCount += 1
                error("should not be called")
            },
            firecrawlToolRegistry = ToolRegistry {},
        )

        val outcome = runtime.runJobMatchingFitAnalysis(jobDescriptionText = "   ")
        assertIs<JobMatchFitAnalysisOutcome.Failure>(outcome)
        assertEquals(0, llmCallCount)
    }

    @Test
    fun `agent chat routes to requested agent id`() = runBlocking {
        var capturedSystemPrompt = ""
        val runtime = PortfolioAgentRuntime(
            llmTextRunner = PortfolioLlmTextRunner { systemPrompt, _, _ ->
                capturedSystemPrompt = systemPrompt
                "Channel mix sketch ready."
            },
            firecrawlToolRegistry = ToolRegistry {},
        )

        val outcome = runtime.runAgentChatTurn(
            agentId = "performance_marketing",
            userMessageText = "Sketch a paid search + LinkedIn test plan.",
        )

        val success = assertIs<AgentChatTurnOutcome.Success>(outcome)
        assertEquals("performance_marketing", success.agentId)
        assertTrue(capturedSystemPrompt.contains("Performance Marketing"))
        assertTrue(success.replyText.contains("Channel mix"))
    }

    @Test
    fun `chat prefetch scrapes pasted url then follows discovered social profiles`() = runBlocking {
        val scrapedPageUrls = mutableListOf<String>()
        val homepageUrl = "https://inspektlabs.com"
        val linkedInUrl = "https://linkedin.com/company/inspektlabs"
        val twitterUrl = "https://twitter.com/InspektlabsAI"
        val recordingScraper = FirecrawlPageScraper { pageUrl ->
            scrapedPageUrls += pageUrl
            if (pageUrl == homepageUrl) {
                FirecrawlPageScrapeResult(
                    pageUrl = pageUrl,
                    sourceUrl = "https://inspektlabs.com/",
                    title = "Inspektlabs",
                    markdown = "AI vehicle inspection software.",
                    discoveredLinks = listOf(
                        "https://inspektlabs.com/about-us",
                        "https://www.linkedin.com/company/inspektlabs",
                        "https://twitter.com/InspektlabsAI",
                    ),
                )
            } else {
                FirecrawlPageScrapeResult(
                    pageUrl = pageUrl,
                    sourceUrl = pageUrl,
                    title = "Social profile",
                    markdown = "Profile body for $pageUrl",
                    discoveredLinks = emptyList(),
                )
            }
        }
        var capturedUserPrompt = ""
        var capturedToolRegistry: ToolRegistry? = null
        val runtime = PortfolioAgentRuntime(
            llmTextRunner = PortfolioLlmTextRunner { _, userPrompt, toolRegistry ->
                capturedUserPrompt = userPrompt
                capturedToolRegistry = toolRegistry
                "LinkedIn: https://www.linkedin.com/company/inspektlabs"
            },
            firecrawlScrapeClient = recordingScraper,
            firecrawlToolRegistry = ToolRegistry {},
        )

        val outcome = runtime.runAgentChatTurn(
            agentId = "social_media_marketing",
            userMessageText = "Check Social media handles of https://inspektlabs.com/ and give me a review",
        )

        assertIs<AgentChatTurnOutcome.Success>(outcome)
        assertTrue(scrapedPageUrls.contains(homepageUrl))
        assertTrue(scrapedPageUrls.contains(linkedInUrl))
        assertTrue(scrapedPageUrls.contains(twitterUrl))
        assertTrue(capturedUserPrompt.contains("FIRECRAWL PREFETCH"))
        assertTrue(capturedUserPrompt.contains(linkedInUrl))
        assertTrue(capturedUserPrompt.contains("Discovered social profiles"))
        assertTrue(capturedToolRegistry != null)
    }
}
