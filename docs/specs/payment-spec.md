# Payment specification

## Start

`StartPaymentUseCaseImpl` claims a persisted attempt with the atomic transition
`CREATED -> PROCESSING` before opening Cielo Smart.

- Only the caller that receives `Updated` may launch the payment.
- An `Unchanged(PROCESSING)` result becomes `AlreadyProcessing` and does not
  launch another charge.
- Missing and terminal attempts return typed outcomes.
- Missing credentials, unavailable Cielo app and technical launch failures move
  the attempt from `PROCESSING` to `ERROR`.

## Request

The Cielo payload contains the purchase UUID, exact total and every item
snapshot. Each item sends its event ID as SKU, quantity and unit price in cents.

The callback URL carries the same opaque UUID as a correlation value:

```text
order://payment?reference=<purchase-uuid>
```

## Callback

The callback adapter validates the expected scheme and host, decodes the Base64
response and rejects malformed or uncorrelated data.

An approved order requires:

- matching response and callback references;
- non-empty order ID and items;
- at least one payment with `paymentFields.statusCode` equal to `0` or `1`;
- a positive `paidAmount` equal to the persisted purchase total.

Compact callback body codes map as follows:

| Code | Outcome |
|---|---|
| 1 | `CANCELLED` |
| 2 | `ERROR` |
| 3 | `DENIED` |
| 4 or unknown | `ERROR` |

`CieloResponseActivity` performs no UI work. It validates the response, places a
compact request in WorkManager and finishes. `CieloCallbackWorker` persists the
terminal state through the same idempotent state machine used elsewhere.

## Trust boundary

Cielo LIO uses an exported custom URI scheme. Payload validation and an opaque
UUID prevent accidental and blind malformed updates, but a custom scheme cannot
cryptographically prove the sender. A production system that treats the result
as financially authoritative must reconcile the transaction through a trusted
Cielo/backend API or a verified HTTPS callback before fulfillment.

