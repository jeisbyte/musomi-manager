package com.musomi.manager.service;

import com.musomi.manager.dto.request.CreateUserRequest;
import com.musomi.manager.dto.request.UpdateUserRequest;
import com.musomi.manager.dto.response.PageResult;
import com.musomi.manager.dto.response.ResetPasswordResponse;
import com.musomi.manager.dto.response.UserResponse;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.UserMapper;
import com.musomi.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    /** v1 serves a single school; keep consistent with AuthService. */
    private static final long DEFAULT_SCHOOL_ID = 1L;

    private static final String PASSWORD_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TEMP_PASSWORD_LENGTH = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Lists users for a school with optional role, active, and free-text filters,
     * paginated.
     */
    @Transactional(readOnly = true)
    public PageResult<UserResponse> listUsers(Long schoolId, Role role, Boolean active,
                                              String search, Pageable pageable) {
        Long scopedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        String normalisedSearch = (search == null || search.isBlank()) ? null : search.trim();

        Page<User> page = userRepository.findUsers(
                scopedSchoolId, role, active, normalisedSearch, pageable);

        return new PageResult<>(
                UserMapper.toResponseList(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Creates a new user. Throws {@link ValidationException} with
     * {@code USERNAME_TAKEN} if the username is already used within the school.
     */
    @Transactional
    public UserResponse createUser(Long schoolId, CreateUserRequest request) {
        Long scopedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;

        if (userRepository.existsBySchoolIdAndUsername(scopedSchoolId, request.username())) {
            throw new ValidationException(ErrorCode.USERNAME_TAKEN);
        }

        // The school is resolved by the caller context in v1 (single school).
        // We attach via school id to avoid an extra SchoolRepository dependency.
        User user = User.builder()
                .school(com.musomi.manager.entity.School.builder().id(scopedSchoolId).build())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .role(request.role())
                .isActive(true)
                .failedLoginAttempts(0)
                .build();

        User saved = userRepository.save(user);

        log.info("User created: userId={}, username={}, role={}",
                saved.getId(), saved.getUsername(), saved.getRole());

        return UserMapper.toResponse(saved);
    }

    /**
     * Returns a single user by id.
     *
     * @throws ResourceNotFoundException with {@code USER_NOT_FOUND} if missing
     */
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        return UserMapper.toResponse(user);
    }

    /**
     * Updates a user's profile fields (full name, email, phone). Password is
     * never touched here.
     *
     * @throws ResourceNotFoundException with {@code USER_NOT_FOUND} if missing
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());

        User saved = userRepository.save(user);
        log.info("User updated: userId={}", saved.getId());

        return UserMapper.toResponse(saved);
    }

    /**
     * Soft-deletes a user by setting {@code isActive = false}.
     *
     * @throws ValidationException with {@code CANNOT_DEACTIVATE_SELF} if the
     *                             caller targets themselves
     * @throws ResourceNotFoundException with {@code USER_NOT_FOUND} if missing
     */
    @Transactional
    public void deactivateUser(Long id, Long currentUserId) {
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new ValidationException(ErrorCode.CANNOT_DEACTIVATE_SELF);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated: userId={}, by={}", id, currentUserId);
    }

    /**
     * Generates a temporary password for a user, stores its hash, and returns
     * the plaintext exactly once.
     *
     * <p>The plaintext is never logged.</p>
     *
     * @throws ResourceNotFoundException with {@code USER_NOT_FOUND} if missing
     */
    @Transactional
    public ResetPasswordResponse resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        String temporaryPassword = generateTemporaryPassword();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);

        log.info("Password reset for userId={}", id);
        return new ResetPasswordResponse(temporaryPassword);
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_ALPHABET.charAt(secureRandom.nextInt(PASSWORD_ALPHABET.length())));
        }
        return sb.toString();
    }
}