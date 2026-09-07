package web

import knowledge.AchievementHighlights
import knowledge.AgentRosterEntries
import knowledge.ContactDetails
import knowledge.ExperienceTimelineEntries
import knowledge.HomepageSalesPitch
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.header
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.section
import kotlinx.html.source
import kotlinx.html.span
import kotlinx.html.textArea
import kotlinx.html.ul
import kotlinx.html.video

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
        a(href = "#hook", classes = "brand") {
            span(classes = "brand-dot")
            +"ANUDEEP"
        }
        navLinkRow()
        a(href = "#contact", classes = "join-orb") { +"Talk" }
    }
}

private fun FlowContent.navLinkRow() {
    div(classes = "topnav") {
        a(href = "#hook") { +"Intro" }
        a(href = "#proof") { +"Proof" }
        a(href = "#agents") { +"Agents" }
        a(href = "#contact") { +"Contact" }
    }
}

private fun FlowContent.renderHookSection() {
    section(classes = "hero-stage") {
        id = "hook"
        span(classes = "hero-giant") { +"ANUDEEP" }
        img(classes = "hero-portrait") {
            src = "/static/images/anudeep-hero.jpg"
            alt = "${ContactDetails.FULL_NAME} portrait"
            attributes["loading"] = "eager"
            attributes["width"] = "840"
            attributes["height"] = "1040"
        }
        span(classes = "hero-script") { +ContactDetails.FULL_NAME }
        span(classes = "float-pill pill-left") {
            span(classes = "pill-dot")
            +"Remote · India"
        }
        span(classes = "float-pill pill-right") {
            span(classes = "pill-dot")
            +"AI agents"
        }
        div(classes = "float-card float-jd") {
            h3 { +"Role fit" }
            p { +"Hiring partners can check published experience against a job description." }
            div(classes = "mini-row") {
                a(href = "#agents", classes = "play-orb") { +"▶" }
                span { +"Open Job Matching" }
            }
        }
        div(classes = "float-card float-video") {
            div(classes = "hero-video-wrap") {
                video {
                    attributes["autoplay"] = "true"
                    attributes["muted"] = "true"
                    attributes["loop"] = "true"
                    attributes["playsinline"] = "true"
                    attributes["preload"] = "auto"
                    attributes["aria-label"] = "Intro video"
                    source {
                        src = ContactDetails.INTRO_VIDEO_URL
                        type = "video/mp4"
                    }
                }
            }
        }
        div(classes = "float-card dark float-contact") {
            h3 { +"Start a conversation" }
            p { +"Email or LinkedIn for hiring partners and peer marketing leaders." }
            div(classes = "mini-row") {
                a(href = "#contact", classes = "play-orb") { +"→" }
                span { +"Get in touch" }
            }
        }
        div(classes = "hero-bottom") {
            span(classes = "date-pill") { +"Lifecycle · demand gen · B2B SaaS" }
            h1 { +"SENIOR MARKETING LEADERSHIP" }
        }
    }
}

private fun FlowContent.renderProofSection() {
    section(classes = "section") {
        id = "proof"
        p(classes = "eyebrow") { +"Proof" }
        h2 { +"Timeline, outcomes, stack" }
        p(classes = "muted") { +HomepageSalesPitch.PARAGRAPH }
        div(classes = "panel proof-grid") {
            div(classes = "panel-head") {
                span(classes = "prompt") { +"Selected outcomes" }
                span { +"from published work" }
            }
            h3 { +"Results" }
            div(classes = "achievements") {
                AchievementHighlights.highlights.forEach { highlight ->
                    div(classes = "achievement-card") {
                        highlight.proofImagePath?.let { proofImagePath ->
                            img(classes = "proof-image") {
                                src = proofImagePath
                                alt = "${highlight.metricLabel} proof"
                                attributes["loading"] = "lazy"
                            }
                        }
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
        p(classes = "eyebrow") { +"Agents" }
        h2 { +"Working agents, not a pitch deck" }
        div(classes = "panel") {
            div(classes = "panel-head") {
                span(classes = "prompt") { +"Live demonstration" }
                span { +"built for hiring partners" }
            }
            div(classes = "agents-layout") {
                div(classes = "agent-roster") {
                    AgentRosterEntries.agents.forEach { agent ->
                        val activeClass = if (agent.isPrimary) " agent-card is-active is-primary" else " agent-card"
                        button(classes = activeClass.trim()) {
                            type = ButtonType.button
                            attributes["data-agent-id"] = agent.agentId
                            img(classes = "agent-avatar") {
                                src = agent.portraitImagePath
                                alt = "${agent.displayName} agent portrait of ${ContactDetails.FULL_NAME}"
                                attributes["loading"] = "lazy"
                                attributes["width"] = "104"
                                attributes["height"] = "104"
                            }
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
                        id = "job-matching-panel"
                        h3 { +"Job Matching" }
                        p(classes = "muted") {
                            +"${primaryAgent.displayName} compares a role to published experience. It is a demonstration for recruiters and hiring managers — not an internal company tool."
                        }
                        form {
                            id = "job-matching-form"
                            attributes["hx-post"] = "/htmx/job-match"
                            attributes["hx-target"] = "#job-match-output"
                            attributes["hx-swap"] = "innerHTML"
                            attributes["hx-indicator"] = "#job-match-indicator"
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
                                    attributes["data-voice-jd"] = "true"
                                    +"Voice input"
                                }
                            }
                        }
                        div(classes = "htmx-indicator thinking-indicator") {
                            id = "job-match-indicator"
                            +"Scoring the role against published experience…"
                        }
                        div(classes = "output-slot") {
                            id = "job-match-output"
                            +"Paste a job description to see strengths, gaps, and talking points."
                        }
                    }
                    div(classes = "chat-panel") {
                        h3 {
                            id = "active-agent-chat-title"
                            +"Chat — ${primaryAgent.displayName}"
                        }
                        p(classes = "muted") {
                            id = "active-agent-chat-hint"
                            +"Select an agent card to switch persona. Job Matching follow-ups stay here too."
                        }
                        form {
                            id = "agent-chat-form"
                            attributes["hx-post"] = "/htmx/agent-chat"
                            attributes["hx-target"] = "#agent-chat-output"
                            attributes["hx-swap"] = "innerHTML"
                            attributes["hx-indicator"] = "#agent-chat-indicator"
                            input {
                                id = "active-agent-id"
                                type = InputType.hidden
                                name = "agentId"
                                value = primaryAgent.agentId
                            }
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
                                    attributes["data-voice-chat"] = "true"
                                    +"Voice input"
                                }
                                button(classes = "btn btn-ghost") {
                                    type = ButtonType.button
                                    attributes["data-speak-reply"] = "true"
                                    +"Play spoken reply"
                                }
                            }
                        }
                        p(classes = "muted") {
                            id = "voice-status"
                            +"Voice: English only. Falls back to typing if the mic or speech API fails."
                        }
                        div(classes = "htmx-indicator thinking-indicator") {
                            id = "agent-chat-indicator"
                            +"Thinking — agent drafting reply…"
                        }
                        div(classes = "output-slot") {
                            id = "agent-chat-output"
                            +"Chat replies land here. Switch agents from the roster on the left."
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
        p(classes = "eyebrow") { +"Contact" }
        div(classes = "panel contact-panel") {
            div(classes = "panel-head") {
                span(classes = "prompt") { +"Direct" }
                span { +"hiring partners and peers" }
            }
            h2 { +"Get in touch" }
            p(classes = "muted") {
                +"Email or LinkedIn. Happy to walk a role live if that is useful."
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
        span { +"Personal portfolio · Kotlin HTMX" }
    }
}
