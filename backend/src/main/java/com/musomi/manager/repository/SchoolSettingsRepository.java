package com.musomi.manager.repository;

import java.util.Optional;

import com.musomi.manager.entity.SchoolSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, Long> {

    Optional<SchoolSettings> findBySchoolId(Long schoolId);

    boolean existsBySchoolId(Long schoolId);
}
