# KcalGrind â€” Production Launch Readiness Checklist & Audit

**Target Platforms:** Android 10+ (API 26 to 35) & Web Landing Engine  
**Release Target:** Google Play Store Production Track  
**Architecture:** Kotlin Jetpack Compose (Material3 "Vitality Flow"), Hilt, Room SQLite (v7 AutoMigrated), WorkManager, Health Connect, Jetpack Glance, Fastify Proxy (Cloud Run + Gemini 2.5 Flash).

---

## 1. Launch Blockers vs. Non-Blockers

### ðŸ”´ LAUNCH BLOCKERS (Must be 100% complete before Play Store review submission)

| # | Requirement | Status | Verification & Action Items |
| :--- | :--- | :---: | :--- |
| **B1** | **In-App Account Deletion & Data Purge** | **READY** | â€¢ Implemented in `ProfileScreen` -> `DeleteAccountUseCase`.<br>â€¢ Backend `DELETE /account` route deletes Supabase records and resets limits.<br>â€¢ Verified with 12 automated unit/integration tests.<br>â€¢ Documented in `Docs/ACCOUNT_DELETION_CHECKLIST.md`. |
| **B2** | **Web-Based Account Deletion URL** | **DOCS READY (BLOCKER)** | â€¢ Play Store policy requires a web URL for users to request account deletion without reinstalling the app.<br>â€¢ Complete web page copy, in-app steps, email workflow, and timeframe documented in `Docs/WEB_ACCOUNT_DELETION_PAGE.md`.<br>â€¢ Must be live at `https://kcalgrind.app/delete-account` before Play review submission. |
| **B3** | **Production Backend Deployment** | **READY FOR DEPLOY** | â€¢ Fastify backend with Gemini 2.5 Flash, Firebase token validation, and rate limiting.<br>â€¢ Needs production deployment to Google Cloud Run with production paid-tier `GEMINI_API_KEY` (paid tier is required by Google API terms to ensure user nutrition/photo inputs are not used for model training) and Supabase credentials. |
| **B4** | **Google Play Console Billing Products** | **DOCS READY** | â€¢ In-app subscriptions must be created in Play Console:<br>&nbsp;&nbsp;&nbsp;&nbsp;1. `kcalgrind_pro_monthly` (\$4.99/mo, 7d trial)<br>&nbsp;&nbsp;&nbsp;&nbsp;2. `kcalgrind_pro_annual` (\$39.99/yr, 7d trial)<br>â€¢ Service account credentials linked for backend verification (`Docs/BILLING_SETUP.md`). |
| **B5** | **Live Privacy Policy & Data Safety Form** | **DOCS READY** | â€¢ Publicly accessible URL required: `https://kcalgrind.app/privacy`.<br>â€¢ Play Console Data Safety form completed per `Docs/play-store-data-safety.md`. |
| **B6** | **Release Keystore & OAuth SHA-256** | **ACTION REQUIRED** | â€¢ Generate production upload keystore (`release.keystore`).<br>â€¢ Register release SHA-1 and SHA-256 fingerprints in Firebase Console -> Project Settings -> Android App (ensures Google Sign-In works in release builds). |

---

### ðŸŸ¡ NON-BLOCKERS (Safe for v1.0 Launch / Post-Launch Iterations)

| # | Feature / Improvement | Target Version | Description |
| :--- | :--- | :---: | :--- |
| **NB1** | **Recipe Builder (Phase G)** | **v1.1 (Post-Launch)** | Custom ingredient combination, yield recalculation, and multi-serving macro decomposition. |
| **NB2** | **Wearable Live Sync** | **v1.1** | Deep background synchronization with Garmin Connect and Samsung Health via Health Connect background read permissions. |
| **NB3** | **PDF Export Enhancements** | **v1.1** | Advanced vector styling, physician summary charts, and weekly macronutrient breakdown printable sheets. |
| **NB4** | **Localization & Internationalization** | **v1.2** | Hindi, Spanish, German, and French string catalogs. |

---

## 2. Real-Device Physical Testing Matrix

*The following items cannot be fully verified inside headless emulators or CI/CD pipelines and require verification on physical Android test hardware before production rollout:*

| Test Category | Physical Device Requirement | Reason Emulator Cannot Verify | Manual Verification Steps |
| :--- | :--- | :--- | :--- |
| **1. Google Play Billing** | Physical phone with logged-in Google Play account | Emulators lack Play Store checkout sheet and license tester payment flow | 1. Sign in to physical phone with licensed tester account.<br>2. Open Paywall from Profile / AI Coach limit banner.<br>3. Tap "Subscribe" for Monthly / Annual.<br>4. Confirm Google Play bottom sheet displays \$0.00 test purchase.<br>5. Confirm `isPro = true` immediately unlocks 50 messages/day and full analytics. |
| **2. Health Connect** | Physical Android 14+ phone (or Android 13 with Health Connect APK) | Standard AOSP / Google APIs emulators lack Health Connect provider app and real sensor data | 1. Open Profile -> Health Connect.<br>2. Tap "Connect" -> grant Steps and Active Calories permissions.<br>3. Walk with phone or record workout in Google Fit / Samsung Health.<br>4. Verify steps and active calories sync correctly into KcalGrind. |
| **3. Android 13+ Notification Runtime Permission** | Physical device running Android 13, 14, or 15 | Emulators auto-grant or bypass OEM background battery killing policies | 1. Toggle Notifications ON in Profile.<br>2. Confirm system prompt: "Allow KcalGrind to send notifications?".<br>3. Deny -> verify app degrades gracefully without crashing.<br>4. Enable -> verify WorkManager periodic notifications arrive during waking hours. |
| **4. Glance Home Screen Widgets** | Physical launcher (Pixel Launcher, Samsung One UI, Nova) | Emulator launcher often does not support AppWidget Host resizing and glance state updates | 1. Long-press home screen -> Add "KcalGrind (4x2 / 2x2 / 4x1)".<br>2. Tap quick-log action (e.g., "+250ml Water").<br>3. Verify widget counter updates reactively.<br>4. Verify privacy mode toggle hides macro figures on locked home screen. |
| **5. CameraX Food Vision & Barcode Scanning** | Physical device with autofocus camera lens | Emulator simulated camera cannot autofocus on barcodes or capture textured food plates | 1. Open AI Scanner -> point at food plate (e.g. Biryani, Salmon bowl).<br>2. Tap Capture -> verify Gemini 2.5 Flash identifies ingredients.<br>3. Point at grocery barcode -> verify ML Kit instantly resolves UPC. |
| **6. Google Sign-In with Release SHA-256** | Physical device with release signed APK / AAB | Debug keystores work locally; release keystores fail with `ApiException: 10` if SHA-256 is missing in Firebase | 1. Build signed release APK: `./gradlew assembleRelease`.<br>2. Install on device: `adb install -r release.apk`.<br>3. Tap "Continue with Google" -> select account.<br>4. Confirm authentication succeeds and redirects to Home. |
| **7. OEM Battery Optimization & Background Reminders** | Physical Samsung One UI, Xiaomi HyperOS/MIUI, and OnePlus OxygenOS devices | Emulators do not enforce aggressive OEM background process freezing, app standby buckets, or deep sleeping states | 1. Install app on Samsung / Xiaomi / OnePlus device.<br>2. Enable Meal and Hydration Reminders in Profile.<br>3. Verify behavior under default OEM battery settings ("Optimized" / "Intelligent Control").<br>4. Verify reminders fire as expected or prompt user to disable battery restrictions if delayed.<br>5. Confirm at most one notification per meal type per day is delivered. |

---

## 3. Launch Verification Summary

- **Unit Test Coverage:** 100% green across all 46+ test suites.
- **Database Migrations:** Room v1 through v7 validated with `KcalGrindDatabaseMigrationTest` (zero destructive migrations).
- **Design System Consistency:** "Vitality Flow" Material3 color tokens (`#2d6a4f` Forest Green, `#646fd4` AI Periwinkle, Roboto Flex / Inter typography) respected across all screens.
- **Offline Reliability:** All diary, saved meal, custom food, and local calorie tracking work completely without internet.


---

## 4. Supabase Staging Purge Verification Checklist

Before releasing to Google Play production, run `DELETE /account` against a real Supabase staging database instance and execute the following SQL verification checklist to confirm zero rows remain across all 17 user tables:

### Pre-Purge Setup
1. Create a dedicated test user account in Firebase Auth (`staging-delete-user@test.kcalgrind.app`).
2. Seed test records for this `firebase_uid` across all 17 relational database tables:
   - `user_profiles` (1 row)
   - `nutrition_goals` (1 row)
   - `daily_goals` (7 rows)
   - `meal_logs` (3 rows)
   - `food_log_items` (6 rows)
   - `weight_entries` (5 rows)
   - `water_logs` (7 rows)
   - `ai_analysis_log` (3 rows)
   - `conversations` (2 rows)
   - `messages` (10 rows)
   - `saved_meals` (2 rows)
   - `saved_meal_items` (4 rows)
   - `recipes` (2 rows)
   - `recipe_items` (6 rows)
   - `custom_foods` (4 rows)
   - `user_subscriptions` (1 row)
   - `ai_coach_daily_usage` (2 rows)

### Execution
1. Send authenticated purge request:
   ```bash
   curl -X DELETE "https://api-staging.kcalgrind.app/account" \
     -H "Authorization: Bearer [FIREBASE_ID_TOKEN]"
   ```
2. Verify HTTP `200 OK` response:
   ```json
   { "purged": true, "uid": "[STAGING_UID]", "timestamp": "..." }
   ```

### Post-Purge SQL Verification Checklist
Execute the following verification query in the Supabase SQL Editor:
```sql
SELECT
  (SELECT COUNT(*) FROM public.user_profiles WHERE firebase_uid = '[STAGING_UID]') AS user_profiles_count,
  (SELECT COUNT(*) FROM public.nutrition_goals WHERE firebase_uid = '[STAGING_UID]') AS nutrition_goals_count,
  (SELECT COUNT(*) FROM public.daily_goals WHERE firebase_uid = '[STAGING_UID]') AS daily_goals_count,
  (SELECT COUNT(*) FROM public.meal_logs WHERE firebase_uid = '[STAGING_UID]') AS meal_logs_count,
  (SELECT COUNT(*) FROM public.food_log_items WHERE meal_log_id IN (SELECT id FROM public.meal_logs WHERE firebase_uid = '[STAGING_UID]')) AS food_log_items_count,
  (SELECT COUNT(*) FROM public.weight_entries WHERE firebase_uid = '[STAGING_UID]') AS weight_entries_count,
  (SELECT COUNT(*) FROM public.water_logs WHERE firebase_uid = '[STAGING_UID]') AS water_logs_count,
  (SELECT COUNT(*) FROM public.ai_analysis_log WHERE firebase_uid = '[STAGING_UID]') AS ai_analysis_log_count,
  (SELECT COUNT(*) FROM public.conversations WHERE firebase_uid = '[STAGING_UID]') AS conversations_count,
  (SELECT COUNT(*) FROM public.messages WHERE conversation_id IN (SELECT id FROM public.conversations WHERE firebase_uid = '[STAGING_UID]')) AS messages_count,
  (SELECT COUNT(*) FROM public.saved_meals WHERE firebase_uid = '[STAGING_UID]') AS saved_meals_count,
  (SELECT COUNT(*) FROM public.saved_meal_items WHERE saved_meal_id IN (SELECT id FROM public.saved_meals WHERE firebase_uid = '[STAGING_UID]')) AS saved_meal_items_count,
  (SELECT COUNT(*) FROM public.recipes WHERE firebase_uid = '[STAGING_UID]') AS recipes_count,
  (SELECT COUNT(*) FROM public.recipe_items WHERE recipe_id IN (SELECT id FROM public.recipes WHERE firebase_uid = '[STAGING_UID]')) AS recipe_items_count,
  (SELECT COUNT(*) FROM public.custom_foods WHERE firebase_uid = '[STAGING_UID]') AS custom_foods_count,
  (SELECT COUNT(*) FROM public.user_subscriptions WHERE user_id = '[STAGING_UID]') AS user_subscriptions_count,
  (SELECT COUNT(*) FROM public.ai_coach_daily_usage WHERE firebase_uid = '[STAGING_UID]') AS ai_coach_daily_usage_count;
```

### Acceptance Criteria
- [ ] Every single column in the result table returns **0**.
- [ ] Other user data in the staging database is completely unaffected.
- [ ] Subsequent requests with the purged UID return 401/403 and do NOT recreate a profile row.
- [ ] In-memory/Redis rate limiting counters for the purged UID are completely reset.
- [ ] Staging purge completeness audit script passes: `npm run audit:purge`.
