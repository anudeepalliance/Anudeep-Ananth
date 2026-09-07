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
            errorMessage = errorMessage,
            clientIpAddress = clientIpAddress,
            sessionId = sessionId,
        ),
    )
}
