package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.CreateClassLevelRequest;
import com.musomi.manager.dto.response.ClassLevelResponse;
import com.musomi.manager.entity.ClassLevel;
import com.musomi.manager.entity.School;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.ClassLevelMapper;
import com.musomi.manager.repository.ClassLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages class levels offered by a school. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassLevelService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final ClassLevelRepository classLevelRepository;

    /** Lists active class levels for a school in configured order. */
    @Transactional(readOnly = true)
    public List<ClassLevelResponse> listLevels(Long schoolId) {
        return ClassLevelMapper.toResponseList(
                classLevelRepository.findBySchoolIdAndIsActiveTrueOrderBySortOrderAsc(resolveSchoolId(schoolId)));
    }

    /** Creates a class level for a school. */
    @Transactional
    public ClassLevelResponse createLevel(Long schoolId, CreateClassLevelRequest request) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        if (classLevelRepository.existsBySchoolIdAndName(resolvedSchoolId, request.name())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        ClassLevel classLevel = ClassLevel.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .name(request.name())
                .sortOrder(request.sortOrder())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        ClassLevel saved = classLevelRepository.save(classLevel);
        log.info("Class level created: classLevelId={}, name={}", saved.getId(), saved.getName());
        return ClassLevelMapper.toResponse(saved);
    }

    private Long resolveSchoolId(Long schoolId) {
        return schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
    }
}
