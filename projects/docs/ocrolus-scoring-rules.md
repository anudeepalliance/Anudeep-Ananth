# Ocrolus scoring rules

Taken from the formulas in Anudeep's scoring sheet (`Ocrolus assignment data - Anudeep.xlsx`, sheet `Dataset`, row 2
shown). Implemented in `server/.../ocrolus/scoring/OcrolusPriorityScoring.kt` and checked against every row of the sheet
by `OcrolusPriorityScoringGoldenTest`.

## Priority per product (columns Q, R, S)

```
=IF(OR(adopted="Y", flagged_as_risk="Y", nps<35, renewal_days<60, note_score=0), 0,
   ROUND(100 * (note_score/10)
         * (0.6*MIN(nps,70)/70 + 0.4*(qbr_part + renewal_part)/2)
         * volume / MAX(volume of all 50), 1))
```

- **Intent** = note score (0–10, from the model) ÷ 10
- **Health** = 0.6 × min(NPS, 70)/70 + 0.4 × (QBR part + renewal part)/2
    - QBR part: 1 if last QBR ≤ 2 months ago, 0.5 if ≤ 4, else 0
    - Renewal part: 1 if renewal is 90–270 days out, else 0.5
- **Size** = monthly volume ÷ largest monthly volume in the file
- **Excluded (priority 0):** already adopted, flagged at risk, NPS below 35, renewal under 60 days, note score 0
  (declined / no budget)
- Rounded to one decimal, half up

## Best product (column T)

`None` if all three priorities are 0; `Tie` if two or more share the highest; otherwise the product with the highest
priority.

## Product pick

The product with the highest **sum** of priorities across all accounts (Fraud Shield 390.2 in the sheet).

## Rank (column X)

Only accounts with a priority above 0 for the chosen product, and whose best product is the chosen product, `Tie` or
`None`, get a rank.
Rank = 1 + accounts with a higher priority + accounts with the same priority and a larger volume. Top 15 = ranks 1–15.

## Manual review (column W)

Flagged when the model's anomaly score is 5 or more.

## Check by hand

- Palisade Home Loans (A002): note 9, NPS 55, QBR 2, renewal 180, volume 24,500 (the largest in the file) → 100 × 0.9 ×
  (0.6×55/70 + 0.4×(1+1)/2) × 1 = **78.4**
- Summit Ridge Mortgage (A001): note 7, NPS 62, QBR 1, renewal 210, volume 22,000 → 100 × 0.7 × (0.6×62/70 + 0.4) ×
  22,000/24,500 = **58.5**
