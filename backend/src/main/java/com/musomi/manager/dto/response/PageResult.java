package com.musomi.manager.dto.response;

import java.util.List;

/**
 * Flat pagination envelope. Wrapped inside {@link ApiResponse#getData()} for
 * paginated list endpoints.
 *
 * <p>Serialised JSON shape for {@code GET /api/v1/admin/users}:</p>
 * <pre>
 * {
 *   "data": {
 *     "data": [ ... ],
 *     "page": 0,
 *     "size": 20,
 *     "total": 45,
 *     "totalPages": 3
 *   }
 * }
 * </pre>
 */
public record PageResult<T>(
        List<T> data,
        int page,
        int size,
        long total,
        int totalPages
) {
}