package com.musomi.manager.dto.response;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        Long studentId,
        String studentName,
        Long termId,
        String termName,
        Integer academicYear,
        String filePath,
        Long fileSizeBytes,
        LocalDateTime generatedAt,
        String downloadUrl
) {
}
