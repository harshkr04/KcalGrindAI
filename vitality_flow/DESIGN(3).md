---
name: Vitality Flow
colors:
  surface: '#f8faf9'
  surface-dim: '#d8dada'
  surface-bright: '#f8faf9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f3'
  surface-container: '#eceeed'
  surface-container-high: '#e6e9e8'
  surface-container-highest: '#e1e3e2'
  on-surface: '#191c1c'
  on-surface-variant: '#404943'
  inverse-surface: '#2e3131'
  inverse-on-surface: '#eff1f0'
  outline: '#707973'
  outline-variant: '#bfc9c1'
  surface-tint: '#2c694e'
  primary: '#0f5238'
  on-primary: '#ffffff'
  primary-container: '#2d6a4f'
  on-primary-container: '#a8e7c5'
  inverse-primary: '#95d4b3'
  secondary: '#2b694d'
  on-secondary: '#ffffff'
  secondary-container: '#b0f1cc'
  on-secondary-container: '#327053'
  tertiary: '#313c9f'
  on-tertiary: '#ffffff'
  tertiary-container: '#4a55b9'
  on-tertiary-container: '#d5d7ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b1f0ce'
  primary-fixed-dim: '#95d4b3'
  on-primary-fixed: '#002114'
  on-primary-fixed-variant: '#0e5138'
  secondary-fixed: '#b0f1cc'
  secondary-fixed-dim: '#94d4b1'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#0c5136'
  tertiary-fixed: '#dfe0ff'
  tertiary-fixed-dim: '#bdc2ff'
  on-tertiary-fixed: '#000866'
  on-tertiary-fixed-variant: '#303b9f'
  background: '#f8faf9'
  on-background: '#191c1c'
  surface-variant: '#e1e3e2'
typography:
  display-lg:
    fontFamily: Roboto Flex
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Roboto Flex
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Roboto Flex
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  margin-mobile: 16px
  margin-tablet: 24px
  gutter: 16px
  container-padding: 20px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style

The design system is built on **Material 3 Expressive**, blending the systematic logic of Android’s native language with a "Liquid Glass" aesthetic. This approach prioritizes a premium, AI-native feel that is both calm and technologically advanced. 

The visual narrative centers on "Nutritional Vitality"—using organic movement, subtle translucency, and high-quality whitespace to make calorie tracking and AI meal analysis feel effortless and fast. The interface avoids the rigidity of traditional medical apps, opting instead for soft depth, gentle background blurs, and responsive motion that makes the AI interaction feel living and supportive.

Targeting health-conscious Android users, the UI avoids iOS-centric patterns (like center-aligned headers or bottom sheet handles that mimic home bars) in favor of standard Material 3 top app bars, navigation rails/bars, and prominent Floating Action Buttons (FABs) for AI-driven logging.

## Colors

The palette is rooted in a spectrum of "Chlorophyll Greens" and "Atmospheric Neutrals." 

*   **Primary (Deep Forest):** Used for key branding, active states, and primary buttons. It evokes trust and growth.
*   **Secondary (Soft Mint):** Used for container backgrounds and subtle accents to maintain a lightweight feel.
*   **Tertiary (AI Action):** A vibrant, tech-forward periwinkle/blue used exclusively for AI-driven features, such as the "Scan Meal" button or AI chat suggestions.
*   **Neutral (Pristine):** A high-brightness off-white that prevents screen fatigue.

**Liquid Glass Implementation:** Surfaces should utilize the `surface_glass` token with a `24px` backdrop blur when overlaying content, creating a sense of depth without adding visual weight.

## Typography

This design system uses **Roboto Flex** for headlines to take advantage of its variable weight and width, allowing for expressive, legible titles that feel modern. **Inter** is utilized for body text and labels to ensure maximum functional readability during data-heavy nutrition logging.

Hierarchy rules:
- Use **Display Large** only for high-impact data visualizations (e.g., daily calorie totals).
- **Headline Large** should be used for page titles in the Top App Bar.
- **Body Large** is the default for AI-generated text responses to ensure high accessibility.
- All caps should be avoided except for small **Label SM** tags.

## Layout & Spacing

The layout follows a **8dp grid system** (using a 4px baseline unit). It utilizes a fluid grid that adapts to the standard Android breakpoints.

- **Mobile (<600dp):** 4-column grid with 16dp margins.
- **Tablet (600dp+):** 12-column grid with 24dp margins and a centered content max-width of 840dp to prevent line lengths from becoming too long.

Vertical rhythm is maintained through "Stacking" tokens. Elements within a card use `stack-sm`, while distinct sections on a page use `stack-lg`. AI-native components (like the meal suggestion carousel) use a horizontal overflow pattern to signal more content without cluttering the vertical viewport.

## Elevation & Depth

In line with the Liquid Glass aesthetic, elevation is communicated through a combination of **Tonal Elevation** (from Material 3) and **Backdrop Blurs**.

- **Level 0 (Surface):** The base background using the neutral color.
- **Level 1 (Cards):** Subtle color shift to a slightly lighter tint with a 1px soft inner-border (low-contrast outline) to simulate a glass edge. No shadow.
- **Level 2 (Active AI Elements):** Elements "floating" with a very soft, diffused shadow (12% opacity of the Primary color) and a semi-transparent background (`surface_glass`).
- **Scrolled States:** Top app bars should transition from transparent to a blurred glass effect when content scrolls beneath them.

## Shapes

The shape language is organic and approachable. 
- **Small Components (Buttons, Chips):** Use `rounded-lg` (16px) for a soft, modern feel.
- **Medium Components (Cards, Sheets):** Use `rounded-xl` (28px) to emphasize the friendly, "squishy" nature of the health app.
- **AI Action Buttons:** Large FABs and primary AI buttons use a full pill-shape to distinguish them from standard navigation or secondary actions.
- Avoid sharp 0px corners entirely to maintain the "Liquid" brand persona.

## Components

### Buttons & Actions
- **Primary FAB:** The main AI logging button. It should be a large, pill-shaped button using the `tertiary_color` (AI Accent).
- **Secondary Buttons:** Outlined with a 1.5px border in `primary_color`.

### Cards & Nutrition Logging
- **Meal Cards:** Use a Level 1 elevation with `rounded-xl` corners. Include high-quality imagery with a subtle gradient overlay at the bottom to ensure text legibility.
- **AI Insights:** Highlighted using a `surface_glass` container with a thin periwinkle border to signal AI-native content.

### Inputs & Selection
- **Text Fields:** Use the Material 3 "Filled" style with the container color matching a very light tint of the Primary color. Rounded top corners (8px).
- **Chips:** Used for dietary filters (e.g., "Keto", "High Protein"). These should be highly rounded (pill-shaped) and use a subtle "Pressed" animation that scales the element down slightly (98%) to provide tactile feedback.

### Data Visualization
- **Progress Rings:** Use thick strokes (12dp) with rounded caps for calorie and macro tracking. Use the Secondary color for the track and Primary for the progress.

### Lists
- Lists should have generous vertical padding (`stack-md`) to ensure touch targets are easily accessible for users on the move.