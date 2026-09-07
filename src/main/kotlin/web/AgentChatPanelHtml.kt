package web

import agents.AgentChatTurnOutcome
import knowledge.AgentRosterEntries
import kotlinx.html.ButtonType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe

/**
 * Renders one HTMX agent chat turn (success bubble or retryable error).
 */
fun renderAgentChatPanelHtml(outcome: AgentChatTurnOutcome): String {
    return when (outcome) {
        is AgentChatTurnOutcome.Success -> renderSuccessfulChatTurnHtml(outcome)
        is AgentChatTurnOutcome.Failure -> renderFailedChatTurnHtml(outcome)
    }
}

private fun renderSuccessfulChatTurnHtml(outcome: AgentChatTurnOutcome.Success): String {
    val displayName = AgentRosterEntries.agents
        .firstOrNull { it.agentId == outcome.agentId }
        ?.displayName
        ?: outcome.agentId

    return createHTML().div(classes = "chat-turn") {
        div(classes = "chat-meta") {
            span(classes = "chip") { +displayName }
            span(classes = "muted") { +"agent reply" }
        }
        div(classes = "chat-bubble chat-markdown") {
            unsafe { +MarkdownToSafeHtml.render(outcome.replyText) }
        }
    }
}

private fun renderFailedChatTurnHtml(outcome: AgentChatTurnOutcome.Failure): String {
    return createHTML().div(classes = "chat-turn is-error") {
        p { +"Chat error: ${outcome.errorMessage}" }
        div(classes = "console-actions") {
            button(classes = "btn btn-primary") {
                type = ButtonType.button
                attributes["onclick"] =
                    "document.getElementById('agent-chat-form')?.requestSubmit()"
                +"Retry send"
            }
        }
    }
}
