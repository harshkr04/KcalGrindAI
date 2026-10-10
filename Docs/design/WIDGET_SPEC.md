# Kcal Grind AI — Home-Screen Widget Design Specification (V2 Redesign)

This specification documents the Stitch-first design architecture, visual assets, design token mappings, character companion identity, calorie visualization concepts, state behaviors, and Android Glance compatibility requirements for the **Kcal Grind AI** home-screen widget system.

---

## 1. Executive Summary & Design Direction

### The Living Nutrition Companion: "Sproutling"
The V2 redesign transforms the widget from a generic circular progress card into a distinctive, collectible living companion experience. 
- **Character Identity:** An original forest-dwelling nutritional seedling creature named **Sproutling**. Sproutling features a rounded silhouette, expressive glowing eyes, a seedling sprout cap, and intentional pixel-art cluster shading.
- **Atmospheric Backgrounds:** Rooted in the *Forest Energy* and *Digital Garden* directions: soft mint container surfaces (`#B0F1CC`) in Light mode and deep forest charcoal containers (`#0B2B1D`) in Dark mode, framed by clean, neutral wallpaper blocks (`#E5E7EB` / `#1E201E`).
- **Tactile + Action:** A high-contrast, recognizable circular `+` quick-add button (40dp target) placed with dedicated spacing so it never obstructs Sproutling or nutrition readouts.
- **Reimagined Calorie Indicators:** Replaces the generic circular ring with two explored distinct concepts:
  - **Concept 1 (Selected Primary): Stepped Pixel Energy Progress Track** — A tactile horizontal track of discrete energy blocks beneath Sproutling representing daily intake progress.
  - **Concept 2 (Alternative Exploration): Segmented Orbital Energy Arc** — A 12-gem segmented orbital arc framing Sproutling's habitat.

---

## 2. Stitch Project & Screen Inventory

- **Stitch Project Title:** `Kcal Grind AI`
- **Stitch Project ID:** `13851982759487954023`
- **Resource Name:** `projects/13851982759487954023`
- **Project Visibility:** `PRIVATE`
- **Design System Asset ID:** `assets/14937809165997237471` / `02401a9e962947508dcb0073d3f9ccc0`

### Complete V2 Screen Inventory

| Screen Key | Stitch Screen ID | Resource Name | Title in Stitch |
|---|---|---|---|
| `widget_v2_2x2_light_normal` | `c94ca235b8f54540b3d2613e45c5fcde` | `projects/13851982759487954023/screens/c94ca235b8f54540b3d2613e45c5fcde` | Kcal Grind AI V2 - 2x2 Widget (Light Theme, Sproutling) |
| `widget_v2_2x2_dark_normal` | `bfa82fb45c0948e9ac8d33d83eda889c` | `projects/13851982759487954023/screens/bfa82fb45c0948e9ac8d33d83eda889c` | Kcal Grind AI V2 - 2x2 Widget (Dark Theme, Sproutling) |
| `widget_v2_concept_orbital_meter_2x2` | `28e4854bcbf74fa490f9f90204c46106` | `projects/13851982759487954023/screens/28e4854bcbf74fa490f9f90204c46106` | Kcal Grind AI V2 - 2x2 Widget (Concept 2: Segmented Orbital Arc) |
| `widget_v2_4x2_light_normal` | `97f19dc6513f4be49441682f834a196c` | `projects/13851982759487954023/screens/97f19dc6513f4be49441682f834a196c` | Kcal Grind AI V2 - 4x2 Widget (Light Theme, Sproutling) |
| `widget_v2_4x2_dark_normal` | `060a32b55ce142c283a08fe307991610` | `projects/13851982759487954023/screens/060a32b55ce142c283a08fe307991610` | Kcal Grind AI V2 - 4x2 Widget (Dark Theme, Sproutling) |
| `widget_v2_4x1_light_normal` | `aa2691bdddb74472be209737e5767e88` | `projects/13851982759487954023/screens/aa2691bdddb74472be209737e5767e88` | Kcal Grind AI V2 - 4x1 Minimal Widget (Light Theme, Sproutling) |
| `widget_v2_4x1_dark_normal` | `211d0fde8d0b4884971125981247cec3` | `projects/13851982759487954023/screens/211d0fde8d0b4884971125981247cec3` | Kcal Grind AI V2 - 4x1 Minimal Widget (Dark Theme, Sproutling) |
| `widget_v2_state_empty_2x2` | `999cc3a5af0a424d9929d4e03ac538d2` | `projects/13851982759487954023/screens/999cc3a5af0a424d9929d4e03ac538d2` | Kcal Grind AI V2 - 2x2 Widget (Empty State: No Meals) |
| `widget_v2_state_goal_reached_4x2` | `f5de9db3d0274c0382c277bcee8f5139` | `projects/13851982759487954023/screens/f5de9db3d0274c0382c277bcee8f5139` | Kcal Grind AI V2 - 4x2 Widget (Goal Reached) |
| `widget_v2_state_over_goal_4x2` | `566336d2aac14bd3b54edd101fd1baa1` | `projects/13851982759487954023/screens/566336d2aac14bd3b54edd101fd1baa1` | Kcal Grind AI V2 - 4x2 Widget (Over Calorie Goal: Neutral Non-Punitive) |
| `widget_v2_state_onboarding_incomplete_2x2` | `bc02c7376afe4993a9adf6a420cbdc94` | `projects/13851982759487954023/screens/bc02c7376afe4993a9adf6a420cbdc94` | Kcal Grind AI V2 - 2x2 Widget (Onboarding Incomplete) |
| `widget_v2_state_loading_2x2` | `de34dfd8f98746ce91bc6da9902f4da5` | `projects/13851982759487954023/screens/de34dfd8f98746ce91bc6da9902f4da5` | Kcal Grind AI V2 - 2x2 Widget (Loading State) |
| `widget_v2_state_hidden_numbers_4x2` | `25c7f4bbe2d04d57a9c5931c29a7076f` | `projects/13851982759487954023/screens/25c7f4bbe2d04d57a9c5931c29a7076f` | Kcal Grind AI V2 - 4x2 Widget (Privacy Mode: Numbers Hidden) |
| `app_screen_v2_onboarding_add_widget` | `1e2804c25c1d43d58e85d7749085abf0` | `projects/13851982759487954023/screens/1e2804c25c1d43d58e85d7749085abf0` | Kcal Grind AI V2 - Onboarding (Add Widget Screen with Sproutling) |
| `app_screen_v2_settings_widget` | `ffdbdfa647854e5ba8722e9ea614aee0` | `projects/13851982759487954023/screens/ffdbdfa647854e5ba8722e9ea614aee0` | Kcal Grind AI V2 - Settings (Home Screen Widget with Sproutling) |
| `app_screen_v2_home_widget_card` | `df9d0e96021f4e168461a7517be1ec6b` | `projects/13851982759487954023/screens/df9d0e96021f4e168461a7517be1ec6b` | Kcal Grind AI V2 - Home Dashboard with Sproutling Widget Promo Card |

*(Original V1 screens `a4ec964b...` through `cdca2961...` remain preserved in Stitch and under `D:\CAloriesssss\_design\widget\` for comparative audit.)*

---

## 3. Export Inventory (V2 Directory)

All V2 assets were verified and exported outside the Git repository to:
`D:\CAloriesssss\_design\widget\v2\`

| Exported Filename | Stitch Screen ID | Size | Purpose |
|---|---|---|---|
| `widget_v2_2x2_light_normal.png` | `c94ca235b8f54540b3d2613e45c5fcde` | 11.7 KB | 2×2 Sproutling companion widget (Light theme) |
| `widget_v2_2x2_light_normal.html` | `c94ca235b8f54540b3d2613e45c5fcde` | 10.3 KB | Responsive HTML layout reference |
| `widget_v2_2x2_dark_normal.png` | `bfa82fb45c0948e9ac8d33d83eda889c` | 18.5 KB | 2×2 Sproutling companion widget (Dark theme) |
| `widget_v2_2x2_dark_normal.html` | `bfa82fb45c0948e9ac8d33d83eda889c` | 11.2 KB | Responsive HTML layout reference |
| `widget_v2_concept_orbital_meter_2x2.png` | `28e4854bcbf74fa490f9f90204c46106` | 29.7 KB | Exploration: Concept 2 Segmented Orbital Halo |
| `widget_v2_concept_orbital_meter_2x2.html` | `28e4854bcbf74fa490f9f90204c46106` | 13.2 KB | Exploration HTML reference |
| `widget_v2_4x2_light_normal.png` | `97f19dc6513f4be49441682f834a196c` | 31.9 KB | 4×2 Sproutling with macros + actions (Light) |
| `widget_v2_4x2_light_normal.html` | `97f19dc6513f4be49441682f834a196c` | 13.0 KB | Responsive HTML layout reference |
| `widget_v2_4x2_dark_normal.png` | `060a32b55ce142c283a08fe307991610` | 27.4 KB | 4×2 Sproutling with macros + actions (Dark) |
| `widget_v2_4x2_dark_normal.html` | `060a32b55ce142c283a08fe307991610` | 12.1 KB | Responsive HTML layout reference |
| `widget_v2_4x1_light_normal.png` | `aa2691bdddb74472be209737e5767e88` | 23.7 KB | 4×1 Minimal widget with Sproutling avatar (Light) |
| `widget_v2_4x1_light_normal.html` | `aa2691bdddb74472be209737e5767e88` | 10.6 KB | Responsive HTML layout reference |
| `widget_v2_4x1_dark_normal.png` | `211d0fde8d0b4884971125981247cec3` | 27.4 KB | 4×1 Minimal widget with Sproutling avatar (Dark) |
| `widget_v2_4x1_dark_normal.html` | `211d0fde8d0b4884971125981247cec3` | 12.0 KB | Responsive HTML layout reference |
| `widget_v2_state_empty_2x2.png` | `999cc3a5af0a424d9929d4e03ac538d2` | 28.5 KB | State 1: No meals logged today (Resting Sproutling) |
| `widget_v2_state_empty_2x2.html` | `999cc3a5af0a424d9929d4e03ac538d2` | 14.0 KB | Empty state HTML reference |
| `widget_v2_state_goal_reached_4x2.png` | `f5de9db3d0274c0382c277bcee8f5139` | 28.1 KB | State 2: Goal reached (Golden leaf bloom) |
| `widget_v2_state_goal_reached_4x2.html` | `f5de9db3d0274c0382c277bcee8f5139` | 13.8 KB | Goal reached HTML reference |
| `widget_v2_state_over_goal_4x2.png` | `566336d2aac14bd3b54edd101fd1baa1` | 27.0 KB | State 3: Over goal (120 over, neutral non-punitive) |
| `widget_v2_state_over_goal_4x2.html` | `566336d2aac14bd3b54edd101fd1baa1` | 13.5 KB | Over goal HTML reference |
| `widget_v2_state_onboarding_incomplete_2x2.png` | `bc02c7376afe4993a9adf6a420cbdc94` | 26.2 KB | State 4: Onboarding incomplete (Waving Sproutling) |
| `widget_v2_state_onboarding_incomplete_2x2.html` | `bc02c7376afe4993a9adf6a420cbdc94` | 11.9 KB | Onboarding incomplete HTML reference |
| `widget_v2_state_loading_2x2.png` | `de34dfd8f98746ce91bc6da9902f4da5` | 23.5 KB | State 5: Loading state (Pulsing pixel dots) |
| `widget_v2_state_loading_2x2.html` | `de34dfd8f98746ce91bc6da9902f4da5` | 13.4 KB | Loading state HTML reference |
| `widget_v2_state_hidden_numbers_4x2.png` | `25c7f4bbe2d04d57a9c5931c29a7076f` | 29.1 KB | State 6: Numbers hidden (Privacy mode) |
| `widget_v2_state_hidden_numbers_4x2.html` | `25c7f4bbe2d04d57a9c5931c29a7076f` | 18.7 KB | Privacy mode HTML reference |
| `app_screen_v2_onboarding_add_widget.png` | `1e2804c25c1d43d58e85d7749085abf0` | 37.2 KB | In-app Onboarding Step: Add Widget with Sproutling |
| `app_screen_v2_onboarding_add_widget.html` | `1e2804c25c1d43d58e85d7749085abf0` | 19.4 KB | Onboarding Add Widget HTML screen |
| `app_screen_v2_settings_widget.png` | `ffdbdfa647854e5ba8722e9ea614aee0` | 39.3 KB | In-app Settings: Home Screen Widget with Sproutling |
| `app_screen_v2_settings_widget.html` | `ffdbdfa647854e5ba8722e9ea614aee0` | 21.9 KB | Settings Home Screen Widget HTML screen |
| `app_screen_v2_home_widget_card.png` | `df9d0e96021f4e168461a7517be1ec6b` | 47.3 KB | In-app Home: Dismissible Sproutling promo banner |
| `app_screen_v2_home_widget_card.html` | `df9d0e96021f4e168461a7517be1ec6b` | 20.9 KB | Home Screen with Promo Card HTML |
| `export_v2_manifest.json` | N/A | 186.8 KB | Machine-readable V2 export index |

---

## 4. Token-to-Glance Mapping (V2)

All tokens remain grounded in `Docs/design/DESIGN.md`:

| UI Element Role | Token Name | Light Theme Value | Dark Theme Value | Android Glance Implementation |
|---|---|---|---|---|
| **Widget Container Surface** | `secondaryContainer` / `surface` | `#B0F1CC` (Soft Mint) | `#0B2B1D` (Deep Forest Charcoal) | `GlanceTheme.colors.secondaryContainer` / `ColorProvider` |
| **Subtle Inner Border** | `outlineVariant` | `#94D4B1` (1px) | `#1F4532` (1px) | Glance modifier border `1.dp` |
| **Primary Nutrition Readout** | `onSurface` / `inverseOnSurface` | `#191C1C` (Dark Slate) | `#EFF1F0` (Crisp Light Mint) | `GlanceTheme.colors.onSurface` (System Roboto bold) |
| **Secondary Metadata / Labels** | `onSurfaceVariant` | `#404943` | `#A8D1BC` | `GlanceTheme.colors.onSurfaceVariant` |
| **Stepped Pixel Energy Blocks (Active)** | `primary` / `inversePrimary` | `#0F5238` (Deep Forest) | `#95D4B3` (Luminous Mint) | Row of small rounded pixel boxes / Canvas bitmap |
| **Stepped Pixel Energy Blocks (Track)** | `primaryFixedDim` | `#C8F7DC` | `#051A10` | Inactive segment background color |
| **Carbohydrate Progress Bar** | `primaryContainer` | `#2D6A4F` | `#95D4B3` | LinearProgressIndicator / Canvas segment |
| **Protein Progress Bar** | `secondary` | `#195E3E` | `#7EC89F` | LinearProgressIndicator / Canvas segment |
| **Fat Progress Bar** | `onSurfaceVariant` tint | `#407855` | `#5BA678` | LinearProgressIndicator / Canvas segment |
| **AI Action (Camera Scan)** | `tertiary` / `tertiaryFixedDim` | `#313C9F` (Periwinkle) | `#BDC2FF` | Glance action button with `Color(0xFF313C9F)` |
| **Standard Add Action (+)** | `primary` | `#0F5238` / Icon `#FFFFFF` | `#95D4B3` / Icon `#0A2619` | Glance action button |
| **Water Quick Log (+250ml)** | `secondary` | `#2B694D` | `#A8E7C5` | Glance action button |
| **Corner Radius** | `rounded.xl` | `16.dp` | `16.dp` | `RoundedCornerShape(16.dp)` |
| **Spacing Scale** | `spacing.unit`, `stack-sm`, `stack-md` | `4.dp`, `8.dp`, `16.dp`, `20.dp` | `4.dp`, `8.dp`, `16.dp`, `20.dp` | Strict adherence to existing tokens |

---

## 5. Calorie Indicator Exploration & Comparison

Per the redesign brief, two alternative progress visualizations were generated in Stitch and evaluated:

### Concept 1: Stepped Pixel Energy Progress Track (Selected Primary)
- **Visual Design:** A horizontal segmented track made of 10 discrete pixel energy tiles positioned below Sproutling.
- **Behavior:** As calories are consumed, tiles illuminate sequentially from left to right in deep forest green (Light) or radiant mint (Dark). Unfilled tiles remain as subtle outline blocks.
- **Readout:** Paired with prominent `680 kcal left` text and secondary budget label `1,420 / 2,100`.
- **Advantages:** Highly legible at small widget sizes (including 4×1 and 2×2), tactile game-companion aesthetic, zero dependency on complex SVG arcs or RemoteViews canvas paths.

### Concept 2: Segmented Orbital Energy Arc (Alternative Exploration)
- **Visual Design:** A 12-gem segmented orbital halo that curves around Sproutling's habitat.
- **Evaluation:** Visually charming and ethereal, but slightly harder to interpret quickly on smaller launcher grids (110×110 dp) compared to the clean horizontal stepped meter. Retained as an optional unlockable visual theme in future iterations.

---

## 6. Widget States in V2 Styling

All six required states maintain Sproutling's character consistency while preserving psychological safety and usability:

1. **State 1: No Meals Logged Today (Empty State)**
   - **Visual:** Sproutling resting peacefully in its grove; energy track at 0% (unfilled outline tiles).
   - **Copy:** `2,100 kcal left` (primary readout), `Ready to log today` (encouraging status).
   - **Tone:** Calm and inviting. Prompts logging without guilt.

2. **State 2: Goal Reached**
   - **Visual:** Sproutling cheerfully energized with a small golden leaf bloom on its head; 10/10 energy blocks filled.
   - **Copy:** `0 kcal left`, `Daily goal reached`.
   - **Tone:** Rewarding and pleasant without excessive confetti or screen noise.

3. **State 3: Over Calorie Goal**
   - **Visual:** Sproutling resting happily, fully satisfied and content (never sad, never crying).
   - **Copy:** `120 kcal over` (clean factual count).
   - **Color & Tone:** Forest green and neutral mint. **NEVER red**, zero punitive copy like "failed" or "bad day".

4. **State 4: Onboarding Incomplete**
   - **Visual:** Sproutling waving gently beside a setup checklist icon.
   - **Copy:** `Finish setup`, `Set your daily goals to begin`.
   - **Action:** Direct tap launches onboarding goal-setting flow.

5. **State 5: Loading State**
   - **Visual:** Sproutling preparing with 3 pulsing mint pixel dots.
   - **Copy:** `Updating...`. Zero fabricated numbers.

6. **State 6: Numbers Hidden (Privacy Mode)**
   - **Visual:** Sproutling present in its grove with a discreet lock badge. Proportional energy blocks remain visible.
   - **Copy:** Numerical values masked as `•••• kcal left` and `•••• g`.

---

## 7. Stitch versus Android Glance Reality

The V2 redesign remains 100% implementable in native Android Glance/AppWidget:

1. **Sproutling Character Asset Strategy:**
   - In Stitch previews, Sproutling is rendered as pixel-art HTML/SVG.
   - For Android Glance, Sproutling will be packaged as a lightweight, crisp vector drawable / raster PNG asset suite (normal, resting, energized, waving, privacy mask) bundled locally in `res/drawable-nodpi/`.
   - No network calls will ever be made to fetch character graphics.
2. **Stepped Energy Track in RemoteViews:**
   - Instead of complex Canvas drawing, the stepped pixel meter can be rendered using standard Glance `Row` containing 10 lightweight `Box` components with rounded corners (4dp) or as a pre-rendered 9-patch bitmap, ensuring 60fps launcher inflation and zero memory leaks.
3. **Typography:**
   - Glance uses system-compatible Roboto (`FontFamily.SansSerif`) to guarantee cross-launcher stability on Android 8.0+ (API 26+).
4. **Interactive Controls:**
   - The top-right `+` button and bottom quick-action row use standard Glance `actionStartActivity` / `actionRunCallback` with comfortable 40dp+ touch targets.

---

## 8. Verification and Numeric Contrast Evidence

### Numeric WCAG Contrast Ratios (Calculated)

All normal text pairings in the V2 designs exceed WCAG AA (4.5:1) and achieve WCAG AAA (7.0:1):

| UI Element Pair | Foreground | Background | Contrast Ratio | WCAG AA (≥ 4.5:1) | WCAG AAA (≥ 7.0:1) |
|---|---|---|---|---|---|
| Light Mint Surface & Dark Readout | `#191C1C` | `#B0F1CC` | **13.31:1** | PASS | PASS |
| Light Mint Surface & Forest Accent | `#0F5238` | `#B0F1CC` | **7.14:1** | PASS | PASS |
| Primary Action Button & White Icon | `#FFFFFF` | `#0F5238` | **9.19:1** | PASS | PASS |
| AI Action Button & White Icon | `#FFFFFF` | `#313C9F` | **9.22:1** | PASS | PASS |
| Dark Forest Container & Light Mint Text | `#EFF1F0` | `#0B2B1D` | **14.88:1** | PASS | PASS |
| Dark Forest Container & Mint Meter | `#95D4B3` | `#0B2B1D` | **9.92:1** | PASS | PASS |
| Dark Slate Wallpaper & Light Mint Text | `#EFF1F0` | `#1E201E` | **14.46:1** | PASS | PASS |
| Dark Action Button & Dark Icon | `#0A2619` | `#95D4B3` | **9.47:1** | PASS | PASS |

### Design Rule Compliance Check

- [x] Product name is consistently **Kcal Grind AI**; zero "Lumina" references.
- [x] Original Sproutling character with distinct silhouette and pixel-art detailing.
- [x] Explored 2 distinct calorie progress visualizations (Stepped Pixel Track & Segmented Orbital Halo).
- [x] Zero purple-to-blue gradients; zero gradient headline text.
- [x] Zero emojis in headings, labels, or UI copy.
- [x] Zero colored left-border cards; zero row of three decorative icon boxes.
- [x] Approved spacing tokens only: 4, 8, 16, 20, 32.
- [x] Neutral, non-punitive language throughout (including State 3 over-goal).
- [x] 16 PNG images + 16 HTML files exported outside Git to `D:\CAloriesssss\_design\widget\v2\`.
- [x] No Android application code was modified.
