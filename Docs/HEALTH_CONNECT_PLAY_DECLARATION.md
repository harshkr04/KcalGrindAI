# Health Connect Policy Declaration & Play Console Review Guide

**App Name:** KcalGrind (AI Nutrition & Calorie Tracker)  
**Package:** `com.kcalgrindai.app`  
**Target Android Version:** Android 14+ (API 34) & Android 10-13 with Health Connect  
**Declaration Scope:** Google Play Console -> App Content -> Health Connect Permissions Declaration

---

## 1. Requested Health Connect Permissions & Data Types

KcalGrind requests **READ-ONLY** access to exactly **one** Health Connect data type:

| Data Type | Health Connect Permission | Android Permission String | Access Mode |
| :--- | :--- | :--- | :---: |
| **Steps** | `StepsRecord` | `android.permission.health.READ_STEPS` | **Read Only** |

> **Note on Active Calories:** KcalGrind does **NOT** request or use `READ_ACTIVE_CALORIES_BURNED`. Energy targets are determined deterministically using Mifflin-St Jeor and user-selected baseline physical activity levels to completely prevent double-counting of burned calories.  
> **Note on Write Permissions:** KcalGrind requests **ZERO** write permissions to Health Connect. All user food logs and nutritional records are kept inside the app's local Room SQLite database and are not written to Health Connect.

---

## 2. Technical Implementation Architecture

1. **Permissions Rationale Activity:**
   - Class: `com.kcalgrindai.app.feature.health.HealthConnectRationaleActivity`
   - Manifest Intent Filters:
     - `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE`
     - Activity-alias `ViewPermissionUsageActivity` with `android.intent.action.VIEW_PERMISSION_USAGE` and category `android.intent.category.HEALTH_PERMISSIONS` (required for Android 14 Health Connect Privacy Dashboard).
2. **On-Demand & Graceful Degradation:**
   - Permission is requested strictly via user interaction in **Profile -> Health Connect** (`PermissionController.createRequestPermissionResultContract()`).
   - If denied or dismissed, the app functions normally without crashing or blocking core calorie tracking features. Step count is simply shown as unlinked.
3. **Local On-Device Processing:**
   - Daily step counts read from Health Connect are stored locally in the on-device Room SQLite database and rendered on the home dashboard and home-screen widget. Step counts are never sold, transferred to data brokers, or transmitted to third-party ad networks.

---

## 3. Play Console Health Connect Declaration Wording

*(Copy and paste these exact justifications into the Google Play Console Health Connect form)*

### Question 1: How does your app use Health Connect data?
> **Answer:**  
> KcalGrind is a nutrition and calorie tracking application. It reads daily step counts from connected wearables or fitness trackers via Health Connect to display daily movement progress alongside the user's nutritional goals.

### Question 2: Why does your app need access to "Steps" (READ_STEPS)?
> **Declaration Wording:**  
> "KcalGrind reads daily step count to monitor baseline non-exercise physical activity (NEAT). The step count is displayed on the user's home dashboard and home-screen widget, allowing users to assess whether their daily movement aligns with the activity level configured in their nutrition plan. Step counts are processed locally on device and are never sold or shared with third parties."

### Question 3: Does your app request Active Calories Burned?
> **Answer:**  
> **No.** KcalGrind does not declare or request `READ_ACTIVE_CALORIES_BURNED`.

### Question 4: Does your app transfer or sell user health data to third parties?
> **Answer:**  
> **No.** Health data read from Health Connect is stored locally in an on-device Room SQLite database and used strictly for user-facing app functionality. It is never sold, transferred to data brokers, or used for targeted advertising.

### Question 5: Privacy Policy URL
> **URL:**  
> `https://kcalgrind.app/privacy`
