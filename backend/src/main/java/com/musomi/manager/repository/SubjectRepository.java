package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findBySchoolIdAndIsActiveTrueOrderByNameAsc(Long schoolId);

    Optional<Subject> findBySchoolIdAndCode(Long schoolId, String code);

    boolean existsBySchoolIdAndCode(Long schoolId, String code);
}
