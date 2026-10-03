package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.TermResponse;
import com.musomi.manager.entity.Term;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps term entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TermMapper {

    /** Maps a term to its response DTO. */
    public static TermResponse toResponse(Term term) {
        if (term == null) {
            return null;
        }
        return new TermResponse(
                term.getId(),
                term.getAcademicYear() != null ? term.getAcademicYear().getId() : null,
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getIsCurrent(),
                term.getCreatedAt()
        );
    }

    /** Maps a list of terms to response DTOs. */
    public static List<TermResponse> toResponseList(List<Term> terms) {
        return terms.stream()
                .map(TermMapper::toResponse)
                .toList();
    }
}
