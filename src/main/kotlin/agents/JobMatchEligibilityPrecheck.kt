package agents

/**
 * Deterministic hard-skip / preference scan for a pasted job description.
 * Complements the LLM so soft-reject behavior is not solely model-dependent.
 */
data class JobMatchEligibilityPrecheckResult(
    val isHardSkip: Boolean,
    val hardSkipReasons: List<String>,
    val preferenceWarnings: List<String>,
    val matchedHardSkipSignalLabels: List<String>,
)

object JobMatchEligibilityPrecheck {
    private val productMarketingPatterns = listOf(
        Regex("""\bproduct\s+marketing\b""", RegexOption.IGNORE_CASE),
        Regex("""\bpmm\b""", RegexOption.IGNORE_CASE),
        Regex("""\bproduct\s+marketer\b""", RegexOption.IGNORE_CASE),
    )

    private val crmOwnerPatterns = listOf(
        Regex("""\bcrm\s+manager\b""", RegexOption.IGNORE_CASE),
        Regex("""\bcrm\s+owner\b""", RegexOption.IGNORE_CASE),
        Regex("""\bowns?\s+the\s+crm\b""", RegexOption.IGNORE_CASE),
        Regex("""\bcrm\s+administrator\b""", RegexOption.IGNORE_CASE),
    )

    private val juniorCapPatterns = listOf(
        Regex("""\bjunior\b""", RegexOption.IGNORE_CASE),
        Regex("""\bentry[- ]level\b""", RegexOption.IGNORE_CASE),
        Regex("""\bassociate\s+marketing\b""", RegexOption.IGNORE_CASE),
        Regex("""\b0\s*[-–to]+\s*5\s*\+?\s*years?\b""", RegexOption.IGNORE_CASE),
        Regex("""\b1\s*[-–to]+\s*5\s*\+?\s*years?\b""", RegexOption.IGNORE_CASE),
        Regex("""\b2\s*[-–to]+\s*5\s*\+?\s*years?\b""", RegexOption.IGNORE_CASE),
        Regex("""\bup\s+to\s+5\s+years?\b""", RegexOption.IGNORE_CASE),
        Regex("""\bmaximum\s+of\s+5\s+years?\b""", RegexOption.IGNORE_CASE),
        Regex("""~\s*5\s*yr""", RegexOption.IGNORE_CASE),
    )

    private val locationLockPatterns = listOf(
        Regex("""\bonsite\b""", RegexOption.IGNORE_CASE),
        Regex("""\bon-site\b""", RegexOption.IGNORE_CASE),
        Regex("""\bin[- ]office\b""", RegexOption.IGNORE_CASE),
        Regex("""\bhybrid\b""", RegexOption.IGNORE_CASE),
        Regex("""\bmust\s+be\s+based\s+in\b""", RegexOption.IGNORE_CASE),
        Regex("""\brelocation\s+required\b""", RegexOption.IGNORE_CASE),
        Regex("""\bus[- ]only\b""", RegexOption.IGNORE_CASE),
        Regex("""\buk[- ]only\b""", RegexOption.IGNORE_CASE),
        Regex("""\beu[- ]only\b""", RegexOption.IGNORE_CASE),
        Regex("""\bonly\s+candidates\s+in\s+(the\s+)?(us|uk|eu|united\s+states|united\s+kingdom)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bmust\s+reside\s+in\b""", RegexOption.IGNORE_CASE),
    )

    private val remoteEligiblePatterns = listOf(
        Regex("""\bremote\b""", RegexOption.IGNORE_CASE),
        Regex("""\bwork\s+from\s+anywhere\b""", RegexOption.IGNORE_CASE),
        Regex("""\bfully\s+remote\b""", RegexOption.IGNORE_CASE),
        Regex("""\blinkedin\s+remote\b""", RegexOption.IGNORE_CASE),
    )

    private val indiaRemoteFriendlyPatterns = listOf(
        Regex("""\bremote\s+from\s+india\b""", RegexOption.IGNORE_CASE),
        Regex("""\bindia\s+remote\b""", RegexOption.IGNORE_CASE),
        Regex("""\bworldwide\s+remote\b""", RegexOption.IGNORE_CASE),
        Regex("""\bwork\s+from\s+anywhere\b""", RegexOption.IGNORE_CASE),
        Regex("""\btimezone[- ]agnostic\b""", RegexOption.IGNORE_CASE),
    )

    private val midSeniorPatterns = listOf(
        Regex("""\bsenior\b""", RegexOption.IGNORE_CASE),
        Regex("""\blead\b""", RegexOption.IGNORE_CASE),
        Regex("""\bmanager\b""", RegexOption.IGNORE_CASE),
        Regex("""\bdirector\b""", RegexOption.IGNORE_CASE),
        Regex("""\bhead\s+of\b""", RegexOption.IGNORE_CASE),
        Regex("""\bvp\b""", RegexOption.IGNORE_CASE),
        Regex("""\bmid[- ]senior\b""", RegexOption.IGNORE_CASE),
    )

    private val b2bSaasPatterns = listOf(
        Regex("""\bb2b\b""", RegexOption.IGNORE_CASE),
        Regex("""\bsaas\b""", RegexOption.IGNORE_CASE),
        Regex("""\bsoftware\s+as\s+a\s+service\b""", RegexOption.IGNORE_CASE),
    )

    /**
     * Scans [jobDescriptionText] for hard-skip signals and preference gaps.
     */
    fun evaluateJobDescriptionText(jobDescriptionText: String): JobMatchEligibilityPrecheckResult {
        val normalizedJobDescriptionText = jobDescriptionText.trim()
        val hardSkipReasons = mutableListOf<String>()
        val matchedHardSkipSignalLabels = mutableListOf<String>()
        val preferenceWarnings = mutableListOf<String>()

        if (productMarketingPatterns.any { it.containsMatchIn(normalizedJobDescriptionText) }) {
            hardSkipReasons += "Looks like Product Marketing / PMM — hard skip under targeting rules."
            matchedHardSkipSignalLabels += "Product Marketing (PMM and similar)"
        }

        if (crmOwnerPatterns.any { it.containsMatchIn(normalizedJobDescriptionText) }) {
            hardSkipReasons += "Looks like a CRM-owner / CRM Manager seat — hard skip under targeting rules."
            matchedHardSkipSignalLabels += "CRM-owner / CRM Manager seats"
        }

        if (juniorCapPatterns.any { it.containsMatchIn(normalizedJobDescriptionText) }) {
            hardSkipReasons += "Junior / ~5yr experience cap signals — hard skip under targeting rules."
            matchedHardSkipSignalLabels += "Junior / ~5yr experience caps"
        }

        val hasRemoteEligibleSignal = remoteEligiblePatterns.any {
            it.containsMatchIn(normalizedJobDescriptionText)
        }
        val hasLocationLockSignal = locationLockPatterns.any {
            it.containsMatchIn(normalizedJobDescriptionText)
        }
        // LinkedIn Remote (or clear remote wording) counts as remote-eligible even if hybrid appears.
        if (hasLocationLockSignal && !hasRemoteEligibleSignal) {
            hardSkipReasons +=
                "Onsite / hybrid / geo-locked signals without remote eligibility — hard skip under targeting rules."
            matchedHardSkipSignalLabels += "Onsite / hybrid / city-locked / geo-locked"
        }

        if (!indiaRemoteFriendlyPatterns.any { it.containsMatchIn(normalizedJobDescriptionText) } &&
            !hasRemoteEligibleSignal
        ) {
            preferenceWarnings += "Role does not clearly support remote-from-India preference."
        }

        if (!midSeniorPatterns.any { it.containsMatchIn(normalizedJobDescriptionText) }) {
            preferenceWarnings += "Mid-senior+ title signals are weak or missing."
        }

        if (!b2bSaasPatterns.any { it.containsMatchIn(normalizedJobDescriptionText) }) {
            preferenceWarnings += "B2B SaaS / DigitalOcean-adjacent preference is not clearly signaled."
        }

        return JobMatchEligibilityPrecheckResult(
            isHardSkip = hardSkipReasons.isNotEmpty(),
            hardSkipReasons = hardSkipReasons.distinct(),
            preferenceWarnings = preferenceWarnings.distinct(),
            matchedHardSkipSignalLabels = matchedHardSkipSignalLabels.distinct(),
        )
    }
}
