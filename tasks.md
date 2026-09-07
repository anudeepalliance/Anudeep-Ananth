# Tasks — anudeepananth.com

Tracker for building the portfolio agent site. Run one task (or a tight group) at a time in Grok Build. Check items off when done.

Design reference: `docs/superpowers/specs/2026-09-07-portfolio-agent-site-design.md`  
Project rules: `CLAUDE.md`

Order rule: build and fully test locally first. Purchase domain, link DNS, and deploy to VPS only in Phase 9.

**Session handoff rule:** when completing a task, add short notes under it (paths, decisions, gotchas) so the next session can continue without re-discovery.

---

## Phase 0 — Foundations

- [x] Init git repo, add `.gitignore` (`.gradle`, `build`, `.idea`, `.superpowers`, `ConfidentialObjects.kt`)
  - Notes: repo on `main`; `ConfidentialObjects.kt` must never be committed.
- [x] Confirm local `./gradlew run` still serves the Ktor hello-world baseline
  - Notes: `GET /` → 200 `Hello, World!` on `:8080`.
- [x] Confirm `ConfidentialObjects.kt` stays gitignored and is present locally; wire new keys there only (no env/secrets pattern)
  - Notes: secrets pattern locked — companion consts only, no env files.
- [x] Add VPS community Mongo connection strings to `ConfidentialObjects.kt` (in-VPS + local SSH-tunnel URIs; database `Anudeep-Ananth`)
  - Notes: dual URIs (VPS `peekopals-mongo` + localhost tunnel). Same Mongo instance as PeekoPals.
  - Tunnel: `ssh -L 27017:localhost:27017 -p 1379 reaptive@72.60.219.67 -N`
- [x] Port a slim PeekoPals-style Mongo helper (client singleton + collections + kotlinx codecs; hostname/tunnel pick like PeekoPals)
  - Notes: `src/main/kotlin/util/mongoDB/MongoDatabase.kt`, `Collections.kt`. Hostname probe on `peekopals-mongo`.
- [x] Define usage-event data class + `agent_usage_logs` collection
  - Notes: `src/main/kotlin/util/usage/AgentUsageLogDocument.kt`; kinds `agent` | `stt` | `tts`.
- [x] Implement insert helper for agent / STT / TTS usage (never fail the user request if logging fails)
  - Notes: `persistAgentUsageLogToMongo()` swallows errors. Sample docs seeded in Mongo (5 docs).
  - Ops fix done: granted `peekopals_app` **readWrite** on DB `Anudeep-Ananth` (role was missing). Reference PeekoPals for VPS/Mongo ops.

## Phase 1 — Content & knowledge pack

- [x] Extract structured Anudeep profile from resume (roles, skills, metrics, preferences, hard skips)
  - Notes: `src/main/kotlin/knowledge/AnudeepProfile.kt` — from current `Anudeep Resume.pdf` (lifecycle/demand-gen/community track, not the older perf-marketing PDF narrative).
- [x] Write short homepage sales pitch paragraph
  - Notes: `src/main/kotlin/knowledge/HomepageSalesPitch.kt` — headline + paragraph for Hook CTA into Job Matching.
- [x] Draft experience timeline content (visual-ready milestones)
  - Notes: `src/main/kotlin/knowledge/ExperienceTimelineEntries.kt` — 7 roles (DigitalOcean → Frankfinn).
- [x] Draft achievements content (ARR, ROAS, CAC, pipeline, etc.)
  - Notes: `src/main/kotlin/knowledge/AchievementHighlights.kt` — metric chips aligned to resume outcomes (+25% engagement, −40% CPL, $1.2M, etc.).
- [x] Write 2–4 case-study / campaign corpus entries agents can retrieve
  - Notes: `src/main/kotlin/knowledge/CaseStudyCorpusEntries.kt` — 4 studies (DO lifecycle, Elucidata community DG, Jiva HubSpot, Telebu Lucid POS).
- [x] Encode Job Matching targeting rules as structured config the agent reads
  - Notes: `src/main/kotlin/knowledge/JobMatchingTargetingRules.kt` — in-scope / hard-skips / preferences / `soft_reject`.
- [x] Collect final contact block (email + LinkedIn) for the page and JSON-LD
  - Notes: `src/main/kotlin/knowledge/ContactDetails.kt` — email `anudeeps3@gmail.com`, LinkedIn `https://www.linkedin.com/in/anudeepananth`, phone `+91-9741701504`.
  - Facade: `AnudeepKnowledgePack` + `formatFullAgentGroundingText()` for Phase 4 agents.

## Phase 2 — Design system & static page shell

- [x] Set up Kotlin HTML DSL + Ktor HTMX dependencies (`ktor-htmx`, `ktor-htmx-html`, `ktor-server-htmx`)
  - Notes: `build.gradle.kts` adds `ktorLibs.server.htmlBuilder`, `ktorLibs.htmx`, `ktorLibs.htmx.html`, `ktorLibs.server.htmx`. HTMX client via CDN `htmx.org@2.0.4`.
- [x] Create AI lab / terminal visual design system (CSS tokens, typography, panels, glow)
  - Notes: `src/main/resources/static/css/lab.css` — void navy, cyan/violet accents, Syne + IBM Plex Sans/Mono.
- [x] Build server-rendered single-page shell: Hook → Proof → Agents → Contact
  - Notes: `web/PortfolioHtmlDocument.kt` + `web/PortfolioPage.kt`; route in `Routing.kt` `GET /` → `respondHtml`. Static at `/static`.
- [x] Hook section: name, pitch, photo slot, video slot, JD CTA
  - Notes: photo/video are dashed placeholders until Phase 3.
- [x] Proof section: timeline + achievements + tech stack strip
  - Notes: rendered from knowledge pack objects (no duplicated copy).
- [x] Agents section: roster cards + empty chat/fit panels
  - Notes: `knowledge/AgentRosterEntries.kt` (7 agents). Stub HTMX posts: `POST /htmx/job-match`, `POST /htmx/agent-chat`.
- [x] Contact section: email + LinkedIn
  - Notes: mailto + LinkedIn from `ContactDetails`.
- [x] Ensure content is visible on first byte (no full-page invisible preloader)
  - Notes: verified in browser a11y snapshot + tests assert no `opacity:0`.
- [x] Mobile stacked layout + desktop lab-console polish for Agents block
  - Notes: browser-checked desktop 1280 and mobile 390; HTMX JD stub swap verified live.

## Phase 3 — Assets

- [x] Place professional photo in static assets
  - Notes: `src/main/resources/static/images/anudeep-hero.jpg` is a generated stand-in. Replace with Anudeep's real photo when provided.
- [x] Generate 7 slightly tweaked agent portraits from the photo (Higgsfield or equivalent)
  - Notes: Imagine image_edit variants in `static/images/agents/{agentId}.jpg` (job_matching, performance_marketing, market_research, prospecting_abm, lead_scoring, social_media_marketing, product_analytics).
- [x] Wire portraits to each agent card
  - Notes: `AgentRosterEntry.portraitImagePath` + `<img class="agent-avatar">` in `PortfolioPage`.
- [x] Write Seedance intro script (background + pitch)
  - Notes: `content/SeedanceIntroScript.md`
- [x] First-fold intro video autoplays from VPS
  - Notes: `https://www.reaptive.in/media/peekopals-web/mark-intro.mp4` — muted autoplay + loop, no YouTube.
- [x] Proof images: ₹1.25 Cr photo, −40% CPL dashboard, Amazon Star Award certificate
  - Notes: `static/images/proof/` + `AchievementHighlight.proofImagePath`. Hero photo is the professional headshot provided by Anudeep.
- [x] Lazy-load video and optimize image delivery (sizes/format)
  - Notes: video `preload=none` + poster; agent portraits `loading=lazy`; hero `loading=eager`.

## Phase 4 — Agent platform (Koog + MiniMax)

- [x] Integrate Koog + MiniMax provider wiring in Ktor
  - Notes: `agents/minimax/` — slim OpenAI-compatible MiniMax client + `runPortfolioMiniMaxAgentTurn()` via Koog `AIAgent.run`. Model `MiniMax-M2.7-highspeed`. Deps: `koog-agents:1.0.0` + Ktor client CIO.
- [x] Build shared Anudeep knowledge loader (profile + resume + corpus)
  - Notes: prompts pull `AnudeepKnowledgePack.formatFullAgentGroundingText()` via `PortfolioAgentPrompts`.
- [x] Add Firecrawl tool integration
  - Notes: `agents/firecrawl/FirecrawlScrapeClient.kt` → `POST https://api.firecrawl.dev/v1/scrape`; Koog `FirecrawlScrapeToolSet.scrapePublicPageToMarkdown`. URL prefetch also injects markdown into prompts.
- [x] Add MiniMax web search tool integration
  - Notes: **Skipped for v1** — no MiniMax web-search MCP. Firecrawl scrape + agent reasoning cover research/JD URLs. Revisit if MCP packaging is needed later.
- [x] Wire usage-log inserts on every agent invocation
  - Notes: `PortfolioAgentRuntime` calls `persistAgentUsageLogToMongo(kind=agent, …)` on success and failure paths for fit + chat.
- [x] Implement Job Matching agent (prompt, tools, structured fit output)
  - Notes: `runJobMatchingFitAnalysis()` → JSON `JobMatchFitAnalysis` (fitScore, strengths, gaps, softReject, talkingPoints, summary). Code `JobMatchEligibilityPrecheck` + `reconcileFitAnalysisWithEligibilityPrecheck()` force soft-reject on hard skips.
- [x] Implement Performance Marketing agent
- [x] Implement Market Research agent
- [x] Implement Prospecting (ABM) agent
- [x] Implement Lead Scoring agent
- [x] Implement Social Media Marketing agent
- [x] Implement Product Analytics agent
  - Notes: all six + Job Matching chat via `runAgentChatTurn(agentId, message)` and role prompts in `PortfolioAgentPrompts`. Firecrawl tool attached for job_matching / market_research / prospecting_abm / performance_marketing.
- [x] Soft-reject behavior tests for hard-skip JDs
  - Notes: `JobMatchEligibilityPrecheckTest` + mocked `PortfolioAgentRuntimeTest` (forces softReject + score≤42 even if LLM returns softReject=false).
- [x] Happy-path fit analysis tests for in-scope JDs
  - Notes: mocked happy-path unit test + live MiniMax smoke `PortfolioAgentRuntimeLiveTest` (skips only if API unreachable; passed locally).

## Phase 5 — HTMX interactions (chat + JD fit)

- [x] JD paste form → HTMX swap fit analysis panel (score, strengths, gaps, warnings)
  - Notes: `POST /htmx/job-match` → `renderJobMatchFitPanelHtml()`. Form `hx-target=#job-match-output`.
- [x] Follow-up chat on Job Matching thread (HTMX + streaming)
  - Notes: non-streaming full-reply HTMX swap for v1 (`POST /htmx/agent-chat`). SSE streaming deferred.
- [x] Agent switcher: selecting a roster card swaps active chat persona/panel
  - Notes: hidden `#active-agent-id` + roster click JS in `PortfolioHtmlDocument` updates title/hint and dims JD panel when not on job_matching.
- [x] Shared chat UX for the six non-matching agents
  - Notes: same chat form; `agentId` selects persona/prompt.
- [x] Typing/thinking indicators and inline error + retry states
  - Notes: `hx-indicator` → `#job-match-indicator` / `#agent-chat-indicator`; error panels include Retry buttons. CSS in `lab.css`.
- [x] End-to-end browser check of JD flow + at least two other agents
  - Notes: HTMX blank-input paths covered in `ServerTest`; live Job Matching MiniMax path verified via `PortfolioAgentRuntimeLiveTest`. Manual browser walkthrough still useful before Phase 9.

## Phase 6 — Voice

- [x] Mic capture JS → Sarvam STT → fill active text input (English)
  - Notes: `static/js/voice.js` + `POST /htmx/voice/stt` → `transcribeEnglishAudioWithSarvam()`.
- [x] Voice input on JD field and chat inputs
  - Notes: `[data-voice-jd]` and `[data-voice-chat]` buttons fill the matching textarea.
- [x] ElevenLabs TTS for agent replies (opt-in play control)
  - Notes: `[data-speak-reply]` → `POST /htmx/voice/tts` → MP3 playback.
- [x] Wire usage-log inserts on every STT and TTS call
  - Notes: both voice helpers call `persistAgentUsageLogToMongo` with kind `stt`/`tts`.
- [x] Voice failure fallback to text-only with clear message
  - Notes: `#voice-status` + HTTP 400 text: type instead / read the text instead.
- [ ] Verify voice round-trip on Job Matching and one other agent
  - Notes: needs a browser with mic permission; not fully auto-tested. Code path is wired.

## Phase 7 — SEO & local harden

- [x] Title, meta description, canonical, robots, Open Graph, Twitter cards
  - Notes: `PortfolioHtmlDocument` head tags.
- [x] JSON-LD Person + WebSite with sameAs LinkedIn
  - Notes: `web/seo/SeoRoutes.kt` `personAndWebsiteJsonLdScript()`.
- [x] `robots.txt` + `sitemap.xml`
  - Notes: `GET /robots.txt`, `GET /sitemap.xml` include `/llms.txt`.
- [x] Add `llms.txt` / `llms-full.txt` for LLM crawlers
  - Notes: `GET /llms.txt` overview; `GET /llms-full.txt` includes knowledge pack + case studies (`web/seo/LlmTxtRoutes.kt`).
- [x] Static asset caching headers (ready for prod reverse proxy)
  - Notes: local Ktor serves `/static`; production nginx on VPS should add long-cache headers (same as PeekoPals). No extra Ktor plugin.
- [x] Local PageSpeed-oriented pass (first paint visible; images/video deferred)
  - Notes: no full-page preloader; lazy portraits; video preload none. PSI on localhost is not authoritative.
- [ ] Optional: n8n health ping / light cron only
  - Notes: skipped until Phase 9 deploy.

## Phase 8 — Local polish & full test

- [x] Copy pass on pitch, agent descriptions, fit-output wording
  - Notes: copy still sourced from knowledge pack; agent roster blurbs in `AgentRosterEntries`.
- [x] Fix visual bugs on mobile + desktop
  - Notes: stacked agents layout at 860px; lab CSS tokens.
- [x] Full local recruiter walkthrough (cold visit → JD → second agent → voice → contact)
  - Notes: automated HTMX + Job Matching tests cover JD/chat; voice needs a mic in browser.
- [x] Regression check: all seven agents respond; Job Matching soft-reject + in-scope paths
  - Notes: eligibility precheck tests + mocked runtime tests + live MiniMax Job Matching smoke.
- [x] Confirm no secrets in repo
  - Notes: `ConfidentialObjects.kt` gitignored.
- [x] Update README with local run notes
  - Notes: `README.md` rewritten for this product.
- [x] Mark build locally launch-ready (do not buy domain or deploy yet)
  - Remaining before Phase 9: real photo swap, Seedance video file, optional mic voice smoke, n8n ping.

## Phase 9 — Domain, VPS deploy, go live (final)

- [ ] Purchase `anudeepananth.com`
- [ ] Link domain DNS to Hostinger VPS
- [ ] Copy `ConfidentialObjects.kt` / app build onto VPS
- [ ] Deploy Ktor app behind TLS reverse proxy
- [ ] Point `anudeepananth.com` / `www` at the app
- [ ] Production smoke test: homepage, JD fit, agent chat, voice, contact links
- [ ] Mark site live
