# Google Play Store — Data Safety Form Response Guide

This document provides exact answers to enter into the Google Play Console **Data Safety** questionnaire for Kcal Grind AI.

---

## 1. Overview Questions

| Question | Answer | Rationale |
|---|---|---|
| Does your app collect or share any of the required user data types? | **Yes** | App collects user profile, meal logs, optional photos/audio, and crash logs. |
| Is all of the user data collected by your app encrypted in transit? | **Yes** | All network requests use HTTPS (TLS 1.3/1.2). |
| Do you provide a way for users to request that their data be deleted? | **Yes** | Users can delete account and local data in-app or via email request. |

---

## 2. Data Types Collected & Declared

### A. Personal Info
- **Email address:**
  - *Collected:* Yes
  - *Shared:* No
  - *Ephemeral:* No
  - *Required or Optional:* Optional (Guest users do not require email; Email/Password and Google Sign-in users do)
  - *Purpose:* App functionality, Account management
- **User IDs (Firebase UID):**
  - *Collected:* Yes
  - *Shared:* No
  - *Purpose:* App functionality, Account management

### B. Health and Fitness
- **Fitness info (Steps, active energy burned from Health Connect):**
  - *Collected:* Yes (Read-only via Health Connect with explicit user permission)
  - *Shared:* No
  - *Required or Optional:* Optional
  - *Purpose:* App functionality (adjusting daily caloric expenditure)
- **Health info (Weight check-ins, dietary targets, nutritional intake):**
  - *Collected:* Yes
  - *Shared:* No
  - *Required or Optional:* Required for core functionality
  - *Purpose:* App functionality, Personalization

### C. Photos and Videos
- **Photos:**
  - *Collected:* Yes (when user uses camera/gallery to log a meal)
  - *Shared:* No
  - *Ephemeral:* Yes (processed ephemerally in memory by AI proxy and discarded; never persisted on servers)
  - *Required or Optional:* Optional
  - *Purpose:* App functionality (Food recognition)

### D. Audio Files
- **Voice or sound recordings:**
  - *Collected:* Yes (when user holds voice-logging button)
  - *Shared:* No
  - *Ephemeral:* Yes (transcribed and immediately discarded)
  - *Required or Optional:* Optional
  - *Purpose:* App functionality (Speech-to-text food logging)

### E. App Info and Performance
- **Crash logs (Firebase Crashlytics):**
  - *Collected:* Yes
  - *Shared:* No
  - *Purpose:* Analytics, App performance

---

## 3. Data Deletion Mechanism

- **In-App Account Deletion:** Located under **Profile &rarr; Account Settings &rarr; Delete Account**.
- **Data Deletion URL for Play Store Listing:** `https://kcalgrind.ai/delete-data`
- **Data Retention Policy:** When a user deletes their account, all local database tables are purged immediately and Firebase Authentication records are removed.
