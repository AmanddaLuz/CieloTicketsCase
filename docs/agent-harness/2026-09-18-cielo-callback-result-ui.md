# Cielo callback result UI

Date: 2026-09-18
Scope: Visible Cielo result after BottomSheet dismissal or process death
Input constraint: The callback must not depend on an in-memory ViewModel, and a
reference-less error must never be assigned to an arbitrary processing attempt.
Decision: Claim the active reference synchronously before launching Cielo,
resolve compact callbacks from that store, bring `MainActivity` to the
foreground and observe the purchase in Room on the receipt/result surface.
Validation: JVM tests, lint, Detekt, Kover, debug builds and API 29 instrumented
tests for referenced and reference-less callbacks, Room observation and result
routing.
Canonical docs updated: ADR 0004, payment, checkout and product specs, Cielo
constraints, testing strategy, project plan and troubleshooting.
