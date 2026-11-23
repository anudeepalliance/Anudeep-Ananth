You are documenting how an AI review step corrected AI-written outreach emails, for a hiring exercise that asks: "Call
out one specific thing the AI got wrong, oversimplified, or that you had to edit or push back on, and how you caught
it."

Below are the first drafts and the reviewer's change logs for three emails. Pick the ONE most important error the
drafting step made. Prefer an error that appears in more than one email, or a factual problem over a style problem.

RULES

1. Use only what is in the drafts and change logs. Quote the offending words exactly.
2. If the change logs are all empty, say that plainly in "error" and keep the other fields short and honest. Do not
   invent an error.
3. Each field is one or two sentences.

FIRST DRAFTS
{{DRAFTS}}

REVIEW CHANGE LOGS
{{CHANGE_LOGS}}

OUTPUT FORMAT
Return ONLY a JSON object, with no text before or after it and no code fences.
{"error":"what the drafting step got wrong, with the quoted words","whyItMatters":"...","howItWasCaught":"which review
pass caught it and on what grounds","fix":"what the email says now"}
