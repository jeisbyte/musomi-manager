package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.response.LoginHistoryResponse;
import com.musomi.manager.entity.LoginHistory;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.mapper.LoginHistoryMapper;
import com.musomi.manager.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages login history records. */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {

    private static final Long DEFAULT_SCHOOL_ID = 1L;

    private final LoginHistoryRepository loginHistoryRepository;

    /** Lists login attempts for a user. */
    @Transactional(readOnly = true)
    public List<LoginHistoryResponse> listByUser(Long userId) {
        return LoginHistoryMapper.toResponseList(loginHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /** Lists login attempts for a school. */
    @Transactional(readOnly = true)
    public List<LoginHistoryResponse> listBySchool(Long schoolId) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        return LoginHistoryMapper.toResponseList(
                loginHistoryRepository.findBySchoolIdOrderByCreatedAtDesc(resolvedSchoolId));
    }

    /** Records a login attempt, retaining unknown usernames without a user association. */
    @Transactional
    public void record(String usernameAttempted, Long userId, boolean success, String ip, String userAgent) {
        User user = userId != null ? User.builder().id(userId).build() : null;
        LoginHistory history = LoginHistory.builder()
                .school(School.builder().id(DEFAULT_SCHOOL_ID).build())
                .user(user)
                .usernameAttempted(usernameAttempted)
                .success(success)
                .ipAddress(ip)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();
        loginHistoryRepository.save(history);
        log.info("Login attempt recorded: username={}, success={}", usernameAttempted, success);
    }
}
