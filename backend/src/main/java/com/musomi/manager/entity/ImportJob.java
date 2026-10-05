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

/** Tracks the status and results of an Excel import. */
@Entity
@Table(name = "import_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"school", "createdBy"})
@EqualsAndHashCode(exclude = {"school", "createdBy"})
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "job_reference", nullable = false, length = 50)
    private String jobReference;

    @Column(name = "import_type", nullable = false, length = 20)
    private String importType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Builder.Default
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows = 0;

    @Builder.Default
    @Column(name = "valid_rows", nullable = false)
    private Integer validRows = 0;

    @Builder.Default
    @Column(name = "error_rows", nullable = false)
    private Integer errorRows = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "errors_json", nullable = true, columnDefinition = "jsonb")
    private String errorsJson;

    @Column(name = "original_filename", nullable = true, length = 255)
    private String originalFilename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at", nullable = true)
    private LocalDateTime confirmedAt;
}
