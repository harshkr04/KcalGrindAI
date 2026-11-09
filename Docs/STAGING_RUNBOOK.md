# KcalGrind Staging Deployment & Hardening Runbook

> **Target Audience:** Engineering, DevOps, SRE  
> **Environment:** Staging (`staging`)  
> **App:** KcalGrind (Android App + Node.js Cloud Run Proxy + Supabase DB)  
> **Last Updated:** 2026-11-06

---

## 1. Overview & Prerequisites

This runbook documents the step-by-step procedures to deploy, verify, and harden the KcalGrind backend and database in a live Staging environment before opening to production users.

### Required Tools & Permissions
- **Google Cloud SDK (`gcloud`)**: Authenticated to GCP project `kcalgrind-staging` with Cloud Run Admin & Secret Manager Admin roles.
- **Supabase CLI (`supabase`)**: Logged in via `supabase login`.
- **Node.js 20+ & npm**: For running test suites and purge verification audits.
- **cURL & jq**: For parallel HTTP concurrency testing and API verification.

---

## 2. Step 1: Create & Configure Supabase Staging Project

1. **Create Project via Supabase Dashboard or CLI:**
   ```bash
   supabase projects create --org-id <your-org-id> kcalgrind-staging
   ```
2. **Collect Credentials:**
   From the Supabase Dashboard (`Settings -> API`), securely extract:
   - `SUPABASE_URL`: e.g. `https://<project-ref>.supabase.co`
   - `SUPABASE_SERVICE_KEY`: `service_role` secret key (bypasses RLS, used exclusively by Cloud Run backend).
   - `SUPABASE_ANON_KEY`: `anon` public key (used for unauthenticated public clients and security rejection verification).

---

## 3. Step 2: Apply Canonical Database Migrations

All canonical SQL migrations live in `supabase/migrations/` (with a symlink/pointer in `backend/supabase/migrations/`).

1. **Link CLI to Staging Project:**
   ```bash
   supabase link --project-ref <project-ref>
   ```

2. **Deploy Migrations:**
   ```bash
   supabase db push
   ```

   **Applied Migration Sequence:**
   - `001_initial_schema.sql`: Core relational tables (`user_profiles`, `nutrition_goals`, `meal_logs`, `food_log_items`, `weight_entries`, `water_logs`, `ai_analysis_log`, `conversations`, `messages`).
   - `002_ai_cleanup_cron.sql`: Automated maintenance cron to truncate AI analysis logs older than 30 days.
   - `003_ai_coach_daily_usage.sql`: Shared AI Coach daily quota storage, row-level atomic locking (`SELECT ... FOR UPDATE`), first-insert race fix, rolling 24-hour backstop, `SET search_path = public`, and strict permission grant to `service_role` only.
   - `004_user_custom_data_and_subscriptions.sql`: Relational tables with RLS for custom foods, saved meals, recipes, and Google Play subscriptions (`daily_goals`, `custom_foods`, `saved_meals`, `saved_meal_items`, `recipes`, `recipe_items`, `user_subscriptions`).

3. **Verify Migration Completeness:**
   Run the live audit script against the Supabase `information_schema`:
   ```bash
   cd backend
   SUPABASE_URL="https://<project-ref>.supabase.co" \
   SUPABASE_SERVICE_KEY="<service-role-key>" \
   npm run audit:purge -- --live
   ```
   **Expected Output:**
   ```
   === Running Supabase Staging Purge Completeness Audit (LIVE MODE) ===
   Discovered 17 user-bound relational tables:
     ...
   ✅ AUDIT PASSED: All 17 user-bound tables are verified in the account purge list.
   ```

---

## 4. Step 3: Deploy Backend to Google Cloud Run (Staging)

1. **Verify Environment Configuration:**
   Ensure production security guardrails are active:
   - `NODE_ENV=production`
   - `FIREBASE_PROJECT_ID=kcalgrind-staging`
   - `ALLOW_DEV_TOKENS` is **UNSET** (or strictly `false`).
   - `FIREBASE_AUTH_EMULATOR_HOST` is **UNSET**.
   - `SUPABASE_URL=https://<project-ref>.supabase.co`
   - `SUPABASE_SERVICE_KEY` mounted from GCP Secret Manager.
   - `GEMINI_API_KEY` mounted from GCP Secret Manager.

2. **Deploy Service:**
   ```bash
   cd backend
   gcloud run deploy kcalgrindai-backend-staging \
     --image gcr.io/kcalgrind-staging/backend:latest \
     --platform managed \
     --region us-central1 \
     --allow-unauthenticated \
     --set-env-vars NODE_ENV=production,FIREBASE_PROJECT_ID=kcalgrind-staging \
     --set-secrets SUPABASE_URL=SUPABASE_URL:latest,SUPABASE_SERVICE_KEY=SUPABASE_SERVICE_KEY:latest,GEMINI_API_KEY=GEMINI_API_KEY:latest \
     --min-instances 1 \
     --max-instances 10
   ```

3. **Verify Health Endpoint:**
   ```bash
   curl -i https://<staging-cloud-run-url>/health
   ```
   **Expected Response:** HTTP 200 `{"status":"ok"}`.

---

## 5. Step 4: Concurrency Verification (20 Concurrent Requests vs Limit 5)

To guarantee that the Supabase RPC atomic locking prevents race conditions across multiple Cloud Run instances, execute 20 concurrent requests for a Free-tier user (daily limit = 5):

```bash
#!/usr/bin/env bash
STAGING_URL="https://<staging-cloud-run-url>"
TEST_TOKEN="<valid-staging-firebase-id-token>"

echo "Triggering 20 concurrent requests to POST /ai/chat..."
for i in {1..20}; do
  curl -s -o /tmp/resp_$i.json -w "%{http_code}\n" \
    -X POST "$STAGING_URL/ai/chat" \
    -H "Authorization: Bearer $TEST_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"messages":[{"role":"user","content":"Quick protein check"}],"userContext":{}}' &
done
wait

echo "--- Response Status Code Summary ---"
cat /tmp/resp_*.json | grep -c "200" || true
cat /tmp/resp_*.json | grep -c "429" || true
```

### Verification Criteria:
- **Exactly 5 requests** return `HTTP 200 OK`.
- **Exactly 15 requests** return `HTTP 429 Too Many Requests` with:
  ```json
  {
    "error": "QuotaExceeded",
    "message": "You've reached today's free AI Coach limit. Your messages reset tomorrow.",
    "usedToday": 5,
    "dailyLimit": 5,
    "remaining": 0
  }
  ```
- In Supabase table `ai_coach_daily_usage`:
  - `used_count = 5`
  - `in_flight = 0` (no dangling or negative values)

---

## 6. Step 5: Account Deletion Purge Verification (All 17 Tables)

1. **Seed Test Data:**
   Seed rows tied to `test_user_staging_purge` across all 17 tables (profiles, goals, water, meals, custom foods, recipes, subscriptions, usage, messages).

2. **Execute Account Deletion:**
   ```bash
   curl -i -X DELETE "$STAGING_URL/account" \
     -H "Authorization: Bearer <test-user-staging-token>"
   ```
   **Expected Response:** HTTP 200 `{"deleted": true, "uid": "test_user_staging_purge"}`.

3. **Verify Database Cleanliness (0 Rows in all 17 tables):**
   Execute SQL via Supabase SQL Editor:
   ```sql
   DO $$
   DECLARE
       v_uid TEXT := 'test_user_staging_purge';
       v_count INT;
   BEGIN
       SELECT COUNT(*) INTO v_count FROM public.user_profiles WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'user_profiles not purged';
       SELECT COUNT(*) INTO v_count FROM public.nutrition_goals WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'nutrition_goals not purged';
       SELECT COUNT(*) INTO v_count FROM public.meal_logs WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'meal_logs not purged';
       SELECT COUNT(*) INTO v_count FROM public.food_log_items WHERE meal_log_id NOT IN (SELECT id FROM public.meal_logs);
       ASSERT v_count = 0, 'orphan food_log_items remaining';
       SELECT COUNT(*) INTO v_count FROM public.weight_entries WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'weight_entries not purged';
       SELECT COUNT(*) INTO v_count FROM public.water_logs WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'water_logs not purged';
       SELECT COUNT(*) INTO v_count FROM public.ai_analysis_log WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'ai_analysis_log not purged';
       SELECT COUNT(*) INTO v_count FROM public.conversations WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'conversations not purged';
       SELECT COUNT(*) INTO v_count FROM public.messages WHERE conversation_id NOT IN (SELECT id FROM public.conversations);
       ASSERT v_count = 0, 'orphan messages remaining';
       SELECT COUNT(*) INTO v_count FROM public.ai_coach_daily_usage WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'ai_coach_daily_usage not purged';
       SELECT COUNT(*) INTO v_count FROM public.daily_goals WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'daily_goals not purged';
       SELECT COUNT(*) INTO v_count FROM public.custom_foods WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'custom_foods not purged';
       SELECT COUNT(*) INTO v_count FROM public.saved_meals WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'saved_meals not purged';
       SELECT COUNT(*) INTO v_count FROM public.saved_meal_items WHERE saved_meal_id NOT IN (SELECT id FROM public.saved_meals);
       ASSERT v_count = 0, 'orphan saved_meal_items remaining';
       SELECT COUNT(*) INTO v_count FROM public.recipes WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'recipes not purged';
       SELECT COUNT(*) INTO v_count FROM public.recipe_items WHERE recipe_id NOT IN (SELECT id FROM public.recipes);
       ASSERT v_count = 0, 'orphan recipe_items remaining';
       SELECT COUNT(*) INTO v_count FROM public.user_subscriptions WHERE firebase_uid = v_uid;
       ASSERT v_count = 0, 'user_subscriptions not purged';
       
       RAISE NOTICE 'SUCCESS: All 17 tables confirmed 0 rows remaining for purged user.';
   END $$;
   ```

---

## 7. Step 6: Security Verification — Anon RPC Execution Rejection

All quota RPC functions (`reserve_ai_coach_quota`, `commit_ai_coach_quota`, `release_ai_coach_reservation`) have execute privileges revoked from `PUBLIC`, `anon`, and `authenticated`. They may only be called by the `service_role`.

Execute an unauthorized direct call using the public `anon` key:

```bash
curl -i -X POST "https://<project-ref>.supabase.co/rest/v1/rpc/reserve_ai_coach_quota" \
  -H "apikey: <SUPABASE_ANON_KEY>" \
  -H "Authorization: Bearer <SUPABASE_ANON_KEY>" \
  -H "Content-Type: application/json" \
  -d '{
    "p_uid": "malicious_actor",
    "p_date": "2026-11-06",
    "p_limit": 5,
    "p_timezone": "UTC"
  }'
```

### Expected Response:
```
HTTP/1.1 401 Unauthorized
{"message":"permission denied for function reserve_ai_coach_quota"}
```
*(Or HTTP 404 Function Not Found depending on PostgREST version).*
**Direct client execution is strictly blocked.**

---

## 8. Rollback & Disaster Recovery

- **Cloud Run Rollback:**
  ```bash
  gcloud run services update-traffic kcalgrindai-backend-staging --to-revisions=<previous-revision>=100
  ```
- **Database Restoration:**
  If migration rollback is needed, restore from point-in-time recovery (PITR) snapshot or re-apply baseline schema.
