# Privacy Policy for Kcal Grind AI

**Last Updated: September 13, 2026**

Kcal Grind AI ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how our mobile application collects, uses, stores, and safeguards your personal and health information when you use Kcal Grind AI.

---

## 1. Information We Collect

### A. Information You Provide
- **Account & Profile Data:** When you create an account, we collect your email address and authentication identifier (Firebase UID). If you use "Continue as guest", an anonymous identifier is assigned. We also collect your nutrition profile, including birth date/age, gender, height, weight, activity level, dietary goals (weight loss, maintenance, muscle gain), and dietary tags/allergies.
- **Nutrition & Health Logs:** Food items logged, portion sizes, calorie and macronutrient estimates, water intake logs, and weight check-ins.

### B. Device Permissions & Sensor Data
- **Camera (Optional):** Used exclusively when you choose to photograph a meal for nutritional recognition. Photos are processed in real-time and are never stored on public servers or used for marketing.
- **Microphone / Audio (Optional):** Used exclusively while you press and hold the voice logging button to transcribe spoken meal descriptions into text. Audio recordings are discarded immediately after transcription.
- **Health Connect by Android (Optional):** If you grant read permissions, Kcal Grind AI reads daily step counts and active calories burned to adjust your remaining daily calorie budget. Kcal Grind AI never writes to or modifies your Health Connect data.

---

## 2. How We Use Your Information

We use the collected information strictly for:
- Calculating your Basal Metabolic Rate (BMR) and personalized Total Daily Energy Expenditure (TDEE).
- Providing automated visual and natural language food logging and nutritional estimates.
- Generating empathetic, contextual coaching insights via the Kcal Grind AI Coach.
- Monitoring application performance and stability via crash reporting (Firebase Crashlytics).

---

## 3. AI Processing & Third-Party Providers

Kcal Grind AI employs advanced AI models (Google Gemini 2.5 Flash) via a secure, authenticated proxy backend to analyze meal photos and text:
- **Ephemeral Processing:** Food photos and meal descriptions sent to the AI proxy are processed in memory and are **not** stored permanently on the inference servers.
- **No Model Training:** Your food photos and private meal logs are **never** sold or used to train public foundation models.
- **Third-Party Service Providers:**
  - **Google Firebase:** Authentication, Crashlytics diagnostics, and rate-limiting enforcement.
  - **Google Gemini API:** Real-time multimodal food analysis and nutrition coaching inference.
  - **USDA FoodData Central, ICMR-NIN IFCT 2017 & OpenFoodFacts:** Nutritional database verification.

---

## 4. Data Storage, Security & Retention

- **Local Storage:** Your profile, meal history, weight logs, and water intake are stored securely on your device using an encrypted local SQLite database (Room).
- **Encryption in Transit:** All communications between the app, our proxy backend, and external APIs are encrypted using industry-standard Transport Layer Security (TLS/HTTPS).
- **Data Retention & Deletion:** You have complete control over your data. You may delete individual logs, reset your history, or permanently delete your account directly within the application's Profile settings.

---

## 5. Children's Privacy

Kcal Grind AI is not directed to individuals under the age of 13. We do not knowingly collect personal information from children.

---

## 6. Your Rights & Contact Us

Under applicable data protection laws (including GDPR and CCPA), you have the right to access, export, rectify, or delete your personal data. 

For questions, feedback, or data deletion requests, contact our privacy team at:
- **Email:** privacy@kcalgrind.ai
- **Website:** https://kcalgrind.ai/privacy
