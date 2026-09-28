package com.musomi.desktop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility for formatting scores into letter grades based on the standard grading scale.
 *
 * <p>Grading thresholds:
 * <ul>
 *   <li>&gt;= 80% &rarr; "A"</li>
 *   <li>&gt;= 70% &rarr; "B"</li>
 *   <li>&gt;= 60% &rarr; "C"</li>
 *   <li>&gt;= 50% &rarr; "D"</li>
 *   <li>&gt;= 40% &rarr; "E"</li>
 *   <li>&lt; 40%  &rarr; "F"</li>
 * </ul>
 */
public final class GradeFormatter {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal GRADE_A_MIN = BigDecimal.valueOf(80);
    private static final BigDecimal GRADE_B_MIN = BigDecimal.valueOf(70);
    private static final BigDecimal GRADE_C_MIN = BigDecimal.valueOf(60);
    private static final BigDecimal GRADE_D_MIN = BigDecimal.valueOf(50);
    private static final BigDecimal GRADE_E_MIN = BigDecimal.valueOf(40);

    private GradeFormatter() {}

    /**
     * Determines the letter grade for a given score relative to the maximum score.
     *
     * @param score    the earned score
     * @param maxScore the maximum possible score
     * @return letter grade string ("A", "B", "C", "D", "E", or "F"),
     *         or {@code null} if either argument is {@code null} or {@code maxScore} is zero
     */
    public static String from(BigDecimal score, BigDecimal maxScore) {
        if (score == null || maxScore == null || maxScore.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal percentage = score.multiply(HUNDRED)
                .divide(maxScore, 4, RoundingMode.HALF_UP);

        if (percentage.compareTo(GRADE_A_MIN) >= 0) {
            return "A";
        }
        if (percentage.compareTo(GRADE_B_MIN) >= 0) {
            return "B";
        }
        if (percentage.compareTo(GRADE_C_MIN) >= 0) {
            return "C";
        }
        if (percentage.compareTo(GRADE_D_MIN) >= 0) {
            return "D";
        }
        if (percentage.compareTo(GRADE_E_MIN) >= 0) {
            return "E";
        }
        return "F";
    }
}
