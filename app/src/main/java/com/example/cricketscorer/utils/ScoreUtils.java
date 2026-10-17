package com.example.cricketscorer.utils;

/**
 * Utility methods used throughout Cricket Score Manager.
 *
 * Handles:
 *
 * - Cricket over formatting
 * - Standard format over limits
 * - Format validation
 * - Run-rate calculations
 * - Strike-rate calculations
 * - Economy-rate calculations
 */
public class ScoreUtils {

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    private ScoreUtils() {
        // Utility class.
    }

    // ============================================================
    // OVERS FORMATTER
    // ============================================================

    /**
     * Converts legal cricket balls into cricket overs.
     *
     * Cricket overs are NOT decimal mathematical values.
     *
     * Examples:
     *
     * 0  -> 0.0
     * 1  -> 0.1
     * 2  -> 0.2
     * 3  -> 0.3
     * 4  -> 0.4
     * 5  -> 0.5
     * 6  -> 1.0
     * 7  -> 1.1
     * 11 -> 1.5
     * 12 -> 2.0
     *
     * @param balls number of legal balls
     * @return cricket-format overs string
     */
    public static String formatOvers(
            int balls
    ) {

        if (balls < 0) {
            balls = 0;
        }

        return (balls / 6)
                + "."
                + (balls % 6);
    }

    // ============================================================
    // STANDARD FORMAT OVERS
    // ============================================================

    /**
     * Returns the standard number of overs
     * for the supplied match format.
     *
     * T20  -> 20
     * ODI  -> 50
     * CUSTOM -> 0
     *
     * Custom matches use the over limit stored with the match.
     *
     * @param format match format
     * @return standard overs
     */
    public static int getStandardOvers(
            String format
    ) {

        if (format == null) {
            return 20;
        }

        switch (
                format.trim().toUpperCase()
        ) {

            case "T20":
                return 20;

            case "ODI":
                return 50;

            case "CUSTOM":
                return 0;

            default:
                return 20;
        }
    }

    // ============================================================
    // FORMAT VALIDATION
    // ============================================================

    /**
     * Checks whether the supplied format
     * is supported by the application.
     *
     * Supported formats:
     *
     * T20
     * ODI
     * CUSTOM
     *
     * @param format match format
     * @return true if valid
     */
    public static boolean isValidFormat(
            String format
    ) {

        if (format == null) {
            return false;
        }

        String value =
                format.trim().toUpperCase();

        return value.equals("T20")
                || value.equals("ODI")
                || value.equals("CUSTOM");
    }

    // ============================================================
    // CURRENT INNINGS BALLS
    // ============================================================

    /**
     * Converts an innings ball count to
     * cricket overs.
     *
     * This is an alias for formatOvers().
     *
     * @param balls legal balls
     * @return formatted overs
     */
    public static String getOversFromBalls(
            int balls
    ) {

        return formatOvers(
                balls
        );
    }

    // ============================================================
    // RUN RATE
    // ============================================================

    /**
     * Calculates run rate.
     *
     * Formula:
     *
     * Runs / Overs
     *
     * Since cricket overs contain six balls,
     * the calculation is performed using balls:
     *
     * Runs × 6 / Balls
     *
     * @param runs total runs
     * @param balls legal balls
     * @return run rate
     */
    public static double calculateRunRate(
            int runs,
            int balls
    ) {

        if (balls <= 0) {
            return 0.0;
        }

        return runs * 6.0 / balls;
    }

    // ============================================================
    // STRIKE RATE
    // ============================================================

    /**
     * Calculates batting strike rate.
     *
     * Formula:
     *
     * Runs / Balls × 100
     *
     * @param runs batter runs
     * @param balls balls faced
     * @return strike rate
     */
    public static double calculateStrikeRate(
            int runs,
            int balls
    ) {

        if (balls <= 0) {
            return 0.0;
        }

        return runs * 100.0 / balls;
    }

    // ============================================================
    // ECONOMY RATE
    // ============================================================

    /**
     * Calculates bowling economy rate.
     *
     * Formula:
     *
     * Runs conceded × 6 / Legal balls bowled
     *
     * @param runsConceded runs conceded by bowler
     * @param balls balls bowled
     * @return economy rate
     */
    public static double calculateEconomyRate(
            int runsConceded,
            int balls
    ) {

        if (balls <= 0) {
            return 0.0;
        }

        return runsConceded * 6.0 / balls;
    }

    // ============================================================
    // OVERS TO BALLS
    // ============================================================

    /**
     * Converts a whole number of overs to legal balls.
     *
     * @param overs number of overs
     * @return legal balls
     */
    public static int oversToBalls(
            int overs
    ) {

        if (overs < 0) {
            overs = 0;
        }

        return overs * 6;
    }

    // ============================================================
    // BALLS REMAINING
    // ============================================================

    /**
     * Returns the number of legal balls remaining
     * in a limited-overs innings.
     *
     * @param totalOvers innings over limit
     * @param ballsBowled legal balls already bowled
     * @return remaining legal balls
     */
    public static int ballsRemaining(
            int totalOvers,
            int ballsBowled
    ) {

        if (totalOvers <= 0) {
            return 0;
        }

        int totalBalls =
                totalOvers * 6;

        return Math.max(
                totalBalls - Math.max(
                        ballsBowled,
                        0
                ),
                0
        );
    }

    // ============================================================
    // REQUIRED RUN RATE
    // ============================================================

    /**
     * Calculates required run rate.
     *
     * Formula:
     *
     * Runs required × 6 / Balls remaining
     *
     * @param runsRequired runs still required
     * @param ballsRemaining balls remaining
     * @return required run rate
     */
    public static double calculateRequiredRunRate(
            int runsRequired,
            int ballsRemaining
    ) {

        if (runsRequired <= 0
                || ballsRemaining <= 0) {

            return 0.0;
        }

        return runsRequired
                * 6.0
                / ballsRemaining;
    }

    // ============================================================
    // TARGET
    // ============================================================

    /**
     * Returns the target required to win.
     *
     * @param firstInningsRuns first innings score
     * @return target score
     */
    public static int calculateTarget(
            int firstInningsRuns
    ) {

        return Math.max(
                firstInningsRuns + 1,
                1
        );
    }

    // ============================================================
    // RUNS REQUIRED
    // ============================================================

    /**
     * Returns runs still required to reach target.
     *
     * @param target target score
     * @param currentRuns current score
     * @return runs required
     */
    public static int runsRequired(
            int target,
            int currentRuns
    ) {

        return Math.max(
                target - currentRuns,
                0
        );
    }

    // ============================================================
    // OVERS COMPLETED
    // ============================================================

    /**
     * Checks whether a complete over has been bowled.
     *
     * @param balls legal balls
     * @return true when balls form one or more complete overs
     */
    public static boolean isCompleteOver(
            int balls
    ) {

        return balls > 0
                && balls % 6 == 0;
    }

    // ============================================================
    // LIMITED OVERS CHECK
    // ============================================================

    /**
     * Checks whether the format is a limited-overs format.
     *
     * @param format match format
     * @return true for T20, ODI and Custom
     */
    public static boolean isLimitedOversFormat(
            String format
    ) {

        if (format == null) {
            return false;
        }

        String value =
                format.trim().toUpperCase();

        return value.equals("T20")
                || value.equals("ODI")
                || value.equals("CUSTOM");
    }

}
