package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.Stream;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamRepository extends JpaRepository<Stream, Long> {

    List<Stream> findByClassEntityIdAndIsActiveTrue(Long classId);

    boolean existsByClassEntityIdAndName(Long classId, String name);
}
