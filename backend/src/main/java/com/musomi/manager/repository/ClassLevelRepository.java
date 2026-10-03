package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.ClassLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassLevelRepository extends JpaRepository<ClassLevel, Long> {

    List<ClassLevel> findBySchoolIdAndIsActiveTrueOrderBySortOrderAsc(Long schoolId);

    boolean existsBySchoolIdAndName(Long schoolId, String name);
}
