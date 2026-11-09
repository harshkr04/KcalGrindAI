# KcalGrind Data Flow & Synchronization Architecture

This document describes the complete data flow between the KcalGrind Android application and the backend service, covering database synchronization, AI processing pipelines, quota management, and privacy boundaries.

---

## 1. Database Synchronization

### 1.1 Trigger Mechanism
* **Manual Only:** Cloud sync is currently triggered on-demand by the user tapping **"Sync Now"** on the Profile screen (`ProfileViewModel.onSyncNow()`).
* **Automatic Sync:** **None.** There is no background worker (`WorkManager`), foreground service, sync-on-launch, or sync-on-log trigger running automatically when signed in.
* **Source of Truth:** The local SQLite / Room database on the Android device remains the primary source of truth. Supabase serves as a synchronized cloud backup.

### 1.2 Sync Protocol
Synchronization uses a two-phase push-pull protocol:
1. **Push Phase (`POST /sync/push`):**
   * Transmits all local Room records formatted into `SyncPushRequest`.
   * Conflict resolution: Last-write-wins using ISO-8601 timestamps (`updated_at` or `logged_at`).
2. **Pull Phase (`GET /sync/pull?since=<ISO_TIMESTAMP>`):**
   * Fetches changes in Supabase updated since the user's `last_sync_timestamp`.
   * Merges remote entities into Room, avoiding overwrite if local edits are newer.

### 1.3 Tables Synchronized
| Table Name | Direction | Sync Fields & Payload | Conflict Resolution |
|---|---|---|---|
| `user_profiles` | Push & Pull | `firebase_uid`, `email`, `goal`, `units`, `age`, `height_cm`, `weight_kg`, `goal_weight_kg`, `activity_level`, `diet_tags`, `allergies`, `targets_source` | Last-write-wins (`updated_at`) |
| `nutrition_goals` | Push & Pull | `firebase_uid`, `calories`, `protein_g`, `carbs_g`, `fat_g`, `water_liters`, `water_glasses`, `bmr`, `tdee`, `is_custom` | Last-write-wins (`updated_at`) |
| `meal_logs` | Push & Pull | `firebase_uid`, `local_id`, `log_date`, `meal_type`, `total_calories`, `logged_at`, `source`, `synced` | Last-write-wins (`logged_at`) |
| `food_log_items` | Push & Pull | `meal_log_id`, `local_id`, `food_id`, `name`, `brand`, `serving_description`, `serving_grams`, `calories`, `protein_g`, `carbs_g`, `fat_g`, `fiber_g`, `source`, `confidence`, `confirmed` | Cascade-deleted and re-inserted per meal log |
| `weight_entries` | Push & Pull | `firebase_uid`, `local_id`, `weight_kg`, `log_date`, `note`, `logged_at` | Last-write-wins (`logged_at`) |
| `water_logs` | Push & Pull | `firebase_uid`, `local_id`, `log_date`, `amount_ml`, `logged_at` | Last-write-wins (`logged_at`) |
| `ai_analysis_log` | Push-only | `firebase_uid`, `local_id`, `input_type`, `raw_input_ref`, `result_json`, `overall_confidence`, `created_at` | Inserted on push; never pulled to client |
| `conversations` | Push & Pull | `firebase_uid`, `local_id`, `started_at`, `last_message_at` | Last-write-wins (`last_message_at`) |
| `messages` | Push & Pull | `conversation_id`, `local_id`, `role`, `text_content`, `structured_data`, `created_at` | Deduplicated by `local_id` per conversation |

### 1.4 Additional Supabase Tables (Managed by Backend / Purge)
The following tables exist in the schema and are managed by backend RPCs, webhooks, or the account deletion cascade:
* `ai_coach_daily_usage`: Shared daily message counter per `firebase_uid` and `local_date` with reservation concurrency locks.
* `user_subscriptions`: In-app purchase entitlement status.
* `saved_meals` & `saved_meal_items`: User-saved meal templates.
* `recipes` & `recipe_items`: User custom recipes.
* `custom_foods`: User-created custom food items.
* `daily_goals`: Custom day-specific goal overrides.

---

## 2. AI Processing Data Flow

### 2.1 Food Photo Analysis (`POST /ai/analyze-photo`)
* **Transport:** Client sends JPEG/PNG/WebP encoded as Base64 in the request body (max 4 MB).
* **Backend Processing:**
  1. Validates magic bytes to authenticate image MIME type.
  2. Forwards the decoded image buffer directly to Google Gemini 2.5 Flash API via ephemeral memory stream.
  3. Receives structured JSON containing identified foods, estimated grams, calories, and macros.
* **Storage:**
  * **Memory:** Discarded immediately after the API call completes.
  * **Disk / Database:** **No image files or photo data are ever stored on backend disks, Supabase, Cloud Storage, or S3.**
  * **Backend Logs:** Only metadata (`bytes`, `mime`, `provider`, `model`) is recorded to `backend.log`.
  * **Local App:** The client saves the structured JSON result to local Room (`ai_analyses`).

### 2.2 Voice & Natural Text Analysis (`POST /ai/transcribe`, `POST /ai/analyze-text`)
* **Transport:** Client sends audio payload or raw text string.
* **Backend Processing:** Processed in-memory and passed to Gemini for natural language entity extraction.
* **Storage:** Audio buffers are ephemeral and immediately discarded. No audio recordings are stored anywhere on the backend or Supabase.

### 2.3 AI Coach Sproutling (`POST /ai/chat`)
* **Transport:** Client sends recent message history, user profile context (`goal`, `targetCalories`, `consumedCalories`, `remainingCalories`, `recentTrends`).
* **Backend Storage:**
  * **Chat Transcripts:** The backend **does NOT store** chat messages or conversation transcripts during `/ai/chat`.
  * **Quota Tracking:** The backend atomically updates `ai_coach_daily_usage` (`used_count`, `in_flight`) in Supabase.
* **App Storage:** The full chat thread is persisted locally in the client's Room database (`conversations` and `messages`) and only synced to Supabase when the user performs a manual sync.

---

## 3. Account Deletion & Data Purge (`DELETE /account`)

When a user initiates account deletion from the app or web portal:
1. Backend authenticates the Firebase UID.
2. Cascading purge executes across all 15 user-bound tables in Supabase:
   * `food_log_items` (via `meal_logs`)
   * `meal_logs`
   * `nutrition_goals`
   * `weight_entries`
   * `water_logs`
   * `ai_analysis_log`
   * `messages` (via `conversations`)
   * `conversations`
   * `user_profiles`
   * `user_subscriptions`
   * `saved_meal_items` & `saved_meals`
   * `recipe_items` & `recipes`
   * `custom_foods`
   * `daily_goals`
   * `ai_coach_daily_usage`
3. Firebase Auth record is revoked/deleted.
4. Local app database is cleared on user confirmation.
