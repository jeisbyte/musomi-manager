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

/** A teacher comment for a student and term. */
@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"school", "student", "term", "subject", "writtenBy"})
@EqualsAndHashCode(exclude = {"school", "student", "term", "subject", "writtenBy"})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = true)
    private Subject subject;

    @Column(name = "comment_type", nullable = false, length = 30)
    private String commentType;

    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "written_by", nullable = false)
    private User writtenBy;

    @Column(name = "written_at", nullable = false, updatable = false)
    private LocalDateTime writtenAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;
}
