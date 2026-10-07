package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {

    Optional<GeneratedReport> findByStudentIdAndTermId(Long studentId, Long termId);

    List<GeneratedReport> findByStudentIdOrderByGeneratedAtDesc(Long studentId);
}
