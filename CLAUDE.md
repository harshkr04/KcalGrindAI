# Kcal Grind AI — Agent Rules

## Brand and Architecture Rules

- **Product Name:** Kcal Grind AI.
- **Brand Protection:** Never use Lumina in new product-facing material, new UI, documentation, generated designs, or new code comments.
- **Evidence Integrity:** Evidence must be raw tool output or actual command output, never fabricated summaries. Do not invent project IDs, screen IDs, file paths, test results, or tool outputs.
- **Production Data:** No mock data in production. Room is the single source of truth for nutrition data; the widget must never fetch nutrition information directly from the network.
- **Domain Purity:** Domain-layer logic must be pure Kotlin and must not depend on Android framework classes.
- **AI Data Flow:** AI-generated results must never be written to Room until the user explicitly confirms them.
- **Architecture Integrity:** Preserve Clean Architecture and the existing design system.
- **Git Discipline:** Separate Git commits by approved step or checkpoint.
- **Remote Protection:** Never push commits to a remote repository.

## Global Design Rules

Create a cute, premium, friendly, and calm nutrition experience. Avoid generic AI-generated UI patterns.

- No purple-to-blue gradients.
- No gradient headline text.
- No emojis in headings or UI copy.
- No colored left-border cards.
- No row of three decorative icon boxes.
- No badge positioned above a headline.
- No generic buzzword-heavy marketing copy.
- No em dashes in UI copy.
- Use only the existing spacing tokens: 4, 8, 16, 20, and 32.
- Dark mode text must meet WCAG AA contrast of at least 4.5:1 for normal text.
- Do not leave untouched default components where a deliberate, brand-consistent implementation is required.
- Use neutral, non-punitive language, especially when users exceed their calorie goals (e.g., "120 kcal over", never red or shaming phrases like "failed" or "bad day").
- Avoid unnecessary visual effects and decorative clutter.
- Do not use gradients, blur, or glassmorphism in the widget.
- Do not introduce custom fonts into the Glance widget.
- Do not introduce a mascot into widget version 1.

## Typography

- **App Screens:** Roboto Flex for headlines, Inter for body text.
- **Widget UI:** System-compatible Roboto typography (Android widget compatibility constraint).
- **Specification Tracking:** Document deviations rather than silently changing the specification.

## Widget Design Guidelines

- Cute and premium visual style.
- Displays calories and all three macronutrients.
- Sizes supported: Compact (2x2), Medium (4x2), and Expanded (4x1 minimal).
- Automatic light/dark adaptation following Android system theme.
- Soft mint background surface.
- Deep forest-green calorie progress ring.
- Periwinkle reserved exclusively for AI-related items.
- Plain, neutral wallpaper blocks for widget previews.
