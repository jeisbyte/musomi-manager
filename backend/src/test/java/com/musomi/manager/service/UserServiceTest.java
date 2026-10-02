package com.musomi.manager.service;

import com.musomi.manager.dto.request.CreateUserRequest;
import com.musomi.manager.dto.request.UpdateUserRequest;
import com.musomi.manager.dto.response.ResetPasswordResponse;
import com.musomi.manager.dto.response.UserResponse;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("should return user when found")
    void shouldReturnUserWhenFound() {
        User user = createUser();
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(42L);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.username()).isEqualTo("jane");
        assertThat(response.fullName()).isEqualTo("Jane Doe");
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("should create user when username available")
    void shouldCreateUserWhenUsernameAvailable() {
        CreateUserRequest request = new CreateUserRequest(
                "newuser", "password123", "New User",
                "new@school.ug", "+256700000001", Role.TEACHER);
        when(userRepository.existsBySchoolIdAndUsername(1L, "newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });

        UserResponse response = userService.createUser(1L, request);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.role()).isEqualTo("TEACHER");
        assertThat(response.isActive()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should throw when username already taken")
    void shouldThrowWhenUsernameAlreadyTaken() {
        CreateUserRequest request = new CreateUserRequest(
                "existing", "password123", "Existing",
                null, null, Role.TEACHER);
        when(userRepository.existsBySchoolIdAndUsername(1L, "existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(1L, request))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_TAKEN);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should hash password when creating user")
    void shouldHashPasswordWhenCreatingUser() {
        CreateUserRequest request = new CreateUserRequest(
                "hashme", "plaintext-pw", "Hash Me",
                null, null, Role.TEACHER);
        when(userRepository.existsBySchoolIdAndUsername(1L, "hashme")).thenReturn(false);
        when(passwordEncoder.encode("plaintext-pw")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.createUser(1L, request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("plaintext-pw");
    }

    @Test
    @DisplayName("should throw when deactivating self")
    void shouldThrowWhenDeactivatingSelf() {
        assertThatThrownBy(() -> userService.deactivateUser(42L, 42L))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_DEACTIVATE_SELF);

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("should set inactive when deactivating other")
    void shouldSetInactiveWhenDeactivatingOther() {
        User target = createUser();
        target.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.deactivateUser(7L, 42L);

        assertThat(target.getIsActive()).isFalse();
        verify(userRepository).save(target);
    }

    @Test
    @DisplayName("should return plaintext when resetting password")
    void shouldReturnPlaintextWhenResettingPassword() {
        User target = createUser();
        when(userRepository.findById(42L)).thenReturn(Optional.of(target));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-temp");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResetPasswordResponse response = userService.resetPassword(42L);

        assertThat(response.temporaryPassword()).isNotNull();
        assertThat(response.temporaryPassword()).hasSize(10);
        assertThat(response.temporaryPassword()).matches("[A-Za-z0-9]{10}");
        assertThat(target.getPasswordHash()).isEqualTo("hashed-temp");
        verify(userRepository).save(target);
    }

    private User createUser() {
        return User.builder()
                .id(42L)
                .school(School.builder().id(1L).name("Musomi School").build())
                .username("jane")
                .passwordHash("hashed-password")
                .fullName("Jane Doe")
                .role(Role.ADMIN)
                .isActive(true)
                .failedLoginAttempts(0)
                .build();
    }
}