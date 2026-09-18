# ADR 0004: Durable Cielo callbacks

**Status:** Accepted

## Decision

Receive the Cielo custom-scheme callback in a no-history Activity, validate its
shape and correlation, then enqueue a compact WorkManager request. A worker
updates Room through `UpdatePurchaseStatusUseCase`.

The callback URI remains exactly `order://payment`. The package-scoped
broadcast is retained for immediate delivery to an active checkout, but it is
not responsible for persistence.

Before Cielo is opened, the gateway synchronously claims the purchase reference
in a private active-payment store. This allows compact error callbacks without
a reference to be correlated after process death. When a new valid request is
ready to open Cielo, a previous `PROCESSING` correlation is first changed to
recoverable `TIMED_OUT`; only then does the new reference become active.

After WorkManager accepts the callback, `CieloResponseActivity` brings
`MainActivity` to the foreground with the resolved reference. The result screen
observes Room until the worker publishes the terminal status.

## Rationale

Broadcasts and Activity-scoped coroutines can lose a one-shot callback when no
screen is active or the Activity is destroyed. WorkManager provides a
lifecycle-independent, durable handoff, while compare-and-set keeps duplicate
and conflicting callbacks idempotent.

The full Base64 response is not placed in WorkManager because its `Data` input
has a size limit. Only the validated reference, status and paid amount are
enqueued.

## Consequences

- UI components observe persisted state instead of owning callback delivery.
- SQLite failures can be retried by WorkManager.
- Unknown, malformed and invalid transitions do not mutate purchases.
- The active correlation is cleared only after terminal callback processing or
  an external-launch failure.
- Approved results open the receipt; denied, cancelled and error results open
  the same persisted transaction surface without a QR Code.
- Custom-scheme provenance remains unsuitable as the sole production
  authorization boundary; backend reconciliation is required for that threat
  model.
