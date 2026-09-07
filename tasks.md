# Tasks — anudeepananth.com

Tracker for building the portfolio agent site. Run one task (or a tight group) at a time in Grok Build. Check items off when done.

Design reference: `docs/superpowers/specs/2026-09-07-portfolio-agent-site-design.md`  
Project rules: `CLAUDE.md`

---

## Phase 0 — Foundations

- [x] Init git repo, add `.gitignore` (`.gradle`, `build`, `.idea`, `.superpowers`, `.env*`, secrets)
- [ ] Confirm local `./gradlew run` still serves the Ktor hello-world baseline
- [ ] Add env/secrets pattern for MiniMax, Sarvam, ElevenLabs, Firecrawl (local + VPS-ready); keep `ConfidentialObjects.kt` gitignored
- [ ] Rotate any keys that were ever committed or pasted into chat, then update local secrets only
- [ ] Purchase/connect `anudeepananth.com` to Hostinger VPS when ready (can finish later in Phase 7)
- [ ] Add usage logging hooks plan (no rate limits, but log agent/voice calls)

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
- [ ] Voice failure fallback to text-only with clear message
- [ ] Verify voice round-trip on Job Matching and one other agent

## Phase 7 — SEO, deploy, harden

- [ ] Title, meta description, canonical, robots, Open Graph, Twitter cards
- [ ] JSON-LD Person + WebSite with sameAs LinkedIn
- [ ] `robots.txt` + `sitemap.xml`
- [ ] Deploy Ktor app to Hostinger VPS behind TLS reverse proxy
- [ ] Point `anudeepananth.com` / `www` at the VPS
- [ ] Static asset caching headers
- [ ] Smoke test production: homepage, JD fit, agent chat, voice, contact links
- [ ] PageSpeed pass review (fix paint visible; images/video deferred)
- [ ] Optional: n8n health ping / light cron only

## Phase 8 — Polish & launch

- [ ] Copy pass on pitch, agent descriptions, fit-output wording
- [ ] Recruiter walkthrough dry run (cold visit → JD → second agent → contact)
- [ ] Fix visual bugs on mobile + desktop
- [ ] Confirm no secrets in repo
- [ ] Mark site launch-ready and update README with run/deploy notes
