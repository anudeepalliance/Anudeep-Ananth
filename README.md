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
- Share image: `/static/images/og-share.jpg` (1200×630)
- `POST /htmx/job-match`, `POST /htmx/agent-chat`
- `POST /htmx/voice/stt`, `POST /htmx/voice/tts`

## Production (Hostinger VPS)

Same box as PeekoPals. Cloudflare in front, `shared-nginx` reverse-proxies to a Docker container on `baedrop_default`.

```bash
./scripts/deploy-vps-build.sh
```

That rsyncs this checkout (including gitignored `ConfidentialObjects.kt`), builds the JVM 21 image on the VPS, and installs the `www.anudeepananth.com` nginx vhost.

Cloudflare DNS must be:

- `A` `@` → `72.60.219.67` (proxied)
- `A` `www` → `72.60.219.67` (proxied)
- SSL/TLS mode **Full** (not Full Strict)

## Notes

- Intro video: script in `content/SeedanceIntroScript.md` (generation blocked under ZDR; poster uses hero still).
- Hero still is a generated stand-in until the real professional photo is dropped in `src/main/resources/static/images/anudeep-hero.jpg`.
