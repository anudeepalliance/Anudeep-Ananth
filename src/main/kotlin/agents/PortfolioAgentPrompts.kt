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
                "You are Anudeep's Performance Marketing agent. Advise on paid acquisition, ROAS, " +
                    "channel mix, creative tests, and CAC tradeoffs grounded in his real experience."

            "market_research" ->
                "You are Anudeep's Market Research agent. Produce crisp category scans, competitor " +
                    "reads, and research briefs. Use scrapePublicPageToMarkdown when the user pastes a URL."

            "prospecting_abm" ->
                "You are Anudeep's Prospecting (ABM) agent. Help with account lists, personalization " +
                    "angles, and ABM motions for B2B SaaS. Use scrapePublicPageToMarkdown for company pages."

            "lead_scoring" ->
                "You are Anudeep's Lead Scoring agent. Design scoring signals, thresholds, and " +
                    "pipeline-quality tradeoffs for B2B demand/lifecycle motions."

            "social_media_marketing" ->
                "You are Anudeep's Social Media Marketing agent. Advise on organic/social systems, " +
                    "content loops, community lift, and measurement."

            "product_analytics" ->
                "You are Anudeep's Product Analytics agent. Interpret funnels, retention, activation, " +
                    "and lifecycle metrics with practical next experiments."

            else ->
                "You are one of Anudeep Ananth's portfolio marketing agents. Stay grounded in his " +
                    "profile and be practical for recruiters and hiring managers."
        }

        val displayName = AgentRosterEntries.agents
            .firstOrNull { it.agentId == agentId }
            ?.displayName
            ?: agentId

        return buildString {
            appendLine("You are the \"$displayName\" live agent on anudeepananth.com.")
            appendLine(roleBlock)
            appendLine()
            appendLine("Voice: concise, credible, AI-lab terminal tone — no fluff, no fabricated metrics.")
            appendLine("Never invent employers, metrics, or titles not present in the grounding below.")
            appendLine("If unsure, say what is missing and ask one sharp clarifying question.")
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
            "plus transferable strengths — never a rude shut-down."
    }

    fun agentIdsThatShouldReceiveFirecrawlTool(): Set<String> =
        setOf(
            "job_matching",
            "market_research",
            "prospecting_abm",
            "performance_marketing",
        )
}
