package com.musomi.manager.entity;

import java.math.BigDecimal;
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

/** An audit record of a score change. */
@Entity
@Table(name = "score_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"school", "score", "changedBy"})
@EqualsAndHashCode(exclude = {"school", "score", "changedBy"})
public class ScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id", nullable = false)
    private Score score;

    @Column(name = "old_score", nullable = true, precision = 6, scale = 2)
    private BigDecimal oldScore;

    @Column(name = "new_score", nullable = true, precision = 6, scale = 2)
    private BigDecimal newScore;

    @Column(name = "old_feedback", nullable = true, length = 200)
    private String oldFeedback;

    @Column(name = "new_feedback", nullable = true, length = 200)
    private String newFeedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @Column(name = "reason", nullable = true, length = 200)
    private String reason;
}
