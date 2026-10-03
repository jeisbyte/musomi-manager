# Musomi Manager

A learning record system for Ugandan secondary schools. Captures marks at topic level, generates professional report cards, and (in later versions) provides AI guidance.

**Current version:** v1 — Core Operations (login, school structure, students, assessments, marks, reports).

## Stack

- **Backend:** Java 21, Spring Boot 4.1.1, Spring Security 7, PostgreSQL 16, Flyway, JPA/Hibernate
- **Desktop:** JavaFX 21 (FXML + MaterialFX)
- **Web (student):** Thymeleaf + HTMX + Tailwind CSS
- **Build:** Maven
- **CI:** GitHub Actions

## Getting started

See [docs/master/MASTER.md](docs/master/MASTER.md) for full project context.

### Backend

```bash
cd backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"