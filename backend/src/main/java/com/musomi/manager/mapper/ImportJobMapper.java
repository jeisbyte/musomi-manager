package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.ImportJobResponse;
import com.musomi.manager.entity.ImportJob;

/** Maps import jobs to response DTOs. */
public final class ImportJobMapper {

    private ImportJobMapper() {
    }

    /** Maps an import job to its response DTO. */
    public static ImportJobResponse toResponse(ImportJob entity) {
        if (entity == null) {
            return null;
        }
        return new ImportJobResponse(
                entity.getId(),
                entity.getJobReference(),
                entity.getImportType(),
                entity.getStatus(),
                entity.getTotalRows(),
                entity.getValidRows(),
                entity.getErrorRows(),
                entity.getOriginalFilename(),
                entity.getCreatedAt(),
                entity.getConfirmedAt()
        );
    }

    /** Maps import jobs to response DTOs. */
    public static List<ImportJobResponse> toResponseList(List<ImportJob> entities) {
        return entities.stream()
                .map(ImportJobMapper::toResponse)
                .toList();
    }
}
