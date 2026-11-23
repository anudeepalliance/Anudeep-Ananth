package com.anudeepananth.projects.ocrolus.notification

import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.util.ConfidentialObjects
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/** Discord links always point at the live site, including runs made on the Mac, so they work once it is deployed. */
private const val PUBLIC_SITE_BASE_URL = "https://projects.anudeepananth.com"
private const val NOTIFICATION_APP_NAME = "Anudeep Projects - Ocrolus assignment"

/** Tells the outside world when a run starts, completes or fails. An interface so tests stay silent. */
fun interface OcrolusRunEventNotifier {
    suspend fun sendRunEventNotification(run: OcrolusAssignmentRun)
}

/** The message shape the existing n8n workflow expects before it posts to Discord. */
@Serializable
internal data class N8nDiscordNotificationBody(val subject: String, val message: String, val appName: String)

/**
 * Posts run updates to the n8n webhook, which forwards them to the Discord channel.
 * A failed notification is only logged; it never fails the run.
 */
object N8nDiscordRunEventNotifier : OcrolusRunEventNotifier {
    private val logger = LoggerFactory.getLogger("N8nDiscordRunEventNotifier")
    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
        }
    }

    override suspend fun sendRunEventNotification(run: OcrolusAssignmentRun) {
        if (ConfidentialObjects.DISCORD_CHANNEL_WEBHOOK_URL.isBlank()) return
        try {
            httpClient.post(ConfidentialObjects.DISCORD_CHANNEL_WEBHOOK_URL) {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(N8nDiscordNotificationBody.serializer(), buildNotificationBody(run)))
            }
        } catch (exception: Exception) {
            logger.warn("Could not send the ${run.status} notification for run ${run.runId}: ${exception.message}")
        }
    }

    /** Builds the Discord text for the run's current status: started, completed (with summary) or failed. */
    internal fun buildNotificationBody(run: OcrolusAssignmentRun): N8nDiscordNotificationBody {
        val runResultUrl = "$PUBLIC_SITE_BASE_URL/ocrolus/runs/${run.runId}"
        return when (run.status) {
            OcrolusRunStatus.COMPLETED -> {
                val accountNamesById = run.result.scoredAccounts.associate {
                    it.account.accountId to it.account.accountName
                }
                val topThreeLines = run.result.outreachMessages.mapIndexed { index, outreachMessage ->
                    "${index + 1}. ${accountNamesById[outreachMessage.accountId] ?: outreachMessage.accountId}: \"${outreachMessage.finalSubject}\""
                }
                N8nDiscordNotificationBody(
                    subject = "Ocrolus assignment completed by ${run.modelDisplayName}",
                    message = listOf(
                        "Product pick: ${run.result.chosenProduct?.displayName ?: "none"}",
                        "Top 15 accounts ranked, outreach written for the top 3:",
                        topThreeLines.joinToString("\n"),
                        "Time taken: ${(run.durationMilliseconds ?: 0) / 1000}s, model calls: ${run.usage.modelCallCount}",
                        "Full result: $runResultUrl",
                    ).joinToString("\n"),
                    appName = NOTIFICATION_APP_NAME,
                )
            }

            OcrolusRunStatus.FAILED -> N8nDiscordNotificationBody(
                subject = "Ocrolus assignment run failed (${run.modelDisplayName})",
                message = "Error: ${run.errorMessage ?: "unknown"}\nWhat it got through: $runResultUrl",
                appName = NOTIFICATION_APP_NAME,
            )

            else -> N8nDiscordNotificationBody(
                subject = "Ocrolus assignment run started with ${run.modelDisplayName}",
                message = "Watch it live: $PUBLIC_SITE_BASE_URL/ocrolus\nFull result once it finishes: $runResultUrl",
                appName = NOTIFICATION_APP_NAME,
            )
        }
    }
}
