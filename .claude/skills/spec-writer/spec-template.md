# <Title in sentence case>

Status: ready
Written: <YYYY-MM-DD> by Claude · Part <1 of 1> · Requires: <none | specs/...-part-1-of-2.md done>

## Executor: read this first

1. Read this whole file once before editing anything.
2. Do the steps in order. Do not skip, merge, or reorder them.
3. After finishing a step, change its `- [ ] Done` to `- [x] Done` in this file.
4. Do only what the steps say. If something is not in a step, do not do it.
5. When all steps are done, run every command in "Verification", then fill in "Executor log".

**Stop rules.** Stop immediately, write what happened in "Executor log", set `Status: stopped`, and
end your session if any of these happen:

- An anchor is not found, or matches more than once in its file.
- A class, function, or file the spec calls "existing" does not exist.
- A step contradicts the code you see.
- You need to edit a file that is not in the "Files" table.
- The build still fails after 3 fix attempts.

Do not guess. Stopping with a clear note is a success; guessing is a failure.

## Goal

<2–4 plain sentences: what changes for the user or the system, and why.>

## Background

<Only the facts the executor needs to do the steps. 3–10 bullets, each with a file path.>

- `<path>` — <what it does today, in one line>

## Files

| Action | Path     | Why        |
|--------|----------|------------|
| modify | `<path>` | <one line> |
| create | `<path>` | <one line> |

Do not edit any file that is not in this table.

## Do not

- Do not refactor, rename, or reformat code that a step does not mention.
- Do not add dependencies or change versions.
- Do not run `git commit`, `git push`, or create branches.
- Do not deploy, run `scripts/deploy-vps-build.sh`, or write to MongoDB.
- Do not uninstall the app from any device or emulator.
- Do not edit `ConfidentialObjects.kt` or any secrets file.
- Do not delete code unless a step tells you to.
- <task-specific item, e.g. "Do not change the existing `sendMessage` route.">

## Conventions for this change

<Only rules this change touches. Keep the code example for formatting rules.>

- Names are long and descriptive. A function that hits the database says so: `fetchXFromMongo`,
  `persistXToMongo`.
- Every new public function gets a 1–2 sentence KDoc (`/** ... */`) in plain English.
- Multi-step function bodies get a short `//` comment before each step.
- Use Kermit `Logger`, never `println`.
- Mongo field names come from the data class: `ChatSession::createdAt.name`, never `"createdAt"`.

```kotlin
// Do this
val latestSession = fetchLatestSessionFromMongo(userId)
sessions.forEach {
    persistSessionToMongo(it)
}

// Not this
val latestSession: Session? =
    fetchLatestSessionFromMongo(userId)
sessions.forEach { persistSessionToMongo(it) }
```

## Steps

### Step 1 — <short description>

- [ ] Done

**File:** `<path>`
**Action:** <create file | insert after anchor | insert before anchor | replace anchor | delete anchor>

**Anchor** (must match exactly once):

```kotlin
<exact existing line(s)>
```

**Change:**

```kotlin
<exact code to insert, or the exact replacement>
```

**Imports to add** (skip any already present):

```kotlin
import <fully.qualified.Name>
```

**Check:** <how to confirm, e.g. "`grep -c 'fun fetchXFromMongo' <path>` prints 1">

### Step 2 — <short description>

- [ ] Done

**File:** `<path>`
**Action:** create file

**Copy the shape of:** `<path to existing sibling file>`

**Requirements:**

- Package: `<package>`
- <exact class/function signature>
- <behavior bullet>
- <behavior bullet>

**Check:** <...>

## Verification

Run each command from the repo root. Each must give the expected result.

1. `./gradlew <task>` → `BUILD SUCCESSFUL`
2. `grep -n "println" <touched files>` → no output

If a build fails: fix only errors inside files from the "Files" table, at most 3 attempts. Never fix
a build by deleting code, adding `@Suppress`, adding `!!`, or editing other files. After 3 failed
attempts, follow the stop rules and paste the full error into "Executor log".

## Acceptance criteria

- [ ] <observable outcome 1>
- [ ] <observable outcome 2>
- [ ] Every step above is ticked.
- [ ] Every verification command passed.

## For the human (executor: skip this section)

- <device check, deploy, store gating, Mongo backfill, prompt smoke test>

## Executor log

<Executor fills this in: steps completed, each verification command and its result, files changed,
and, if stopped, the exact step, what was expected, and what was found.>
