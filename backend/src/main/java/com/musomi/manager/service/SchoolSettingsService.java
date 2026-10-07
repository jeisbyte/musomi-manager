package com.musomi.manager.service;

import java.time.LocalDateTime;

import com.musomi.manager.dto.request.UpdateSchoolSettingsRequest;
import com.musomi.manager.dto.response.SchoolSettingsResponse;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.SchoolSettings;
import com.musomi.manager.entity.Term;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.mapper.SchoolSettingsMapper;
import com.musomi.manager.repository.SchoolRepository;
import com.musomi.manager.repository.SchoolSettingsRepository;
import com.musomi.manager.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages per-school settings. */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolSettingsService {

    private static final long DEFAULT_SCHOOL_ID = 1L;
    private static final String DEFAULT_GRADING_SCALE = """
            [{"grade":"A","min":80,"max":100},{"grade":"B","min":70,"max":79},\
            {"grade":"C","min":60,"max":69},{"grade":"D","min":50,"max":59},\
            {"grade":"E","min":40,"max":49},{"grade":"F","min":0,"max":39}]
            """.replace("\\\n", "").trim();

    private final SchoolSettingsRepository schoolSettingsRepository;
    private final SchoolRepository schoolRepository;
    private final TermRepository termRepository;

    /** Returns school settings, using defaults without persisting a missing settings row. */
    @Transactional(readOnly = true)
    public SchoolSettingsResponse getSettings(Long schoolId) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        School school = findSchool(resolvedSchoolId);
        SchoolSettings settings = schoolSettingsRepository.findBySchoolId(resolvedSchoolId).orElse(null);
        SchoolSettingsResponse response = SchoolSettingsMapper.toResponse(settings, school);
        if (settings == null) {
            return new SchoolSettingsResponse(
                    response.id(),
                    response.schoolId(),
                    response.schoolName(),
                    response.logoUrl(),
                    response.address(),
                    response.phone(),
                    response.email(),
                    DEFAULT_GRADING_SCALE,
                    response.reportHeader(),
                    response.reportFooter(),
                    response.currentTermId(),
                    response.currentAcademicYearId());
        }
        return response;
    }

    /** Creates or updates school settings. */
    @Transactional
    public SchoolSettingsResponse updateSettings(Long schoolId, UpdateSchoolSettingsRequest request) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        School school = findSchool(resolvedSchoolId);
        Term currentTerm = findCurrentTerm(request.currentTermId(), resolvedSchoolId);
        SchoolSettings settings = schoolSettingsRepository.findBySchoolId(resolvedSchoolId)
                .orElseGet(() -> SchoolSettings.builder()
                        .school(school)
                        .gradingScale(DEFAULT_GRADING_SCALE)
                        .createdAt(LocalDateTime.now())
                        .build());

        if (request.gradingScale() != null) {
            settings.setGradingScale(request.gradingScale());
        }
        settings.setReportHeader(request.reportHeader());
        settings.setReportFooter(request.reportFooter());
        settings.setCurrentTerm(currentTerm);
        settings.setUpdatedAt(LocalDateTime.now());

        SchoolSettings saved = schoolSettingsRepository.save(settings);
        log.info("School settings updated for school {}", resolvedSchoolId);
        return SchoolSettingsMapper.toResponse(saved, school);
    }

    private School findSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                // TODO: Use a dedicated school-not-found error code when one is available.
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
    }

    private Term findCurrentTerm(Long termId, Long schoolId) {
        if (termId == null) {
            return null;
        }
        // TODO: Use a dedicated term-not-found error code for school settings when one is available.
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (term.getSchool() == null || !schoolId.equals(term.getSchool().getId())) {
            // TODO: Use a dedicated school-settings validation error code when one is available.
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        return term;
    }
}
