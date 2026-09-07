package web

import knowledge.AchievementHighlights
import knowledge.AgentRosterEntries
import knowledge.ContactDetails
import knowledge.ExperienceTimelineEntries
import knowledge.HomepageSalesPitch
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.header
import kotlinx.html.id
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.section
import kotlinx.html.span
import kotlinx.html.textArea
import kotlinx.html.ul

private val techStackLabels = listOf(
    "Koog",
    "MiniMax",
    "Firecrawl",
    "Higgsfield",
    "n8n",
    "ElevenLabs",
    "Sarvam",
    "Ktor HTMX",
)

/**
 * Renders the single-page portfolio body sections (Hook → Proof → Agents → Contact).
 */
fun FlowContent.renderPortfolioPageBody() {
    renderTopBar()
    renderHookSection()
    renderProofSection()
    renderAgentsSection()
    renderContactSection()
    renderSiteFooter()
}

private fun FlowContent.renderTopBar() {
    header(classes = "topbar") {
        div(classes = "brand") {
            span(classes = "brand-dot")
            span { +"anudeepananth.com // agent_os" }
        }
        navLinkRow()
    }
}

private fun FlowContent.navLinkRow() {
    div(classes = "topnav") {
        a(href = "#hook") { +"Hook" }
        a(href = "#proof") { +"Proof" }
        a(href = "#agents") { +"Agents" }
        a(href = "#contact") { +"Contact" }
    }
}

private fun FlowContent.renderHookSection() {
    section(classes = "section") {
        id = "hook"
        p(classes = "eyebrow") { +"01 / hook" }
        div(classes = "hero-grid") {
            div(classes = "panel hero-copy") {
                div(classes = "panel-head") {
                    span(classes = "prompt") { +"> identity.boot" }
                    span { +"visible_html=true" }
                }
                h1 { +ContactDetails.FULL_NAME }
                p {
                    attributes["style"] =
                        "margin-top:0.85rem;color:var(--text);font-size:1.15rem;font-family:var(--font-display);"
                    +HomepageSalesPitch.HEADLINE
                }
                p { +HomepageSalesPitch.PARAGRAPH }
                div(classes = "cta-row") {
                    a(href = "#agents", classes = "btn btn-primary") { +"Paste a JD → Job Matching" }
                    a(href = "#proof", classes = "btn btn-ghost") { +"See proof" }
                }
                div(classes = "chip-row") {
                    span(classes = "chip") { +"Remote from India" }
                    span(classes = "chip") { +"Lifecycle + demand gen" }
                    span(classes = "chip") { +"Custom AI agents" }
                }
            }
            div(classes = "panel hero-media") {
                div(classes = "panel-head") {
                    span(classes = "prompt") { +"> media.slots" }
                    span { +"phase_3_assets" }
                }
                div(classes = "media-slot") {
                    +"Photo slot — professional portrait lands in Phase 3"
                }
                div(classes = "media-slot") {
                    +"Video slot — Seedance intro lazy-loads in Phase 3"
                }
            }
        }
    }
}

private fun FlowContent.renderProofSection() {
    section(classes = "section") {
        id = "proof"
        p(classes = "eyebrow") { +"02 / proof" }
        h2 { +"Timeline, outcomes, stack" }
        div(classes = "panel proof-grid") {
            div(classes = "panel-head") {
                span(classes = "prompt") { +"> proof.render" }
                span { +"from AnudeepKnowledgePack" }
            }
            h3 { +"Achievement signals" }
            div(classes = "achievements") {
                AchievementHighlights.highlights.forEach { highlight ->
                    div(classes = "achievement-card") {
                        div(classes = "metric") { +highlight.metricLabel }
                        div(classes = "context") { +highlight.contextLabel }
                        div(classes = "chip-row") {
                            span(classes = "chip") { +highlight.category }
                        }
                    }
                }
            }
            h3 {
                attributes["style"] = "margin-top:1.25rem;"
                +"Experience timeline"
            }
            div(classes = "timeline") {
                ExperienceTimelineEntries.entries.forEach { entry ->
                    div(classes = "timeline-item") {
                        div(classes = "timeline-meta") { +"${entry.dateRangeLabel} · ${entry.companyName}" }
                        h3 { +entry.roleTitle }
                        ul {
                            entry.summaryBullets.forEach { bullet ->
                                li { +bullet }
                            }
                        }
                    }
                }
            }
            h3 {
                attributes["style"] = "margin-top:1.25rem;"
                +"Tech stack"
            }
            div(classes = "tech-strip") {
                techStackLabels.forEach { label ->
                    span(classes = "chip") { +label }
                }
            }
        }
    }
}

private fun FlowContent.renderAgentsSection() {
    val primaryAgent = AgentRosterEntries.agents.first { it.isPrimary }
    section(classes = "section") {
        id = "agents"
        p(classes = "eyebrow") { +"03 / agents" }
        h2 { +"Agent OS console" }
        div(classes = "panel") {
            div(classes = "panel-head") {
                span(classes = "prompt") { +"> agent_os.online" }
                span { +"chat wiring: phase 4–5" }
            }
            div(classes = "agents-layout") {
                div(classes = "agent-roster") {
                    AgentRosterEntries.agents.forEach { agent ->
                        val activeClass = if (agent.isPrimary) " agent-card is-active is-primary" else " agent-card"
                        button(classes = activeClass.trim()) {
                            type = ButtonType.button
                            attributes["data-agent-id"] = agent.agentId
                            div(classes = "agent-avatar") { +"IMG" }
                            div {
                                h3 {
                                    +agent.displayName
                                    if (agent.isPrimary) {
                                        span(classes = "badge") { +"primary" }
                                    }
                                }
                                p { +agent.shortDescription }
                            }
                        }
                    }
                }
                div(classes = "console-panel") {
                    div(classes = "fit-panel") {
                        h3 { +"Job Matching — paste JD" }
                        p(classes = "muted") {
                            +"Primary agent: ${primaryAgent.displayName}. Soft-reject rules loaded from knowledge pack."
                        }
                        form {
                            id = "job-matching-form"
                            attributes["hx-post"] = "/htmx/job-match"
                            attributes["hx-target"] = "#job-match-output"
                            attributes["hx-swap"] = "innerHTML"
                            textArea {
                                name = "jobDescription"
                                placeholder = "Paste the full job description here…"
                                attributes["aria-label"] = "Job description"
                            }
                            div(classes = "console-actions") {
                                button(classes = "btn btn-primary") {
                                    type = ButtonType.submit
                                    +"Run fit analysis"
                                }
                                button(classes = "btn btn-ghost") {
                                    type = ButtonType.button
                                    +"Voice (Phase 6)"
                                }
                            }
                        }
                        div(classes = "output-slot") {
                            id = "job-match-output"
                            +"Fit score panel waits here — agent brain in Phase 4, HTMX swap live in Phase 5."
                        }
                    }
                    div(classes = "chat-panel") {
                        h3 { +"Follow-up / agent chat" }
                        form {
                            id = "agent-chat-form"
                            attributes["hx-post"] = "/htmx/agent-chat"
                            attributes["hx-target"] = "#agent-chat-output"
                            attributes["hx-swap"] = "innerHTML"
                            textArea {
                                name = "message"
                                placeholder = "Ask a follow-up about fit, channels, lifecycle, ABM…"
                                attributes["aria-label"] = "Agent chat message"
                            }
                            div(classes = "console-actions") {
                                button(classes = "btn btn-primary") {
                                    type = ButtonType.submit
                                    +"Send"
                                }
                                button(classes = "btn btn-ghost") {
                                    type = ButtonType.button
                                    +"Voice (Phase 6)"
                                }
                            }
                        }
                        div(classes = "output-slot") {
                            id = "agent-chat-output"
                            +"Chat thread waits here — streaming replies land in Phase 5."
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.renderContactSection() {
    section(classes = "section") {
        id = "contact"
        p(classes = "eyebrow") { +"04 / contact" }
        div(classes = "panel contact-panel") {
            div(classes = "panel-head") {
                span(classes = "prompt") { +"> contact.open" }
                span { +"hiring managers welcome" }
            }
            h2 { +"Let's talk" }
            p(classes = "muted") {
                +"Email or LinkedIn — happy to walk through a JD live with the Job Matching agent."
            }
            div(classes = "contact-links") {
                a(href = "mailto:${ContactDetails.EMAIL}", classes = "btn btn-primary") {
                    +ContactDetails.EMAIL
                }
                a(href = ContactDetails.LINKEDIN_URL, classes = "btn btn-ghost") {
                    attributes["target"] = "_blank"
                    attributes["rel"] = "noopener noreferrer"
                    +"LinkedIn"
                }
            }
        }
    }
}

private fun FlowContent.renderSiteFooter() {
    div(classes = "site-footer") {
        span { +"© ${ContactDetails.FULL_NAME}" }
        span { +"Built with Ktor + Kotlin HTMX · grounded by AnudeepKnowledgePack" }
    }
}
