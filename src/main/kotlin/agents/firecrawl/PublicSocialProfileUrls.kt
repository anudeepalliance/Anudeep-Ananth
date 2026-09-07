package agents.firecrawl

/**
 * Pulls public social profile URLs (LinkedIn, X/Twitter, Instagram, and similar) out of
 * scraped markdown and Firecrawl's links list.
 */
object PublicSocialProfileUrls {
    private val httpUrlInTextRegex = Regex(
        """https?://[^\s<>"'\)\]]+""",
        RegexOption.IGNORE_CASE,
    )

    private val socialHostNames = setOf(
        "linkedin.com",
        "twitter.com",
        "x.com",
        "instagram.com",
        "facebook.com",
        "fb.com",
        "youtube.com",
        "youtu.be",
        "tiktok.com",
        "threads.net",
    )

    /**
     * Deduped profile URLs in first-seen order. Share/login/intent URLs are dropped.
     */
    fun extractFromMarkdownAndLinks(
        markdown: String,
        discoveredLinks: List<String>,
    ): List<String> {
        val candidateUrls = (discoveredLinks + httpUrlInTextRegex.findAll(markdown).map { matchResult ->
            matchResult.value
        }).mapNotNull { rawUrl ->
            canonicalizeHttpUrlOrNull(rawUrl)
        }

        return candidateUrls.filter { pageUrl ->
            isPublicSocialProfileUrl(pageUrl)
        }.distinct()
    }

    fun canonicalizeHttpUrlOrNull(rawUrl: String): String? {
        val trimmedUrl = rawUrl.trim().trimEnd('.', ',', ';', ')', ']', '"', '\'')
        if (trimmedUrl.isEmpty()) {
            return null
        }
        val parsedUri = runCatching {
            java.net.URI(trimmedUrl)
        }.getOrNull() ?: return null
        val hostName = parsedUri.host?.lowercase()?.removePrefix("www.") ?: return null
        if (parsedUri.scheme !in setOf("http", "https")) {
            return null
        }
        val path = parsedUri.path.orEmpty().trimEnd('/')
        return "https://$hostName$path"
    }

    fun isPublicSocialProfileUrl(pageUrl: String): Boolean {
        val parsedUri = runCatching {
            java.net.URI(pageUrl)
        }.getOrNull() ?: return false
        val hostName = parsedUri.host?.lowercase()?.removePrefix("www.") ?: return false
        if (hostName !in socialHostNames) {
            return false
        }
        val pathSegments = parsedUri.path.orEmpty()
            .trim('/')
            .split('/')
            .filter { pathSegment ->
                pathSegment.isNotBlank()
            }
        if (pathSegments.isEmpty()) {
            return false
        }
        val firstPathSegment = pathSegments.first().lowercase()
        val skippedFirstSegments = setOf(
            "share",
            "sharer",
            "sharearticle",
            "intent",
            "home",
            "search",
            "login",
            "signup",
            "uas",
            "feed",
            "jobs",
            "pulse",
            "help",
            "legal",
            "hashtag",
            "explore",
            "reels",
            "stories",
            "accounts",
            "watch",
            "shorts",
            "embed",
            "i",
            "share-offsite",
        )
        if (firstPathSegment in skippedFirstSegments) {
            return false
        }
        return when (hostName) {
            "linkedin.com" -> firstPathSegment in setOf("company", "in", "school")
            "youtube.com" -> firstPathSegment in setOf("channel", "c", "user") || firstPathSegment.startsWith("@")
            "youtu.be" -> false
            "tiktok.com", "threads.net" -> firstPathSegment.startsWith("@")
            "instagram.com" -> firstPathSegment !in setOf("p", "reel", "tv")
            "facebook.com", "fb.com" -> firstPathSegment !in setOf("dialog", "tr")
            "twitter.com", "x.com" -> firstPathSegment !in setOf("status") && pathSegments.none { pathSegment ->
                pathSegment.equals("status", ignoreCase = true)
            }
            else -> true
        }
    }

    fun extractHttpUrlsFromText(sourceText: String): List<String> {
        return httpUrlInTextRegex.findAll(sourceText).mapNotNull { matchResult ->
            canonicalizeHttpUrlOrNull(matchResult.value)
        }.distinct().toList()
    }
}
