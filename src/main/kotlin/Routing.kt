package com

import agents.PortfolioAgentRuntime
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import knowledge.AgentRosterEntries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import web.renderAgentChatPanelHtml
import web.renderJobMatchFitPanelHtml
import web.renderPortfolioHtmlDocument
import web.voiceHtmxRoutes
import web.seo.llmTxtRoutes
import web.seo.seoDiscoveryRoutes

fun Application.configureRouting() {
    val portfolioAgentRuntime = PortfolioAgentRuntime()

    routing {
        staticResources("/static", "static")
        seoDiscoveryRoutes()
        llmTxtRoutes()
        voiceHtmxRoutes()

        get("/health") {
            call.respondText("ok")
        }

        get("/") {
            call.respondHtml {
                renderPortfolioHtmlDocument()
            }
        }

        post("/htmx/job-match") {
            val parameters = call.receiveParameters()
            val jobDescriptionText = parameters["jobDescription"].orEmpty()
            val outcome = withContext(Dispatchers.IO) {
                portfolioAgentRuntime.runJobMatchingFitAnalysis(
                    jobDescriptionText = jobDescriptionText,
                    clientIpAddress = call.request.local.remoteAddress,
                )
            }
            call.respondText(renderJobMatchFitPanelHtml(outcome), ContentType.Text.Html)
        }

        post("/htmx/agent-chat") {
            val parameters = call.receiveParameters()
            val messageText = parameters["message"].orEmpty()
            val agentId = parameters["agentId"]
                ?: AgentRosterEntries.agents.first { it.isPrimary }.agentId
            val replyHtml = withContext(Dispatchers.IO) {
                val chatOutcome = portfolioAgentRuntime.runAgentChatTurn(
                    agentId = agentId,
                    userMessageText = messageText,
                    clientIpAddress = call.request.local.remoteAddress,
                )
                renderAgentChatPanelHtml(chatOutcome)
            }
            call.respondText(replyHtml, ContentType.Text.Html)
        }
    }
}
