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
and initially hand persistence to WorkManager. This was later simplified by ADR
0006 to the callback URI and package-scoped broadcast already validated in the
Compose application and Cielo emulator. Treat custom-scheme provenance as an
emulator/case constraint and require trusted reconciliation for production.
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

## Catalog and cart phase

Scope: XML event list, quantity controls, cart summary and BottomSheet.
Decision: Serialize quantity mutations and publish selections only after
`BuildCartUseCase` returns a valid domain Cart. Map exact domain totals to
immutable UI models, reuse one passive QuantitySelectorView and share the parent
EventsViewModel with the child BottomSheet. Keep checkout hidden until payment
orchestration is connected.
Canonical docs updated: catalog/cart spec, ADR 0007, architecture, SDD and
testing.

## Checkout phase

Scope: Persist-before-pay orchestration, Cielo return observation and terminal
XML states.
Decision: Pass the validated domain Cart directly to a parent-scoped
CheckoutViewModel, reject repeated active requests and isolate dynamic broadcast
registration behind `PaymentResultObserver`. Keep the observer registered while
the feature ViewModel exists to avoid a return-to-app lifecycle race. Clear the
cart only after a terminal callback is persisted; preserve it after launcher,
availability or credential failures.
Canonical docs updated: checkout spec, ADR 0008, architecture, SDD and testing.

## History and receipt phase

Scope: Reactive persisted history, status filtering, receipt recovery and
approved ticket QR Code.
Decision: Combine a presentation-only status filter with the canonical history
stream and place single-selection chips at the beginning of the XML page.
Navigate using only the purchase reference, reload the persisted snapshot
through `GetPurchaseAttemptUseCase` and generate an opaque QR payload only for
approved purchases. Keep event, monetary and payment details outside the code.
Canonical docs updated: history/receipt spec, ADR 0009, architecture, SDD and
testing.

Post-phase adjustment: align the XML palette with the Compose reference blue,
center receipt fields and ship only the Cielo emulator's public test values in
`local.properties.example`. Real credentials remain untracked.

Approved checkout adjustment: after persisting the terminal result and clearing
the cart, navigate once by purchase reference to the receipt with its QR Code.
Keep non-approved terminal feedback in the checkout BottomSheet.
