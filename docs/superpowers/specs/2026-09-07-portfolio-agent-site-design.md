# Anudeep Ananth portfolio agent site — design

Date: 2026-09-07  
Domain: www.anudeepananth.com  
Audience: talent acquisition, recruiters, hiring managers

## Goal

A single-page portfolio that makes Anudeep feel like a standout mid-senior marketing hire by showcasing live custom AI agents — not just a static resume site.

Recruiters should quickly answer:

1. Is Anudeep the right fit for this role?
2. What extra does he bring (AI agents, generalist range, speed, cost leverage)?

## Locked decisions

- UI: Kotlin HTMX on Ktor (not Compose for Web)
- Visual style: AI lab / terminal
- Page order: Hook → Proof → Agents → Contact
- Agents: all live from day one (Koog + MiniMax)
- Grounding: resume + structured profile + case-study corpus + live tools (Firecrawl, MiniMax web search)
- Job Matching on hard skips: soft reject (strong misalignment + transferable strengths)
- Voice: Sarvam STT in, ElevenLabs TTS out, English only
- Rate limits: none (accepted cost risk)
- Usage logging: MongoDB Atlas `Cluster0`, dedicated `anudeepananth` DB, usage-event collection; PeekoPals-style Kotlin Mongo client; URI in `ConfidentialObjects.kt`
- Hosting: Hostinger VPS ready; domain purchase + DNS + deploy only after full local testing (final phase)
- Secrets: `src/main/kotlin/util/ConfidentialObjects.kt` only (gitignored) — no env/secrets pattern
- Assets ready: professional photo (source for agent portraits)
- Assets to create: Seedance intro video, case studies/corpus, 7 portrait variants

## Architecture

Single Ktor monolith on the VPS:

1. Server-rendered `/` via Kotlin HTML DSL + HTMX
2. Agent HTTP/SSE endpoints backed by Koog agents (MiniMax brain)
3. Voice endpoints (Sarvam STT, ElevenLabs TTS) with minimal client JS for mic/playback
4. Static assets (photo, portraits, video, CSS)
5. SEO HTML visible on first byte (no full-page `opacity: 0` preloader)
6. n8n for cron/background only — not on the live chat path

## Page sections

### Hook

- Name, short sales pitch, photo
- Seedance intro video (lazy-loaded)
- CTA into Job Matching (“Paste a JD”)

### Proof

- Visual experience timeline
- Achievement visuals (ARR, ROAS, CAC, pipeline metrics)
- Tech stack strip: Koog, MiniMax, Firecrawl, Higgsfield, MiniMax web search MCP, n8n, ElevenLabs, Sarvam

### Agents

Roster (each with a slightly tweaked Anudeep portrait):

1. Job Matching (primary)
2. Performance Marketing
3. Market Research
4. Prospecting (ABM)
5. Lead Scoring
6. Social Media Marketing
7. Product Analytics

Job Matching: JD textarea + voice input → fit score, strengths, gaps, preference/skip warnings, follow-up chat.  
Other agents: text + voice chat with streaming replies and optional spoken playback.

### Contact

- Email: anudeepananth@gmail.com
- LinkedIn: https://www.linkedin.com/in/anudeepananth

## Job Matching targeting rules

### In scope

- Demand gen — Manager / Lead / Director / Head
- Growth — Growth Marketing Lead, Head of Growth, Senior Growth Manager
- Lifecycle — Manager / Director
- Marketing ops / RevOps marketing
- Senior B2B performance marketing
- Marketing Director / Head of Marketing (pipeline/demand/growth owned, not brand-only)
- VP Marketing at smaller B2B companies when the role owns pipeline/demand/growth

### Hard skips

- Product Marketing (PMM and similar)
- CRM-owner / CRM Manager seats
- Junior / ~5yr caps (unless explicitly overridden for a listing)
- Onsite / hybrid / city-locked / US-UK-EU-only (LinkedIn Remote tag counts as remote-eligible)

### Preference

- Remote from India
- Mid-senior+
- B2B SaaS / DigitalOcean-adjacent

## SEO / LLM discovery

- Semantic HTML for pitch, timeline, achievements, agent list, contact
- Title, meta description, canonical, Open Graph, Twitter cards
- JSON-LD: Person + WebSite with sameAs LinkedIn
- robots.txt + sitemap.xml

## Reliability

- Agent errors: inline message + retry
- Voice failures: fall back to text
- Missing early assets: placeholders
- No rate limits; monitor MiniMax/voice spend via Mongo usage documents (queryable in Atlas)

## Usage logging

- Persist one Mongo document per billable-ish call: agent, STT, TTS
- PeekoPals-inspired wiring: Mongo coroutine client singleton, typed collections, kotlinx codecs
- Atlas UI / `mongosh` for inspection; optional later n8n daily count summary

## Success bar

A recruiter can land, understand Anudeep in seconds, paste a JD, get a credible fit analysis, try another agent, and contact him — all on one page.

## Out of scope for v1

- Compose for Web UI
- Rate limiting / captcha / invite gates
- Non-English voice
- Multi-page marketing site
- Using n8n as the live agent runtime
