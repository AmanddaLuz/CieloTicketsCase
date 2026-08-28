# ADR 0001: Native XML UI

**Status:** Accepted

## Decision

Use native Android Views, XML layouts and ViewBinding. A single Activity hosts
Fragments through Navigation Component.

## Rationale

The Cielo Smart target uses Android 10 and benefits from a conventional,
predictable UI stack aligned with the project requirements.

## Consequences

- XML is the only UI source of truth.
- Binding references must be cleared with the Fragment view lifecycle.
- Reusable BottomSheets and state components must remain presentation-only.
