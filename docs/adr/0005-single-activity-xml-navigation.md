# ADR 0005: Single-Activity XML navigation

**Status:** Accepted

## Decision

Use one `AppCompatActivity`, one XML `NavHostFragment` and Fragment destinations
with ViewBinding. Share UI primitives as passive Views and lifecycle helpers
instead of base feature classes.

## Rationale

An XML navigation graph makes the back stack explicit and testable. A binding
delegate removes repeated nullable binding code while still clearing references
with the Fragment view lifecycle. Passive compound Views provide reuse without
moving feature decisions into the View layer.

## Consequences

- Home, Events and History are real navigation destinations.
- Feature screens replace placeholders without changing the Activity contract.
- Fragment bindings must only be accessed while their view exists.
- State rendering is shared, while feature state and actions remain owned by
  ViewModels and Fragments.

