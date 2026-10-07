package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.response.ImportJobResponse;
import com.musomi.manager.entity.ImportJob;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.mapper.ImportJobMapper;
import com.musomi.manager.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages import job records. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportJobService {

    private static final Long DEFAULT_SCHOOL_ID = 1L;

    private final ImportJobRepository importJobRepository;

    /** Lists import jobs for a school. */
    @Transactional(readOnly = true)
    public List<ImportJobResponse> listBySchool(Long schoolId) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        return ImportJobMapper.toResponseList(
                importJobRepository.findBySchoolIdOrderByCreatedAtDesc(resolvedSchoolId));
    }

    /** Returns the import job with the specified reference. */
    @Transactional(readOnly = true)
    public ImportJobResponse getByReference(String reference) {
        ImportJob job = importJobRepository.findByJobReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        return ImportJobMapper.toResponse(job);
    }

    /** Creates a pending import job with empty validation counts. */
    @Transactional
    public ImportJobResponse create(String jobReference, String importType, Integer totalRows,
                                   String originalFilename, Long createdBy) {
        ImportJob job = ImportJob.builder()
                .school(School.builder().id(DEFAULT_SCHOOL_ID).build())
                .jobReference(jobReference)
                .importType(importType)
                .status("PENDING")
                .totalRows(totalRows)
                .validRows(0)
                .errorRows(0)
                .originalFilename(originalFilename)
                .createdBy(User.builder().id(createdBy).build())
                .createdAt(LocalDateTime.now())
                .build();
        ImportJob saved = importJobRepository.save(job);
        log.info("Import job created: reference={}, schoolId={}", jobReference, DEFAULT_SCHOOL_ID);
        return ImportJobMapper.toResponse(saved);
    }
}
