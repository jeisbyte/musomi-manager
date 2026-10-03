package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    List<ClassEntity> findBySchoolIdAndAcademicYearIdAndIsActiveTrue(Long schoolId, Long academicYearId);

    boolean existsBySchoolIdAndAcademicYearIdAndClassLevelId(
            Long schoolId, Long academicYearId, Long classLevelId);
}
