# Lumina — Execution Prompts (for Claude Code)

Run these in order. Each assumes the docs in this folder (`00_MASTER_PLAN.md` through `09_COMPETITOR_ANALYSIS.md`) are present in the repo — point Claude Code at them explicitly so it doesn't improvise decisions already made here.

---

## PHASE 0 — Design Extraction (run this first, before any Android code)

```
Read every .html file under app/ in this repo, plus shared/vitality.css and
vitality_flow/DESIGN.md. Do not write any Kotlin or Android code in this task.

Produce docs/screen-inventory.md with one row per screen containing:
- Screen name and filename
- Where it sits in the navigation flow (which flow, what screens link to/from it)
- Layout structure (top bar, content, bottom nav, sheet/modal?)
- Every distinct UI component used (cards, chips, progress rings, buttons, etc.) —
  flag components that repeat across multiple screens as "shared component candidates"
- Which data fields (referencing 03_DATABASE.md entities) this screen reads or writes
- User interactions (taps, swipes, forms, sheets triggered)
- Whether the screen currently has an empty state and/or error state defined in the
  HTML/JS — if not, note it as "needs design" rather than skipping it

Also produce docs/design-tokens.md extracting the exact color values, type scale,
spacing scale, and corner-radius/elevation values from vitality.css and DESIGN.md,
ready to become a Compose Theme.kt.

Do not redesign anything. This is extraction only.
```

---

## PHASE 1 — Project Bootstrap

```
Using 01_ARCHITECTURE.md and 02_TECH_STACK.md in this repo, bootstrap a new
Android project:
- Kotlin + Jetpack Compose + Material 3, package com.lumina.nutrition
- minSdk 26, targetSdk 34
- Hilt for DI, Navigation Compose, Room, Retrofit + OkHttp + kotlinx.serialization,
  Coil, CameraX, ML Kit Barcode Scanning
- Package structure exactly as specified in 01_ARCHITECTURE.md section 1
- Build Theme.kt from docs/design-tokens.md (produced in Phase 0)
- Create one empty Composable screen + route per row in docs/screen-inventory.md
  (produced in Phase 0), wired into a single NavHost, navigable end to end with
  no real data yet — this is a clickable skeleton, not functional screens

Confirm the project builds and runs on an emulator before finishing this task.
```

---

## PHASE 2 — Data Layer

```
Using 03_DATABASE.md in this repo, implement:
- All Room entities and DAOs exactly as specified (UserProfileEntity,
  NutritionGoalEntity, FoodEntity, MealLogEntity, FoodLogItemEntity,
  WeightEntryEntity, AIAnalysisEntity, ConversationEntity, MessageEntity)
- Repository interfaces in domain/repository, implementations in data/repository
- Port the BMR/TDEE and macro-split calculation logic from
  stitch_lumina_ai_nutrition/app/shared/nutrition.js into a Kotlin domain use case —
  this logic is already validated, translate it faithfully, do not redesign the formulas
- Room DAO tests using an in-memory database for every entity

Do not wire any UI to this yet — this phase is data layer only, verified by tests.
```

---

## PHASE 3 — Onboarding + Profile

```
Using docs/screen-inventory.md (onboarding + profile rows) and the Phase 2 data
layer, implement the full onboarding flow (10 screens) writing to
UserProfileEntity/NutritionGoalEntity via Room, and the Profile screen (note:
per 05_UI_DESIGN_SYSTEM.md, Profile only exists as a standalone Stitch mockup
that was never wired into the prototype's flow — extract it carefully, it wasn't
validated end-to-end like the other screens).

Confirm a user can complete onboarding, close the app, reopen it, and land on
Home with their saved profile intact.
```

---

## PHASE 4 — Food Engine

```
Using 02_TECH_STACK.md's food-database decision and 03_DATABASE.md's FoodEntity
schema, integrate USDA FoodData Central for search and Open Food Facts for
barcode lookup. Implement manual search and barcode-scan (via ML Kit) entry
points, writing results into FoodEntity as a local cache.

Handle the "barcode not found" case per 08_USER_FLOWS.md section 4 — route to
manual search, don't dead-end.
```

---

## PHASE 5 — AI Logging Pipeline

```
Using 04_AI_INTEGRATION.md, build the backend proxy (specify your chosen
language/runtime) with the four endpoints listed in section 3, using real
model calls (not mocked data) from the first working version. Implement the
confidence → UX mapping from section 6, and the confirmation UI described in
01_ARCHITECTURE.md section 4 — no path may write an AI result directly into
Room without passing through user confirmation.

Wire up photo (CameraX), voice (SpeechRecognizer or chosen STT), and text entry
points from docs/screen-inventory.md to this pipeline.
```

---

## PHASE 6 onward

```
Continue in the order specified in 00_MASTER_PLAN.md section 2 (Diary/Home,
Insights + AI Coach, Water/Activity, Auth/Monetization/Production), using the
corresponding numbered doc in this folder for each phase's detailed spec.
Do not start a phase until the previous one is a working, demoable build per
07_WORKFLOW.md section 4 (Definition of Done).
```
