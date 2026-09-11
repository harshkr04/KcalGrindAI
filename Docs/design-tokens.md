# Design Tokens (Vitality Flow)

This document contains the design tokens extracted from `vitality_flow/DESIGN.md` and `app/shared/vitality.css`. These tokens are ready to be converted into a Jetpack Compose `Theme.kt`.

## 1. Colors
The palette is rooted in "Chlorophyll Greens" and "Atmospheric Neutrals".

```kotlin
// Surface / Background
val surface = Color(0xFFF8FAF9)
val surfaceDim = Color(0xFFD8DADA)
val surfaceBright = Color(0xFFF8FAF9)
val surfaceContainerLowest = Color(0xFFFFFFFF)
val surfaceContainerLow = Color(0xFFF2F4F3)
val surfaceContainer = Color(0xFFECEEED)
val surfaceContainerHigh = Color(0xFFE6E9E8)
val surfaceContainerHighest = Color(0xFFE1E3E2)
val onSurface = Color(0xFF191C1C)
val onSurfaceVariant = Color(0xFF404943)
val inverseSurface = Color(0xFF2E3131)
val inverseOnSurface = Color(0xFFEFF1F0)
val surfaceTint = Color(0xFF2C694E)
val background = Color(0xFFF8FAF9)
val onBackground = Color(0xFF191C1C)
val surfaceVariant = Color(0xFFE1E3E2)

// Primary (Deep Forest)
val primary = Color(0xFF0F5238)
val onPrimary = Color(0xFFFFFFFF)
val primaryContainer = Color(0xFF2D6A4F)
val onPrimaryContainer = Color(0xFFA8E7C5)
val inversePrimary = Color(0xFF95D4B3)
val primaryFixed = Color(0xFFB1F0CE)
val primaryFixedDim = Color(0xFF95D4B3)
val onPrimaryFixed = Color(0xFF002114)
val onPrimaryFixedVariant = Color(0xFF0E5138)

// Secondary (Soft Mint)
val secondary = Color(0xFF2B694D)
val onSecondary = Color(0xFFFFFFFF)
val secondaryContainer = Color(0xFFB0F1CC)
val onSecondaryContainer = Color(0xFF327053)
val secondaryFixed = Color(0xFFB0F1CC)
val secondaryFixedDim = Color(0xFF94D4B1)
val onSecondaryFixed = Color(0xFF002113)
val onSecondaryFixedVariant = Color(0xFF0C5136)

// Tertiary (AI Accent - Periwinkle/Blue)
val tertiary = Color(0xFF313C9F)
val onTertiary = Color(0xFFFFFFFF)
val tertiaryContainer = Color(0xFF4A55B9)
val onTertiaryContainer = Color(0xFFD5D7FF)
val tertiaryFixed = Color(0xFFDFE0FF)
val tertiaryFixedDim = Color(0xFFBDC2FF)
val onTertiaryFixed = Color(0xFF000866)
val onTertiaryFixedVariant = Color(0xFF303B9F)

// Error
val error = Color(0xFFBA1A1A)
val onError = Color(0xFFFFFFFF)
val errorContainer = Color(0xFFFFDAD6)
val onErrorContainer = Color(0xFF93000A)

// Outline
val outline = Color(0xFF707973)
val outlineVariant = Color(0xFFBFC9C1)
```

## 2. Typography
Roboto Flex for headlines (expressive, modern). Inter for body and labels (functional readability).

```kotlin
// Display Large
val displayLarge = TextStyle(
    fontFamily = RobotoFlex,
    fontWeight = FontWeight.W400,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp
)

// Headline Large
val headlineLarge = TextStyle(
    fontFamily = RobotoFlex,
    fontWeight = FontWeight.W600,
    fontSize = 32.sp,
    lineHeight = 40.sp
)

// Headline Large Mobile
val headlineLargeMobile = TextStyle(
    fontFamily = RobotoFlex,
    fontWeight = FontWeight.W600,
    fontSize = 28.sp,
    lineHeight = 36.sp
)

// Title Large
val titleLarge = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W500,
    fontSize = 22.sp,
    lineHeight = 28.sp
)

// Body Large
val bodyLarge = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W400,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

// Body Medium
val bodyMedium = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W400,
    fontSize = 14.sp,
    lineHeight = 20.sp
)

// Label Large
val labelLarge = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W500,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
)

// Label Small
val labelSmall = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.W500,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)
```

## 3. Shape / Corner Radius
Organic, approachable, "squishy".

```kotlin
val shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), // sm: 0.25rem
    small = RoundedCornerShape(8.dp),      // DEFAULT: 0.5rem
    medium = RoundedCornerShape(12.dp),    // md: 0.75rem
    large = RoundedCornerShape(16.dp),     // lg: 1rem (Small Components: Buttons, Chips)
    extraLarge = RoundedCornerShape(24.dp) // xl: 1.5rem (Medium Components: Cards, Sheets)
)
// pill-shape (full: 9999px) for AI Action Buttons / FABs
```

## 4. Spacing Scale

```kotlin
val spacingUnit = 4.dp
val marginMobile = 16.dp
val marginTablet = 24.dp
val gutter = 16.dp
val containerPadding = 20.dp
val stackSm = 8.dp
val stackMd = 16.dp
val stackLg = 32.dp
```

## 5. Elevation & Depth (Liquid Glass)

- **Level 0 (Surface):** Base background using neutral color (`surface`, `background`).
- **Level 1 (Cards):** Light tint with 1px soft inner-border (`rgba(255, 255, 255, 0.5)`). No shadow.
- **Level 2 (Active AI Elements):** Floating with soft shadow (`box-shadow: 0 4px 24px rgba(15, 82, 56, 0.05)` or similar), semi-transparent background (`surface_glass` `rgba(248, 250, 249, 0.6)` with backdrop blur 12px-16px).
- **Scrolled States:** Top app bars transition from transparent to a blurred glass effect when scrolled.
- **Liquid Glass / AI Glass Effect:**
    - Blur: 12px to 16px.
    - Background: `rgba(248, 250, 249, 0.7)` or `linear-gradient(135deg, rgba(255,255,255,0.4), rgba(255,255,255,0.1))`
    - Border: 1px solid `rgba(255,255,255,0.5)`
    - Shadow: `0 8px 32px rgba(15, 82, 56, 0.05)`
