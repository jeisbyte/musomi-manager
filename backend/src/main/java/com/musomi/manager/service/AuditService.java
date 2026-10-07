package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.response.AuditLogResponse;
import com.musomi.manager.entity.AuditLog;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.mapper.AuditLogMapper;
import com.musomi.manager.repository.AuditLogRepository;
import com.musomi.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Records and retrieves audit events. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /** Records an audit event without additional details. */
    public void log(String action, String entityType, Long entityId, Long userId, Long schoolId) {
        log(action, entityType, entityId, userId, schoolId, null);
    }

    /** Records an audit event with JSON details. */
    public void log(String action, String entityType, Long entityId, Long userId, Long schoolId,
                    String detailsJson) {
        try {
            User user = userId == null ? null : userRepository.findById(userId).orElse(null);
            AuditLog auditLog = AuditLog.builder()
                    .school(School.builder().id(schoolId).build())
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(detailsJson)
                    .ipAddress(null)
                    .userAgent(null)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
            log.info("Audit event recorded: action={}, entityType={}, entityId={}", action, entityType, entityId);
        } catch (RuntimeException exception) {
            log.warn("Failed to record audit event: action={}, entityType={}, entityId={}",
                    action, entityType, entityId, exception);
        }
    }

    /** Lists audit events for a user. */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> listByUser(Long userId) {
        return AuditLogMapper.toResponseList(auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /** Lists audit events for a school. */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> listBySchool(Long schoolId) {
        return AuditLogMapper.toResponseList(auditLogRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId));
    }

    /** Lists audit events for an action. */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> listByAction(String action) {
        return AuditLogMapper.toResponseList(auditLogRepository.findByActionOrderByCreatedAtDesc(action));
    }

    /** Lists audit events for an entity. */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> listByEntity(String entityType, Long entityId) {
        return AuditLogMapper.toResponseList(
                auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId));
    }
}
