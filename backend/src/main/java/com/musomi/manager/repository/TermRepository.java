package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByAcademicYearIdOrderByNameAsc(Long academicYearId);

    Optional<Term> findBySchoolIdAndIsCurrentTrue(Long schoolId);

    List<Term> findBySchoolIdOrderByStartDateDesc(Long schoolId);

    boolean existsByAcademicYearIdAndName(Long academicYearId, String name);

    @Transactional
    @Modifying
    @Query("UPDATE Term t SET t.isCurrent = false WHERE t.school.id = :schoolId AND t.isCurrent = true")
    void clearCurrentFlag(@Param("schoolId") Long schoolId);
}
