# projects.anudeepananth.com

A showcase site for interview assignments, built with **Compose Multiplatform (Kotlin/Wasm)** on the front end and
**Ktor** on the back end.

The first project is the **Ocrolus GTM Engineer take-home**. The app performs the whole assignment end to end with AI,
shows each step live in the browser, saves every run to MongoDB, and announces runs on Discord through n8n.

Live at `https://projects.anudeepananth.com/ocrolus`.

## What one run does

The app repeats what I did by hand for the take-home. The maths runs in Kotlin; the model only does judgement and
writing.

| #  | Step                       | Done by      | What happens                                                                     |
|----|----------------------------|--------------|----------------------------------------------------------------------------------|
| 1  | `load-accounts`            | code         | Read the 50 accounts from MongoDB                                                |
| 2  | `score-csm-notes`          | model        | Prompt 1.1: score each CSM note 0–10 per add-on not yet adopted, with evidence   |
| 3  | `flag-at-risk-accounts`    | model        | Prompt 1.2: Y/N risk flag with quoted evidence                                   |
| 4  | `compute-priorities`       | code         | Intent × Health × Size per product, exclusions, product totals                   |
| 5  | `pick-product`             | code + model | Code picks the highest-total product; the model writes the rationale             |
| 6  | `rank-top-accounts`        | code         | Rank, break ties by volume, take the top 15                                      |
| 7  | `check-anomalies`          | model        | Prompt 1.3: anomaly score and reason, flag for manual review at 5+               |
| 8  | `outreach-strategy`        | model        | Prompt 2.1 for each of the top 3                                                 |
| 9  | `outreach-draft`           | model        | Prompt 2.2 for each of the top 3                                                 |
| 10 | `outreach-review`          | model        | Prompt 2.3 five-pass review, with a change log                                   |
| 11 | `summarise-ai-corrections` | model        | One concrete thing the AI got wrong and how the review caught it                 |
| 12 | `measurement-plan`         | model        | Part 3 of the brief in 3–5 sentences                                             |
| 13 | `save-and-notify`          | code         | Save the result and send the completed event to n8n                              |

Every model step asks for JSON, validates it against a typed class, retries once with the validation error, then falls
back to the next model. Hard caps from the brief (email length, sentence counts) are enforced in code.

## Scoring

```
priority = 100 × intent × health × size        (rounded to 1 decimal)

intent = note score / 10
health = 0.6 × min(NPS, 70)/70 + 0.4 × (QBR part + renewal part)/2
size   = monthly volume / largest monthly volume
```

An account scores 0 for a product if it already uses it, is flagged at risk, has NPS under 35, renews in under 60 days,
or its note score is 0. Full rules with spreadsheet cell references: [`docs/ocrolus-scoring-rules.md`](docs/ocrolus-scoring-rules.md).

A golden test checks the Kotlin engine against every row of my scoring sheet.

## Stack

| Module       | What it is                                                                        |
|--------------|-----------------------------------------------------------------------------------|
| `shared`     | Kotlin Multiplatform (JVM + wasmJs) data models and API paths                     |
| `server`     | Ktor on JVM 21. Serves `/api/ocrolus/...` and the built web app                   |
| `composeApp` | Compose Multiplatform web UI (Kotlin/Wasm)                                        |

- **AI:** [Koog](https://github.com/JetBrains/koog) with OpenRouter. Public runs use free models only; paid models need
  an admin passcode
- **Data:** MongoDB, collections `ocrolus-accounts`, `ocrolus-assignment-runs`, `ocrolus-model-call-logs`
- **Live progress:** server-sent events
- **Notifications:** n8n webhook → Discord on run started, completed and failed
- **Deploy:** Docker behind nginx on a VPS

## API

| Method   | Path                                  | Purpose                                       |
|----------|---------------------------------------|-----------------------------------------------|
| `GET`    | `/api/ocrolus/assignment`             | Steps, available models, active run, quota    |
| `POST`   | `/api/ocrolus/runs`                   | Start a run                                   |
| `GET`    | `/api/ocrolus/runs`                   | List saved runs                               |
| `GET`    | `/api/ocrolus/runs/{runId}`           | One run with its full result                  |
| `GET`    | `/api/ocrolus/runs/{runId}/...`       | Model call logs and the live event stream     |
| `DELETE` | `/api/ocrolus/runs/{runId}`           | Delete a run (admin)                          |

## Run it locally

Run Gradle from the repo root.

```bash
./gradlew :projects:server:run                               # API + web app on http://localhost:9494
./gradlew :projects:composeApp:wasmJsBrowserDevelopmentRun   # UI dev server on :9495, proxies /api to :9494
./gradlew :projects:composeApp:wasmJsBrowserDistribution     # build the web bundle the server serves
./gradlew :projects:server:test                              # scoring golden test + pipeline tests (no network)
```

Secrets (OpenRouter keys, MongoDB connection strings, n8n webhook, admin passcode) live in a gitignored
`server/src/main/kotlin/com/anudeepananth/projects/util/ConfidentialObjects.kt`.

## Project layout

```
projects/
├── composeApp/   Compose web UI: live run tracker, results, saved runs
├── server/       Ktor API, run pipeline, scoring engine, prompts
│   └── src/main/resources/prompts/ocrolus/   the take-home prompts (1.1–2.3) plus helper prompts
├── shared/       models and API paths used by both sides
├── docs/         build plan and scoring rules
├── deploy/       Dockerfile
├── nginx/        site config
└── scripts/      seed accounts, deploy
```

The assignment brief and dataset are not in this repo.
