package com.musomi.manager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.request.CreateAssessmentRequest;
import com.musomi.manager.dto.request.UpdateAssessmentRequest;
import com.musomi.manager.dto.response.AssessmentResponse;
import com.musomi.manager.entity.Assessment;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Subject;
import com.musomi.manager.entity.Term;
import com.musomi.manager.entity.Topic;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.AssessmentRepository;
import com.musomi.manager.repository.AssessmentTopicRepository;
import com.musomi.manager.repository.ClassRepository;
import com.musomi.manager.repository.ScoreRepository;
import com.musomi.manager.repository.StreamRepository;
import com.musomi.manager.repository.StudentRepository;
import com.musomi.manager.repository.SubjectRepository;
import com.musomi.manager.repository.TermRepository;
import com.musomi.manager.repository.TopicRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    private static final Long SCHOOL_ID = 1L;
    private static final Long TEACHER_ID = 2L;
    private static final Long CLASS_ID = 3L;
    private static final Long SUBJECT_ID = 4L;
    private static final Long TERM_ID = 5L;
    private static final Long TOPIC_ID = 6L;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private AssessmentTopicRepository assessmentTopicRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StreamRepository streamRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TermRepository termRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AssessmentService assessmentService;

    @Test
    @DisplayName("should list assessments when teacher has them")
    void shouldListAssessmentsWhenTeacherHasThem() {
        Assessment assessment = assessment("DRAFT");
        when(assessmentRepository.findAll()).thenReturn(List.of(assessment));

        List<AssessmentResponse> result = assessmentService.listAssessments(TEACHER_ID, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Mid-Term Exam");
    }

    @Test
    @DisplayName("should create assessment when request valid")
    void shouldCreateAssessmentWhenRequestValid() {
        stubCreateParents();
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> {
            Assessment saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });
        when(studentRepository.findByCurrentClassIdAndIsActiveTrue(CLASS_ID)).thenReturn(List.of());

        AssessmentResponse response = assessmentService.createAssessment(SCHOOL_ID, TEACHER_ID, createRequest());

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.title()).isEqualTo("Mid-Term Exam");
        verify(assessmentTopicRepository).saveAll(any());
    }

    @Test
    @DisplayName("should throw when topic ids empty")
    void shouldThrowWhenTopicIdsEmpty() {
        CreateAssessmentRequest request = new CreateAssessmentRequest(
                CLASS_ID, null, SUBJECT_ID, TERM_ID, "Mid-Term Exam", "EXAM",
                LocalDate.of(2026, 3, 15), new BigDecimal("100"), List.of());

        assertThatThrownBy(() -> assessmentService.createAssessment(SCHOOL_ID, TEACHER_ID, request))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSESSMENT_TOPIC_REQUIRED);
    }

    @Test
    @DisplayName("should throw when class missing")
    void shouldThrowWhenClassMissing() {
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assessmentService.createAssessment(SCHOOL_ID, TEACHER_ID, createRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("should throw when subject missing")
    void shouldThrowWhenSubjectMissing() {
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity()));
        when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assessmentService.createAssessment(SCHOOL_ID, TEACHER_ID, createRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("should throw when term missing")
    void shouldThrowWhenTermMissing() {
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity()));
        when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.of(subject()));
        when(termRepository.findById(TERM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assessmentService.createAssessment(SCHOOL_ID, TEACHER_ID, createRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("should get assessment when it exists")
    void shouldGetAssessmentWhenExists() {
        when(assessmentRepository.findById(8L)).thenReturn(Optional.of(assessment("DRAFT")));

        AssessmentResponse response = assessmentService.getAssessment(8L);

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.title()).isEqualTo("Mid-Term Exam");
    }

    @Test
    @DisplayName("should throw when assessment not found")
    void shouldThrowWhenAssessmentNotFound() {
        when(assessmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assessmentService.getAssessment(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSESSMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("should throw when updating published")
    void shouldThrowWhenUpdatingPublished() {
        when(assessmentRepository.findById(8L)).thenReturn(Optional.of(assessment("PUBLISHED")));

        assertThatThrownBy(() -> assessmentService.updateAssessment(8L, updateRequest()))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSESSMENT_ALREADY_PUBLISHED);
    }

    @Test
    @DisplayName("should throw when publishing non-draft")
    void shouldThrowWhenPublishingNonDraft() {
        when(assessmentRepository.findById(8L)).thenReturn(Optional.of(assessment("PUBLISHED")));

        assertThatThrownBy(() -> assessmentService.publishAssessment(8L, TEACHER_ID))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSESSMENT_ALREADY_PUBLISHED);
    }

    @Test
    @DisplayName("should throw when publishing with empty scores")
    void shouldThrowWhenPublishingWithEmptyScores() {
        when(assessmentRepository.findById(8L)).thenReturn(Optional.of(assessment("DRAFT")));
        when(scoreRepository.countByAssessmentIdAndScoreIsNull(8L)).thenReturn(2L);

        assertThatThrownBy(() -> assessmentService.publishAssessment(8L, TEACHER_ID))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_PUBLISH_EMPTY);
    }

    @Test
    @DisplayName("should set published status when all scores entered")
    void shouldSetPublishedStatusWhenAllScoresEntered() {
        Assessment assessment = assessment("DRAFT");
        when(assessmentRepository.findById(8L)).thenReturn(Optional.of(assessment));
        when(scoreRepository.countByAssessmentIdAndScoreIsNull(8L)).thenReturn(0L);
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assessmentService.publishAssessment(8L, TEACHER_ID);

        assertThat(assessment.getStatus()).isEqualTo("PUBLISHED");
        assertThat(assessment.getPublishedAt()).isNotNull();
        verify(assessmentRepository).save(assessment);
    }

    private void stubCreateParents() {
        when(userRepository.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(classEntity()));
        when(subjectRepository.findById(SUBJECT_ID)).thenReturn(Optional.of(subject()));
        when(termRepository.findById(TERM_ID)).thenReturn(Optional.of(term()));
        when(topicRepository.findById(TOPIC_ID)).thenReturn(Optional.of(
                Topic.builder().id(TOPIC_ID).school(school()).name("Algebra").build()));
    }

    private CreateAssessmentRequest createRequest() {
        return new CreateAssessmentRequest(
                CLASS_ID, null, SUBJECT_ID, TERM_ID, "Mid-Term Exam", "EXAM",
                LocalDate.of(2026, 3, 15), new BigDecimal("100"), List.of(TOPIC_ID));
    }

    private UpdateAssessmentRequest updateRequest() {
        return new UpdateAssessmentRequest(
                null, TERM_ID, "Mid-Term Exam", "EXAM",
                LocalDate.of(2026, 3, 15), new BigDecimal("100"), List.of(TOPIC_ID));
    }

    private Assessment assessment(String status) {
        return Assessment.builder()
                .id(8L)
                .school(school())
                .teacher(teacher())
                .classEntity(classEntity())
                .subject(subject())
                .term(term())
                .title("Mid-Term Exam")
                .type("EXAM")
                .assessmentDate(LocalDate.of(2026, 3, 15))
                .maxScore(new BigDecimal("100"))
                .status(status)
                .build();
    }

    private ClassEntity classEntity() {
        return ClassEntity.builder().id(CLASS_ID).school(school()).name("S3").build();
    }

    private Subject subject() {
        return Subject.builder().id(SUBJECT_ID).school(school()).name("Mathematics").build();
    }

    private Term term() {
        return Term.builder().id(TERM_ID).school(school()).name("Term 1").build();
    }

    private User teacher() {
        return User.builder().id(TEACHER_ID).fullName("Taylor Teacher").build();
    }

    private School school() {
        return School.builder().id(SCHOOL_ID).build();
    }
}
