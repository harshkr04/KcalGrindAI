/* ==========================================================================
   Vitality Flow — Phase 1 design tokens (single source of truth)
   Extracted verbatim from vitality_flow/DESIGN.md and the Phase 1 screens
   (welcome, goal_selection, activity_level, calorie_target, home_dashboard_*).
   Nothing here is new: this file only stops every screen from re-declaring it.
   ========================================================================== */
tailwind.config = {
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        "surface": "#f8faf9",
        "surface-dim": "#d8dada",
        "surface-bright": "#f8faf9",
        "surface-container-lowest": "#ffffff",
        "surface-container-low": "#f2f4f3",
        "surface-container": "#eceeed",
        "surface-container-high": "#e6e9e8",
        "surface-container-highest": "#e1e3e2",
        "on-surface": "#191c1c",
        "on-surface-variant": "#404943",
        "inverse-surface": "#2e3131",
        "inverse-on-surface": "#eff1f0",
        "outline": "#707973",
        "outline-variant": "#bfc9c1",
        "surface-tint": "#2c694e",
        "primary": "#0f5238",
        "on-primary": "#ffffff",
        "primary-container": "#2d6a4f",
        "on-primary-container": "#a8e7c5",
        "inverse-primary": "#95d4b3",
        "secondary": "#2b694d",
        "on-secondary": "#ffffff",
        "secondary-container": "#b0f1cc",
        "on-secondary-container": "#327053",
        "tertiary": "#313c9f",
        "on-tertiary": "#ffffff",
        "tertiary-container": "#4a55b9",
        "on-tertiary-container": "#d5d7ff",
        "error": "#ba1a1a",
        "on-error": "#ffffff",
        "error-container": "#ffdad6",
        "on-error-container": "#93000a",
        "primary-fixed": "#b1f0ce",
        "primary-fixed-dim": "#95d4b3",
        "on-primary-fixed": "#002114",
        "on-primary-fixed-variant": "#0e5138",
        "secondary-fixed": "#b0f1cc",
        "secondary-fixed-dim": "#94d4b1",
        "on-secondary-fixed": "#002113",
        "on-secondary-fixed-variant": "#0c5136",
        "tertiary-fixed": "#dfe0ff",
        "tertiary-fixed-dim": "#bdc2ff",
        "on-tertiary-fixed": "#000866",
        "on-tertiary-fixed-variant": "#303b9f",
        "background": "#f8faf9",
        "on-background": "#191c1c",
        "surface-variant": "#e1e3e2",
        /* splash_screen_refinement_mint_glow */
        "mint-light": "#d8f3dc",
        "mint-sage": "#95d5b2",
        /* macro accents used by home_dashboard_normal_state + goal_setting */
        "macro-protein": "#f43f5e",
        "macro-carbs": "#3b82f6",
        "macro-fat": "#eab308"
      },
      borderRadius: {
        "DEFAULT": "0.25rem",
        "lg": "0.5rem",
        "xl": "0.75rem",
        "full": "9999px"
      },
      spacing: {
        "container-padding": "20px",
        "unit": "4px",
        "gutter": "16px",
        "margin-tablet": "24px",
        "stack-md": "16px",
        "margin-mobile": "16px",
        "stack-sm": "8px",
        "stack-lg": "32px"
      },
      fontFamily: {
        "display-lg": ["Roboto Flex"],
        "headline-lg": ["Roboto Flex"],
        "headline-lg-mobile": ["Roboto Flex"],
        "title-lg": ["Inter"],
        "body-lg": ["Inter"],
        "body-md": ["Inter"],
        "label-lg": ["Inter"],
        "label-sm": ["Inter"]
      },
      fontSize: {
        "display-lg": ["57px", { lineHeight: "64px", letterSpacing: "-0.25px", fontWeight: "400" }],
        "headline-lg": ["32px", { lineHeight: "40px", fontWeight: "600" }],
        "headline-lg-mobile": ["28px", { lineHeight: "36px", fontWeight: "600" }],
        "title-lg": ["22px", { lineHeight: "28px", fontWeight: "500" }],
        "body-lg": ["16px", { lineHeight: "24px", letterSpacing: "0.5px", fontWeight: "400" }],
        "body-md": ["14px", { lineHeight: "20px", fontWeight: "400" }],
        "label-lg": ["14px", { lineHeight: "20px", letterSpacing: "0.1px", fontWeight: "500" }],
        "label-sm": ["11px", { lineHeight: "16px", letterSpacing: "0.5px", fontWeight: "500" }]
      }
    }
  }
};
