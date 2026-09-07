# CLAUDE.md — anudeepananth.com

Personal portfolio site for **Anudeep Ananth**. Target audience: TA, recruiters, hiring managers for mid-senior B2B marketing roles. The differentiator is live custom AI agents, not a static resume page.

Canonical design: `docs/superpowers/specs/2026-09-07-portfolio-agent-site-design.md`  
Work tracker: `tasks.md` (run phases/tasks one by one in Grok Build)

## Product intent

- Domain: `www.anudeepananth.com`
- Single page: Hook → Proof → Agents → Contact
- Feel: special, AI-native marketer who ships agents
- Visual: dark AI lab / terminal
- Primary interaction: Job Matching agent (paste JD → fit analysis)
- Visitors can chat with all listed agents via text or voice

## Stack (locked)

- **Ktor** server (JVM 21) on **Hostinger VPS**
- **Kotlin HTMX** (`ktor-htmx`, `ktor-htmx-html`, `ktor-server-htmx`) for UI interactivity
- **Koog** for custom agents
- **MiniMax** as agent brain (+ web search MCP where useful)
- **Firecrawl** for scraping
- **Sarvam** speech-to-text (English only)
- **ElevenLabs** text-to-speech
- **Higgsfield** for marketing assets (portraits / creative)
- **n8n** for cron/background jobs only — not live chat
- **Seedance** for the intro video asset (created offline, hosted statically)

Do **not** use Compose for Web for this site.

## Architecture rules

- One Ktor monolith serves HTML, static assets, agent APIs, and voice endpoints
- Page content is server-rendered and **visible on first byte** — never hide the whole page behind `opacity: 0` until JS/Wasm boots
- HTMX handles swaps for score cards, chat bubbles, agent panel switches; streaming via SSE (or equivalent) for agent replies
- Minimal client JS only for mic capture and audio playback
- Secrets live in `src/main/kotlin/util/ConfidentialObjects.kt` (companion `const val`s). Reference those variables in code — do not use env/secrets files
- That file is gitignored on purpose; never commit it. Keep a local copy on the VPS the same way

## Agents (all live)

1. Job Matching (primary)
2. Performance Marketing
3. Market Research
4. Prospecting (ABM)
5. Lead Scoring
6. Social Media Marketing
7. Product Analytics

Each agent gets a slightly tweaked portrait derived from Anudeep’s professional photo.

### Grounding

Every agent can use:

- Structured Anudeep profile (skills, metrics, roles, preferences)
- Resume text
- Case-study / campaign corpus
- Live tools: Firecrawl, MiniMax web search

### Job Matching rules

**In scope:** demand gen (Manager/Lead/Director/Head), growth (Lead/Head/Senior Manager), lifecycle (Manager/Director), marketing ops / RevOps marketing, senior B2B performance marketing, Marketing Director/Head when pipeline/demand/growth-owned, VP Marketing at smaller B2B when pipeline-owned.

**Hard skips:** PMM, CRM-owner/CRM Manager, junior/~5yr caps, onsite/hybrid/city-locked/geo-locked (LinkedIn Remote = eligible).

**Preference:** remote from India, mid-senior+, B2B SaaS / DigitalOcean-adjacent.

**On hard-skip JDs:** soft reject — strong misalignment + transferable strengths.

**Rate limits:** none (cost risk accepted). Still log usage.

## Content & contact

- Pitch + timeline + achievements derived from resume and case studies
- Contact on site: `anudeepananth@gmail.com`, LinkedIn `linkedin.com/in/anudeepananth`
- Resume source: `Anudeep Resume.pdf`

## SEO

- Semantic HTML for bio/proof/agents/contact
- Title, description, canonical, Open Graph, Twitter cards
- JSON-LD `Person` + `WebSite` with `sameAs` LinkedIn
- `robots.txt` + `sitemap.xml`

## Commands

```bash
./gradlew run     # local server (default :8080)
./gradlew test
./gradlew build
```

## Code conventions

- Follow the user/global Kotlin style rules (descriptive names, KDoc for new public APIs, lambda bodies on their own lines, no redundant local types)
- Prefer Kotlin HTML DSL + HTMX attributes over ad-hoc string HTML where practical
- Keep agent prompts/config in clear dedicated files — not buried in route handlers
- Comments only for non-obvious constraints

## Gotchas

- Official Ktor HTMX APIs are experimental — use `@OptIn(ExperimentalKtorApi::class)`
- Public agents with no rate limits can burn MiniMax/voice budget; add logging early
- Lazy-load video and heavy images; keep first paint HTML light
- n8n must not become the live agent runtime

## Workflow

1. Pick the next unchecked task in `tasks.md`
2. Implement only that task (or a small contiguous group if blocked otherwise)
3. Mark it done in `tasks.md` when verified
4. Do not skip ahead into later phases without need
