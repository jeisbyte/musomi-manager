package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.CreateSubjectRequest;
import com.musomi.manager.dto.response.SubjectResponse;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Subject;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.SubjectMapper;
import com.musomi.manager.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages subjects offered by a school. */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final SubjectRepository subjectRepository;

    /** Lists active subjects for a school in name order. */
    @Transactional(readOnly = true)
    public List<SubjectResponse> listSubjects(Long schoolId) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        return SubjectMapper.toResponseList(
                subjectRepository.findBySchoolIdAndIsActiveTrueOrderByNameAsc(resolvedSchoolId));
    }

    /** Creates a subject for a school. */
    @Transactional
    public SubjectResponse createSubject(Long schoolId, CreateSubjectRequest request) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        if (subjectRepository.existsBySchoolIdAndCode(resolvedSchoolId, request.code())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        Subject subject = Subject.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .isCore(Boolean.TRUE.equals(request.isCore()))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        Subject saved = subjectRepository.save(subject);
        log.info("Subject created: subjectId={}, code={}", saved.getId(), saved.getCode());
        return SubjectMapper.toResponse(saved);
    }

    private Long resolveSchoolId(Long schoolId) {
        return schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
    }
}
