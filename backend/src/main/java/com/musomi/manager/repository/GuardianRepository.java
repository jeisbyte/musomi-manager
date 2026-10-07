package com.musomi.manager.repository;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.entity.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    List<Guardian> findByStudentId(Long studentId);

    Optional<Guardian> findByStudentIdAndIsPrimaryTrue(Long studentId);

    void deleteByStudentId(Long studentId);
}
