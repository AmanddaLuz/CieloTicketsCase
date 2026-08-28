# Data specification

## Event catalog

`LocalEventRepositoryImpl` exposes the deterministic ten-event interview
catalog through `EventRepository`. Consumers receive domain models and never
depend on catalog storage details.

## Purchase persistence

Room stores purchases in two normalized tables:

```text
purchase_attempts
  reference PK
  status
  createdAt
  updatedAt

purchase_items
  attemptReference FK
  position
  eventId
  eventName
  quantity
  unitPriceInCents
```

The item foreign key cascades on attempt deletion. Item position preserves the
original cart order, while a unique attempt/event index prevents duplicated
event snapshots.

## Atomic behavior

- Parent attempt and item snapshots are inserted in one transaction.
- Duplicate references return `DuplicateReference` without replacing data.
- Status updates use `reference + expectedStatus` in the SQL `WHERE` clause.
- A failed compare-and-set reports either `NotFound` or the actual current
  status.
- Storage exceptions propagate; they are not converted into business outcomes.
- History is emitted newest-first.

The exported Room schema under `app/schemas/` is versioned with the project.

