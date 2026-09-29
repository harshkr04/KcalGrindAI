# Kcal Grind AI — Tech Stack

## Client (Android)

| Concern | Choice | Why |
|---|---|---|
| Language | Kotlin | Standard for modern Android |
| UI | Jetpack Compose + Material 3 | Matches "Material 3 Expressive" design direction already chosen |
| Min/Target SDK | minSdk 26, targetSdk 34/35 | Covers ~98% of active devices; confirm against Play Console stats before locking |
| DI | Hilt | Standard, low boilerplate, good Compose integration |
| Local DB | Room | Confirmed by original spec; maps directly from the localStorage schemas already validated in the prototype |
| Async | Kotlin Coroutines + Flow | Standard; Flow lets Room emit live UI updates |
| Networking | Retrofit + OkHttp + Moshi/kotlinx.serialization | Backend proxy + food DB API calls |
| Navigation | Navigation Compose | Type-safe routes |
| Image loading | Coil | Compose-native, handles camera capture previews well |
| Camera | CameraX | Simpler lifecycle handling than raw Camera2 |
| Barcode scanning | ML Kit Barcode Scanning (on-device) | Free, offline, fast — no need for a paid SDK |
| Speech-to-text | Android SpeechRecognizer, or a cloud STT (Google Cloud Speech / Whisper API) if on-device accuracy is insufficient | Start on-device, upgrade only if needed |
| Health data | Health Connect | Already specified; standard for Android fitness/activity/weight sync |
| Background work | WorkManager | Retry/sync for AI calls, reminders |
| Crash/analytics | Firebase Crashlytics + Firebase Analytics (or PostHog if you want self-hosted analytics) | |
| Local prefs | DataStore (Preferences) | Replaces simple flags that don't need full Room tables |

## Backend (required — do not skip)

| Concern | Choice | Why |
|---|---|---|
| Runtime | Node.js (Fastify/Express) or Python (FastAPI) — pick whichever you're more fluent in | Thin proxy, logic is not heavy |
| Hosting | Cloud Run, Fly.io, or Supabase Edge Functions | Serverless, scales to zero, cheap at low volume |
| AI model — vision (photo analysis) | NVIDIA Llama 3.2 Vision (`meta/llama-3.2-11b-vision-instruct`), Gemini 2.x, or GPT-4o | Handles multimodal food-photo identification and structured JSON |
| AI model — text/chat | Same provider as vision, function-calling enabled (`meta/llama-3.2-11b-vision-instruct`) | Single provider, unified proxy |
| AI model — speech-to-text | Provider's native STT, or Whisper API if you want provider independence | |
| Auth | Firebase Auth or Supabase Auth | Handles email/password + Google/Apple sign-in without building it yourself |
| Backend DB (if any server-side data needed — e.g., cross-device sync) | Postgres (Supabase/Neon) | Only needed once you support multi-device sync; local-only can defer this |
| Secrets | Cloud provider's secret manager (never `.env` committed, never in APK) | |

> **Note on Model Volatility & Deprecations (Revisit before Phase 9):**
> NVIDIA free-tier model catalog names on `integrate.api.nvidia.com` are volatile and experience abrupt end-of-life deprecations (e.g., `meta/llama-3.3-70b-instruct` and `nvidia/nemotron-3.5-lightning-30b-a3b` returned HTTP 410 Gone with little notice). For long-term production stability, keep backend model identifiers purely environment-variable driven (`VISION_MODEL`, `TEXT_MODEL`, `CHAT_MODEL`) and consider transitioning to standard Google Gemini (Vertex AI / AI Studio) or OpenAI endpoints before Phase 9 production hardening.

## Food & nutrition data

| Concern | Choice | Why |
|---|---|---|
| Primary food database | USDA FoodData Central (free, no key limits) | Best free option, good coverage for whole foods |
| Branded/packaged foods | Open Food Facts (free) or Nutritionix (paid, better branded coverage + barcode hit rate) | USDA is weak on packaged/branded items |
| Barcode lookup | Open Food Facts API first, fall back to Nutritionix if you pay for it | |

## Design tooling (already in use)

- Google Stitch — existing screen designs, "Vitality Flow" design system (Roboto Flex + Inter, Deep Forest Green #2d6a4f, Periwinkle #646fd4).
- Keep using Stitch exports as the visual reference; do not let engineering redesign screens ad hoc.

## Explicitly deferred (don't build until a real phase calls for it)

- Multi-module Gradle setup
- Native iOS build / KMP sharing
- Subscription/monetization SDK (RevenueCat, Play Billing) — Phase 9 only
- Custom analytics pipeline beyond Firebase — only if Firebase proves insufficient
