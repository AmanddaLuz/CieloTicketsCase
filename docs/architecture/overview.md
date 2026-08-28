# Architecture

The project uses MVVM with Clean Architecture in a single Android module. Package
boundaries remain explicit without introducing premature Gradle modules.

```text
presentation -> use-case contract <- use-case Impl
                                  -> repository contract <- repository Impl
                                  -> gateway contract <- Cielo adapter
```

## Layers

- `domain/model`: pure business entities.
- `domain/repository`: repository contracts.
- `domain/gateway`: payment and external-service contracts.
- `domain/usecase`: use-case contracts.
- `domain/usecase/impl`: use-case implementations.
- `data`: repository implementations, Room and local catalog.
- `payment/cielo`: Android-specific Cielo adapter.
- `feature`: ViewModels, immutable UI state, Fragments and adapters.
- `di`: the composition root and factories.

Views are passive presentation adapters. They collect lifecycle-aware state,
render it and dispatch user events. Validation policy, totals, persistence and
payment orchestration stay outside the View layer.

Manual constructor injection is the default. The composition root creates
implementations and exposes contracts; features do not instantiate data or
payment dependencies.

## Implemented domain rules

- `Cart` owns multi-event quantity and monetary calculations.
- `PurchaseAttempt` stores item snapshots instead of a mutable catalog reference.
- `CreatePurchaseAttemptUseCaseImpl` owns reference and timestamp generation.
- Duplicate purchase references never overwrite the original attempt.
- Payment status changes are validated by `UpdatePurchaseStatusUseCaseImpl`.
- Repository status writes use compare-and-set to protect terminal states from
  concurrent callbacks.
- Unknown references, duplicates and invalid transitions produce typed results.

See `../specs/domain-spec.md` for the canonical behavior.

## Data adapters

- `LocalEventRepositoryImpl` supplies the deterministic interview catalog.
- `RoomPurchaseRepositoryImpl` maps domain attempts to normalized Room records.
- `PurchaseAttemptDao` inserts attempts and items transactionally.
- Status changes use an atomic expected-state update.
- `purchase_items.position` restores the original cart order.

See `../specs/data-spec.md` and ADR 0003 for persistence guarantees.

## Payment adapters

- `StartPaymentUseCaseImpl` atomically claims a purchase before launching Cielo.
- `CieloPaymentGatewayImpl` depends on request-encoder and Intent-launcher
  contracts instead of constructing Android details in the domain.
- `CieloPaymentRequestEncoderImpl` creates a multi-item request and correlated
  callback URL.
- `CieloResponseActivity` validates and enqueues callbacks without owning their
  persistence lifecycle.
- `CieloCallbackWorker` applies terminal states through the domain state machine.

See `../specs/payment-spec.md` and ADR 0004 for callback guarantees and the
custom-scheme trust boundary.

## XML presentation foundation

- `MainActivity` contains only the application `NavHostFragment`.
- `HomeFragment` dispatches navigation actions without feature policy.
- Fragment ViewBinding references follow `viewLifecycleOwner` and are released
  at `onDestroyView`.
- `StatePanelView` renders reusable loading and message states.
- `CieloTicketsApplication` owns `AppContainerImpl`, the single composition root
  for repositories, use cases and payment adapters.

See `../specs/ui-foundation-spec.md` and ADR 0005.
