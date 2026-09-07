package knowledge

/**
 * Single entry point for page rendering and agents to load Anudeep's grounded content.
 */
object AnudeepKnowledgePack {
    val profile = AnudeepProfile
    val salesPitch = HomepageSalesPitch
    val timelineEntries = ExperienceTimelineEntries.entries
    val achievementHighlights = AchievementHighlights.highlights
    val caseStudyEntries = CaseStudyCorpusEntries.entries
    val jobMatchingRules = JobMatchingTargetingRules
    val contactDetails = ContactDetails
    val agentRoster = AgentRosterEntries.agents

    /**
     * Full text blob for LLM grounding (profile + rules + case studies + timeline).
     */
    fun formatFullAgentGroundingText(): String {
        return buildString {
            appendLine("=== PROFILE ===")
            appendLine(AnudeepProfile.formatAsAgentContextText())
            appendLine()
            appendLine("=== JOB MATCHING RULES ===")
            appendLine(JobMatchingTargetingRules.formatAsAgentRulesText())
            appendLine()
            appendLine("=== CASE STUDIES ===")
            appendLine(CaseStudyCorpusEntries.formatAsAgentContextText())
            appendLine()
            appendLine("=== TIMELINE ===")
            ExperienceTimelineEntries.entries.forEach { timelineEntry ->
                appendLine("${timelineEntry.dateRangeLabel} | ${timelineEntry.roleTitle} @ ${timelineEntry.companyName}")
                timelineEntry.summaryBullets.forEach { bullet ->
                    appendLine("  - $bullet")
                }
            }
            appendLine()
            appendLine("=== CONTACT ===")
            appendLine("Email: ${ContactDetails.EMAIL}")
            appendLine("LinkedIn: ${ContactDetails.LINKEDIN_URL}")
            appendLine("Phone: ${ContactDetails.PHONE_DISPLAY}")
        }
    }
}
