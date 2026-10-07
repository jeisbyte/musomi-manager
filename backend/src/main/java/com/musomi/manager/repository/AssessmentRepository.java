package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByTeacherIdAndTermIdOrderByAssessmentDateDesc(Long teacherId, Long termId);

    List<Assessment> findByClassEntityIdAndSubjectIdAndTermId(Long classId, Long subjectId, Long termId);

    List<Assessment> findByTeacherIdAndStatus(Long teacherId, String status);

    boolean existsByTeacherIdAndClassEntityIdAndSubjectIdAndTitle(
            Long teacherId, Long classId, Long subjectId, String title);
}
