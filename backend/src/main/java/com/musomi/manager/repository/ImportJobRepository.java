package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persists import job history. */
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    /** Lists import jobs for a school, newest first. */
    List<ImportJob> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

    /** Finds an import job by its unique reference. */
    Optional<ImportJob> findByJobReference(String jobReference);
}
