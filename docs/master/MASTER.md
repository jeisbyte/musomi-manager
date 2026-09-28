# MASTER.md — Musomi manager
---

## 1. Project Overview

**Musomi manager** is a lifelong learning record system for Ugandan secondary schools.

It captures every assessment at topic level, tracks student progress across years, generates professional report cards, and — in later versions — uses AI to guide students toward their strengths and careers.

**v1 goal:** One school enters marks for one term and generates report cards.

**Current version:** v1 — Core Operations.

---

## 2. Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot 3.2.x |
| Database | PostgreSQL 15/16 |
| Migrations | Flyway |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT + BCrypt |
| Desktop | JavaFX 21 + FXML + Scene Builder |
| Web (student) | Thymeleaf + HTMX + Tailwind CSS |
| Build | Maven |
| PDF | OpenPDF or iText |
| Excel | Apache POI |
| Hosting | School server (Ubuntu/Windows) + Cloudflare Tunnel |
| CI/CD | GitHub Actions |

---

## 3. Version Status

**We are building v1.** Everything else is planned but not built.

| Version | Name | Focus | Status |
|---|---|---|---|
| v1 | Core Operations | Marks + topic tagging + reports | Building |
| v2a | Daily Usability | Offline + notifications + UX | Planned |
| v2b | Wider Access | Parents + sharing + messaging | Planned |
| v3 | Insights | Analytics on topic data | Planned |
| v4 | Intelligence | AI narratives, recommendations | Planned |
| v5 | Scale & Fairness | Multi-school, billing, equity | Planned |
| v6 | Full Vision | Career maps, advanced AI | Planned |

**Rule:** Never start a later version until the previous one is stable and used by at least one real school.

---

## 4. v1 Scope

### In v1

- Login (ADMIN, TEACHER, STUDENT roles)
- School structure (years, terms, classes, streams, subjects, topics)
- Student records with Excel import
- Teacher assignments
- Assessment creation with topic tagging
- Mark entry grid (keyboard-driven, auto-save)
- Draft/Published workflow
- Subject and class teacher comments
- Term report card PDF
- Student portal (web) to view marks and download reports
- Audit log for key actions
- School settings (name, logo, grading scale)

### Not in v1

- AI features
- Analytics and charts
- Parent portal
- Offline mode
- Teacher-to-teacher sharing
- Notifications and messaging
- Multi-school management
- Custom roles
- Fine-grained permissions
- Equity disaggregation
- Self/peer assessment
- Career guidance
- Two-factor authentication
- Billing

**Every one of these is planned for v2–v6. None are in v1.**

---

## 5. Non-Negotiable Design Rules

These cannot be changed without team agreement.

1. **Topic tagging exists in v1.** Even though analytics isn't in v1, every assessment must be taggable with a topic. Without this, v3 has no data to analyze.

2. **Every school-scoped table has `school_id`.** Even though v1 serves one school, the schema is multi-tenant from day one.

3. **Longitudinal by default.** Every record has date, term, year, level. History is never lost.

4. **Soft deletes only.** Students and scores are never hard-deleted.

5. **Audit log for state changes.** Every mark change, publish, user change, and settings change is logged.

6. **Two frontends, one backend.** JavaFX desktop for teachers/admins, Thymeleaf web for students. Same Spring Boot backend, same PostgreSQL.

---

## 6. Repository Structure

```
musomi-manager/
├── README.md
├── CONTRIBUTING.md
├── .gitignore
├── docker-compose.yml
├── Makefile
│
├── docs/                          ← all documentation
│   ├── master/
│   │   └── MASTER.md              ← this file
│   ├── specialists/
│   │   ├── BACKEND.md
│   │   ├── DESKTOP.md
│   │   ├── WEB.md
│   │   ├── DATABASE.md
│   │   ├── DEVOPS.md
│   │   └── PRODUCT-QA.md
│   ├── shared/
│   │   ├── API_CONTRACT.md
│   │   ├── SCHEMA.md
│   │   ├── STYLE_GUIDE.md
│   │   ├── ERROR_CODES.md
│   │   └── DESIGN_SYSTEM.md
│   └── 00-vision/
│       ├── vision.md
│       ├── full-version.md
│       └── version-plan.md
│
├── backend/                       ← Spring Boot
├── desktop/                       ← JavaFX
├── web/                           ← student web assets
├── shared/                        ← shared contracts
├── scripts/                       ← utility scripts
├── infra/                         ← deployment configs
└── .github/                       ← CI/CD
```

---

## 7. Naming Conventions

### Java

| Thing | Convention | Example |
|---|---|---|
| Package | lowercase | `com.musomi.manager.service` |
| Class | PascalCase | `StudentService` |
| Method | camelCase, verb-first | `findByAdmissionNumber()` |
| Field | camelCase | `admissionNumber` |
| Constant | UPPER_SNAKE_CASE | `MAX_SCORE` |
| Boolean | `isX` / `hasX` | `isActive`, `hasGuardian` |

### Database

| Thing | Convention | Example |
|---|---|---|
| Table | snake_case, plural | `students`, `assessments` |
| Column | snake_case | `admission_number` |
| Foreign key | `<table_singular>_id` | `student_id` |
| Index | `idx_<table>_<column>` | `idx_students_admission_number` |
| Migration | `V<n>__<description>.sql` | `V010__create_students.sql` |

### Frontend

| Thing | Convention | Example |
|---|---|---|
| FXML file | kebab-case | `mark-entry.fxml` |
| JavaFX controller | PascalCase | `MarkEntryController` |
| JavaFX CSS class | kebab-case | `.btn-primary` |
| Thymeleaf template | kebab-case | `dashboard.html` |
| Thymeleaf fragment | kebab-case | `topbar.html` |

### Git

| Thing | Convention | Example |
|---|---|---|
| Branch | `<type>/<description>` | `feature/mark-entry`, `fix/login-bug` |
| Commit | Conventional Commits | `feat: add mark entry grid` |
| PR title | Same as commit | `[FEAT] Mark entry grid` |

---

## 8. Git Conventions

- **Main branch:** `main` — always working, always deployable
- **Feature branches:** `feature/<name>`, `fix/<name>`, `chore/<name>`, `docs/<name>`
- **Merge to main every 1–2 days** — no long-lived branches
- **Every PR reviewed** by one other team member
- **CI must pass** before merge (GitHub Actions)
- **Commit format:** `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`

---

## 9. API Conventions

- **Base URL:** `/api/v1`
- **Auth:** JWT in `Authorization: Bearer <token>` header for API
- **Auth:** Session cookie (JSESSIONID) for HTML web pages
- **Content-Type:** `application/json`
- **JSON fields:** camelCase (`admissionNumber`, `createdAt`)

### Success Response

```json
{ "data": { ... } }
```

### Error Response

```json
{
  "error": {
    "code": "STUDENT_NOT_FOUND",
    "message": "Student not found.",
    "details": null
  }
}
```

### Paginated Response

```json
{
  "data": [ ... ],
  "page": 0,
  "size": 20,
  "total": 145
}
```

---

## 10. Database Conventions

- **Every school-scoped table has `school_id`** (FK to `schools`)
- **Soft deletes only** — `is_active` BOOLEAN, default true
- **Audit columns:** `created_at`, `updated_at`, `created_by`
- **Timestamps in UTC**
- **Foreign keys enforced**
- **Unique constraints** where specified (e.g., admission number per school)
- **Indexes on foreign keys and search columns**

Full schema: see `docs/shared/SCHEMA.md`

---

## 11. Error Handling

Every error has:
- A **code** (UPPER_SNAKE_CASE) — e.g., `STUDENT_NOT_FOUND`
- A **message** — human-readable, no jargon
- Optional **details** — additional context

Standard errors:
- `INVALID_CREDENTIALS`
- `ACCOUNT_LOCKED`
- `STUDENT_NOT_FOUND`
- `ASSESSMENT_NOT_FOUND`
- `SCORE_EXCEEDS_MAX`
- `NOT_YOUR_CLASS`
- `PERMISSION_DENIED`
- `VALIDATION_FAILED`

Full catalog: see `docs/shared/ERROR_CODES.md`

---

## 12. Roles and Permissions (v1)

Three preset roles only:

| Role | Can do |
|---|---|
| **ADMIN** | Full access within the school |
| **TEACHER** | Enter marks for assigned classes only |
| **STUDENT** | View own marks only |

No custom roles in v1. No fine-grained permissions yet. That's v5.

**Resource-level rules:**
- A teacher can only edit marks for classes they're assigned to
- A student can only see their own data
- These are enforced **server-side**, not just hidden in the UI

---

## 13. Design System

| Element | Value |
|---|---|
| Primary | Indigo 600 `#4F46E5` |
| Success | Emerald 600 `#059669` |
| Warning | Amber 500 `#F59E0B` |
| Danger | Red 600 `#DC2626` |
| Background | Slate 50 `#F8FAFC` |
| Text primary | Slate 900 `#0F172A` |
| Border | Slate 200 `#E2E8F0` |
| UI Font | Inter |
| Number Font | JetBrains Mono |
| Radius | 8px inputs, 12px cards, pill badges |
| Shadow | Subtle, layered |

Full details: see `docs/shared/DESIGN_SYSTEM.md`

---

## 14. Definition of Done

A task is done when:

- Code follows STYLE_GUIDE
- Tests written and passing (≥80% on new code)
- Error cases handled with standard codes
- Audit log written (for state changes)
- Permission check enforced server-side
- API contract updated if endpoint changed
- Reviewed and approved by another team member
- Merged to main
- CI passes

**Full checklist:** see `docs/04-team/DONE.md`

---

## 15. AI Rules

When using AI (ChatGPT, Claude, DeepSeek):

1. **Always paste MASTER.md first**, then your specialist doc
2. **One task per prompt** — not "build the whole feature"
3. **Review every output** — never merge AI code you don't understand
4. **Never paste secrets** — no `.env`, no JWT secrets, no DB passwords
5. **Test AI code more** — AI produces plausible code that fails edge cases
6. **Log AI usage** — note substantially AI-generated files in the PR
7. **No AI for architecture decisions** — humans decide
8. **No AI for cross-cutting refactors** — use IDE tools

**What to use AI for:** boilerplate, entities, DTOs, templates, tests, docs, debugging.

**What NOT to use AI for:** schema design, security config, version planning, cross-module refactors.

---

## 16. How Team Members Use This Document

Every team member pastes this file at the top of every AI session.

Then they add their specialist document:

| Role | Adds |
|---|---|
| Backend Lead | `docs/specialists/BACKEND.md` |
| JavaFX Developer | `docs/specialists/DESKTOP.md` |
| Web Developer | `docs/specialists/WEB.md` |
| Database + DevOps | `docs/specialists/DATABASE.md` + `DEVOPS.md` |
| Product + QA | `docs/specialists/PRODUCT-QA.md` |

Then, if needed, they paste the relevant section from:
- `docs/shared/API_CONTRACT.md` — for the endpoints they're working on
- `docs/shared/SCHEMA.md` — for the tables they're working with
- `docs/shared/DESIGN_SYSTEM.md` — for UI work
- `docs/shared/ERROR_CODES.md` — for error handling

**Example — Backend dev working on MarkService:**

```
[Paste MASTER.md]
[Paste BACKEND.md]
[Paste SCHEMA.md sections: scores, assessments]
[Paste API_CONTRACT.md section: PUT /teacher/scores/{id}]

"Write MarkService.updateScore() following the conventions."
```

---

## 17. Team

| Person | Role |
|---|---|
| [Name] | Lead / Founder |
| [Name] | Backend Lead |
| [Name] | JavaFX Developer |
| [Name] | Web Developer |
| [Name] | Database + DevOps |
| [Name] | Product + QA |

---

## 18. The First School

v1 requires a specific school:
- Named school
- Head teacher or DOS who agreed to pilot
- Specific term when they'll use it
- Specific date for server installation

**Without a pilot school, v1 is just code.**

---

## 19. Timeline

| Month | Focus |
|---|---|
| Month 1 | Foundation, auth, school structure |
| Month 2 | Student records, teacher assignments, subjects, topics |
| Month 3 | Assessments, mark entry grid, publish workflow, student web |
| Month 4 | Reports, testing, packaging, deploy to first school |

**Target:** one real school using the system for one full term.

---

## 20. Success Test

v1 is done when:

- Admin sets up the school in under 1 day
- Students imported from Excel
- Teachers enter marks for all subjects
- Students log in from home and see their marks
- Report cards generate as professional PDFs
- No data loss
- School completes the term using the system
- Teachers say: "We'd use this again next term"

If any of these fail, v1 isn't done.

---

## The One-Sentence Summary

**Musomi manager is a learning record system for Ugandan schools. We're building v1: one school, one term, marks, reports, student portal. Java backend, JavaFX desktop, Thymeleaf web. Topic tagging from day one. No AI, no analytics, no parents yet. Read the specialist doc for your role.**

---


