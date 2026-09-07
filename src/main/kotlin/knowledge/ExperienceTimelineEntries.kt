package knowledge

/**
 * Visual-ready experience milestones for the homepage timeline.
 */
object ExperienceTimelineEntries {
    val entries = listOf(
        TimelineEntry(
            companyName = "DigitalOcean",
            roleTitle = "Lifecycle Marketing Manager",
            dateRangeLabel = "Oct 2023 – Present",
            summaryBullets = listOf(
                "Lifecycle marketing for the AI/ML portfolio across email, in-app, and community",
                "Customer journeys, onboarding, and cross-functional work with Product",
                "Community operations and content loops",
            ),
        ),
        TimelineEntry(
            companyName = "Elucidata",
            roleTitle = "Marketing Manager (Demand Generation)",
            dateRangeLabel = "Jun 2022 – Oct 2023",
            summaryBullets = listOf(
                "Cost per lead −40% in 3 months",
                "Launched/grew 15 industry communities to 100,000 members in year one",
                "Qualified leads +22%; acquisition velocity +15%",
                "50+ biotech partnerships — new user acquisition +30%",
            ),
        ),
        TimelineEntry(
            companyName = "Jiva Infotech",
            roleTitle = "Assistant Marketing Manager (Demand Generation)",
            dateRangeLabel = "Apr 2020 – Jun 2022",
            summaryBullets = listOf(
                "30+ local communities; \$1.2M new business growth in 18 months",
                "HubSpot onboarding automation — active participation +35%/month",
                "Wealthtech in-app community — engagement +10%, 38% retention",
                "Moderator-led content — organic growth +25% in 6 months",
            ),
        ),
        TimelineEntry(
            companyName = "Telebu Communications",
            roleTitle = "Digital Marketing Manager (Demand Generation)",
            dateRangeLabel = "Jul 2019 – Apr 2020",
            summaryBullets = listOf(
                "Lucid POS community 50,000+ members; revenue growth +300%",
                "Influencer collaborations — organic reach +200%, UGC +50%",
                "Ecosystem growth — acquisition cost −40%, conversions +30%",
            ),
        ),
        TimelineEntry(
            companyName = "Honeycomb Creative Support",
            roleTitle = "Social Media Manager",
            dateRangeLabel = "Jan 2015 – Apr 2019",
            summaryBullets = listOf(
                "₹1.25 crore revenue on ₹10 lakh spend in 6 months",
                "Social sentiment program — +15% positive sentiment shift",
            ),
        ),
        TimelineEntry(
            companyName = "Amazon India",
            roleTitle = "Process Associate",
            dateRangeLabel = "Oct 2013 – Dec 2014",
            summaryBullets = listOf(
                "Employee of the Month — analytics framework improved audience tracking +20%",
            ),
        ),
        TimelineEntry(
            companyName = "Frankfinn Institute",
            roleTitle = "Assistant Marketing Manager",
            dateRangeLabel = "Jan 2013 – Oct 2013",
            summaryBullets = listOf(
                "SEO lead engine — exceeded sales targets +20%; top South India center",
                "Facebook community 50K followers — awareness/engagement +18%",
            ),
        ),
    )
}

data class TimelineEntry(
    val companyName: String,
    val roleTitle: String,
    val dateRangeLabel: String,
    val summaryBullets: List<String>,
)
