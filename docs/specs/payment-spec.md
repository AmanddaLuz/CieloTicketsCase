# Payment specification

## Start

`StartPaymentUseCaseImpl` claims a persisted attempt with the atomic transition
`CREATED -> PROCESSING` before opening Cielo Smart.

- Only the caller that receives `Updated` may launch the payment.
- That caller schedules unique durable timeout work for one minute before
  opening Cielo.
- An `Unchanged(PROCESSING)` result becomes `AlreadyProcessing` and does not
  launch another charge.
- Missing and terminal attempts return typed outcomes.
- Missing credentials, unavailable Cielo app and technical launch failures move
  the attempt from `PROCESSING` to `ERROR`.

## Processing timeout

After one minute, delayed WorkManager work changes an attempt only if it is
still `PROCESSING`. The resulting `TIMED_OUT` status means that the charge
outcome is unknown:

- no automatic payment retry is performed;
- active UI receives the persisted timeout result;
- history and receipt observation expose the new status;
- the active reference remains available for compact late callbacks;
- a late authoritative callback may replace `TIMED_OUT` with its terminal
  result.

## Request

The Cielo payload contains the purchase UUID, exact total and every item
snapshot. Each item sends its event ID as SKU, quantity and unit price in cents.

The callback URL uses the exact format validated by the Cielo LIO emulator:
`order://payment`.

## Callback

The callback adapter validates the expected scheme and host, decodes the Base64
response and rejects malformed data. Approved order responses must contain the
purchase reference. Compact emulator errors may omit it.

Compact callback body codes map as follows:

| Code | Outcome |
| --- | --- |
| 1 | `CANCELLED` |
| 2 | `DENIED` |
| 3 | `DENIED` |
| 4 or unknown | `ERROR` |

Before launching Cielo, the gateway synchronously persists the active purchase
reference. If another reference remains active, its `PROCESSING` attempt first
becomes recoverable `TIMED_OUT`, then the new reference replaces it. Credentials
and request encoding are validated before this rollover. Launch failures clear
the new correlation.

`CieloResponseActivity` performs no direct UI rendering. It parses the callback,
resolves a missing reference from the persisted active payment, enqueues a
compact WorkManager request and sends the package-scoped broadcast. It then
brings `MainActivity` to the foreground with the resolved reference.

The worker and the active checkout ViewModel use the same idempotent state
machine, so either may persist first without allowing a duplicate or
conflicting terminal update. The worker clears the matching active correlation
after processing.

## Trust boundary

Cielo LIO uses an exported custom URI scheme, which cannot cryptographically
prove the sender. A production system that treats the result
as financially authoritative must reconcile the transaction through a trusted
Cielo/backend API or a verified HTTPS callback before fulfillment.
