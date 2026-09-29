# Architectural & Project Decisions Log

## 2026-09-13: Rebrand Lumina → Kcal Grind AI
- **Context:** The app is being rebranded from "Lumina" / "Lumina Nutrition" to "Kcal Grind AI" (Tagline: "AI-Powered Calorie Tracker") prior to initial Google Cloud Run deployment and Google Play Store submission.
- **Decision:** Renamed application ID, package namespace, Firebase configuration, Cloud Run service name, Secret Manager secret keys, keystore templates, documentation, and metadata before any live deployment to avoid costly live migrations, user data redirects, or keystore alias re-issuance later.
- **Package ID:** `com.kcalgrindai.app`
- **Cloud Run Service:** `kcalgrindai-backend`
- **Secret Names:** `kcalgrindai-gemini-api-key`, `kcalgrindai-nvidia-api-key`
- **Keystore / Alias:** `kcalgrindai-release-key.jks` / `kcalgrindai-release-alias`
- **Domain & URLs:** `https://kcalgrind.ai` (contact: `privacy@kcalgrind.ai`, deletion: `https://kcalgrind.ai/delete-data`)
- **Design System Exception:** Retained "Vitality Flow" as the internal design system moniker per specification.
