package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.CreateAssignmentRequest;
import com.musomi.manager.dto.response.AssignmentResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Stream;
import com.musomi.manager.entity.Subject;
import com.musomi.manager.entity.TeacherAssignment;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.TeacherAssignmentMapper;
import com.musomi.manager.repository.AcademicYearRepository;
import com.musomi.manager.repository.ClassRepository;
import com.musomi.manager.repository.StreamRepository;
import com.musomi.manager.repository.SubjectRepository;
import com.musomi.manager.repository.TeacherAssignmentRepository;
import com.musomi.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages teacher assignments. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;
    private final StreamRepository streamRepository;
    private final AcademicYearRepository academicYearRepository;

    /** Lists all school assignments, optionally filtered by teacher. */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> listAssignments(Long schoolId, Long teacherId) {
        List<TeacherAssignment> assignments = teacherId == null
                ? teacherAssignmentRepository.findBySchoolIdOrderByIdAsc(schoolId)
                : teacherAssignmentRepository.findBySchoolIdAndTeacherIdOrderByIdAsc(schoolId, teacherId);
        return TeacherAssignmentMapper.toResponseList(assignments);
    }

    /** Creates an assignment after validating its parent records and school scope. */
    @Transactional
    public AssignmentResponse createAssignment(Long schoolId, CreateAssignmentRequest request) {
        User teacher = userRepository.findById(request.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        if (!belongsToSchool(teacher.getSchool(), schoolId) || teacher.getRole() != Role.TEACHER) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ValidationException(ErrorCode.VALIDATION_FAILED));
        if (!belongsToSchool(subject.getSchool(), schoolId)) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        ClassEntity classEntity = classRepository.findById(request.classId())
                .orElseThrow(() -> new ValidationException(ErrorCode.VALIDATION_FAILED));
        if (!belongsToSchool(classEntity.getSchool(), schoolId)) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        AcademicYear academicYear = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new ValidationException(ErrorCode.VALIDATION_FAILED));
        if (!belongsToSchool(academicYear.getSchool(), schoolId)
                || classEntity.getAcademicYear() == null
                || !academicYear.getId().equals(classEntity.getAcademicYear().getId())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        Stream stream = null;
        if (request.streamId() != null) {
            stream = streamRepository.findById(request.streamId())
                    .orElseThrow(() -> new ValidationException(ErrorCode.VALIDATION_FAILED));
            if (!belongsToSchool(stream.getSchool(), schoolId)
                    || stream.getClassEntity() == null
                    || !classEntity.getId().equals(stream.getClassEntity().getId())) {
                throw new ValidationException(ErrorCode.VALIDATION_FAILED);
            }
        }

        TeacherAssignment assignment = TeacherAssignment.builder()
                .school(School.builder().id(schoolId).build())
                .teacher(teacher)
                .subject(subject)
                .classEntity(classEntity)
                .stream(stream)
                .academicYear(academicYear)
                .createdAt(LocalDateTime.now())
                .build();
        TeacherAssignment saved = teacherAssignmentRepository.save(assignment);
        log.info("Teacher assignment created: assignmentId={}, schoolId={}", saved.getId(), schoolId);
        return TeacherAssignmentMapper.toResponse(saved);
    }

    /** Deletes an assignment by id. */
    @Transactional
    public void deleteAssignment(Long id) {
        if (!teacherAssignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        teacherAssignmentRepository.deleteById(id);
        log.info("Teacher assignment deleted: assignmentId={}", id);
    }

    private boolean belongsToSchool(School school, Long schoolId) {
        return school != null && schoolId != null && schoolId.equals(school.getId());
    }
}
