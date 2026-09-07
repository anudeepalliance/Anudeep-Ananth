package knowledge

/**
 * Campaign / case-study corpus agents can retrieve beyond the resume bullets.
 */
object CaseStudyCorpusEntries {
    val entries = listOf(
        CaseStudyEntry(
            caseStudyId = "digitalocean-aiml-lifecycle",
            title = "DigitalOcean — AI/ML portfolio lifecycle engagement",
            companyName = "DigitalOcean",
            roleTitle = "Lifecycle Marketing Manager",
            problemStatement =
                "AI/ML product users needed clearer journeys across email, in-app, and community " +
                    "touchpoints to deepen engagement and retention.",
            whatAnudeepDid =
                "Built data-informed lifecycle campaigns and content curation for the AI/ML portfolio; " +
                    "partnered with Product on local product-market fit; grew moderator capacity and UGC loops.",
            outcomes = listOf(
                "Customer engagement +25%",
                "Onboarding conversion +30%",
                "Community moderators +40%",
                "User-generated content +50%",
                "Daily active users +18%",
                "Retention +15% in key US markets",
            ),
            skillsDemonstrated = listOf(
                "Lifecycle marketing",
                "Cross-functional collaboration",
                "Community operations",
                "Experimentation",
                "Retention",
            ),
        ),
        CaseStudyEntry(
            caseStudyId = "elucidata-community-demand-gen",
            title = "Elucidata — community-led demand gen for biotech/AI",
            companyName = "Elucidata",
            roleTitle = "Marketing Manager (Demand Generation)",
            problemStatement =
                "Needed efficient qualified pipeline and category presence without scaling paid CAC linearly.",
            whatAnudeepDid =
                "Stood up industry-specific online communities, partnership motions with biotech influencers, " +
                    "and analytics-informed acquisition loops using SQL and product feedback.",
            outcomes = listOf(
                "CPL −40% in 3 months",
                "15 communities → 100,000 members in year one",
                "Qualified leads +22%",
                "Acquisition velocity +15%",
                "50+ partnerships → new user acquisition +30%",
                "User satisfaction signal +20% from behavior-informed product input",
            ),
            skillsDemonstrated = listOf(
                "Demand generation",
                "Community-led growth",
                "Partnerships",
                "SQL / analytics",
                "Lead quality",
            ),
        ),
        CaseStudyEntry(
            caseStudyId = "jiva-hubspot-community-pipeline",
            title = "Jiva Infotech — HubSpot-automated community pipeline",
            companyName = "Jiva Infotech",
            roleTitle = "Assistant Marketing Manager (Demand Generation)",
            problemStatement =
                "Local community energy was high but onboarding and participation were inconsistent, " +
                    "limiting pipeline conversion.",
            whatAnudeepDid =
                "Ran multichannel community programs, automated onboarding in HubSpot, and partnered with " +
                    "moderators on content systems across local chapters.",
            outcomes = listOf(
                "30+ active local communities",
                "\$1.2M new business growth within 18 months",
                "Active participation +35% per month via HubSpot journey automation",
                "Wealthtech in-app community engagement +10% with 38% retention",
                "Organic growth +25% in 6 months",
            ),
            skillsDemonstrated = listOf(
                "HubSpot lifecycle automation",
                "Community-led pipeline",
                "Local growth",
                "Retention programs",
            ),
        ),
        CaseStudyEntry(
            caseStudyId = "telebu-lucid-pos-community-revenue",
            title = "Telebu / Lucid POS — community as a revenue engine",
            companyName = "Telebu Communications",
            roleTitle = "Digital Marketing Manager (Demand Generation)",
            problemStatement =
                "Needed scalable acquisition and conversion for Lucid POS without only buying more ads.",
            whatAnudeepDid =
                "Built and nurtured a large online community, ran influencer collaborations, and tested " +
                    "ecosystem growth plays to lower acquisition cost while lifting conversion.",
            outcomes = listOf(
                "Community 50,000+ members",
                "Revenue growth +300% via community-led initiatives",
                "Organic reach +200%",
                "User-generated content +50%",
                "User acquisition cost −40%",
                "Conversions +30%",
            ),
            skillsDemonstrated = listOf(
                "Community-led revenue",
                "Influencer partnerships",
                "Acquisition efficiency",
                "Conversion optimization",
            ),
        ),
    )

    fun formatAsAgentContextText(): String {
        return entries.joinToString(separator = "\n\n") { caseStudy ->
            buildString {
                appendLine("CASE STUDY: ${caseStudy.title}")
                appendLine("ID: ${caseStudy.caseStudyId}")
                appendLine("Company/role: ${caseStudy.companyName} — ${caseStudy.roleTitle}")
                appendLine("Problem: ${caseStudy.problemStatement}")
                appendLine("What Anudeep did: ${caseStudy.whatAnudeepDid}")
                appendLine("Outcomes:")
                caseStudy.outcomes.forEach { outcome ->
                    appendLine("- $outcome")
                }
                appendLine("Skills: ${caseStudy.skillsDemonstrated.joinToString(", ")}")
            }
        }
    }
}

data class CaseStudyEntry(
    val caseStudyId: String,
    val title: String,
    val companyName: String,
    val roleTitle: String,
    val problemStatement: String,
    val whatAnudeepDid: String,
    val outcomes: List<String>,
    val skillsDemonstrated: List<String>,
)
