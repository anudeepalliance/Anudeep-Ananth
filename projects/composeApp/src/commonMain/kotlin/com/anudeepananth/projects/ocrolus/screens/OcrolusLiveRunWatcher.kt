package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.api.OcrolusServerApi
import com.anudeepananth.projects.ocrolus.api.ServerReply
import com.anudeepananth.projects.ocrolus.api.ocrolusJson
import com.anudeepananth.projects.ocrolus.formatting.isStillInProgress
import com.anudeepananth.projects.platform.ServerSentEventStreamConnection
import com.anudeepananth.projects.platform.openServerSentEventStream
import kotlinx.coroutines.delay

private const val FALLBACK_POLLING_INTERVAL_MILLISECONDS = 3000L

/**
 * Follows one run while it is in progress. It listens to the server's live event stream, and if that
 * stream breaks it asks the server for the run every three seconds instead, so the page never goes stale.
 *
 * @property latestRun the newest copy of the run received so far, or null before the first one arrives
 * @property loadFailureMessage set when the run could not be fetched at all (for example it was deleted)
 */
class OcrolusLiveRunWatcher(private val runId: String) {
    var latestRun by mutableStateOf<OcrolusAssignmentRun?>(null)
        private set
    var loadFailureMessage by mutableStateOf<String?>(null)
        private set

    private val hasRunFinished: Boolean
        get() = latestRun?.status?.isStillInProgress() == false

    /** Keeps [latestRun] up to date from the server; returns once the run has completed or failed. */
    suspend fun followRunOnServerUntilFinished() {
        var hasEventStreamBroken = false
        var eventStreamConnection: ServerSentEventStreamConnection? = null
        eventStreamConnection = openServerSentEventStream(
            address = OcrolusServerApi.runEventsStreamAddress(runId),
            onEventData = { eventData ->
                decodeRunOrNull(eventData)?.let {
                    latestRun = it
                }
                if (hasRunFinished) eventStreamConnection?.close()
            },
            onConnectionError = {
                // Closing stops the browser from reconnecting forever; polling below takes over.
                hasEventStreamBroken = true
                eventStreamConnection?.close()
            },
        )
        try {
            while (!hasRunFinished) {
                if (hasEventStreamBroken || latestRun == null) {
                    fetchRunFromServerAndStoreIt()
                    if (loadFailureMessage != null) return
                }
                if (!hasRunFinished) delay(FALLBACK_POLLING_INTERVAL_MILLISECONDS)
            }
        } finally {
            eventStreamConnection.close()
        }
    }

    private suspend fun fetchRunFromServerAndStoreIt() {
        when (val runReply = OcrolusServerApi.fetchRunFromServer(runId)) {
            is ServerReply.Success -> latestRun = runReply.value
            // A short network blip while a run is on screen is not worth an error; the next poll retries.
            is ServerReply.Failure -> if (latestRun == null) loadFailureMessage = runReply.message
        }
    }

    private fun decodeRunOrNull(runJsonText: String): OcrolusAssignmentRun? = try {
        ocrolusJson.decodeFromString(OcrolusAssignmentRun.serializer(), runJsonText)
    } catch (decodingProblem: Exception) {
        null
    }
}
