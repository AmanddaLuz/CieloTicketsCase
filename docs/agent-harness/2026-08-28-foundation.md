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
