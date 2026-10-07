package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.ReportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRequestRepository extends JpaRepository<ReportRequest, Long> {

    List<ReportRequest> findByStatusOrderByRequestedAtAsc(String status);

    List<ReportRequest> findByStudentIdAndTermId(Long studentId, Long termId);

    Optional<ReportRequest> findByStudentIdAndTermIdAndStatus(Long studentId, Long termId, String status);
}
