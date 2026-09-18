# Durable Cielo callback

Date: 2026-08-29
Scope: Cielo callback recovery after application process death
Input constraint: Preserve the emulator-validated `order://payment` callback
and never correlate a reference-less result to an arbitrary pending purchase.
Decision: Enqueue referenced callbacks through WorkManager and update Room with
the existing idempotent `UpdatePurchaseStatusUseCase`. Retain the package-scoped
broadcast for immediate active-checkout delivery.
Validation: Static analysis, JVM tests, coverage verification, debug build and
instrumented callback persistence scenario.
Canonical docs updated: ADR 0004, ADR 0006, payment, checkout and product specs,
Cielo constraints, testing strategy and troubleshooting.
