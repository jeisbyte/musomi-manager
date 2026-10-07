package com.musomi.manager.service;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.response.AuditLogResponse;
import com.musomi.manager.entity.AuditLog;
import com.musomi.manager.entity.User;
import com.musomi.manager.repository.AuditLogRepository;
import com.musomi.manager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private static final Long SCHOOL_ID = 1L;
    private static final Long USER_ID = 2L;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    @DisplayName("should record audit with basic fields")
    void shouldRecordAuditWithBasicFields() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.log("SCORE_UPDATED", "Score", 3L, USER_ID, SCHOOL_ID);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("should record audit with details")
    void shouldRecordAuditWithDetails() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.log("SCORE_UPDATED", "Score", 3L, USER_ID, SCHOOL_ID, "{\"newScore\":80}");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("should not throw when save fails")
    void shouldNotThrowWhenSaveFails() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new IllegalStateException("database error"));

        assertThatCode(() -> auditService.log("SCORE_UPDATED", "Score", 3L, USER_ID, SCHOOL_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should list by user")
    void shouldListByUser() {
        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(auditLog()));

        List<AuditLogResponse> response = auditService.listByUser(USER_ID);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).userName()).isEqualTo("Alex Admin");
    }

    @Test
    @DisplayName("should list by school")
    void shouldListBySchool() {
        when(auditLogRepository.findBySchoolIdOrderByCreatedAtDesc(SCHOOL_ID)).thenReturn(List.of(auditLog()));

        assertThat(auditService.listBySchool(SCHOOL_ID)).hasSize(1);
    }

    @Test
    @DisplayName("should list by action")
    void shouldListByAction() {
        when(auditLogRepository.findByActionOrderByCreatedAtDesc("SCORE_UPDATED"))
                .thenReturn(List.of(auditLog()));

        assertThat(auditService.listByAction("SCORE_UPDATED")).hasSize(1);
    }

    @Test
    @DisplayName("should list by entity")
    void shouldListByEntity() {
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("Score", 3L))
                .thenReturn(List.of(auditLog()));

        assertThat(auditService.listByEntity("Score", 3L)).hasSize(1);
    }

    private AuditLog auditLog() {
        return AuditLog.builder()
                .id(4L)
                .user(user())
                .action("SCORE_UPDATED")
                .entityType("Score")
                .entityId(3L)
                .details("{\"newScore\":80}")
                .build();
    }

    private User user() {
        return User.builder().id(USER_ID).fullName("Alex Admin").build();
    }
}
