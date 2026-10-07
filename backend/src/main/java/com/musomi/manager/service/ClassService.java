package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.CreateClassRequest;
import com.musomi.manager.dto.response.ClassResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.ClassLevel;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.ClassMapper;
import com.musomi.manager.repository.AcademicYearRepository;
import com.musomi.manager.repository.ClassLevelRepository;
import com.musomi.manager.repository.ClassRepository;
import com.musomi.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages classes within school academic years. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final ClassRepository classRepository;
    private final ClassLevelRepository classLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final UserRepository userRepository;

    /** Lists active classes for a school and academic year. */
    @Transactional(readOnly = true)
    public List<ClassResponse> listClasses(Long schoolId, Long academicYearId) {
        return ClassMapper.toResponseList(
                classRepository.findBySchoolIdAndAcademicYearIdAndIsActiveTrue(
                        resolveSchoolId(schoolId), academicYearId));
    }

    /** Creates a class for a school and academic year. */
    @Transactional
    public ClassResponse createClass(Long schoolId, CreateClassRequest request) {
        Long resolvedSchoolId = resolveSchoolId(schoolId);
        ClassLevel classLevel = classLevelRepository.findById(request.classLevelId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        AcademicYear academicYear = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (!resolvedSchoolId.equals(classLevel.getSchool().getId())
                || !resolvedSchoolId.equals(academicYear.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        if (classRepository.existsBySchoolIdAndAcademicYearIdAndClassLevelId(
                resolvedSchoolId, request.academicYearId(), request.classLevelId())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        User classTeacher = null;
        if (request.classTeacherId() != null) {
            classTeacher = userRepository.findById(request.classTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
            if (classTeacher.getSchool() == null
                    || !resolvedSchoolId.equals(classTeacher.getSchool().getId())) {
                throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND);
            }
        }

        ClassEntity classEntity = ClassEntity.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .classLevel(classLevel)
                .academicYear(academicYear)
                .name(request.name())
                .classTeacher(classTeacher)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        ClassEntity saved = classRepository.save(classEntity);
        log.info("Class created: classId={}, name={}, academicYearId={}",
                saved.getId(), saved.getName(), request.academicYearId());
        return ClassMapper.toResponse(saved);
    }

    private Long resolveSchoolId(Long schoolId) {
        return schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
    }
}
