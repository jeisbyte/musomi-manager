package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.response.ReportResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.GeneratedReport;
import com.musomi.manager.entity.ReportRequest;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Student;
import com.musomi.manager.entity.Term;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.GeneratedReportRepository;
import com.musomi.manager.repository.ReportRequestRepository;
import com.musomi.manager.repository.StudentRepository;
import com.musomi.manager.repository.TermRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final Long SCHOOL_ID = 1L;
    private static final Long STUDENT_ID = 2L;
    private static final Long TERM_ID = 3L;
    private static final Long USER_ID = 4L;

    @Mock
    private GeneratedReportRepository generatedReportRepository;

    @Mock
    private ReportRequestRepository reportRequestRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TermRepository termRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("should list reports by student")
    void shouldListReportsByStudent() {
        when(generatedReportRepository.findByStudentIdOrderByGeneratedAtDesc(STUDENT_ID))
                .thenReturn(List.of(report()));

        List<ReportResponse> response = reportService.listByStudent(STUDENT_ID);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).studentName()).isEqualTo("Alex Student");
        assertThat(response.get(0).academicYear()).isEqualTo(2026);
    }

    @Test
    @DisplayName("should get report when it exists")
    void shouldGetReportWhenExists() {
        when(generatedReportRepository.findById(5L)).thenReturn(Optional.of(report()));

        ReportResponse response = reportService.getReport(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.downloadUrl()).isEqualTo("/reports/download/5");
    }

    @Test
    @DisplayName("should throw when report not found")
    void shouldThrowWhenReportNotFound() {
        when(generatedReportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReport(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REPORT_NOT_FOUND);
    }

    @Test
    @DisplayName("should create request when none active")
    void shouldCreateRequestWhenNoneActive() {
        stubRequestParents();
        when(reportRequestRepository.findByStudentIdAndTermIdAndStatus(STUDENT_ID, TERM_ID, "PENDING"))
                .thenReturn(Optional.empty());
        when(reportRequestRepository.save(any(ReportRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReportRequest response = reportService.createReportRequest(SCHOOL_ID, STUDENT_ID, TERM_ID, USER_ID);

        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getStudent().getId()).isEqualTo(STUDENT_ID);
        verify(reportRequestRepository).save(any(ReportRequest.class));
    }

    @Test
    @DisplayName("should throw when active request exists")
    void shouldThrowWhenActiveRequestExists() {
        stubRequestParents();
        when(reportRequestRepository.findByStudentIdAndTermIdAndStatus(STUDENT_ID, TERM_ID, "PENDING"))
                .thenReturn(Optional.of(ReportRequest.builder().id(8L).status("PENDING").build()));

        assertThatThrownBy(() -> reportService.createReportRequest(SCHOOL_ID, STUDENT_ID, TERM_ID, USER_ID))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REPORT_ALREADY_GENERATED);
    }

    @Test
    @DisplayName("should mark generated and update status")
    void shouldMarkGeneratedAndUpdateStatus() {
        ReportRequest request = ReportRequest.builder().id(8L).status("PENDING").build();
        when(reportRequestRepository.findById(8L)).thenReturn(Optional.of(request));
        when(generatedReportRepository.findById(5L)).thenReturn(Optional.of(report()));
        when(reportRequestRepository.save(any(ReportRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        reportService.markGenerated(8L, 5L);

        assertThat(request.getStatus()).isEqualTo("GENERATED");
        assertThat(request.getProcessedAt()).isNotNull();
        verify(reportRequestRepository).save(request);
    }

    @Test
    @DisplayName("should record generated report")
    void shouldRecordGeneratedReport() {
        stubRequestParents();
        when(generatedReportRepository.save(any(GeneratedReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GeneratedReport response = reportService.recordGenerated(
                SCHOOL_ID, STUDENT_ID, TERM_ID, "reports/term-1/student-2.pdf", 1024L, USER_ID);

        assertThat(response.getFilePath()).isEqualTo("reports/term-1/student-2.pdf");
        assertThat(response.getFileSizeBytes()).isEqualTo(1024L);
        assertThat(response.getGeneratedAt()).isNotNull();
        verify(generatedReportRepository).save(any(GeneratedReport.class));
    }

    private void stubRequestParents() {
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(
                Student.builder().id(STUDENT_ID).school(school()).fullName("Alex Student").build()));
        when(termRepository.findById(TERM_ID)).thenReturn(Optional.of(term()));
    }

    private GeneratedReport report() {
        return GeneratedReport.builder()
                .id(5L)
                .school(school())
                .student(Student.builder().id(STUDENT_ID).school(school()).fullName("Alex Student").build())
                .term(term())
                .filePath("reports/term-1/student-2.pdf")
                .fileSizeBytes(1024L)
                .generatedAt(LocalDateTime.of(2026, 4, 20, 15, 0))
                .build();
    }

    private Term term() {
        return Term.builder()
                .id(TERM_ID)
                .school(school())
                .name("Term 1")
                .academicYear(AcademicYear.builder().id(6L).year(2026).build())
                .build();
    }

    private School school() {
        return School.builder().id(SCHOOL_ID).build();
    }
}
