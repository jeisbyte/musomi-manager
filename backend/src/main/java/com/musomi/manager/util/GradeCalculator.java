package com.musomi.manager.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Calculates the grade for a score against the assessment max score. */
public final class GradeCalculator {

    private GradeCalculator() {
    }

    /** Calculates a grade from the score and the assessment max score. */
    public static String calculate(BigDecimal score, BigDecimal maxScore) {
        if (score == null || maxScore == null) {
            return null;
        }
        if (maxScore.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal percentage = score.multiply(BigDecimal.valueOf(100))
                .divide(maxScore, 4, RoundingMode.HALF_UP);

        if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "A";
        }
        if (percentage.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "B";
        }
        if (percentage.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "C";
        }
        if (percentage.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "D";
        }
        if (percentage.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "E";
        }
        return "F";
    }
}
