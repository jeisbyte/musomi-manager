package com.musomi.manager.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musomi.manager.dto.response.ImportConfirmResponse;
import com.musomi.manager.dto.response.ImportPreviewResponse;
import com.musomi.manager.entity.ImportJob;
import com.musomi.manager.entity.Student;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.repository.ImportJobRepository;
import com.musomi.manager.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentImportServiceTest {

    private static final Long SCHOOL_ID = 1L;
    private static final Long USER_ID = 2L;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private ExcelService excelService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private StudentImportService studentImportService;

    @Test
    @DisplayName("should preview and create job when file valid")
    void shouldPreviewAndCreateJobWhenFileValid() throws IOException {
        ImportPreviewResponse preview = new ImportPreviewResponse(null, 1, 1, 0, List.of());
        ExcelService.ParsedStudentFile parsed = new ExcelService.ParsedStudentFile(preview, List.of(studentRow()));
        when(excelService.parseStudentFileWithRows(any(ByteArrayInputStream.class))).thenReturn(parsed);
        when(importJobRepository.save(any(ImportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile file = new MockMultipartFile("file", "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[] {1, 2, 3});

        ImportPreviewResponse response = studentImportService.previewImport(SCHOOL_ID, USER_ID, file);

        assertThat(response.importId()).startsWith("imp_");
        assertThat(response.totalRows()).isEqualTo(1);
        assertThat(response.validRows()).isEqualTo(1);
        verify(importJobRepository).save(any(ImportJob.class));
    }

    @Test
    @DisplayName("should throw when job reference invalid on confirm")
    void shouldThrowWhenJobReferenceInvalidOnConfirm() {
        when(importJobRepository.findByJobReference("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentImportService.confirmImport(SCHOOL_ID, "missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("should skip duplicates on confirm")
    void shouldSkipDuplicatesOnConfirm() throws Exception {
        ImportJob job = jobWithRows(List.of(studentRow(), studentRow()));
        when(importJobRepository.findByJobReference("imp_test")).thenReturn(Optional.of(job));
        when(studentRepository.existsBySchoolIdAndAdmissionNumber(SCHOOL_ID, "A-1"))
                .thenReturn(false, false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(importJobRepository.save(any(ImportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportConfirmResponse response = studentImportService.confirmImport(SCHOOL_ID, "imp_test");

        assertThat(response.imported()).isEqualTo(1);
        assertThat(response.skipped()).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("should confirm and create students")
    void shouldConfirmAndCreateStudents() throws Exception {
        ImportJob job = jobWithRows(List.of(studentRow()));
        when(importJobRepository.findByJobReference("imp_test")).thenReturn(Optional.of(job));
        when(studentRepository.existsBySchoolIdAndAdmissionNumber(SCHOOL_ID, "A-1")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(importJobRepository.save(any(ImportJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportConfirmResponse response = studentImportService.confirmImport(SCHOOL_ID, "imp_test");

        assertThat(response.imported()).isEqualTo(1);
        assertThat(response.skipped()).isZero();
        assertThat(job.getStatus()).isEqualTo("CONFIRMED");
        assertThat(job.getConfirmedAt()).isNotNull();
        verify(studentRepository).save(any(Student.class));
    }

    private ImportJob jobWithRows(List<ExcelService.StudentRow> rows) throws Exception {
        List<java.util.Map<String, Object>> validRows = rows.stream()
                .map(row -> java.util.Map.<String, Object>of(
                        "admissionNumber", row.admissionNumber(),
                        "fullName", row.fullName(),
                        "gender", row.gender(),
                        "dateOfBirth", row.dateOfBirth().toString(),
                        "currentClassId", row.currentClassId(),
                        "currentStreamId", row.currentStreamId()))
                .toList();
        String json = objectMapper.writeValueAsString(
                java.util.Map.of("errors", List.of(), "validRows", validRows));
        return ImportJob.builder()
                .jobReference("imp_test")
                .status("PENDING")
                .errorsJson(json)
                .build();
    }

    private ExcelService.StudentRow studentRow() {
        return new ExcelService.StudentRow("A-1", "Alex Student", "MALE",
                LocalDate.of(2008, 1, 15), 3L, 4L);
    }
}
