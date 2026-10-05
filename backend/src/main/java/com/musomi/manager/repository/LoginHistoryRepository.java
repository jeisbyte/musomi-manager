package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persists login history records. */
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /** Lists a user's login history, newest first. */
    List<LoginHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Lists a school's login history, newest first. */
    List<LoginHistory> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);
}
