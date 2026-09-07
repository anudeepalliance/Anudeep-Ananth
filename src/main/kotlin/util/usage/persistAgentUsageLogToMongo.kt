package util.usage

import org.slf4j.LoggerFactory
import util.mongoDB.DatabaseReferences

private val usageLogger = LoggerFactory.getLogger("AgentUsageLog")

/**
 * Inserts one usage document into Mongo. Never throws to callers — logging failures are swallowed
 * so recruiters still get agent/voice responses when Mongo is down.
 */
suspend fun persistAgentUsageLogToMongo(usageLogDocument: AgentUsageLogDocument) {
    try {
        DatabaseReferences.agentUsageLogsCollection.insertOne(usageLogDocument)
    } catch (exception: Exception) {
        usageLogger.error(
            "Failed to persist usage log kind=${usageLogDocument.kind} agentId=${usageLogDocument.agentId}: ${exception.message}",
            exception,
        )
    }
}

/**
 * Convenience builder for a successful or failed usage event, then [persistAgentUsageLogToMongo].
 */
suspend fun persistAgentUsageLogToMongo(
    kind: String,
    ok: Boolean,
    durationMs: Long,
    agentId: String? = null,
    inputChars: Int? = null,
    visitorEnteredQueryText: String? = null,
    agentReplyText: String? = null,
    errorMessage: String? = null,
    clientIpAddress: String? = null,
    sessionId: String? = null,
) {
    persistAgentUsageLogToMongo(
        AgentUsageLogDocument(
            kind = kind,
            agentId = agentId,
            ok = ok,
            durationMs = durationMs,
            inputChars = inputChars,
            visitorEnteredQueryText = clippedUsageLogText(visitorEnteredQueryText),
            agentReplyText = clippedUsageLogText(agentReplyText),
            errorMessage = errorMessage,
            clientIpAddress = clientIpAddress,
            sessionId = sessionId,
        ),
    )
}

internal const val MAX_VISITOR_ENTERED_QUERY_TEXT_CHARACTERS = 20_000

/**
 * Keeps query or reply text from blowing past a single Mongo document. Blank input becomes null.
 */
internal fun clippedVisitorEnteredQueryText(visitorEnteredQueryText: String?): String? {
    return clippedUsageLogText(visitorEnteredQueryText)
}

internal fun clippedUsageLogText(usageLogText: String?): String? {
    val trimmedUsageLogText = usageLogText?.trim().orEmpty()
    if (trimmedUsageLogText.isEmpty()) {
        return null
    }
    return trimmedUsageLogText.take(MAX_VISITOR_ENTERED_QUERY_TEXT_CHARACTERS)
}
