# Tasks — anudeepananth.com

Tracker for building the portfolio agent site. Run one task (or a tight group) at a time in Grok Build. Check items off when done.

Design reference: `docs/superpowers/specs/2026-09-07-portfolio-agent-site-design.md`  
Project rules: `CLAUDE.md`

Order rule: build and fully test locally first. Purchase domain, link DNS, and deploy to VPS only in Phase 9.

---

## Phase 0 — Foundations

- [x] Init git repo, add `.gitignore` (`.gradle`, `build`, `.idea`, `.superpowers`, `ConfidentialObjects.kt`)
- [ ] Confirm local `./gradlew run` still serves the Ktor hello-world baseline
- [ ] Confirm `ConfidentialObjects.kt` stays gitignored and is present locally; wire new keys there only (no env/secrets pattern)
- [ ] Add VPS community Mongo connection strings to `ConfidentialObjects.kt` (in-VPS + local SSH-tunnel URIs; dedicated `anudeepananth` DB)
- [ ] Port a slim PeekoPals-style Mongo helper (client singleton + collections + kotlinx codecs; hostname/tunnel pick like PeekoPals)
- [ ] Define usage-event data class + `agent_usage_logs` collection
- [ ] Implement insert helper for agent / STT / TTS usage (never fail the user request if logging fails)

## Phase 1 — Content & knowledge pack

- [ ] Extract structured Anudeep profile from resume (roles, skills, metrics, preferences, hard skips)
- [ ] Write short homepage sales pitch paragraph
- [ ] Draft experience timeline content (visual-ready milestones)
- [ ] Draft achievements content (ARR, ROAS, CAC, pipeline, etc.)
- [ ] Write 2–4 case-study / campaign corpus entries agents can retrieve
- [ ] Encode Job Matching targeting rules as structured config the agent reads
- [ ] Collect final contact block (email + LinkedIn) for the page and JSON-LD

## Phase 2 — Design system & static page shell

- [ ] Set up Kotlin HTML DSL + Ktor HTMX dependencies (`ktor-htmx`, `ktor-htmx-html`, `ktor-server-htmx`)
- [ ] Create AI lab / terminal visual design system (CSS tokens, typography, panels, glow)
- [ ] Build server-rendered single-page shell: Hook → Proof → Agents → Contact
- [ ] Hook section: name, pitch, photo slot, video slot, JD CTA
- [ ] Proof section: timeline + achievements + tech stack strip
- [ ] Agents section: roster cards + empty chat/fit panels
- [ ] Contact section: email + LinkedIn
- [ ] Ensure content is visible on first byte (no full-page invisible preloader)
- [ ] Mobile stacked layout + desktop lab-console polish for Agents block

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
