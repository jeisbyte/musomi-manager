package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findByAssessmentId(Long assessmentId);

    Optional<Score> findByAssessmentIdAndStudentId(Long assessmentId, Long studentId);

    List<Score> findByStudentId(Long studentId);

    long countByAssessmentIdAndScoreIsNull(Long assessmentId);
}
