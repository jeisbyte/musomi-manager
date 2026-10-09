package com.musomi.desktop.controller.teacher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.musomi.desktop.controller.teacher.AssessmentStore.Assessment;
import com.musomi.desktop.controller.teacher.AssessmentStore.StudentMark;

class AssessmentStoreTest {

    @Test
    void calculatesGradeAgainstAssessmentMaximum() {
        StudentMark mark = new StudentMark("1", "Student", "32");

        assertEquals("A", mark.computeGrade(40));
        assertEquals("Excellent", mark.computeRemark(40));
        assertEquals("F", new StudentMark("2", "Student", "15").computeGrade(40));
    }

    @Test
    void rejectsMarksOutsideTheAssessmentRange() {
        assertFalse(StudentMark.hasValidMark("-1", 40));
        assertFalse(StudentMark.hasValidMark("41", 40));
        assertFalse(StudentMark.hasValidMark("", 40));
        assertTrue(StudentMark.hasValidMark("0", 40));
    }

    @Test
    void cannotPublishUntilEveryStudentHasAValidMark() {
        Assessment assessment = new Assessment(
                "Test", "S3 Blue", "Mathematics", "Algebra",
                "15 Sep 2026", 40, "Draft");

        assertThrows(IllegalStateException.class, () -> AssessmentStore.publish(assessment));

        List<StudentMark> marks = AssessmentStore.getMarks(assessment);
        marks.forEach(mark -> mark.setMark("0"));
        AssessmentStore.publish(assessment);

        assertEquals("Published", assessment.getStatus());
    }

    @Test
    void pendingAssessmentsAreThoseNotPublishedOrNotFullyMarked() {
        assertEquals(2, AssessmentStore.getPendingAssessments().size());
        assertTrue(AssessmentStore.getPendingAssessments().stream()
                .allMatch(AssessmentStore::isPending));

        Assessment assessment = new Assessment(
                "Complete draft", "S3 Blue", "Mathematics", "Algebra",
                "15 Sep 2026", 40, "Draft");
        List<StudentMark> marks = AssessmentStore.getMarks(assessment);
        marks.forEach(mark -> mark.setMark("0"));
        assertTrue(AssessmentStore.isPending(assessment));

        assessment.setStatus("Published");
        assertFalse(AssessmentStore.isPending(assessment));
    }
}
