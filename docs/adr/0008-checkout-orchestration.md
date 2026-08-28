# ADR 0008: Checkout orchestration

**Status:** Accepted

## Decision

Pass the validated domain `Cart` from `EventsViewModel` to a parent-scoped
`CheckoutViewModel`. Persist a purchase snapshot before invoking the existing
single-flight payment use case.

Hide Android broadcast registration behind `PaymentResultObserver`. Its Cielo
implementation is registered for the lifetime of the checkout ViewModel instead
of a started Fragment, preventing the deep-link Activity from broadcasting
before the receiver is restored after returning from the Cielo application.

## Rationale

Reconstructing a cart from formatted UI models would duplicate domain rules and
could change exact totals. Fragment lifecycle registration also creates a race:
the Fragment is stopped while Cielo is foregrounded, but `CieloResponseActivity`
can emit its result before that Fragment starts again.

## Consequences

- Views remain passive and never calculate payment values.
- Repeated taps are rejected while checkout is starting or processing.
- A callback without a reference applies only to the current processing attempt.
- A callback for another reference cannot change the visible attempt.
- The cart is cleared after a persisted terminal result, while launch failures
  preserve it for retry.
