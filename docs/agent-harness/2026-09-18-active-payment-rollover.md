# Active payment rollover

Date: 2026-09-18
Scope: New checkout while another Cielo attempt remains processing
Input constraint: Allow another sale without assigning a reference-less
callback to the wrong purchase.
Decision: After validating credentials and encoding, change the previous active
`PROCESSING` attempt to recoverable `TIMED_OUT`, atomically replace the active
reference and then launch the new Cielo session. Dismissing a processing
BottomSheet detaches its in-memory checkout so another attempt can be created.
Validation: Coordinator JVM tests, Android Room/SharedPreferences integration
test and full payment/timeout gates.
Canonical docs updated: ADR 0004, payment, checkout and product specs, testing
strategy and troubleshooting.
