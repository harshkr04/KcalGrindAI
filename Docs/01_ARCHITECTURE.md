# Kcal Grind AI — App Architecture

## 1. Style: Clean Architecture + MVVM, single module to start

Don't multi-module this on day one — it adds build overhead you don't need until the team or codebase actually grows. Structure packages so a future multi-module split is a mechanical move, not a rewrite.

```
com.kcalgrindai.app
├── app/                     # Application class, DI graph roots, MainActivity
├── core/
│   ├── designsystem/        # Colors, type, spacing — ported from vitality.css/DESIGN.md
│   ├── navigation/          # NavGraph, routes, destinations
│   └── common/              # Result wrappers, extension fns, base classes
├── data/
│   ├── local/                # Room DB, entities, DAOs
│   ├── remote/                # Retrofit services (food DB API, backend AI proxy)
│   ├── repository/            # Repository implementations (single source of truth)
│   └── mapper/                # Entity <-> domain model mappers
├── domain/
│   ├── model/                 # Pure Kotlin domain models
│   ├── usecase/                # One class per use case (LogFoodUseCase, GetTodayDiaryUseCase, ...)
│   └── repository/             # Repository interfaces (domain owns the contract)
└── feature/
    ├── onboarding/             # UI + ViewModel, one package per screen group
    ├── home/
    ├── diary/
    ├── logging/                # Photo/voice/text/barcode/search entry + confirm flow
    ├── insights/
    ├── aicoach/
    └── profile/
```

## 2. Layering rules

- `feature/*` depends on `domain`, never directly on `data`.
- `domain` has zero Android dependencies — pure Kotlin, testable without an emulator.
- `data` implements `domain` repository interfaces. ViewModels never talk to Room or Retrofit directly.
- Single source of truth: Room is the only place UI reads from. Network/AI results write into Room; UI observes Room via Flow.

## 3. Presentation pattern

- MVVM with `StateFlow`-based UI state per screen (one sealed `UiState` class per screen: `Loading / Content / Error`).
- Navigation via Jetpack Navigation Compose, single `NavHost`, routes as sealed classes/objects (type-safe args).
- Each feature screen: `XScreen.kt` (stateless composable) + `XViewModel.kt` (Hilt-injected) + `XUiState.kt`. Stateless composables make screens directly previewable and testable.

## 4. AI logging flow (architecturally important — this is the core loop)

```
Camera/Voice/Text UI
        │
        ▼
  LoggingViewModel  ── calls ──▶  AnalyzeFoodUseCase
        │                               │
        │                               ▼
        │                     AIRepository (data layer)
        │                               │
        │                               ▼
        │                  Retrofit → backend proxy → LLM
        │                               │
        │              (returns FoodCandidate list + confidence)
        ▼                               │
  Confirmation UI  ◀────────────────────┘
        │
        │  user edits/confirms
        ▼
  LogFoodUseCase → FoodLogRepository → Room (source=AI, confidence=X)
        │
        ▼
  Home/Diary/Insights (all observe the same Room tables via Flow)
```

The confirmation step is architectural, not just UX — no path exists for an AI result to reach Room without passing through it.

## 5. Error handling & offline

- Repositories return a sealed `Result<T>` (Success/Error/Loading) — no throwing across layer boundaries.
- Offline-first: local logging always works (manual entry, saved meals, favorites). AI features degrade gracefully with a clear "no connection" state — never a silent failure.
- WorkManager for any deferred sync (e.g., retrying a failed AI analysis request).

## 6. Testing shape

- `domain`: pure unit tests, no mocking framework needed for models, use fakes for repos.
- `data`: Room DAO tests via in-memory DB; Retrofit via MockWebServer.
- `feature`: ViewModel tests with fake use cases; Compose UI tests for critical flows (onboarding completion, log-food-and-confirm).
