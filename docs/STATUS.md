# Musomi Manager — Status Report

_Generated: 2026-10-01 14:48:01 +03:00_

Evidence is from the checked-in files and commands listed below. The repository root is this directory; the Maven wrapper and backend source tree are under `backend\`. Tests and application startup were not run.

## 1. Repository

- **Branch:** `feat/user-entity-and-flyway-fix`
- **Sync with `origin/main`:** 9 commits ahead, 2 behind (`git rev-list --left-right --count origin/main...HEAD` returned `2 9`).
- **Last 10 commits:**
  - `d604df4` test(auth): add JwtTokenProviderTest covering token generation, claims, and validation
  - `c0176d9` chore: remove stray pracApp folder from repository
  - `68d932e` test: add Testcontainers base class for integration tests
  - `0273a64` test(auth): add AuthServiceTest covering login, lockout, and account states
  - `aa5b491` fix(auth): return SESSION_EXPIRED with standard error envelope on invalid token
  - `b6f5a23` chore(config): move JWT secret to environment variable
  - `b443b06` chore(deps): add spring-boot-starter-actuator
  - `d2ef0dc` refactor(auth): return UserSummary from /me instead of raw Map
  - `b440533` feat(auth): add School entity and dev seeder, fix User.school relationship
  - `9703e6f` feat: User entity, repository, V002 migration, and Flyway starter fix
- **Working tree:** `docs/STATUS.md` is untracked; no staged changes were reported. This report replaces the previously untracked copy.
- The requested `.\mvnw.cmd` and `src\...` paths do not exist at repository root. The backend wrapper and source paths are under `backend\`; the desktop and docs are inside this repository, not siblings of the repository root.

## 2. Actual Stack

Versions below come from `backend\pom.xml` and the requested `dependency:list` command run from `backend\`. The PostgreSQL server image is additionally read from `docker-compose.yml`.

| Technology | Actual on disk | MASTER.md §2 | Comparison |
|---|---|---|---|
| Java | 21 (`java.version` in `backend\pom.xml`) | Java 21 | Match |
| Spring Boot | 4.1.1 parent and resolved starters | Spring Boot 3.2.x | **Mismatch** |
| Spring Security | `spring-security-core` 7.1.1; JWT library JJWT 0.12.6; `BCryptPasswordEncoder` configured | Spring Security + JWT + BCrypt (no versions) | Present; no version specified in MASTER |
| Hibernate / JPA | `hibernate-core` 7.4.5.Final; Spring Data JPA starter declared | Spring Data JPA (Hibernate) | Present; no version specified in MASTER |
| PostgreSQL | JDBC driver 42.7.13; server image `postgres:16` in `docker-compose.yml` (server is not versioned in `pom.xml`) | PostgreSQL 15/16 | Image major matches; server patch version unknown |
| Flyway | `flyway-core` and PostgreSQL database module 12.4.0; Spring Boot Flyway starter 4.1.1 | Flyway (no version) | Present |
| Desktop | JavaFX 21.0.2 in `desktop\pom.xml` | JavaFX 21 | Match |
| Student web | Thymeleaf starter declared; no files under backend templates/static; no HTMX or Tailwind dependency/file found | Thymeleaf + HTMX + Tailwind CSS | **Mismatch:** only Thymeleaf dependency is present |
| Build | Maven | Maven | Match |
| PDF | No OpenPDF or iText dependency in `backend\pom.xml` | OpenPDF or iText | **Mismatch:** absent |
| Excel | No Apache POI dependency in `backend\pom.xml` | Apache POI | **Mismatch:** absent |
| Hosting | `infra\` contains only `.gitkeep`; no Cloudflare Tunnel config found | School server + Cloudflare Tunnel | Deployment setup unknown |
| CI/CD | `.github\workflows\ci.yml` runs backend and desktop `mvn clean verify` on pushes/PRs to `main`; latest run result not inspected | GitHub Actions | Configured |

`spring-boot-starter-webmvc` is declared rather than `spring-boot-starter-web`; the requested dependency filter returned WebMVC 4.1.1.

## 3. Backend — What Exists

There are 26 Java source files in `backend\src\main\java`; the table covers the 25 files in the requested component categories. The remaining file is the application entry point, `MusomiManagerApplication`.

| Component | Files | Status |
|---|---|---|
| Entities | **2:** `School`, `User` (plus `entity\enums\Role`) | Implemented for schools and users only |
| Repositories | **2:** `SchoolRepository`, `UserRepository` | Implemented for schools and users only |
| Services | **1:** `AuthService` | Login and user summary only |
| API controllers | **1:** `AuthApiController` | Login and `/me` implemented; logout is a stub |
| Web controllers | **0** | Missing |
| DTOs | **7:** requests `CreateAcademicYearRequest`, `LoginRequest`; responses `AcademicYearResponse`, `ApiResponse`, `ErrorResponse`, `LoginResponse`, `UserSummary` | Login/error types used; academic-year request/response have no endpoint |
| Mappers | **0** | Missing |
| Exception classes | **6:** `AuthenticationException`, `ErrorCode`, `GlobalExceptionHandler`, `PermissionDeniedException`, `ResourceNotFoundException`, `ValidationException` | Present |
| Security classes | **3:** `CustomUserDetailsService`, `JwtAuthenticationFilter`, `JwtTokenProvider` | Present |
| Config classes | **2:** `DevDataSeeder`, `SecurityConfig` | Present |

`AuthService.login` looks up users using school ID `1L`. `SecurityConfig` is stateless, permits `/api/v1/auth/me` without authentication, and requires authentication for other non-permitted paths. Logout clears the request security context but does not revoke the JWT.

## 4. Migrations

| Version | Filename | Table created |
|---|---|---|
| V001 | `V001__create_schools.sql` | `schools` |
| V002 | `V002__create_users.sql` | `users` |

SCHEMA.md defines 25 tables; 23 have no migration: `academic_years`, `terms`, `class_levels`, `classes`, `streams`, `user_sessions`, `login_history`, `students`, `guardians`, `subjects`, `class_subjects`, `topics`, `teacher_assignments`, `assessments`, `assessment_topics`, `scores`, `score_history`, `comments`, `report_requests`, `generated_reports`, `audit_log`, `school_settings`, and `import_jobs`.

## 5. API Endpoints

API_CONTRACT.md §2 sets `/api/v1` as the base URL; the paths below include that prefix. There are **63** endpoints in §§5–17: **2 implemented, 1 stub, 60 missing**.

| Method | Path | Controller | Status |
|---|---|---|---|
| POST | `/api/v1/auth/login` | `AuthApiController` | implemented |
| POST | `/api/v1/auth/logout` | `AuthApiController` | stub |
| GET | `/api/v1/auth/me` | `AuthApiController` | implemented |
| POST | `/api/v1/auth/change-password` | — | missing |
| GET | `/api/v1/admin/users` | — | missing |
| POST | `/api/v1/admin/users` | — | missing |
| GET | `/api/v1/admin/users/{id}` | — | missing |
| PUT | `/api/v1/admin/users/{id}` | — | missing |
| DELETE | `/api/v1/admin/users/{id}` | — | missing |
| POST | `/api/v1/admin/users/{id}/reset-password` | — | missing |
| GET | `/api/v1/admin/academic-years` | — | missing |
| POST | `/api/v1/admin/academic-years` | — | missing |
| PUT | `/api/v1/admin/academic-years/{id}/current` | — | missing |
| GET | `/api/v1/admin/terms?yearId={id}` | — | missing |
| POST | `/api/v1/admin/terms` | — | missing |
| PUT | `/api/v1/admin/terms/{id}/current` | — | missing |
| GET | `/api/v1/admin/class-levels` | — | missing |
| POST | `/api/v1/admin/class-levels` | — | missing |
| GET | `/api/v1/admin/classes?yearId={id}` | — | missing |
| POST | `/api/v1/admin/classes` | — | missing |
| POST | `/api/v1/admin/streams` | — | missing |
| GET | `/api/v1/admin/subjects` | — | missing |
| POST | `/api/v1/admin/subjects` | — | missing |
| GET | `/api/v1/admin/subjects/{id}/topics` | — | missing |
| POST | `/api/v1/admin/subjects/{id}/topics` | — | missing |
| GET | `/api/v1/admin/students` | — | missing |
| POST | `/api/v1/admin/students` | — | missing |
| GET | `/api/v1/admin/students/{id}` | — | missing |
| PUT | `/api/v1/admin/students/{id}` | — | missing |
| DELETE | `/api/v1/admin/students/{id}` | — | missing |
| POST | `/api/v1/admin/students/import` | — | missing |
| POST | `/api/v1/admin/students/import/{importId}/confirm` | — | missing |
| GET | `/api/v1/admin/students/import/template` | — | missing |
| GET | `/api/v1/admin/assignments?teacherId={id}` | — | missing |
| POST | `/api/v1/admin/assignments` | — | missing |
| DELETE | `/api/v1/admin/assignments/{id}` | — | missing |
| GET | `/api/v1/admin/settings` | — | missing |
| PUT | `/api/v1/admin/settings` | — | missing |
| POST | `/api/v1/admin/settings/logo` | — | missing |
| GET | `/api/v1/admin/audit` | — | missing |
| GET | `/api/v1/teacher/classes` | — | missing |
| GET | `/api/v1/teacher/classes/{classId}/streams/{streamId}/students` | — | missing |
| GET | `/api/v1/teacher/assessments` | — | missing |
| POST | `/api/v1/teacher/assessments` | — | missing |
| GET | `/api/v1/teacher/assessments/{id}` | — | missing |
| PUT | `/api/v1/teacher/assessments/{id}` | — | missing |
| DELETE | `/api/v1/teacher/assessments/{id}` | — | missing |
| POST | `/api/v1/teacher/assessments/{id}/publish` | — | missing |
| GET | `/api/v1/teacher/assessments/{id}/grid` | — | missing |
| PUT | `/api/v1/teacher/scores/{scoreId}` | — | missing |
| POST | `/api/v1/teacher/assessments/{id}/scores/batch` | — | missing |
| GET | `/api/v1/teacher/students/{studentId}/comments?termId={id}` | — | missing |
| POST | `/api/v1/teacher/comments` | — | missing |
| DELETE | `/api/v1/teacher/comments/{id}` | — | missing |
| POST | `/api/v1/reports/generate` | — | missing |
| GET | `/api/v1/reports/student/{studentId}?termId={id}` | — | missing |
| GET | `/api/v1/reports/download/{reportId}` | — | missing |
| GET | `/api/v1/student/me` | — | missing |
| GET | `/api/v1/student/marks` | — | missing |
| GET | `/api/v1/student/results` | — | missing |
| GET | `/api/v1/student/reports` | — | missing |
| GET | `/api/v1/student/profile` | — | missing |
| POST | `/api/v1/student/profile/change-password` | — | missing |

## 6. Tests

There are **18 `@Test` methods** in backend test sources. Tests were not run; pass/fail is **unknown**. No desktop test sources were found.

| Test class | Tests | Type |
|---|---:|---|
| `AuthServiceTest` | 7 | Unit (Mockito) |
| `JwtTokenProviderTest` | 10 | Unit |
| `MusomiManagerApplicationTests` | 1 | Spring context integration |
| `AbstractIntegrationTest` | 0 | Abstract Testcontainers integration base |

Covered: authentication success/failure, inactive and locked accounts, failed-attempt lockout/reset, and JWT claims, validation, tampering, garbage and expiry. The single context test covers application context startup. No HTTP endpoint, filter/config authorization, database/repository, migration, or desktop behavior tests were found.

## 7. Doc Drift

- **README.md:** begins with the literal shell command `cat > README.md << 'EOF'` and describes Spring Boot 3.2; the file does not end with a closing `EOF`, and `backend\pom.xml` specifies 4.1.1.
- **MASTER.md §2:** specifies Spring Boot 3.2.x, but `backend\pom.xml` resolves 4.1.1. It also lists HTMX/Tailwind, PDF and Excel stack items for which the corresponding dependencies/assets are absent; see §2.
- **MASTER.md §6:** lists `web\`, `scripts\`, `shared\`, and `Makefile`; none exist at repository root. `infra\` exists but contains only `.gitkeep`.
- **MASTER.md §14:** references `docs\04-team\DONE.md`, which is absent from the recursive docs file listing.
- **MASTER.md §10 / SCHEMA.md:** MASTER says audit columns include `created_by`; the documented `users` schema and V002 users migration do not include that column.
- **SCHEMA.md:** documents 25 tables and a `V099__test_data.sql` seed; only V001 and V002 migration files exist. The V002 constraint/index names also differ from the documented names.
- **API_CONTRACT.md §§5–17:** documents 63 endpoints; only login and `/me` are implemented, logout is a stub, and the other 60 are absent. Logout returns 200 with a body and clears only the current security context, rather than the documented 204/token invalidation.
- **ERROR_CODES.md:** the catalog lists 38 error codes; `ErrorCode.java` defines 9. The 29 undocumented-in-code entries are `WEAK_PASSWORD`, `CANNOT_DEACTIVATE_SELF`, `STUDENT_NOT_FOUND`, `DUPLICATE_ADMISSION_NUMBER`, `STUDENT_NOT_ENROLLED`, `STUDENT_INACTIVE`, `ASSESSMENT_NOT_FOUND`, `ASSESSMENT_ALREADY_PUBLISHED`, `ASSESSMENT_NOT_DRAFT`, `CANNOT_PUBLISH_EMPTY`, `ASSESSMENT_TOPIC_REQUIRED`, `SCORE_NOT_FOUND`, `SCORE_EXCEEDS_MAX`, `SCORE_BELOW_MIN`, `SCORE_REQUIRED`, `FEEDBACK_TOO_LONG`, `NOT_YOUR_CLASS`, `NOT_YOUR_SUBJECT`, `ADMIN_ONLY`, `INVALID_IMPORT_FILE`, `IMPORT_VALIDATION_FAILED`, `IMPORT_DUPLICATE_ROWS`, `IMPORT_MISSING_COLUMNS`, `REPORT_NOT_FOUND`, `REPORT_ALREADY_GENERATED`, `NO_MARKS_PUBLISHED`, `SERVICE_UNAVAILABLE`, `FILE_TOO_LARGE`, and `UNSUPPORTED_FILE_TYPE`.
- **BACKEND.md:** describes web controllers/session auth, audit logging, Excel/PDF, and tests for the backend scope. The backend has no web controller or templates, configures stateless security, and has no audit, Excel-import, or PDF implementation.
- **Desktop source:** 76 Java files are present, but 53 are three lines or fewer; sampled import, mark-entry and settings controllers are empty class declarations. Their corresponding FXML screens exist, which is not evidence of working features.

## 8. Blockers

- Backend production sources compile successfully with `backend\mvnw.cmd -q -DskipTests compile`; no compile blocker was observed. Desktop compilation and the test suite were not run, so their build status is unknown.
- Most v1 features lack their database tables, backend entities/services/controllers, or all of those. The 23 missing migrations listed in §4 block persistence for most expected product workflows.
- Multi-school login is not wired: `AuthService` queries school ID `1L` rather than deriving the school from an authenticated context.
- Reports and Excel import lack their documented libraries and implementation. Student web pages lack controllers/templates and the documented endpoints.
- The branch is two commits behind `origin/main`; integration/conflict status is unknown.
- Pilot-school agreement, deployment and actual school usage are unknown from the files inspected.

## 9. v1 Scope Progress

Ratings reflect implementation evidence in the backend plus desktop files inspected; a screen or empty controller alone does not count as a working feature. The rough percentage assigns half credit to each partial item and no credit to items not started.

| MASTER.md §4 item | Status | Evidence |
|---|---|---|
| Login (ADMIN, TEACHER, STUDENT roles) | partial | Login API, JWT and role enum exist; lookup is fixed to school ID 1 and role-specific workflow is not implemented. |
| School structure (years, terms, classes, streams, subjects, topics) | partial | School entity/migration exist; the listed structure tables and endpoints do not. |
| Student records with Excel import | not started | No backend student entity/table/API; desktop import controller is empty. |
| Teacher assignments | not started | No backend assignment entity/table/API. |
| Assessment creation with topic tagging | not started | No assessment/topic entities, migrations or endpoints. |
| Mark entry grid (keyboard-driven, auto-save) | not started | No backend score workflow; desktop mark-entry controller is empty. |
| Draft/Published workflow | not started | No backend assessment/publish implementation. |
| Subject and class teacher comments | not started | No comments table, entity or endpoint. |
| Term report card PDF | not started | No report implementation or PDF dependency. |
| Student portal (web) to view marks and download reports | not started | No backend web controller/templates or student endpoints. |
| Audit log for key actions | not started | No audit table, entity or service. |
| School settings (name, logo, grading scale) | not started | No settings table/API; desktop settings controller is empty. |

**Rough progress: 8%** (two partial items at half credit out of 12; no item is complete).

## 10. Recommended Next 5 Tasks

1. **Sync the feature branch with `origin/main`.** It is two commits behind; review the incoming changes before continuing.
2. **Resolve the Spring Boot baseline mismatch.** Agree whether to keep the actual 4.1.1 dependency line or align to MASTER's 3.2.x, then update the source of truth.
3. **Add and verify the missing schema migrations.** The 23-table gap blocks storage and schema validation for most v1 flows; add migrations incrementally and verify against PostgreSQL.
4. **Make authentication tenant-aware and enforce access rules.** Remove the hardcoded school ID, require authentication for `/me`, implement role/resource checks, and define real logout/token invalidation.
5. **Build the core v1 workflow in dependency order.** Implement school structure, students/import and teacher assignments, then topic-tagged assessments, score entry and publish; add endpoint/integration tests as each flow lands.
