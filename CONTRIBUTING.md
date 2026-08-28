# Contributing

## GitFlow

- `main`: production releases.
- `develop`: integration branch.
- `feature/*` and `bugfix/*`: branch from and return to `develop`.
- `release/*`: branch from `develop`, merge into `main` and back into `develop`.
- `hotfix/*`: branch from `main`, merge into `main` and `develop`.

Use Conventional Commits, keep branches short-lived and never push directly to
`main` or `develop`.

## Pull requests

PRs require CI and resolved conversations; no approval is mandatory in this
personal repository. Only `develop` may target `main`. Follow the PR template and
`.github/skills/pr-quality/SKILL.md`.
