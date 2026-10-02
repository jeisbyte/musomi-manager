package com.musomi.manager.repository;

import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySchoolIdAndUsername(Long schoolId, String username);

    List<User> findBySchoolIdAndRole(Long schoolId, Role role);

    List<User> findBySchoolIdAndIsActiveTrue(Long schoolId);

    boolean existsBySchoolIdAndUsername(Long schoolId, String username);

    /**
     * Paginated, filtered, searchable lookup used by the admin user list.
     * Any of {@code role}, {@code active}, or {@code search} may be null to
     * skip that filter.
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.school.id = :schoolId
              AND (:role IS NULL OR u.role = :role)
              AND (:active IS NULL OR u.isActive = :active)
              AND (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<User> findUsers(@Param("schoolId") Long schoolId,
                         @Param("role") Role role,
                         @Param("active") Boolean active,
                         @Param("search") String search,
                         Pageable pageable);
}