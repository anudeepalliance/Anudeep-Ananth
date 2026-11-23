# Ocrolus assignment automation — build plan

**Goal:** a web app at `https://projects.anudeepananth.com/ocrolus` that performs Anudeep's Ocrolus take-home assignment
end to end with AI, shows the work happening in real time, saves every run to MongoDB, and announces runs on Discord
through n8n.

**Deadline:** interview is **24 Nov 2026, 7:30 pm IST**. Everything in phases 0–9 must be live and verified well before
that. Phase 10 is stretch.

**Who executes this:** an autonomous Claude session working in `/Users/anudeepmac/IntelliJ/Anudeep-Ananth/projects` (run
Gradle from the repo root). Read the whole file first, then work phase by phase.

---

## How to execute this plan

1. Work through the phases in order. Tick each checkbox (`- [x]`) in this file as soon as the item is verified.
2. Commit at the end of every phase (small, sentence-case commit messages).
3. Do not stop to ask the user unless you hit one of the items in "Inputs needed from Anudeep". If an input is missing,
   build everything around it with a placeholder constant, keep going, and list it in your final report.
4. Every phase ends with a "verify" item. Do not move on until it passes. If something cannot be verified, say so
   plainly in the final report.
5. Follow the code style in `~/.claude/CLAUDE.md` (long descriptive names, KDoc for an intern, lambda bodies on their
   own line, no hardcoded Mongo field names — use `DataClass::property.name`).
6. When a library API differs from what this plan assumes, check the real version in the Gradle cache or docs and adapt.
   The plan describes intent, not exact syntax.
7. If you are running short of time, cut from the bottom: phase 10 first, then live token streaming, then the admin
   edit/delete UI (keep the API). Never cut phases 1–6.

## Skills and sub-agents to use

These are already copied into this repo's `.claude/` folder. They were written for other projects (PeekoPals, AIAstro),
so follow their rules and patterns but ignore paths and names that only exist in those projects.

| Tool                        | Type      | Use it for                                                                                                                                                                                                                                                                                    |
|-----------------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `koog-logic-creator`        | skill     | **Load before phase 4.** The model pipeline is built with Koog (same `koog-agents` version as the portfolio repo), using its OpenRouter client and typed structured output. Read its `docs/llm-providers.md`, `docs/structured-output.md`, `docs/graph-based-agents.md` and `docs/testing.md` |
| `adp-code-structure-style`  | skill     | **Load before phase 1.** Feature-based folders, shared-module placement, server route and ViewModel conventions                                                                                                                                                                               |
| `kmp-business-logic-expert` | sub-agent | Phase 1–2 shared data models, repositories, and the web app's ViewModels and API client                                                                                                                                                                                                       |
| `compose-ui-designer`       | sub-agent | All of phase 6. It requires a design system (shared dimensions, text and button composables, spacing helper, theme colours only), so phase 6 starts by creating one                                                                                                                           |
| `kmp-error-resolver`        | sub-agent | Any build or runtime error that is not fixed in two attempts                                                                                                                                                                                                                                  |
| `kmp-code-reviewer`         | sub-agent | A review at the end of phases 3, 5 and 6, and before the GitHub push. Fix what it confirms; note what you reject and why                                                                                                                                                                      |

Sub-agents know nothing about this plan. Give each one the relevant section of this file, the file paths, and what
"done" looks like. Check their work yourself before ticking a box.

## Decisions already made (do not re-ask)

| Topic       | Decision                                                                                                                     |
|-------------|------------------------------------------------------------------------------------------------------------------------------|
| Repo        | `projects/` module inside the Anudeep-Ananth repo (Compose Multiplatform web + Ktor server)                                  |
| URL         | `projects.anudeepananth.com/ocrolus`, `/ocrolus/runs`, `/ocrolus/runs/{runId}`                                               |
| Database    | Existing `Anudeep-Ananth` database on the VPS community MongoDB. Collections prefixed `ocrolus-`                             |
| AI provider | OpenRouter, called through the Koog framework. Free models for public runs                                                   |
| Access      | Anyone can start a run with a free model. One run at a time, daily cap. Paid models (DeepSeek) need a private admin passcode |
| n8n         | The app calls an n8n webhook on run started / completed / failed. n8n posts to Discord                                       |
| GitHub      | Public repo, pushed by Anudeep from IntelliJ. The build makes it safe to publish and adds a build check                      |
| Local ports | Ktor server on **9494**. Webpack dev server on **9495**, proxying `/api` to 9494                                             |

## Inputs from Anudeep

Already provided:

- [x] **OpenRouter key for free models**: constant `OPEN_ROUTER_FREE_MODELS_ONLY` in the portfolio's
  `/Users/anudeepmac/IntelliJ/Anudeep-Ananth/src/main/kotlin/util/ConfidentialObjects.kt`. Copy the constant (same name)
  into this repo's `ConfidentialObjects.kt`. Never print its value
- [x] **n8n**: set the workflow up yourself over SSH on the VPS (see phase 7). No n8n API key needed
- [x] **n8n webhook path**: `trigger-ocrolus-workflow`
- [x] **GitHub**: Anudeep pushes from IntelliJ himself. Do not create a repo or push

Still open (build around them with an empty constant, keep going, and report):

- [ ] **Discord webhook URL** — the `https://discord.com/api/webhooks/...` address from Discord (Server settings →
  Integrations → Webhooks). Goes in `ConfidentialObjects.DISCORD_WEBHOOK_URL` and into the n8n workflow at import time,
  never into git
- [ ] **Key for the paid DeepSeek run** — the key above only allows free models. Add `OPEN_ROUTER_PAID_MODELS_KEY`
  (empty until Anudeep fills it). Paid models stay hidden in the app while it is empty

## Source material (read before coding)

All in `/Users/anudeepmac/IntelliJ/Anudeep-Ananth/company-docs/ocrolus-assignment/`:

- `GTM- Project Brief (1).docx` — the assignment brief
- `Project Dataset (1).xlsx` — the 50 raw accounts (12 columns)
- `Ocrulus Assignment - Anudeep.docx` — Anudeep's submitted answer. **Its appendix holds the six real prompts (1.1, 1.2,
  1.3, 2.1, 2.2, 2.3). Use them verbatim.**
- `Ocrolus assignment data - Anudeep.xlsx` — his scoring sheet with formulas. **This is the source of truth for the
  scoring maths.** Open it with `openpyxl` twice: once normally to read the formulas, once with `data_only=True` to read
  the values.

Read docx files with `textutil -convert txt -stdout "<file>"`.

VPS and Mongo patterns to copy (do not reinvent):

- Portfolio repo `/Users/anudeepmac/IntelliJ/Anudeep-Ananth`: `src/main/kotlin/util/mongoDB/`, `util/usage/`,
  `util/ConfidentialObjects.kt` (Mongo connection strings — copy the two `MONGO_COMMUNITY_*` values), `Dockerfile`,
  `docker-compose.yml`, `nginx/anudeepananth.conf`, `scripts/`
- PeekoPals `/Users/anudeepmac/IntelliJ/Peekopals/PeekoPals/server/src/main/kotlin/com/peekopals/app/util/mongoDB/`
- VPS: SSH on port 1379, containers on docker network `baedrop_default`, Mongo container `peekopals-mongo`, n8n
  container `n8n-app`, reverse proxy container `shared-nginx` whose site configs live in docker volume `nginx-conf-vol`
- Local Mongo access is through an SSH tunnel (command in the portfolio `README.md`)

## What one run does

The app repeats what Anudeep did by hand. Maths is done in Kotlin; judgement is done by the model.

| #  | Step key                   | Done by      | What happens                                                                                                   |
|----|----------------------------|--------------|----------------------------------------------------------------------------------------------------------------|
| 1  | `load-accounts`            | code         | Read the 50 accounts from Mongo                                                                                |
| 2  | `score-csm-notes`          | model        | Prompt 1.1: score each note 0–10 per add-on not yet adopted, with quoted evidence                              |
| 3  | `flag-at-risk-accounts`    | model        | Prompt 1.2: Y/N risk flag with quoted evidence                                                                 |
| 4  | `compute-priorities`       | code         | Intent × Health × Size per product, exclusions, product totals                                                 |
| 5  | `pick-product`             | code + model | Code picks the product with the highest total. Model writes the short rationale from the computed numbers only |
| 6  | `rank-top-accounts`        | code         | Rank, break ties by volume, take the top 15                                                                    |
| 7  | `check-anomalies`          | model        | Prompt 1.3: anomaly score and reason, flag for manual review at 5+                                             |
| 8  | `outreach-strategy`        | model        | Prompt 2.1 for each of the top 3                                                                               |
| 9  | `outreach-draft`           | model        | Prompt 2.2 for each of the top 3                                                                               |
| 10 | `outreach-review`          | model        | Prompt 2.3 five-pass review; keep the change log                                                               |
| 11 | `summarise-ai-corrections` | model        | From the review change logs: one concrete thing the AI got wrong and how the review caught it                  |
| 12 | `measurement-plan`         | model        | Part 3 in 3–5 sentences                                                                                        |
| 13 | `save-and-notify`          | code         | Persist the result, send the completed event to n8n                                                            |

Rules for model steps:

- Ask for JSON only, validate it against a `@Serializable` class, and check every account id is present exactly once. On
  invalid output: one repair retry that includes the validation error, then fall back to the next model in the free
  list, then fail the step.
- Batch accounts (`ACCOUNTS_PER_MODEL_CALL = 25`, one constant) to stay inside free-tier limits.
- Enforce the brief's hard caps in code: emails ≤ 150 words, measurement 3–5 sentences. Over the cap → one retry asking
  to shorten.
- Retry on HTTP 429 and 5xx with backoff (2s, 5s, 15s). Respect `Retry-After`.
- Low temperature (0.2) for scoring steps, default for writing steps.

---

## Phase 0 — groundwork

- [ ] Read all source material above. Write `docs/ocrolus-scoring-rules.md`: the exact formula for intent, health (NPS
  part, timing part, caps), size, every exclusion rule, tie-breaks, and how `best_product`, `flagged_for_manual_review`
  and `overall_rank` are derived — taken from the spreadsheet formulas, with cell references
- [ ] Extract the six prompts verbatim into `server/src/main/resources/prompts/ocrolus/` (`1.1-csm-note-scoring.md`,
  `1.2-risk-flag.md`, `1.3-anomaly-check.md`, `2.1-outreach-strategy.md`, `2.2-outreach-draft.md`,
  `2.3-outreach-review.md`). Only add a JSON output-format block at the end of each; do not reword the body
- [ ] Write three new short prompts in the same folder: `product-rationale.md`, `ai-corrections-summary.md`,
  `measurement-plan.md`
- [ ] Create a `CLAUDE.md` in the repo root: what the project is, stack, ports, commands, where secrets live, and "never
  commit VPS connection details"
- [ ] Verify: the scoring rules doc reproduces by hand the priority of Palisade Home Loans (78.4) and Summit Ridge
  Mortgage (58.5)

## Phase 1 — project restructure

Turn the single-module static site into three modules.

- [ ] `shared` — Kotlin Multiplatform (`jvm` + `wasmJs`): `@Serializable` data classes and API path constants used by
  both sides
- [ ] `server` — Ktor (Netty) on the JVM, toolchain 17 (the Mac has JDK 17). Plugins: content negotiation (kotlinx
  json), status pages, call logging, server-sent events, compression
- [ ] `composeApp` — keep `wasmJs`, depend on `shared`, add Ktor client (JS engine) with content negotiation
- [ ] Server serves the built web bundle as static files, and returns `index.html` for any non-`/api` path so deep links
  like `/ocrolus/runs/abc` work on refresh
- [ ] Server port from the `PORT` environment variable, default **9494**
- [ ] Webpack dev server moved to **9495** with a proxy for `/api` → `http://localhost:9494` (keeps hot reload for UI
  work)
- [ ] `GET /health` returns 200
- [ ] Update `.claude/launch.json` with two entries: `projects-server` (9494) and `projects-web-dev` (9495)
- [ ] Verify: `./gradlew build` passes; server on 9494 serves the starter page and `/health`

## Phase 2 — secrets, MongoDB and data model

- [ ] `server/src/main/kotlin/com/anudeepananth/projects/util/ConfidentialObjects.kt` (gitignored) with:
  `OPEN_ROUTER_FREE_MODELS_ONLY`, `OPEN_ROUTER_PAID_MODELS_KEY`, `MONGO_COMMUNITY_VPS_CONNECTION_STRING`,
  `MONGO_COMMUNITY_TUNNELED_CONNECTION_STRING`, `N8N_RUN_EVENTS_WEBHOOK_URL`, `N8N_WEBHOOK_SHARED_SECRET` (generate),
  `DISCORD_WEBHOOK_URL`, `ADMIN_PASSCODE` (generate a random one). Copy the Mongo values from the portfolio's file
- [ ] Commit `ConfidentialObjects.kt.example` with the same constants and empty values, for the public repo and the
  build check
- [ ] Mongo client singleton + collection-name constants, copied from the portfolio's `util/mongoDB` pattern (in-VPS
  connection string in production, tunnelled one on the Mac)
- [ ] Collections in database `Anudeep-Ananth`:

**`ocrolus-accounts`** — the 50 input accounts. `_id` = account id (`A001`…)

```
accountId, accountName, vertical, annualRecurringRevenueUsd, monthlyProcessingVolume,
hasAdoptedFraudShield, hasAdoptedCashFlowInsights, hasAdoptedIncomeVerify,
monthsSinceLastQuarterlyBusinessReview, netPromoterScore, renewalDaysOut, customerSuccessManagerNote
```

**`ocrolus-assignment-runs`** — one document per run. `_id` = a short URL-safe id (ObjectId hex is fine)

```
runId, assignmentName ("Ocrolus GTM Engineer take-home"), status (QUEUED | RUNNING | COMPLETED | FAILED),
modelId, modelDisplayName, isFreeModel, triggeredBy (WEB | ADMIN),
startedAt, completedAt, durationMilliseconds,
isFeatured, displayLabel, presenterNotes,              // editable by admin
steps: [ { stepKey, title, status, startedAt, completedAt, progressMessages: [ { at, text } ] } ],
result: {
  chosenProduct, productPriorityTotals: { fraudShield, cashFlowInsights, incomeVerify },
  productRationale, scoringFormulaExplanation,
  scoredAccounts: [ 50 × { account fields, noteScores, noteScoreEvidence, isFlaggedAsRisk, riskEvidence,
                           priorities, bestProduct, anomalyScore, anomalyReason, isFlaggedForManualReview, overallRank } ],
  topAccountIds: [15],
  outreachMessages: [ 3 × { accountId, strategy, firstDraft, reviewChangeLog, finalSubject, finalBody, wordCount } ],
  aiCorrectionsSummary, measurementPlan
},
usage: { modelCallCount, promptTokens, completionTokens, fallbackModelIdsUsed },
errorMessage
```

**`ocrolus-model-call-logs`** — one document per model call, so the run page can show the real prompts

```
runId, stepKey, modelId, promptText, responseText, wasSuccessful, errorMessage,
durationMilliseconds, promptTokens, completionTokens, createdAt
```

- [ ] Indexes: runs on `completedAt` descending and on `status`; call logs on `runId`
- [ ] Seeding: `scripts/seed-ocrolus-accounts` (a Gradle task or small Kotlin main) that reads the raw dataset xlsx from
  the portfolio repo path and upserts the 50 accounts. **The xlsx and any JSON copy of it stay out of git**
- [ ] Repository functions with names that say they hit Mongo: insert run, fetch run by id, fetch run list (summary
  fields only, newest first), update run step/progress, update run editable fields, delete run and its call logs, insert
  call log, fetch call logs for run, fetch all accounts
- [ ] Verify: with the SSH tunnel open, seed runs and `mongosh` shows 50 documents in `ocrolus-accounts`; an integration
  test inserts, reads, updates and deletes a throwaway run

## Phase 3 — scoring engine (pure Kotlin, no AI)

- [ ] Implement priorities, exclusions, product totals, product pick, ranking and top 15 exactly per
  `docs/ocrolus-scoring-rules.md`. Pure functions, no I/O
- [ ] **Golden test:** export a fixture from Anudeep's scoring sheet (his three note-score columns and risk flags as
  input; his three priority columns, `best_product` and `overall_rank` as expected output). The engine must reproduce
  every priority to one decimal place and the same top 15 in the same order. Keep the fixture in
  `server/src/test/resources/` only if it holds no CSM note text or account names (ids and numbers only); otherwise load
  it from the portfolio path and skip the test when the file is absent
- [ ] Verify: `./gradlew :server:test` passes with the golden test running, not skipped

## Phase 4 — Koog, OpenRouter and model list

- [ ] Load the `koog-logic-creator` skill. Add Koog (match the version in the portfolio's `gradle/libs.versions.toml`)
  and set up its OpenRouter client with `OPEN_ROUTER_FREE_MODELS_ONLY` for free models and `OPEN_ROUTER_PAID_MODELS_KEY`
  for paid ones. See how the portfolio wires Koog in `/Users/anudeepmac/IntelliJ/Anudeep-Ananth/src/main/kotlin/agents/`
- [ ] Each model step is a Koog structured-output request returning a `@Serializable` class, with prompts loaded from
  the resource files. Model the 13 steps as a Koog graph strategy if that fits cleanly; otherwise a plain coroutine
  pipeline that calls Koog per step is fine. Keep prompts and step config out of route handlers
- [ ] Capture text, token usage and timing for every call. Streaming is only needed for phase 10
- [ ] **Time box:** if Koog's OpenRouter client or structured output is not working with a free model within 45 minutes,
  fall back to a small hand-written client for `POST https://openrouter.ai/api/v1/chat/completions` and say so in the
  final report
- [ ] **Do not trust remembered model ids.** Call `GET https://openrouter.ai/api/v1/models`, pick 3–4 current free
  models (ids ending `:free`, context ≥ 32k, good at JSON), and the current DeepSeek model Anudeep wants (he called it
  "DeepSeek v4.1 flash" — find the closest real id and note it in the final report)
- [ ] `OcrolusModelCatalogue` — one file listing allowed models: id, display name, isFree, order for fallback. Public
  runs may only use free ones; paid ones require `ADMIN_PASSCODE`
- [ ] JSON extraction helper (strip code fences and any reasoning tags) + validation + repair retry + fallback, as
  described in "Rules for model steps"
- [ ] Every call writes an `ocrolus-model-call-logs` document; logging failures never break the run
- [ ] Verify: a live smoke test scores 5 accounts with a free model and returns valid JSON (skipped automatically when
  the key is blank)

## Phase 5 — run pipeline, API and live progress

- [ ] `OcrolusAssignmentRunPipeline`: runs the 13 steps in a background coroutine, updates the run document after every
  step and progress message, and publishes each update to an in-memory `SharedFlow` keyed by run id
- [ ] Guards: only one run active at a time (a second request gets HTTP 409 with the active run id, so the visitor can
  watch it instead); `MAXIMUM_PUBLIC_RUNS_PER_DAY = 10`; free-model allowlist; a run stuck longer than 15 minutes is
  marked FAILED; on server start, mark any leftover RUNNING run as FAILED
- [ ] API (paths as constants in `shared`):

| Method | Path                                    | Purpose                                                                           |
|--------|-----------------------------------------|-----------------------------------------------------------------------------------|
| GET    | `/api/ocrolus/assignment`               | Brief summary, pipeline steps, model list, active run id if any, featured run id  |
| POST   | `/api/ocrolus/runs`                     | Start a run `{ modelId, adminPasscode? }` → `{ runId }`                           |
| GET    | `/api/ocrolus/runs`                     | List: name, status, model, completed date/time, duration, isFeatured              |
| GET    | `/api/ocrolus/runs/{runId}`             | Full run                                                                          |
| GET    | `/api/ocrolus/runs/{runId}/events`      | Server-sent events: replay saved progress, then live updates, heartbeat every 15s |
| GET    | `/api/ocrolus/runs/{runId}/model-calls` | Prompts and responses for the run                                                 |
| PATCH  | `/api/ocrolus/runs/{runId}`             | Edit `displayLabel`, `presenterNotes`, `isFeatured` (admin passcode header)       |
| DELETE | `/api/ocrolus/runs/{runId}`             | Delete run and its call logs (admin passcode header)                              |

- [ ] Pipeline test with a fake model client (canned JSON) covering: happy path, invalid JSON then repair, fallback
  model, step failure → run FAILED with message
- [ ] Verify: start a real run locally with a free model via `curl`, watch the events stream, and confirm the saved
  document has all three parts filled in. Note the duration and call count in the final report

## Phase 6 — the web app

Build with Material 3 and a custom theme (not the default purple). Editorial and calm: generous spacing, one accent
colour, clear type scale. Three layouts by width: under 600dp single column; 600–840dp roomier single column; over 840dp
two columns where noted. Check each screen at 375px and 1440px.

Things Compose web needs that are easy to miss:

- Wrap readable content in `SelectionContainer` so text can be selected, and add copy buttons for emails and prompts
- Add a lightweight HTML loading splash in `index.html` (name + spinner), removed when Compose starts — the first load
  is about 10 MB
- Keep the browser URL in sync and make back/forward and refresh work. Try navigation-compose's browser binding first;
  if it costs more than 30 minutes, hand-roll a tiny router on `window.location` and `history.pushState`
- Add `<meta name="robots" content="noindex">` — this is for interviewers, not search engines

Design system first (the `compose-ui-designer` agent's rules depend on it):

- [ ] `composeApp/.../design/`: theme (light colour scheme, type scale), `dimens.kt` (all spacing and sizes), text
  composables (title, heading, body, label), button composables (primary, secondary, text), `AddSpace()`, card, status
  chip, and a width-class helper for the three layouts. Screens use only these — no raw `Text()`, `Button()`, hardcoded
  dp or hex colours

Screens (delegate to `compose-ui-designer`, one screen per brief):

- [ ] **Home `/`** — short intro and a card linking to the Ocrolus project (room for future company projects)
- [ ] **Assignment `/ocrolus`** — before a run: assignment title, what Ocrolus asked for (Parts 1–3, **in our own words,
  not the brief pasted verbatim**), a diagram of the 13-step pipeline showing which steps are code and which are AI, a
  tip banner linking to the featured reference run, a model picker (free models; an "admin" expander reveals paid
  models + passcode field), and a prominent Start button. If a run is already active, show "A run is in progress — watch
  it live"
- [ ] **Assignment `/ocrolus` while running (same page)** — a step timeline with pending / running / done / failed
  states, the live progress messages under the running step, an elapsed timer, model call counter, and results appearing
  as they are ready (chosen product and totals → top 15 table → the three emails). On desktop: timeline left, results
  right. On completion: summary card and a button to the permanent result page. On failure: the error and a retry button
- [ ] **Completed runs `/ocrolus/runs`** — each row: assignment name, completion date and time (viewer's local time
  zone), model, time taken, status, featured badge. Table on desktop, cards on mobile. Empty state. Featured run pinned
  first
- [ ] **Run result `/ocrolus/runs/{runId}`** — header (model, date, duration, call count, share-link button); Part 1:
  product pick, rationale, formula, product totals as a simple bar comparison, top 15 table, expandable all-50 table
  with evidence quotes; Part 2: three emails with word counts, and per email the strategy and review change log, plus
  "what the AI got wrong"; Part 3: measurement plan; "Show the prompts" section listing the real prompts and responses
  from the call logs. If the run is still going, redirect to the live view. Admin expander: edit label/notes, mark
  featured, delete
- [ ] Not-found screen for unknown run ids and paths
- [ ] Verify in the browser at both widths: start a run from the UI, watch it finish, open it from the list, refresh the
  result page, use back/forward

## Phase 7 — n8n and Discord

- [ ] Server-side notifier: on run started, completed and failed, POST JSON to `N8N_RUN_EVENTS_WEBHOOK_URL` with header
  `X-Webhook-Secret`. Payload: `eventType`, `runId`, `runUrl`, `assignmentName`, `modelDisplayName`, `startedAt`, and
  for completed: `durationSeconds`, `chosenProduct`, top 3 account names with priorities, `modelCallCount`; for failed:
  `errorMessage`. Timeouts and errors are logged and never fail the run
- [ ] `n8n/ocrolus-run-notifications.json` — workflow: Webhook (POST, path `trigger-ocrolus-workflow`) → check the
  secret header → Switch on `eventType` → three Discord messages (started: model + live link; completed: summary +
  result link; failed: error). The Discord webhook URL is a placeholder in the committed file
- [ ] Get it into n8n over SSH: copy the workflow JSON (with the real Discord URL and secret substituted into a
  temporary copy, not the committed file) into the `n8n-app` container and import it with the n8n command-line tool
  (`n8n import:workflow`), then activate it (`n8n update:workflow --id=<id> --active=true`). Check `n8n --help` inside
  the container for the exact commands of the installed version. If activation needs a restart, **list the active
  workflows first and confirm they are active again afterwards** — other projects rely on this n8n. Delete the temporary
  copy. Write the steps into `n8n/README.md`
- [ ] In production, prefer the internal address `http://n8n-app:5678/webhook/trigger-ocrolus-workflow` (same docker
  network); check it resolves from the container, else use the public URL
- [ ] Verify: a run produces a "started" and a "completed" message in Discord. If the Discord URL is still empty, verify
  that `curl` to the n8n webhook returns 200 and the execution shows in n8n, and report the gap

## Phase 8 — deploy

- [ ] `deploy/Dockerfile`: a slim JRE 17 image that copies in the server fat jar and the web bundle, both built on the
  Mac. The VPS never runs Gradle
- [ ] `docker-compose.yml`: same container name `anudeep-projects`, network `baedrop_default`, `PORT=8585`, health check
  on `/health`, memory limit ~512m
- [ ] `nginx/projects-anudeepananth.conf`: upstream → `anudeep-projects:8585`; a dedicated location for the events
  stream with `proxy_buffering off`, `proxy_cache off`, `proxy_read_timeout 900s`; keep `nginx -t` before reload
- [ ] Move VPS connection details (host, user, SSH port, remote paths) out of `scripts/deploy-vps.sh` and `README.md`
  into a gitignored `scripts/deploy.env`, with a committed `scripts/deploy.env.example`. `ConfidentialObjects.kt` must
  be copied into the build before the jar is made, never into the image as source
- [ ] Deploy script: build jar + bundle → rsync → rebuild container → health check from inside `shared-nginx` → install
  vhost → test → reload. On a failed health check, keep the old container running
- [ ] Seed `ocrolus-accounts` in production (same seeding task through the SSH tunnel — it is the same database)
- [ ] Verify on the live URL: all four screens load, deep links survive refresh, a free-model run completes with live
  progress through Cloudflare, Discord messages arrive, `www.anudeepananth.com` and `peekopals.com` still return 200

## Phase 9 — make the repo safe to publish

Anudeep pushes to a public GitHub repo himself from IntelliJ. Your job is to make sure that is safe. **Do not create a
repo or push.**

- [ ] Secret sweep over the whole tree and git history: API keys, Mongo connection strings, the VPS IP, the SSH port,
  webhook URLs, the admin passcode. Confirm `.gitignore` covers `ConfidentialObjects.kt`, `scripts/deploy.env`, any
  dataset copies, `build/`, `.gradle/`, `.kotlin/`. If an earlier commit contains any of these, rewrite the local
  history before it is ever pushed
- [ ] `README.md`: what it is, a screenshot, an architecture diagram (browser → Cloudflare → nginx → Ktor → Koog →
  OpenRouter, plus MongoDB and n8n → Discord), the pipeline table, the data model, how to run locally, and an honest
  "what is automated vs. what was Anudeep's judgement" note
- [ ] `.github/workflows/build.yml`: on push, copy `ConfidentialObjects.kt.example` into place, then `./gradlew build`
  (tests needing secrets or Mongo skip themselves)
- [ ] Verify: a fresh `git clone` of the local repo into a temporary folder builds after copying the example secrets
  file, and a search of that clone finds no secrets, dataset or VPS details

## Phase 10 — stretch (only if phases 0–9 are done and verified)

- [ ] Stream tokens for the email-drafting step so the email types itself out live
- [ ] "Compare runs" view: two runs side by side (product pick, top-15 overlap, emails)
- [ ] Show how closely a run's top 15 matches Anudeep's hand-built top 15 ("agreement with the human answer: 13 of 15")
- [ ] Download a run as a single document

---

## Final report (write this when done)

1. What is live, with URLs
2. Each verify item: passed / failed / not run, with evidence
3. Anything skipped or stubbed, and every input still needed from Anudeep
4. The exact model ids chosen (free list and the DeepSeek one)
5. Duration, call count and token count of a typical free-model run
6. The admin passcode location (file and constant name — never the value)
7. Steps for Anudeep before the interview:
    - Run once with the DeepSeek model using the admin expander
    - Open that run, mark it featured, add presenter notes
    - Do one free-model run on a phone and one on a laptop to rehearse
    - Push the repo to GitHub from IntelliJ (public)
    - Share `https://projects.anudeepananth.com/ocrolus` and the GitHub link

## Known risks

- **Free models are slower and less reliable.** That is why validation, repair, fallback and batching exist. A free run
  may take several minutes; the live progress view is what makes that acceptable
- **Free-tier daily limits.** One run is roughly 15–20 calls. The daily cap and one-at-a-time rule protect the quota;
  buying a small amount of credit raises the limit
- **Their brief and dataset are Ocrolus's material.** Keep both out of the public repo, paraphrase the brief on the
  page, and keep the site unindexed
- **Results differ between runs and models.** That is expected and worth saying out loud in the interview: the maths is
  fixed in code, only the judgement steps vary, and every prompt and response is saved for inspection
