# Kcal Grind AI — Production & Deployment

## 1. Environments

- **Local/dev**: local backend (or dev Cloud Run instance), dev Firebase/Supabase project, test API keys with low rate limits.
- **Staging**: separate Firebase/Supabase project, staging backend, used for internal testing builds.
- **Production**: separate project again — never share Firebase/Supabase projects or API keys across environments.

## 2. CI/CD

- GitHub Actions (or equivalent): on every PR — build, lint (ktlint/detekt), unit tests, Compose UI tests for critical flows.
- On merge to `main`: build a signed internal-test APK/AAB, upload to Play Console internal testing track automatically.
- Manual promotion (not automatic) from internal → closed testing → production.

## 3. Secrets management

- Backend AI/food-DB API keys live only in the backend host's secret manager (Cloud Run env vars via Secret Manager, or Supabase's secrets store) — never in the Android repo, never in `local.properties` committed to git.
- Android signing keys in a secure keystore, referenced via CI secrets, never committed.

## 4. Backend hosting specifics

- Cloud Run (or Fly.io) container running your Node/FastAPI proxy — scales to zero when idle, keeps costs low pre-launch.
- Set request timeouts appropriately for vision model calls (can take a few seconds) — don't let the mobile client time out before the backend does.
- Basic rate limiting per user ID to cap AI spend from any single account.

## 5. Monitoring

- Firebase Crashlytics for crash reporting.
- Backend: structured logging + a basic dashboard (Cloud Run's built-in metrics are enough at first) — track AI call volume, error rate, and average confidence score over time (a slow drift down in confidence is an early warning sign of model or prompt drift).
- Track AI cost per user per day — this is your main variable cost and the one most likely to surprise you.

## 6. Play Store release checklist

- Privacy policy covering: camera use, health data (Health Connect), AI processing of food photos/voice, data retention.
- Data safety form filled out accurately (what's collected, shared, and why) — get this wrong and Google will flag/delay the listing.
- Health Connect permissions justified clearly in the listing.
- Subscription/IAP terms clear if monetization ships (Phase 9) — Play Billing integration reviewed separately by Google.

## 7. Rollout strategy

- Start with a staged rollout (5% → 20% → 50% → 100%) on every production release once you have real users, not just the first launch — this catches device-specific crashes before they hit everyone.
- Keep a fast-rollback path (previous AAB ready to re-promote) for the first several releases.
