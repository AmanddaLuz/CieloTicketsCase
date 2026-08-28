# CieloTickets

Android interview case implemented with native XML, ViewBinding, MVVM and Clean
Architecture. The application sells event tickets through Cielo Smart while
preserving payment idempotency and local state.

## Technical baseline

- Kotlin and native Android Views
- `minSdk 24`, `targetSdk 29`, `compileSdk 36`
- Manual dependency injection
- Room persistence and Cielo Deep Link adapter
- Android Lint, Detekt and Kover with 75% eligible line coverage
- GitFlow with protected `main` and `develop`

Debug builds use the `.xml` application ID suffix so the XML and Compose
projects can be installed on the same emulator. Release builds keep
`br.com.amandaluz.cielotickets`.

## Build

Copy `local.properties.example` to `local.properties`, configure the Android SDK
and keep real Cielo credentials outside version control. The example contains
only the public test values accepted by the Cielo emulator.

```bash
./gradlew lintDebug detekt testDebugUnitTest
./gradlew koverVerifyDebug
./gradlew assembleDebug
```

## Documentation

Start with `docs/sdd/project-plan.md`. Product rules, architecture, tests and
decisions each have one canonical document to reduce duplication.
