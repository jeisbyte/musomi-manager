package com.musomi.manager.service;

import java.util.List;

import com.musomi.manager.dto.request.CreateClassLevelRequest;
import com.musomi.manager.dto.response.ClassLevelResponse;
import com.musomi.manager.entity.ClassLevel;
import com.musomi.manager.entity.School;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.ClassLevelRepository;
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
class ClassLevelServiceTest {

    @Mock
    private ClassLevelRepository classLevelRepository;

    @InjectMocks
    private ClassLevelService classLevelService;

    @Test
    @DisplayName("should return levels when listed")
    void shouldReturnLevelsWhenListed() {
        when(classLevelRepository.findBySchoolIdAndIsActiveTrueOrderBySortOrderAsc(1L))
                .thenReturn(List.of(classLevel()));

        List<ClassLevelResponse> levels = classLevelService.listLevels(1L);

        assertThat(levels).hasSize(1);
        assertThat(levels.getFirst().name()).isEqualTo("S1");
    }

    @Test
    @DisplayName("should create level when name is available")
    void shouldCreateLevelWhenNameAvailable() {
        when(classLevelRepository.existsBySchoolIdAndName(1L, "S2")).thenReturn(false);
        when(classLevelRepository.save(any(ClassLevel.class))).thenAnswer(inv -> {
            ClassLevel level = inv.getArgument(0);
            level.setId(2L);
            return level;
        });

        ClassLevelResponse response = classLevelService.createLevel(1L, new CreateClassLevelRequest("S2", 2));

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.name()).isEqualTo("S2");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("should throw when name already exists")
    void shouldThrowWhenNameAlreadyExists() {
        when(classLevelRepository.existsBySchoolIdAndName(1L, "S1")).thenReturn(true);

        assertThatThrownBy(() -> classLevelService.createLevel(1L, new CreateClassLevelRequest("S1", 1)))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(classLevelRepository, never()).save(any(ClassLevel.class));
    }

    private ClassLevel classLevel() {
        return ClassLevel.builder()
                .id(1L)
                .school(School.builder().id(1L).build())
                .name("S1")
                .sortOrder(1)
                .isActive(true)
                .build();
    }
}
