# Google Play Data Safety Form Mapping Guide

**Application:** Kcal Grind (AI Nutrition & Calorie Tracker)  
**Package:** `com.kcalgrindai.app`  
**Play Console Section:** Policy and Programs -> App Content -> Data Safety  

---

## 1. Executive Data Safety Summary
- **Data Encrypted in Transit:** **Yes** (All cloud communications use HTTPS / TLS 1.3 with AES-256 encrypted connections).
- **User Data Deletion Mechanism:** **Yes** (Users can request immediate and permanent deletion of all data via in-app **Profile -> Delete Account** and via the web request URL `https://kcalgrind.app/delete-account`, documented in `Docs/WEB_ACCOUNT_DELETION_PAGE.md`).
- **Data Shared with Third Parties:** **No**. User personal data, nutrition logs, photos, and chat messages are never sold, rented, or shared with third-party advertising networks or data brokers.
- **Children's Policy:** Kcal Grind is not directed to children under 13.
- **Device Backups & Data Extraction:** `android:allowBackup="false"` is enforced in `AndroidManifest.xml`. Furthermore, Android 12+ `dataExtractionRules` and full backup rules explicitly exclude all Room SQLite databases and SharedPreferences/DataStore, preventing cloud backup or device-to-device transfer of private nutrition or health records.
- **AI Processing & Gemini Paid Tier:** The backend authenticates to Google GenAI / Gemini 2.5 Flash via a paid-tier API key. Under Google Cloud terms of service, paid-tier usage ensures customer content (meal photos, transcripts, chat queries) is processed ephemerally and is **never** used for model training or improvement.

---

## 2. Comprehensive Data Category Mapping

| Data Type Category | Specific Data Item | Collected? | Shared? | Purpose | Optional / Required? | Encrypted in Transit? | User Can Request Deletion? |
| :--- | :--- | :---: | :---: | :--- | :--- | :---: | :---: |
| **Personal Info** | **Name** (First Name) | Yes | No | • App functionality (personalization in greetings) | Optional (guest can leave blank) | Yes (TLS 1.3) | Yes |
| **Personal Info** | **Email address** | Yes | No | • Account management<br>• Authentication (Firebase Auth) | Optional (not required in Guest mode) | Yes (TLS 1.3) | Yes |
| **Personal Info** | **User IDs** (Firebase UID) | Yes | No | • Account management<br>• Cloud sync matching | Required for account creation | Yes (TLS 1.3) | Yes |
| **Health & Fitness** | **Fitness info** (Steps, Active calories burned) | Yes | No | • App functionality (energy expenditure & net calorie balance) | Optional (requires explicit Health Connect permission toggle) | Yes (Processed on-device, synced only if backup enabled) | Yes |
| **Health & Fitness** | **Other health info** (Weight, Height, Age, Calorie/Macro goals, Water intake) | Yes | No | • App functionality (Mifflin-St Jeor BMR/TDEE calculation and daily targets) | Required for calorie target calculation | Yes (TLS 1.3) | Yes |
| **Photos & Videos** | **Photos** (Meal plates / food photos) | Yes | No | • App functionality (AI camera vision macro estimation via Gemini 2.5 Flash) | Optional (only used when user triggers AI Photo Scan) | Yes (TLS 1.3) | Yes (Ephemeral on server: discarded after inference; deleted locally when log deleted) |
| **Audio Files** | **Voice / Sound recordings** | Yes | No | • App functionality (natural language speech-to-text food logging) | Optional (only used when user taps Voice Log button) | Yes (TLS 1.3) | Yes (Ephemeral: discarded immediately after audio transcription) |
| **Messages** | **In-app messages** (AI Coach chat queries) | Yes | No | • App functionality (AI nutritional coaching and meal suggestions) | Optional | Yes (TLS 1.3) | Yes (Purged upon conversation reset or Account Deletion) |
| **App Activity** | **App interactions** (Logged meals, custom foods, saved meal templates) | Yes | No | • App functionality (Food diary, macro totals, weekly analytics) | Required for core calorie tracking | Yes (TLS 1.3) | Yes |
| **App Info & Performance** | **Crash logs** (Firebase Crashlytics stack traces) | Yes | No | • Analytics & App stability monitoring | Required | Yes (TLS 1.3) | Yes (Retained per Crashlytics 90-day retention; strictly anonymous — `setUserId` is never called) |
| **App Info & Performance** | **Diagnostics** (API latency, database sync status) | Yes | No | • Diagnostics & troubleshooting | Required | Yes (TLS 1.3) | Yes |
| **Device or other IDs** | **Device / Installation ID** (Firebase installation auth token) | Yes | No | • Account management<br>• Security and fraud prevention (Firebase Auth session verification)<br>*(Note: Reminders are 100% on-device local notifications via WorkManager; no remote push notifications are used)* | Required | Yes (TLS 1.3) | Yes |

---

## 3. Play Console Form Step-by-Step Response Guide

### Section 1: Data Collection & Security
- **Does your app collect or share any of the required user data types?** -> **Yes**
- **Is all of the user data collected by your app encrypted in transit?** -> **Yes**
- **Do you provide a way for users to request that their data be deleted?** -> **Yes**
- **Add a link that users can use to request that their data be deleted:** -> `https://kcalgrind.app/delete-account`

### Section 2: Data Usage & Sharing Details
For each data type declared above:
1. **Data Collection:** Select **Collected** (Data is transmitted off the device to developer servers / Firebase / Cloud Run proxy).
2. **Data Sharing:** Select **Not Shared** (Kcal Grind does not share user data with third parties outside essential service providers providing infrastructure under developer control).
3. **Is this data processed ephemerally?**
   - For **Photos & Audio**: Select **Yes** (The images and voice clips are processed in-memory by Gemini 2.5 Flash and not saved to long-term storage buckets).
   - For **Profile, Weight & Food Logs**: Select **No** (Stored in database for user historical tracking until deleted).
4. **Is this data required or optional?**
   - Health Connect (Steps, Active Calories), Camera, Microphone, and Email are **Optional**.
   - Basic Age/Height/Weight inputs are **Required for core feature functionality** (calculating calorie targets).

### Section 3: Device Backups & Security Safeguards
- **Device Backup Policy:** `android:allowBackup="false"` is configured in `AndroidManifest.xml` to prevent automated device backups from transmitting health or nutrition databases to third-party or unmanaged backup targets.
- **Crashlytics Anonymity:** Firebase Crashlytics does NOT record or associate user identity (`setUserId` is disabled in code), ensuring crash stack traces remain non-identifiable.
