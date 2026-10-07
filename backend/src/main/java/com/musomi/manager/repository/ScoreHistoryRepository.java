package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {

    List<ScoreHistory> findByScoreIdOrderByChangedAtDesc(Long scoreId);
}
