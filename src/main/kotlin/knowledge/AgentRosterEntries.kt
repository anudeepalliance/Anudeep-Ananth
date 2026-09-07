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
            isPrimary = true,
        ),
        AgentRosterEntry(
            agentId = "performance_marketing",
            displayName = "Performance Marketing Manager",
            shortDescription = "I use this for paid acquisition, ROAS, channel mix, and tests.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "market_research",
            displayName = "Market Researcher",
            shortDescription = "I use this for category scans, competitor reads, and research briefs.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "prospecting_abm",
            displayName = "Prospecting Agent",
            shortDescription = "I use this for account lists, personalization, and ABM motions.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "lead_scoring",
            displayName = "Lead Scorer",
            shortDescription = "I use this for scoring signals, thresholds, and pipeline quality.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "social_media_marketing",
            displayName = "Social Media Manager",
            shortDescription = "I use this for organic systems, content loops, and community lift.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "product_analytics",
            displayName = "Product Analytics Agent",
            shortDescription = "I use this for funnels, retention, and lifecycle metrics.",
            isPrimary = false,
        ),
    )
}

data class AgentRosterEntry(
    val agentId: String,
    val displayName: String,
    val shortDescription: String,
    val isPrimary: Boolean,
    val portraitImagePath: String = "/static/images/agents/${agentId}.jpg",
)
