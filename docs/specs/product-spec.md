# Product specification

## Goal

Allow an operator to select local event tickets, charge through Cielo Smart and
show a persisted receipt with a QR Code for approved purchases.

## Functional scope

1. Home with sale and history entry points.
2. Event catalog with name, venue, date and price.
3. Multi-item cart with valid quantities and total in cents.
4. Checkout through the Cielo Smart Deep Link.
5. Persisted approved, denied, cancelled and technical-error outcomes.
6. Sales history and receipt recovery.
7. QR Code only for approved purchases.
8. Sales history filterable by persisted payment status.

## Critical rules

- Zero or negative quantity cannot enter checkout.
- Prices and totals use `Long` cents.
- Repeated clicks create at most one payment attempt.
- A UUID reference is persisted before Cielo is opened.
- Duplicate callbacks are idempotent.
- Unknown references are rejected as technical errors.
- Callbacks are persisted after process recreation through durable background
  work; reference-less errors use the active reference saved before launch.
- A callback reopens the application and presents the persisted transaction
  result.
- Missing callbacks remain pending and never trigger an automatic charge
  retry.
- A `PROCESSING` attempt becomes recoverable `TIMED_OUT` after one minute.
- A late callback may replace `TIMED_OUT` with the real terminal result.
- A new sale is allowed while an older result is unknown; the older active
  attempt becomes `TIMED_OUT` before the new Cielo session starts.
- Credentials and payment data never appear in logs or QR Codes.
- Receipt navigation reloads persisted data by purchase reference.

## Payment states

```text
CREATED -> PROCESSING
PROCESSING -> TIMED_OUT | APPROVED | DENIED | CANCELLED | ERROR
TIMED_OUT -> APPROVED | DENIED | CANCELLED | ERROR
```
