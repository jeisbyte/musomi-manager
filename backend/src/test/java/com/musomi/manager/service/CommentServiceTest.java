package com.musomi.manager.service;

import java.util.List;
import java.util.Optional;

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
import com.musomi.manager.repository.CommentRepository;
import com.musomi.manager.repository.StudentRepository;
import com.musomi.manager.repository.SubjectRepository;
import com.musomi.manager.repository.TermRepository;
import com.musomi.manager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final Long SCHOOL_ID = 1L;
    private static final Long STUDENT_ID = 2L;
    private static final Long TERM_ID = 3L;
    private static final Long SUBJECT_ID = 4L;
    private static final Long AUTHOR_ID = 5L;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private TermRepository termRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("should list comments for student")
    void shouldListCommentsForStudent() {
        when(commentRepository.findByStudentIdAndTermIdOrderByWrittenAtDesc(STUDENT_ID, TERM_ID))
                .thenReturn(List.of(comment()));

        List<CommentResponse> response = commentService.listComments(STUDENT_ID, TERM_ID);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).commentText()).isEqualTo("Good improvement");
        assertThat(response.get(0).subjectName()).isEqualTo("Mathematics");
    }

    @Test
    @DisplayName("should save new comment when none exists")
    void shouldSaveNewCommentWhenNoneExists() {
        stubValidParents();
        when(commentRepository.findByStudentIdAndTermIdAndCommentType(
                STUDENT_ID, TERM_ID, "SUBJECT")).thenReturn(List.of());
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        CommentResponse response = commentService.saveComment(SCHOOL_ID, AUTHOR_ID, request());

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.commentText()).isEqualTo("Good improvement");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("should update comment when same type and subject exists")
    void shouldUpdateCommentWhenSameTypeAndSubjectExists() {
        stubValidParents();
        Comment existing = comment();
        existing.setId(8L);
        when(commentRepository.findByStudentIdAndTermIdAndCommentType(
                STUDENT_ID, TERM_ID, "SUBJECT")).thenReturn(List.of(existing));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = commentService.saveComment(
                SCHOOL_ID, AUTHOR_ID, new SaveCommentRequest(
                        STUDENT_ID, TERM_ID, SUBJECT_ID, "SUBJECT", "Updated comment"));

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.commentText()).isEqualTo("Updated comment");
        assertThat(existing.getUpdatedAt()).isNotNull();
        verify(commentRepository).save(existing);
    }

    @Test
    @DisplayName("should throw when student missing")
    void shouldThrowWhenStudentMissing() {
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.saveComment(SCHOOL_ID, AUTHOR_ID, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDENT_NOT_FOUND);
    }

    @Test
    @DisplayName("should throw when term missing")
    void shouldThrowWhenTermMissing() {
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student()));
        when(termRepository.findById(TERM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.saveComment(SCHOOL_ID, AUTHOR_ID, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("should delete comment when it exists")
    void shouldDeleteCommentWhenExists() {
        when(commentRepository.findById(9L)).thenReturn(Optional.of(comment()));

        commentService.deleteComment(9L);

        verify(commentRepository).delete(any(Comment.class));
    }

    @Test
    @DisplayName("should throw when deleting missing comment")
    void shouldThrowWhenDeletingMissingComment() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    private void stubValidParents() {
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student()));
        when(termRepository.findById(TERM_ID)).thenReturn(Optional.of(term()));
        when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.of(subject()));
        when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(
                User.builder().id(AUTHOR_ID).fullName("Teacher").build()));
    }

    private SaveCommentRequest request() {
        return new SaveCommentRequest(STUDENT_ID, TERM_ID, SUBJECT_ID, "SUBJECT", "Good improvement");
    }

    private Comment comment() {
        return Comment.builder()
                .id(6L)
                .school(school())
                .student(student())
                .term(term())
                .subject(subject())
                .commentType("SUBJECT")
                .commentText("Good improvement")
                .writtenBy(User.builder().id(AUTHOR_ID).fullName("Teacher").build())
                .build();
    }

    private Student student() {
        return Student.builder().id(STUDENT_ID).school(school()).fullName("Alex Student").build();
    }

    private Term term() {
        return Term.builder().id(TERM_ID).school(school()).name("Term 1").build();
    }

    private Subject subject() {
        return Subject.builder().id(SUBJECT_ID).school(school()).name("Mathematics").build();
    }

    private School school() {
        return School.builder().id(SCHOOL_ID).build();
    }
}
