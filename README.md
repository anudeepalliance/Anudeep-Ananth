# anudeepananth.com

Recruiter-facing portfolio for **Anudeep Ananth**: a single-page AI-lab site with live Koog + MiniMax agents.

## Local run

```bash
# Optional: PeekoPals-style Mongo tunnel (usage logs)
ssh -L 27017:localhost:27017 -p 1379 reaptive@72.60.219.67 -N

./gradlew run
```

Open http://127.0.0.1:8080

- `./gradlew test` — unit + HTMX + Job Matching tests
- Secrets: `src/main/kotlin/util/ConfidentialObjects.kt` (gitignored)
- Tracker: `tasks.md`
- Rules: `CLAUDE.md`

## Surfaces

- `/` — Hook → Achievements → Job Matching → Agents → Stack → Contact (visible HTML on first byte)
- `/llms.txt` and `/llms-full.txt` — LLM/Google-AI crawlable overviews (llmstxt.org)
- `/robots.txt`, `/sitemap.xml`
- Share image: `/static/images/og-cover.jpg` (1200×630)
- `POST /htmx/job-match`, `POST /htmx/agent-chat`
- `POST /htmx/voice/stt`, `POST /htmx/voice/tts`

## Notes

- Domain purchase + VPS deploy is **Phase 9 only** after local sign-off.
- Intro video: script in `content/SeedanceIntroScript.md` (generation blocked under ZDR; poster uses hero still).
- Hero still is a generated stand-in until the real professional photo is dropped in `src/main/resources/static/images/anudeep-hero.jpg`.
