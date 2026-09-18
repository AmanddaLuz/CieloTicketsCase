# Checkout specification

## Input

Checkout receives the immutable domain `Cart` already validated by
`BuildCartUseCase`. XML Views and UI models must not rebuild purchase items,
quantities or monetary totals.

`CartBottomSheetFragment` also collects the operator's chosen `PaymentMethod`
(`CREDIT_CASH` or `DEBIT_CASH`) from one of two dedicated buttons. The Views
only dispatch the selected method; they never compute or override it.

## Orchestration

For each accepted checkout action:

1. `CreatePurchaseAttemptUseCase` creates a UUID-backed snapshot that stores
   the chosen `PaymentMethod` alongside the cart items.
2. `SavePurchaseAttemptUseCase` persists the attempt before external work.
3. `StartPaymentUseCase` atomically changes `CREATED` to `PROCESSING`.
4. The owner schedules a unique one-minute timeout.
5. Only that caller launches Cielo.

`STARTING` and `PROCESSING` reject repeated taps, so one visible checkout creates
at most one attempt.

Dismissing a `PROCESSING` BottomSheet detaches only its in-memory presentation.
The persisted attempt and timeout remain active, while the cart becomes
available for another checkout. Starting the next Cielo session performs the
safe active-reference rollover described below.

If the timeout worker persists `TIMED_OUT`, an active checkout presents that
the result is still unknown and directs the operator to history. It does not
open the approved receipt or initiate another charge.

`CieloPaymentRequestEncoderImpl` maps `attempt.paymentMethod` to the Cielo
`paymentCode` field (`CREDITO_AVISTA` or `DEBITO_AVISTA`); see
[ADR 0010](../adr/0010-payment-method-selection.md).

## Callback

The checkout ViewModel observes the package-scoped result through the
`PaymentResultObserver` contract. Its Cielo implementation owns Android receiver
registration and remains active while the Events ViewModel exists, including
while the Cielo application is in the foreground.

Callbacks containing a purchase reference are also handed to WorkManager by
`CieloResponseActivity`. This durable path updates Room even when the process
that initiated Cielo no longer exists. The broadcast remains the immediate UI
path.

- A matching reference updates the current processing attempt.
- A blank reference is accepted only for the current processing attempt.
- A different reference is ignored.
- Cielo terminal statuses and the recoverable local timeout reach
  `UpdatePurchaseStatusUseCase`.
- Persistent compare-and-set rules make repeated callbacks idempotent.
- A blank-reference callback is resolved from the active payment reference
  persisted before Cielo was opened.
- Starting another valid external payment first changes the previous
  `PROCESSING` attempt to `TIMED_OUT`, then activates the new reference.

After accepting the durable handoff, the callback adapter brings
`MainActivity` to the foreground. The transaction result surface observes Room
by reference: approvals expose the receipt and QR Code; denied, cancelled and
error outcomes expose the persisted status without a QR Code.

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

Cancelled, denied and error outcomes follow the validated result hierarchy:
a large semantic icon, emphasized title, concise explanation and full-width
close action inside a subtle blue result surface.
