package web.seo

import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import knowledge.AchievementHighlights
import knowledge.AgentRosterEntries
import knowledge.AiTechStackEntries
import knowledge.AnudeepKnowledgePack
import knowledge.AnudeepProfile
import knowledge.CaseStudyCorpusEntries
import knowledge.ContactDetails
import knowledge.ExperienceTimelineEntries
import knowledge.HomepageSalesPitch
import knowledge.JobMatchingTargetingRules

/**
 * llmstxt.org endpoints so Google AI / LLMs can cite Anudeep without executing JS.
 */
fun Routing.llmTxtRoutes() {
    get("/llms.txt") {
        call.respondText(buildLlmsTxtOverview(), ContentType.Text.Plain)
    }
    get("/llms-full.txt") {
        call.respondText(buildLlmsFullTxt(), ContentType.Text.Plain)
    }
}

internal fun buildLlmsTxtOverview(): String {
    val siteUrl = ContactDetails.PORTFOLIO_URL
    return buildString {
        appendLine("# ${ContactDetails.FULL_NAME}")
        appendLine()
        appendLine("> ${HomepageSalesPitch.HEADLINE}")
        appendLine(">")
        appendLine("> ${HomepageSalesPitch.PARAGRAPH}")
        appendLine()
        appendLine(AnudeepProfile.CAREER_SUMMARY)
        appendLine()
        appendLine("Location: ${AnudeepProfile.LOCATION_SUMMARY}. ${AnudeepProfile.YEARS_EXPERIENCE_SUMMARY}.")
        appendLine("Current role: ${ExperienceTimelineEntries.entries.first().roleTitle} at ${ExperienceTimelineEntries.entries.first().companyName}.")
        appendLine()
        appendLine("## Site")
        appendLine()
        appendLine("- [Home]($siteUrl/): Single-page portfolio — hook, achievements, Job Matching, live agents, AI stack, contact.")
        appendLine("- [Job Matching]($siteUrl/#job-matching): Paste a job description. A live agent scores fit against published experience.")
        appendLine("- [Agents]($siteUrl/#agents): Custom marketing agents Anudeep built and uses for performance, research, prospecting, scoring, social, and analytics.")
        appendLine("- [Achievements]($siteUrl/#achievements): Published outcomes and experience, newest role first.")
        appendLine("- [AI tech stack]($siteUrl/#tech-stack): Tools used to build this site and the agents.")
        appendLine("- [Contact]($siteUrl/#contact): Email and LinkedIn.")
        appendLine("- [Sitemap]($siteUrl/sitemap.xml)")
        appendLine("- [Detailed overview]($siteUrl/llms-full.txt): Full grounding pack, case studies, and targeting rules.")
        appendLine()
        appendLine("## Who this is for")
        appendLine()
        appendLine("Talent acquisition, recruiters, and hiring managers evaluating Anudeep for mid-senior B2B marketing roles.")
        appendLine()
        appendLine("## In-scope roles")
        appendLine()
        JobMatchingTargetingRules.inScopeRoleFamilies.forEach { roleFamily ->
            appendLine("- $roleFamily")
        }
        appendLine()
        appendLine("## Hard skips")
        appendLine()
        JobMatchingTargetingRules.hardSkipSignals.forEach { skipSignal ->
            appendLine("- $skipSignal")
        }
        appendLine()
        appendLine("## Preferences")
        appendLine()
        JobMatchingTargetingRules.preferences.forEach { preference ->
            appendLine("- $preference")
        }
        appendLine()
        appendLine("Hard-skip JDs get a soft reject: strong misalignment plus transferable strengths. LinkedIn Remote counts as remote-eligible.")
        appendLine()
        appendLine("## Experience (newest first)")
        appendLine()
        ExperienceTimelineEntries.entries.forEach { timelineEntry ->
            appendLine("- ${timelineEntry.dateRangeLabel} — ${timelineEntry.roleTitle}, ${timelineEntry.companyName}")
            timelineEntry.summaryBullets.forEach { bullet ->
                appendLine("  - $bullet")
            }
        }
        appendLine()
        appendLine("## Published outcomes")
        appendLine()
        AchievementHighlights.highlights.forEach { highlight ->
            appendLine("- ${highlight.metricLabel} — ${highlight.contextLabel} (${highlight.category})")
        }
        appendLine()
        appendLine("## Agents on this site")
        appendLine()
        appendLine("These are live custom agents (not mockups). Visitors can chat by text or voice.")
        appendLine()
        AgentRosterEntries.agents.forEach { agent ->
            val primaryMark = if (agent.isPrimary) " (primary)" else ""
            appendLine("- ${agent.displayName}$primaryMark — ${agent.shortDescription}")
        }
        appendLine()
        appendLine("## AI tech stack")
        appendLine()
        AiTechStackEntries.entries.forEach { stackEntry ->
            appendLine("- [${stackEntry.displayName}](${stackEntry.siteUrl}): ${stackEntry.useLabel}")
        }
        appendLine()
        appendLine("## Contact")
        appendLine()
        appendLine("- Email: ${ContactDetails.EMAIL}")
        appendLine("- LinkedIn: ${ContactDetails.LINKEDIN_URL}")
        appendLine("- Phone: ${ContactDetails.PHONE_DISPLAY}")
        appendLine("- Website: $siteUrl")
        appendLine()
        appendLine("## Keywords")
        appendLine()
        appendLine(PortfolioSeo.KEYWORDS)
    }
}

internal fun buildLlmsFullTxt(): String {
    return buildString {
        appendLine("# ${ContactDetails.FULL_NAME} — detailed information")
        appendLine()
        append(buildLlmsTxtOverview())
        appendLine()
        appendLine("## Case studies")
        appendLine()
        CaseStudyCorpusEntries.entries.forEach { caseStudy ->
            appendLine("### ${caseStudy.title}")
            appendLine()
            appendLine("Problem: ${caseStudy.problemStatement}")
            appendLine("What Anudeep did: ${caseStudy.whatAnudeepDid}")
            appendLine("Outcomes: ${caseStudy.outcomes.joinToString("; ")}")
            appendLine("Skills: ${caseStudy.skillsDemonstrated.joinToString("; ")}")
            appendLine()
        }
        appendLine("## Full grounding pack")
        appendLine()
        append(AnudeepKnowledgePack.formatFullAgentGroundingText())
    }
}
