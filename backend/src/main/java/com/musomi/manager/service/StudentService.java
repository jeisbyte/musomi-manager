package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.musomi.manager.dto.request.CreateGuardianRequest;
import com.musomi.manager.dto.request.CreateStudentRequest;
import com.musomi.manager.dto.request.UpdateStudentRequest;
import com.musomi.manager.dto.response.StudentResponse;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.Guardian;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Stream;
import com.musomi.manager.entity.Student;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.StudentMapper;
import com.musomi.manager.repository.ClassRepository;
import com.musomi.manager.repository.GuardianRepository;
import com.musomi.manager.repository.StreamRepository;
import com.musomi.manager.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages students and their guardians. */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final ClassRepository classRepository;
    private final StreamRepository streamRepository;
    private final AuditService auditService;

    /** Lists students for a school with optional class, stream, status, and search filters. */
    @Transactional(readOnly = true)
    public List<StudentResponse> listStudents(Long schoolId, Long classId, Long streamId,
                                              String status, String search) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        String resolvedStatus = status != null ? status : "ACTIVE";
        String normalizedSearch = search == null || search.isBlank()
                ? null
                : search.trim().toLowerCase(Locale.ROOT);
        List<Student> students = studentRepository.findBySchoolIdAndStatus(resolvedSchoolId, resolvedStatus)
                .stream()
                .filter(student -> classId == null || student.getCurrentClass() != null
                        && classId.equals(student.getCurrentClass().getId()))
                .filter(student -> streamId == null || student.getCurrentStream() != null
                        && streamId.equals(student.getCurrentStream().getId()))
                .filter(student -> normalizedSearch == null
                        || student.getAdmissionNumber().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                        || student.getFullName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .toList();
        return StudentMapper.toResponseList(students);
    }

    /** Creates a student and optionally creates the student's primary guardian. */
    @Transactional
    public StudentResponse createStudent(Long schoolId, CreateStudentRequest request) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        if (studentRepository.existsBySchoolIdAndAdmissionNumber(
                resolvedSchoolId, request.admissionNumber())) {
            throw new ValidationException(
                    ErrorCode.DUPLICATE_ADMISSION_NUMBER,
                    Map.of("number", request.admissionNumber()));
        }

        ClassEntity currentClass = findClass(request.currentClassId(), resolvedSchoolId);
        Stream currentStream = findStream(request.currentStreamId(), resolvedSchoolId);
        validateClassStream(currentClass, currentStream);

        Student student = Student.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .admissionNumber(request.admissionNumber())
                .fullName(request.fullName())
                .gender(request.gender())
                .dateOfBirth(request.dateOfBirth())
                .currentClass(currentClass)
                .currentStream(currentStream)
                .status("ACTIVE")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        Student saved = studentRepository.save(student);

        if (request.guardian() != null) {
            guardianRepository.save(buildGuardian(saved, request.guardian()));
        }

        log.info("Student {} created for school {}", saved.getAdmissionNumber(), resolvedSchoolId);
        // TODO(backend-lead): pass real userId once @CurrentUser is wired in services
        auditService.log("STUDENT_CREATED", "Student", saved.getId(), 1L, resolvedSchoolId);
        return StudentMapper.toResponseWithGuardians(saved, guardianRepository.findByStudentId(saved.getId()));
    }

    /** Returns one student with guardian details. */
    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        Student student = findStudent(id);
        return StudentMapper.toResponseWithGuardians(student, guardianRepository.findByStudentId(id));
    }

    /** Updates a student's mutable profile, enrollment, and status fields. */
    @Transactional
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request) {
        Student student = findStudent(id);
        Long schoolId = student.getSchool() != null ? student.getSchool().getId() : DEFAULT_SCHOOL_ID;
        ClassEntity currentClass = findClass(request.currentClassId(), schoolId);
        Stream currentStream = findStream(request.currentStreamId(), schoolId);
        validateClassStream(currentClass, currentStream);

        student.setFullName(request.fullName());
        student.setGender(request.gender());
        student.setDateOfBirth(request.dateOfBirth());
        student.setCurrentClass(currentClass);
        student.setCurrentStream(currentStream);
        if (request.status() != null) {
            student.setStatus(request.status());
        }
        student.setUpdatedAt(LocalDateTime.now());

        Student saved = studentRepository.save(student);
        log.info("Student {} updated", id);
        // TODO(backend-lead): pass real userId once @CurrentUser is wired in services
        // TODO(backend-lead): pass real schoolId once school context is wired in services
        auditService.log("STUDENT_UPDATED", "Student", saved.getId(), 1L, 1L);
        return StudentMapper.toResponseWithGuardians(saved, guardianRepository.findByStudentId(id));
    }

    /** Deactivates a student without deleting the record. */
    @Transactional
    public void deactivateStudent(Long id) {
        Student student = findStudent(id);
        student.setIsActive(false);
        student.setStatus("INACTIVE");
        student.setUpdatedAt(LocalDateTime.now());
        studentRepository.save(student);
        log.info("Student {} deactivated", id);
        // TODO(backend-lead): pass real userId once @CurrentUser is wired in services
        // TODO(backend-lead): pass real schoolId once school context is wired in services
        auditService.log("STUDENT_DEACTIVATED", "Student", id, 1L, 1L);
    }

    /** Adds a guardian to an existing student and returns the updated student details. */
    @Transactional
    public StudentResponse addGuardian(Long studentId, CreateGuardianRequest request) {
        Student student = findStudent(studentId);
        guardianRepository.save(buildGuardian(student, request));
        // TODO(backend-lead): pass real userId once @CurrentUser is wired in services
        // TODO(backend-lead): pass real schoolId once school context is wired in services
        auditService.log("STUDENT_UPDATED", "Student", studentId, 1L, 1L);
        return StudentMapper.toResponseWithGuardians(
                student, guardianRepository.findByStudentId(studentId));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STUDENT_NOT_FOUND));
    }

    private ClassEntity findClass(Long classId, Long schoolId) {
        if (classId == null) {
            return null;
        }
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (classEntity.getSchool() == null || !schoolId.equals(classEntity.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        return classEntity;
    }

    private Stream findStream(Long streamId, Long schoolId) {
        if (streamId == null) {
            return null;
        }
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (stream.getSchool() == null || !schoolId.equals(stream.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        return stream;
    }

    private void validateClassStream(ClassEntity currentClass, Stream currentStream) {
        if (currentClass != null && currentStream != null
                && (currentStream.getClassEntity() == null
                || !currentClass.getId().equals(currentStream.getClassEntity().getId()))) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }
    }

    private Guardian buildGuardian(Student student, CreateGuardianRequest request) {
        return Guardian.builder()
                .school(student.getSchool())
                .student(student)
                .name(request.name())
                .relationship(request.relationship())
                .phone(request.phone())
                .email(request.email())
                .isPrimary(Boolean.TRUE.equals(request.isPrimary()))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
