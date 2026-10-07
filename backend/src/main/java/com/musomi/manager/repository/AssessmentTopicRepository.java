package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.AssessmentTopic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentTopicRepository extends JpaRepository<AssessmentTopic, Long> {

    List<AssessmentTopic> findByAssessmentId(Long assessmentId);

    void deleteByAssessmentId(Long assessmentId);

    boolean existsByAssessmentIdAndTopicId(Long assessmentId, Long topicId);
}
