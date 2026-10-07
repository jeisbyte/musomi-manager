package com.musomi.manager.config;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.repository.SchoolRepository;
import com.musomi.manager.repository.UserRepository;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        School school = schoolRepository.findById(1L).orElseGet(this::seedSchool);

        if (userRepository.findBySchoolIdAndUsername(school.getId(), "admin").isPresent()) {
            log.info("Dev admin already exists, skipping seed");
            return;
        }

        userRepository.save(User.builder()
                .school(school)
                .username("admin")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Dev Admin")
                .role(Role.ADMIN)
                .isActive(true)
                .failedLoginAttempts(0)
                .build());
        log.info("Seeded dev admin user: admin / password123");
    }

    private School seedSchool() {
        log.info("Seeding dev school: St. Mary's Secondary School");
        return schoolRepository.save(School.builder()
                .name("St. Mary's Secondary School")
                .phone("+256700000000")
                .email("info@stmarys.ac.ug")
                .createdAt(LocalDateTime.now())
                .build());
    }
}
