# ERROR_CODES.md — Shared Document

---

## 1. How to Use This Document

- **Backend devs:** throw exceptions with the codes defined here
- **Frontend devs:** handle the codes you receive
- **QA:** verify correct codes are returned in error cases
- **Never invent an error code** without adding it here first
- **Never change a message** without updating both the code and this file

Every error response follows the standard shape defined in `API_CONTRACT.md`:

```json
{
  "error": {
    "code": "STUDENT_NOT_FOUND",
    "message": "Student not found.",
    "details": null
  }
}
```

---

## 2. Error Code Rules

### Naming
- `UPPER_SNAKE_CASE`
- Descriptive — the code alone should tell you what happened
- Grouped by domain (auth, students, assessments, etc.)

### Messages
- Human-readable, plain English
- Start with a capital letter
- End with a period
- No exclamation marks
- No technical jargon
- No stack traces or database names
- Use `{placeholders}` for dynamic values

### Details
- Optional field for extra context
- Use for: field-level validation, extra params (max, entered, etc.)
- Always an object, never a string

### Structure

Every error entry in this document has:

- **Code** — the exact string
- **HTTP status** — the response status
- **Message** — the message returned to the client
- **Details** — optional extra data
- **When thrown** — the condition
- **How to handle (frontend)** — what the UI should do

---

## 3. Authentication Errors

### INVALID_CREDENTIALS

- **HTTP:** 401
- **Message:** "Invalid username or password."
- **Details:** null
- **When thrown:** Username doesn't exist, OR password is wrong.
- **Note:** Never reveal which one failed — prevents user enumeration.
- **Frontend:** Show message under the login form. Do not reveal whether username or password was wrong.

---

### ACCOUNT_LOCKED

- **HTTP:** 403
- **Message:** "Your account is locked. Try again in {minutes} minutes."
- **Details:** `{ "minutes": 15 }`
- **When thrown:** 5 failed login attempts within 15 minutes.
- **Frontend:** Show message with the remaining lock time. Disable the login button until time expires.

---

### ACCOUNT_INACTIVE

- **HTTP:** 403
- **Message:** "Your account has been deactivated. Contact an administrator."
- **Details:** null
- **When thrown:** User exists but `is_active = false`.
- **Frontend:** Show message. Do not allow login.

---

### SESSION_EXPIRED

- **HTTP:** 401
- **Message:** "Your session has expired. Please log in again."
- **Details:** null
- **When thrown:** JWT expired or session timed out.
- **Frontend:** Clear session, redirect to login.

---

### WEAK_PASSWORD

- **HTTP:** 400
- **Message:** "Password must be at least 8 characters and include a number."
- **Details:** `{ "requirements": ["minLength:8", "containsNumber"] }`
- **When thrown:** New password doesn't meet policy.
- **Frontend:** Show requirements inline with the password field.

---

## 4. User Errors

### USER_NOT_FOUND

- **HTTP:** 404
- **Message:** "User not found."
- **When thrown:** Requested user ID doesn't exist.
- **Frontend:** Show a "not found" page or redirect to list.

---

### USERNAME_TAKEN

- **HTTP:** 409
- **Message:** "This username is already taken."
- **Details:** `{ "username": "teacher1" }`
- **When thrown:** Creating a user with a username that exists in the same school.
- **Frontend:** Highlight the username field, show message below.

---

### CANNOT_DEACTIVATE_SELF

- **HTTP:** 422
- **Message:** "You cannot deactivate your own account."
- **When thrown:** Admin tries to deactivate themselves.
- **Frontend:** Disable the deactivate button for the current user.

---

## 5. Student Errors

### STUDENT_NOT_FOUND

- **HTTP:** 404
- **Message:** "Student not found."
- **When thrown:** Requested student ID doesn't exist.
- **Frontend:** Show a "not found" page or redirect.

---

### DUPLICATE_ADMISSION_NUMBER

- **HTTP:** 409
- **Message:** "Admission number {number} already exists."
- **Details:** `{ "number": "2024/0456" }`
- **When thrown:** Creating or importing a student with an admission number already in use in the same school.
- **Frontend:** Highlight the admission number field, show message.

---

### STUDENT_NOT_ENROLLED

- **HTTP:** 422
- **Message:** "This student is not enrolled in the selected class."
- **When thrown:** Trying to add a mark for a student not in the assessment's class.
- **Frontend:** Refresh the roster, show message.

---

### STUDENT_INACTIVE

- **HTTP:** 422
- **Message:** "This student is inactive."
- **When thrown:** Trying to operate on an inactive student.
- **Frontend:** Show message. Prevent the action.

---

## 6. Assessment Errors

### ASSESSMENT_NOT_FOUND

- **HTTP:** 404
- **Message:** "Assessment not found."
- **When thrown:** Requested assessment ID doesn't exist.
- **Frontend:** Redirect to the assessment list.

---

### ASSESSMENT_ALREADY_PUBLISHED

- **HTTP:** 409
- **Message:** "This assessment has been published. Unpublish it first to edit."
- **When thrown:** Trying to edit or delete a published assessment.
- **Frontend:** Disable edit and delete buttons after publishing. Show message if attempted.

---

### ASSESSMENT_NOT_DRAFT

- **HTTP:** 422
- **Message:** "Only draft assessments can be deleted."
- **When thrown:** Trying to delete a published assessment.
- **Frontend:** Hide the delete button for published assessments.

---

### CANNOT_PUBLISH_EMPTY

- **HTTP:** 422
- **Message:** "Cannot publish: {count} students have no marks."
- **Details:** `{ "count": 4, "studentIds": [1, 2, 3, 4] }`
- **When thrown:** Trying to publish with missing scores.
- **Frontend:** Highlight missing rows. Show count in a modal.

---

### ASSESSMENT_TOPIC_REQUIRED

- **HTTP:** 400
- **Message:** "At least one topic must be selected."
- **When thrown:** Creating or updating an assessment without topics.
- **Frontend:** Highlight the topic selector, prevent submission.

---

## 7. Score Errors

### SCORE_NOT_FOUND

- **HTTP:** 404
- **Message:** "Score not found."
- **When thrown:** Requested score ID doesn't exist.
- **Frontend:** Refresh the grid.

---

### SCORE_EXCEEDS_MAX

- **HTTP:** 400
- **Message:** "Score cannot exceed {max}. You entered {entered}."
- **Details:** `{ "max": 100, "entered": 120 }`
- **When thrown:** Score above the assessment's max.
- **Frontend:** Highlight the cell, show message via toast. Keep the old value.

---

### SCORE_BELOW_MIN

- **HTTP:** 400
- **Message:** "Score cannot be negative."
- **Details:** `{ "entered": -5 }`
- **When thrown:** Score below 0.
- **Frontend:** Highlight the cell, show message.

---

### SCORE_REQUIRED

- **HTTP:** 400
- **Message:** "Score is required."
- **When thrown:** Trying to save a score with no value.
- **Frontend:** Prevent empty submission.

---

### FEEDBACK_TOO_LONG

- **HTTP:** 400
- **Message:** "Feedback cannot exceed {max} characters. You entered {length}."
- **Details:** `{ "max": 200, "length": 245 }`
- **When thrown:** Feedback text is too long.
- **Frontend:** Show character count. Prevent submission past max.

---

## 8. Permission Errors

### NOT_YOUR_CLASS

- **HTTP:** 403
- **Message:** "You do not teach this class."
- **When thrown:** Teacher tries to access or edit a class not assigned to them.
- **Frontend:** Hide the class from their list. If attempted, show error and redirect to their classes.

---

### NOT_YOUR_SUBJECT

- **HTTP:** 403
- **Message:** "You do not teach this subject."
- **When thrown:** Teacher tries to access a subject they don't teach.
- **Frontend:** Same as NOT_YOUR_CLASS.

---

### PERMISSION_DENIED

- **HTTP:** 403
- **Message:** "You do not have permission to perform this action."
- **When thrown:** Any permission check fails without a more specific code.
- **Frontend:** Show message. Do not crash. Log for the developer.

---

### ADMIN_ONLY

- **HTTP:** 403
- **Message:** "Only administrators can perform this action."
- **When thrown:** Non-admin tries an admin-only action.
- **Frontend:** Hide admin controls for non-admins.

---

## 9. Import Errors

### INVALID_IMPORT_FILE

- **HTTP:** 400
- **Message:** "The file could not be read. Please use the provided template."
- **When thrown:** File is not a valid `.xlsx`, is corrupted, or is missing.
- **Frontend:** Show message. Offer template download link.

---

### IMPORT_VALIDATION_FAILED

- **HTTP:** 422
- **Message:** "Import failed: {count} rows have errors."
- **Details:** `{ "count": 3, "errors": [...] }`
- **When thrown:** Validation fails during import preview.
- **Frontend:** Show a preview table with errors highlighted by row. Allow fixing and re-uploading.

---

### IMPORT_DUPLICATE_ROWS

- **HTTP:** 422
- **Message:** "{count} rows have duplicate admission numbers."
- **Details:** `{ "count": 3, "duplicates": ["2024/0456", "2024/0457"] }`
- **When thrown:** Duplicates within the file or against existing data.
- **Frontend:** Highlight duplicate rows in the preview.

---

### IMPORT_MISSING_COLUMNS

- **HTTP:** 400
- **Message:** "Required columns are missing: {list}."
- **Details:** `{ "missing": ["admissionNumber", "fullName"] }`
- **When thrown:** Excel file doesn't have required columns.
- **Frontend:** Show missing columns. Offer template download.

---

## 10. Report Errors

### REPORT_NOT_FOUND

- **HTTP:** 404
- **Message:** "Report not found."
- **When thrown:** Requested report ID doesn't exist.
- **Frontend:** Redirect to the reports list.

---

### REPORT_ALREADY_GENERATED

- **HTTP:** 409
- **Message:** "A report has already been generated for this student and term."
- **When thrown:** Trying to generate an already-existing report.
- **Frontend:** Show the existing report link instead of regenerating.

---

### NO_MARKS_PUBLISHED

- **HTTP:** 422
- **Message:** "No published marks found for this term. Cannot generate report."
- **When thrown:** Generating a report before publishing any marks.
- **Frontend:** Show message. Suggest publishing marks first.

---

## 11. Validation Errors

### VALIDATION_FAILED

- **HTTP:** 400
- **Message:** "One or more fields are invalid."
- **Details:** `{ "fieldName": "error message", ... }`
- **When thrown:** Any bean validation failure (`@Valid`).
- **Frontend:** Show field-level errors below the corresponding inputs.

Example details:

```json
{
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "One or more fields are invalid.",
    "details": {
      "admissionNumber": "must not be blank",
      "dateOfBirth": "must be in the past",
      "maxScore": "must be greater than 0"
    }
  }
}
```

---

## 12. System Errors

### INTERNAL_ERROR

- **HTTP:** 500
- **Message:** "Something went wrong. Please try again."
- **Details:** null (never expose the stack trace to the client)
- **When thrown:** Any unhandled exception.
- **Note:** Log the actual exception server-side with a request ID.
- **Frontend:** Show generic message. Include the request ID if provided (future enhancement).

---

### SERVICE_UNAVAILABLE

- **HTTP:** 503
- **Message:** "The service is temporarily unavailable. Please try again shortly."
- **When thrown:** Database down, dependency failure, or maintenance.
- **Frontend:** Show message. Optionally auto-retry.

---

### FILE_TOO_LARGE

- **HTTP:** 413
- **Message:** "File size exceeds the {max} MB limit."
- **Details:** `{ "max": 2 }`
- **When thrown:** Upload exceeds size limit.
- **Frontend:** Show message. Prevent submission.

---

### UNSUPPORTED_FILE_TYPE

- **HTTP:** 415
- **Message:** "Only {types} files are supported."
- **Details:** `{ "types": [".xlsx", ".xls"] }`
- **When thrown:** Wrong file type uploaded.
- **Frontend:** Show message. Filter file picker.

---

## 13. Error Code Index

| Code | HTTP | Domain |
|---|---|---|
| INVALID_CREDENTIALS | 401 | Auth |
| ACCOUNT_LOCKED | 403 | Auth |
| ACCOUNT_INACTIVE | 403 | Auth |
| SESSION_EXPIRED | 401 | Auth |
| WEAK_PASSWORD | 400 | Auth |
| USER_NOT_FOUND | 404 | Users |
| USERNAME_TAKEN | 409 | Users |
| CANNOT_DEACTIVATE_SELF | 422 | Users |
| STUDENT_NOT_FOUND | 404 | Students |
| DUPLICATE_ADMISSION_NUMBER | 409 | Students |
| STUDENT_NOT_ENROLLED | 422 | Students |
| STUDENT_INACTIVE | 422 | Students |
| ASSESSMENT_NOT_FOUND | 404 | Assessments |
| ASSESSMENT_ALREADY_PUBLISHED | 409 | Assessments |
| ASSESSMENT_NOT_DRAFT | 422 | Assessments |
| CANNOT_PUBLISH_EMPTY | 422 | Assessments |
| ASSESSMENT_TOPIC_REQUIRED | 400 | Assessments |
| SCORE_NOT_FOUND | 404 | Scores |
| SCORE_EXCEEDS_MAX | 400 | Scores |
| SCORE_BELOW_MIN | 400 | Scores |
| SCORE_REQUIRED | 400 | Scores |
| FEEDBACK_TOO_LONG | 400 | Scores |
| NOT_YOUR_CLASS | 403 | Permissions |
| NOT_YOUR_SUBJECT | 403 | Permissions |
| PERMISSION_DENIED | 403 | Permissions |
| ADMIN_ONLY | 403 | Permissions |
| INVALID_IMPORT_FILE | 400 | Import |
| IMPORT_VALIDATION_FAILED | 422 | Import |
| IMPORT_DUPLICATE_ROWS | 422 | Import |
| IMPORT_MISSING_COLUMNS | 400 | Import |
| REPORT_NOT_FOUND | 404 | Reports |
| REPORT_ALREADY_GENERATED | 409 | Reports |
| NO_MARKS_PUBLISHED | 422 | Reports |
| VALIDATION_FAILED | 400 | Validation |
| INTERNAL_ERROR | 500 | System |
| SERVICE_UNAVAILABLE | 503 | System |
| FILE_TOO_LARGE | 413 | System |
| UNSUPPORTED_FILE_TYPE | 415 | System |

**Total: 38 codes.**

---

## 14. Backend Implementation

### ErrorCode enum

```java
package com.musomi.manager.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Your account is locked. Try again in {minutes} minutes."),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "Your account has been deactivated. Contact an administrator."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters and include a number."),

    // Users
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found."),
    USERNAME_TAKEN(HttpStatus.CONFLICT, "This username is already taken."),
    CANNOT_DEACTIVATE_SELF(HttpStatus.UNPROCESSABLE_ENTITY, "You cannot deactivate your own account."),

    // Students
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Student not found."),
    DUPLICATE_ADMISSION_NUMBER(HttpStatus.CONFLICT, "Admission number {number} already exists."),
    STUDENT_NOT_ENROLLED(HttpStatus.UNPROCESSABLE_ENTITY, "This student is not enrolled in the selected class."),
    STUDENT_INACTIVE(HttpStatus.UNPROCESSABLE_ENTITY, "This student is inactive."),

    // Assessments
    ASSESSMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Assessment not found."),
    ASSESSMENT_ALREADY_PUBLISHED(HttpStatus.CONFLICT, "This assessment has been published. Unpublish it first to edit."),
    ASSESSMENT_NOT_DRAFT(HttpStatus.UNPROCESSABLE_ENTITY, "Only draft assessments can be deleted."),
    CANNOT_PUBLISH_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot publish: {count} students have no marks."),
    ASSESSMENT_TOPIC_REQUIRED(HttpStatus.BAD_REQUEST, "At least one topic must be selected."),

    // Scores
    SCORE_NOT_FOUND(HttpStatus.NOT_FOUND, "Score not found."),
    SCORE_EXCEEDS_MAX(HttpStatus.BAD_REQUEST, "Score cannot exceed {max}. You entered {entered}."),
    SCORE_BELOW_MIN(HttpStatus.BAD_REQUEST, "Score cannot be negative."),
    SCORE_REQUIRED(HttpStatus.BAD_REQUEST, "Score is required."),
    FEEDBACK_TOO_LONG(HttpStatus.BAD_REQUEST, "Feedback cannot exceed {max} characters. You entered {length}."),

    // Permissions
    NOT_YOUR_CLASS(HttpStatus.FORBIDDEN, "You do not teach this class."),
    NOT_YOUR_SUBJECT(HttpStatus.FORBIDDEN, "You do not teach this subject."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "Only administrators can perform this action."),

    // Import
    INVALID_IMPORT_FILE(HttpStatus.BAD_REQUEST, "The file could not be read. Please use the provided template."),
    IMPORT_VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "Import failed: {count} rows have errors."),
    IMPORT_DUPLICATE_ROWS(HttpStatus.UNPROCESSABLE_ENTITY, "{count} rows have duplicate admission numbers."),
    IMPORT_MISSING_COLUMNS(HttpStatus.BAD_REQUEST, "Required columns are missing: {list}."),

    // Reports
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "Report not found."),
    REPORT_ALREADY_GENERATED(HttpStatus.CONFLICT, "A report has already been generated for this student and term."),
    NO_MARKS_PUBLISHED(HttpStatus.UNPROCESSABLE_ENTITY, "No published marks found for this term. Cannot generate report."),

    // Validation
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "One or more fields are invalid."),

    // System
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "The service is temporarily unavailable. Please try again shortly."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the {max} MB limit."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only {types} files are supported.");

    private final HttpStatus status;
    private final String message;
}
```

### Custom exceptions

Each exception carries an `ErrorCode` and optional details.

```java
public class ValidationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public ValidationException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ValidationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
    // getters
}
```

Repeat for: `ResourceNotFoundException`, `PermissionDeniedException`, `BusinessException`, `AuthenticationException`, `ImportException`.

### Global exception handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(ValidationException ex) {
        return build(ex.getErrorCode(), ex.getDetails());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex) {
        return build(ex.getErrorCode(), null);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handlePermission(PermissionDeniedException ex) {
        return build(ex.getErrorCode(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(ErrorCode.INTERNAL_ERROR, null);
    }

    private ResponseEntity<ApiResponse<?>> build(ErrorCode code, Map<String, Object> details) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.error(code, details));
    }
}
```

---

## 15. Frontend Handling

### Desktop app (JavaFX)

```java
try {
    markService.saveScore(row);
} catch (ApiException ex) {
    switch (ex.getCode()) {
        case "SCORE_EXCEEDS_MAX" -> {
            BigDecimal max = (BigDecimal) ex.getDetails().get("max");
            Toast.error("Score cannot exceed " + max);
        }
        case "NOT_YOUR_CLASS" -> {
            Toast.error("You do not teach this class");
            Navigator.back();
        }
        case "SESSION_EXPIRED" -> {
            Session.clear();
            Navigator.toLogin();
        }
        default -> Toast.error(ex.getMessage());
    }
}
```

### Web app (Thymeleaf)

Server-side exceptions render error pages:

- `403` → `errors/access-denied.html`
- `404` → `errors/404.html`
- `500` → `errors/500.html`

For HTMX requests, return an error fragment with the message:

```html
<div class="bg-red-50 text-red-700 p-4 rounded-lg">
    <span th:text="${error.message}">Error message</span>
</div>
```

---

## 16. Logging Rules

- **Never log** the error message sent to the client as your only log
- Always log the underlying exception with context
- Include: user ID, request ID, entity ID, action
- Use `log.error` for 500s, `log.warn` for 4xx that indicates misuse

```java
log.warn("Permission denied: user {} tried to access assessment {}",
        userId, assessmentId);
log.error("Failed to save score {}: {}", scoreId, ex.getMessage(), ex);
```

---

## 17. What NOT to Do

- Don't return raw exception messages to the client
- Don't include stack traces in API responses
- Don't reveal whether a username exists (use `INVALID_CREDENTIALS` for both cases)
- Don't invent error codes without adding them to this document
- Don't use the same code for different meanings
- Don't use HTTP 200 with an error body — use the correct status code
- Don't use lowercase or camelCase codes — always `UPPER_SNAKE_CASE`
- Don't skip the `details` field when it would help the user
- Don't leak database column names or internal IDs in messages
- Don't use exclamation marks
- Don't blame the user ("You did something wrong")

---

## The One-Sentence Summary

**ERROR_CODES.md is the complete catalog of every error in Musomi Manager — code, HTTP status, message, details, and how to handle. Backend throws these codes. Frontend handles them by code, never by message. Nothing ships without a proper code.**

---

**That's ERROR_CODES.md.**

**All five shared documents are now written:**

1. **API_CONTRACT.md** ✅
2. **SCHEMA.md** ✅
3. **DESIGN_SYSTEM.md** ✅
4. **STYLE_GUIDE.md** ✅
5. **ERROR_CODES.md** ✅

**All six specialist documents are also done:**

1. **BACKEND.md** ✅
2. **DESKTOP.md** ✅
3. **WEB.md** ✅
4. **DATABASE.md** ✅
5. **DEVOPS.md** ✅
6. **PRODUCT-QA.md** ✅

**Plus the foundation:**

- **MASTER.md** ✅

---

## What You Have Now

A complete AI documentation kit:

```
docs/
├── master/
│   └── MASTER.md                    ← every member pastes this
│
├── specialists/
│   ├── BACKEND.md
│   ├── DESKTOP.md
│   ├── WEB.md
│   ├── DATABASE.md
│   ├── DEVOPS.md
│   └── PRODUCT-QA.md
│
└── shared/
    ├── API_CONTRACT.md
    ├── SCHEMA.md
    ├── DESIGN_SYSTEM.md
    ├── STYLE_GUIDE.md
    └── ERROR_CODES.md
```

**12 documents total.**

Each team member pastes:

- MASTER.md (always)
- Their specialist document (always)
- The relevant shared section (when needed)

Then types their specific prompt.

---

## What Comes Next

You now have everything for AI-assisted development. The remaining missing pieces are:

| Document | Purpose |
|---|---|
| **SPRINT_PLAN.md** | Week-by-week tasks for v1 |
| **DONE.md** | Definition of done per task type |
| **V099__test_data.sql** | Seed data for local development |
| **TEAM_WORKFLOW.md** | Git rules, PR process, standups |
| **Operations docs** | Deployment, backup, school onboarding |
| **First school playbook** | How to find and onboard a pilot |


