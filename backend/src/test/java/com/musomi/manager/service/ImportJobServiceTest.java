package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.response.ImportJobResponse;
import com.musomi.manager.entity.ImportJob;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.repository.ImportJobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportJobServiceTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @InjectMocks
    private ImportJobService importJobService;

    @Test
    @DisplayName("should list import jobs by school")
    void shouldListBySchool() {
        when(importJobRepository.findBySchoolIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(job()));

        List<ImportJobResponse> result = importJobService.listBySchool(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).jobReference()).isEqualTo("imp_20261005_001");
    }

    @Test
    @DisplayName("should find import job by reference")
    void shouldFindByReference() {
        when(importJobRepository.findByJobReference("imp_20261005_001")).thenReturn(Optional.of(job()));

        ImportJobResponse result = importJobService.getByReference("imp_20261005_001");

        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.totalRows()).isEqualTo(12);
    }

    @Test
    @DisplayName("should throw when import job reference missing")
    void shouldThrowWhenReferenceMissing() {
        when(importJobRepository.findByJobReference("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> importJobService.getByReference("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("should create import job with pending status")
    void shouldCreateWithPendingStatus() {
        when(importJobRepository.save(any(ImportJob.class))).thenAnswer(invocation -> {
            ImportJob saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        ImportJobResponse result = importJobService.create(
                "imp_20261005_002", "STUDENTS", 12, "students.xlsx", 42L);

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.totalRows()).isEqualTo(12);
        assertThat(result.validRows()).isZero();
        assertThat(result.errorRows()).isZero();
        ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
        verify(importJobRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy().getId()).isEqualTo(42L);
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    private ImportJob job() {
        return ImportJob.builder()
                .id(3L)
                .jobReference("imp_20261005_001")
                .importType("STUDENTS")
                .status("PENDING")
                .totalRows(12)
                .validRows(0)
                .errorRows(0)
                .originalFilename("students.xlsx")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
