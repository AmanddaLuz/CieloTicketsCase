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
- Transactional insertion of attempts and item snapshots.
- Room duplicate-reference handling and compare-and-set outcomes.
- History ordering and mapper round trips.
- Persist-before-pay and single-flight checkout.
- Duplicate and unknown callbacks.
- Process recreation with a pending attempt.
- Multi-item Cielo payload and callback reference correlation.
- Rejection of malformed, mismatched and amount-inconsistent approvals.
- Cielo callback parsing, durable persistence and package-scoped broadcast
  delivery.
- Referenced callback recovery without an active checkout observer.
- Reference-less callback recovery from the persisted active payment.
- Callback result routing when the main application task is backgrounded or
  absent.
- Receipt/result observation while WorkManager changes `PROCESSING` to a
  terminal status.
- Unique one-minute processing timeout scheduling.
- `PROCESSING -> TIMED_OUT` only while the attempt remains pending.
- Late terminal callback recovery from `TIMED_OUT`.
- Active-checkout timeout presentation without automatic payment retry.
- Safe active-payment rollover: previous `PROCESSING` becomes `TIMED_OUT`
  before a new reference is activated.
- Credential or request failures do not expire the previous active payment.
- XML destination navigation and back-stack behavior.
- Fragment binding usage across view recreation.
- Reusable loading, message and action-state rendering.
- Layout geometry for primary Home actions.
- Serialized catalog quantity changes and per-event limits.
- Domain-backed cart totals and removal of the final item.
- Catalog-to-BottomSheet synchronization.
- Distinct approved, denied, cancelled and error rendering.
- Persist-before-pay ordering and duplicate checkout tap rejection.
- Reference-less callbacks and rejection of foreign callbacks.
- Callback observer registration across the external Cielo application.
- Cart cleanup only after a persisted terminal callback.
- QR Code restricted to approved purchases.
- Status filters over reactive history without changing persisted ordering.
- Empty database and empty filtered-result presentation.
- Receipt recovery by reference instead of navigation snapshots.
- Multi-event receipt item, quantity and exact total mapping.
- Opaque QR payload without event or payment details.

## Gates

Android Lint and Detekt fail on errors. Kover requires at least 80% eligible line
coverage; Activities, Fragments, Views, adapters and Android wiring are excluded.
Generated ViewBinding classes are also excluded. Tests must protect behavior
rather than inflate metrics.

Room entities, DAO, database and the Android-backed repository are excluded from
JVM Kover and validated with instrumented tests on API 29.

Android Views, Fragment binding delegates, lifecycle adapters and navigation
wiring are validated by instrumented tests and excluded from JVM coverage.
