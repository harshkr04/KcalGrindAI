# Security Policy

## Supported Versions

Only the latest commit on the `main` branch is actively supported with security updates.

| Branch / Version | Supported          |
| ---------------- | ------------------ |
| `main`           | :white_check_mark: |
| Older releases   | :x:                |

---

## Reporting a Vulnerability

We take the security of KcalGrind and user health data seriously. If you discover a security vulnerability or sensitive information exposure, please report it privately.

**Please DO NOT open a public GitHub issue.**

### Reporting Channels:
1. **GitHub Private Vulnerability Reporting (Preferred):**
   Navigate to the repository on GitHub, open the **Security** tab, click **Report a vulnerability**, and submit your report via GitHub Security Advisories.
2. **Direct Security Contact:**
   <!-- TODO: Replace with your designated security contact email address -->
   Email: `security@TODO_YOUR_DOMAIN_OR_EMAIL.com` (or contact `@harshkr04` on GitHub).

### What to Include:
- Description of the vulnerability and its potential impact.
- Step-by-step reproduction instructions or a minimal proof-of-concept.
- Affected component(s) (e.g. Android client, backend route, authentication hook).
- Any suggested mitigations or patches.

### Our Commitment:
- **Acknowledgement:** We will acknowledge receipt of your vulnerability report within **7 days**.
- **Assessment & Fix:** We will assess the severity, collaborate with you on verifying the issue, and prepare a patch.
- **Responsible Disclosure:** We ask that you maintain confidentiality and refrain from public disclosure until an official fix is merged and released.

---

## Security-Sensitive Areas in Codebase

When auditing or submitting pull requests, pay close attention to the following security-sensitive areas:

1. **Authentication & Authorization (`backend/src/server.js`):**
   - The Fastify preHandler hook verifies incoming Firebase ID tokens using the Firebase Admin SDK.
   - All `/ai/*` and `/sync/*` routes enforce valid Bearer token authentication with non-empty user UIDs.
   - Public exceptions (`/health`, `/`) must never leak internal metrics, database state, or user data.

2. **AI Inference Proxy Endpoints (`backend/src/server.js`):**
   - `/ai/analyze-photo`, `/ai/analyze-text`, `/ai/transcribe`, and `/ai/chat`.
   - Ensure multimodal image and audio inputs are processed ephemerally in-memory and never cached to disk or public buckets.
   - Validate and sanitize structured JSON outputs returned to the client.

3. **Cloud Sync Routes (`/sync/push`, `/sync/pull`):**
   - Validate user ID isolation: queries must strictly enforce `.eq('firebase_uid', uid)` to prevent horizontal privilege escalation or cross-tenant data leakage.

4. **Quota Management & Rate Limiting (`backend/src/server.js`):**
   - In-memory rate limiting (`rateLimits` map) enforces sliding window boundaries (30 requests/hour/UID).
   - Atomic AI Coach quota reservation (`tryReserveAiCoachQuota`) prevents race condition abuse of Gemini 2.5 Flash API quotas.

5. **Health & Personal Data Storage:**
   - **Android Health Connect (`HealthConnectManager.kt`):** Only requests minimum required permissions (`READ_STEPS`). Data must remain on-device unless cloud backup is explicitly activated.
   - **Local Database (`KcalGrindDatabase.kt`):** Room SQLite database must never be accessible via unencrypted cloud backup. `android:allowBackup="false"` and `data_extraction_rules.xml` must remain intact.
   - **Crash Reporting (`KcalGrindAICrashReporter.kt`):** User IDs must never be attached to Crashlytics stack traces (`setUserId` is an explicit no-op).

6. **Secrets Management:**
   - Provider API keys (`GEMINI_API_KEY`, `SUPABASE_SERVICE_KEY`) must strictly reside in environment variables or Google Cloud Secret Manager.
   - No API keys, `google-services.json`, or release keystores (`keystore.properties`, `*.jks`) may ever be committed to git.

7. **Account Deletion & Data Removal (`DeleteAccountUseCase.kt`, `server.js`):**
   - `DELETE /account` must completely cascade and purge all relational user rows across all 16 tables.
   - The Android client must verify session freshness before initiating destructive remote calls to prevent partial state corruption.

---

8. **Network Security & Cleartext Traffic Disallowance:**
   - **Cleartext Traffic Disabled in Release:** Release builds strictly disallow unencrypted cleartext HTTP traffic via `android:usesCleartextTraffic="false"` in `AndroidManifest.xml` and `app/src/main/res/xml/network_security_config.xml` (`<base-config cleartextTrafficPermitted="false" />`).
   - All production communications (AI proxy, Firebase, Supabase, Health Connect) require encrypted TLS 1.2+ HTTPS.
   - Unencrypted cleartext endpoints (`10.0.2.2`, `localhost`, `127.0.0.1`) are permitted strictly in debug build variants (`app/src/debug/res/xml/network_security_config.xml`) for local emulator development (specifically permitting `http://10.0.2.2:8000` to connect to the local development Fastify proxy backend), while release builds block all cleartext HTTP traffic without exception.

---

## Scope & Out-of-Scope

### In Scope:
- Authentication bypass or privilege escalation.
- Cross-user data leakage or unauthorized access in sync endpoints.
- Remote code execution or denial-of-service in the backend proxy.
- Insecure local storage leading to credential extraction on non-rooted devices.
- Flaws in account deletion that leave persistent identifiable health records.

### Out of Scope:
- Attacks requiring physical access to a rooted device or debugger attached to a debug build.
- Social engineering, phishing, or physical attacks against maintainers.
- Rate limiting / denial of service on local development instances (`localhost:8000`).
- Vulnerabilities in third-party services (Google Cloud, Firebase, Supabase) unless caused by misconfiguration in this repository.
