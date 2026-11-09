# KcalGrind Domain Glossary

This glossary defines the authoritative domain terms, entity definitions, and system vocabulary across the KcalGrind Android app, backend proxy, Supabase database, and web presence.

---

### Core Entities & Brand

* **KcalGrind:** The official product name (single word, camel-cased). An Android-first AI calorie and nutrition tracker (`com.kcalgrindai.app`).
* **Sproutling:** The resident AI nutrition coach mascot. Visually rendered as an expressive pixel-art sprout seedling (`ic_sproutling_*`), Sproutling provides conversational meal advice, macro check-ins, and contextual suggestions via backend function-calling tools.
* **Energy Ring:** The signature primary UI visualization on the Home screen and widgets. A circular progress ring displaying calories remaining vs. daily target without overlapping text.
* **Vitality Flow (KcalGrind V3 Design System):** The Material 3 design system featuring deep forest greens (`#14201a`), OLED card surfaces (`#191d1b`), energetic green accents (`#38c77b` dark / `#16834d` light), clean borders, and generous whitespace.

### Food & Nutrition Data

* **IFCT 2017 (ICMR-NIN):** Indian Food Composition Tables (2017) published by the National Institute of Nutrition (ICMR-NIN), Indian Council of Medical Research, Hyderabad. Bundled as an offline Room SQLite dataset in the app for instant, zero-latency Indian dish logging.
* **USDA FoodData Central:** The global standard nutritional dataset for raw ingredients, whole foods, and foundation foods; queried online via USDA API.
* **Open Food Facts (OFF):** The open collaborative food product database used exclusively for barcode UPC/EAN lookups.
* **Food Log Item:** A discrete logged food record containing serving grams, calories, protein, carbs, fat, fiber, and provenance attributes (`source` and `confidence`).
* **Source & Confidence:** Provenance metadata attached to every food log entry indicating whether data originated from verified tables (`ifct`, `usda`), barcode (`openfoodfacts`), user custom entry (`custom`), or an AI estimate (`ai_photo`, `ai_voice`, `ai_text`) with a confidence level.

### Sync, Storage & Privacy

* **Local-First Food Log:** The architectural principle where the Android device's local Room SQLite database is the primary source of truth. All food logging, diary editing, and history viewing function fully offline.
* **Sync Now:** The on-demand manual synchronization protocol (`POST /sync/push` and `GET /sync/pull`) triggered from the Profile screen. Note: there is currently zero background or automated syncing.
* **Data Purge (`DELETE /account`):** The comprehensive account deletion mechanism that atomically removes all records across 15 Supabase tables, revokes Firebase Auth tokens, and deletes the local SQLite database.
* **Ephemeral AI Streams:** The privacy architecture where user photos and audio voice logs are forwarded directly in memory to Google Gemini 2.5 Flash and never written to disk, object storage, or persistent databases.

### Platform & Hardware Integrations

* **Glance Home-Screen Widgets:** Interactive Android app widgets built using Jetpack Glance in 4Ã—2 (expanded), 2Ã—2 (compact), and 4Ã—1 (minimal energy bar) layouts, featuring a public privacy mode toggle.
* **Health Connect:** Android's unified health data platform. KcalGrind requests strictly read-only permissions for `READ_STEPS`, with calorie deductions defaulted to OFF.
* **Daily Energy Balance:** Net calorie expenditure calculated as: $\text{Net Intake} = \text{Consumed} - (\text{Active Calories if enabled})$.
