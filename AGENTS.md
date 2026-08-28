# Agent guide

## Source of truth

- Product behavior: `docs/specs/product-spec.md`
- Delivery phases: `docs/sdd/project-plan.md`
- Architecture: `docs/architecture/overview.md`
- Tests and quality gates: `docs/testing/strategy.md`
- Decisions: `docs/adr/`

## Non-negotiable rules

- XML native UI with ViewBinding as the only presentation stack.
- Pure Kotlin domain with inward dependency flow.
- Interfaces plus `Impl` classes for repositories and use cases.
- Passive Views, immutable UI state and constructor injection.
- Money in `Long` cents.
- Persist idempotency reference before invoking Cielo.
- Do not retry an unknown payment result automatically.
- Never log or commit credentials or payment data.
- Update the canonical document when behavior or a decision changes.

## Workflow

1. Work from an updated `develop` branch using a short-lived branch.
2. Read the applicable spec and ADR.
3. Implement the smallest complete vertical change.
4. Add deterministic tests for rules and state transitions.
5. Run the smallest applicable validation.
6. Record relevant AI constraints or decisions in `docs/agent-harness/`.
