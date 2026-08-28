# Contributing

## GitFlow

- `main`: production releases.
- `develop`: integration branch.
- `feature/*` and `bugfix/*`: branch from and return to `develop`.
- `release/*`: branch from `develop`, merge into `main` and back into `develop`.
- `hotfix/*`: branch from `main`, merge into `main` and `develop`.

Use Conventional Commits, keep branches short-lived and never push directly to
`main` or `develop`.

## Releases

1. Atualize `VERSION` em uma branch `release/*`.
2. Abra PR para `develop`.
3. Depois do merge, abra PR de `develop` para `main`.
4. O merge em `main` cria automaticamente a tag `v<versão>`.

Se a tag já existir, o workflow encerra sem alterar ou recriar a release.

## Pull requests

PRs require CI and resolved conversations; no approval is mandatory in this
personal repository. Only `develop` may target `main`. Follow the PR template and
`.github/skills/pr-quality/SKILL.md`.
