# Contributing to KcalGrind

Thank you for your interest in contributing to KcalGrind! We welcome contributions from developers, designers, and open-source contributors of all skill levels.

---

## 1. Getting Started & Development Setup

1. **Fork and Clone:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/LuminaAndroid.git
   cd LuminaAndroid
   ```

2. **Android Environment:**
   - Install **Android Studio Ladybug (2024.2+)** or newer.
   - Ensure **JDK 17** is installed and selected as your Gradle JDK in Android Studio (`Settings -> Build, Execution, Deployment -> Build Tools -> Gradle`).
   - Create a local `local.properties` file in the project root:
     ```properties
     sdk.dir=/path/to/your/android/sdk
     USDA_API_KEY=DEMO_KEY
     BACKEND_URL=http://10.0.2.2:8000/
     ```

3. **Backend Environment:**
   - Ensure **Node.js 20+** is installed.
   ```bash
   cd backend
   npm install
   cp .env.example .env
   # Add your test GEMINI_API_KEY and start:
   npm run dev
   ```

---

## 2. Branch & Pull Request Flow

1. **Create a Feature Branch:**
   Always create a dedicated feature branch from `main`:
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feat/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

2. **Commit Guidelines:**
   Follow [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat(area): description` (e.g., `feat(diary): add weekly fiber calculation`)
   - `fix(area): description` (e.g., `fix(worker): prevent duplicate hydration notification`)
   - `docs(area): description` (e.g., `docs(readme): clarify local setup instructions`)
   - `test(area): description` (e.g., `test(dao): add edge case tests for FoodDao`)

3. **Open a Pull Request:**
   - Push your branch to your fork.
   - Open a PR against `main` on the upstream repository.
   - Fill out the provided [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md).

---

## 3. Code Style & Conventions

- **Kotlin:** Follow the [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and Android Compose style guidelines.
- **Compose UI:** Keep composables stateless where possible; hoist state to ViewModels using `StateFlow`.
- **Architecture Layers:** Respect Clean Architecture boundaries:
  - UI never interacts directly with Room DAOs or Retrofit services.
  - Domain UseCases encapsulate single business actions.
  - Room is the single source of truth for all observed UI data.
- **Design System:** Match the "Vitality Flow" Material3 design tokens defined in `com.kcalgrindai.app.core.designsystem`. Re-use existing colors (`Theme.kt`, `Color.kt`) and typography.
- **JavaScript / Node.js:** Use standard ES module syntax (`import`/`export`) and Fastify best practices.

---

## 4. Testing Requirements

Before pushing your changes or opening a PR, ensure all tests pass cleanly:

1. **Run Android Unit Tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```
   All unit and Robolectric tests must pass with 100% success rate.

2. **Run Backend Integration Tests:**
   ```bash
   cd backend
   node --test test-account-deletion.js test-gemini-client-unit.js test-ai-coach-quota.js test-auth-middleware.js test-gemini-structure.js
   ```

---

## 5. Security & Secret Management

> [!CAUTION]
> **NEVER commit secrets, credentials, or keys.**
> Before staging any commit, check `git status` and `git diff`.
> - Do NOT commit `google-services.json` containing production credentials.
> - Do NOT commit `.env` or files containing `AIza...`, `GEMINI_API_KEY`, or database service keys.
> - Do NOT commit release keystores (`*.jks`, `*.keystore`, `keystore.properties`).
>
> If you accidentally stage a sensitive file, unstage it immediately with `git restore --staged <file>`.

---

## 6. Good First Issue Ideas

Looking for a place to start? We welcome contributions in the following areas:

1. **Documentation Fixes & Clarifications:**
   - Improving setup instructions for different operating systems (Windows/Linux/macOS).
   - Adding developer guides for running Firebase emulators locally.
2. **Additional Unit & Edge-Case Tests:**
   - Adding unit tests for edge cases in `NutritionCalculator.kt`.
   - Expanding DAO query test coverage in `app/src/test/java/.../dao`.
   - Adding validation tests for custom food input boundaries.
3. **Localization & Translations:**
   - Adding string translations for Spanish, German, French, or Hindi in `app/src/main/res/values-*/strings.xml`.
4. **UI Accessibility:**
   - Adding or improving `contentDescription` attributes on icons for screen readers.
   - Verifying TalkBack navigation order across screens.
