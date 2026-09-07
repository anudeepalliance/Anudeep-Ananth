package web

import agents.AgentChatTurnOutcome
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkdownToSafeHtmlTest {

    @Test
    fun `renders headings bold lists and tables`() {
        val html = MarkdownToSafeHtml.render(
            """
            ## Company Snapshot
            **Company:** Inspektlabs
            **Category:** AI vehicle inspection

            - Build a sandbox
            - Publish an API

            --

            | Vertical | Why now |
            | --- | --- |
            | Fleets | EV damage is a differentiator |
            """.trimIndent(),
        )
        assertTrue(html.contains("<h2>"))
        assertTrue(html.contains("<strong>Company:</strong>"))
        assertTrue(html.contains("Inspektlabs"))
        assertTrue(html.contains("<li>"))
        assertTrue(html.contains("<table>"))
        assertTrue(html.contains("<hr"))
        assertTrue(html.contains("Fleets"))
        assertTrue(html.contains("<br"))
    }

    @Test
    fun `escapes raw html from the model`() {
        val html = MarkdownToSafeHtml.render("<script>alert(1)</script> **ok**")
        assertFalse(html.contains("<script>"))
        assertTrue(html.contains("<strong>ok</strong>"))
    }

    @Test
    fun `agent chat bubble swaps rendered markdown instead of raw pipes`() {
        val html = renderAgentChatPanelHtml(
            AgentChatTurnOutcome.Success(
                replyText = """
                    ## Company Snapshot
                    **Inspektlabs** is a platform.

                    | Vertical | Why now |
                    | --- | --- |
                    | Fleets | EV damage is a differentiator |
                """.trimIndent(),
                agentId = "market_research",
            ),
        )
        assertTrue(html.contains("chat-markdown"))
        assertTrue(html.contains("<h2>"))
        assertTrue(html.contains("<strong>Inspektlabs</strong>"))
        assertTrue(html.contains("<table>"))
        assertFalse(html.contains("## Company Snapshot"))
        assertFalse(html.contains("| Vertical |"))
    }
}
