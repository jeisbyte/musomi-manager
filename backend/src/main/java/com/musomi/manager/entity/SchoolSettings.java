package com.musomi.manager.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Per-school configuration for reports and grading. */
@Entity
@Table(name = "school_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"school", "currentTerm"})
@EqualsAndHashCode(exclude = {"school", "currentTerm"})
public class SchoolSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "grading_scale", nullable = false, columnDefinition = "jsonb")
    private String gradingScale;

    @Column(name = "report_header", nullable = true, columnDefinition = "TEXT")
    private String reportHeader;

    @Column(name = "report_footer", nullable = true, columnDefinition = "TEXT")
    private String reportFooter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_term_id", nullable = true)
    private Term currentTerm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
}
