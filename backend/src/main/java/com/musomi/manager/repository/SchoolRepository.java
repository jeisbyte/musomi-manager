package com.musomi.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musomi.manager.entity.School;

public interface SchoolRepository extends JpaRepository<School, Long> {
}
