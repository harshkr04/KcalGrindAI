# KcalGrind Project Constitution

> Canonical Spec Kit location: [`.specify/memory/constitution.md`](file:///d:/CAloriesssss/.specify/memory/constitution.md)

## Core Principles

### I. Nutrition Source of Truth & Provenance
AI is never the source of truth for nutrition data. Every food entry logged or displayed in the application MUST carry explicit provenance metadata (`source` and `confidence`). Provenance MUST be visibly identified in the user interface as verified (`USDA`, `ICMR-NIN`), user-entered (`Custom`), or AI estimate (`Photo`, `Voice`, `Text`). AI-estimated values require mandatory user confirmation and portion review before being committed to the food diary.

### II. Offline-First Architecture
The local SQLite / Room database on the Android device is the single source of truth for all user logs, meals, water entries, custom foods, weight entries, and historical data. Core application capabilities—including food diary CRUD, offline food search across the ICMR-NIN (IFCT 2017) dataset, water tracking, and home-screen widgets—MUST operate seamlessly without network connectivity. Network access is reserved strictly for remote AI inference, on-demand cloud sync, and online barcode catalog lookup.

### III. Deterministic Nutrition Calculator & Safety Guardrails
All nutritional targets, energy expenditure math (BMR, TDEE), macro splits, and deficit caps MUST execute through one centralized, deterministic calculation engine. Calculations MUST enforce non-negotiable app guardrails:
- Calorie floors: Absolute daily minimum of 1,500 kcal for males and 1,200 kcal for females and unspecified gender profiles.
- Deficit rate cap: Caloric deficits MUST NOT exceed 1% of total body weight loss per week ($\approx 11 \cdot W\text{ kcal/day}$).
- Age gating: Minimum age to use the application is 13. Weight-change goals (weight loss, surplus/gain) and caloric target adjustments are strictly restricted to adults aged 18 and older.

### IV. Truthful Claims, Privacy Integrity & Medical Disclaimers
Marketing materials, landing pages, application copy, and legal documents MUST NOT contain fabricated testimonials, fictional statistics, or unsubstantiated performance claims. Every privacy declaration MUST accurately describe actual system behavior as documented in `Docs/DATA_FLOW.md` and `Docs/DATA_SAFETY.md`:
- We do not store photos or audio: meal photos and voice recordings are processed in memory for analysis and are never stored on backend disks, Supabase, or cloud object stores.
- Real-time AI endpoints do not store chat transcripts or media files; transcripts and logs reside locally on-device.
- Cloud database sync is strictly manual on-demand ("Sync now") with zero background automated sync in v1, uploading local database entities to the user's private cloud backup.
- Prominent not-medical-advice disclaimers MUST accompany all calculator, target, and AI coaching screens.

### V. Database Migration Integrity & Defense-in-Depth
Database schema migrations across local Room SQLite (Android) and remote Supabase PostgreSQL MUST be non-destructive, strictly sequenced, idempotent, and verified by automated migration tests. In Supabase:
- Row Level Security (RLS) MUST be enabled on every table.
- Quota-critical database functions (such as `reserve_ai_coach_quota`) MUST enforce `SET search_path = public`, have permissions revoked from `PUBLIC`, `anon`, and `authenticated`, and be granted exclusively to `service_role`.

### VI. Comprehensive Account Deletion & Automated Purge Audits
The account deletion system (`DELETE /account` and client `DeleteAccountUseCase`) MUST permanently and atomically purge all user-associated records across all 17 user-bound tables in Supabase (`user_profiles`, `nutrition_goals`, `daily_goals`, `meal_logs`, `food_log_items`, `weight_entries`, `water_logs`, `ai_analysis_log`, `conversations`, `messages`, `saved_meals`, `saved_meal_items`, `recipes`, `recipe_items`, `custom_foods`, `user_subscriptions`, `ai_coach_daily_usage`) and delete the Firebase Auth user record. The completeness of this deletion cascade MUST be verified automatically in CI against the live database schema.

### VII. Fail-Closed Production Security
The production environment MUST fail closed under all circumstances. Development credentials, test tokens (`test-token-*`), and mock bypasses MUST be rejected unconditionally in `NODE_ENV=production`. If quota reservation RPCs or backend services encounter database errors in production, the application MUST return `HTTP 503 Service Unavailable` rather than falling back to an unmetered or non-atomic local counter.

### VIII. Empirical Verification & Real Environment Testing
Verification requires raw, verifiable test output and a green continuous integration pipeline. Mocks and unit stubs do not prove concurrency safety, transaction isolation, security boundaries, or device behavior. Quota concurrency and race conditions MUST be tested against a real PostgreSQL container with at least 20 parallel connections. Critical mobile capabilities (Google Play Billing, widgets, Health Connect permissions, CameraX food vision, and Google Sign-In with release SHA-256) MUST be validated on physical hardware devices.

### IX. Canonical Brand Identity & Package Namespace
The user-facing product name is strictly **KcalGrind** (single word, camel-cased). The resident AI coach is named **Sproutling**, visually rendered solely using genuine `ic_sproutling_*` assets. The canonical Android package namespace and application ID is permanently fixed as `com.kcalgrindai.app` and MUST NOT be altered across build flavors or environments.

### X. Design Token Hierarchy & Accessibility
The Android application's `Color.kt` tokens are the single design source of truth for color palettes, typography, spacing, and elevation. The web landing engine and widgets MUST import and mirror these tokens directly, anchored by the signature circular **Energy Ring**. All interfaces MUST satisfy accessibility standards:
- Contrast ratio of at least 4.5:1 for standard text (WCAG AA).
- Minimum interactive touch targets of 48×48 dp.
- Strict compliance with system reduced-motion settings.

### XI. Scope Discipline & Secret Hygiene
Engineering agents and contributors MUST adhere to strict scope discipline:
- File modifications MUST be restricted solely to the specific directories and files requested.
- No new features, speculative abstractions, or architectural expansions may be introduced during stabilization or hardening passes.
- API keys, service account JSON secrets, release keystores, and credentials MUST NEVER be committed to version control.

## Quality Gates & Verification Standards

1. **Pre-Release Test Gate:** All automated test suites (Room migration tests, backend Jest/Node suites, Supabase purge audits) must pass with 100% green status before release builds are cut.
2. **Deterministic Math Proof:** All user-facing nutrition summaries must mathematically reconcile: $\text{Calories} = 4 \times \text{Protein(g)} + 4 \times \text{Carbs(g)} + 9 \times \text{Fat(g)}$.
3. **Data Safety Reconciliation:** Any change to backend routes or client sync fields requires a mandatory synchronized audit of `Docs/DATA_FLOW.md` and `Docs/DATA_SAFETY.md`.

## Development Workflow & Hardening Constraints

1. **Hardening Mode:** During hardening passes, tasks are strictly confined to bug fixes, security enhancements, test regressions, and documentation alignment.
2. **Clean Builds:** The Android app must compile warning-clean on target SDK 35, and the website build (`tsc -b && vite build`) must pass without errors.
3. **Emulator & Hardware Sign-Off:** Core user flows (auth, offline diary, widget resize, photo analysis) must be screenshotted and validated on the reference Pixel 8 emulator or physical test devices.

## Governance

This Constitution represents the supreme architectural and operational contract for KcalGrind. All specifications, implementations, pull requests, and automated agent workflows MUST conform to these principles.

- **Amendment Procedure:** Amendments require explicit documentation of rationale, consensus approval, a semver version increment, and an updated Sync Impact Report.
- **Precedence:** In the event of any ambiguity or conflict between existing legacy documentation, prototypes, or user prompts, this Constitution supersedes all other guidelines.

**Version**: 1.0.1 | **Ratified**: 2026-11-07 | **Last Amended**: 2026-11-07
