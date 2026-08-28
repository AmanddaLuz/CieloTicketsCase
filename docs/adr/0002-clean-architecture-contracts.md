# ADR 0002: Explicit contracts and implementations

**Status:** Accepted

## Decision

Repositories and use cases expose interfaces, with concrete classes suffixed by
`Impl`. Dependencies are provided through constructors from a manual composition
root.

## Rationale

Explicit boundaries keep Android, Room and Cielo replaceable and make business
rules deterministic in tests.

## Constraint

An interface is introduced only at an architectural boundary or when it enables
a real substitute. Internal helpers remain concrete to avoid speculative design.

