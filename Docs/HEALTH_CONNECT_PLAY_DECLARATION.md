# Health Connect Policy Declaration & Play Console Review Guide

**App Name:** Kcal Grind (AI Nutrition & Calorie Tracker)  
**Package:** `com.kcalgrindai.app`  
**Target Android Version:** Android 14+ (API 34) & Android 10-13 with Health Connect  
**Declaration Scope:** Google Play Console -> App Content -> Health Connect Permissions Declaration

---

## 1. Requested Health Connect Permissions & Data Types

Kcal Grind requests **READ-ONLY** access to exactly two Health Connect data types:

| Data Type | Health Connect Permission | Android Permission String | Access Mode |
| :--- | :--- | :--- | :---: |
| **Steps** | `StepsRecord` | `android.permission.health.READ_STEPS` | **Read Only** |
| **Active Calories Burned** | `ActiveCaloriesBurnedRecord` | `android.permission.health.READ_ACTIVE_CALORIES_BURNED` | **Read Only** |

> **Note on Write Permissions:** Kcal Grind requests **ZERO** write permissions to Health Connect. All user food logs and nutritional records are kept inside the app's local Room SQLite database and are not written to Health Connect.

---

## 2. Technical Implementation Architecture

1. **Permissions Rationale Activity:**
   - Class: `com.kcalgrindai.app.feature.health.HealthConnectRationaleActivity`
   - Manifest Intent Filters:
     - `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE`
     - Activity-alias `ViewPermissionUsageActivity` with `android.intent.action.VIEW_PERMISSION_USAGE` and category `android.intent.category.HEALTH_PERMISSIONS` (required for Android 14 Health Connect Privacy Dashboard).
2. **On-Demand & Graceful Degradation:**
   - Permission is requested strictly via user interaction in **Profile -> Health Connect** (`PermissionController.createRequestPermissionResultContract()`).
   - If denied or dismissed, the app functions normally using standard user-reported physical activity multipliers without crashing or blocking core calorie tracking features.
3. **Explicit User Setting & Double-Counting Safeguard:**
   - Deducting active workout calories from daily intake is governed by an explicit toggle under Profile settings (`deductActiveCaloriesEnabled`), which defaults to **OFF**.
   - UI copy explicitly informs users:  
     *"Warning: If you already selected an active TDEE multiplier in your profile, deducting active workout calories risks double-counting burned calories."*

---

## 3. Play Console Health Connect Declaration Wording

*(Copy and paste these exact justifications into the Google Play Console Health Connect form)*

### Question 1: How does your app use Health Connect data?
> **Answer:**  
> Kcal Grind is a nutrition and energy balance application. It reads daily step counts and active calories burned from workouts recorded by the user's connected wearable or fitness tracker to calculate daily net energy expenditure against their personalized calorie and macronutrient targets.

### Question 2: Why does your app need access to "Steps" (READ_STEPS)?
> **Declaration Wording:**  
> "Kcal Grind reads daily step count to monitor baseline non-exercise physical activity (NEAT). The step count is displayed on the user's dashboard and widget, allowing users to assess whether their daily movement aligns with the activity level set in their nutrition plan. Step counts are processed locally on device and are never sold or shared with third parties."

### Question 3: Why does your app need access to "Active Calories Burned" (READ_ACTIVE_CALORIES_BURNED)?
> **Declaration Wording:**  
> "Kcal Grind reads active calories burned during recorded exercise sessions (such as running, cycling, or weightlifting) to provide an optional net calorie calculation. When enabled by the user in settings, active workout calories can offset daily food intake so athletes and active individuals can adjust their fueling accordingly. This permission is read-only, strictly user-controlled (defaults to off), and never shared with advertisers."

### Question 4: Does your app transfer or sell user health data to third parties?
> **Answer:**  
> **No.** Health data read from Health Connect is stored locally in an encrypted Room SQLite database and used strictly for user-facing app functionality. It is never sold, transferred to data brokers, or used for targeted advertising.

### Question 5: Privacy Policy URL
> **URL:**  
> `https://kcalgrind.app/privacy`
