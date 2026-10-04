package com.musomi.desktop.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musomi.desktop.api.TeacherClassApi;
import com.musomi.desktop.model.dto.TeacherClassResponse;

/**
 * Client-side service for teacher class operations.
 *
 * <p>Orchestrates calls to {@link TeacherClassApi} and exposes data suitable
 * for JavaFX controllers. Controllers must never call {@link TeacherClassApi}
 * directly — always go through this service.
 *
 * <p>Layer diagram (DESKTOP.md §4):
 * <pre>
 *   Controller → TeacherClassService → TeacherClassApi → ApiClient → Backend
 * </pre>
 */
public class TeacherClassService {

    private static final Logger log = LoggerFactory.getLogger(TeacherClassService.class);

    private final TeacherClassApi api;

    /**
     * Creates a {@code TeacherClassService} using the provided API collaborator.
     *
     * @param api teacher class API wrapper
     */
    public TeacherClassService(TeacherClassApi api) {
        this.api = api;
    }

    /**
     * Convenience constructor that wires up the singleton automatically.
     */
    public TeacherClassService() {
        this(new TeacherClassApi());
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the list of class assignments for the currently authenticated teacher.
     *
     * <p>Calls {@code GET /teacher/classes} via {@link TeacherClassApi#getMyClasses()}.
     *
     * @return list of {@link TeacherClassResponse} entries; never null, may be empty
     * @throws com.musomi.desktop.api.ApiException on HTTP 401, 403, or network error
     */
    public List<TeacherClassResponse> getMyClasses() {
        log.debug("TeacherClassService.getMyClasses");
        return api.getMyClasses();
    }
}
