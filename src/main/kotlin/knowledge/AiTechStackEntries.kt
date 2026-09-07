package knowledge

/**
 * Public AI tools used to build this site and the live agents.
 */
object AiTechStackEntries {
    val entries = listOf(
        AiTechStackEntry(
            displayName = "Koog",
            useLabel = "Custom AI agents",
            siteUrl = "https://www.jetbrains.com/koog/",
            logoImagePath = "/static/images/stack/koog.png",
        ),
        AiTechStackEntry(
            displayName = "Seedance 2.5",
            useLabel = "Videos",
            siteUrl = "https://seed.bytedance.com/en/seedance2_5",
            logoImagePath = "/static/images/stack/seedance.png",
        ),
        AiTechStackEntry(
            displayName = "Nano Banana Pro via Higgsfield",
            useLabel = "Images",
            siteUrl = "https://higgsfield.ai",
            logoImagePath = "/static/images/stack/higgsfield.png",
        ),
        AiTechStackEntry(
            displayName = "MiniMax 3.0",
            useLabel = "The agent brain",
            siteUrl = "https://www.minimax.io",
            logoImagePath = "/static/images/stack/minimax.png",
        ),
        AiTechStackEntry(
            displayName = "n8n",
            useLabel = "Cron jobs",
            siteUrl = "https://n8n.io",
            logoImagePath = "/static/images/stack/n8n.svg",
        ),
        AiTechStackEntry(
            displayName = "ElevenLabs",
            useLabel = "Voice",
            siteUrl = "https://elevenlabs.io",
            logoImagePath = "/static/images/stack/elevenlabs.svg",
        ),
        AiTechStackEntry(
            displayName = "Sarvam API",
            useLabel = "Voice input",
            siteUrl = "https://www.sarvam.ai",
            logoImagePath = "/static/images/stack/sarvam.png",
        ),
        AiTechStackEntry(
            displayName = "Grok Build",
            useLabel = "Build custom internal apps",
            siteUrl = "https://x.ai",
            logoImagePath = "/static/images/stack/grok.png",
        ),
        AiTechStackEntry(
            displayName = "Firecrawl",
            useLabel = "Web scraping",
            siteUrl = "https://www.firecrawl.dev",
            logoImagePath = "/static/images/stack/firecrawl.png",
        ),
        AiTechStackEntry(
            displayName = "GrokBot",
            useLabel = "Autonomous work",
            siteUrl = "https://x.ai/news/introducing-grok-bot",
            logoImagePath = "/static/images/stack/grokbot.png",
        ),
        AiTechStackEntry(
            displayName = "Claude Code",
            useLabel = "Power user",
            siteUrl = "https://code.claude.com",
            logoImagePath = "/static/images/stack/claude-code.png",
        ),
        AiTechStackEntry(
            displayName = "OpenDesign",
            useLabel = "Create social media assets",
            siteUrl = "https://open-design.ai",
            logoImagePath = "/static/images/stack/opendesign.png",
        ),
    )
}

data class AiTechStackEntry(
    val displayName: String,
    val useLabel: String,
    val siteUrl: String,
    val logoImagePath: String,
) {
    /**
     * Host and path shown on the stack tile, without https:// or a trailing slash.
     */
    fun visibleHyperlinkText(): String {
        return siteUrl
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .trimEnd('/')
    }
}
