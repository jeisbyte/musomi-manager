
---

## `docs/specialists/DATABASE.md`

```markdown
# DATABASE.md — Database + DevOps Specialist Document

---

## 1. Who This Is For

You are the **Database + DevOps Engineer** on Musomi Manager.

You own the schema, migrations, data integrity, and the database side of deployments. You work alongside the Backend Lead (who writes JPA entities matching your schema) and the DevOps side (covered in `DEVOPS.md`).

You work alongside:
- **Backend Lead** — provides the JPA entities and repositories
- **DevOps** — same person in a small team; `DEVOPS.md` covers server ops
- **Product + QA** — verifies data integrity and migrations
- **JavaFX / Web Developers** — consume the data you model

You own the database. You don't edit backend service code, JavaFX screens, or web templates.

---

## 2. What You Build in v1

- PostgreSQL 15/16 database for a single school
- Flyway migrations for all 25 v1 tables (see `SCHEMA.md`)
- Seed data for local development (`V099__test_data.sql`)
- Indexes and constraints as specified in `SCHEMA.md`
- Backup and restore scripts (with DevOps — see `DEVOPS.md`)
- Data integrity checks and migration tests
- Multi-tenancy enforcement via `school_id`
- Soft-delete patterns
- Audit log table and retention

**Not in v1:** sharding, replication, read replicas, partitioning, data warehouse, analytics tables, multi-school management tables. Those are v2+.

---

## 3. Folder Structure

Flyway migrations live in the backend project:
