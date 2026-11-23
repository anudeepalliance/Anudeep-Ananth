package com.anudeepananth.projects.ocrolus.api

import com.anudeepananth.projects.ocrolus.OcrolusApiError
import com.anudeepananth.projects.ocrolus.OcrolusApiPaths
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentOverview
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusModelCallLog
import com.anudeepananth.projects.ocrolus.OcrolusRunSummary
import com.anudeepananth.projects.ocrolus.OcrolusStartRunRequest
import com.anudeepananth.projects.ocrolus.OcrolusStartRunResponse
import com.anudeepananth.projects.ocrolus.OcrolusUpdateRunRequest
import com.anudeepananth.projects.platform.createBrowserHttpClient
import com.anudeepananth.projects.platform.readBrowserOriginAddress
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** What came back from the server: either the value asked for, or a reason it could not be given. */
sealed interface ServerReply<out Value> {
    data class Success<Value>(val value: Value) : ServerReply<Value>

    /**
     * The request did not work.
     *
     * @property httpStatusCode the server's status number, or null when the server could not be reached at all
     * @property message a sentence that is safe to show to the visitor
     * @property activeRunId set when the server refused because another run is in progress
     */
    data class Failure(val httpStatusCode: Int?, val message: String, val activeRunId: String? = null) :
        ServerReply<Nothing>
}

const val HTTP_STATUS_NOT_FOUND = 404
const val HTTP_STATUS_CONFLICT = 409

/** Reads and writes the JSON the server speaks; unknown fields are skipped so older pages keep working. */
val ocrolusJson = Json {
    ignoreUnknownKeys = true
}

/** Every call the web app makes to the server's `/api/ocrolus/...` endpoints. */
object OcrolusServerApi {
    private val browserHttpClient = createBrowserHttpClient()

    suspend fun fetchAssignmentOverviewFromServer(): ServerReply<OcrolusAssignmentOverview> =
        sendRequestToServer(HttpMethod.Get, OcrolusApiPaths.ASSIGNMENT_OVERVIEW, OcrolusAssignmentOverview.serializer())

    /** Asks the server to start a run; a refusal (another run active, daily limit, wrong passcode) comes back as a failure. */
    suspend fun requestServerToStartRun(modelId: String, adminPasscode: String?): ServerReply<OcrolusStartRunResponse> =
        sendRequestToServer(HttpMethod.Post, OcrolusApiPaths.RUNS, OcrolusStartRunResponse.serializer()) {
            setJsonBody(
                ocrolusJson.encodeToString(
                    OcrolusStartRunRequest.serializer(),
                    OcrolusStartRunRequest(modelId, adminPasscode)
                )
            )
        }

    suspend fun fetchRunSummariesFromServer(): ServerReply<List<OcrolusRunSummary>> =
        sendRequestToServer(HttpMethod.Get, OcrolusApiPaths.RUNS, ListSerializer(OcrolusRunSummary.serializer()))

    suspend fun fetchRunFromServer(runId: String): ServerReply<OcrolusAssignmentRun> =
        sendRequestToServer(HttpMethod.Get, OcrolusApiPaths.run(runId), OcrolusAssignmentRun.serializer())

    /** Fetches the real prompts and replies of every model call a run made. */
    suspend fun fetchModelCallLogsFromServer(runId: String): ServerReply<List<OcrolusModelCallLog>> =
        sendRequestToServer(
            HttpMethod.Get,
            OcrolusApiPaths.runModelCalls(runId),
            ListSerializer(OcrolusModelCallLog.serializer())
        )

    /** Saves an admin's edits to a run on the server and returns the run as it now stands. */
    suspend fun saveRunEditsToServer(
        runId: String,
        adminPasscode: String,
        updateRunRequest: OcrolusUpdateRunRequest
    ): ServerReply<OcrolusAssignmentRun> =
        sendRequestToServer(HttpMethod.Patch, OcrolusApiPaths.run(runId), OcrolusAssignmentRun.serializer()) {
            header(OcrolusApiPaths.ADMIN_PASSCODE_HEADER, adminPasscode)
            setJsonBody(ocrolusJson.encodeToString(OcrolusUpdateRunRequest.serializer(), updateRunRequest))
        }

    /** Deletes a run and its saved prompts from the server for good; [adminPasscode] is only needed for the featured reference run. */
    suspend fun deleteRunFromServer(runId: String, adminPasscode: String?): ServerReply<Unit> =
        sendRequestToServerExpectingNoBody(HttpMethod.Delete, OcrolusApiPaths.run(runId)) {
            if (!adminPasscode.isNullOrBlank()) {
                header(OcrolusApiPaths.ADMIN_PASSCODE_HEADER, adminPasscode)
            }
        }

    /** The full address of the live event stream for a run, for the browser's `EventSource`. */
    fun runEventsStreamAddress(runId: String): String = readBrowserOriginAddress() + OcrolusApiPaths.runEvents(runId)

    private fun HttpRequestBuilder.setJsonBody(jsonText: String) {
        contentType(ContentType.Application.Json)
        setBody(jsonText)
    }

    private suspend fun <Value> sendRequestToServer(
        httpMethod: HttpMethod,
        path: String,
        replyDeserializer: DeserializationStrategy<Value>,
        configureRequest: HttpRequestBuilder.() -> Unit = {},
    ): ServerReply<Value> = when (val textReply = sendRequestToServerAndReadText(httpMethod, path, configureRequest)) {
        is ServerReply.Failure -> textReply
        is ServerReply.Success -> try {
            ServerReply.Success(ocrolusJson.decodeFromString(replyDeserializer, textReply.value))
        } catch (decodingProblem: Exception) {
            ServerReply.Failure(null, "The server sent a reply this page could not read.")
        }
    }

    private suspend fun sendRequestToServerExpectingNoBody(
        httpMethod: HttpMethod,
        path: String,
        configureRequest: HttpRequestBuilder.() -> Unit,
    ): ServerReply<Unit> = when (val textReply = sendRequestToServerAndReadText(httpMethod, path, configureRequest)) {
        is ServerReply.Failure -> textReply
        is ServerReply.Success -> ServerReply.Success(Unit)
    }

    /** Sends one request and returns the reply text; any non-success status becomes a failure with the server's message. */
    private suspend fun sendRequestToServerAndReadText(
        httpMethod: HttpMethod,
        path: String,
        configureRequest: HttpRequestBuilder.() -> Unit,
    ): ServerReply<String> = try {
        val httpResponse = browserHttpClient.request(readBrowserOriginAddress() + path) {
            method = httpMethod
            configureRequest()
        }
        val responseText = httpResponse.bodyAsText()
        if (httpResponse.status.isSuccess()) {
            ServerReply.Success(responseText)
        } else {
            val apiError = try {
                ocrolusJson.decodeFromString(OcrolusApiError.serializer(), responseText)
            } catch (decodingProblem: Exception) {
                null
            }
            ServerReply.Failure(
                httpStatusCode = httpResponse.status.value,
                message = apiError?.message ?: "The server replied with an error (${httpResponse.status.value}).",
                activeRunId = apiError?.activeRunId,
            )
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (networkProblem: Throwable) {
        ServerReply.Failure(null, "Could not reach the server. Check your connection and try again.")
    }
}
