package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findBySchoolIdAndAdmissionNumber(Long schoolId, String admissionNumber);

    List<Student> findBySchoolIdAndStatus(Long schoolId, String status);

    List<Student> findByCurrentClassIdAndIsActiveTrue(Long classId);

    List<Student> findByCurrentStreamIdAndIsActiveTrue(Long streamId);

    boolean existsBySchoolIdAndAdmissionNumber(Long schoolId, String admissionNumber);

    long countBySchoolIdAndStatus(Long schoolId, String status);
}
