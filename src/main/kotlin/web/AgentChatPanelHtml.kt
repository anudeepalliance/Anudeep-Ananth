package web

import agents.AgentChatTurnOutcome
import knowledge.AgentRosterEntries
import knowledge.ContactDetails
import kotlinx.html.ButtonType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.p
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
    val rosterEntry = AgentRosterEntries.agents.firstOrNull { agentRosterEntry ->
        agentRosterEntry.agentId == outcome.agentId
    }
    val displayName = rosterEntry?.displayName ?: outcome.agentId
    val portraitImagePath = rosterEntry?.portraitImagePath
        ?: "/static/images/agents/${outcome.agentId}.jpg"

    return createHTML().div(classes = "chat-turn") {
        div(classes = "chat-speaker") {
            img(classes = "chat-speaker-photo") {
                src = portraitImagePath
                alt = "$displayName portrait of ${ContactDetails.FULL_NAME}"
                attributes["width"] = "56"
                attributes["height"] = "56"
            }
        }
        div(classes = "chat-bubble") {
            attributes["role"] = "group"
            attributes["aria-label"] = "$displayName reply"
            div(classes = "chat-markdown") {
                unsafe { +MarkdownToSafeHtml.render(outcome.replyText) }
            }
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
