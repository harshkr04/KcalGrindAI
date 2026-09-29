# Kcal Grind AI — AI Integration

## 1. Non-negotiable rule

No API key for any AI provider ever ships inside the Android app. Every AI call goes: `App → your backend proxy → model provider`. The proxy holds the key, applies rate limiting, and is the only thing that talks to the model.

## 2. Backend proxy responsibilities

- Authenticate the incoming request (Firebase/Supabase Auth token).
- Rate-limit per user (protects your API bill from abuse).
- Hold the system prompt / tool schema — the client sends raw input (image, text, transcript) and gets back structured JSON, never a raw model completion it has to parse itself.
- Log requests for quality monitoring (strip PII where possible).

## 3. Endpoints to build

| Endpoint | Input | Output |
|---|---|---|
| `POST /ai/analyze-photo` | image (base64 or upload URL) | `{ foods: [{name, estimatedGrams, calories, macros, confidence}], overallConfidence }` |
| `POST /ai/analyze-text` | free-text meal description | same shape as above |
| `POST /ai/transcribe` | audio blob | `{ transcript }` (then feed transcript into analyze-text, or combine into one call) |
| `POST /ai/chat` | message + user nutrition context (see below) | `{ reply, suggestedAction (nullable, e.g. "log_this_meal" with a food payload) }` |

## 4. Function-calling tools (for the AI coach — `/ai/chat`)

Read tools (safe, no confirmation needed):
- `get_user_profile()` → goal, targets, diet, allergies
- `get_today_diary()` → meals logged today, totals, remaining calories/macros
- `search_food(query)` → hits your food DB (USDA/OpenFoodFacts cache), not the model's imagination
- `get_recent_trends()` → last 7/30 day averages, adherence

Write tools (must surface a confirmation card in the app — never auto-commit):
- `create_food_log(items)` → returns a proposed log for the user to confirm, does not write to Room directly
- `create_meal_plan(...)`
- `log_water(amount)` — lower stakes, can auto-apply if the user explicitly asked in chat ("log a glass of water")

This mirrors the structure already designed in the product spec — implement it for real rather than the prototype's regex rules.

## 5. Prompt design principles

- **Always request structured JSON output** (use the provider's native structured-output / JSON mode, don't parse free text).
- **Always request a confidence score per food item**, not just overall. Low-confidence items should visually stand out in the confirmation UI so the user knows what to double-check.
- **Include user context in every relevant call**: diet restrictions and allergies must be in the system prompt for photo/text analysis too, not just chat — e.g. flagging a possible allergen in a detected food.
- **Never let the model invent precise nutrition values with false precision.** Instruct it to estimate to reasonable rounding (nearest 5–10 kcal, nearest 1g macro) and to say so.

## 6. Confidence → UX mapping (contract between backend and app)

| Confidence | App behavior |
|---|---|
| ≥ 0.85 | Show as "likely correct," pre-checked, still editable |
| 0.5–0.85 | Show with a "please confirm" prompt, item highlighted |
| < 0.5 | Show as "AI wasn't sure," require the user to actively pick/replace before it can be logged |

## 7. Model choice & Provider Architecture
 
- **Primary Production Provider:** **`gemini-2.5-flash`** via official `@google/genai` SDK.
  - Native structured JSON output mode (`responseMimeType: 'application/json'`).
  - Strict JSON schema enforcement (`FOOD_ANALYSIS_SCHEMA`).
  - Native function calling tool declarations (`GEMINI_TOOLS`).
  - Token-cost efficiency: ~$0.045/user/month at standard active usage.
- **Fallback Provider:** **`meta/llama-3.2-11b-vision-instruct`** on NVIDIA integrate API.
- All model choices remain config-driven (`.env` / Secret Manager: `AI_PROVIDER`, `GEMINI_MODEL`, `GEMINI_API_KEY`).

> [!NOTE]
> **Known Prompt-Tuning Item (Chat Function-Calling Macros):**
> During Phase 9 Section 2 live verification, a minor quality distinction was identified: for sparsely-described chat logging requests (e.g. "log a banana for snack"), `create_food_log`'s function call generated an accurate calorie count (~100 kcal) but defaulted the item's macros to zeros (`protein: 0, carbs: 0, fat: 0`), unlike `/ai/analyze-photo` and `/ai/analyze-text` which produced full macro breakdowns.
> **Fix / Recommendation for Future Pass:**
> The AI Coach system instruction must enforce: *"Always estimate complete nutritional breakdown (protein, carbs, fat in grams); never leave macros at zero when calories > 0"*. Additionally, the `GEMINI_TOOLS` parameter schema for `create_food_log` requires `proteinG`, `carbsG`, and `fatG` in the required array so the model is structurally constrained from omitting them.

## 8. What NOT to do (based on the prototype's mistakes)

- Don't return random/mock data from any endpoint, even for early testing — use a real (even free-tier) model call from day one so confidence calibration is real.
- Don't let voice input skip a transcript-review step — STT errors compound into bad food identification.
- Don't let the barcode flow silently fall back to "AI guesses from the barcode number" — if the barcode isn't found in a real database, say so and route to manual search.

## 9. Known Limitation — AI Tool Grounding Is Client-Trusted, Not Server-Verified

> [!WARNING]
> **The AI coach's "read" tools do NOT independently query any authoritative data source.**

When the Gemini or NVIDIA model invokes a function-calling tool during `/ai/chat`, the backend's `executeTool()` handler (in `server.js`) resolves each tool entirely from the `userContext` object that the Android client included in the request body:

| Tool | Actual data source |
|---|---|
| `get_today_diary()` | `userContext.targetCalories`, `.consumedCalories`, `.remainingCalories`, `.meals` — verbatim echo |
| `get_user_profile()` | `userContext.goal`, `.targetCalories`, `.dietTags`, `.allergies` — verbatim echo |
| `get_recent_trends()` | `userContext.recentTrends` — verbatim echo, or hardcoded placeholder values if missing |
| `search_food(query)` | **Not backed by any real database.** Returns a single hardcoded stub result (`{ calories: 150 }`) regardless of the query. |

### Implications

1. **Trust boundary**: The AI's "grounded" answers are only as accurate as whatever the client chose to send. A buggy or compromised client could fabricate `userContext` and the AI would present the fabricated data as fact.
2. **`search_food` is non-functional**: It returns a static placeholder, not a real USDA/OpenFoodFacts lookup. Any chat answer citing nutritional info from `search_food` is fabricated.
3. **No server-side verification**: The backend has access to Supabase (Phase 10), which mirrors the user's real Room data, but `executeTool()` does not query it. The `userContext` system prompt injection (lines 668-683 of `server.js`) is the only grounding mechanism.

### Recommended Fix (Future Phase)

Replace the client-trust model with server-side queries:

```
get_today_diary()   → SELECT from supabase.meal_logs WHERE firebase_uid = request.user.uid AND date = today
get_user_profile()  → SELECT from supabase.user_profiles WHERE firebase_uid = request.user.uid
get_recent_trends() → Aggregate from supabase.daily_nutrition_cache WHERE firebase_uid = ... AND date >= now() - 7 days
search_food(query)  → Call a real food API (USDA FoodData Central, OpenFoodFacts, or Nutritionix)
```

Until this is implemented, the AI coach should be considered a convenience feature with **no guarantee of data accuracy beyond what the honest client provides**.

