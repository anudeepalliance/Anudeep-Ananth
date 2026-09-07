# CLAUDE.md — anudeepananth.com

Personal portfolio site for **Anudeep Ananth**. Target audience: TA, recruiters, hiring managers for mid-senior B2B marketing roles. The differentiator is live custom AI agents, not a static resume page.

Canonical design: `docs/superpowers/specs/2026-09-07-portfolio-agent-site-design.md`  
Work tracker: `tasks.md` (run phases/tasks one by one in Grok Build)

## Reference project (VPS + Mongo)

When wiring Hostinger VPS deploy, MongoDB community edition, SSH tunnels, docker-network hostname probes, or related ops — **copy patterns from PeekoPals**, do not reinvent:

- Project root: `/Users/anudeepmac/IntelliJ/Peekopals/PeekoPals`
- Mongo helpers: `server/src/main/kotlin/com/peekopals/app/util/mongoDB/`
- Secrets pattern: `server/src/main/kotlin/com/peekopals/app/util/confidentialObjects/ConfidentialObjects.kt`

That project is already set up to work with this same VPS and the MongoDB community edition running on it. Prefer adapting its connection/probe/tunnel approach for this portfolio (database name `Anudeep-Ananth`).

## Product intent

- Domain: `www.anudeepananth.com` (buy + deploy only in the final phase after local sign-off)
- Single page: Hook → Achievements → Job Matching → Agents → Contact
- Feel: senior marketing leader at a company; public-safe language only (no internal org words such as GROWTH)
- Giant hero word: ANUDEEP (not GROWTH)
- Visual: light festival/editorial landing (Odyssey-style) — giant type, script name, floating cards; not the old dark terminal look
- Agents section: showcase that Anudeep built these custom agents and uses them in the work (live, not a mockup)
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
2. Performance Marketing Manager
3. Market Researcher
4. Prospecting Agent
5. Lead Scorer
6. Social Media Manager
7. Product Analytics Agent

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

**Rate limits:** none (cost risk accepted). Log every agent/STT/TTS call to **MongoDB community on the Hostinger VPS** (see Usage logging).

## Usage logging (MongoDB)

Mirror the PeekoPals Mongo pattern (`util/mongoDB`: client singleton, collection constants, kotlinx serialization codecs). Connection string(s) live in `ConfidentialObjects.kt`.

- **Server:** MongoDB **community edition already on the Hostinger VPS** (same ops model as PeekoPals — not Atlas for this app)
- **Database:** existing VPS DB `Anudeep-Ananth` — not the PeekoPals `peekopals` DB
- **Collection:** usage events (e.g. `agent_usage_logs`)
- **Connectivity:** PeekoPals-style dual URI — in-VPS hostname/network string for production, SSH-tunnel/`localhost` string for local Mac; optional hostname probe if sharing the same docker network pattern
- **When:** insert one document after each agent run, Sarvam STT call, and ElevenLabs TTS call
- **Fields (minimum):** `createdAt`, `kind` (`agent` | `stt` | `tts`), `agentId` (nullable for pure voice), `ok`, `durationMs`, `inputChars`, `visitorEnteredQueryText` (chat / JD / STT transcript; null for TTS), `agentReplyText` (chat reply or Job Matching fit JSON; null for STT/TTS and failures with no reply), `errorMessage` (nullable), optional client IP / coarse session id
- **Rules:** no rate limiting; logging must not break the user request (catch/log failures); never hardcode Mongo field name strings — use data-class property `.name` references
- **Reference implementation:** `/Users/anudeepmac/IntelliJ/Peekopals/PeekoPals/server/src/main/kotlin/com/peekopals/app/util/mongoDB`

## Content & contact

- Knowledge pack (Phase 1): `src/main/kotlin/knowledge/` — load via `AnudeepKnowledgePack`
- Pitch + timeline + achievements + case studies derived from current `Anudeep Resume.pdf`
- Contact on site: `anudeeps3@gmail.com`, LinkedIn `https://www.linkedin.com/in/anudeepananth`, phone `+91-9741701504`
- Resume source of truth: `Anudeep Resume.pdf` (lifecycle / demand-gen / community track — DigitalOcean → Elucidata → Jiva → Telebu → …)

## SEO

- Semantic HTML for bio/achievements/agents/contact
- Title, description, canonical, Open Graph, Twitter cards
- JSON-LD `Person` + `WebSite` with `sameAs` LinkedIn
- `robots.txt` + `sitemap.xml`
- `GET /llms.txt` and `GET /llms-full.txt` (llmstxt.org) for Google AI / LLM crawlers

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
- Public agents with no rate limits can burn MiniMax/voice budget; persist usage to Mongo early
- Do not reuse the PeekoPals database — use the existing `Anudeep-Ananth` DB on the VPS community Mongo
- Lazy-load video and heavy images; keep first paint HTML light
- n8n must not become the live agent runtime

## Workflow

1. Pick the next unchecked task in `tasks.md`
2. Implement only that task (or a small contiguous group if blocked otherwise)
3. Mark it done in `tasks.md` when verified
4. Do not skip ahead into later phases without need
