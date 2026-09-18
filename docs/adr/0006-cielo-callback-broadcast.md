# ADR 0006: Cielo callback broadcast

**Status:** Superseded by ADR 0004

## Decision

Use the callback shape already validated with the Cielo LIO emulator:

```text
urlCallback=order://payment
```

`CieloResponseActivity` decodes the response and sends a package-scoped broadcast.
The active checkout ViewModel will persist the result through
`UpdatePurchaseStatusUseCase`.

This decision was superseded after process-death testing confirmed that the
dynamic receiver cannot provide durable delivery. ADR 0004 restores a
WorkManager handoff while preserving the same callback URI and broadcast for
active UI delivery.

## Rationale

Adding query parameters to `urlCallback` and introducing a WorkManager handoff
changed the integration contract without evidence that the Cielo emulator
preserves that callback format. The previously validated implementation
demonstrated reliable deep-link return using a plain callback URI and broadcast.

Reusing the known integration reduces moving parts and avoids an unnecessary
background-work dependency.

## Consequences

- Error callbacks may contain no purchase reference; checkout resolves them
  against its current processing attempt.
- A callback received without an active checkout remains pending and is not
  retried automatically.
- Duplicate and terminal updates remain protected by the persistent
  compare-and-set state machine.
