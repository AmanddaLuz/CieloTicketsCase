# ADR 0004: Durable Cielo callbacks

**Status:** Accepted

## Decision

Receive the Cielo custom-scheme callback in a no-history Activity, validate its
shape and correlation, then enqueue a compact WorkManager request. A worker
updates Room through `UpdatePurchaseStatusUseCase`.

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
- Custom-scheme provenance remains unsuitable as the sole production
  authorization boundary; backend reconciliation is required for that threat
  model.

