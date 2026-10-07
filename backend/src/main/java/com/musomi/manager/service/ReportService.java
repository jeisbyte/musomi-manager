package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.response.ReportResponse;
import com.musomi.manager.entity.GeneratedReport;
import com.musomi.manager.entity.ReportRequest;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Student;
import com.musomi.manager.entity.Term;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.GeneratedReportRepository;
import com.musomi.manager.repository.ReportRequestRepository;
import com.musomi.manager.repository.StudentRepository;
import com.musomi.manager.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages generated report records and requests. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final GeneratedReportRepository generatedReportRepository;
    private final ReportRequestRepository reportRequestRepository;
    private final StudentRepository studentRepository;
    private final TermRepository termRepository;

    /** Lists generated reports for a student. */
    @Transactional(readOnly = true)
    public List<ReportResponse> listByStudent(Long studentId) {
        return generatedReportRepository.findByStudentIdOrderByGeneratedAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Returns a generated report by id. */
    @Transactional(readOnly = true)
    public ReportResponse getReport(Long id) {
        GeneratedReport report = generatedReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPORT_NOT_FOUND));
        return toResponse(report);
    }

    /** Creates a pending report request when no pending request exists for the student and term. */
    @Transactional
    public ReportRequest createReportRequest(Long schoolId, Long studentId, Long termId, Long requestedBy) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STUDENT_NOT_FOUND));
        if (student.getSchool() == null || !resolvedSchoolId.equals(student.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (term.getSchool() == null || !resolvedSchoolId.equals(term.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        if (reportRequestRepository.findByStudentIdAndTermIdAndStatus(studentId, termId, "PENDING").isPresent()) {
            throw new ValidationException(ErrorCode.REPORT_ALREADY_GENERATED);
        }

        ReportRequest request = ReportRequest.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .student(student)
                .term(term)
                .status("PENDING")
                .requestedBy(User.builder().id(requestedBy).build())
                .requestedAt(LocalDateTime.now())
                .build();
        ReportRequest saved = reportRequestRepository.save(request);
        log.info("Report request {} created for student {} and term {}", saved.getId(), studentId, termId);
        return saved;
    }

    /** Marks a report request generated after its report has been recorded. */
    @Transactional
    public void markGenerated(Long requestId, Long reportId) {
        ReportRequest request = reportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPORT_NOT_FOUND));
        generatedReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPORT_NOT_FOUND));
        request.setStatus("GENERATED");
        request.setProcessedAt(LocalDateTime.now());
        reportRequestRepository.save(request);
        log.info("Report request {} marked generated with report {}", requestId, reportId);
    }

    /** Records generated report metadata without generating a PDF. */
    @Transactional
    public GeneratedReport recordGenerated(Long schoolId, Long studentId, Long termId, String filePath,
                                           Long fileSizeBytes, Long generatedBy) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STUDENT_NOT_FOUND));
        if (student.getSchool() == null || !resolvedSchoolId.equals(student.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (term.getSchool() == null || !resolvedSchoolId.equals(term.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }

        GeneratedReport report = generatedReportRepository.save(GeneratedReport.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .student(student)
                .term(term)
                .filePath(filePath)
                .fileSizeBytes(fileSizeBytes)
                .generatedBy(User.builder().id(generatedBy).build())
                .generatedAt(LocalDateTime.now())
                .build());
        log.info("Report {} recorded for student {} and term {}", report.getId(), studentId, termId);
        return report;
    }

    private ReportResponse toResponse(GeneratedReport report) {
        Student student = report.getStudent();
        Term term = report.getTerm();
        return new ReportResponse(
                report.getId(),
                student != null ? student.getId() : null,
                student != null ? student.getFullName() : null,
                term != null ? term.getId() : null,
                term != null ? term.getName() : null,
                term != null && term.getAcademicYear() != null ? term.getAcademicYear().getYear() : null,
                report.getFilePath(),
                report.getFileSizeBytes(),
                report.getGeneratedAt(),
                "/reports/download/" + report.getId()
        );
    }
}
