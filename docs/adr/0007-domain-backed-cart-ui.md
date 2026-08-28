# ADR 0007: Domain-backed cart UI

**Status:** Accepted

## Decision

Keep only event-ID selections as internal ViewModel input. Build every visible
cart through `BuildCartUseCase` and map the resulting `Cart` into immutable UI
models.

Use one parent-scoped `EventsViewModel` for the catalog Fragment and its child
cart BottomSheet.

## Rationale

Calculating totals in XML Views, Adapters or ViewModels would duplicate domain
rules and could diverge from checkout. Serializing mutations prevents rapid
clicks from publishing stale cart results.

Sharing the parent ViewModel avoids passing mutable maps or parceling domain
objects into the BottomSheet.

## Consequences

- The UI never publishes an invalid candidate selection.
- Catalog and BottomSheet quantity controls stay synchronized.
- All currency strings are presentation mappings of exact domain values.
- Checkout can consume the same validated domain cart in the next phase.

