# KcalGrind â€” Tech Stack

## Client (Android)

| Concern | Choice | Why |
|---|---|---|
| Language | Kotlin | Standard for modern Android |
| UI | Jetpack Compose + Material 3 | Matches "Material 3 Expressive" design direction already chosen |
| Min/Target SDK | minSdk 29, targetSdk 34/35 | Covers ~98% of active devices; confirm against Play Console stats before locking |
| DI | Hilt | Standard, low boilerplate, good Compose integration |
| Local DB | Room | Confirmed by original spec; maps directly from the localStorage schemas already validated in the prototype |
| Async | Kotlin Coroutines + Flow | Standard; Flow lets Room emit live UI updates |
| Networking | Retrofit + OkHttp + Moshi/kotlinx.serialization | Backend proxy + food DB API calls |
| Navigation | Navigation Compose | Type-safe routes |
| Image loading | Coil | Compose-native, handles camera capture previews well |
| Camera | CameraX | Simpler lifecycle handling than raw Camera2 |
| Barcode scanning | ML Kit Barcode Scanning (on-device) | Free, offline, fast â€” no need for a paid SDK |
| Speech-to-text | Android SpeechRecognizer, or a cloud STT (Google Cloud Speech / Whisper API) if on-device accuracy is insufficient | Start on-device, upgrade only if needed |
| Health data | Health Connect | Already specified; standard for Android fitness/activity/weight sync |
| Background work | WorkManager | Retry/sync for AI calls, reminders |
| Crash/analytics | Firebase Crashlytics + Firebase Analytics (or PostHog if you want self-hosted analytics) | |
| Local prefs | DataStore (Preferences) | Replaces simple flags that don't need full Room tables |

## Backend (required â€” do not skip)

| Concern | Choice | Why |
|---|---|---|
| Runtime | Node.js (Fastify/Express) or Python (FastAPI) â€” pick whichever you're more fluent in | Thin proxy, logic is not heavy |
| Hosting | Cloud Run, Fly.io, or Supabase Edge Functions | Serverless, scales to zero, cheap at low volume |
| AI model â€” vision (photo analysis) | Google Gemini 2.5 Flash (`gemini-2.5-flash`) | Handles multimodal food-photo identification and structured JSON |
| AI model â€” text/chat | Google Gemini 2.5 Flash (`gemini-2.5-flash`) | Unified multimodal foundation model with function calling |
| AI model â€” speech-to-text | Android SpeechRecognizer / Gemini text analysis | Free on-device speech-to-text with AI text structuring |
| Auth | Firebase Auth | Handles email/password + Google sign-in and anonymous accounts |
| Backend DB (cross-device sync) | Supabase Postgres | Encrypted cloud sync for profiles and meal history |
| Secrets | Google Cloud Secret Manager (never `.env` committed, never in APK) | |

## Food & nutrition data

| Concern | Choice | Why |
|---|---|---|
| Primary food database | USDA FoodData Central (free, no key limits) | Best free option, good coverage for whole foods |
| Branded/packaged foods | Open Food Facts (free) or Nutritionix (paid, better branded coverage + barcode hit rate) | USDA is weak on packaged/branded items |
| Barcode lookup | Open Food Facts API first, fall back to Nutritionix if you pay for it | |

## Design tooling (already in use)

- Google Stitch â€” existing screen designs, "Vitality Flow" design system (Roboto Flex + Inter, Deep Forest Green #2d6a4f, Periwinkle #646fd4).
- Keep using Stitch exports as the visual reference; do not let engineering redesign screens ad hoc.

## Explicitly deferred (don't build until a real phase calls for it)

- Multi-module Gradle setup
- Native iOS build / KMP sharing
- Subscription/monetization SDK (RevenueCat, Play Billing) â€” Phase 9 only
- Custom analytics pipeline beyond Firebase â€” only if Firebase proves insufficient
