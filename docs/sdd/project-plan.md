# SDD project plan

**Status:** Final delivery completed
**Repository:** `AmanddaLuz/CieloTicketsCase`  
**UI:** Native XML with ViewBinding

## Delivery phases

| Phase | Outcome |
|---|---|
| 0. Foundation | Completed: XML host, quality gates, documentation and GitFlow |
| 1. Domain audit | Completed: models, repository contracts and use-case contracts with `Impl` classes |
| 2. Data | Completed: local catalog and relational Room persistence adapters |
| 3. Payment | Completed: Cielo gateway, deep-link callback broadcast and persistent state machine |
| 4. UI foundation | Completed: navigation, binding lifecycle and reusable state components |
| 5. Catalog and cart | Completed: events list, quantity controls and reusable cart BottomSheet |
| 6. Checkout | Completed: persist-before-pay orchestration and terminal outcomes |
| 7. History and receipt | Completed: status-filtered sales, persisted receipt and approved QR Code |
| 8. Hardening | Completed: requirement audit, final validation and documentation |

Each phase uses a short-lived branch from `develop`. A phase is complete only
when its behavior, tests and canonical documentation agree.

## Acceptance

- XML and ViewBinding are the only presentation stack.
- Passive XML Views and unidirectional state flow.
- Pure domain and dependency inversion at external boundaries.
- Payment idempotency survives process recreation.
- Lint, Detekt, tests, coverage and build pass.
