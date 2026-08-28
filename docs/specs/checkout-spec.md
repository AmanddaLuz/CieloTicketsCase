# Checkout specification

## Input

Checkout receives the immutable domain `Cart` already validated by
`BuildCartUseCase`. XML Views and UI models must not rebuild purchase items,
quantities or monetary totals.

## Orchestration

For each accepted checkout action:

1. `CreatePurchaseAttemptUseCase` creates a UUID-backed snapshot.
2. `SavePurchaseAttemptUseCase` persists the attempt before external work.
3. `StartPaymentUseCase` atomically changes `CREATED` to `PROCESSING`.
4. Only the caller that owns that transition launches Cielo.

`STARTING` and `PROCESSING` reject repeated taps, so one visible checkout creates
at most one attempt.

## Callback

The checkout ViewModel observes the package-scoped result through the
`PaymentResultObserver` contract. Its Cielo implementation owns Android receiver
registration and remains active while the Events ViewModel exists, including
while the Cielo application is in the foreground.

- A matching reference updates the current processing attempt.
- A blank reference is accepted only for the current processing attempt.
- A different reference is ignored.
- Only terminal statuses reach `UpdatePurchaseStatusUseCase`.
- Persistent compare-and-set rules make repeated callbacks idempotent.

The terminal state is retained in `StateFlow`, so returning to a stopped XML
surface still clears the cart after rendering the persisted result. Gateway
launch, credential and availability failures keep the cart available for
correction or retry.

## XML presentation

`CartBottomSheetFragment` renders the cart, starting state, processing state and
non-approved terminal outcomes in one passive surface. After an approved result
is persisted and the completed cart is cleared, the feature navigates directly
to the receipt loaded by purchase reference so its QR Code is immediately
available.
