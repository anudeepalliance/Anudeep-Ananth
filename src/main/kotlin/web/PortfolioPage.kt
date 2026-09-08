package web

import knowledge.AchievementHighlight
import knowledge.AchievementHighlights
import knowledge.AgentRosterEntries
import knowledge.AiTechStackEntries
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
import kotlinx.html.footer
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.header
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.main
import kotlinx.html.nav
import kotlinx.html.ol
import kotlinx.html.p
import kotlinx.html.section
import kotlinx.html.source
import kotlinx.html.span
import kotlinx.html.textArea
import kotlinx.html.ul
import kotlinx.html.unsafe
import kotlinx.html.video

/**
 * Renders the single-page portfolio body sections (Hook → Achievements → Job Matching → Agents → Tech stack → Contact).
 */
fun FlowContent.renderPortfolioPageBody() {
    renderTopBar()
    main {
        id = "main"
        renderHookSection()
        renderAchievementsSection()
        renderJobMatchingSection()
        renderAgentsSection()
        renderAiTechStackSection()
        renderContactSection()
    }
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
    nav(classes = "topnav") {
        attributes["aria-label"] = "Primary"
        a(href = "#hook") { +"Intro" }
        a(href = "#achievements") { +"Achievements" }
        a(href = "#job-matching") { +"Job Matching" }
        a(href = "#agents") { +"Agents" }
        a(href = "#tech-stack") { +"Stack" }
        a(href = "#contact") { +"Contact" }
    }
}

private fun FlowContent.renderHookSection() {
    section(classes = "hero-stage") {
        id = "hook"
        h1(classes = "hero-giant") { +"ANUDEEP" }
        div(classes = "hero-video-frame") {
            attributes["tabindex"] = "0"
            attributes["aria-label"] = "Pause intro"
            video(classes = "hero-stage-video") {
                attributes["autoplay"] = "true"
                attributes["muted"] = "true"
                attributes["playsinline"] = "true"
                attributes["preload"] = "auto"
                attributes["width"] = "960"
                attributes["height"] = "720"
                attributes["aria-label"] = "Intro video"
                source {
                    src = ContactDetails.INTRO_VIDEO_URL
                    type = "video/mp4"
                }
            }
            div(classes = "hero-video-tap") {
                attributes["aria-hidden"] = "true"
                span(classes = "hero-video-tap-orb") {
                    span(classes = "hero-video-tap-play") {
                        renderButtonIcon(ButtonIconKind.PLAY)
                    }
                    span(classes = "hero-video-tap-replay") {
                        renderButtonIcon(ButtonIconKind.REPLAY)
                    }
                }
            }
            div(classes = "hero-video-controls") {
                button(classes = "hero-video-replay-control") {
                    type = ButtonType.button
                    attributes["aria-label"] = "Replay intro"
                    renderButtonIcon(ButtonIconKind.REPLAY)
                    span { +"Replay" }
                }
                button(classes = "hero-video-mute is-muted") {
                    type = ButtonType.button
                    attributes["aria-label"] = "Unmute intro video"
                    unsafe {
                        +"""
                        <svg class="icon-muted" viewBox="0 0 24 24" aria-hidden="true">
                          <path d="M4.5 9.5v5h3.2L12 18.2V5.8L7.7 9.5H4.5z"/>
                          <path d="m16.2 9.1 5.2 5.2M21.4 9.1l-5.2 5.2" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                        </svg>
                        <svg class="icon-sound" viewBox="0 0 24 24" aria-hidden="true">
                          <path d="M4.5 9.5v5h3.2L12 18.2V5.8L7.7 9.5H4.5z"/>
                          <path d="M15.2 8.4a4.4 4.4 0 0 1 0 7.2M17.8 6a7.4 7.4 0 0 1 0 12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                        </svg>
                        """.trimIndent()
                    }
                }
                input(classes = "hero-video-seek") {
                    type = InputType.range
                    attributes["min"] = "0"
                    attributes["max"] = "1000"
                    attributes["value"] = "0"
                    attributes["step"] = "1"
                    attributes["aria-label"] = "Intro video position"
                }
            }
        }
        span(classes = "float-pill pill-a") {
            span(classes = "pill-dot")
            +"Growth Marketing"
        }
        span(classes = "float-pill pill-b") {
            span(classes = "pill-dot")
            +"Demand Generation"
        }
        span(classes = "float-pill pill-c") {
            span(classes = "pill-dot")
            +"AI Native Marketer"
        }
        a(href = "#job-matching", classes = "float-card float-jd") {
            attributes["aria-label"] = "Open Job Matching"
            h3 { +"Agents I use" }
            p { +"Custom agents I built for marketing work. Start with Job Matching." }
            div(classes = "mini-row") {
                span(classes = "play-orb") { +"▶" }
                span { +"Open Job Matching" }
            }
        }
        a(href = "#contact", classes = "float-card dark float-contact") {
            attributes["aria-label"] = "Get in touch"
            h3 { +"Start a conversation" }
            p { +"Email or LinkedIn for hiring partners and peer marketing leaders." }
            div(classes = "mini-row") {
                span(classes = "play-orb") { +"→" }
                span { +"Get in touch" }
            }
        }
        div(classes = "hero-bottom") {
            span(classes = "date-pill") { +"Anudeep Ananth · lifecycle · demand gen · B2B SaaS" }
        }
    }
}

private fun FlowContent.renderAchievementsSection() {
    val outcomeHighlightsWithProof = AchievementHighlights.highlights.filter { highlight ->
        highlight.proofImagePath != null
    }
    val outcomeHighlightsWithoutProof = AchievementHighlights.highlights.filter { highlight ->
        highlight.proofImagePath == null
    }
    section(classes = "section achievements-stage") {
        id = "achievements"
        p(classes = "eyebrow") { +"Achievements" }
        h2 { +"Timeline and outcomes" }
        p(classes = "muted achievements-lede") { +HomepageSalesPitch.PARAGRAPH }
        div(classes = "receipt-board") {
            p(classes = "receipt-kicker") { +"From published work" }
            div(classes = "receipt-headliners") {
                outcomeHighlightsWithProof.forEach { highlight ->
                    renderPublishedOutcomeTicket(highlight, isFeatured = true)
                }
            }
            div(classes = "receipt-stubs") {
                outcomeHighlightsWithoutProof.forEach { highlight ->
                    renderPublishedOutcomeTicket(highlight, isFeatured = false)
                }
            }
        }
        div(classes = "path-stage") {
            h3(classes = "path-heading") { +"My Experience" }
            renderDrawnExperienceJourney()
        }
    }
}

/**
 * One published outcome as a festival ticket. Featured tickets clip on a proof photo that opens in the lightbox.
 */
private fun FlowContent.renderPublishedOutcomeTicket(
    highlight: AchievementHighlight,
    isFeatured: Boolean,
) {
    article(classes = "outcome-ticket${if (isFeatured) " is-featured" else ""}") {
        if (isFeatured) {
            span(classes = "outcome-tape")
        }
        highlight.proofImagePath?.let { proofImagePath ->
            button(classes = "outcome-polaroid") {
                type = ButtonType.button
                attributes["data-lightbox-src"] = proofImagePath
                attributes["data-lightbox-alt"] = "${highlight.metricLabel} — ${highlight.contextLabel}"
                attributes["aria-label"] = "Open proof image for ${highlight.metricLabel}"
                img {
                    src = proofImagePath
                    alt = "${highlight.metricLabel} achievement"
                    attributes["loading"] = "lazy"
                    attributes["width"] = "640"
                    attributes["height"] = "400"
                }
            }
        }
        div(classes = "ticket-stub") {
            h3(classes = "outcome-metric") { +highlight.metricLabel }
            span(classes = "outcome-stamp") { +highlight.category }
        }
        p(classes = "outcome-context") { +highlight.contextLabel }
        highlight.proofExternalUrl?.let { proofExternalUrl ->
            a(href = proofExternalUrl, classes = "proof-external-link") {
                attributes["target"] = "_blank"
                attributes["rel"] = "noopener noreferrer"
                +"View Facebook post"
            }
        }
    }
}

private fun FlowContent.renderDrawnExperienceJourney() {
    val timelineEntriesNewestFirst = ExperienceTimelineEntries.entries
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
                class="journey-ink"
                fill="none" stroke="#ffcc00" stroke-width="3.2"
                stroke-linecap="round" stroke-dasharray="11 7 3 8"/>
            </svg>
            """.trimIndent()
        }
        timelineEntriesNewestFirst.forEachIndexed { index, entry ->
            val sideClass = if (index % 2 == 0) "is-left" else "is-right"
            val isLatest = index == 0
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
                    div(classes = "journey-company") {
                        img(classes = "journey-logo") {
                            src = entry.logoImagePath
                            alt = ""
                            attributes["width"] = "40"
                            attributes["height"] = "40"
                        }
                        h3 { +entry.companyName }
                    }
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

private fun FlowContent.renderJobMatchingSection() {
    val jobMatchingAgent = AgentRosterEntries.agents.first { it.agentId == "job_matching" }
    section(classes = "section") {
        id = "job-matching"
        p(classes = "eyebrow") { +"Job Matching" }
        h2 { +"Paste your role. See the fit." }
        p(classes = "muted") {
            +"I built this agent and I use it to score a job description against published experience. It has its own input and output — separate from the other agents."
        }
        div(classes = "panel") {
            div(classes = "panel-head") {
                span(classes = "prompt") { +"Built by me" }
                span { +"used in the work" }
            }
            div(classes = "job-matching-layout") {
                div(classes = "job-matching-intro") {
                    img(classes = "job-matching-portrait") {
                        src = jobMatchingAgent.portraitImagePath
                        alt = "${jobMatchingAgent.displayName} portrait of ${ContactDetails.FULL_NAME}"
                        attributes["width"] = "280"
                        attributes["height"] = "280"
                    }
                    h3 { +jobMatchingAgent.displayName }
                    p(classes = "muted") { +jobMatchingAgent.shortDescription }
                }
                div(classes = "fit-panel") {
                    id = "job-matching-panel"
                    h3 { +"Job description" }
                    p(classes = "muted") {
                        +"Paste the full posting. The output is a fit read: strengths, gaps, and talking points."
                    }
                    form {
                        id = "job-matching-form"
                        attributes["hx-post"] = "/htmx/job-match"
                        attributes["hx-target"] = "#job-match-output"
                        attributes["hx-swap"] = "innerHTML"
                        attributes["hx-indicator"] = "#job-match-indicator"
                        attributes["hx-disabled-elt"] = "find .btn-primary"
                        textArea {
                            name = "jobDescription"
                            placeholder = "Paste the full job description here…"
                            attributes["aria-label"] = "Job description"
                        }
                        div(classes = "console-actions") {
                            button(classes = "btn btn-primary") {
                                type = ButtonType.submit
                                renderButtonIcon(ButtonIconKind.SEARCH)
                                +"Run fit analysis"
                            }
                            button(classes = "btn btn-ghost voice-record-btn") {
                                type = ButtonType.button
                                attributes["data-voice-jd"] = "true"
                                attributes["data-idle-label"] = "Voice input"
                                attributes["aria-pressed"] = "false"
                                renderButtonIcon(ButtonIconKind.MICROPHONE)
                                renderButtonIcon(ButtonIconKind.STOP_RECORDING)
                                span(classes = "voice-record-label") { +"Voice input" }
                            }
                        }
                    }
                    p(classes = "muted voice-status") {
                        attributes["aria-live"] = "polite"
                    }
                    renderThinkingIndicator(indicatorId = "job-match-indicator")
                    div(classes = "output-slot") {
                        id = "job-match-output"
                        +"Paste a job description to see strengths, gaps, and talking points."
                    }
                }
            }
        }
    }
}

private fun FlowContent.renderAgentsSection() {
    val workingAgents = AgentRosterEntries.agents.filter { agent ->
        agent.agentId != "job_matching"
    }
    val defaultChatAgent = workingAgents.first()
    section(classes = "section agents-stage") {
        id = "agents"
        p(classes = "eyebrow") { +"Agents" }
        h2 { +"The AI agents I've built and use day to day" }
        p(classes = "muted agents-lede") {
            +"I built these and I use them. Pick one and talk to it. Job Matching has its own section above."
        }
        div(classes = "agent-kit") {
            attributes["role"] = "radiogroup"
            attributes["aria-label"] = "Choose an agent"
            workingAgents.forEachIndexed { index, agent ->
                val isActive = index == 0
                button(classes = "agent-poster${if (isActive) " is-active" else ""}") {
                    type = ButtonType.button
                    attributes["role"] = "radio"
                    attributes["data-agent-id"] = agent.agentId
                    attributes["data-agent-name"] = agent.displayName
                    attributes["data-agent-use"] = agent.shortDescription
                    attributes["data-agent-hint"] = agent.inputHintText
                    attributes["aria-checked"] = if (isActive) "true" else "false"
                    img(classes = "agent-poster-photo") {
                        src = agent.portraitImagePath
                        alt = "${agent.displayName} portrait of ${ContactDetails.FULL_NAME}"
                        attributes["loading"] = "lazy"
                        attributes["width"] = "280"
                        attributes["height"] = "280"
                    }
                    span(classes = "agent-poster-name") { +agent.displayName }
                }
            }
        }
        div(classes = "agent-desk") {
            id = "agent-live"
            p(classes = "agent-live") {
                span(classes = "live-pip")
                +"Live"
            }
            h3 {
                id = "active-agent-chat-title"
                +defaultChatAgent.displayName
            }
            p(classes = "muted") {
                id = "active-agent-chat-hint"
                +defaultChatAgent.shortDescription
            }
            form {
                id = "agent-chat-form"
                attributes["hx-post"] = "/htmx/agent-chat"
                attributes["hx-target"] = "#agent-chat-output"
                attributes["hx-swap"] = "innerHTML"
                attributes["hx-indicator"] = "#agent-chat-indicator"
                attributes["hx-disabled-elt"] = "find .btn-primary"
                input {
                    id = "active-agent-id"
                    type = InputType.hidden
                    name = "agentId"
                    value = defaultChatAgent.agentId
                }
                textArea {
                    name = "message"
                    placeholder = defaultChatAgent.inputHintText
                    attributes["aria-label"] = "Agent chat message"
                }
                div(classes = "console-actions") {
                    button(classes = "btn btn-primary") {
                        type = ButtonType.submit
                        renderButtonIcon(ButtonIconKind.SEND)
                        +"Send"
                    }
                    button(classes = "btn btn-ghost voice-record-btn") {
                        type = ButtonType.button
                        attributes["data-voice-chat"] = "true"
                        attributes["data-idle-label"] = "Voice input"
                        attributes["aria-pressed"] = "false"
                        renderButtonIcon(ButtonIconKind.MICROPHONE)
                        renderButtonIcon(ButtonIconKind.STOP_RECORDING)
                        span(classes = "voice-record-label") { +"Voice input" }
                    }
                    button(classes = "btn btn-ghost") {
                        type = ButtonType.button
                        attributes["data-speak-reply"] = "true"
                        renderButtonIcon(ButtonIconKind.SPEAKER)
                        +"Play spoken reply"
                    }
                }
            }
            p(classes = "muted voice-status") {
                id = "voice-status"
                attributes["aria-live"] = "polite"
            }
            renderThinkingIndicator(indicatorId = "agent-chat-indicator")
            div(classes = "output-slot") {
                id = "agent-chat-output"
                +"Replies from the agent I built land here."
            }
        }
    }
}

private fun FlowContent.renderAiTechStackSection() {
    section(classes = "section") {
        id = "tech-stack"
        p(classes = "eyebrow") { +"Tools" }
        h2 { +"My AI tech stack" }
        p(classes = "muted agents-lede") {
            +"What I use to build the agents on this page, the intro video, and the rest of the site."
        }
        div(classes = "stack-grid") {
            AiTechStackEntries.entries.forEach { stackEntry ->
                a(href = stackEntry.siteUrl, classes = "stack-card") {
                    attributes["target"] = "_blank"
                    attributes["rel"] = "noopener noreferrer"
                    img(classes = "stack-logo") {
                        src = stackEntry.logoImagePath
                        alt = "${stackEntry.displayName} logo"
                        attributes["width"] = "48"
                        attributes["height"] = "48"
                    }
                    h3 { +stackEntry.displayName }
                    p { +stackEntry.useLabel }
                    p(classes = "stack-url") { +stackEntry.visibleHyperlinkText() }
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
            div(classes = "contact-layout") {
                img(classes = "contact-portrait") {
                    src = "/static/images/anudeep-hero.jpg"
                    alt = "${ContactDetails.FULL_NAME} portrait"
                    attributes["width"] = "480"
                    attributes["height"] = "480"
                }
                div(classes = "contact-copy") {
                    h2 { +"Get in touch" }
                    p(classes = "muted") {
                        +"Email or LinkedIn. Happy to walk a role live if that is useful."
                    }
                    div(classes = "contact-links") {
                        a(href = ContactDetails.MAILTO_HREF, classes = "btn btn-primary") {
                            attributes["aria-label"] = "Email ${ContactDetails.EMAIL}"
                            attributes["data-gmail-compose"] = ContactDetails.GMAIL_COMPOSE_URL
                            renderButtonIcon(ButtonIconKind.MAIL)
                            +"Email me"
                        }
                        a(href = ContactDetails.LINKEDIN_URL, classes = "btn btn-ghost") {
                            attributes["target"] = "_blank"
                            attributes["rel"] = "me noopener noreferrer"
                            renderButtonIcon(ButtonIconKind.LINKEDIN)
                            +"DM on LinkedIn"
                        }
                    }
                    p(classes = "contact-email-line") {
                        a(href = ContactDetails.MAILTO_HREF) {
                            +ContactDetails.EMAIL
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.renderSiteFooter() {
    footer(classes = "site-footer") {
        span { +"© ${ContactDetails.FULL_NAME}" }
        span { +"Personal portfolio · Kotlin HTMX" }
    }
}

/**
 * HTMX thinking bar: cycles [ThinkingProgressLines] every two seconds, then holds on the last line.
 */
private fun FlowContent.renderThinkingIndicator(indicatorId: String) {
    div(classes = "htmx-indicator thinking-indicator") {
        id = indicatorId
        span(classes = "thinking-spinner") {
            attributes["aria-hidden"] = "true"
        }
        span(classes = "thinking-spinner thinking-spinner-ring") {
            attributes["aria-hidden"] = "true"
        }
        span(classes = "thinking-status") {
            attributes["aria-live"] = "polite"
            +ThinkingProgressLines.lines.first()
        }
        span(classes = "thinking-dots") {
            attributes["aria-hidden"] = "true"
            repeat(3) {
                span {}
            }
        }
        ol(classes = "thinking-steps") {
            attributes["hidden"] = "hidden"
            ThinkingProgressLines.lines.forEach { progressLine ->
                li { +progressLine }
            }
        }
    }
}
