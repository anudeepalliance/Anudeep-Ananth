---
name: spec-writer
description: >
  Write an implementation spec into the repo's specs/ folder that a cheaper, instruction-following
  coding model (DeepSeek, Grok, Qwen, Kimi, etc.) can execute without making any design decisions.
  Claude does all the investigation and every judgment call; the spec carries exact files, anchors,
  names, signatures, code for the tricky parts, ordered steps, verification, and stop rules.
  Use when the user says "write a spec", "spec this out", "make a spec for deepseek/grok",
  "hand this off to another model", "/spec-writer", or asks to review a spec an executor ran.
allowed-tools: Read, Write, Edit, Glob, Grep, Bash, Agent, AskUserQuestion
---

# Spec writer

Claude is the architect; the executor is a fast typist that follows instructions literally. Every
judgment call happens here, in the spec. **If the executor has to decide anything, the spec failed.**

Output: one file at `specs/YYYY-MM-DD-<kebab-slug>.md` (repo root, today's date), built from
`spec-template.md` in this folder. Create `specs/` if it is missing.

## Who reads the spec

- Follows instructions literally and in order. Very good at copying a pattern from a file you point
  to.
- Weak at: guessing unstated intent, holding many constraints at once, reasoning across files,
  knowing project conventions, and knowing when to stop.
- Typical failure modes: "improves" nearby code, invents APIs that don't exist, picks the wrong
  import, renames things, adds dependencies, deletes code to make a build pass, declares success
  without running the build.
- Has **no** access to this conversation, Claude skills, or memory. It sees only the spec, the repo,
  and maybe `AGENTS.md`. Anything it needs must be in the spec or in a file the spec names.

## Workflow

1. **Pin the outcome.** Restate the task in one sentence. Use AskUserQuestion only for product or
   behavior decisions the code can't settle. Implementation choices are yours — don't ask.
2. **Investigate.** Read every file the change touches, plus the closest existing example of the
   same pattern (a sibling route, screen, ViewModel, repository). Use an Explore agent for wide
   sweeps. Load `adp-code-structure-style` when the change adds files or is large (per `AGENTS.md`).
   Open the file for every symbol, import, and API you plan to reference — never assume it exists.
3. **Decide everything.** File paths, every new class/function/field name (following the naming
   rules), signatures, data shapes, where each line goes, error behavior, old-client compatibility
   (`llm-notes/post-update-unlocks.md`), and what is out of scope.
4. **Size it.** One spec = one executor session, about 12 steps max, under ~400 lines. Bigger work
   becomes numbered parts (`...-part-1-of-3.md`) where each part leaves the build green and names
   the part that must be finished first.
5. **Write** the spec from `spec-template.md`. Delete template sections that don't apply; never
   leave placeholder text.
6. **Self-check** with the mechanical checks below. Fix the spec until all pass.
7. **Reply** briefly: path, one-line summary, step count, and the run prompt (see end).

## Writing the steps

- **One step = one file + one change.** Each step gives: file path, action (create / insert after /
  insert before / replace / delete), anchor, the change, and a check.
- **Anchor by text, not line number.** Quote a short, exact line of existing code. Line numbers
  drift as earlier steps edit the same file; you may add `(~line 140)` only as a hint. If an earlier
  step edits the same file, make sure the later anchor is still present and still unique after it.
- **Replacements show both sides:** the exact old snippet and the exact new snippet.
- **How much code to write:**
    - Full code for anything where a plausible-looking wrong version still compiles: Mongo queries
      and updates, coroutines/Flow plumbing, concurrency, serialization and wire models, auth or
      permission checks, time zones and date math, money/coin math, retry and caching logic, prompt
      text for the chat model.
    - Signature + behavior bullets + "copy the shape of `X` in `path`" for boilerplate: new route
      files, ViewModel state holders, Koin registration, simple composables, data classes.
- **Name everything.** Every new file, class, function, parameter, constant, string key, and enum
  value gets its exact name in the spec.
- **List non-obvious imports** (same simple name in several packages, extension functions, Kermit
  `Logger`, `kotlinx` vs `java` types). Weak models pick the wrong one.
- **Enumerate, never summarize.** Banned in steps: "handle errors appropriately", "update related
  code", "and so on", "etc.", "similarly for the others", "as needed", "if necessary". If five
  call sites change, write five steps or one step with a table of all five.
- **Fence tempting code.** When a step's file has nearby code that looks broken or improvable, say
  "Do not change `<thing>`".

## Sections every spec carries

- **Conventions for this change.** The executor won't reliably read `AGENTS.md` or the global rules.
  Inline only the rules this change touches, as short bullets, with one tiny prefer/avoid code
  example for any formatting rule — weak models copy examples far better than they follow prose.
  Common ones for this repo: long descriptive names with I/O in the name (`...FromMongo`,
  `...ToMongo`); KDoc of 1–2 plain sentences; short `//` step comments in multi-step bodies;
  lambda bodies on their own line; no explicit types on locals; declarations on one line; Mongo
  field names via `DataClass::field.name`; Kermit `Logger`, not `println`; constants in
  `Constants.kt`; `ApiRoutes` / `RequestKeys`, never raw strings; UI via `PublicTextComposables`,
  `AddSpace()`, `dimens.kt`; ViewModels extend `CrashSafeViewModel` with `@KoinViewModel` and
  registration in `ViewModelModule.kt`.
- **Do not.** Always include, then add task-specific items: touch only listed files; no refactors,
  renames, or reformatting of untouched code; no new dependencies or version bumps; no git commit,
  push, or branch; no deploy; no Mongo writes; never uninstall the app; never edit
  `ConfidentialObjects.kt` or other secrets; never delete code a step didn't name; no wire-shape
  changes (shared DTOs, enums, routes) beyond what a step says.
- **Verification.** Exact build commands for the affected targets (from `AGENTS.md` → "Verify After
  Significant Code Changes") with the expected result. Add self-check greps the executor can run (e.g.
  `grep -n "println" <touched files>` → no output). Failure rule: fix only errors in files this
  spec touched, at most 3 attempts, never by deleting code, `@Suppress`, `!!`, or editing other
  files — then stop and log.
- **Stop rules.** The executor stops and writes to "Executor log" instead of guessing when: an
  anchor is missing or matches more than once; a referenced symbol doesn't exist; a step
  contradicts the code it sees; it needs a file not in the table; the build fails 3 times.
- **For the human.** Anything the executor must not or cannot do: visual checks on device, deploy,
  store-release gating, Mongo backfills, prompt smoke tests that need judgment.

## Mechanical self-check (run before replying)

1. Every `modify` path exists; every `create` path does not.
2. Every anchor matches exactly once in its file: `grep -c -F '<anchor>' <file>` → `1`. Re-check
   anchors that sit in files an earlier step edits.
3. Every symbol the spec calls "existing" has a declaration you can grep.
4. Scan the spec for decision words and rewrite them as instructions: should, could, maybe,
   consider, appropriate, ideally, probably, as needed, if necessary, etc., TBD, TODO.
5. No references to this conversation ("as discussed", "the bug above", "like before").
6. Read the spec once as the executor. Every place you'd have to choose something → resolve it in
   the spec.

## Reply format

```
Spec: specs/<file>.md — <one line>. <N> steps.
Run with: Read specs/<file>.md and execute it exactly, step by step. Tick each checkbox as you finish it. Follow its stop rules.
```

## Review mode

When the user says an executor finished a spec (or asks to review one): read the spec and its
"Executor log", `git diff` the listed files, check each acceptance criterion, look for edits outside
the file table, and run the verification commands. Report pass/fail per criterion in a few lines.
Then fix small misses directly, or write a follow-up spec `specs/<same-name>-fixups.md` if the
user wants the executor to do it. Set the spec's status line to `done` once everything passes.
