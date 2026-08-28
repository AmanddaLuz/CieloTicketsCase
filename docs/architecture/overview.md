# Architecture

The project uses MVVM with Clean Architecture in a single Android module. Package
boundaries remain explicit without introducing premature Gradle modules.

```text
presentation -> use-case contract <- use-case Impl
                                  -> repository contract <- repository Impl
                                  -> gateway contract <- Cielo adapter
```

## Layers

- `domain/model`: pure business entities.
- `domain/repository`: repository contracts.
- `domain/gateway`: payment and external-service contracts.
- `domain/usecase`: use-case contracts.
- `domain/usecase/impl`: use-case implementations.
- `data`: repository implementations, Room and local catalog.
- `payment/cielo`: Android-specific Cielo adapter.
- `feature`: ViewModels, immutable UI state, Fragments and adapters.
- `di`: the composition root and factories.

Views are passive presentation adapters. They collect lifecycle-aware state,
render it and dispatch user events. Validation policy, totals, persistence and
payment orchestration stay outside the View layer.

Manual constructor injection is the default. The composition root creates
implementations and exposes contracts; features do not instantiate data or
payment dependencies.

