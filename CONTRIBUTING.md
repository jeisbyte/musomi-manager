## Team

| Role | Person |
|------|--------|
| Lead / Founder | Jeison & Habib |
| Backend  | Habib & Matthew |
| JavaFX Developer | Jeison & Adrian |
| Web Developer | Rogers & Moses & Dickens |
| Database + DevOps | Solomon & Harry |
| Product + QA | Derick & Habib |

# Contributing to Musomi Manager

## Branches

- `main` — always working, always deployable
- `feature/<name>` — new feature
- `fix/<name>` — bug fix
- `chore/<name>` — tooling, dependencies
- `docs/<name>` — documentation

Merge to `main` every 1–2 days. No long-lived branches.

## Commits

Use [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` new feature
- `fix:` bug fix
- `refactor:` code change that neither fixes a bug nor adds a feature
- `docs:` documentation only
- `test:` adding or updating tests
- `chore:` build process, dependencies, tooling

Example: `feat: add login screen for desktop app`

## Pull Requests

- Title matches commit format.
- Description: what changed, why, how to test.
- One reviewer minimum.
- CI must pass before merge.
- Link the issue.

## Rules

- Paste `MASTER.md` + your specialist doc into every AI session.
- One task per AI prompt.
- Review every AI output before merging.
- Never commit secrets.
- Follow `docs/shared/STYLE_GUIDE.md` in every file.
- Update `API_CONTRACT.md` when endpoints change.
- Update `SCHEMA.md` when tables change.
- Update `ERROR_CODES.md` when errors change.

## Definition of Done

A task is done when:

- [ ] Code follows `STYLE_GUIDE.md`
- [ ] Tests written and passing (≥80% on new code)
- [ ] Error cases handled with standard codes
- [ ] Audit log written (for state changes)
- [ ] Permission check enforced server-side
- [ ] API contract updated if endpoint changed
- [ ] Reviewed and approved by another team member
- [ ] Merged to `main`
- [ ] CI passes
