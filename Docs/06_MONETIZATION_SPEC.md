# Kcal Grind AI — Monetization & Google Play Billing Architecture

This document defines the monetization strategy, tier structure, feature gating architecture, and Google Play Billing integration roadmap for Kcal Grind AI.

---

## 1. Product Tiers

| Feature | Kcal Grind AI Free (Default / Guest) | Kcal Grind AI Pro ($4.99/mo or $39.99/yr) |
|---|---|---|
| **Manual Food Logging & Barcode Scan** | Unlimited (USDA & OFF) | Unlimited |
| **Water Logging & Weight Tracking** | Unlimited | Unlimited |
| **Health Connect Step Sync** | Included | Included |
| **AI Meal Photo Recognition** | 5 scans / day | Unlimited high-res scans |
| **AI Natural Language Text Meal Parsing** | 10 queries / day | Unlimited natural language logging |
| **AI Coach Conversational Assistant** | 5 messages / day (Resets 12:00 AM local time) | 50 messages / day (Priority coaching with deep weekly trends & micronutrient feedback) |
| **Nutritional Insights & Export** | Last 7 days history | Full history, 30/90-day macro trend graphs, PDF export |

---

## 2. Technical Architecture for Play Billing

### A. Client-Side Integration (`com.android.billingclient:billing-ktx:7.0.0`)
- **BillingClient Lifecycle:** Managed inside a dedicated `BillingRepository` singleton.
- **Product IDs:**
  - `kcal_grind_pro_monthly`: Auto-renewing subscription ($4.99/month, 7-day free trial).
  - `kcal_grind_pro_annual`: Auto-renewing subscription ($39.99/year, 33% discount).
- **Subscription Status Verification:**
  - Upon purchase or app start, `BillingClient.queryPurchasesAsync()` checks active entitlement tokens.
  - Purchases are cryptographically acknowledged via `BillingClient.acknowledgePurchase()`.

### B. Backend Verification & Entitlement Enforcement
- For production security, the client sends purchase token to backend:
  `POST /billing/verify-purchase` { `purchaseToken`, `productId`, `package` }
- The Fastify backend validates the purchase with the Google Play Developer API (`googleapis.androidpublisher.purchases.subscriptions.get`).
- Upon verification, the user's Firebase Custom Claims or database record is updated with:
  `{ "isPro": true, "proExpiresAt": 1792837400000 }`
- The backend AI rate limiter dynamically adjusts:
  - Free users: max 30 AI requests/hour.
  - Pro users: relaxed burst limit (300 requests/hour) with priority model allocation.

---

## 3. Grace Periods & Offline Handling

- **Account Hold / Grace Period:** Supports Google Play's 16-day grace period for failed renewals, ensuring user access is not abruptly terminated during card updates.
- **Offline Entitlement Caching:** The device securely caches subscription state in EncryptedSharedPreferences for up to 72 hours of offline use before requiring a license re-check.
- **Restore Purchases:** Dedicated "Restore Purchases" button in Profile settings that queries Google Play cache and reactivates entitlements seamlessly across device re-installs.

---

## 4. Implementation Roadmap

- **Phase 9 (Current):** Completed architectural specification and backend rate-limiting tier hooks.
- **Phase 10 Milestone:** Add `billing-ktx` dependency, `BillingRepository`, subscription paywall screen (`PaywallSheet.kt`), and Google Play Developer API service account webhook.
