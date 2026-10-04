package com.musomi.desktop.api;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musomi.desktop.model.dto.TeacherClassResponse;

/**
 * API client for teacher-facing class endpoints.
 *
 * <p>Covers the endpoint defined in API_CONTRACT.md section 12:
 * <ul>
 *   <li>GET /teacher/classes — list classes assigned to the authenticated teacher</li>
 * </ul>
 *
 * <p>Uses the {@link ApiClient} singleton for all HTTP communication.
 * The JWT token is injected automatically by {@link ApiClient} from the active {@link com.musomi.desktop.config.Session}.
 */
public class TeacherClassApi {

    private static final Logger log = LoggerFactory.getLogger(TeacherClassApi.class);

    private static final String CLASSES_PATH = "/teacher/classes";

    private final ApiClient client;

    /**
     * Creates a {@code TeacherClassApi} backed by the shared {@link ApiClient} singleton.
     */
    public TeacherClassApi() {
        this.client = ApiClient.getInstance();
    }

    // -------------------------------------------------------------------------
    // Endpoints
    // -------------------------------------------------------------------------

    /**
     * Returns the list of class assignments for the currently authenticated teacher.
     *
     * <p>Corresponds to: {@code GET /teacher/classes}
     *
     * @return list of {@link TeacherClassResponse} entries; never null, may be empty
     * @throws ApiException on HTTP 401 (session expired), 403 (not a teacher), or network error
     */
    public List<TeacherClassResponse> getMyClasses() {
        log.debug("Fetching teacher classes from {}", CLASSES_PATH);
        TeacherClassResponse[] array = client.get(CLASSES_PATH, TeacherClassResponse[].class);
        if (array == null) {
            return List.of();
        }
        return Arrays.asList(array);
    }
}
