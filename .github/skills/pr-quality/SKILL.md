# PR quality

## Required context

Read the affected spec, ADR and `docs/testing/strategy.md`.

## Required properties

- Small, traceable scope.
- Domain and presentation dependencies point inward.
- Repository and use-case contracts have separate `Impl` classes.
- Views only bind state and dispatch events.
- Payment reference is persisted before opening Cielo.
- Unknown payment results are never retried automatically.
- Behavior changes update their canonical documentation.

## Required validation

Run the smallest applicable subset:

```text
./gradlew lintDebug detekt testDebugUnitTest
./gradlew koverVerifyDebug
./gradlew assembleDebug
```

Payment, persistence and navigation changes require tests for critical states.

