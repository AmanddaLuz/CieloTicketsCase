# ADR 0011: Recoverable processing payment timeout

**Status:** Accepted

## Decision

When a purchase first transitions from `CREATED` to `PROCESSING`, enqueue unique
WorkManager work for its reference with an initial delay of one minute.

If the attempt is still `PROCESSING` when the worker runs, apply
`PROCESSING -> TIMED_OUT` through the existing compare-and-set state machine.
`TIMED_OUT` represents an unknown financial outcome, is not terminal and may
transition to `APPROVED`, `DENIED`, `CANCELLED` or `ERROR` when a late callback
or trusted reconciliation arrives.

The timeout worker publishes a package-scoped result only after the persisted
transition succeeds, allowing an active checkout to present the timeout. A
closed application is not forcibly opened; history and receipt observation
show the persisted state when the user returns.

## Rationale

Treating timeout as `ERROR` would incorrectly assert that no charge occurred
and would reject a later authoritative result. Keeping `PROCESSING` forever
does not satisfy the operational timeout requirement.

Unique delayed work survives process death. Compare-and-set makes it harmless
when a Cielo callback wins before the deadline.

## Consequences

- Timeout never retries or repeats the charge.
- The active Cielo correlation remains stored after timeout so a late
  reference-less callback can still be resolved.
- A callback received before one minute makes the timeout worker a no-op.
- A late terminal callback replaces `TIMED_OUT` exactly once.
- Production financial authority still requires backend/acquirer
  reconciliation.
