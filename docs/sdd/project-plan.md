# SDD project plan

**Status:** XML UI foundation completed
**Repository:** `AmanddaLuz/CieloTicketsCase`  
**UI:** Native XML with ViewBinding

## Delivery phases

| Phase | Outcome |
|---|---|
| 0. Foundation | Completed: XML host, quality gates, documentation and GitFlow |
| 1. Domain audit | Completed: models, repository contracts and use-case contracts with `Impl` classes |
| 2. Data | Completed: local catalog and relational Room persistence adapters |
| 3. Payment | Completed: Cielo gateway, durable callback and persistent state machine |
| 4. UI foundation | Completed: navigation, binding lifecycle and reusable state components |
| 5. Catalog and cart | Events list, quantity controls and generic cart BottomSheet |
| 6. Checkout | Single-flight payment orchestration and terminal outcomes |
| 7. History and receipt | Persisted sales, receipt and approved QR Code |
| 8. Hardening | Instrumented tests, emulator validation and final documentation |

Each phase uses a short-lived branch from `develop`. A phase is complete only
when its behavior, tests and canonical documentation agree.

## Acceptance

- No Compose code or dependency.
- Passive XML Views and unidirectional state flow.
- Pure domain and dependency inversion at external boundaries.
- Payment idempotency survives process recreation.
- Lint, Detekt, tests, coverage and build pass.
