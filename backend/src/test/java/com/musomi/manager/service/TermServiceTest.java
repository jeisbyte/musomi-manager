package com.musomi.manager.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.request.CreateTermRequest;
import com.musomi.manager.dto.response.TermResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Term;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.repository.AcademicYearRepository;
import com.musomi.manager.repository.TermRepository;
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
class TermServiceTest {

    @Mock
    private TermRepository termRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @InjectMocks
    private TermService termService;

    @Test
    @DisplayName("should return terms when listed")
    void shouldReturnTermsWhenListed() {
        when(termRepository.findBySchoolIdOrderByStartDateDesc(1L)).thenReturn(List.of(term(false)));

        List<TermResponse> terms = termService.listTerms(1L, null);

        assertThat(terms).hasSize(1);
        assertThat(terms.getFirst().name()).isEqualTo("Term 1");
    }

    @Test
    @DisplayName("should create term when parent academic year exists")
    void shouldCreateTermWhenParentExists() {
        AcademicYear year = academicYear();
        when(academicYearRepository.findById(3L)).thenReturn(Optional.of(year));
        when(termRepository.existsByAcademicYearIdAndName(3L, "Term 1")).thenReturn(false);
        when(termRepository.save(any(Term.class))).thenAnswer(inv -> {
            Term saved = inv.getArgument(0);
            saved.setId(8L);
            return saved;
        });

        TermResponse response = termService.createTerm(1L,
                new CreateTermRequest(3L, "Term 1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1)));

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.academicYearId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("should throw when parent academic year is missing")
    void shouldThrowWhenParentAcademicYearMissing() {
        when(academicYearRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> termService.createTerm(1L,
                new CreateTermRequest(99L, "Term 1", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 5, 1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(termRepository, never()).save(any(Term.class));
    }

    @Test
    @DisplayName("should set current and clear others")
    void shouldSetCurrentAndClearOthers() {
        Term target = term(false);
        when(termRepository.findById(8L)).thenReturn(Optional.of(target));
        when(termRepository.save(any(Term.class))).thenAnswer(inv -> inv.getArgument(0));

        TermResponse response = termService.setCurrent(8L, 1L);

        assertThat(response.isCurrent()).isTrue();
        verify(termRepository).clearCurrentFlag(1L);
        verify(termRepository).save(target);
    }

    private Term term(boolean current) {
        return Term.builder()
                .id(8L)
                .school(School.builder().id(1L).build())
                .academicYear(academicYear())
                .name("Term 1")
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 5, 1))
                .isCurrent(current)
                .build();
    }

    private AcademicYear academicYear() {
        return AcademicYear.builder().id(3L).school(School.builder().id(1L).build()).year(2026).build();
    }
}
