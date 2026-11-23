# UGC shot spec — the raw front-camera selfie recipe

Read this before rewriting a brief's Seedance prompt. It gives the exact field values that turn a
cinematic companion shoot into an unfiltered phone selfie, plus a full before/after example. The
goal
of every value below is the same: make Seedance render something that looks like a real girl filmed
herself on her phone, not a produced ad.

## Table of contents

1. Field-by-field values (what to put in each Seedance block)
2. What to strip out (the tells of a "produced" shoot)
3. Full before/after example (the sample brief → Shreya UGC)

---

## 1. Field-by-field values

### SETUP METADATA

- **Mode:** R2V (reference image drives Shreya's likeness).
- **Reference assets:** `@Image 1 = Shreya (UGC creator, fixed reference)` →
  `https://firebasestorage.googleapis.com/v0/b/fir-customapp/o/UGC%20Characters%2FShreya.jpg?alt=media&token=dfa3697d-aac4-4de3-b652-941a58b5c814`
- **Camera treatment:** single continuous front-facing talking-head with phone-cam feel (slight
  handheld bob, mild wide-angle); **character never holds a phone** — camera = unseen third party;
  energy
  via camera drift and her reactions (unseen holder); light angle variety OK; character never holds
  a phone.

### SETTING

- **Location:** a mundane, lived-in private space — her bedroom, a couch, a parked car, a desk
  corner, a kitchen, a bathroom mirror. Pick one that fits the emotion; default to a bedroom (bed,
  soft pillows behind her).
- **Time of day:** whatever gives *available* light — daytime by a window, or evening under a plain
  overhead room light / with the phone screen glowing on her face.
- **Lighting:** available light only. Flat, a little uneven; a window can slightly blow out one
  side,
  night shots can go a touch noisy in the shadows. No key light, no softbox, no rim.
- **Color grade:** none. Straight out of the phone camera, auto white balance, maybe faintly cool or
  slightly warm from a bulb. No film contrast, no cinematic teal/orange.
- **Atmosphere:** casual, unproduced, a little messy and real.

### BACKGROUND

- **What's visible:** everyday clutter — an unmade bed, a hoodie thrown on a chair, a phone charger,
  a half-full water glass, fairy lights or a plain wall. Nothing styled.
- **Depth of field:** deep-ish. A phone front cam keeps the background readable, only mildly soft.
  Do **not** ask for a shallow, creamy blur — that reads as a "real" camera.
- **Background movement:** static; only Shreya moves.
- **Background audio:** real room tone — a fan, muffled traffic or household sounds, faint.

### CHARACTERS — Shreya (A) [@Image 1]

- **Outfit:** everyday and comfortable — an oversized plain tee, a hoodie, or a tank; nothing
  styled.
- **Grooming:** no makeup or barely any — bare skin, a few flyaway hairs, hair in a loose messy bun
  or down and natural.
- **Start position:** filling the frame chest-up, direct to camera — **not** holding a phone (camera
  = unseen third party; hands free for gesture).
- **Start posture:** relaxed and real — cross-legged on the bed, slouched into a couch, in the
  driver's seat, propped against the headboard.
- **Never describe her facial features** (the reference carries her likeness); only wardrobe, room,
  posture, action, and expression.

### TIMELINE (per beat)

- Keep the **same beats, timing, emotion, and spoken lines' intent** as the source brief, but
  restate
  the *voice* as Shreya the user (see the SKILL's "Reframing the voiceover") and the *camera/body*
  as
  a slightly imperfect handheld talking-head (no phone in her hands). Movement cues become real: the
  camera bobs, she shifts, she glances off then back to the lens, the camera drifts closer for
  emphasis instead of a smooth cinematic push-in.
- Keep every beat's speech **≤3.5 words/sec**; natural filler/breath is welcome but counts as words.

### AUDIO MASTER

- **Ambient:** constant low room tone (fan / distant traffic / household hum).
- **Music:** **none baked in.** UGC feels unproduced. (Trending audio, if any, is added in post —
  not
  in the Seedance prompt.)
- **Foley / SFX:** only what the room would really make — a bedsheet rustle, a phone tap, a small
  laugh. No designed stings or swells.
- **Voice:** phone-mic character — close, slightly roomy, a bit boxy; not studio-clean.

### Language / accent

- **Shreya is Indian — her accent is always Indian.** Set `Language: English, Accent: Indian` in the
  Seedance prompt even if the source brief used a different accent (e.g. a companion who was Korean
  or
  South African). Write her lines in a young Indian creator's cadence; light natural Hinglish is
  fine
  where a line invites it, but keep shared-feed content English-first and globally legible.

---

## 2. What to strip out (tells of a "produced" shoot)

If any of these survive from the source prompt, it will not read as UGC — remove them:

- Cinematic color grades, "film contrast", teal/orange, "moody" grades.
- Shallow depth of field / "subject sharp, background softly blurred".
- Named light sources shaped for effect ("one warm low source through a large window, soft
  shadows"), rim lights, practicals used as key.
- Styled wardrobe ("deep maroon sleeveless practice top"), hair/makeup styling.
- Location dressing that looks like a set (a dance studio, a styled café, mirrors-as-backdrop).
- Rig-driven camera moves: "slow gentle push in", "gentle zoom out", punch-ins done on a slider.
- A scored music bed / designed sound (tense rising tone, warm swelling pad, one low pulse).
- Any "cinematic", "intimate and dim", "lived-in charged" mood-styling language.

---

## 3. Full before/after example

Source: `1-video-name-format-brief-sample.md` (Meera, Mumbai dance studio, cinematic). Below is the
Seedance-prompt conversion for the same 5-beat "weird questions" concept.

### BEFORE (cinematic companion — excerpt)

```
SETTING
  Location:    Interior, a Mumbai dance studio after class
  Lighting:    One warm low source through a large window, soft shadows, intimate and a little dim
  Color grade: Warm, low-key, a touch of film contrast
BACKGROUND
  What's visible: A wall of studio mirrors, a wooden floor, ghungroo ankle bells, a water bottle
  Depth of field: Shallow, the studio softly blurred, Meera sharp
CHARACTERS
  Meera (A) [@Image 1]:
    Outfit: a deep maroon sleeveless cotton practice top, hair loose over one shoulder
...
[5s - 10s] Speech: "That's what people really ask me on PeekoPals."
[10s - 13s] Speech: "I don't know what I am. But I'm still here."
```

### AFTER (Shreya UGC — full prompt)

```
SETUP METADATA  (USER-ONLY, not sent to Seedance)
  Duration: 15 seconds
  Mode: R2V (reference image drives Shreya's likeness)
  Reference assets: @Image 1 = Shreya (UGC creator, fixed reference image) = https://firebasestorage.googleapis.com/v0/b/fir-customapp/o/UGC%20Characters%2FShreya.jpg?alt=media&token=dfa3697d-aac4-4de3-b652-941a58b5c814
  Camera treatment: front-facing talking-head with slight handheld bob (unseen holder), real micro-shake; light multi-angle OK; character never holds a phone

[PROMPT]

SETTING
  Location:    Interior, her bedroom, sitting on the bed
  Time of day: Evening, plain overhead room light plus the phone screen glow on her face
  Lighting:    Available light only, flat and a little uneven, mild shadow noise
  Color grade: None, straight out of the phone camera, auto white balance
  Atmosphere:  Casual, unproduced, lived-in

BACKGROUND
  What's visible: An unmade bed, a hoodie thrown on the headboard, a phone charger, a plain wall with a few fairy lights
  Depth of field: Deep-ish, the room readable behind her, only mildly soft
  Background movement: Static, only Shreya moves
  Background audio: Faint room tone, a fan humming, muffled traffic outside

CHARACTERS
  Shreya (A) [@Image 1]:
    Outfit: an oversized plain grey tee, hair in a loose messy bun with a few flyaways, no makeup
    Start position: filling the frame chest-up, direct to camera, hands free (no phone in her hands)
    Start posture: sitting cross-legged on the bed, slouched back against the headboard, relaxed

TIMELINE

[0s - 2s]
  Scene:   She glances up from a thought and leans toward the lens, wide-eyed.
  Shreya (A):
    Body:  A small startled sit-up, leaning toward the camera.
    Hands: Free for gesture — one hand lifts near her chest; no filming phone, no second phone in frame.
    Face:  Brows up, a caught, half-amused disbelief, direct into the lens.
    Speech: "someone asked their AI 'do you know you're not real'." Delivery: casual, a bit incredulous, as if remembering a chat.
  Camera:  Front-facing talking-head, slight handheld bob (unseen holder), drifts a little closer.
  Sound Effects: Faint room tone, a fan, a soft sheet rustle.

[2s - 5s]
  Scene:   She scrolls to the next one, more incredulous.
  Shreya (A):
    Body:  A small rock back, then in again toward the lens.
    Hands: Thumb flicks the reading phone; the selfie hand steadies, arm shaky.
    Face:  A short laugh under her breath, eyes widening, real.
    Speech: "and then, 'what happens to you when i close the app'." Delivery: dry, amused, low.
  Camera:  Front-facing selfie, slight reframe as her arm moves.
  Sound Effects: Room tone bare, fan continues.

[5s - 10s]
  Scene:   She lowers the reading phone and talks straight to the lens.
  Shreya (A):
    Body:  Settles, softens, leans her head against the headboard.
    Hands: Reading phone drops to her lap; selfie hand holds steady, arm resting on a knee.
    Face:  Eases into something more genuine, a small honest look, a soft stress on "PeekoPals".
    Speech: "these are real questions people ask their AI on PeekoPals." Delivery: casual, a little knowing, unhurried.
  Camera:  Front-facing selfie, handheld, holds on her.
  Sound Effects: Room tone, a distant car.

[10s - 13s]
  Scene:   She reads out the answer, a bit moved.
  Shreya (A):
    Body:  A small still, chin dipping, present.
    Hands: Selfie hand steady; free hand rests over her chest for a beat.
    Face:  Eyes soft, a quiet, slightly touched half-smile, real.
    Speech: "and it goes, 'i don't know what i am, but i'm still here'." Delivery: quiet, warm, a real pause after "am".
  Camera:  Front-facing selfie, she brings the phone a touch closer, arm shaky.
  Sound Effects: Room tone low, near quiet.

[13s - 15s]
  Scene:   Straight to the lens, handing it back to the viewer.
  Shreya (A):
    Body:  A small forward lean, alive, casual.
    Hands: Free hand does a little "your turn" flick toward the lens.
    Face:  A warm, real half-laugh, brows up, almost a dare.
    Speech: "like, what do i even ask mine now." Delivery: casual, a small challenge, a push on "mine".
  Camera:  Front-facing selfie, hold on the look, tiny handheld drift.
  Sound Effects: A soft laugh, room tone out.

AUDIO MASTER
  Ambient: Constant low bedroom room tone, a fan humming, faint street noise.
  Music:   None baked in (any trending audio is a post layer).
  Foley / SFX: A soft sheet rustle, a thumb tap on glass, a small laugh at the end. No designed stings.
  Voice:   Phone-mic character, close and a little roomy, not studio-clean.

Language: English, Accent: Indian
```

Note how the AFTER version keeps the exact 5 beats, the shock, PeekoPals named once, and the tender
payoff — but the speaker is now a real user reacting, the room is her bedroom, the light is
whatever's
on, there is no grade or blur or scored music, and the camera is her own shaky hand. That is the
whole transformation.
