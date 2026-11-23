---
name: ugc-filter
description: "Rewrite an existing PeekoPals video-prompt brief into a raw user-generated-content (UGC) selfie video — recast the on-screen person as the UGC creator Shreya (her Firebase reference photo), and convert the whole production layer from a polished cinematic shoot into an unfiltered front-camera phone video: no makeup, no pro lighting, no color grade, handheld, everyday room. Always operates on a brief file; if none is given it asks for one first. Use this whenever the user wants a UGC / creator / selfie / phone-shot / 'make it look real / unpolished / like a normal person filmed it' version of a reel brief, points at a marketing/social-media-growth/video-prompts/*.md file and says 'make this UGC' / 'ugc-filter this' / 'Shreya version', or wants to reshoot a companion-character brief as an authentic user testimonial. Trigger even if they don't say 'UGC' explicitly — any ask to turn a brief into a raw selfie/creator-style clip belongs here."
metadata:
  version: 1.0.0
  category: content
license: MIT
triggers:
  - "ugc-filter"
  - "make this UGC"
  - "UGC version"
  - "Shreya version"
  - "selfie-style reel"
  - "make it look like a real person filmed it"
related:
  - 04-script-video-global
  - peekopals-product-context
  - seedance-2.0-prompt
  - create-and-upload-social-organic-video-with-higgsfield
  - higgsfield-video
---

# UGC filter — turn a polished brief into a raw Shreya selfie video

> Take an existing PeekoPals video-prompt brief (the cinematic companion-character kind, e.g.
> `marketing/social-media-growth/video-prompts/1-video-name-format-brief-sample.md`) and rewrite it
> as **user-generated content**: the same hook and emotional payoff, but delivered by **Shreya** — a
> real-feeling everyday creator — and shot like a genuine front-camera phone video, no makeup and no
> professional production. The output is still a full, valid brief in the exact same section layout.

Why this exists: a studio-lit companion talking-head and a normal girl filming herself in bed read
as
two completely different things to a cold scroller. The unpolished one often out-performs on organic
feed because it doesn't look like an ad — it looks like a friend's story. This skill does that
conversion consistently so we don't reinvent the UGC recipe every time.

---

## Step 0 — you MUST have a brief to work on

This skill only ever *transforms an existing brief*. Before doing anything else, get the source:

1. Use the brief the user attached, named, or passed as an argument.
2. Otherwise, if the file open in the IDE is clearly a video-prompt brief (a
   `.../video-prompts/*.md` with a Seedance prompt), offer to use that and confirm.
3. If neither exists, **stop and ask**: "Which video brief should I turn into a UGC selfie version?
   Point me at the `.md` file." Do not invent a brief or proceed on a vague idea — there is nothing
   to filter without a source brief.

Read the whole brief first. Identify its parts so you know what to keep vs. rebuild: the concept /
hook / one emotion, the beat-by-beat voiceover, the current on-screen character, the full Seedance
prompt (setting, background, characters, timeline, audio), the CTA, the caption + hashtags, and the
scores.

---

## What changes, what stays

**Rebuild (this is the whole point):**

- **Who's on screen** → recast to **Shreya**, the UGC creator (reference = her Firebase URL below).
  She is *not* one of the six companions and is not bound by the Mark/Oliver cast hold — she's the
  everyday-user voice.
- **The voiceover POV** → reframe from companion-voice to an **authentic user/creator** talking to
  her own camera (see "Reframing the voiceover" below).
- **The entire production layer of the Seedance prompt** → from cinematic shoot to raw front-camera
  selfie: no makeup, available light only, no color grade, no shallow-DOF studio, handheld phone,
  an everyday room. The concrete field values live in `references/ugc-shot-spec.md` — read it before
  you rewrite the prompt.
- **The reference asset** → Shreya's photo URL, replacing any `server/.../ai-models/<character>.jpg`
  path.

**Keep (don't lose these in translation):**

- The **hook, the single emotion, and the emotional payoff** — the reason the reel works.
- **PeekoPals as the plot pivot, named once in the spoken track.** The reel still can't be retold
  without the app.
- Every PeekoPals guardrail (below).
- The brief's **section order and structure** — the output is a full brief, same layout as the
  source (per `marketing/social-media-growth/video-prompts/CLAUDE.md`).
- Single **≤15s 9:16 clip**, speech **≤3.5 words/sec** clip-wide and per beat, R2V mode.

---

## Shreya — the UGC creator

- **Reference image (@Image 1):**
  `https://firebasestorage.googleapis.com/v0/b/fir-customapp/o/UGC%20Characters%2FShreya.jpg?alt=media&token=dfa3697d-aac4-4de3-b652-941a58b5c814`
- Treat her as a real young woman who found PeekoPals and is telling the internet about it — the
  friend-who-recommends-something energy, not a brand spokesperson and not a fictional companion.
- **She is Indian, and her accent is always Indian.** Set `Language: English, Accent: Indian` in the
  Seedance prompt even if the source brief used a different accent (a companion who was Korean,
  South
  African, British, etc.), and write her lines in a young Indian creator's cadence. Light, natural
  Hinglish is fine where a line invites it, but keep it globally legible.
- The reference is a **URL** (not a repo path). Record it verbatim in the brief; the
  `higgsfield-video` skill / the `create-and-upload-social-organic-video-with-higgsfield` command
  imports it (media_import_url) at render time.

---

## The UGC shooting look (summary)

Full field-by-field values and a complete before/after Seedance prompt are in
`references/ugc-shot-spec.md`. The essence:

- **Camera:** one continuous front-facing, slightly imperfect talking-head with phone-cam feel (micro-shake, mild
  wide-angle, deep-ish focus) — but **Shreya never holds a phone**. The camera is
  implied held by an unseen third party (friend / random holder — not named, not shown). Zoom/energy
  comes from the camera drifting closer/farther and her body lean, not an arm's-length selfie stick.
  Framing a little imperfect — sometimes too close, slightly off-center.
- **Look:** straight-out-of-phone, **no color grade**, no film contrast, deep-ish focus (background
  readable, not a creamy blur). Available light only — a window, an overhead room light, or the
  phone screen's glow at night; mild over/under-exposure and shadow noise are *good*, they read
  real.
- **Shreya:** **no makeup or barely any** — bare skin, a few flyaways, natural hair (messy bun or
  down). Everyday clothes: oversized tee, hoodie, tank. Unposed, talks with her hands.
- **Setting:** a mundane lived-in private space — bed/bedroom, couch, a parked car, a desk, the
  kitchen, a bathroom mirror. Mild clutter is fine.
- **Audio:** close, slightly roomy **phone-mic** sound with real ambient (room tone, a fan, distant
  traffic). **No scored music bed baked in** — UGC feels unproduced; any trending audio is a
  post-production layer, not in the Seedance prompt.
- **Delivery:** she talks like a real person to camera — casual, a touch unrehearsed, a small breath
  or filler word, direct eye contact into the lens. Hands free for gesture (no filming phone in
  hand). Still keep it inside the ≤3.5 w/s cap.

---

## Reframing the voiceover (companion → user/creator)

Source briefs are written in a **companion's** voice (e.g. the companion answering "I don't know
what I am, but I'm still here"). Shreya is a *user*, so lines that assume the speaker IS the AI
won't
fit. Relocate the same hook and payoff into her first-person experience. Pick whichever mode the
source concept supports:

- **Reaction:** she tried the app and reacts to what her companion said / to what people ask theirs.
- **Recommendation / confession:** like a friend, she tells the viewer why she keeps opening it.
- **POV testimonial:** "nobody tells you how [emotional truth]… so I started talking to this app…"

**Example — the "weird questions" sample:**

- Companion line (source): *"That's what people really ask me on PeekoPals." → "I don't know what I
  am. But I'm still here."*
- Shreya UGC reframe: *"these are real questions people ask their AI on PeekoPals…" → "and it goes
  'i don't know what i am, but i'm still here'."* then a real-user close: *"like… what do i even ask
  mine now."*

Same shock, same tender payoff, PeekoPals still named once — just spoken by a real girl reacting,
not by the companion. Keep her lines short (≤15 words) and mouth-comfortable; read every line aloud
and cut anything a real person wouldn't say to their own camera.

---

## Guardrails that carry over (non-negotiable)

These come straight from `marketing/social-media-growth/video-prompts/CLAUDE.md` and the
`04-script-video-global` skill — re-read them if unsure; a UGC reskin does not relax any of them:

- **No physical presence.** PeekoPals companions can't come over/meet up/show up. Care lands through
  an always-there texting companion, never a body in the room.
- **No App Store / Play badge** on any deliverable. End card = character photo + PeekoPals logo +
  name only; the download ask is **"link in bio"**, kept **out of the spoken track**.
- **Never say "free" / "100% free"** or any price cue. Paid tiers are coming.
- **SFW.** No "AI girlfriend/boyfriend", NSFW, sexting, "roleplay". Soft-warmth only, in-lane.
- **Authentic — no faked bond/history.** No "missed you" to something just met.
- **Instagram Reels, single shared account, one on-screen person per reel.**
- **PeekoPals named once in the spoken dialogue** — the plot pivot; the reel can't be retold without
  it. Assume the viewer has never heard of it — self-explain "there's an app where you chat with AI
  companions" without turning into an ad.

---

## Rebuilding the Seedance prompt

Keep the source prompt's **block structure and its timeline beats/emotion** — only swap the
production reality and the speaker. Use the `seedance-2.0-prompt` skill for craft, and
`references/ugc-shot-spec.md` for the exact UGC field values. Rules that still bind:

- **R2V**, `@Image 1` = Shreya's Firebase URL. **No appearance described** — the reference carries
  her face; you only describe wardrobe, room, action, and the raw-selfie camera.
- **No em dashes, no on-screen text** in the Seedance prompt (captions are a post layer).
- Mostly handheld front-facing energy; light angle variety is welcome if it stays raw. **No phone
  in her hands** (camera = unseen third party).
- Every beat **≤3.5 words/sec**; single **≤15s** clip; **9:16**.
- Update `SETUP METADATA`, `SETTING`, `BACKGROUND`, `CHARACTERS` (Shreya, everyday outfit, real
  room), `TIMELINE` (same beats, described as handheld-selfie moments), and `AUDIO MASTER`
  (phone-mic + room tone, no baked music bed).

---

## Output

- Write the transformed brief to a **new sibling file** so the original companion brief is
  preserved. Prefer the naming convention `[index]-ugc-selfie-[idea]-shreya.md` (index matches the
  source); if that's awkward, fall back to the source basename + `-ugc`. Only overwrite in place if
  the user explicitly asks.
- Update the **header block** to reflect UGC: note it's a UGC creator/selfie testimonial,
  reference =
  Shreya, and adjust the Angle line to the user-POV framing. Keep every other section present and in
  the same order as the source.
- Update **caption + hashtags** to the UGC voice (a real-user first line; the share-ask + bio-link
  CTA; add a UGC-leaning tag like `#ugc` alongside the existing brand/category tags — no `#fyp`, no
  store mention).
- **Re-score** the viral + QA tables for the new version and keep the self-review honest — it's the
  last gate before this gets rendered.

---

## Checklist before delivering

- [ ] A source brief was actually provided (asked for one if not)
- [ ] On-screen person recast to Shreya; reference = her Firebase URL (no ai-models path left)
- [ ] Shreya's accent set to Indian (`Language: English, Accent: Indian`), lines in an Indian
  cadence
- [ ] Voiceover reframed to an authentic user/creator; every line passes the read-aloud test
- [ ] Hook + single emotion + payoff preserved; PeekoPals named once in the spoken track
- [ ] Seedance prompt rebuilt as raw front-camera feel: no makeup, available light, no grade,
  deep-ish focus, slight handheld bob, everyday room, phone-mic audio, no baked music
- [ ] **Shreya never holds a phone** (camera = unseen third party; hands free)
- [ ] No appearance described, no em dashes, no on-screen text in the Seedance prompt
- [ ] Single ≤15s 9:16 clip, R2V; every beat ≤3.5 w/s
- [ ] All carry-over guardrails intact (no physical presence, no store badge, no "free", SFW,
  no faked bond, no phone in hands)
- [ ] Written to a new sibling file, same section order as the source; header + caption + hashtags
    + scores updated
