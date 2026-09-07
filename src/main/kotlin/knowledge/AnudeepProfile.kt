package knowledge

/**
 * Structured candidate profile agents and the page use as grounding (from `Anudeep Resume.pdf`).
 */
object AnudeepProfile {
    const val FULL_NAME = ContactDetails.FULL_NAME
    const val LOCATION_SUMMARY = "India (open to remote)"
    const val YEARS_EXPERIENCE_SUMMARY = "13+ years in SaaS marketing, community, demand gen, and lifecycle"

    const val CAREER_SUMMARY =
        "Growth-driven lifecycle marketing leader with 13+ years of experience in SaaS marketing, " +
            "specializing in customer journey optimization, retention strategies, and revenue growth. " +
            "Currently Lifecycle Marketing Manager at DigitalOcean, working on customer journeys across " +
            "email, in-app, and community touchpoints. Proven expertise in A/B testing, " +
            "customer segmentation, and cross-functional collaboration with Product, Sales, and Customer Success."

    val skillGroups = listOf(
        SkillGroup(
            groupName = "AI & Automation",
            skills = listOf(
                "AI agents (custom-coded Python & n8n)",
                "n8n automation",
                "Prompt engineering",
                "AI workflows (OpenAI, Claude, Gemini, DeepSeek)",
                "Content automation",
                "Google AI Studio",
                "Image prompt engineering (MidJourney, Google Imagen, Nano Banana, SeeDream)",
                "WhatsApp Business API",
            ),
        ),
        SkillGroup(
            groupName = "Technical",
            skills = listOf(
                "Website development (Deno, Wix)",
                "Docker",
                "Google Cloud",
                "DigitalOcean Cloud",
                "Data analytics",
                "SQL",
                "Google Analytics",
                "Google Looker",
            ),
        ),
        SkillGroup(
            groupName = "Marketing & Growth",
            skills = listOf(
                "Growth hacking / growth marketing",
                "User acquisition & retention",
                "A/B testing & experimentation",
                "Content strategy & curation",
                "Market analysis & trend identification",
                "Product-market fit optimization",
                "Localization strategies",
                "Lifecycle / journey marketing",
                "Demand generation",
                "Community-led growth",
            ),
        ),
        SkillGroup(
            groupName = "Platforms & Tools",
            skills = listOf(
                "HubSpot",
                "Marketo",
                "Salesforce",
                "Canva",
                "Amplitude",
                "Brevo",
            ),
        ),
        SkillGroup(
            groupName = "Leadership",
            skills = listOf(
                "Community building & nurturing",
                "Cross-functional leadership",
                "Partnerships & influencer relations",
                "Performance measurement & reporting",
                "Cultural sensitivity",
                "Entrepreneurial mindset",
            ),
        ),
    )

    val certifications = listOf(
        "Google Ads",
        "Email Marketing",
        "Web Analytics",
        "HTML & CSS",
        "Amazon — Employee Award",
        "Facebook Marketing",
        "Inbound Marketing",
        "Search Engine Marketing",
        "Google Ads Display",
        "Google Ads Mobile",
        "Google Ads Video",
    )

    fun formatAsAgentContextText(): String {
        return buildString {
            appendLine("NAME: $FULL_NAME")
            appendLine("LOCATION: $LOCATION_SUMMARY")
            appendLine("EXPERIENCE: $YEARS_EXPERIENCE_SUMMARY")
            appendLine()
            appendLine("CAREER SUMMARY:")
            appendLine(CAREER_SUMMARY)
            appendLine()
            skillGroups.forEach { skillGroup ->
                appendLine("${skillGroup.groupName.uppercase()}:")
                skillGroup.skills.forEach { skill ->
                    appendLine("- $skill")
                }
                appendLine()
            }
            appendLine("CERTIFICATIONS:")
            certifications.forEach { certification ->
                appendLine("- $certification")
            }
        }
    }
}

data class SkillGroup(
    val groupName: String,
    val skills: List<String>,
)
