package knowledge

/**
 * Public AI tools used to build this site and the live agents.
 */
object AiTechStackEntries {
    val entries = listOf(
        AiTechStackEntry(
            displayName = "Koog",
            useLabel = "Custom AI agents",
            siteUrl = "https://docs.koog.ai",
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
    )
}

data class AiTechStackEntry(
    val displayName: String,
    val useLabel: String,
    val siteUrl: String,
    val logoImagePath: String,
)
