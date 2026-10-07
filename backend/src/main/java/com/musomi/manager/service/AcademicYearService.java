package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.CreateAcademicYearRequest;
import com.musomi.manager.dto.response.AcademicYearResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.School;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.AcademicYearMapper;
import com.musomi.manager.repository.AcademicYearRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages academic years for a school. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicYearService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final AcademicYearRepository academicYearRepository;

    /** Lists a school's academic years, newest first. */
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> listYears(Long schoolId) {
        return AcademicYearMapper.toResponseList(
                academicYearRepository.findBySchoolIdOrderByYearDesc(resolveSchoolId(schoolId)));
    }

    /** Creates an academic year for a school. */
    @Transactional
    public AcademicYearResponse createYear(Long schoolId, CreateAcademicYearRequest request) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        if (academicYearRepository.existsBySchoolIdAndYear(resolvedSchoolId, request.year())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        AcademicYear academicYear = AcademicYear.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .year(request.year())
                .isCurrent(false)
                .createdAt(LocalDateTime.now())
                .build();

        AcademicYear saved = academicYearRepository.save(academicYear);
        log.info("Academic year created: academicYearId={}, year={}", saved.getId(), saved.getYear());
        return AcademicYearMapper.toResponse(saved);
    }

    /** Sets a school's academic year as current. */
    @Transactional
    public AcademicYearResponse setCurrent(Long id, Long schoolId) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (!resolvedSchoolId.equals(academicYear.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }

        academicYearRepository.clearCurrentFlag(resolvedSchoolId);
        academicYear.setIsCurrent(true);
        AcademicYear saved = academicYearRepository.save(academicYear);
        log.info("Current academic year set: academicYearId={}, schoolId={}", id, resolvedSchoolId);
        return AcademicYearMapper.toResponse(saved);
    }

    private Long resolveSchoolId(Long schoolId) {
        return schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
    }
}
