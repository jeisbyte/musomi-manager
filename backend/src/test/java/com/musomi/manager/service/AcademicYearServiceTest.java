package com.musomi.manager.service;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.request.CreateAcademicYearRequest;
import com.musomi.manager.dto.response.AcademicYearResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.School;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.AcademicYearRepository;
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
class AcademicYearServiceTest {

    @Mock
    private AcademicYearRepository academicYearRepository;

    @InjectMocks
    private AcademicYearService academicYearService;

    @Test
    @DisplayName("should return years when listed")
    void shouldReturnYearsWhenListed() {
        when(academicYearRepository.findBySchoolIdOrderByYearDesc(1L))
                .thenReturn(List.of(academicYear(2026, false)));

        List<AcademicYearResponse> years = academicYearService.listYears(1L);

        assertThat(years).hasSize(1);
        assertThat(years.getFirst().year()).isEqualTo(2026);
    }

    @Test
    @DisplayName("should create year when it does not exist")
    void shouldCreateYearWhenNotExists() {
        when(academicYearRepository.existsBySchoolIdAndYear(1L, 2027)).thenReturn(false);
        when(academicYearRepository.save(any(AcademicYear.class))).thenAnswer(inv -> {
            AcademicYear year = inv.getArgument(0);
            year.setId(5L);
            return year;
        });

        AcademicYearResponse response = academicYearService.createYear(1L, new CreateAcademicYearRequest(2027));

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.year()).isEqualTo(2027);
        verify(academicYearRepository).save(any(AcademicYear.class));
    }

    @Test
    @DisplayName("should throw when year already exists")
    void shouldThrowWhenYearAlreadyExists() {
        when(academicYearRepository.existsBySchoolIdAndYear(1L, 2026)).thenReturn(true);

        assertThatThrownBy(() -> academicYearService.createYear(1L, new CreateAcademicYearRequest(2026)))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(academicYearRepository, never()).save(any(AcademicYear.class));
    }

    @Test
    @DisplayName("should set current and clear others")
    void shouldSetCurrentAndClearOthers() {
        AcademicYear year = academicYear(2026, false);
        when(academicYearRepository.findById(5L)).thenReturn(Optional.of(year));
        when(academicYearRepository.save(any(AcademicYear.class))).thenAnswer(inv -> inv.getArgument(0));

        AcademicYearResponse response = academicYearService.setCurrent(5L, 1L);

        assertThat(response.isCurrent()).isTrue();
        verify(academicYearRepository).clearCurrentFlag(1L);
        verify(academicYearRepository).save(year);
    }

    @Test
    @DisplayName("should throw when setting a missing year current")
    void shouldThrowWhenSetCurrentNotFound() {
        when(academicYearRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicYearService.setCurrent(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(academicYearRepository, never()).clearCurrentFlag(any());
    }

    private AcademicYear academicYear(int year, boolean current) {
        return AcademicYear.builder()
                .id(5L)
                .school(School.builder().id(1L).build())
                .year(year)
                .isCurrent(current)
                .build();
    }
}
