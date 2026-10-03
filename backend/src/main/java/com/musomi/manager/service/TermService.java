package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.CreateTermRequest;
import com.musomi.manager.dto.response.TermResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Term;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.TermMapper;
import com.musomi.manager.repository.AcademicYearRepository;
import com.musomi.manager.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages a school's terms. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TermService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final TermRepository termRepository;
    private final AcademicYearRepository academicYearRepository;

    /** Lists a school's terms, optionally filtered by academic year. */
    @Transactional(readOnly = true)
    public List<TermResponse> listTerms(Long schoolId, Long academicYearId) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        List<Term> terms = academicYearId == null
                ? termRepository.findBySchoolIdOrderByStartDateDesc(resolvedSchoolId)
                : termRepository.findByAcademicYearIdOrderByNameAsc(academicYearId).stream()
                        .filter(term -> resolvedSchoolId.equals(term.getSchool().getId()))
                        .toList();
        return TermMapper.toResponseList(terms);
    }

    /** Creates a term in a school's academic year. */
    @Transactional
    public TermResponse createTerm(Long schoolId, CreateTermRequest request) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        AcademicYear academicYear = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (!resolvedSchoolId.equals(academicYear.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        if (request.endDate().isBefore(request.startDate()) || request.endDate().isEqual(request.startDate())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }
        if (termRepository.existsByAcademicYearIdAndName(request.academicYearId(), request.name())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        Term term = Term.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .academicYear(academicYear)
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .isCurrent(false)
                .createdAt(LocalDateTime.now())
                .build();
        Term saved = termRepository.save(term);
        log.info("Term created: termId={}, academicYearId={}", saved.getId(), request.academicYearId());
        return TermMapper.toResponse(saved);
    }

    /** Sets a school's term as current. */
    @Transactional
    public TermResponse setCurrent(Long id, Long schoolId) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (!resolvedSchoolId.equals(term.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }

        termRepository.clearCurrentFlag(resolvedSchoolId);
        term.setIsCurrent(true);
        Term saved = termRepository.save(term);
        log.info("Current term set: termId={}, schoolId={}", id, resolvedSchoolId);
        return TermMapper.toResponse(saved);
    }

    private Long resolveSchoolId(Long schoolId) {
        return schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
    }
}
