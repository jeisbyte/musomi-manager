cat > README.md << 'EOF'
# Musomi Manager

A lifelong learning record system for Ugandan secondary schools.

**Current version:** v1 — Core Operations.

## What's in this repo

- `backend/` — Spring Boot 3.2 + Java 21 + PostgreSQL (API for desktop, HTML for students)
- `desktop/` — JavaFX 21 desktop app for teachers and admins
- `web/` — static assets for the student portal (templates live in `backend/`)
- `docs/` — all project documentation
- `infra/` — deployment configs
- `scripts/` — utility scripts
- `shared/` — shared contracts (if needed)

## Documentation

**Every team member pastes `docs/master/MASTER.md` first into every AI session, then their specialist doc.**

| Document | Purpose |
|----------|---------|
| `docs/master/MASTER.md` | Project context — paste first |
| `docs/specialists/BACKEND.md` | Backend conventions |
| `docs/specialists/DESKTOP.md` | JavaFX conventions |
| `docs/specialists/WEB.md` | Student web conventions |
| `docs/specialists/DATABASE.md` | Schema + migrations |
| `docs/specialists/DEVOPS.md` | Server + deployment |
| `docs/specialists/PRODUCT-QA.md` | Testing + product |
| `docs/shared/API_CONTRACT.md` | Every endpoint |
| `docs/shared/SCHEMA.md` | Every table |
| `docs/shared/DESIGN_SYSTEM.md` | Colors, fonts, components |
| `docs/shared/ERROR_CODES.md` | Every error |
| `docs/shared/STYLE_GUIDE.md` | Naming and formatting |

## Getting started

See `CONTRIBUTING.md`.

