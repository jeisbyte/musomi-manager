package com.musomi.manager.service;

import java.util.Optional;

import com.musomi.manager.dto.request.UpdateSchoolSettingsRequest;
import com.musomi.manager.dto.response.SchoolSettingsResponse;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.SchoolSettings;
import com.musomi.manager.entity.Term;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.repository.SchoolRepository;
import com.musomi.manager.repository.SchoolSettingsRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolSettingsServiceTest {

    private static final Long SCHOOL_ID = 1L;
    private static final Long TERM_ID = 2L;

    @Mock
    private SchoolSettingsRepository schoolSettingsRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private TermRepository termRepository;

    @InjectMocks
    private SchoolSettingsService schoolSettingsService;

    @Test
    @DisplayName("should return settings when they exist")
    void shouldReturnSettingsWhenExists() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(school()));
        when(schoolSettingsRepository.findBySchoolId(SCHOOL_ID)).thenReturn(Optional.of(settings()));

        SchoolSettingsResponse response = schoolSettingsService.getSettings(SCHOOL_ID);

        assertThat(response.schoolName()).isEqualTo("Musomi School");
        assertThat(response.gradingScale()).isEqualTo("[{\"grade\":\"A\"}]");
        assertThat(response.currentTermId()).isEqualTo(TERM_ID);
    }

    @Test
    @DisplayName("should return defaults when settings are missing")
    void shouldReturnDefaultsWhenMissing() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(school()));
        when(schoolSettingsRepository.findBySchoolId(SCHOOL_ID)).thenReturn(Optional.empty());

        SchoolSettingsResponse response = schoolSettingsService.getSettings(SCHOOL_ID);

        assertThat(response.schoolName()).isEqualTo("Musomi School");
        assertThat(response.gradingScale()).contains("\"grade\":\"A\"");
        assertThat(response.id()).isNull();
        verify(schoolSettingsRepository).findBySchoolId(SCHOOL_ID);
    }

    @Test
    @DisplayName("should update existing settings")
    void shouldUpdateExistingSettings() {
        SchoolSettings existing = settings();
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(school()));
        when(termRepository.findById(TERM_ID)).thenReturn(Optional.of(term()));
        when(schoolSettingsRepository.findBySchoolId(SCHOOL_ID)).thenReturn(Optional.of(existing));
        when(schoolSettingsRepository.save(any(SchoolSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UpdateSchoolSettingsRequest request = new UpdateSchoolSettingsRequest(
                "{\"grade\":\"B\"}", "Header", "Footer", TERM_ID);

        SchoolSettingsResponse response = schoolSettingsService.updateSettings(SCHOOL_ID, request);

        assertThat(response.gradingScale()).isEqualTo("{\"grade\":\"B\"}");
        assertThat(response.reportHeader()).isEqualTo("Header");
        assertThat(response.currentTermId()).isEqualTo(TERM_ID);
        verify(schoolSettingsRepository).save(existing);
    }

    @Test
    @DisplayName("should create settings when missing")
    void shouldCreateWhenMissing() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(school()));
        when(schoolSettingsRepository.findBySchoolId(SCHOOL_ID)).thenReturn(Optional.empty());
        when(schoolSettingsRepository.save(any(SchoolSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UpdateSchoolSettingsRequest request = new UpdateSchoolSettingsRequest(
                "{\"grade\":\"A\"}", "Header", null, null);

        SchoolSettingsResponse response = schoolSettingsService.updateSettings(SCHOOL_ID, request);

        assertThat(response.schoolId()).isEqualTo(SCHOOL_ID);
        assertThat(response.gradingScale()).isEqualTo("{\"grade\":\"A\"}");
        assertThat(response.reportHeader()).isEqualTo("Header");
        verify(schoolSettingsRepository).save(any(SchoolSettings.class));
    }

    @Test
    @DisplayName("should throw when current term not found")
    void shouldThrowWhenCurrentTermNotFound() {
        when(schoolRepository.findById(SCHOOL_ID)).thenReturn(Optional.of(school()));
        when(termRepository.findById(TERM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schoolSettingsService.updateSettings(
                SCHOOL_ID, new UpdateSchoolSettingsRequest(null, null, null, TERM_ID)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    private SchoolSettings settings() {
        return SchoolSettings.builder()
                .id(3L)
                .school(school())
                .gradingScale("[{\"grade\":\"A\"}]")
                .currentTerm(term())
                .build();
    }

    private Term term() {
        return Term.builder().id(TERM_ID).school(school()).name("Term 1").build();
    }

    private School school() {
        return School.builder()
                .id(SCHOOL_ID)
                .name("Musomi School")
                .logoUrl("/logo.png")
                .address("Kampala")
                .phone("+256700000000")
                .email("school@example.com")
                .build();
    }
}
