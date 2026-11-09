# Architecture & Project Decisions Log (ADR)

This document records the architectural decision records (ADRs) for the KcalGrind project.

---

## ADR 1: Canonical Rebrand to KcalGrind

* **Date:** 2026-09-13
* **Status:** Accepted

### Context
Prior to initial Google Cloud Run deployment, Firebase integration, and Google Play Store preparation, the project required a unified, distinct brand identity.

### Decision
* Officially rebranded to **KcalGrind** (Tagline: "Calorie & Nutrition Tracking Built for Android").
* Package ID and application namespace set permanently to `com.kcalgrindai.app`.
* Backend service name on Google Cloud Run: `kcalgrindai-backend`.
* Retained "Vitality Flow" as the internal Material 3 design system name.
* Official domains and endpoints: `https://kcalgrind.app` with legal endpoints `/privacy` and `/delete-account`.

### Consequences
* Eliminates costly live package migrations or keystore re-generation later.
* Establishes permanent brand identity across code, drawables, strings, backend services, and web properties.

---

## ADR 2: Ratification of KcalGrind Project Constitution

* **Date:** 2026-11-07
* **Status:** Accepted

### Context
To govern ongoing engineering work, prevent architectural drift, ensure compliance with Google Play Store policies, and maintain truthful privacy representations across the mobile app, backend, and web properties, the project required an explicit, binding set of non-negotiable principles.

### Decision
Ratified **Version 1.0.0** of the KcalGrind Project Constitution at [`.specify/memory/constitution.md`](file:///d:/CAloriesssss/.specify/memory/constitution.md) and [`Docs/CONSTITUTION.md`](file:///d:/CAloriesssss/Docs/CONSTITUTION.md) containing 11 core principles:
1. **Nutrition Source of Truth:** AI is never the source of truth for nutrition; mandatory provenance tagging (`source`, `confidence`) and user portion review.
2. **Offline-First Architecture:** Local SQLite / Room database is the primary source of truth; core diary, Indian food search (ICMR-NIN IFCT 2017), water tracking, and Glance widgets work 100% offline.
3. **Deterministic Nutrition Math:** Single deterministic calculator with safety guardrails (calorie floors 1,200/1,500 kcal, 1% body weight/week deficit cap, age 18+).
4. **Truthful Claims & Privacy Integrity:** Zero fabricated claims; exact match with `Docs/DATA_FLOW.md` (ephemeral in-memory AI processing, manual-only sync, no stored photos/audio); visible medical disclaimers.
5. **Database Migration Integrity:** Non-destructive, idempotent Room and Supabase migrations; Supabase RLS on all tables; quota RPCs restricted to `service_role` with `SET search_path = public`.
6. **Comprehensive Account Deletion:** Permanent cascading purge across all 15+ user-bound Supabase tables and Firebase Auth; audited in CI against live schema.
7. **Fail-Closed Production Security:** Reject test tokens (`test-token-*`) in `NODE_ENV=production`; return `HTTP 503` on quota RPC failure rather than falling back to unmetered local counts.
8. **Empirical Verification:** Raw test outputs and passing CI; verify concurrency on real PostgreSQL containers; test mobile hardware capabilities on physical Android devices.
9. **Canonical Brand Identity:** Canonical product name **KcalGrind**, resident coach **Sproutling**, package name `com.kcalgrindai.app`.
10. **Design Token Hierarchy:** **Vitality Flow** Material 3 tokens are the design source of truth imported by web/widgets; 4.5:1 contrast, 48dp touch targets, reduced motion.
11. **Scope Discipline:** Strict adherence to requested directories; zero new features during hardening passes; zero committed secrets.

### Consequences
* All future specifications (`/speckit-specify`), architectural plans (`/speckit-plan`), and implementations (`/speckit-implement`) must validate against these 11 constitutional principles.
* Any code change violating privacy truth, migration idempotency, or safety floors is considered a critical regression.
