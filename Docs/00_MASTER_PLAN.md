# Kcal Grind AI — Master Implementation Plan

## 0. Where we actually are (confirmed by codebase audit)

- The existing workspace (`stitch_kcal_grind_ai_nutrition/`) is a **42-screen HTML/CSS/Tailwind/vanilla-JS prototype**, not an Android app. Zero Kotlin, zero Gradle.
- It is a genuinely useful **design + behavior spec**: onboarding math (BMR/TDEE), diary CRUD, insights logic, and the "Vitality Flow" design system (colors, type, spacing) are all real and correct.
- All "AI" in the prototype (photo/voice/text/barcode analysis, chat) is **mocked** — random data, regex, `setTimeout`. No network calls exist anywhere.
- No auth, no backend, no real food database.

**Conclusion:** we are not starting from zero — we're starting from a validated spec. The job now is: (1) formally extract that spec into a design reference, then (2) build a real native Android app + backend against it.

## 1. Companion documents

This plan is deliberately split so each concern can be worked (and hired-for / agent-assigned) independently:

| File | Owns |
|---|---|
| `01_ARCHITECTURE.md` | App architecture, module/package structure, layering |
| `02_TECH_STACK.md` | Every technology choice, with justification |
| `03_DATABASE.md` | Room schema, entities, migrations, source-of-truth rules |
| `04_AI_INTEGRATION.md` | Backend proxy, model choice, tool/function design, prompts |
| `05_UI_DESIGN_SYSTEM.md` | How to mine the existing 42 HTML screens into Compose, screen-by-screen |
| `06_PRODUCTION_DEPLOYMENT.md` | CI/CD, environments, secrets, release process, monitoring |
| `07_WORKFLOW.md` | Day-to-day dev process, branching, phase gating, agent handoffs |
| `08_USER_FLOWS.md` | Personas, core journeys, edge cases |
| `09_COMPETITOR_ANALYSIS.md` | What WiseMeal / MyFitnessPal / Cal AI / Journable do well, gaps to exploit |
| `EXECUTION_PROMPT.md` | Copy-paste prompts to run in Claude Code, phase by phase |

## 2. Build order (phases)

```
Phase 0 — Design Extraction (NEW — do this first)
  Analyze all 42 HTML screens + vitality.css + DESIGN.md
  Produce a screen inventory + Compose component mapping
  Output: 05_UI_DESIGN_SYSTEM.md screen table filled in

Phase 1 — Project Bootstrap
  Kotlin/Compose project, Gradle, Hilt, Room, Navigation
  Empty screens wired to nav graph, matching Phase 0 inventory

Phase 2 — Data Layer
  Room entities + DAOs (per 03_DATABASE.md)
  Repository layer, single source of truth
  Port BMR/TDEE/macro-split calculation logic from nutrition.js (this logic is correct — translate, don't redesign)

Phase 3 — Onboarding + Profile
  10-screen onboarding flow, Profile screen
  Local persistence via Room (replacing localStorage)

Phase 4 — Food Engine
  Food database provider integration (see 03_DATABASE.md decision)
  Manual search + entry, barcode lookup (real API)

Phase 5 — AI Logging Pipeline
  Backend proxy (04_AI_INTEGRATION.md)
  Photo analysis, text parsing, voice transcription — real models
  Confirmation UI (AI proposes, user edits, user confirms — never auto-commit)

Phase 6 — Diary + Home
  Full diary CRUD, home dashboard reflecting single source of truth

Phase 7 — Insights + AI Coach
  Trends, streaks, adherence (port insights.js logic)
  AI assistant with real function-calling tools (04_AI_INTEGRATION.md)

Phase 8 — Water / Activity / Health Connect
  Supporting context only — do not let this expand scope

Phase 9 — Auth, Monetization, Production Hardening
  Real auth, subscriptions, crash reporting, Play Store release
```

Each phase should end with a working, demoable build — not partial screens with no data flow.

## 3. Non-negotiable constraints (carried over from product spec)

- AI is never the source of truth for nutrition data — every AI-derived value carries a `source` + `confidence` field and requires user confirmation before it's logged.
- No UI redesign — the 42-screen prototype and Vitality Flow design system are the spec. Deviations must be justified, not incidental.
- API keys never live in the client. All AI calls go through the backend proxy.
- Every food log entry must show, on request, whether its data came from a verified source or an AI estimate.

## 4. Immediate next action

Run the Phase 0 prompt in `EXECUTION_PROMPT.md` against the actual `stitch_kcal_grind_ai_nutrition/` repo to produce the screen inventory. Everything else in this plan depends on that inventory existing and being accurate — don't skip it.
