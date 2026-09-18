# Processing payment timeout

Date: 2026-09-18
Scope: Timeout for Cielo attempts that remain processing
Input constraint: One-minute timeout, update history and active UI, do not
repeat the charge, and allow a late callback to provide the financial result.
Decision: Schedule unique delayed WorkManager work when the attempt first enters
`PROCESSING`. Persist recoverable `TIMED_OUT` by compare-and-set and preserve
the active reference for late callbacks.
Validation: State-machine and orchestration JVM tests plus API 29 worker tests
for expiration, active-UI broadcast, terminal-state protection and late
approval.
Canonical docs updated: ADR 0011, domain, payment, checkout, product,
history/receipt and testing specs, Cielo constraints, troubleshooting and
project plan.
