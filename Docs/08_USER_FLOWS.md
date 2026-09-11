# Lumina — User Flows

## 1. Core loop (the one that must work perfectly)

```
User eats → opens Lumina → taps AI/Add → picks photo/voice/text/barcode/search
   → AI analyzes → user reviews/edits/confirms → logged
   → Home + Diary + Insights update instantly (same Room data)
```

Every other flow in the app supports this one. If a feature doesn't make this loop faster or more accurate, it's lower priority than something that does (per the product's own North Star).

## 2. First-time user journey

1. Onboarding (10 screens): goal → personal details → activity level → diet/allergies → calorie/macro target reveal → permissions (camera, mic, notifications, Health Connect)
2. Land on Home with an empty state that immediately invites the first log ("Log your first meal")
3. First AI log — this is the make-or-break moment for retention; the confirmation UX must feel fast and trustworthy, not laborious

## 3. Daily returning user journey

Morning: opens Home, sees today's targets reset, AI suggestion for the day.
Through the day: logs meals via whichever input is fastest for the context (photo at a restaurant, text at a desk, barcode for packaged food).
Evening: checks Insights or asks the AI coach "what should I eat tonight" when short on a macro.

## 4. Edge cases to design for explicitly (the prototype under-designs these)

- **Low-confidence AI result**: user must be able to quickly correct without feeling like the AI failed them — frame as "help me get this right," not an error state.
- **No internet**: manual logging (search cached/favorite foods, saved meals) must still work; AI features show a clear "reconnect to use AI logging" state, not a spinner that never resolves.
- **Barcode not found**: route straight to manual search/photo, don't dead-end.
- **User skips confirming a meal mid-flow**: don't lose the AI analysis — let them resume later (this implies the AIAnalysisEntity draft should be resumable, not just an audit log).
- **Goal changes mid-use** (e.g., switches from "lose weight" to "maintain"): targets recalculate, historical logs are not retroactively altered.
- **Allergy/diet conflict detected in a photo/text analysis**: this should surface as a visible warning in the confirmation UI, not just silently logged.

## 5. Personas (lightweight — expand if you have real user research)

- **Busy tracker**: wants near-zero friction, will abandon if logging takes more than ~10 seconds; primary user of photo/voice logging.
- **Detail-oriented tracker**: wants to verify/edit AI results, cares about micronutrients, uses search/manual entry more.
- **Goal-driven beginner**: motivated by the goal, not the mechanics; leans on AI coach suggestions ("what should I eat") rather than self-directed planning.

Design decisions (e.g., how aggressively to auto-confirm high-confidence AI results, how much detail Home shows by default) should be checked against at least the first two personas — they pull in opposite directions on friction vs. control.
