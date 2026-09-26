# STYLE_GUIDE.md — Shared Document

**Naming, formatting, and code style rules for every layer of Musomi Manager.**

---

## 1. How to Use This Document

- **All developers:** follow these rules in every file you write.
- **Reviewers:** use this as a checklist when reviewing PRs.
- **Never change a convention without team agreement.**
- **If code and this document disagree, this document wins** — until both are updated.

This document complements the specialist docs:
- `BACKEND.md` — backend code conventions
- `DESKTOP.md` — JavaFX conventions
- `WEB.md` — Thymeleaf/HTML conventions
- `DESIGN_SYSTEM.md` — visual design rules

---

## 2. General Principles

1. **Readability first.** Code is read far more than it is written.
2. **Consistency above cleverness.** Same pattern everywhere.
3. **Explicit over implicit.** No magic, no hidden side effects.
4. **Small, focused units.** Methods do one thing.
5. **No surprises.** Follow the conventions in this document.

---

## 3. Java Naming Conventions

| Thing | Convention | Example |
|-------|------------|---------|
| Package | lowercase, dot-separated | `com.musomi.manager.service` |
| Class / Interface | PascalCase | `StudentService`, `MarkRepository` |
| Enum | PascalCase | `AssessmentStatus` |
| Enum constant | UPPER_SNAKE_CASE | `DRAFT`, `PUBLISHED` |
| Method | camelCase, verb-first | `findByAdmissionNumber()`, `updateScore()` |
| Field | camelCase | `admissionNumber`, `maxScore` |
| Constant | UPPER_SNAKE_CASE | `MAX_SCORE`, `DEFAULT_PAGE_SIZE` |
| Boolean | `isX` / `hasX` / `canX` | `isActive`, `hasGuardian`, `canPublish` |
| Generic type | Single uppercase letter | `T`, `R`, `ID` |
| Test method | `should<Behavior>When<Condition>` | `shouldThrowWhenScoreExceedsMax()` |

### Package structure
Follow the exact structure in `BACKEND.md`. Do not invent new packages without agreement.

### DTOs
- Request DTOs: `CreateStudentRequest`, `UpdateStudentRequest`, `SaveMarksRequest`
- Response DTOs: `StudentResponse`, `ScoreResponse`, `ApiResponse<T>`
- Use Java `record` for DTOs.

### Exceptions
- Custom exceptions end with `Exception`: `ResourceNotFoundException`, `ValidationException`
- Error codes are in `ErrorCode` enum, UPPER_SNAKE_CASE.

---

## 4. Java Formatting

- **Indentation:** 4 spaces, no tabs.
- **Line length:** 120 characters max.
- **Braces:** K&R style (opening brace on same line).
- **Blank lines:** one between methods, two between top-level classes.
- **Imports:** no wildcard imports. Order: java, javax, then third-party, then project.
- **Annotations:** each on its own line.
- **Lombok:** use `@RequiredArgsConstructor` for constructor injection, `@Slf4j` for logging, `@Data` on entities, `@Builder` where helpful.
- **Records:** prefer records for DTOs and value objects.

### Example
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MarkService {

    private final ScoreRepository scoreRepository;
    private final AuditService auditService;

    @Transactional
    public ScoreResponse updateScore(Long scoreId, SaveMarksRequest request, Long currentUserId) {
        Score score = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SCORE_NOT_FOUND));

        // permission check
        if (!score.getAssessment().getTeacher().getId().equals(currentUserId)) {
            throw new PermissionDeniedException(ErrorCode.NOT_YOUR_CLASS);
        }

        // validation
        if (request.score().compareTo(score.getAssessment().getMaxScore()) > 0) {
            throw new ValidationException(ErrorCode.SCORE_EXCEEDS_MAX,
                    Map.of("max", score.getAssessment().getMaxScore(), "entered", request.score()));
        }

        score.setScore(request.score());
        score.setFeedback(request.feedback());
        score.setUpdatedAt(LocalDateTime.now());

        Score saved = scoreRepository.save(score);
        auditService.log(AuditAction.SCORE_UPDATED, "Score", scoreId, currentUserId);

        return ScoreMapper.toResponse(saved);
    }
}