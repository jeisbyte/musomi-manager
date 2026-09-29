package com.musomi.manager.repository;

import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySchoolIdAndUsername(Long schoolId, String username);

    List<User> findBySchoolIdAndRole(Long schoolId, Role role);

    List<User> findBySchoolIdAndIsActiveTrue(Long schoolId);

    boolean existsBySchoolIdAndUsername(Long schoolId, String username);
}
