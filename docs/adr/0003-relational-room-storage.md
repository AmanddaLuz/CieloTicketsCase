# ADR 0003: Relational Room storage

**Status:** Accepted

## Decision

Persist purchase attempts and item snapshots in separate Room tables linked by a
foreign key. Do not serialize cart items into JSON or store only a purchase
summary.

## Rationale

Relational snapshots preserve every purchased event, support schema validation
and avoid coupling persistence to a serialization format. A transaction keeps
the attempt and its items consistent.

Status changes use a compare-and-set SQL update. This ensures two concurrent
callbacks cannot both replace the same expected state.

## Consequences

- Room schema exports are committed.
- Future schema changes require explicit migrations.
- Room adapters are covered by instrumented tests on API 29.
- Domain models remain independent from Room annotations.

