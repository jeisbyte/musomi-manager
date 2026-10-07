package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.CreateGuardianRequest;
import com.musomi.manager.dto.response.GuardianResponse;
import com.musomi.manager.entity.Guardian;
import com.musomi.manager.entity.Student;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.mapper.StudentMapper;
import com.musomi.manager.repository.GuardianRepository;
import com.musomi.manager.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages guardians associated with students. */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final StudentRepository studentRepository;

    /** Lists guardians for a student. */
    @Transactional(readOnly = true)
    public List<GuardianResponse> listGuardians(Long studentId) {
        return guardianRepository.findByStudentId(studentId).stream()
                .map(StudentMapper::toGuardianResponse)
                .toList();
    }

    /** Creates a guardian for an existing student. */
    @Transactional
    public GuardianResponse createGuardian(Long studentId, CreateGuardianRequest request) {
        Student student = findStudent(studentId);
        Guardian guardian = Guardian.builder()
                .school(student.getSchool())
                .student(student)
                .name(request.name())
                .relationship(request.relationship())
                .phone(request.phone())
                .email(request.email())
                .isPrimary(Boolean.TRUE.equals(request.isPrimary()))
                .createdAt(LocalDateTime.now())
                .build();
        Guardian saved = guardianRepository.save(guardian);
        log.info("Guardian {} created for student {}", saved.getId(), studentId);
        return StudentMapper.toGuardianResponse(saved);
    }

    /** Hard-deletes a guardian child record. */
    @Transactional
    public void deleteGuardian(Long guardianId) {
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STUDENT_NOT_FOUND));
        guardianRepository.delete(guardian);
        log.info("Guardian {} deleted", guardianId);
    }

    private Student findStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
