package com.anudeepananth.projects

import com.anudeepananth.projects.ocrolus.OcrolusApiError
import com.anudeepananth.projects.ocrolus.data.OcrolusMongoRunStorage
import com.anudeepananth.projects.ocrolus.model.KoogOpenRouterModelCaller
import com.anudeepananth.projects.ocrolus.notification.N8nDiscordRunEventNotifier
import com.anudeepananth.projects.ocrolus.pipeline.OcrolusRunCoordinator
import com.anudeepananth.projects.ocrolus.routes.ocrolusRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File

private const val DEFAULT_LOCAL_SERVER_PORT = 9494
private const val API_PATH_PREFIX = "/api"
private const val MAIN_SITE_ADDRESS = "https://www.anudeepananth.com"
private val webBundleDirectoryCandidates = listOf(
    "web",
    "composeApp/build/dist/wasmJs/productionExecutable",
    "../composeApp/build/dist/wasmJs/productionExecutable",
)

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_LOCAL_SERVER_PORT
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        configureProjectsServer()
    }.start(wait = true)
}

/** Wires up the whole server: JSON, live events, the Ocrolus endpoints and the web app's files. */
fun Application.configureProjectsServer() {
    val logger = LoggerFactory.getLogger("ProjectsServer")
    val runStorage = OcrolusMongoRunStorage
    val runCoordinator = OcrolusRunCoordinator(
        runStorage = runStorage,
        modelCaller = KoogOpenRouterModelCaller(),
        runEventNotifier = N8nDiscordRunEventNotifier,
        backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    )

    // A run that was in progress when the server last stopped can never finish, so mark it failed.
    runBlocking {
        try {
            OcrolusMongoRunStorage.createIndexesInMongo()
            runStorage.markInterruptedRunsAsFailed("The server restarted while this run was in progress.")
        } catch (exception: Exception) {
            logger.error("MongoDB is not reachable at start-up: ${exception.message}")
        }
    }

    install(ContentNegotiation) {
        json(
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            },
        )
    }
    install(SSE)
    install(AutoHeadResponse)
    install(CallLogging)
    install(Compression) {
        gzip()
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Request ${call.request.path()} failed: ${cause.message}", cause)
            call.respond(HttpStatusCode.InternalServerError, OcrolusApiError(cause.message ?: "Something went wrong."))
        }
    }
    install(webAppCacheHeadersPlugin)

    val webBundleDirectory = webBundleDirectoryCandidates.map(::File).firstOrNull {
        File(it, "index.html").exists()
    }
    routing {
        get("/health") {
            call.respondText("ok")
        }
        // This subdomain has no home page of its own; its root sends visitors to the main site.
        get("/") {
            call.respondRedirect(MAIN_SITE_ADDRESS, permanent = false)
        }
        ocrolusRoutes(runStorage, runCoordinator)
        if (webBundleDirectory != null) {
            // Any path that is not a file (for example /ocrolus/runs/abc) gets index.html, so deep links survive a refresh.
            singlePageApplication {
                filesPath = webBundleDirectory.path
                defaultPage = "index.html"
            }
        } else {
            logger.warn("No built web app found. Run ./gradlew :composeApp:wasmJsBrowserDistribution to serve it from here.")
        }
    }
}

/**
 * Tells browsers how long to keep the web app's files: the big `.wasm` files have unique names
 * and never change, everything else must be re-checked so a new deploy shows up straight away.
 */
private val webAppCacheHeadersPlugin = createApplicationPlugin("WebAppCacheHeaders") {
    onCall { call ->
        val requestPath = call.request.path()
        if (requestPath.startsWith(API_PATH_PREFIX)) return@onCall
        val cacheControl = if (requestPath.endsWith(".wasm")) "public, max-age=31536000, immutable" else "no-cache"
        call.response.header(HttpHeaders.CacheControl, cacheControl)
    }
}
