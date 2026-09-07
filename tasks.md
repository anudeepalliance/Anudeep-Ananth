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

- [ ] Place professional photo in static assets
- [ ] Generate 7 slightly tweaked agent portraits from the photo (Higgsfield or equivalent)
- [ ] Wire portraits to each agent card
- [ ] Write Seedance intro script (background + pitch)
- [ ] Generate Seedance intro video and host it statically
- [ ] Lazy-load video and optimize image delivery (sizes/format)

## Phase 4 — Agent platform (Koog + MiniMax)

- [ ] Integrate Koog + MiniMax provider wiring in Ktor
- [ ] Build shared Anudeep knowledge loader (profile + resume + corpus)
  - Hint: start from `AnudeepKnowledgePack.formatFullAgentGroundingText()`.
- [ ] Add Firecrawl tool integration
- [ ] Add MiniMax web search tool integration
- [ ] Wire usage-log inserts on every agent invocation
- [ ] Implement Job Matching agent (prompt, tools, structured fit output)
- [ ] Implement Performance Marketing agent
- [ ] Implement Market Research agent
- [ ] Implement Prospecting (ABM) agent
- [ ] Implement Lead Scoring agent
- [ ] Implement Social Media Marketing agent
- [ ] Implement Product Analytics agent
- [ ] Soft-reject behavior tests for hard-skip JDs
- [ ] Happy-path fit analysis tests for in-scope JDs

## Phase 5 — HTMX interactions (chat + JD fit)

- [ ] JD paste form → HTMX swap fit analysis panel (score, strengths, gaps, warnings)
- [ ] Follow-up chat on Job Matching thread (HTMX + streaming)
- [ ] Agent switcher: selecting a roster card swaps active chat persona/panel
- [ ] Shared chat UX for the six non-matching agents
- [ ] Typing/thinking indicators and inline error + retry states
- [ ] End-to-end browser check of JD flow + at least two other agents

## Phase 6 — Voice

- [ ] Mic capture JS → Sarvam STT → fill active text input (English)
- [ ] Voice input on JD field and chat inputs
- [ ] ElevenLabs TTS for agent replies (opt-in play control)
- [ ] Wire usage-log inserts on every STT and TTS call
- [ ] Voice failure fallback to text-only with clear message
- [ ] Verify voice round-trip on Job Matching and one other agent

## Phase 7 — SEO & local harden

- [ ] Title, meta description, canonical, robots, Open Graph, Twitter cards
- [ ] JSON-LD Person + WebSite with sameAs LinkedIn
- [ ] `robots.txt` + `sitemap.xml`
- [ ] Static asset caching headers (ready for prod reverse proxy)
- [ ] Local PageSpeed-oriented pass (first paint visible; images/video deferred)
- [ ] Optional: n8n health ping / light cron only

## Phase 8 — Local polish & full test

- [ ] Copy pass on pitch, agent descriptions, fit-output wording
- [ ] Fix visual bugs on mobile + desktop
- [ ] Full local recruiter walkthrough (cold visit → JD → second agent → voice → contact)
- [ ] Regression check: all seven agents respond; Job Matching soft-reject + in-scope paths
- [ ] Confirm no secrets in repo
- [ ] Update README with local run notes
- [ ] Mark build locally launch-ready (do not buy domain or deploy yet)

## Phase 9 — Domain, VPS deploy, go live (final)

- [ ] Purchase `anudeepananth.com`
- [ ] Link domain DNS to Hostinger VPS
- [ ] Copy `ConfidentialObjects.kt` / app build onto VPS
- [ ] Deploy Ktor app behind TLS reverse proxy
- [ ] Point `anudeepananth.com` / `www` at the app
- [ ] Production smoke test: homepage, JD fit, agent chat, voice, contact links
- [ ] Mark site live
