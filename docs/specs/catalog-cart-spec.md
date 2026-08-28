# Catalog and cart specification

## Catalog

`EventsViewModel` loads the deterministic catalog through
`GetAvailableEventsUseCase`. `EventsFragment` only renders immutable UI models
and dispatches event IDs for user actions.

Each event row shows:

- name, venue and date;
- formatted unit price;
- reusable add/remove quantity controls;
- subtotal supplied from the domain cart when selected.

## Cart mutations

Quantity mutations are serialized with a `Mutex`. Each candidate selection is
validated by `BuildCartUseCase` before it becomes visible.

- Quantities never exceed `Event.maxTicketsPerPurchase`.
- Removing the last ticket removes the event.
- An empty cart closes the BottomSheet.
- Invalid candidates do not replace the last valid cart.
- Financial totals and subtotals come from `Cart` and `CartItem`.

## Presentation mapping

`EventsUiMapper` converts domain models into display-only models. Adapters and
custom Views do not multiply prices, sum quantities or enforce business limits.

`QuantitySelectorView` is a passive reusable component configured with a model
and add/remove callbacks.

## BottomSheet

`CartBottomSheetFragment` shares the parent `EventsViewModel`, so catalog rows,
the summary bar and the sheet always render the same cart state.

The sheet lists unique events, allows quantity adjustments, shows the exact
total and can clear the cart. Its checkout action remains hidden until the
checkout phase wires payment orchestration.

