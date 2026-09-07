# Seedance intro script — Anudeep Ananth

Duration target: 10–12 seconds, vertical or 16:9 lab aesthetic.

## Visual
- Setting: dark AI-lab / terminal desk, cyan UI glow on a monitor behind him
- Subject: Anudeep facing camera, professional but approachable
- Camera: medium close-up, slow push-in
- On-screen: subtle terminal chrome / agent_os watermark (optional)

## Spoken line (English)
"Hi — I'm Anudeep Ananth. I build lifecycle and demand systems for B2B SaaS, and I ship custom AI agents that do the work. Paste a job description on this page — my Job Matching agent will tell you if I'm the right hire."

## Generation notes
- Model preference: Seedance 2.5 / Higgsfield when CLI auth is available
- Source still: `src/main/resources/static/images/anudeep-hero.jpg` (replace with real professional photo when provided)
- Output path when generated: `src/main/resources/static/video/anudeep-intro.mp4`
- Lazy-load on the homepage video element (`preload="none"`, poster = hero image)
