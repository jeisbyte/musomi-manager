package com.musomi.desktop.controller.teacher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared, in-process store for assessments and their student mark data.
 *
 * This is the single source of truth for all assessment data while the backend
 * is not yet integrated. Replace the static fields with API calls once ready.
 *
 * Thread-safety: not needed — JavaFX is single-threaded on the UI thread.
 */
public final class AssessmentStore {

    // =========================================================================
    // Assessment model
    // =========================================================================

    public static class Assessment {
        private String name;
        private String className;
        private String subject;
        private String topic;
        private String date;        // display string e.g. "15 Sep 2026"
        private int    maxMarks;
        private String status;      // "Draft" | "Published"

        public Assessment(String name, String className, String subject,
                          String topic, String date, int maxMarks, String status) {
            this.name      = name;
            this.className = className;
            this.subject   = subject;
            this.topic     = topic;
            this.date      = date;
            this.maxMarks  = maxMarks;
            this.status    = status;
        }

        public String getName()      { return name; }
        public String getClassName() { return className; }
        public String getSubject()   { return subject; }
        public String getTopic()     { return topic; }
        public String getDate()      { return date; }
        public int    getMaxMarks()  { return maxMarks; }
        public String getStatus()    { return status; }

        public void setName(String n)      { this.name = n; }
        public void setClassName(String c) { this.className = c; }
        public void setSubject(String s)   { this.subject = s; }
        public void setTopic(String t)     { this.topic = t; }
        public void setDate(String d)      { this.date = d; }
        public void setMaxMarks(int m)     { this.maxMarks = m; }
        public void setStatus(String st)   { this.status = st; }
    }

    // =========================================================================
    // Student mark model
    // =========================================================================

    public static class StudentMark {
        private final String number;
        private final String student;
        private       String mark;    // empty string = not entered

        public StudentMark(String number, String student, String mark) {
            this.number  = number;
            this.student = student;
            this.mark    = mark;
        }

        public String getNumber()  { return number; }
        public String getStudent() { return student; }
        public String getMark()    { return mark; }
        public void   setMark(String m) { this.mark = m; }

        public boolean hasValidMark(int maxMarks) {
            return hasValidMark(mark, maxMarks);
        }

        public static boolean hasValidMark(String mark, int maxMarks) {
            if (mark == null || mark.trim().isEmpty() || maxMarks <= 0) return false;
            try {
                double v = Double.parseDouble(mark.trim());
                return v >= 0 && v <= maxMarks;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public String computeGrade(int maxMarks) {
            return gradeFor(mark, maxMarks);
        }

        public static String gradeFor(String mark, int maxMarks) {
            if (!hasValidMark(mark, maxMarks)) return "";
            double pct = (Double.parseDouble(mark.trim()) / maxMarks) * 100.0;
            if (pct >= 80) return "A";
            if (pct >= 70) return "B";
            if (pct >= 60) return "C";
            if (pct >= 50) return "D";
            if (pct >= 40) return "E";
            return "F";
        }

        public String computeRemark(int maxMarks) {
            return remarkFor(computeGrade(maxMarks));
        }

        public static String remarkFor(String grade) {
            switch (grade) {
                case "A": return "Excellent";
                case "B": return "Very Good";
                case "C": return "Good";
                case "D": return "Satisfactory";
                case "E": return "Needs Improvement";
                case "F": return "Fail";
                default:  return "";
            }
        }
    }

    // =========================================================================
    // Singleton store
    // =========================================================================

    /** All assessments, in insertion order. */
    private static final List<Assessment> ASSESSMENTS = new ArrayList<>(List.of(
        new Assessment("Mathematics Test 1", "S3 Blue", "Mathematics",
                       "Algebra",      "15 Sep 2026", 40, "Draft"),
        new Assessment("Mathematics Test 2", "S3 Red",  "Mathematics",
                       "Geometry",     "20 Sep 2026", 40, "Published"),
        new Assessment("Biology Assignment", "S4 Blue", "Biology",
                       "Cell Biology", "25 Sep 2026", 30, "Draft")
    ));

    /**
     * Student marks per assessment, keyed by Assessment identity (object ref).
     * Populated lazily on first access.
     */
    private static final Map<Assessment, List<StudentMark>> MARKS =
            new LinkedHashMap<>();

    /** Default mock student roster — shared across all classes for now. */
    private static final String[][] MOCK_STUDENTS = {
        {"1",  "John Mugisha"},
        {"2",  "Sarah Namukasa"},
        {"3",  "Brian Okello"},
        {"4",  "Mary Achieng"},
        {"5",  "David Kato"},
        {"6",  "Grace Nakato"},
        {"7",  "Daniel Ouma"},
        {"8",  "Rebecca Atim"},
        {"9",  "Peter Ssekandi"},
        {"10", "Esther Nankya"},
        {"11", "Samuel Tumusiime"},
        {"12", "Angela Nabirye"}
    };

    // Pre-populate marks for the already-Published assessment so it isn't empty
    static {
        Assessment published = ASSESSMENTS.get(1); // "Mathematics Test 2"
        String[] pubMarks = {"35","29","32","38","25","36","21","34","28","33","20","37"};
        List<StudentMark> list = new ArrayList<>();
        for (int i = 0; i < MOCK_STUDENTS.length; i++) {
            list.add(new StudentMark(MOCK_STUDENTS[i][0], MOCK_STUDENTS[i][1],
                                     pubMarks[i]));
        }
        MARKS.put(published, list);

        // "Mathematics Test 1" — partially filled (8 of 12)
        Assessment draft1 = ASSESSMENTS.get(0);
        String[] draft1Marks = {"35","32","28","30","24","38","","34","27","31","","36"};
        List<StudentMark> d1list = new ArrayList<>();
        for (int i = 0; i < MOCK_STUDENTS.length; i++) {
            d1list.add(new StudentMark(MOCK_STUDENTS[i][0], MOCK_STUDENTS[i][1],
                                       draft1Marks[i]));
        }
        MARKS.put(draft1, d1list);
    }

    private AssessmentStore() {} // static-only

    // =========================================================================
    // Public API
    // =========================================================================

    /** Returns the live mutable assessment list. */
    public static List<Assessment> getAssessments() {
        return ASSESSMENTS;
    }

    /** Returns assessments that are incomplete or have not yet been published. */
    public static List<Assessment> getPendingAssessments() {
        return ASSESSMENTS.stream()
                .filter(AssessmentStore::isPending)
                .toList();
    }

    /** An assessment remains pending until it is complete and published. */
    public static boolean isPending(Assessment assessment) {
        return assessment != null
                && (!"Published".equals(assessment.getStatus()) || !allMarksComplete(assessment));
    }

    /**
     * Returns the student mark list for the given assessment.
     * If no list exists yet, one is created with blank marks.
     */
    public static List<StudentMark> getMarks(Assessment assessment) {
        return MARKS.computeIfAbsent(assessment, a -> {
            List<StudentMark> list = new ArrayList<>();
            for (String[] s : MOCK_STUDENTS) {
                list.add(new StudentMark(s[0], s[1], ""));
            }
            return list;
        });
    }

    /** Adds a new assessment and returns it. */
    public static Assessment addAssessment(String name, String className,
                                           String subject, String topic,
                                           String date, int maxMarks) {
        Assessment a = new Assessment(name, className, subject, topic,
                                      date, maxMarks, "Draft");
        ASSESSMENTS.add(a);
        return a;
    }

    /** Publishes the given assessment (must be Draft with all marks filled). */
    public static void publish(Assessment assessment) {
        if (!allMarksComplete(assessment)) {
            throw new IllegalStateException("All students must have a valid mark before publishing.");
        }
        assessment.setStatus("Published");
    }

    /** How many students have valid marks for this assessment. */
    public static int countCompleted(Assessment assessment) {
        List<StudentMark> marks = getMarks(assessment);
        int count = 0;
        for (StudentMark sm : marks) {
            if (sm.hasValidMark(assessment.getMaxMarks())) count++;
        }
        return count;
    }

    /** Returns the number of assessments with the exact status "Published". */
    public static int countPublished() {
        return (int) getAssessments().stream()
                .filter(assessment -> "Published".equals(assessment.getStatus()))
                .count();
    }

    /** Returns the number of distinct, non-blank class names across all assessments. */
    public static int countDistinctClasses() {
        return (int) getAssessments().stream()
                .map(Assessment::getClassName)
                .filter(className -> className != null && !className.isBlank())
                .map(String::trim)
                .distinct()
                .count();
    }

    /**
     * Returns the size of the mock student roster only.
     * This mock-only count will be replaced by a real endpoint when the backend is available.
     */
    public static int mockRosterSize() {
        return MOCK_STUDENTS.length;
    }

    /** True if every student has a valid mark. */
    public static boolean allMarksComplete(Assessment assessment) {
        List<StudentMark> marks = getMarks(assessment);
        if (marks.isEmpty()) return false;
        for (StudentMark sm : marks) {
            if (!sm.hasValidMark(assessment.getMaxMarks())) return false;
        }
        return true;
    }
}
