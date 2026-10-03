package com.musomi.manager.service;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.request.CreateClassRequest;
import com.musomi.manager.dto.response.ClassResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.ClassLevel;
import com.musomi.manager.entity.School;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.repository.AcademicYearRepository;
import com.musomi.manager.repository.ClassLevelRepository;
import com.musomi.manager.repository.ClassRepository;
import com.musomi.manager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassServiceTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private ClassLevelRepository classLevelRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClassService classService;

    @Test
    @DisplayName("should return classes when listed")
    void shouldReturnClassesWhenListed() {
        when(classRepository.findBySchoolIdAndAcademicYearIdAndIsActiveTrue(1L, 3L))
                .thenReturn(List.of(classEntity()));

        List<ClassResponse> classes = classService.listClasses(1L, 3L);

        assertThat(classes).hasSize(1);
        assertThat(classes.getFirst().name()).isEqualTo("S1");
    }

    @Test
    @DisplayName("should create class when parents exist")
    void shouldCreateClassWhenParentsExist() {
        when(classLevelRepository.findById(2L)).thenReturn(Optional.of(classLevel()));
        when(academicYearRepository.findById(3L)).thenReturn(Optional.of(academicYear()));
        when(classRepository.existsBySchoolIdAndAcademicYearIdAndClassLevelId(1L, 3L, 2L))
                .thenReturn(false);
        when(classRepository.save(any(ClassEntity.class))).thenAnswer(inv -> {
            ClassEntity saved = inv.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        ClassResponse response = classService.createClass(1L, new CreateClassRequest("S1", 2L, 3L, null));

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.classLevelId()).isEqualTo(2L);
        assertThat(response.academicYearId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("should throw when class level is missing")
    void shouldThrowWhenClassLevelMissing() {
        when(classLevelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classService.createClass(1L, new CreateClassRequest("S1", 99L, 3L, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(classRepository, never()).save(any(ClassEntity.class));
    }

    @Test
    @DisplayName("should throw when academic year is missing")
    void shouldThrowWhenAcademicYearMissing() {
        when(classLevelRepository.findById(2L)).thenReturn(Optional.of(classLevel()));
        when(academicYearRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classService.createClass(1L, new CreateClassRequest("S1", 2L, 99L, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(classRepository, never()).save(any(ClassEntity.class));
    }

    private ClassEntity classEntity() {
        return ClassEntity.builder()
                .id(9L)
                .school(School.builder().id(1L).build())
                .classLevel(classLevel())
                .academicYear(academicYear())
                .name("S1")
                .isActive(true)
                .build();
    }

    private ClassLevel classLevel() {
        return ClassLevel.builder().id(2L).school(School.builder().id(1L).build()).name("S1").build();
    }

    private AcademicYear academicYear() {
        return AcademicYear.builder().id(3L).school(School.builder().id(1L).build()).year(2026).build();
    }
}
