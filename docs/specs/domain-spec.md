# Domain specification

## Catalog

An event has a stable non-blank ID, display data, a positive price in `Long`
cents and a positive per-purchase ticket limit.

## Cart

A cart contains one or more unique events. Each item respects its event limit.
The domain calculates item subtotals, total quantity and total price using exact
integer arithmetic. Presentation code must not calculate financial totals.

## Purchase snapshot

A purchase attempt stores defensive item snapshots so history and receipts do
not depend on a future catalog change. Its UUID is the idempotency key.

Saving accepts only a new `CREATED` attempt. A duplicate reference does not
overwrite the original attempt.

`CreatePurchaseAttemptUseCaseImpl` receives reference and time providers, making
UUID and timestamp generation deterministic in tests.

## State machine

```text
CREATED -> PROCESSING
PROCESSING -> APPROVED | DENIED | CANCELLED | ERROR
```

Repeating the current status is idempotent. Terminal states cannot transition to
another state. Unknown references and invalid transitions return typed results
instead of being ignored.

Status persistence uses compare-and-set with the expected current status. This
prevents concurrent callbacks from replacing one terminal result with another.
Storage exceptions are not converted into duplicate or not-found results.

## Boundaries

- Repository and payment gateway contracts live in the domain.
- Use cases expose interfaces and implementations live in `domain/usecase/impl`.
- Data and Cielo implementations will be supplied by later phases.
- Repository history is exposed newest-first.
- Monetary and quantity overflow becomes an explicit cart-building result.
