# History and receipt specification

## History

The history observes persisted `PurchaseAttempt` snapshots through
`GetSalesHistoryUseCase`. Records remain ordered from newest to oldest and show
all states, including attempts that are still `CREATED` or `PROCESSING`.

A horizontally scrollable, single-selection filter appears at the beginning of
the page. Available values are:

- all;
- approved;
- denied;
- cancelled;
- error;
- processing;
- created.

Filtering is presentation state in `HistoryViewModel`. It never changes the
repository query, persisted attempt or canonical ordering. An empty database and
an empty filtered result use distinct messages.

Selecting a history item navigates with only its purchase reference. The receipt
reloads the snapshot through `GetPurchaseAttemptUseCase`; UI models are not used
as navigation data.

## Receipt

The receipt renders:

- persisted status and creation date;
- immutable event snapshots;
- quantity, unit price and subtotal for every item;
- exact total quantity and total in cents formatted for display;
- selectable purchase reference.

The receipt supports every payment state. A missing reference produces an
explicit not-found state.

## Approved QR Code

Only `APPROVED` purchases produce QR content. The payload is the opaque value:

```text
CIELO_TICKET|<purchase-reference>
```

Event names, prices, credentials, card data and payment response fields are not
embedded. A validator must resolve the reference against trusted purchase data
before admitting a ticket.
