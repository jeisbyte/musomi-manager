package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.SaveCommentRequest;
import com.musomi.manager.dto.response.CommentResponse;
import com.musomi.manager.entity.Comment;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Student;
import com.musomi.manager.entity.Subject;
import com.musomi.manager.entity.Term;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.mapper.CommentMapper;
import com.musomi.manager.repository.CommentRepository;
import com.musomi.manager.repository.StudentRepository;
import com.musomi.manager.repository.SubjectRepository;
import com.musomi.manager.repository.TermRepository;
import com.musomi.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages student comments. */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final CommentRepository commentRepository;
    private final StudentRepository studentRepository;
    private final TermRepository termRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    /** Lists comments for a student and term. */
    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long studentId, Long termId) {
        return commentRepository.findByStudentIdAndTermIdOrderByWrittenAtDesc(studentId, termId)
                .stream()
                .map(CommentMapper::toResponse)
                .toList();
    }

    /** Lists comments for a student, term, and comment type. */
    @Transactional(readOnly = true)
    public List<CommentResponse> listByType(Long studentId, Long termId, String commentType) {
        return commentRepository.findByStudentIdAndTermIdAndCommentType(studentId, termId, commentType)
                .stream()
                .map(CommentMapper::toResponse)
                .toList();
    }

    /** Creates or updates the comment for the same student, term, subject, and type. */
    @Transactional
    public CommentResponse saveComment(Long schoolId, Long authorId, SaveCommentRequest request) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STUDENT_NOT_FOUND));
        if (student.getSchool() == null || !resolvedSchoolId.equals(student.getSchool().getId())) {
            // TODO: Use a dedicated comment/student ownership error code when one is available.
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        // TODO: Use a dedicated term-not-found error code for comments when one is available.
        Term term = termRepository.findById(request.termId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (term.getSchool() == null || !resolvedSchoolId.equals(term.getSchool().getId())) {
            // TODO: Use a dedicated comment/term ownership error code when one is available.
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }

        // TODO: Use a dedicated subject-not-found error code for comments when one is available.
        Subject subject = request.subjectId() == null
                ? null
                : subjectRepository.findById(request.subjectId())
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (subject != null && (subject.getSchool() == null
                || !resolvedSchoolId.equals(subject.getSchool().getId()))) {
            // TODO: Use a dedicated comment/subject ownership error code when one is available.
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        Comment comment = commentRepository
                .findByStudentIdAndTermIdAndCommentType(request.studentId(), request.termId(), request.commentType())
                .stream()
                .filter(existing -> sameSubject(existing.getSubject(), subject))
                .findFirst()
                .orElseGet(() -> Comment.builder()
                        .school(School.builder().id(resolvedSchoolId).build())
                        .student(student)
                        .term(term)
                        .subject(subject)
                        .commentType(request.commentType())
                        .writtenBy(author)
                        .writtenAt(LocalDateTime.now())
                        .build());

        boolean created = comment.getId() == null;
        comment.setCommentText(request.commentText());
        comment.setWrittenBy(author);
        if (!created) {
            comment.setUpdatedAt(LocalDateTime.now());
        }
        Comment saved = commentRepository.save(comment);
        log.info("Comment {} for student {} and term {}", created ? "created" : "updated",
                request.studentId(), request.termId());
        auditService.log("COMMENT_SAVED", "Comment", saved.getId(), authorId, resolvedSchoolId);
        return CommentMapper.toResponse(saved);
    }

    /** Deletes a comment by id. */
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                // TODO: Use a dedicated comment-not-found error code when one is available.
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        commentRepository.delete(comment);
        log.info("Comment {} deleted", id);
        // TODO(backend-lead): pass real userId once @CurrentUser is wired in services
        // TODO(backend-lead): pass real schoolId once school context is wired in services
        auditService.log("COMMENT_DELETED", "Comment", id, 1L, 1L);
    }

    private boolean sameSubject(Subject first, Subject second) {
        return first == null ? second == null
                : second != null && first.getId().equals(second.getId());
    }
}
