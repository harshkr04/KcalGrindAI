# Kcal Grind — Account Deletion Verification & Compliance Checklist
*(Google Play Store Data Safety & Account Deletion Policy Compliance)*

## 1. Overview & Policy Requirements
Per Google Play's **User Data Policy (Account Deletion Requirement)**, all apps that allow users to create an account must:
1. Provide an in-app path for users to delete their account and associated personal data.
2. Provide a web-based account deletion URL where users can request data deletion without reinstalling the app.
3. Completely purge user data (profile, history, meal logs, biometrics, sync records) from both local device storage and backend cloud databases.

---

## 2. In-App Architecture
- **Location:** Profile Screen -> Bottom Actions (below "Sign Out") -> **Delete Account** button (red border, trash icon).
- **Confirmation:** Native alert dialog explicitly stating all personal records, food logs, weights, AI chats, and cloud sync backups will be permanently purged.
- **Security Check (Re-authentication):** If Firebase Auth rejects deletion due to an expired auth session (`CREDENTIAL_TOO_OLD_LOGIN_AGAIN` / `FirebaseAuthRecentLoginRequiredException`), an inline password re-auth dialog is displayed.
- **Backend Purge Route:** `DELETE /account` on the Fastify proxy, authenticated via Firebase Bearer ID token:
  - Deletes Supabase relational records: `food_items`, `food_logs`, `daily_goals`, `weight_entries`, `water_logs`, `ai_coach_messages`, `ai_coach_conversations`, `profiles`.
  - Clears in-memory Redis/Fastify rate limit buckets.
  - Returns `200 { success: true, message: "Account data purged" }` (idempotent: subsequent calls return 200).
- **Local Wipe:** `KcalGrindDatabase.clearAllTables()` + `UserPreferencesRepository.clearAllPreferences()`.
- **Navigation:** Seamless transition to `Welcome` / `Auth` onboarding graph.

---

## 3. Manual Testing Checklist

| Test Scenario | Steps | Expected Result | Pass/Fail |
| :--- | :--- | :--- | :--- |
| **1. Guest / Anonymous User** | 1. Open app as guest (no Firebase login).<br>2. Navigate to Profile.<br>3. Tap "Delete Account".<br>4. Confirm deletion. | • No network call attempted.<br>• Local Room database wiped.<br>• Local SharedPreferences cleared.<br>• App navigates to Welcome/Onboarding screen. | [ ] |
| **2. Email / Password User (Fresh Login)** | 1. Sign in with test email & password.<br>2. Add custom foods and log a meal.<br>3. Go to Profile -> "Delete Account".<br>4. Tap "Permanently Delete". | • Progress spinner displays.<br>• Backend `DELETE /account` executes (returns 200).<br>• Firebase Auth user is deleted.<br>• Local Room DB and prefs are wiped.<br>• App navigates to Welcome screen.<br>• Attempting to sign in with same credentials fails (user not found). | [ ] |
| **3. Email / Password (Stale Token / Re-auth Required)** | 1. Sign in with test account.<br>2. Wait for credential expiration or simulate stale token.<br>3. Go to Profile -> "Delete Account" -> Confirm.<br>4. Firebase throws `RequiresRecentLogin`.<br>5. Enter wrong password.<br>6. Enter correct password. | • Re-authentication dialog pops up.<br>• Wrong password shows inline error, local data remains intact.<br>• Correct password verifies user, completes backend purge, and navigates to Welcome. | [ ] |
| **4. Google Sign-In User** | 1. Sign in via Google OAuth.<br>2. Tap "Delete Account" -> Confirm. | • Token refreshed seamlessly or prompted for Google re-auth.<br>• Backend user records purged.<br>• Firebase user deleted.<br>• Local data wiped. | [ ] |
| **5. Offline / Airplane Mode Graceful Handling** | 1. Sign in with cloud account.<br>2. Turn on Airplane Mode.<br>3. Go to Profile -> "Delete Account" -> Confirm. | • App alerts: "Network unavailable. Please connect to the internet to delete your account."<br>• **CRITICAL:** Local Room database and preferences are **NOT** wiped (prevents partial silent data loss).<br>• User remains logged in. | [ ] |
| **6. Backend Server Failure (HTTP 500)** | 1. Simulate server downtime or invalid endpoint.<br>2. Tap "Delete Account" -> Confirm. | • App alerts: "Cloud server error. Could not purge account data."<br>• Local data is preserved so user doesn't lose data before cloud confirmation. | [ ] |
| **7. Idempotent Retry** | 1. Call `DELETE /account` twice in succession with the same auth token. | • Both requests return `HTTP 200 OK`.<br>• Server does not crash or throw 404/500 on missing records. | [ ] |

---

## 4. Google Play Console Submission Steps
1. In Play Console -> **Policy and Programs** -> **App Content** -> **Data Safety**.
2. Under "Does your app allow users to create an account?": Select **Yes**.
3. Under "Account deletion URL": Enter public deletion URL:
   `https://kcalgrind.app/delete-account` (or backend account deletion landing endpoint).
4. Under "Do you provide a way for users to request that their account and data be deleted?": Select **Yes**.
5. Disclose retention policy: "When an account deletion request is processed, all personal biometric data, nutrition logs, and chat records are purged immediately and permanently."
