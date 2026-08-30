# ADR 0010: Payment method selection at checkout

**Status:** Accepted

## Decision

Let the operator choose between two cash payment methods before starting a
charge: `PaymentMethod.CREDIT_CASH` and `PaymentMethod.DEBIT_CASH`. Carry that
choice through the existing single-flight checkout flow instead of creating a
parallel flow per method.

`CartBottomSheetFragment` renders two `MaterialButton`s ("Crédito à vista" and
"Débito à vista"), both calling `CheckoutViewModel.start(cart, paymentMethod)`.
`PurchaseAttempt` gains a `paymentMethod: PaymentMethod` field set at creation
and preserved through every status transition. `CieloPaymentRequestEncoderImpl`
maps `attempt.paymentMethod` to the Cielo `paymentCode` field
(`CREDITO_AVISTA` or `DEBITO_AVISTA`) instead of using a fixed constant.

The Room schema adds a `paymentMethod` column (`purchase_attempts`, schema
version 2). A destructive-free migration (`MIGRATION_1_2`) backfills
`CREDIT_CASH` for attempts persisted before this decision, since credit was the
only method available at the time.

## Rationale

The domain snapshot, not the UI, must own the payment method: reconstructing
it from button state in a Fragment would duplicate business meaning and risk
divergence from what was actually charged. Adding the field to `PurchaseAttempt`
keeps `CieloPaymentRequestEncoderImpl` a pure translation from domain to Cielo
payload, preserving ADR 0002's isolation of Android/Cielo behind interfaces.

Reusing the existing single-flight `start` orchestration (ADR 0008) avoids a
second payment pathway and keeps idempotency, error handling and terminal
states identical for both methods.

## Consequences

- `CreatePurchaseAttemptUseCase` and `CheckoutViewModel.start` require an
  explicit `PaymentMethod` argument; all call sites were updated.
- Persisted attempts always carry a payment method; the migration assigns
  `CREDIT_CASH` to rows created before this change.
- Views remain passive: buttons only dispatch the chosen method, they do not
  decide `paymentCode` values.
- Adding a future payment method means extending the `PaymentMethod` enum and
  its `cieloCode` mapping, not branching the checkout orchestration.
