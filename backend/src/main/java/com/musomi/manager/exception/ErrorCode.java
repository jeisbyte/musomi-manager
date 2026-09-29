package com.musomi.manager.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password."),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Your account is locked. Try again later."),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "Your account has been deactivated. Contact an administrator."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found."),
    USERNAME_TAKEN(HttpStatus.CONFLICT, "This username is already taken."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "One or more fields are invalid."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");

    private final HttpStatus status;
    private final String message;
}
