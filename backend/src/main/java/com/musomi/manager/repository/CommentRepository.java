package com.musomi.manager.repository;

import java.util.List;

import com.musomi.manager.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByStudentIdAndTermIdOrderByWrittenAtDesc(Long studentId, Long termId);

    List<Comment> findByStudentIdAndTermIdAndCommentType(Long studentId, Long termId, String type);

    List<Comment> findByStudentIdAndTermIdAndSubjectId(Long studentId, Long termId, Long subjectId);
}
