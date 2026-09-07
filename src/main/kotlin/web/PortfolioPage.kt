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
import kotlinx.html.article
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
import kotlinx.html.unsafe
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
 * Renders the single-page portfolio body sections (Hook → Achievements → Agents → Contact).
 */
fun FlowContent.renderPortfolioPageBody() {
    renderTopBar()
    renderHookSection()
    renderPortraitFold()
    renderAchievementsSection()
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
        a(href = "#achievements") { +"Achievements" }
        a(href = "#agents") { +"Agents" }
        a(href = "#contact") { +"Contact" }
    }
}

private fun FlowContent.renderHookSection() {
    section(classes = "hero-stage") {
        id = "hook"
        span(classes = "hero-giant") { +"ANUDEEP" }
        video(classes = "hero-stage-video") {
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

private fun FlowContent.renderPortraitFold() {
    section(classes = "portrait-fold") {
        id = "portrait"
        p(classes = "eyebrow") { +"Portrait" }
        img(classes = "fold-portrait") {
            src = "/static/images/anudeep-hero.jpg"
            alt = "${ContactDetails.FULL_NAME} portrait"
            attributes["loading"] = "lazy"
            attributes["width"] = "840"
            attributes["height"] = "840"
        }
        p(classes = "muted portrait-caption") { +HomepageSalesPitch.HEADLINE }
    }
}

private fun FlowContent.renderAchievementsSection() {
    section(classes = "section") {
        id = "achievements"
        p(classes = "eyebrow") { +"Achievements" }
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
                            button(classes = "proof-image-button") {
                                type = ButtonType.button
                                attributes["data-lightbox-src"] = proofImagePath
                                attributes["data-lightbox-alt"] = "${highlight.metricLabel} — ${highlight.contextLabel}"
                                img(classes = "proof-image") {
                                    src = proofImagePath
                                    alt = "${highlight.metricLabel} achievement"
                                    attributes["loading"] = "lazy"
                                }
                            }
                        }
                        div(classes = "metric") { +highlight.metricLabel }
                        div(classes = "context") { +highlight.contextLabel }
                        div(classes = "chip-row") {
                            span(classes = "chip") { +highlight.category }
                        }
                        highlight.proofExternalUrl?.let { proofExternalUrl ->
                            a(href = proofExternalUrl, classes = "proof-external-link") {
                                attributes["target"] = "_blank"
                                attributes["rel"] = "noopener noreferrer"
                                +"View Facebook post"
                            }
                        }
                    }
                }
            }
            h3 {
                attributes["style"] = "margin-top:1.25rem;"
                +"Experience, drawn as a path"
            }
            renderDrawnExperienceJourney()
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

private fun FlowContent.renderDrawnExperienceJourney() {
    val chronologicalEntries = ExperienceTimelineEntries.entries.asReversed()
    div(classes = "journey-map") {
        unsafe {
            +"""
            <svg class="journey-spine" viewBox="0 0 200 1540" preserveAspectRatio="none" aria-hidden="true">
              <path d="M100 24
                C 36 90, 164 150, 100 220
                C 28 290, 172 350, 100 430
                C 40 510, 160 570, 100 650
                C 24 730, 176 790, 100 870
                C 44 950, 156 1010, 100 1090
                C 30 1170, 170 1230, 100 1310
                C 60 1370, 140 1430, 100 1510"
                fill="none" stroke="#4f46e5" stroke-width="3.2"
                stroke-linecap="round" stroke-dasharray="11 7 3 8"/>
            </svg>
            """.trimIndent()
        }
        chronologicalEntries.forEachIndexed { index, entry ->
            val sideClass = if (index % 2 == 0) "is-left" else "is-right"
            val isLatest = index == chronologicalEntries.lastIndex
            article(classes = "journey-stop $sideClass${if (isLatest) " is-now" else ""}") {
                span(classes = "journey-pin") {
                    span { +"${index + 1}" }
                }
                div(classes = "journey-card") {
                    span(classes = "journey-tape")
                    if (isLatest) {
                        span(classes = "journey-now") { +"now" }
                    }
                    span(classes = "journey-year") { +entry.dateRangeLabel }
                    h3 { +entry.companyName }
                    p(classes = "journey-role") { +entry.roleTitle }
                    ul {
                        entry.summaryBullets.take(3).forEach { bullet ->
                            li { +bullet }
                        }
                    }
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
