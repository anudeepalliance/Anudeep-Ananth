package knowledge

/**
 * Scannable achievement chips/cards for the Proof section and agent grounding.
 */
object AchievementHighlights {
    val highlights = listOf(
        AchievementHighlight(
            metricLabel = "+25% engagement",
            contextLabel = "DigitalOcean AI/ML portfolio ecosystem",
            category = "Lifecycle",
        ),
        AchievementHighlight(
            metricLabel = "+30% onboarding conversion",
            contextLabel = "DigitalOcean customer journey optimization",
            category = "Lifecycle",
        ),
        AchievementHighlight(
            metricLabel = "−40% CPL",
            contextLabel = "Elucidata demand gen in 3 months",
            category = "Demand gen",
        ),
        AchievementHighlight(
            metricLabel = "100K community members",
            contextLabel = "15 industry communities in year one (Elucidata)",
            category = "Community",
        ),
        AchievementHighlight(
            metricLabel = "\$1.2M new business",
            contextLabel = "Jiva Infotech community-led growth in 18 months",
            category = "Revenue",
        ),
        AchievementHighlight(
            metricLabel = "+300% revenue growth",
            contextLabel = "Telebu / Lucid POS community of 50K+",
            category = "Revenue",
        ),
        AchievementHighlight(
            metricLabel = "−40% acquisition cost",
            contextLabel = "Telebu ecosystem growth strategy",
            category = "Efficiency",
        ),
        AchievementHighlight(
            metricLabel = "₹1.25 Cr / ₹10 L",
            contextLabel = "Honeycomb social programs in 6 months",
            category = "ROAS-style efficiency",
        ),
        AchievementHighlight(
            metricLabel = "+15% retention",
            contextLabel = "DigitalOcean local PMF in key US markets",
            category = "Retention",
        ),
        AchievementHighlight(
            metricLabel = "AI agents + n8n",
            contextLabel = "Ships custom agents and automation for marketing workflows",
            category = "AI leverage",
        ),
    )
}

data class AchievementHighlight(
    val metricLabel: String,
    val contextLabel: String,
    val category: String,
)
