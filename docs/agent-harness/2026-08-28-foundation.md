# Foundation record

Date: 2026-08-28  
Scope: Independent native XML project bootstrap  
Input constraint: Preserve CieloTickets behavior while replacing Compose with
XML/ViewBinding and applying explicit repository/use-case contracts.  
Decision: Start from a clean Git history, use the same application ID and publish
to `AmanddaLuz/CieloTicketsCase`.  
Validation: Build, quality and coverage gates must pass before feature migration.  
Canonical docs updated: product spec, SDD, architecture, testing and ADRs.

## Domain phase

Scope: Domain audit and explicit use-case implementations.
Decision: Model a multi-event cart and persist purchase item snapshots so totals
and purchase identity do not remain in ViewModels. Reject duplicate references
and invalid payment transitions through typed results. Status updates use an
atomic expected-state contract so concurrent callbacks cannot replace a terminal
result. Reference and time generation are injected into the creation use case.
Canonical docs updated: domain spec, architecture, SDD and testing strategy.

## Data phase

Scope: Local event catalog and Room persistence.
Decision: Store attempts and item snapshots in normalized tables rather than
JSON or a reduced summary. Use Room transactions for insertion and SQL
compare-and-set for status updates. Debug builds use the `.xml` application ID
suffix so the original Compose app can remain installed during comparison.
Canonical docs updated: data spec, ADR 0003, architecture, SDD and testing.

## Payment phase

Scope: Cielo Smart Deep Link request, callback and persistent state transitions.
Decision: Claim `PROCESSING` atomically before launch, encode every purchase item
and correlate callbacks with the purchase UUID. Validate approved order evidence
and paid amount, then hand persistence to WorkManager so no active UI or Activity
lifecycle is required. Treat custom-scheme provenance as an emulator/case
constraint and require trusted reconciliation for production authorization.
Canonical docs updated: payment spec, Cielo constraints, ADR 0004, architecture,
SDD and testing.

## XML UI foundation phase

Scope: Single-Activity navigation, Fragment lifecycle and reusable state UI.
Decision: Use an XML NavHost with passive Fragments, a view-lifecycle-aware
ViewBinding delegate and a generic StatePanelView. Centralize concrete
repositories, use cases and the Cielo adapter in an Application-owned
AppContainer. Keep Events and History as real destinations whose placeholders
will be replaced incrementally.
Canonical docs updated: UI foundation spec, ADR 0005, architecture, SDD and
testing.
