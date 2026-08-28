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

## Critical rules

- Zero or negative quantity cannot enter checkout.
- Prices and totals use `Long` cents.
- Repeated clicks create at most one payment attempt.
- A UUID reference is persisted before Cielo is opened.
- Duplicate callbacks are idempotent.
- Unknown references are rejected as technical errors.
- Missing callbacks remain pending and are not retried automatically.
- Credentials and payment data never appear in logs or QR Codes.

## Payment states

`CREATED -> PROCESSING -> APPROVED | DENIED | CANCELLED | ERROR`

