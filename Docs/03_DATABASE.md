# Kcal Grind AI — Database Design

## 1. Source of truth rule

Room is the single source of truth on-device. Every screen reads from Room via Flow. Network/AI results are written into Room before the UI ever displays them — nothing renders "live" from a network response.

## 2. Entities (translated from the validated localStorage schemas in the prototype)

### UserProfileEntity
```
id (PK), firebaseUid (nullable, linked after auth), email (nullable),
goal, units, age, heightCm, weightKg, goalWeightKg,
activityLevel, dietTags (List<String>), allergies (List<String>),
targetsSource (recommended|custom|fallback), createdAt, updatedAt
```

### NutritionGoalEntity
```
id (PK), userId (FK), calories, proteinG, carbsG, fatG,
waterLiters, waterGlasses, bmr, tdee, isCustom, updatedAt
```

### FoodEntity  (local cache of food DB lookups + custom foods)
```
id (PK), source (usda|openfoodfacts|nutritionix|custom|ifct),
externalId, name, brand, servingDescription, servingGrams,
calories, proteinG, carbsG, fatG, fiberG, barcodeUpc (nullable),
isUserCreated (Bool), createdAt
```

### MealLogEntity  (was "Meal" in the prototype)
```
id (PK), date, mealType (breakfast|lunch|dinner|snack),
totalCalories, loggedAt, source (manual|ai_photo|ai_voice|ai_text|barcode|search|recipe|curated_recipe),
synced (Bool)
```

### FoodLogItemEntity  (line items inside a MealLogEntity)
```
id (PK), mealLogId (FK), foodId (FK, nullable if ad-hoc AI item),
name, brand, servingDescription, servingGrams,
calories, proteinG, carbsG, fatG, fiberG,
source (manual|ai|verified|recipe|curated_recipe), confidence (0.0–1.0, nullable),
confirmed (Bool)
```

### RecipeEntity (curated recipe catalog, added in v4)
```
id (PK text), name, description, emoji, mealType (breakfast|lunch|dinner|snack|dessert),
prepTimeMinutes, totalCalories, proteinG, carbsG, fatG, fiberG,
dietTags (List<String>), ingredients (List<RecipeIngredient>), instructions (List<String>),
popularityScore, isFavorite (Bool), createdAt
```
This is the critical "AI must not be source of truth" enforcement point: every line item carries its own `source` and `confidence`, independent of the meal-level source, so a user can mix a verified barcode item and an AI-estimated item in the same meal and the UI can distinguish them.

### WeightEntryEntity
```
id (PK), weightKg, date, note (nullable), loggedAt
```

### WaterLogEntity
```
id (PK), date (YYYY-MM-DD), amountMl, loggedAt
```

### AIAnalysisEntity  (audit trail, not the live data path)
```
id (PK), inputType (photo|voice|text), rawInputRef (image path / transcript),
resultJson, overallConfidence, createdAt
```
Kept for debugging model quality and for a future "why did AI say this" feature — the UI never reads nutrition values from here, only from FoodLogItemEntity.

### ConversationEntity / MessageEntity  (AI coach chat)
```
Conversation: id (PK), startedAt, lastMessageAt
Message: id (PK), conversationId (FK), role (user|assistant), text,
         structuredDataJson (nullable, e.g. a suggested meal card), createdAt
```

## 3. Relationships

- `UserProfileEntity 1—1 NutritionGoalEntity` (current active goal; keep history via `updatedAt` rows if you want goal history later)
- `MealLogEntity 1—N FoodLogItemEntity`
- `FoodLogItemEntity N—1 FoodEntity` (nullable — AI can log ad-hoc items not in the food cache)
- `ConversationEntity 1—N MessageEntity`

## 4. Migration strategy

- Use Room's `AutoMigration` where possible; hand-write `Migration` objects for anything involving column type changes or data backfills.
- Version the schema from day one (`exportSchema = true`, commit the schema JSON) — you will need this once real users have real data.

## 5. Decisions you still need to make

1. **Food database provider**: recommend starting with USDA FoodData Central (free, generous) + Open Food Facts for barcodes, upgrading to Nutritionix only if barcode hit-rate proves insufficient in testing (see `02_TECH_STACK.md`).
2. ~~**Cross-device sync**: local-only Room is sufficient for v1.~~ → Resolved in Phase 10 (see §6).
3. ~~**Data retention for AIAnalysisEntity**: decide a retention window.~~ → Resolved: 60-day retention via pg_cron (see §6.3).

## 6. Cloud Sync (Phase 10 — Supabase)

### 6.1 Architecture

Room remains the local source of truth. Supabase Postgres serves as a cloud mirror for cross-device sync. The Android app **never talks to Supabase directly** — all sync flows through the Node.js backend's `/sync/push` and `/sync/pull` routes, authenticated via the existing Firebase middleware.

```
Android (Room) → Backend (Fastify + Firebase Auth) → Supabase (Postgres)
```

### 6.2 Supabase Schema

9 tables mirroring Room entities, with type optimizations:

| Supabase Table | Room Entity | PK Strategy | Key Differences |
|---|---|---|---|
| `user_profiles` | UserProfileEntity | `firebase_uid text PK` | `diet_tags text[]`, `allergies text[]` |
| `nutrition_goals` | NutritionGoalEntity | `firebase_uid text PK` | 1:1 with user_profiles |
| `meal_logs` | MealLogEntity | `bigint IDENTITY` | `log_date date`, `total_calories real` |
| `food_log_items` | FoodLogItemEntity | `bigint IDENTITY` | `calories real`, `confidence real` |
| `weight_entries` | WeightEntryEntity | `bigint IDENTITY` | `weight_kg real`, `log_date date` |
| `water_logs` | WaterLogEntity | `bigint IDENTITY` | `amount_ml smallint` |
| `ai_analysis_log` | AIAnalysisEntity | `bigint IDENTITY` | `result_json jsonb` (not text) |
| `conversations` | ConversationEntity | `bigint IDENTITY` | FK to user_profiles |
| `messages` | MessageEntity | `bigint IDENTITY` | `structured_data jsonb` |

Type choices: `smallint` for calories/amount_ml (0–32767), `real` for grams/scores, `date` for log dates, `text[]` for tags, `jsonb` for structured data.

All tables have `firebase_uid` column (direct or via FK), with indexes. Row Level Security is enabled on all tables.

### 6.3 Sync Strategy & Conflict Resolution

- **Strategy**: Manual sync ("Sync now" button on Profile screen). No automatic background sync.
- **Conflict resolution**: Last-write-wins via `updated_at`/`logged_at` timestamp comparison. If the incoming timestamp is ≥ the existing cloud timestamp, the push overwrites; otherwise it's skipped.
- **Flow**: Push all local data → Pull remote changes since last sync → Merge into Room with timestamp check.

### 6.4 AI Analysis Log Retention

- **Retention window**: 60 days.
- **Implementation**: `pg_cron` job running daily at 03:00 UTC deletes rows older than 60 days.
- **Migration file**: `backend/supabase/migrations/002_ai_cleanup_cron.sql`
- **Free tier note**: Supabase free projects auto-pause after 7 days of inactivity, stopping cron jobs until manually restored.

### 6.5 Storage Budget

- Estimated ~3 KB/user/day for an active user tracking 3 meals + water + weight.
- ~1 MB/user/year.
- **500 MB free tier supports ~400–500 active users/year** with 60-day AI log retention.
- Actual numbers will vary based on AI analysis frequency — verify against the Supabase dashboard after a real data load.
