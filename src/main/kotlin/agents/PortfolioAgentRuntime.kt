package agents

import agents.firecrawl.FirecrawlScrapeClient
import agents.firecrawl.buildFirecrawlScrapeToolRegistry
import agents.minimax.runPortfolioMiniMaxAgentTurn
import ai.koog.agents.core.tools.ToolRegistry
import knowledge.AgentRosterEntries
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import util.usage.UsageKind
import util.usage.persistAgentUsageLogToMongo
import kotlin.math.max
import kotlin.math.min

private val portfolioAgentRuntimeLogger = LoggerFactory.getLogger("PortfolioAgentRuntime")

private val lenientJsonParser = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

private val httpUrlInTextRegex = Regex(
    """https?://[^\s<>"')\]]+""",
    RegexOption.IGNORE_CASE,
)

/**
 * Runs one MiniMax agent turn. Injectable so unit tests can mock the LLM.
 */
fun interface PortfolioLlmTextRunner {
    suspend fun runAgentTurn(
        systemPrompt: String,
        userPrompt: String,
        toolRegistry: ToolRegistry?,
    ): String
}

object MiniMaxPortfolioLlmTextRunner : PortfolioLlmTextRunner {
    override suspend fun runAgentTurn(
        systemPrompt: String,
        userPrompt: String,
        toolRegistry: ToolRegistry?,
    ): String {
        return runPortfolioMiniMaxAgentTurn(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            toolRegistry = toolRegistry,
        )
    }
}

/**
 * Portfolio agent brain: Job Matching structured fit + chat for the other six agents.
 */
class PortfolioAgentRuntime(
    private val llmTextRunner: PortfolioLlmTextRunner = MiniMaxPortfolioLlmTextRunner,
    private val firecrawlScrapeClient: FirecrawlScrapeClient = FirecrawlScrapeClient(),
    private val firecrawlToolRegistry: ToolRegistry = buildFirecrawlScrapeToolRegistry(),
) {
    /**
     * Runs Job Matching: deterministic eligibility precheck + MiniMax structured fit JSON.
     */
    suspend fun runJobMatchingFitAnalysis(
        jobDescriptionText: String,
        clientIpAddress: String? = null,
        sessionId: String? = null,
    ): JobMatchFitAnalysisOutcome {
        val trimmedJobDescriptionText = jobDescriptionText.trim()
        if (trimmedJobDescriptionText.isBlank()) {
            return JobMatchFitAnalysisOutcome.Failure("Paste a job description first.")
        }

        val eligibilityPrecheck =
            JobMatchEligibilityPrecheck.evaluateJobDescriptionText(trimmedJobDescriptionText)
        val startedAtMs = System.currentTimeMillis()

        return try {
            val enrichedJobDescriptionText = enrichTextWithScrapedUrlsIfPresent(trimmedJobDescriptionText)
            val systemPrompt = PortfolioAgentPrompts.jobMatchingStructuredOutputInstructions(eligibilityPrecheck)
            val userPrompt = buildString {
                appendLine("Analyze this job description for Anudeep Ananth fit.")
                appendLine()
                appendLine(enrichedJobDescriptionText)
            }

            val rawAssistantText = llmTextRunner.runAgentTurn(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                toolRegistry = firecrawlToolRegistry,
            )
            val parsedFitAnalysis = parseJobMatchFitAnalysisFromAssistantText(rawAssistantText)
                ?: return failJobMatch(
                    errorMessage = "Job Matching returned unparseable output. Retry in a moment.",
                    startedAtMs = startedAtMs,
                    inputChars = trimmedJobDescriptionText.length,
                    clientIpAddress = clientIpAddress,
                    sessionId = sessionId,
                    eligibilityPrecheck = eligibilityPrecheck,
                )

            val reconciledFitAnalysis = reconcileFitAnalysisWithEligibilityPrecheck(
                fitAnalysis = parsedFitAnalysis,
                eligibilityPrecheck = eligibilityPrecheck,
            )

            persistAgentUsageLogToMongo(
                kind = UsageKind.AGENT,
                ok = true,
                durationMs = System.currentTimeMillis() - startedAtMs,
                agentId = "job_matching",
                inputChars = trimmedJobDescriptionText.length,
                clientIpAddress = clientIpAddress,
                sessionId = sessionId,
            )

            JobMatchFitAnalysisOutcome.Success(
                fitAnalysis = reconciledFitAnalysis,
                eligibilityPrecheck = eligibilityPrecheck,
            )
        } catch (exception: Exception) {
            portfolioAgentRuntimeLogger.error("Job Matching failed: ${exception.message}", exception)
            failJobMatch(
                errorMessage = exception.message ?: "Job Matching failed. Retry in a moment.",
                startedAtMs = startedAtMs,
                inputChars = trimmedJobDescriptionText.length,
                clientIpAddress = clientIpAddress,
                sessionId = sessionId,
                eligibilityPrecheck = eligibilityPrecheck,
            )
        }
    }

    /**
     * Free-form chat turn for any roster agent (including Job Matching follow-ups).
     */
    suspend fun runAgentChatTurn(
        agentId: String,
        userMessageText: String,
        clientIpAddress: String? = null,
        sessionId: String? = null,
    ): AgentChatTurnOutcome {
        val normalizedAgentId = normalizeAgentIdOrDefault(agentId)
        val trimmedUserMessageText = userMessageText.trim()
        if (trimmedUserMessageText.isBlank()) {
            return AgentChatTurnOutcome.Failure(
                errorMessage = "Type a message first.",
                agentId = normalizedAgentId,
            )
        }

        val startedAtMs = System.currentTimeMillis()
        return try {
            val enrichedUserMessageText = enrichTextWithScrapedUrlsIfPresent(trimmedUserMessageText)
            val systemPrompt = PortfolioAgentPrompts.systemPromptForAgentId(normalizedAgentId)
            val toolRegistry = if (
                PortfolioAgentPrompts.agentIdsThatShouldReceiveFirecrawlTool().contains(normalizedAgentId)
            ) {
                firecrawlToolRegistry
            } else {
                null
            }

            val replyText = llmTextRunner.runAgentTurn(
                systemPrompt = systemPrompt,
                userPrompt = enrichedUserMessageText,
                toolRegistry = toolRegistry,
            ).ifBlank {
                "I could not produce a reply. Retry with a slightly shorter ask."
            }

            persistAgentUsageLogToMongo(
                kind = UsageKind.AGENT,
                ok = true,
                durationMs = System.currentTimeMillis() - startedAtMs,
                agentId = normalizedAgentId,
                inputChars = trimmedUserMessageText.length,
                clientIpAddress = clientIpAddress,
                sessionId = sessionId,
            )

            AgentChatTurnOutcome.Success(replyText = replyText, agentId = normalizedAgentId)
        } catch (exception: Exception) {
            portfolioAgentRuntimeLogger.error(
                "Agent chat failed agentId=$normalizedAgentId: ${exception.message}",
                exception,
            )
            persistAgentUsageLogToMongo(
                kind = UsageKind.AGENT,
                ok = false,
                durationMs = System.currentTimeMillis() - startedAtMs,
                agentId = normalizedAgentId,
                inputChars = trimmedUserMessageText.length,
                errorMessage = exception.message,
                clientIpAddress = clientIpAddress,
                sessionId = sessionId,
            )
            AgentChatTurnOutcome.Failure(
                errorMessage = exception.message ?: "Agent chat failed. Retry in a moment.",
                agentId = normalizedAgentId,
            )
        }
    }

    private suspend fun enrichTextWithScrapedUrlsIfPresent(sourceText: String): String {
        val discoveredUrls = httpUrlInTextRegex.findAll(sourceText)
            .map { matchResult ->
                matchResult.value.trimEnd('.', ',', ';')
            }
            .distinct()
            .take(2)
            .toList()

        if (discoveredUrls.isEmpty()) {
            return sourceText
        }

        val scrapedBlocks = discoveredUrls.map { pageUrl ->
            firecrawlScrapeClient.scrapeUrlToMarkdownOrErrorMessage(pageUrl = pageUrl)
        }

        return buildString {
            appendLine(sourceText)
            appendLine()
            appendLine("=== FIRECRAWL PREFETCH ===")
            scrapedBlocks.forEach { scrapedBlock ->
                appendLine(scrapedBlock)
                appendLine()
            }
        }
    }

    private suspend fun failJobMatch(
        errorMessage: String,
        startedAtMs: Long,
        inputChars: Int,
        clientIpAddress: String?,
        sessionId: String?,
        eligibilityPrecheck: JobMatchEligibilityPrecheckResult?,
    ): JobMatchFitAnalysisOutcome.Failure {
        persistAgentUsageLogToMongo(
            kind = UsageKind.AGENT,
            ok = false,
            durationMs = System.currentTimeMillis() - startedAtMs,
            agentId = "job_matching",
            inputChars = inputChars,
            errorMessage = errorMessage,
            clientIpAddress = clientIpAddress,
            sessionId = sessionId,
        )
        return JobMatchFitAnalysisOutcome.Failure(
            errorMessage = errorMessage,
            eligibilityPrecheck = eligibilityPrecheck,
        )
    }
}

internal fun parseJobMatchFitAnalysisFromAssistantText(rawAssistantText: String): JobMatchFitAnalysis? {
    val jsonObjectText = extractJsonObjectFromLlmTextOrNull(rawAssistantText) ?: return null
    return runCatching {
        lenientJsonParser.decodeFromString(JobMatchFitAnalysis.serializer(), jsonObjectText)
    }.getOrNull()
}

/**
 * Forces soft-reject + merges precheck reasons when code detects a hard skip.
 */
internal fun reconcileFitAnalysisWithEligibilityPrecheck(
    fitAnalysis: JobMatchFitAnalysis,
    eligibilityPrecheck: JobMatchEligibilityPrecheckResult,
): JobMatchFitAnalysis {
    val clampedFitScore = min(100, max(0, fitAnalysis.fitScore))
    if (!eligibilityPrecheck.isHardSkip) {
        return fitAnalysis.copy(
            fitScore = clampedFitScore,
            preferenceWarnings = (fitAnalysis.preferenceWarnings + eligibilityPrecheck.preferenceWarnings)
                .distinct(),
        )
    }

    val mergedSoftRejectReasons = (
        eligibilityPrecheck.hardSkipReasons + fitAnalysis.softRejectReasons
        ).distinct()
    val softRejectFitScore = min(clampedFitScore, 42)

    return fitAnalysis.copy(
        fitScore = softRejectFitScore,
        softReject = true,
        softRejectReasons = mergedSoftRejectReasons,
        preferenceWarnings = (fitAnalysis.preferenceWarnings + eligibilityPrecheck.preferenceWarnings)
            .distinct(),
        summary = if (fitAnalysis.summary.isBlank()) {
            "Soft reject: hard-skip signals in the JD. Transferable strengths are still listed for the recruiter."
        } else {
            fitAnalysis.summary
        },
    )
}

fun normalizeAgentIdOrDefault(agentId: String?): String {
    val trimmedAgentId = agentId?.trim().orEmpty()
    val knownAgentIds = AgentRosterEntries.agents.map { it.agentId }.toSet()
    return if (trimmedAgentId in knownAgentIds) {
        trimmedAgentId
    } else {
        "job_matching"
    }
}

/** Shared singleton used by HTMX routes. */
val sharedPortfolioAgentRuntime: PortfolioAgentRuntime by lazy {
    PortfolioAgentRuntime()
}
