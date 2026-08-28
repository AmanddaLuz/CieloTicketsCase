# Android XML feature

1. Read the feature spec and related ADRs.
2. Define or update domain models and contracts.
3. Implement use-case and repository `Impl` classes through constructor injection.
4. Expose immutable UI state and events from the ViewModel.
5. Build XML/ViewBinding Views that only render and dispatch.
6. Reuse an existing component before introducing a new abstraction.
7. Add deterministic tests for rules and state transitions.
8. Run targeted quality tasks and update canonical documentation.

Do not place business rules, persistence, payment orchestration or formatting
policy in Activities, Fragments, adapters or BottomSheets.

