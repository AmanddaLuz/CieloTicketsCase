# Testing strategy

## Test pyramid

- Unit: domain rules, use cases, repositories with fakes, parsers and ViewModels.
- Instrumented: Room, navigation, Fragment lifecycle and critical XML flows.
- Manual: Cielo emulator on Android 10/API 29.

## Critical scenarios

- Cart totals and quantity limits.
- Valid and invalid payment transitions.
- Duplicate references without overwriting the original purchase.
- Multi-event purchase snapshots independent from catalog changes.
- Concurrent duplicate and conflicting terminal callbacks.
- Exact monetary and quantity overflow.
- Persist-before-pay and single-flight checkout.
- Duplicate and unknown callbacks.
- Process recreation with a pending attempt.
- Distinct approved, denied, cancelled and error rendering.
- QR Code restricted to approved purchases.

## Gates

Android Lint and Detekt fail on errors. Kover requires at least 75% eligible line
coverage; Activities, Fragments, Views, adapters and Android wiring are excluded.
Generated ViewBinding classes are also excluded. Tests must protect behavior
rather than inflate metrics.
