package com

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.html.respondHtml
import web.renderPortfolioHtmlDocument

fun Application.configureRouting() {
    routing {
        staticResources("/static", "static")

        get("/") {
            call.respondHtml {
                renderPortfolioHtmlDocument()
            }
        }

        // Stub HTMX endpoints until Phase 4/5 agent runtime lands.
        post("/htmx/job-match") {
            call.respondText(
                """
                <div class="muted">
                  Job Matching stub received your JD. Live Koog + MiniMax scoring lands in Phase 4;
                  richer HTMX fit cards in Phase 5.
                </div>
                """.trimIndent(),
                ContentType.Text.Html,
            )
        }

        post("/htmx/agent-chat") {
            call.respondText(
                """
                <div class="muted">
                  Chat stub online. Streaming agent replies wire up in Phase 5.
                </div>
                """.trimIndent(),
                ContentType.Text.Html,
            )
        }
    }
}
