package knowledge

/**
 * Scannable achievement chips/cards for the Achievements section and agent grounding.
 */
object AchievementHighlights {
    val highlights = listOf(
        AchievementHighlight(
            metricLabel = "−40% CPL",
            contextLabel = "Elucidata demand gen in 3 months",
            category = "Demand gen",
            proofImagePath = "/static/images/proof/elucidata-cpl.jpg",
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
            proofImagePath = "/static/images/proof/honeycomb-revenue.jpg",
            proofExternalUrl = "https://www.facebook.com/photo.php?fbid=10215222361291409&set=a.2069073445506&type=3&theater",
        ),
        AchievementHighlight(
            metricLabel = "Amazon Star Award",
            contextLabel = "Employee of the Month — Feb 2014, signed by Karthik Mitta",
            category = "Recognition",
            proofImagePath = "/static/images/proof/amazon-star-award.jpg",
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
    val proofImagePath: String? = null,
    val proofExternalUrl: String? = null,
)
