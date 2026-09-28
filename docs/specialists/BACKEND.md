# BACKEND.md — Backend Lead Specialist Document


---

## 1. Who This Is For

You are the **Backend Lead** on Musomi Manager.

You build the brain of the system — all server-side Java code.

You work alongside:
- **JavaFX Developer** — uses your REST API
- **Web Developer** — uses your services and HTML controllers
- **Database + DevOps** — owns schema and migrations
- **Product + QA** — tests your output

You own the backend. You don't edit the desktop app or web templates.

---

## 2. What You Build in v1

- REST API for the desktop app (`/api/v1/**`)
- HTML controllers for the student web (`/login`, `/student/**`)
- Service layer (business logic)
- Repository layer (database access via Spring Data JPA)
- JPA entities (map Java classes to tables)
- DTOs (request/response shapes)
- Mappers (entity ↔ DTO)
- Security (JWT for API, session for web, BCrypt passwords)
- Exception handling
- Audit logging
- Excel import/export (Apache POI)
- PDF generation (OpenPDF or iText)
- Tests for all of the above

**Not in v1:** AI, analytics, notifications, sharing, offline, multi-school. Those are v2–v6.

---

## 3. Package Structure

```
backend/src/main/java/com/musomi/manager/
│
├── MusomiManagerApplication.java
│
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── WebConfig.java
│   ├── ThymeleafConfig.java
│   ├── JacksonConfig.java
│   └── OpenApiConfig.java
│
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java
│   ├── RateLimiter.java
│   └── CurrentUser.java
│
├── controller/
│   ├── api/                          ← REST for desktop
│   │   ├── AuthApiController.java
│   │   ├── AdminUserApiController.java
│   │   ├── AdminSchoolApiController.java
│   │   ├── AdminStudentApiController.java
│   │   ├── AdminAssignmentApiController.java
│   │   ├── TeacherAssessmentApiController.java
│   │   ├── TeacherMarkApiController.java
│   │   ├── TeacherCommentApiController.java
│   │   ├── ReportApiController.java
│   │   └── AuditApiController.java
│   │
│   └── web/                          ← HTML for students
│       ├── LoginWebController.java
│       ├── StudentDashboardWebController.java
│       ├── StudentMarksWebController.java
│       ├── StudentResultsWebController.java
│       ├── StudentReportsWebController.java
│       ├── StudentProfileWebController.java
│       └── ErrorWebController.java
│
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── StudentService.java
│   ├── AcademicYearService.java
│   ├── TermService.java
│   ├── ClassService.java
│   ├── StreamService.java
│   ├── SubjectService.java
│   ├── TopicService.java
│   ├── TeacherAssignmentService.java
│   ├── AssessmentService.java
│   ├── MarkService.java
│   ├── CommentService.java
│   ├── ReportService.java
│   ├── PdfService.java
│   ├── ExcelService.java
│   ├── AuditService.java
│   ├── GradeCalculator.java
│   └── PositionCalculator.java
│
├── repository/
│   ├── UserRepository.java
│   ├── UserSessionRepository.java
│   ├── LoginHistoryRepository.java
│   ├── StudentRepository.java
│   ├── GuardianRepository.java
│   ├── AcademicYearRepository.java
│   ├── TermRepository.java
│   ├── ClassLevelRepository.java
│   ├── ClassRepository.java
│   ├── StreamRepository.java
│   ├── SubjectRepository.java
│   ├── TopicRepository.java
│   ├── TeacherAssignmentRepository.java
│   ├── AssessmentRepository.java
│   ├── AssessmentTopicRepository.java
│   ├── ScoreRepository.java
│   ├── ScoreHistoryRepository.java
│   ├── CommentRepository.java
│   ├── ReportRequestRepository.java
│   ├── GeneratedReportRepository.java
│   ├── AuditLogRepository.java
│   ├── SchoolSettingsRepository.java
│   ├── SchoolRepository.java
│   └── ImportJobRepository.java
│
├── entity/
│   ├── School.java
│   ├── AcademicYear.java
│   ├── Term.java
│   ├── ClassLevel.java
│   ├── ClassEntity.java              ← "Class" is reserved in Java
│   ├── Stream.java
│   ├── User.java
│   ├── UserSession.java
│   ├── LoginHistory.java
│   ├── Student.java
│   ├── Guardian.java
│   ├── Subject.java
│   ├── ClassSubject.java
│   ├── Topic.java
│   ├── TeacherAssignment.java
│   ├── Assessment.java
│   ├── AssessmentTopic.java
│   ├── Score.java
│   ├── ScoreHistory.java
│   ├── Comment.java
│   ├── ReportRequest.java
│   ├── GeneratedReport.java
│   ├── AuditLog.java
│   ├── SchoolSettings.java
│   ├── ImportJob.java
│   └── enums/
│       ├── Role.java
│       ├── AssessmentType.java
│       ├── AssessmentStatus.java
│       ├── CommentType.java
│       ├── StudentStatus.java
│       ├── Gender.java
│       └── AuditAction.java
│
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── ChangePasswordRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── UpdateUserRequest.java
│   │   ├── CreateStudentRequest.java
│   │   ├── UpdateStudentRequest.java
│   │   ├── CreateAcademicYearRequest.java
│   │   ├── CreateTermRequest.java
│   │   ├── CreateClassRequest.java
│   │   ├── CreateStreamRequest.java
│   │   ├── CreateSubjectRequest.java
│   │   ├── CreateTopicRequest.java
│   │   ├── CreateAssignmentRequest.java
│   │   ├── CreateAssessmentRequest.java
│   │   ├── SaveMarksRequest.java
│   │   ├── SaveCommentRequest.java
│   │   ├── GenerateReportRequest.java
│   │   └── ImportStudentsRequest.java
│   │
│   └── response/
│       ├── LoginResponse.java
│       ├── UserResponse.java
│       ├── StudentResponse.java
│       ├── AcademicYearResponse.java
│       ├── TermResponse.java
│       ├── ClassResponse.java
│       ├── SubjectResponse.java
│       ├── TopicResponse.java
│       ├── AssignmentResponse.java
│       ├── AssessmentResponse.java
│       ├── MarkGridResponse.java
│       ├── ScoreResponse.java
│       ├── CommentResponse.java
│       ├── ReportResponse.java
│       ├── AuditLogResponse.java
│       └── ApiResponse.java
│
├── mapper/
│   ├── UserMapper.java
│   ├── StudentMapper.java
│   ├── ClassMapper.java
│   ├── SubjectMapper.java
│   ├── TopicMapper.java
│   ├── AssessmentMapper.java
│   ├── ScoreMapper.java
│   ├── CommentMapper.java
│   └── AuditMapper.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── ValidationException.java
│   ├── BusinessException.java
│   ├── PermissionDeniedException.java
│   ├── AuthenticationException.java
│   ├── ImportException.java
│   └── ErrorCode.java
│
└── util/
    ├── DateUtils.java
    ├── PasswordGenerator.java
    ├── GradeUtils.java
    ├── FileStorageService.java
    ├── SecurityUtils.java
    └── Constants.java
```

---

## 4. Layer Responsibilities

### Controllers

**REST controllers (`@RestController`)** — for the desktop app:
- Accept JSON, return JSON
- Use `@Valid` on request bodies
- Delegate to services — no business logic
- Never touch repositories directly
- JWT authentication via header

**Web controllers (`@Controller`)** — for the student portal:
- Accept HTML form data or path params
- Add data to `Model`
- Return view name (Thymeleaf)
- Session authentication via cookie
- CSRF protection auto-enabled

### Services

- All business logic lives here
- Transaction boundaries (`@Transactional`)
- Validate inputs
- Check permissions
- Write audit logs
- Throw custom exceptions
- Return entities or DTOs

### Repositories

- Spring Data JPA interfaces
- Standard CRUD inherited from `JpaRepository`
- Custom finders via method names (`findByAdmissionNumber`)
- Complex queries via `@Query`
- No business logic

### Entities

- Map to database tables
- JPA annotations (`@Entity`, `@Table`, `@Column`)
- Relationships (`@ManyToOne`, `@OneToMany`)
- No business logic
- Lombok for boilerplate (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Auditing annotations (`@CreatedDate`, `@LastModifiedDate`)

### DTOs

- Immutable where possible
- Request DTOs: validation annotations (`@NotNull`, `@Size`)
- Response DTOs: only fields the client needs
- No entities exposed directly to the API

### Mappers

- Convert entity ↔ DTO
- One mapper class per entity
- Static methods for simplicity

---

## 5. Code Conventions

### Entities

```java
@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "admission_number", nullable = false, length = 50)
    private String admissionNumber;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_class_id")
    private ClassEntity currentClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Rules:**
- `@Table` always specifies the table name explicitly
- `@Column` always specifies `name` and constraints
- Enums use `@Enumerated(EnumType.STRING)` — never ORDINAL
- Lazy loading on `@ManyToOne` — avoids N+1 by default
- `nullable` and `length` always set
- Timestamps: `created_at` is `updatable = false`

### Services

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MarkService {

    private final ScoreRepository scoreRepository;
    private final AssessmentRepository assessmentRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;
    private final AuditService auditService;
    private final GradeCalculator gradeCalculator;

    @Transactional
    public ScoreResponse updateScore(Long scoreId, SaveMarksRequest request, Long currentUserId) {
        Score score = scoreRepository.findById(scoreId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SCORE_NOT_FOUND));

        Assessment assessment = score.getAssessment();

        // Permission check
        if (!assessment.getTeacher().getId().equals(currentUserId)) {
            throw new PermissionDeniedException(ErrorCode.NOT_YOUR_CLASS);
        }

        // Validation
        if (request.getScore() < 0 || request.getScore() > assessment.getMaxScore()) {
            throw new ValidationException(ErrorCode.SCORE_EXCEEDS_MAX,
                Map.of("max", assessment.getMaxScore(), "entered", request.getScore()));
        }

        // Track history
        BigDecimal oldScore = score.getScore();
        if (oldScore != null && !oldScore.equals(request.getScore())) {
            scoreHistoryRepository.save(ScoreHistory.builder()
                .score(score)
                .oldScore(oldScore)
                .newScore(request.getScore())
                .changedBy(currentUserId)
                .changedAt(LocalDateTime.now())
                .build());
        }

        score.setScore(request.getScore());
        score.setFeedback(request.getFeedback());
        score.setUpdatedAt(LocalDateTime.now());

        Score saved = scoreRepository.save(score);

        auditService.log(AuditAction.SCORE_UPDATED, "Score", scoreId, currentUserId);

        log.info("Score {} updated by user {}", scoreId, currentUserId);

        return ScoreMapper.toResponse(saved, gradeCalculator);
    }
}
```

**Rules:**
- Constructor injection via `@RequiredArgsConstructor`
- `@Transactional` on write methods
- Permission checks before business logic
- Validation with meaningful errors
- Audit log for every state change
- Log at INFO for business events, DEBUG for detail
- Return DTOs, not entities

### Repositories

```java
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByAdmissionNumber(String admissionNumber);

    List<Student> findBySchoolIdAndStatus(Long schoolId, StudentStatus status);

    List<Student> findByCurrentClassIdAndStatus(Long classId, StudentStatus status);

    boolean existsBySchoolIdAndAdmissionNumber(Long schoolId, String admissionNumber);

    @Query("SELECT s FROM Student s WHERE s.school.id = :schoolId " +
           "AND LOWER(s.fullName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Student> searchByName(@Param("schoolId") Long schoolId,
                               @Param("query") String query);
}
```

**Rules:**
- Extend `JpaRepository<Entity, IdType>`
- Method names follow Spring Data conventions
- `@Query` for complex queries
- Always return `Optional` for single-result finders
- Always filter by `school_id` (multi-tenancy)

### Controllers

```java
@RestController
@RequestMapping("/api/v1/teacher/scores")
@RequiredArgsConstructor
public class TeacherMarkApiController {

    private final MarkService markService;

    @PutMapping("/{scoreId}")
    public ApiResponse<ScoreResponse> updateScore(
            @PathVariable Long scoreId,
            @Valid @RequestBody SaveMarksRequest request,
            @CurrentUser Long currentUserId) {

        ScoreResponse response = markService.updateScore(scoreId, request, currentUserId);
        return ApiResponse.success(response);
    }
}
```

**Rules:**
- `@RestController` for API, `@Controller` for HTML
- `@RequestMapping` at class level for base path
- `@Valid` on request bodies
- Return `ApiResponse<T>` wrapper
- Delegate everything to services
- Use `@CurrentUser` custom annotation to inject logged-in user id

### DTOs

```java
public record SaveMarksRequest(
    @NotNull @DecimalMin("0") BigDecimal score,
    @Size(max = 200) String feedback
) {}
```

**Rules:**
- Use Java records for DTOs (immutable, concise)
- Validation annotations on request DTOs
- No JPA annotations
- No business logic

### Exceptions

```java
public class ResourceNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;
    public ResourceNotFoundException(ErrorCode code) {
        super(code.getMessage());
        this.errorCode = code;
    }
    public ErrorCode getErrorCode() { return errorCode; }
}
```

Global handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getErrorCode(), ex.getDetails()));
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handlePermission(PermissionDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getErrorCode()));
    }

    // ... more handlers
}
```

---

## 6. Security

### JWT (for API)

- Token signed with HS256
- Secret from environment variable `JWT_SECRET`
- 8-hour expiry
- Claims: `userId`, `schoolId`, `role`
- Filter extracts token from `Authorization: Bearer <token>` header

### Sessions (for web)

- Spring Security session cookie (JSESSIONID)
- 8-hour session timeout
- CSRF protection auto-enabled on forms

### Passwords

- BCrypt with cost factor 10
- Stored in `users.password_hash`
- Never logged, never returned in responses

### Permission Checks

- Always server-side
- Role check + resource ownership check
- Example: teacher editing a score must own the assessment

```java
if (!assessment.getTeacher().getId().equals(currentUserId)) {
    throw new PermissionDeniedException(ErrorCode.NOT_YOUR_CLASS);
}
```

### Multi-Tenancy

- Every query filters by `school_id`
- `@CurrentUser` provides the current user's `schoolId`
- Never trust `school_id` from request body — always from session

---

## 7. Testing

### Unit tests

- JUnit 5
- Mockito for dependencies
- Test service methods in isolation
- Cover: happy path, validation, permissions, not-found

### Integration tests

- `@SpringBootTest`
- `@AutoConfigureMockMvc`
- Testcontainers for real PostgreSQL
- Test controller endpoints end-to-end

### Naming

```java
@Test
@DisplayName("should throw when score exceeds max")
void shouldThrowWhenScoreExceedsMax() { ... }
```

Pattern: `should<Behavior>When<Condition>()`

---

## 8. Excel and PDF

### Excel (Apache POI)

- Import students from `.xlsx`
- Import marks from `.xlsx`
- Export lists to `.xlsx`
- Always validate before committing
- Return preview with errors highlighted

### PDF (OpenPDF or iText)

- Report card generation
- One PDF per student per term
- Stored in `/reports/{school}/{year}/{term}/{student}.pdf`
- Uses school logo, header, footer from `school_settings`

---

## 9. Common Prompts for Backend AI

**Generate an entity:**
```
Using the conventions in BACKEND.md, generate a JPA entity for [table].
Include Lombok, JPA annotations, relationships, and enums.
Follow the naming and structure in the examples.
```

**Generate a repository:**
```
Using the conventions in BACKEND.md, generate a Spring Data JPA repository for [entity].
Include: findByAdmissionNumber, findBySchoolIdAndStatus, countByStatus.
Use @Query for complex queries.
```

**Generate a service method:**
```
Context: MASTER.md + BACKEND.md
Entity: [paste entity]
Repository: [paste repo]
API contract: [paste endpoint]

Write [methodName] in [ServiceName].
Follow the service pattern in BACKEND.md.
Include validation, permission checks, audit logging.
```

**Generate a REST controller:**
```
Context: MASTER.md + BACKEND.md
API contract: [paste endpoint]

Generate a @RestController for [path].
Follow the controller pattern in BACKEND.md.
Return ApiResponse wrapper. Use @Valid. Delegate to service.
```

**Write tests:**
```
Class under test: [paste class]
Method under test: [paste method]

Write JUnit 5 tests following BACKEND.md testing conventions.
Cover: happy path, validation, permissions, not-found.
Use Mockito for dependencies.
```

**Debug an error:**
```
Context: MASTER.md + BACKEND.md
Error: [paste stack trace]
Code: [paste relevant code]

Explain: cause, fix, prevention.
```

---

## 10. Definition of Done (Backend)

A backend task is done when:

- [ ] Code follows MASTER.md and BACKEND.md conventions
- [ ] Unit tests written and passing
- [ ] Integration test written if it crosses layers
- [ ] Error cases handled with standard error codes
- [ ] Audit log written for state changes
- [ ] Permission check enforced server-side
- [ ] Input validation via `@Valid`
- [ ] API contract updated in `API_CONTRACT.md`
- [ ] Javadoc on public service methods
- [ ] No hardcoded values (use config)
- [ ] No secrets in code
- [ ] Reviewed by another team member
- [ ] CI passes
- [ ] Merged to main

---

## 11. What NOT to Do

- Don't put business logic in controllers
- Don't expose entities directly in API responses — use DTOs
- Don't trust `school_id` from request body — use session
- Don't skip permission checks
- Don't write SQL strings manually — use JPA
- Don't use `@Enumerated(EnumType.ORDINAL)` — always STRING
- Don't log passwords, tokens, or full PII
- Don't hardcode values — use `application.yml` or environment variables
- Don't write to the database outside a `@Transactional` method
- Don't catch exceptions just to log and rethrow — let the global handler do it

---

## 12. Reference Documents

- `MASTER.md` — project context (paste first)
- `docs/shared/SCHEMA.md` — every table
- `docs/shared/API_CONTRACT.md` — every endpoint
- `docs/shared/STYLE_GUIDE.md` — naming conventions
- `docs/shared/ERROR_CODES.md` — standard errors
- `docs/specialists/DATABASE.md` — schema conventions
- `docs/specialists/DEVOPS.md` — deployment

---

## The One-Sentence Summary

**You are the Backend Lead on Musomi Manager. You build the REST API, HTML controllers, services, repositories, entities, and DTOs using Java 21 + Spring Boot 3.2 + PostgreSQL. Follow the conventions in this document. Paste MASTER.md and BACKEND.md into every AI session. Keep the backend simple, secure, tested, and audit-logged.**

