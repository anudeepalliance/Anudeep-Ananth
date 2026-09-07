package knowledge

/**
 * Public agent roster for the Agents section (portraits wired in Phase 3).
 */
object AgentRosterEntries {
    val agents = listOf(
        AgentRosterEntry(
            agentId = "job_matching",
            displayName = "Job Matching",
            shortDescription = "I use this to compare a job description to published experience.",
            inputHintText = "Paste the full job description here…",
            isPrimary = true,
        ),
        AgentRosterEntry(
            agentId = "performance_marketing",
            displayName = "Performance Marketing Manager",
            shortDescription = "I use this for paid acquisition, ROAS, channel mix, and tests.",
            inputHintText = "Ask about paid mix, ROAS, creative tests, or channel budget…",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "market_research",
            displayName = "Market Researcher",
            shortDescription = "I use this for category scans, competitor reads, and research briefs.",
            inputHintText = "Ask for a category scan, competitor read, or research brief…",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "prospecting_abm",
            displayName = "Prospecting Agent",
            shortDescription = "I use this for account lists, personalization, and ABM motions.",
            inputHintText = "Ask for an account list, a personalization angle, or an ABM motion…",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "lead_scoring",
            displayName = "Lead Scorer",
            shortDescription = "I use this for scoring signals, thresholds, and pipeline quality.",
            inputHintText = "Ask how to score a lead, set a threshold, or judge pipeline quality…",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "social_media_marketing",
            displayName = "Social Media Manager",
            shortDescription = "I use this for organic systems, content loops, and community lift.",
            inputHintText = "Ask about organic systems, content loops, or community lift…",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "product_analytics",
            displayName = "Product Analytics Agent",
            shortDescription = "I use this for funnels, retention, and lifecycle metrics.",
            inputHintText = "Ask about a funnel, retention, or a lifecycle metric…",
            isPrimary = false,
        ),
    )
}

data class AgentRosterEntry(
    val agentId: String,
    val displayName: String,
    val shortDescription: String,
    val inputHintText: String,
    val isPrimary: Boolean,
    val portraitImagePath: String = "/static/images/agents/${agentId}.jpg",
)
