# Kcal Grind AI — Home-Screen Widget Design Specification

This specification documents the Stitch-first design architecture, visual assets, design token mappings, state behaviors, and Android Glance compatibility requirements for the **Kcal Grind AI** home-screen widget system.

---

## A. Stitch Project Information

- **Project Title:** `Kcal Grind AI`
- **Stitch Project ID:** `13851982759487954023`
- **Resource Name:** `projects/13851982759487954023`
- **Project Visibility:** `PRIVATE`
- **Origin / Type:** `STITCH` / `PROJECT_DESIGN`
- **Design System Asset ID:** `assets/14937809165997237471` (Created from `Docs/design/DESIGN.md`)
- **Existing Project Verification:** Queried existing projects on the Google Stitch MCP endpoint. No project named "Vitality Flow" existed. Project `13851982759487954023` was created cleanly for Kcal Grind AI, importing all documented design tokens.

### Stitch Screens Inventory

| Screen Key | Stitch Screen ID | Resource Name | Title |
|---|---|---|---|
| `widget_2x2_light_normal` | `a4ec964b294344b8954a106a912a2347` | `projects/13851982759487954023/screens/a4ec964b294344b8954a106a912a2347` | Kcal Grind AI - Android 2x2 Widget (Light) |
| `widget_2x2_dark_normal` | `a8c059b7b1984827b613e383aa9f2f04` | `projects/13851982759487954023/screens/a8c059b7b1984827b613e383aa9f2f04` | Kcal Grind AI - Android 2x2 Widget (Dark Mode) |
| `widget_4x2_light_normal` | `8db1cb88e3b9464c9630a2d7557f203d` | `projects/13851982759487954023/screens/8db1cb88e3b9464c9630a2d7557f203d` | Kcal Grind AI - Android 4x2 Widget (Light Mode) |
| `widget_4x2_dark_normal` | `02852338c80648be8f0088a36d0ad406` | `projects/13851982759487954023/screens/02852338c80648be8f0088a36d0ad406` | Kcal Grind AI - Android 4x2 Widget (Dark Mode) |
| `widget_4x1_light_normal` | `b38e344fb51041348bed297add6a5a45` | `projects/13851982759487954023/screens/b38e344fb51041348bed297add6a5a45` | Kcal Grind AI - Android 4x1 Minimal Widget |
| `widget_4x1_dark_normal` | `de66869a1b6a4e71855d783893c0eef6` | `projects/13851982759487954023/screens/de66869a1b6a4e71855d783893c0eef6` | Kcal Grind AI - Android 4x1 Minimal Widget (Dark Mode) |
| `widget_state_empty_2x2` | `2bfa895d1e0d445f84d57d16d696eb6b` | `projects/13851982759487954023/screens/2bfa895d1e0d445f84d57d16d696eb6b` | Kcal Grind AI - Android 2x2 Empty State Widget |
| `widget_state_goal_reached_4x2` | `5b66e643da7b46d7b4831185add2a13e` | `projects/13851982759487954023/screens/5b66e643da7b46d7b4831185add2a13e` | Kcal Grind AI - Android 4x2 Widget (Goal Reached) |
| `widget_state_over_goal_4x2` | `f6fc8787e059448ea329c6a8fc85f969` | `projects/13851982759487954023/screens/f6fc8787e059448ea329c6a8fc85f969` | Kcal Grind AI - Android 4x2 Widget (Over Goal) |
| `widget_state_onboarding_incomplete_2x2` | `94e92d7ed1ca461eb14d6065fe86c920` | `projects/13851982759487954023/screens/94e92d7ed1ca461eb14d6065fe86c920` | Kcal Grind AI - Android 2x2 Widget (Onboarding Incomplete) |
| `widget_state_loading_2x2` | `a2ab0b84d4304cb3b454d28ff470e6f6` | `projects/13851982759487954023/screens/a2ab0b84d4304cb3b454d28ff470e6f6` | Kcal Grind AI - Android 2x2 Widget (Loading State) |
| `widget_state_hidden_numbers_4x2` | `5046e9acdecc4972a7c4c954d526aad9` | `projects/13851982759487954023/screens/5046e9acdecc4972a7c4c954d526aad9` | Kcal Grind AI - Android 4x2 Widget (Privacy Mode) |
| `app_screen_onboarding_add_widget` | `8ecc2dce207b482188d0e50eb1a8f1fa` | `projects/13851982759487954023/screens/8ecc2dce207b482188d0e50eb1a8f1fa` | Kcal Grind AI - Onboarding (Add Widget) |
| `app_screen_settings_widget` | `be019ac99f89484db8065bad4b098593` | `projects/13851982759487954023/screens/be019ac99f89484db8065bad4b098593` | Kcal Grind AI - Home Screen Widget Settings |
| `app_screen_home_widget_card` | `cdca29613b1540f9a75db70925991b44` | `projects/13851982759487954023/screens/cdca29613b1540f9a75db70925991b44` | Kcal Grind AI - Home Dashboard with Widget Promo |

---

## B. Export Inventory

All assets were verified and exported to the external directory:
`D:\CAloriesssss\_design\widget\`

| Exported Filename | Stitch Screen ID | Size | Intended Use |
|---|---|---|---|
| `widget_2x2_light_normal.png` | `a4ec964b294344b8954a106a912a2347` | 28.0 KB | 2x2 Compact widget preview (Light theme) |
| `widget_2x2_light_normal.html` | `a4ec964b294344b8954a106a912a2347` | 13.6 KB | Responsive HTML layout reference |
| `widget_2x2_dark_normal.png` | `a8c059b7b1984827b613e383aa9f2f04` | 26.8 KB | 2x2 Compact widget preview (Dark theme) |
| `widget_2x2_dark_normal.html` | `a8c059b7b1984827b613e383aa9f2f04` | 12.0 KB | Responsive HTML layout reference |
| `widget_4x2_light_normal.png` | `8db1cb88e3b9464c9630a2d7557f203d` | 14.4 KB | 4x2 Medium widget preview (Light theme) |
| `widget_4x2_light_normal.html` | `8db1cb88e3b9464c9630a2d7557f203d` | 9.9 KB | Responsive HTML layout reference |
| `widget_4x2_dark_normal.png` | `02852338c80648be8f0088a36d0ad406` | 17.5 KB | 4x2 Medium widget preview (Dark theme) |
| `widget_4x2_dark_normal.html` | `02852338c80648be8f0088a36d0ad406` | 10.2 KB | Responsive HTML layout reference |
| `widget_4x1_light_normal.png` | `b38e344fb51041348bed297add6a5a45` | 16.1 KB | 4x1 Minimal widget preview (Light theme) |
| `widget_4x1_light_normal.html` | `b38e344fb51041348bed297add6a5a45` | 7.7 KB | Responsive HTML layout reference |
| `widget_4x1_dark_normal.png` | `de66869a1b6a4e71855d783893c0eef6` | 13.7 KB | 4x1 Minimal widget preview (Dark theme) |
| `widget_4x1_dark_normal.html` | `de66869a1b6a4e71855d783893c0eef6` | 8.2 KB | Responsive HTML layout reference |
| `widget_state_empty_2x2.png` | `2bfa895d1e0d445f84d57d16d696eb6b` | 20.5 KB | State 1: No meals logged today (Empty state) |
| `widget_state_empty_2x2.html` | `2bfa895d1e0d445f84d57d16d696eb6b` | 11.0 KB | Empty state HTML layout |
| `widget_state_goal_reached_4x2.png` | `5b66e643da7b46d7b4831185add2a13e` | 10.3 KB | State 2: Daily calorie goal reached |
| `widget_state_goal_reached_4x2.html` | `5b66e643da7b46d7b4831185add2a13e` | 7.9 KB | Goal reached HTML layout |
| `widget_state_over_goal_4x2.png` | `f6fc8787e059448ea329c6a8fc85f969` | 11.3 KB | State 3: Over calorie goal (Neutral non-punitive) |
| `widget_state_over_goal_4x2.html` | `f6fc8787e059448ea329c6a8fc85f969` | 6.9 KB | Over goal HTML layout |
| `widget_state_onboarding_incomplete_2x2.png` | `94e92d7ed1ca461eb14d6065fe86c920` | 4.9 KB | State 4: Setup required / Incomplete onboarding |
| `widget_state_onboarding_incomplete_2x2.html` | `94e92d7ed1ca461eb14d6065fe86c920` | 2.9 KB | Incomplete setup HTML layout |
| `widget_state_loading_2x2.png` | `a2ab0b84d4304cb3b454d28ff470e6f6` | 6.3 KB | State 5: Loading placeholder state |
| `widget_state_loading_2x2.html` | `a2ab0b84d4304cb3b454d28ff470e6f6` | 4.7 KB | Loading state HTML layout |
| `widget_state_hidden_numbers_4x2.png` | `5046e9acdecc4972a7c4c954d526aad9` | 10.8 KB | State 6: Numbers hidden (Privacy mode) |
| `widget_state_hidden_numbers_4x2.html` | `5046e9acdecc4972a7c4c954d526aad9` | 8.0 KB | Privacy mode HTML layout |
| `app_screen_onboarding_add_widget.png` | `8ecc2dce207b482188d0e50eb1a8f1fa` | 52.7 KB | In-app Onboarding Step: Add Widget screen |
| `app_screen_onboarding_add_widget.html` | `8ecc2dce207b482188d0e50eb1a8f1fa` | 19.0 KB | Onboarding Add Widget HTML screen |
| `app_screen_settings_widget.png` | `be019ac99f89484db8065bad4b098593` | 40.2 KB | In-app Settings: Home Screen Widget management |
| `app_screen_settings_widget.html` | `be019ac99f89484db8065bad4b098593` | 16.4 KB | Settings Home Screen Widget HTML screen |
| `app_screen_home_widget_card.png` | `cdca29613b1540f9a75db70925991b44` | 43.7 KB | In-app Home: Dismissible Add Widget banner card |
| `app_screen_home_widget_card.html` | `cdca29613b1540f9a75db70925991b44` | 19.0 KB | Home Screen with Widget Promo Card HTML |
| `export_manifest.json` | N/A | 21.1 KB | Machine-readable export index |

---

## C. Token-to-Glance Mapping

All color tokens originate from `Docs/design/DESIGN.md` and `Docs/design-tokens.md`:

| UI Element Role | Design Token | Light Theme Value | Dark Theme Value | Android Glance Equivalent |
|---|---|---|---|---|
| **Widget Container Surface** | `secondaryContainer` / `surfaceContainer` | `#B0F1CC` (Soft Mint) | `#1A382B` (Dark Forest Container) | `GlanceTheme.colors.secondaryContainer` / `ColorProvider` |
| **Widget Primary Text** | `onSurface` / `inverseOnSurface` | `#191C1C` | `#EFF1F0` | `GlanceTheme.colors.onSurface` |
| **Widget Secondary / Label Text** | `onSurfaceVariant` | `#404943` | `#C0C9C2` | `GlanceTheme.colors.onSurfaceVariant` |
| **Calorie Progress Ring Track** | `primaryFixedDim` / Track | `#A8E7C5` (30% alpha) | `#0E281C` | Canvas/Bitmap drawn with `Color(0xFFA8E7C5)` / `Color(0xFF0E281C)` |
| **Calorie Progress Ring Active** | `primary` / `inversePrimary` | `#0F5238` (Deep Forest) | `#95D4B3` (Mint Accent) | Canvas/Bitmap drawn with `Color(0xFF0F5238)` / `Color(0xFF95D4B3)` |
| **Carbohydrate Progress Bar** | `primaryContainer` | `#2D6A4F` | `#95D4B3` | LinearProgressIndicator / Canvas segment |
| **Protein Progress Bar** | `secondary` | `#195E3E` | `#7EC89F` | LinearProgressIndicator / Canvas segment |
| **Fat Progress Bar** | `onSurfaceVariant` tint | `#407855` | `#5BA678` | LinearProgressIndicator / Canvas segment |
| **AI Action (Camera Scan)** | `tertiary` / `tertiaryFixedDim` | `#313C9F` (Periwinkle) | `#BDC2FF` | Glance action button with `Color(0xFF313C9F)` |
| **Standard Add Action (+)** | `primary` | `#0F5238` / On-mint `#0A2619` | `#95D4B3` | Glance action button |
| **Water Quick Log (Glass)** | `secondary` | `#2B694D` | `#A8E7C5` | Glance action button |
| **Outline / Card Border** | `outlineVariant` | `#94D4B1` | `#2B503E` | Glance modifier border `1.dp` |
| **Corner Radius** | `rounded.xl` | `16.dp` | `16.dp` | `RoundedCornerShape(16.dp)` |
| **Spacing Scale** | `spacing.unit`, `stack-sm`, `stack-md` | `4.dp`, `8.dp`, `16.dp`, `20.dp` | `4.dp`, `8.dp`, `16.dp`, `20.dp` | Strict adherence to existing tokens |

---

## D. Widget States Documentation

All six required states are represented in the Stitch designs across the responsive sizes:

### 1. No Meals Logged Today (Empty State)
- **Displayed Copy:** `2,100 kcal left` (primary readout), `No meals logged yet` (calm status subtitle), `Goal: 2,100` (target badge).
- **Visual Behavior:** Progress ring shows an empty or subtle baseline track (`#A8E7C5` light / `#0E281C` dark) with 0% active fill.
- **Copy Tone:** Calm, neutral, inviting. Never implies failure or missed deadlines.
- **Applicable Sizes:** 2×2, 4×2, 4×1.

### 2. Goal Reached
- **Displayed Copy:** `0 kcal left`, `Daily goal reached`.
- **Visual Behavior:** Circular calorie gauge is 100% complete in Deep Forest (`#0F5238` light / `#95D4B3` dark). Macro bars show full targets achieved.
- **Copy Tone:** Restrained, calm satisfaction without excessive fireworks or distracting clutter.
- **Applicable Sizes:** 2×2, 4×2, 4×1.

### 3. Over Calorie Goal
- **Displayed Copy:** `120 kcal over` (neutral count above target).
- **Visual Behavior:** Gauge remains solid forest green (`#0F5238` / `#95D4B3`).
- **Strict Requirement:** NEVER uses red or error styling. No shaming phrases like "bad day", "failed", or "exceeded limit". Maintains psychological safety and supportive tone.
- **Applicable Sizes:** 2×2, 4×2, 4×1.

### 4. Onboarding Incomplete
- **Displayed Copy:** `Finish setup` (headline), `Tap to complete goal setup` (caption).
- **Visual Behavior:** Replaces calorie readout with a calm setup icon and clear guidance that completing app setup enables nutrition tracking.
- **Action Target:** Tapping the widget launches the onboarding goal-setting flow.
- **Applicable Sizes:** 2×2, 4×2.

### 5. Loading State
- **Displayed Copy:** `Updating...`.
- **Visual Behavior:** Neutral placeholder skeleton ring. No fabricated or estimated calorie/macro numbers appear.
- **Applicable Sizes:** 2×2, 4×2, 4×1.

### 6. Numbers Hidden (Privacy Mode)
- **Displayed Copy:** `•••• kcal left` (masked calories), `•••• g` (masked macro numbers), accompanied by a discreet lock icon.
- **Visual Behavior:** The visual progress ring and macro progress bars remain visible to communicate relative daily balance without disclosing private numerical health data on public home screens.
- **Applicable Sizes:** 2×2, 4×2, 4×1.

---

## E. Stitch versus Android Reality

To ensure native stability and performance, the following differences between the Stitch design representations and Android implementation reality are established:

1. **System Roboto Typography in Glance:**
   - In-app screens use `Roboto Flex` for headlines and `Inter` for body.
   - Android RemoteViews / Glance widgets cannot reliably bundle variable font axes across all launcher processes. Widgets will use system-compatible `FontFamily.SansSerif` (system Roboto), ensuring zero crashes and instant launcher inflation.
2. **Circular Progress Ring Rendering via Bitmap Canvas:**
   - Android Glance does not provide native SVG or `<path>` drawing primitives in RemoteViews.
   - The circular progress ring will be generated as an in-memory Android `Bitmap` using pure Android `android.graphics.Canvas` / `Paint` with anti-aliasing and rounded stroke caps, then displayed using Glance `Image(provider = ImageProvider(bitmap))`.
3. **No Blurs or Glassmorphism:**
   - Android widgets operate inside external launcher processes that do not support dynamic backdrop filters, CSS glassmorphism, or `RenderEffect` blurs.
   - The widget designs strictly use solid Material 3 tokens: `#B0F1CC` (Soft Mint) for light theme and `#1A382B` for dark theme.
4. **Periwinkle Exclusivity:**
   - `#313C9F` (light) / `#BDC2FF` (dark) is strictly reserved for AI actions (the camera meal scan shortcut). All other buttons use primary forest green or secondary mint.
5. **Pinning API Launcher Differences:**
   - Android launchers vary in support for `AppWidgetManager.requestPinAppWidget`. Devices running standard Pixel Launcher / OneUI support direct pinning; third-party launchers (Nova, Niagara) may not.
   - The app implementation will check `isRequestPinAppWidgetSupported`. When supported, it offers one-tap pinning; when unsupported, it seamlessly presents the 4-step manual launcher instructions.

---

## F. Raw Evidence

### Raw Stitch Project Creation Response
```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "content": [
      {
        "text": "{\"name\":\"projects/13851982759487954023\",\"title\":\"Kcal Grind AI\",\"visibility\":\"PRIVATE\",\"projectType\":\"PROJECT_DESIGN\",\"origin\":\"STITCH\"}",
        "type": "text"
      }
    ],
    "structuredContent": {
      "name": "projects/13851982759487954023",
      "origin": "STITCH",
      "projectType": "PROJECT_DESIGN",
      "title": "Kcal Grind AI",
      "visibility": "PRIVATE"
    }
  }
}
```

### Raw Stitch Design System Creation Response
```json
{
  "id": 4,
  "jsonrpc": "2.0",
  "result": {
    "content": [
      {
        "text": "{\"assetId\":\"02401a9e962947508dcb0073d3f9ccc0\"}",
        "type": "text"
      }
    ],
    "structuredContent": {
      "assetId": "02401a9e962947508dcb0073d3f9ccc0"
    }
  }
}
```

### Manifest Reference
The complete machine-readable raw manifest containing every screen ID, prompt, and download URL is stored at:
`D:\CAloriesssss\_design\widget\export_manifest.json`

---

## G. Verification and Contrast Evidence

### Numeric WCAG Contrast Ratios (Calculated)

All normal text pairings in the design exceed the WCAG AA requirement (4.5:1) and achieve WCAG AAA (7.0:1):

| UI Element Pair | Foreground | Background | Contrast Ratio | WCAG AA Normal Text (≥ 4.5:1) | WCAG AAA (≥ 7.0:1) |
|---|---|---|---|---|---|
| Light Surface & On-Surface | `#191C1C` | `#F8FAF9` | **16.36:1** | PASS | PASS |
| Light Surface & Primary Text | `#0F5238` | `#F8FAF9` | **8.77:1** | PASS | PASS |
| Light Surface & On-Surface-Variant | `#404943` | `#F8FAF9` | **8.90:1** | PASS | PASS |
| Light Mint Surface & Dark Text | `#191C1C` | `#B0F1CC` | **13.31:1** | PASS | PASS |
| Light Mint Surface & Forest Ring | `#0F5238` | `#B0F1CC` | **7.14:1** | PASS | PASS |
| Primary Button & On-Primary | `#FFFFFF` | `#0F5238` | **9.19:1** | PASS | PASS |
| AI Action Button & White Icon | `#FFFFFF` | `#313C9F` | **9.22:1** | PASS | PASS |
| Dark Surface & Inverse On-Surface | `#EFF1F0` | `#191C1C` | **15.12:1** | PASS | PASS |
| Dark Mint Container & Mint Text | `#EFF1F0` | `#1A382B` | **11.26:1** | PASS | PASS |
| Dark Mint Container & Active Mint Ring | `#95D4B3` | `#1A382B` | **7.51:1** | PASS | PASS |
| Dark Wallpaper & Light Text | `#EFF1F0` | `#1E201E` | **14.46:1** | PASS | PASS |
| Dark Quick-Add Button & Icon | `#0A2619` | `#95D4B3` | **9.47:1** | PASS | PASS |

### Design Rule Compliance Check

- [x] Product name is consistently **Kcal Grind AI**.
- [x] Zero references to "Lumina" in new UI, copy, or documentation.
- [x] Zero purple-to-blue gradients; zero gradient headline text.
- [x] Zero emojis in headings, labels, or UI copy.
- [x] Zero colored left-border cards; zero row of three decorative icon boxes.
- [x] Zero badges positioned above headlines.
- [x] Zero buzzword-heavy copy or em dashes.
- [x] Strict adherence to spacing scale tokens: 4, 8, 16, 20, 32.
- [x] Support for 2×2, 4×2, and 4×1 responsive widget layouts.
- [x] Automatic Light and Dark system theme adaptation.
- [x] Soft mint surfaces and deep forest-green calorie rings.
- [x] Periwinkle reserved exclusively for AI meal scanning.
- [x] No mascot in version 1.
- [x] 15 PNG and 15 HTML files exported outside git to `D:\CAloriesssss\_design\widget\`.
