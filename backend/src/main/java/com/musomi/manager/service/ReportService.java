package com.musomi.manager.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.musomi.manager.dto.response.ReportResponse;
import com.musomi.manager.entity.Assessment;
import com.musomi.manager.entity.Comment;
import com.musomi.manager.entity.GeneratedReport;
import com.musomi.manager.entity.ReportRequest;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.SchoolSettings;
import com.musomi.manager.entity.Score;
import com.musomi.manager.entity.Student;
import com.musomi.manager.entity.Term;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.AssessmentRepository;
import com.musomi.manager.repository.CommentRepository;
import com.musomi.manager.repository.GeneratedReportRepository;
import com.musomi.manager.repository.ReportRequestRepository;
import com.musomi.manager.repository.SchoolSettingsRepository;
import com.musomi.manager.repository.ScoreRepository;
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
    private final AssessmentRepository assessmentRepository;
    private final ScoreRepository scoreRepository;
    private final CommentRepository commentRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final PdfService pdfService;

    /** Generates PDF report cards for active students in a class and stream. */
    @Transactional
    public List<ReportResponse> generateForClassStreamTerm(Long schoolId, Long classId, Long streamId, Long termId,
                                                           Long generatedBy) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (term.getSchool() == null || !resolvedSchoolId.equals(term.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        SchoolSettings settings = schoolSettingsRepository.findBySchoolId(resolvedSchoolId).orElse(null);
        List<Student> students = studentRepository.findByCurrentClassIdAndIsActiveTrue(classId).stream()
                .filter(student -> belongsToSchoolAndStream(student, resolvedSchoolId, streamId))
                .toList();
        if (students.isEmpty()) {
            throw new ValidationException(ErrorCode.NO_MARKS_PUBLISHED);
        }

        Map<Long, List<Score>> scoresByStudent = new HashMap<>();
        Set<Long> subjectIds = new HashSet<>();
        for (Student student : students) {
            List<Score> scores = scoreRepository.findByStudentId(student.getId());
            scoresByStudent.put(student.getId(), scores);
            for (Score score : scores) {
                Assessment assessment = score.getAssessment();
                if (isEligibleAssessment(assessment, resolvedSchoolId, classId, streamId, termId)
                        && assessment.getSubject() != null && assessment.getSubject().getId() != null) {
                    subjectIds.add(assessment.getSubject().getId());
                }
            }
        }
        List<Assessment> publishedAssessments = findPublishedAssessments(
                subjectIds, resolvedSchoolId, classId, streamId, termId);
        if (publishedAssessments.isEmpty()) {
            throw new ValidationException(ErrorCode.NO_MARKS_PUBLISHED);
        }
        List<StudentReport> reports = students.stream()
                .map(student -> calculateStudentReport(student, publishedAssessments,
                        scoresByStudent.getOrDefault(student.getId(), List.of())))
                .toList();
        Map<Long, Integer> positions = calculatePositions(reports);
        List<ReportResponse> responses = new ArrayList<>();
        for (StudentReport report : reports) {
            responses.add(generateStudentReport(
                    report, term, settings, resolvedSchoolId, termId, generatedBy,
                    positions.get(report.student().getId()), students.size()));
        }
        return List.copyOf(responses);
    }

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

    /** Returns generated report metadata for file download. */
    @Transactional(readOnly = true)
    public GeneratedReport getGeneratedReport(Long id) {
        return generatedReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPORT_NOT_FOUND));
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

    private boolean belongsToSchoolAndStream(Student student, Long schoolId, Long streamId) {
        if (student.getSchool() == null || !schoolId.equals(student.getSchool().getId())
                || !Boolean.TRUE.equals(student.getIsActive())) {
            return false;
        }
        return streamId == null
                || student.getCurrentStream() != null && streamId.equals(student.getCurrentStream().getId());
    }

    private List<Assessment> findPublishedAssessments(Set<Long> subjectIds, Long schoolId, Long classId,
                                                      Long streamId, Long termId) {
        Map<Long, Assessment> assessments = new HashMap<>();
        for (Long subjectId : subjectIds) {
            for (Assessment assessment : assessmentRepository.findByClassEntityIdAndSubjectIdAndTermId(
                    classId, subjectId, termId)) {
                if (isEligibleAssessment(assessment, schoolId, classId, streamId, termId)
                        && assessment.getId() != null) {
                    assessments.put(assessment.getId(), assessment);
                }
            }
        }
        return List.copyOf(assessments.values());
    }

    private StudentReport calculateStudentReport(Student student, List<Assessment> assessments, List<Score> scores) {
        Map<Long, Score> scoresByAssessment = new HashMap<>();
        for (Score score : scores) {
            if (score.getAssessment() != null && score.getAssessment().getId() != null) {
                scoresByAssessment.put(score.getAssessment().getId(), score);
            }
        }
        Map<Long, SubjectTotals> totalsBySubject = new HashMap<>();
        for (Assessment assessment : assessments) {
            Score score = scoresByAssessment.get(assessment.getId());
            if (score == null
                    || score.getScore() == null || assessment.getMaxScore() == null
                    || assessment.getMaxScore().compareTo(BigDecimal.ZERO) <= 0
                    || assessment.getSubject() == null) {
                continue;
            }
            BigDecimal percentage = score.getScore().multiply(BigDecimal.valueOf(100))
                    .divide(assessment.getMaxScore(), 4, RoundingMode.HALF_UP);
            Long subjectId = assessment.getSubject().getId();
            SubjectTotals totals = totalsBySubject.computeIfAbsent(subjectId,
                    ignored -> new SubjectTotals(assessment.getSubject().getName()));
            totals.add(percentage);
        }
        if (totalsBySubject.isEmpty()) {
            throw new ValidationException(ErrorCode.NO_MARKS_PUBLISHED);
        }

        List<PdfService.SubjectAverage> subjects = totalsBySubject.values().stream()
                .map(SubjectTotals::toAverage)
                .sorted(Comparator.comparing(PdfService.SubjectAverage::subjectName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        BigDecimal overallAverage = subjects.stream()
                .map(PdfService.SubjectAverage::average)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(subjects.size()), 1, RoundingMode.HALF_UP);
        String overallGrade = com.musomi.manager.util.GradeCalculator.calculate(
                overallAverage, BigDecimal.valueOf(100));
        return new StudentReport(student, subjects, overallAverage, overallGrade);
    }

    private boolean isEligibleAssessment(Assessment assessment, Long schoolId, Long classId, Long streamId,
                                         Long termId) {
        return assessment != null
                && "PUBLISHED".equalsIgnoreCase(assessment.getStatus())
                && assessment.getSchool() != null && schoolId.equals(assessment.getSchool().getId())
                && assessment.getClassEntity() != null && classId.equals(assessment.getClassEntity().getId())
                && assessment.getTerm() != null && termId.equals(assessment.getTerm().getId())
                && (assessment.getStream() == null
                        || streamId != null && streamId.equals(assessment.getStream().getId()));
    }

    private Map<Long, Integer> calculatePositions(List<StudentReport> reports) {
        List<StudentReport> ranked = reports.stream()
                .sorted(Comparator.comparing(StudentReport::overallAverage).reversed())
                .toList();
        Map<Long, Integer> positions = new HashMap<>();
        BigDecimal previousAverage = null;
        int position = 0;
        for (int index = 0; index < ranked.size(); index++) {
            StudentReport report = ranked.get(index);
            if (previousAverage == null || previousAverage.compareTo(report.overallAverage()) != 0) {
                position = index + 1;
                previousAverage = report.overallAverage();
            }
            positions.put(report.student().getId(), position);
        }
        return positions;
    }

    private ReportResponse generateStudentReport(StudentReport report, Term term, SchoolSettings settings,
                                                 Long schoolId, Long termId, Long generatedBy, Integer position,
                                                 int classSize) {
        byte[] pdf = pdfService.generateReportCard(
                report.student(), term, settings, report.subjects(), report.overallAverage(), report.overallGrade(),
                position == null ? 0 : position, classSize,
                commentRepository.findByStudentIdAndTermIdOrderByWrittenAtDesc(report.student().getId(), termId));
        Path filePath = reportFilePath(report.student(), term, schoolId, termId);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, pdf);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write generated report card to " + filePath, exception);
        }

        GeneratedReport saved = generatedReportRepository.save(GeneratedReport.builder()
                .school(School.builder().id(schoolId).build())
                .student(report.student())
                .term(term)
                .filePath(filePath.toString())
                .fileSizeBytes((long) pdf.length)
                .generatedBy(User.builder().id(generatedBy == null ? DEFAULT_SCHOOL_ID : generatedBy).build())
                .generatedAt(LocalDateTime.now())
                .build());
        log.info("Generated report {} for student {} in term {}", saved.getId(),
                report.student().getId(), termId);
        return toResponse(saved);
    }

    private Path reportFilePath(Student student, Term term, Long schoolId, Long termId) {
        String year = term.getAcademicYear() == null || term.getAcademicYear().getYear() == null
                ? "unknown"
                : term.getAcademicYear().getYear().toString();
        String admissionNumber = student.getAdmissionNumber() == null || student.getAdmissionNumber().isBlank()
                ? "student-" + student.getId()
                : student.getAdmissionNumber().replaceAll("[^A-Za-z0-9._-]", "_");
        return Path.of("data", "reports", schoolId.toString(), year, termId.toString(), admissionNumber + ".pdf");
    }

    private record StudentReport(Student student, List<PdfService.SubjectAverage> subjects,
                                 BigDecimal overallAverage, String overallGrade) {
    }

    private static final class SubjectTotals {
        private final String subjectName;
        private BigDecimal total = BigDecimal.ZERO;
        private int count;

        private SubjectTotals(String subjectName) {
            this.subjectName = subjectName;
        }

        private void add(BigDecimal score) {
            total = total.add(score);
            count++;
        }

        private PdfService.SubjectAverage toAverage() {
            BigDecimal average = total.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
            return new PdfService.SubjectAverage(subjectName, average,
                    com.musomi.manager.util.GradeCalculator.calculate(average, BigDecimal.valueOf(100)), count);
        }
    }
}
