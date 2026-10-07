package com.musomi.manager.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.request.SaveMarksBatchRequest;
import com.musomi.manager.dto.request.SaveMarksRequest;
import com.musomi.manager.dto.response.MarkGridResponse;
import com.musomi.manager.dto.response.ScoreResponse;
import com.musomi.manager.entity.Assessment;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.Score;
import com.musomi.manager.entity.Student;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.PermissionDeniedException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.AssessmentRepository;
import com.musomi.manager.repository.AssessmentTopicRepository;
import com.musomi.manager.repository.ScoreHistoryRepository;
import com.musomi.manager.repository.ScoreRepository;
import com.musomi.manager.repository.StudentRepository;
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
class ScoreServiceTest {

    private static final Long SCORE_ID = 10L;
    private static final Long ASSESSMENT_ID = 20L;
    private static final Long STUDENT_ID = 30L;
    private static final Long TEACHER_ID = 40L;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private ScoreHistoryRepository scoreHistoryRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AssessmentTopicRepository assessmentTopicRepository;

    @InjectMocks
    private ScoreService scoreService;

    @Test
    @DisplayName("should update score when within range")
    void shouldUpdateScoreWhenWithinRange() {
        Score score = score(new BigDecimal("40"));
        when(scoreRepository.findById(SCORE_ID)).thenReturn(Optional.of(score));
        when(scoreRepository.save(any(Score.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScoreResponse response = scoreService.updateScore(
                SCORE_ID, new SaveMarksRequest(new BigDecimal("82"), "Good work"), TEACHER_ID);

        assertThat(response.score()).isEqualTo(new BigDecimal("82"));
        assertThat(response.feedback()).isEqualTo("Good work");
        verify(scoreRepository).save(score);
    }

    @Test
    @DisplayName("should throw when score exceeds max")
    void shouldThrowWhenScoreExceedsMax() {
        when(scoreRepository.findById(SCORE_ID)).thenReturn(Optional.of(score(new BigDecimal("40"))));

        assertThatThrownBy(() -> scoreService.updateScore(
                SCORE_ID, new SaveMarksRequest(new BigDecimal("101"), null), TEACHER_ID))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_EXCEEDS_MAX);
    }

    @Test
    @DisplayName("should throw when score below min")
    void shouldThrowWhenScoreBelowMin() {
        when(scoreRepository.findById(SCORE_ID)).thenReturn(Optional.of(score(new BigDecimal("40"))));

        assertThatThrownBy(() -> scoreService.updateScore(
                SCORE_ID, new SaveMarksRequest(new BigDecimal("-1"), null), TEACHER_ID))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SCORE_BELOW_MIN);
    }

    @Test
    @DisplayName("should throw when teacher does not own assessment")
    void shouldThrowWhenTeacherDoesNotOwnAssessment() {
        Score score = score(new BigDecimal("40"));
        score.getAssessment().setTeacher(User.builder().id(99L).build());
        when(scoreRepository.findById(SCORE_ID)).thenReturn(Optional.of(score));

        assertThatThrownBy(() -> scoreService.updateScore(
                SCORE_ID, new SaveMarksRequest(new BigDecimal("50"), null), TEACHER_ID))
                .isInstanceOf(PermissionDeniedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_YOUR_CLASS);
    }

    @Test
    @DisplayName("should record history when score changes")
    void shouldRecordHistoryWhenScoreChanges() {
        Score score = score(new BigDecimal("40"));
        when(scoreRepository.findById(SCORE_ID)).thenReturn(Optional.of(score));
        when(scoreRepository.save(any(Score.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scoreService.updateScore(SCORE_ID, new SaveMarksRequest(new BigDecimal("50"), "Updated"), TEACHER_ID);

        verify(scoreHistoryRepository).save(any());
    }

    @Test
    @DisplayName("should not record history when score unchanged")
    void shouldNotRecordHistoryWhenScoreUnchanged() {
        BigDecimal existingScore = new BigDecimal("82");
        Score score = score(existingScore);
        when(scoreRepository.findById(SCORE_ID)).thenReturn(Optional.of(score));
        when(scoreRepository.save(any(Score.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scoreService.updateScore(
                SCORE_ID, new SaveMarksRequest(existingScore, "Same mark"), TEACHER_ID);

        verify(scoreHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("should save batch successfully")
    void shouldSaveBatchSuccessfully() {
        Assessment assessment = assessment();
        Score score = score(new BigDecimal("40"));
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
        when(scoreRepository.findById(SCORE_ID)).thenReturn(Optional.of(score));
        when(scoreRepository.save(any(Score.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SaveMarksBatchRequest request = new SaveMarksBatchRequest(List.of(
                new SaveMarksBatchRequest.ScoreEntry(SCORE_ID, STUDENT_ID, new BigDecimal("55"), "Saved")));

        List<ScoreResponse> response = scoreService.saveBatch(ASSESSMENT_ID, request, TEACHER_ID);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).score()).isEqualTo(new BigDecimal("55"));
        assertThat(response.get(0).feedback()).isEqualTo("Saved");
        verify(scoreRepository).save(score);
    }

    @Test
    @DisplayName("should get grid when assessment exists")
    void shouldGetGridWhenAssessmentExists() {
        Assessment assessment = assessment();
        assessment.setClassEntity(ClassEntity.builder().id(3L).name("S3").build());
        when(assessmentRepository.findById(ASSESSMENT_ID)).thenReturn(Optional.of(assessment));
        when(studentRepository.findByCurrentClassIdAndIsActiveTrue(3L)).thenReturn(List.of());
        when(scoreRepository.findByAssessmentId(ASSESSMENT_ID)).thenReturn(List.of());
        when(assessmentTopicRepository.findByAssessmentId(ASSESSMENT_ID)).thenReturn(List.of());

        MarkGridResponse response = scoreService.getGrid(ASSESSMENT_ID);

        assertThat(response.assessment().id()).isEqualTo(ASSESSMENT_ID);
        assertThat(response.assessment().className()).isEqualTo("S3");
        assertThat(response.students()).isEmpty();
        assertThat(response.scores()).isEmpty();
    }

    private Score score(BigDecimal existingScore) {
        Student student = Student.builder()
                .id(STUDENT_ID)
                .admissionNumber("A-030")
                .fullName("Alex Student")
                .build();
        return Score.builder()
                .id(SCORE_ID)
                .assessment(assessment())
                .student(student)
                .score(existingScore)
                .feedback("Previous feedback")
                .build();
    }

    private Assessment assessment() {
        return Assessment.builder()
                .id(ASSESSMENT_ID)
                .teacher(User.builder().id(TEACHER_ID).build())
                .title("Mid-Term Exam")
                .type("EXAM")
                .maxScore(new BigDecimal("100"))
                .status("DRAFT")
                .build();
    }
}
