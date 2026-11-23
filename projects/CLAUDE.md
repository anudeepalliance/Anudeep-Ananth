# CLAUDE.md: projects/ module (projects.anudeepananth.com)

Interview-assignment showcase site. Lives in the `projects/` folder of the Anudeep-Ananth repo as three Gradle modules;
run Gradle from the repo root. First project: the Ocrolus GTM Engineer take-home, performed end to end by AI with live
progress, saved to MongoDB, announced on Discord via n8n.

Build plan and status: `docs/ocrolus-assignment-automation-plan.md`. Scoring maths: `docs/ocrolus-scoring-rules.md`.

## Modules

- `shared`: Kotlin Multiplatform (jvm + wasmJs) data models and API paths (`OcrolusApiPaths`)
- `server`: Ktor (JVM 21). Serves `/api/ocrolus/...` and the built web app. Koog 1.3 + OpenRouter for model calls
- `composeApp`: Compose Multiplatform web (Kotlin/Wasm) UI

## Commands

```bash
./gradlew :projects:server:run                               # server + API on http://localhost:9494 (serves the last built web bundle)
./gradlew :projects:composeApp:wasmJsBrowserDevelopmentRun   # UI dev server on :9495, proxies /api to :9494
./gradlew :projects:composeApp:wasmJsBrowserDistribution     # build the web bundle the server serves
./gradlew :projects:server:test                              # scoring golden test + pipeline tests (no network)
projects/scripts/seed-ocrolus-accounts.sh "<dataset.xlsx>" # load the 50 accounts into MongoDB (needs SSH tunnel)
projects/scripts/deploy-vps.sh                             # build + deploy (needs projects/scripts/deploy.env)
```

Local MongoDB access is through the SSH tunnel to the VPS (see the portfolio repo README).

## Data

MongoDB database `Anudeep-Ananth` (community edition on the VPS). Collections are prefixed per company:
`ocrolus-accounts`, `ocrolus-assignment-runs`, `ocrolus-model-call-logs`. Names live in `util/mongoDB/Collections.kt`.
Never hardcode field names; use `DataClass::property.name`.

## Rules

- Secrets live in `server/.../util/ConfidentialObjects.kt` (gitignored). Copy `ConfidentialObjects.kt.example`. Never
  commit secrets, VPS details (`scripts/deploy.env`), or the Ocrolus brief/dataset (`*.xlsx`, `*.docx`)
- Prompts live in `server/src/main/resources/prompts/ocrolus/`. Prompts 1.1–2.3 are Anudeep's originals, word for word,
  plus a JSON output block
- The maths stays in code (`OcrolusPriorityScoring`); the model only does judgement and writing
- Public runs use free OpenRouter models only. Paid models need `OPEN_ROUTER_PAID_MODELS_KEY` and the admin passcode
- Follow `~/.claude/CLAUDE.md` code style
