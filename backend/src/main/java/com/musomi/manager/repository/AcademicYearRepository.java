package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    List<AcademicYear> findBySchoolIdOrderByYearDesc(Long schoolId);

    Optional<AcademicYear> findBySchoolIdAndIsCurrentTrue(Long schoolId);

    boolean existsBySchoolIdAndYear(Long schoolId, Integer year);

    @Transactional
    @Modifying
    @Query("UPDATE AcademicYear a SET a.isCurrent = false WHERE a.school.id = :schoolId AND a.isCurrent = true")
    void clearCurrentFlag(@Param("schoolId") Long schoolId);
}
