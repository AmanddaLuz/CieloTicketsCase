# ADR 0009: Filtered history and opaque receipt QR

**Status:** Accepted

## Decision

Keep status filtering in `HistoryViewModel` over the reactive, canonically
ordered history stream. Put the single-selection status chips at the beginning
of the XML page.

Navigate to receipts by purchase reference and load the persisted snapshot
through a dedicated use-case contract. Generate QR content only for approved
purchases and include only the purchase reference behind a fixed
`CIELO_TICKET` prefix.

## Rationale

Filtering a small local case-study history in memory keeps Room and repository
contracts independent from presentation choices. Passing a reference instead of
a serialized UI model prevents stale or incomplete receipts.

An opaque QR payload minimizes exposed information and supports future trusted
validation. Embedding display names, totals or payment fields would disclose
unnecessary data and would not prove ticket validity.

## Consequences

- Filter changes are immediate and do not mutate persisted history.
- New Room emissions automatically respect the currently selected filter.
- Receipt totals always come from the persisted domain snapshot.
- Denied, cancelled, error and pending receipts never display a QR Code.
- QR validation requires resolving the reference against trusted data.
