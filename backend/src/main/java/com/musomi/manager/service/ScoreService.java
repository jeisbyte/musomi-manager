package com.musomi.manager.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.musomi.manager.dto.request.SaveMarksBatchRequest;
import com.musomi.manager.dto.request.SaveMarksRequest;
import com.musomi.manager.dto.response.MarkGridResponse;
import com.musomi.manager.dto.response.ScoreResponse;
import com.musomi.manager.entity.Assessment;
import com.musomi.manager.entity.AssessmentTopic;
import com.musomi.manager.entity.Score;
import com.musomi.manager.entity.ScoreHistory;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Student;
import com.musomi.manager.entity.User;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.PermissionDeniedException;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.ScoreMapper;
import com.musomi.manager.repository.AssessmentRepository;
import com.musomi.manager.repository.AssessmentTopicRepository;
import com.musomi.manager.repository.ScoreHistoryRepository;
import com.musomi.manager.repository.ScoreRepository;
import com.musomi.manager.repository.StudentRepository;
import com.musomi.manager.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages score entry and mark-grid reads. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final ScoreRepository scoreRepository;
    private final AssessmentRepository assessmentRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;
    private final StudentRepository studentRepository;
    private final AssessmentTopicRepository assessmentTopicRepository;

    /** Updates a single score after permission and range checks. */
    @Transactional
    public ScoreResponse updateScore(Long scoreId, SaveMarksRequest req, Long currentUserId) {
        Score score = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SCORE_NOT_FOUND));

        Assessment assessment = score.getAssessment();
        if (assessment == null || assessment.getTeacher() == null
                || !assessment.getTeacher().getId().equals(currentUserId)) {
            throw new PermissionDeniedException(ErrorCode.NOT_YOUR_CLASS);
        }

        BigDecimal maxScore = assessment.getMaxScore();
        BigDecimal newScore = req.score();
        validateScoreRange(newScore, maxScore);

        BigDecimal oldScore = score.getScore();
        if (hasScoreChanged(oldScore, newScore)) {
            scoreHistoryRepository.save(ScoreHistory.builder()
                    .school(School.builder().id(DEFAULT_SCHOOL_ID).build())
                    .score(score)
                    .oldScore(oldScore)
                    .newScore(newScore)
                    .oldFeedback(score.getFeedback())
                    .newFeedback(req.feedback())
                    .changedBy(User.builder().id(currentUserId).build())
                    .changedAt(LocalDateTime.now())
                    .reason("MARK_UPDATED")
                    .build());
        }

        score.setScore(newScore);
        score.setFeedback(req.feedback());
        score.setUpdatedAt(LocalDateTime.now());
        if (score.getEnteredAt() == null) {
            score.setEnteredAt(LocalDateTime.now());
        }
        Score saved = scoreRepository.save(score);

        log.info("Score updated: scoreId={}, studentId={}, teacherId={}",
                saved.getId(), saved.getStudent().getId(), currentUserId);
        return ScoreMapper.toResponse(saved, GradeCalculator.calculate(saved.getScore(), assessment.getMaxScore()));
    }

    /** Saves a batch of scores for an assessment. */
    @Transactional
    public List<ScoreResponse> saveBatch(Long assessmentId, SaveMarksBatchRequest req, Long currentUserId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ASSESSMENT_NOT_FOUND));

        if (assessment.getTeacher() == null || !assessment.getTeacher().getId().equals(currentUserId)) {
            throw new PermissionDeniedException(ErrorCode.NOT_YOUR_CLASS);
        }

        List<ScoreResponse> responses = new ArrayList<>();
        for (SaveMarksBatchRequest.ScoreEntry entry : req.scores()) {
            Score score = entry.scoreId() != null
                    ? scoreRepository.findById(entry.scoreId())
                            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SCORE_NOT_FOUND))
                    : scoreRepository.findByAssessmentIdAndStudentId(assessmentId, entry.studentId())
                            .orElseGet(() -> {
                                Student student = studentRepository.findById(entry.studentId())
                                        .orElseThrow(() -> new ValidationException(ErrorCode.VALIDATION_FAILED));
                                return Score.builder()
                                        .school(School.builder().id(DEFAULT_SCHOOL_ID).build())
                                        .assessment(assessment)
                                        .student(student)
                                        .score(null)
                                        .feedback(null)
                                        .createdAt(LocalDateTime.now())
                                        .build();
                            });

            if (entry.score() != null) {
                validateScoreRange(entry.score(), assessment.getMaxScore());
            }

            BigDecimal previousValue = score.getScore();
            String previousFeedback = score.getFeedback();
            score.setScore(entry.score());
            score.setFeedback(entry.feedback());
            score.setUpdatedAt(LocalDateTime.now());
            if (score.getEnteredAt() == null) {
                score.setEnteredAt(LocalDateTime.now());
            }
            Score saved = scoreRepository.save(score);

            if (hasScoreChanged(previousValue, entry.score())) {
                scoreHistoryRepository.save(ScoreHistory.builder()
                        .school(School.builder().id(DEFAULT_SCHOOL_ID).build())
                        .score(saved)
                        .oldScore(previousValue)
                        .newScore(entry.score())
                        .oldFeedback(previousFeedback)
                        .newFeedback(entry.feedback())
                        .changedBy(User.builder().id(currentUserId).build())
                        .changedAt(LocalDateTime.now())
                        .reason("BATCH_MARK_SAVE")
                        .build());
            }

            responses.add(ScoreMapper.toResponse(saved,
                    GradeCalculator.calculate(saved.getScore(), assessment.getMaxScore())));
        }

        log.info("Batch saved: assessmentId={}, savedCount={}", assessmentId, responses.size());
        return responses;
    }

    /** Loads the assessment mark grid for the class-stream. */
    @Transactional(readOnly = true)
    public MarkGridResponse getGrid(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ASSESSMENT_NOT_FOUND));

        List<Student> students = studentRepository.findByCurrentClassIdAndIsActiveTrue(
                assessment.getClassEntity().getId());
        if (assessment.getStream() != null) {
            students = students.stream()
                    .filter(student -> student.getCurrentStream() != null
                            && assessment.getStream().getId().equals(student.getCurrentStream().getId()))
                    .toList();
        }

        List<Score> scores = scoreRepository.findByAssessmentId(assessmentId);
        List<AssessmentTopic> topics = assessmentTopicRepository.findByAssessmentId(assessmentId);

        List<MarkGridResponse.StudentRow> studentRows = students.stream()
                .map(student -> new MarkGridResponse.StudentRow(
                        student.getId(),
                        student.getAdmissionNumber(),
                        student.getFullName()))
                .toList();

        List<MarkGridResponse.ScoreRow> scoreRows = scores.stream()
                .map(score -> new MarkGridResponse.ScoreRow(
                        score.getId(),
                        score.getStudent() != null ? score.getStudent().getId() : null,
                        score.getScore(),
                        score.getFeedback(),
                        GradeCalculator.calculate(score.getScore(), assessment.getMaxScore())))
                .toList();

        MarkGridResponse.AssessmentSummary assessmentSummary = new MarkGridResponse.AssessmentSummary(
                assessment.getId(),
                assessment.getTitle(),
                assessment.getType(),
                assessment.getMaxScore(),
                assessment.getStatus(),
                assessment.getSubject() != null ? assessment.getSubject().getName() : null,
                assessment.getClassEntity() != null ? assessment.getClassEntity().getName() : null,
                assessment.getStream() != null ? assessment.getStream().getName() : null,
                assessment.getTerm() != null ? assessment.getTerm().getName() : null,
                assessment.getTerm() != null && assessment.getTerm().getAcademicYear() != null
                        ? assessment.getTerm().getAcademicYear().getYear()
                        : null,
                topics.stream()
                        .map(topic -> topic.getTopic() != null ? topic.getTopic().getName() : null)
                        .filter(name -> name != null)
                        .toList());

        return new MarkGridResponse(assessmentSummary, studentRows, scoreRows);
    }

    private void validateScoreRange(BigDecimal score, BigDecimal maxScore) {
        if (score == null) {
            return;
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.SCORE_BELOW_MIN);
        }
        if (score.compareTo(maxScore) > 0) {
            throw new ValidationException(
                    ErrorCode.SCORE_EXCEEDS_MAX,
                    Map.of("max", maxScore, "entered", score));
        }
    }

    private boolean hasScoreChanged(BigDecimal oldScore, BigDecimal newScore) {
        if (oldScore == null && newScore == null) {
            return false;
        }
        if (oldScore == null || newScore == null) {
            return true;
        }
        return !oldScore.equals(newScore);
    }
}
