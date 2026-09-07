package knowledge

/**
 * Public agent roster for the Agents section (portraits wired in Phase 3).
 */
object AgentRosterEntries {
    val agents = listOf(
        AgentRosterEntry(
            agentId = "job_matching",
            displayName = "Job Matching",
            shortDescription = "Paste a JD. Get a fit score, gaps, and talking points against Anudeep's real profile.",
            isPrimary = true,
        ),
        AgentRosterEntry(
            agentId = "performance_marketing",
            displayName = "Performance Marketing",
            shortDescription = "Paid acquisition, ROAS, channel mix, and experiment thinking.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "market_research",
            displayName = "Market Research",
            shortDescription = "Category scans, competitor reads, and research briefs.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "prospecting_abm",
            displayName = "Prospecting (ABM)",
            shortDescription = "Account lists, personalization angles, and ABM motions.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "lead_scoring",
            displayName = "Lead Scoring",
            shortDescription = "Signal design, scoring logic, and pipeline quality tradeoffs.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "social_media_marketing",
            displayName = "Social Media Marketing",
            shortDescription = "Organic/social systems, content loops, and community lift.",
            isPrimary = false,
        ),
        AgentRosterEntry(
            agentId = "product_analytics",
            displayName = "Product Analytics",
            shortDescription = "Funnel reads, retention signals, and lifecycle metrics.",
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
