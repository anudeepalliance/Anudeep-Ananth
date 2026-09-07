package web.seo

import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import knowledge.AgentRosterEntries
import knowledge.AnudeepKnowledgePack
import knowledge.CaseStudyCorpusEntries
import knowledge.ContactDetails
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
        appendLine("## Site")
        appendLine()
        appendLine("- [Home]($siteUrl/): Single-page portfolio — hook, proof, live agents, contact.")
        appendLine("- [Job Matching agent]($siteUrl/#agents): Hiring partners can compare a role to published experience.")
        appendLine("- [Sitemap]($siteUrl/sitemap.xml)")
        appendLine("- [Detailed overview]($siteUrl/llms-full.txt)")
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
        appendLine("## Agents on this site")
        appendLine()
        AgentRosterEntries.agents.forEach { agent ->
            val primaryMark = if (agent.isPrimary) " (primary)" else ""
            appendLine("- ${agent.displayName}$primaryMark — ${agent.shortDescription}")
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
        appendLine(
            "Anudeep Ananth, lifecycle marketing, demand generation, growth marketing, " +
                "B2B SaaS, DigitalOcean, AI agents, Koog, MiniMax, Job Matching agent",
        )
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
            appendLine()
        }
        appendLine("## Full grounding pack")
        appendLine()
        append(AnudeepKnowledgePack.formatFullAgentGroundingText())
    }
}
