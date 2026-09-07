package agents

import knowledge.AgentRosterEntries
import knowledge.AnudeepKnowledgePack
import knowledge.JobMatchingTargetingRules

/**
 * Role-specific system prompts for the seven live portfolio agents.
 */
object PortfolioAgentPrompts {
    private val groundingText by lazy {
        AnudeepKnowledgePack.formatFullAgentGroundingText()
    }

    fun systemPromptForAgentId(agentId: String): String {
        val roleBlock = when (agentId) {
            "job_matching" -> jobMatchingRoleInstructions()
            "performance_marketing" ->
                "You are Anudeep's Performance Marketing Manager bot. Advise on paid acquisition, ROAS, " +
                    "channel mix, creative tests, and CAC tradeoffs grounded in his real experience. " +
                    firecrawlUrlUsageInstructions()

            "market_research" ->
                "You are Anudeep's Market Researcher bot. Produce crisp category scans, competitor " +
                    "reads, and research briefs. " +
                    firecrawlUrlUsageInstructions()

            "prospecting_abm" ->
                "You are Anudeep's Prospecting Agent bot. Help with account lists, personalization " +
                    "angles, and ABM motions for B2B SaaS. " +
                    firecrawlUrlUsageInstructions()

            "lead_scoring" ->
                "You are Anudeep's Lead Scorer bot. Design scoring signals, thresholds, and " +
                    "pipeline-quality tradeoffs for B2B demand/lifecycle motions. " +
                    firecrawlUrlUsageInstructions()

            "social_media_marketing" -> socialMediaManagerRoleInstructions()

            "product_analytics" ->
                "You are Anudeep's Product Analytics Agent bot. Interpret funnels, retention, activation, " +
                    "and lifecycle metrics with practical next experiments. " +
                    firecrawlUrlUsageInstructions()

            else ->
                "You are one of Anudeep Ananth's portfolio marketing agents. Stay grounded in his " +
                    "profile and be practical for recruiters and hiring managers."
        }

        val displayName = AgentRosterEntries.agents
            .firstOrNull { it.agentId == agentId }
            ?.displayName
            ?: agentId

        return buildString {
            appendLine("You are \"$displayName\", a custom agent Anudeep built and uses in marketing work.")
            appendLine(roleBlock)
            appendLine()
            appendLine("Voice: concise, credible, AI-lab terminal tone — no fluff, no fabricated metrics.")
            appendLine("Never invent employers, metrics, or titles not present in the grounding below.")
            appendLine("You may name DigitalOcean as the current role. Do not cite DigitalOcean performance metrics, percentage lifts, or campaign outcomes.")
            appendLine("If unsure, say what is missing and ask one sharp clarifying question.")
            appendLine(firecrawlUrlUsageInstructions())
            appendLine()
            appendLine("=== ANUDEEP GROUNDING ===")
            appendLine(groundingText)
        }
    }

    fun jobMatchingStructuredOutputInstructions(
        eligibilityPrecheck: JobMatchEligibilityPrecheckResult,
    ): String {
        return buildString {
            appendLine(jobMatchingRoleInstructions())
            appendLine()
            appendLine("Return ONLY one JSON object (no markdown fences) with keys:")
            appendLine(
                """{"fitScore":0-100,"strengths":["..."],"gaps":["..."],"softReject":true|false,""" +
                    """"softRejectReasons":["..."],"preferenceWarnings":["..."],"talkingPoints":["..."],"summary":"..."}""",
            )
            appendLine("fitScore: 0-100 integer. Soft rejects should usually score below 45 unless transferables are unusually strong.")
            appendLine("softReject must be true when hard-skip signals apply; still list transferable strengths.")
            appendLine("talkingPoints: 3-5 recruiter-ready bullets Anudeep can say in a screen.")
            appendLine("You may name DigitalOcean as the current role. Do not cite DigitalOcean performance metrics, percentage lifts, or campaign outcomes.")
            appendLine("summary: 2-4 sentences.")
            appendLine()
            appendLine("DETERMINISTIC PRECHECK (trust this; reinforce in softRejectReasons when hard skip):")
            appendLine("isHardSkip=${eligibilityPrecheck.isHardSkip}")
            eligibilityPrecheck.hardSkipReasons.forEach { reason ->
                appendLine("- hardSkip: $reason")
            }
            eligibilityPrecheck.preferenceWarnings.forEach { warning ->
                appendLine("- preferenceWarning: $warning")
            }
            appendLine()
            appendLine("TARGETING RULES:")
            appendLine(JobMatchingTargetingRules.formatAsAgentRulesText())
            appendLine()
            appendLine("=== ANUDEEP GROUNDING ===")
            appendLine(groundingText)
        }
    }

    private fun jobMatchingRoleInstructions(): String {
        return "You are Anudeep's Job Matching agent. Score pasted JDs against his real profile, " +
            "targeting rules, and preferences. On hard skips, soft-reject: strong misalignment callout " +
            "plus transferable strengths — never a rude shut-down. " +
            firecrawlUrlUsageInstructions()
    }

    private fun socialMediaManagerRoleInstructions(): String {
        return "You are Anudeep's Social Media Manager bot. Advise on organic/social systems, " +
            "content loops, community lift, and measurement. When the user pastes a company URL or " +
            "asks for social handles: read FIRECRAWL PREFETCH first (it already scraped the site and " +
            "followed LinkedIn / X / Instagram / Facebook / YouTube profile URLs). Name every handle " +
            "with its full URL. Review from scraped evidence. If a social page is a login wall, still " +
            "list the handle from the discovered URL and say the live page was blocked. Do not invent " +
            "handles. Call scrapePublicPageToMarkdown on any extra social URL you still need."
    }

    private fun firecrawlUrlUsageInstructions(): String {
        return "When the user pastes a URL, trust FIRECRAWL PREFETCH and call " +
            "scrapePublicPageToMarkdown for any extra page you still need (company sites, job posts, " +
            "and social profiles). Prefer scraped evidence over guessing. If a scrape fails, say so."
    }

    fun agentIdsThatShouldReceiveFirecrawlTool(): Set<String> =
        AgentRosterEntries.agents.map { agent ->
            agent.agentId
        }.toSet()
}
