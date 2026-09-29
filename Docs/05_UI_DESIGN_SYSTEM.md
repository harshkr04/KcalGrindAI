# Kcal Grind AI — UI / Design System Extraction

## 1. The rule

**Do not redesign.** The 42-screen HTML prototype + `vitality_flow/DESIGN.md` + `shared/vitality.css` is the design spec. Engineering's job is faithful translation to Compose, not reinterpretation. Any visual deviation should be a deliberate, called-out decision — not something that happens because a screen wasn't checked against the source.

## 2. Confirmed design tokens (from the audit — already extracted)

- Type: Roboto Flex (headlines), Inter (body), Material Symbols Outlined (icons)
- Color: Deep Forest Green `#2d6a4f` (primary), Periwinkle `#646fd4` (AI accent)
- Design language: Material 3 Expressive + subtle "Liquid Glass"-inspired depth
- These should become a Compose `Theme.kt` with a `ColorScheme` and `Typography` object — pull exact values from `vitality.css`, don't approximate them.

## 3. Screen inventory — process (do this before writing any Compose code)

For every `.html` file in `app/`, extract:

| Field | What to capture |
|---|---|
| Screen name | filename + human name |
| Route | where it sits in navigation (which flow, entry points) |
| Layout | top bar, content structure, bottom nav present? |
| Components used | cards, sheets, buttons, chips, progress rings, etc. — note reusable ones |
| Data displayed | which entities/fields from `03_DATABASE.md` feed this screen |
| Interactions | taps, swipes, sheets triggered, forms |
| States | empty state, loading state, error state (check if the prototype shows these — if not, design them fresh, calling this out explicitly) |

Build this as a table (or one row per screen in a spreadsheet) — this becomes the literal checklist for Phase 1's screen scaffolding.

## 4. Known screen groups (from the audit — use as your starting checklist)

- **Onboarding (10 screens):** `01_welcome` → `10_ready` — goal, personal details, activity, diet/allergies, calorie target reveal, permissions
- **Home:** `11_home` — calorie ring, macro bars, meal list, water, bottom nav
- **Diary:** date nav, meal sections, swipe-to-delete, undo, duplicate, totals
- **Logging entry points:** `add_food_sheet`, `camera` + `ai_analyzing`, `voice_input`, `text_input`, `barcode_scanner`, `food_search`
- **Confirmation:** `food_result`, `meal_confirm`, `food_editor`/`food_editing`
- **Insights:** averages, streaks, adherence, weight progress
- **Weight log**
- **AI chat:** `ai_chat`
- **Recipes, favorites/saved meals, custom food, nutrition library** — scaffolded in prototype, lower priority than the core loop
- **Profile** — exists only as a standalone Stitch mockup (`user_profile/code.html`), not wired into the prototype's flow — needs its own careful extraction since it wasn't validated end-to-end like the others

## 5. Reusable component candidates (build these once, use everywhere)

- Calorie/macro ring or bar (Home + Diary + Insights all use variants of this)
- Meal section card (used identically in Home preview and full Diary)
- Confidence badge (new — doesn't exist in the mocked prototype, needed for real AI results per `04_AI_INTEGRATION.md` §6)
- Bottom sheet shell (used by add-food, food-editor, and others)
- Empty state component (the prototype likely under-designs these — check during inventory)

## 6. Output of this phase

A filled-in screen inventory table (from §3) checked into the repo as `docs/screen-inventory.md`, used directly as the Phase 1 (Project Bootstrap) task list — one Compose screen stub per row, wired into the nav graph in the same structure as the inventory's "Route" column.
