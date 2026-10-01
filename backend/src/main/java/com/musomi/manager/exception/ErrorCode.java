package com.musomi.manager.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // === Auth ===
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,
            "Invalid username or password."),
    // TODO(backend-lead): substitute {minutes} with remaining lock time when throwing this.
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN,
            "Your account is locked. Try again in {minutes} minutes."),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN,
            "Your account has been deactivated. Contact an administrator."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED,
            "Your session has expired. Please log in again."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST,
            "Password must be at least 8 characters and include a number."),

    // === Users ===
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "User not found."),
    USERNAME_TAKEN(HttpStatus.CONFLICT,
            "This username is already taken."),
    CANNOT_DEACTIVATE_SELF(HttpStatus.UNPROCESSABLE_ENTITY,
            "You cannot deactivate your own account."),

    // === Students ===
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Student not found."),
    DUPLICATE_ADMISSION_NUMBER(HttpStatus.CONFLICT,
            "Admission number {number} already exists."),
    STUDENT_NOT_ENROLLED(HttpStatus.UNPROCESSABLE_ENTITY,
            "This student is not enrolled in the selected class."),
    STUDENT_INACTIVE(HttpStatus.UNPROCESSABLE_ENTITY,
            "This student is inactive."),

    // === Assessments ===
    ASSESSMENT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Assessment not found."),
    ASSESSMENT_ALREADY_PUBLISHED(HttpStatus.CONFLICT,
            "This assessment has been published. Unpublish it first to edit."),
    ASSESSMENT_NOT_DRAFT(HttpStatus.UNPROCESSABLE_ENTITY,
            "Only draft assessments can be deleted."),
    CANNOT_PUBLISH_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY,
            "Cannot publish: {count} students have no marks."),
    ASSESSMENT_TOPIC_REQUIRED(HttpStatus.BAD_REQUEST,
            "At least one topic must be selected."),

    // === Scores ===
    SCORE_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Score not found."),
    SCORE_EXCEEDS_MAX(HttpStatus.BAD_REQUEST,
            "Score cannot exceed {max}. You entered {entered}."),
    SCORE_BELOW_MIN(HttpStatus.BAD_REQUEST,
            "Score cannot be negative."),
    SCORE_REQUIRED(HttpStatus.BAD_REQUEST,
            "Score is required."),
    FEEDBACK_TOO_LONG(HttpStatus.BAD_REQUEST,
            "Feedback cannot exceed {max} characters. You entered {length}."),

    // === Permissions ===
    NOT_YOUR_CLASS(HttpStatus.FORBIDDEN,
            "You do not teach this class."),
    NOT_YOUR_SUBJECT(HttpStatus.FORBIDDEN,
            "You do not teach this subject."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN,
            "You do not have permission to perform this action."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN,
            "Only administrators can perform this action."),

    // === Import ===
    INVALID_IMPORT_FILE(HttpStatus.BAD_REQUEST,
            "The file could not be read. Please use the provided template."),
    IMPORT_VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY,
            "Import failed: {count} rows have errors."),
    IMPORT_DUPLICATE_ROWS(HttpStatus.UNPROCESSABLE_ENTITY,
            "{count} rows have duplicate admission numbers."),
    IMPORT_MISSING_COLUMNS(HttpStatus.BAD_REQUEST,
            "Required columns are missing: {list}."),

    // === Reports ===
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "Report not found."),
    REPORT_ALREADY_GENERATED(HttpStatus.CONFLICT,
            "A report has already been generated for this student and term."),
    NO_MARKS_PUBLISHED(HttpStatus.UNPROCESSABLE_ENTITY,
            "No published marks found for this term. Cannot generate report."),

    // === Validation ===
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST,
            "One or more fields are invalid."),

    // === System ===
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "Something went wrong. Please try again."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,
            "The service is temporarily unavailable. Please try again shortly."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE,
            "File size exceeds the {max} MB limit."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Only {types} files are supported.");

    private final HttpStatus status;
    private final String message;
}