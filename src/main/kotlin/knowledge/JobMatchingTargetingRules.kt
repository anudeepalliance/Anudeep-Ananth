package knowledge

/**
 * Structured targeting rules the Job Matching agent must apply when scoring a JD.
 */
object JobMatchingTargetingRules {
    val inScopeRoleFamilies = listOf(
        "Demand gen — Manager / Lead / Director / Head",
        "Growth — Growth Marketing Lead, Head of Growth, Senior Growth Manager",
        "Lifecycle — Manager / Director",
        "Marketing ops / RevOps marketing",
        "Senior B2B performance marketing",
        "Marketing Director / Head of Marketing (pipeline/demand/growth owned, not brand-only)",
        "VP Marketing at smaller B2B companies when the role owns pipeline/demand/growth",
    )

    val hardSkipSignals = listOf(
        "Product Marketing (PMM and similar)",
        "CRM-owner / CRM Manager seats",
        "Junior / ~5yr experience caps (unless explicitly overridden for a listing)",
        "Onsite / hybrid / city-locked / US-UK-EU-only (LinkedIn Remote tag counts as remote-eligible)",
    )

    val preferences = listOf(
        "Remote from India",
        "Mid-senior+",
        "B2B SaaS / DigitalOcean-adjacent",
    )

    /**
     * Soft reject: flag misalignment strongly, still surface transferable strengths.
     */
    const val HARD_SKIP_BEHAVIOR = "soft_reject"

    fun formatAsAgentRulesText(): String {
        return buildString {
            appendLine("IN SCOPE ROLE FAMILIES:")
            inScopeRoleFamilies.forEach { appendLine("- $it") }
            appendLine()
            appendLine("HARD SKIP SIGNALS:")
            hardSkipSignals.forEach { appendLine("- $it") }
            appendLine()
            appendLine("PREFERENCES:")
            preferences.forEach { appendLine("- $it") }
            appendLine()
            appendLine("HARD SKIP BEHAVIOR: $HARD_SKIP_BEHAVIOR — strong misalignment callout + transferable strengths.")
        }
    }
}
