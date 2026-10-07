package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.response.LoginHistoryResponse;
import com.musomi.manager.entity.LoginHistory;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.repository.LoginHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginHistoryServiceTest {

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @InjectMocks
    private LoginHistoryService loginHistoryService;

    @Test
    @DisplayName("should list login history by user")
    void shouldListByUser() {
        when(loginHistoryRepository.findByUserIdOrderByCreatedAtDesc(42L))
                .thenReturn(List.of(history(true, user())));

        List<LoginHistoryResponse> result = loginHistoryService.listByUser(42L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(42L);
        assertThat(result.get(0).userName()).isEqualTo("Jamie User");
    }

    @Test
    @DisplayName("should list login history by school")
    void shouldListBySchool() {
        when(loginHistoryRepository.findBySchoolIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(history(false, null)));

        List<LoginHistoryResponse> result = loginHistoryService.listBySchool(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).usernameAttempted()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("should record successful login")
    void shouldRecordSuccess() {
        loginHistoryService.record("jamie", 42L, true, "127.0.0.1", "browser");

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getUsernameAttempted()).isEqualTo("jamie");
        assertThat(captor.getValue().getUser().getId()).isEqualTo(42L);
        assertThat(captor.getValue().getSuccess()).isTrue();
        assertThat(captor.getValue().getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should record failed login without user")
    void shouldRecordFailureWithoutUser() {
        loginHistoryService.record("unknown", null, false, null, null);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getUsernameAttempted()).isEqualTo("unknown");
        assertThat(captor.getValue().getUser()).isNull();
        assertThat(captor.getValue().getSuccess()).isFalse();
    }

    private LoginHistory history(boolean success, User user) {
        return LoginHistory.builder()
                .id(1L)
                .school(School.builder().id(1L).build())
                .user(user)
                .usernameAttempted(user != null ? user.getUsername() : "unknown")
                .success(success)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private User user() {
        return User.builder().id(42L).username("jamie").fullName("Jamie User").build();
    }
}
