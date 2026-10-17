package com.example.cricketscorer.models;

import java.io.Serializable;

/**
 * Represents a cricket match, its setup, live state,
 * innings information and final result.
 *
 * Supports:
 * - T20
 * - ODI
 * - Custom
 */
public class MatchInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    // ============================================================
    // CONSTANTS
    // ============================================================

    public static final int MAX_WICKETS_PER_INNINGS = 10;

    // ============================================================
    // BASIC MATCH INFORMATION
    // ============================================================

    private long id;

    private long teamAId;
    private long teamBId;

    private String teamAName;
    private String teamBName;

    private String matchFormat;

    private int totalOvers;

    private int inningsPerTeam;

    // ============================================================
    // TOSS
    // ============================================================

    private long tossWinnerId;

    private String tossDecision;

    private long battingTeamId;

    private long bowlingTeamId;

    // ============================================================
    // RESULT
    // ============================================================

    private String result;

    private boolean completed;

    private String matchDate;

    /*
     * Player of the Match is stored both by ID and name.
     *
     * ID is required by DatabaseHelper for database persistence.
     * Name is retained for display/compatibility with existing
     * screens.
     */
    private long playerOfMatchId;
    private String playerOfMatchName;

    // ============================================================
    // INNINGS 1
    // ============================================================

    private int innings1Runs;
    private int innings1Wickets;
    private int innings1Balls;
    private long innings1TeamId;

    // ============================================================
    // INNINGS 2
    // ============================================================

    private int innings2Runs;
    private int innings2Wickets;
    private int innings2Balls;
    private long innings2TeamId;

    // ============================================================
    // INNINGS 3
    // ============================================================

    private int innings3Runs;
    private int innings3Wickets;
    private int innings3Balls;
    private long innings3TeamId;

    // ============================================================
    // INNINGS 4
    // ============================================================

    private int innings4Runs;
    private int innings4Wickets;
    private int innings4Balls;
    private long innings4TeamId;

    // ============================================================
    // LIVE MATCH STATE
    // ============================================================

    private int currentInnings = 1;

    private int currentDay = 1;

    private int currentSession = 1;

    private boolean inningsDeclared = false;

    private boolean isPaused = false;

    private boolean followOn = false;

    private long strikerId = -1;
    private long nonStrikerId = -1;
    private long bowlerId = -1;
    private long previousBowlerId = -1;
    private int legalBallsThisOver = 0;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public MatchInfo() {

        id = -1;

        teamAId = -1;
        teamBId = -1;

        teamAName = "";
        teamBName = "";

        matchFormat = "T20";

        totalOvers = 20;

        inningsPerTeam = 1;

        tossWinnerId = -1;

        tossDecision = "";

        battingTeamId = -1;
        bowlingTeamId = -1;

        result = "";

        completed = false;

        matchDate = "";

        playerOfMatchId = -1;
        playerOfMatchName = "";

        innings1TeamId = -1;
        innings2TeamId = -1;
        innings3TeamId = -1;
        innings4TeamId = -1;
    }

    // ============================================================
    // ID
    // ============================================================

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // ============================================================
    // TEAM A
    // ============================================================

    public long getTeamAId() {
        return teamAId;
    }

    public void setTeamAId(long teamAId) {
        this.teamAId = teamAId;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public void setTeamAName(String teamAName) {
        this.teamAName =
                teamAName == null ? "" : teamAName;
    }

    // ============================================================
    // TEAM B
    // ============================================================

    public long getTeamBId() {
        return teamBId;
    }

    public void setTeamBId(long teamBId) {
        this.teamBId = teamBId;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public void setTeamBName(String teamBName) {
        this.teamBName =
                teamBName == null ? "" : teamBName;
    }

    // ============================================================
    // MATCH FORMAT
    // ============================================================

    public String getMatchFormat() {
        return matchFormat;
    }

    public void setMatchFormat(String matchFormat) {

        if (matchFormat == null ||
                matchFormat.trim().isEmpty()) {

            this.matchFormat = "T20";

            return;
        }

        this.matchFormat =
                matchFormat.trim();

        // Test matches are no longer supported.
        // Keep all new matches to one innings per team.
        if (this.matchFormat.equalsIgnoreCase("Test")
                || this.matchFormat.equalsIgnoreCase("Test Match")) {
            this.matchFormat = "T20";
            this.totalOvers = 20;
        }

        inningsPerTeam = 1;
    }

    // ============================================================
    // TOTAL OVERS
    // ============================================================

    public int getTotalOvers() {
        return totalOvers;
    }

    public void setTotalOvers(int totalOvers) {
        this.totalOvers =
                Math.max(0, totalOvers);
    }

    // ============================================================
    // INNINGS PER TEAM
    // ============================================================

    public int getInningsPerTeam() {
        return inningsPerTeam;
    }

    public void setInningsPerTeam(int inningsPerTeam) {

        // Every supported match has exactly one innings per team.
        this.inningsPerTeam = 1;
    }

    // ============================================================
    // TOSS WINNER
    // ============================================================

    public long getTossWinnerId() {
        return tossWinnerId;
    }

    public void setTossWinnerId(long tossWinnerId) {
        this.tossWinnerId = tossWinnerId;
    }

    /*
     * Compatibility method used by existing project code.
     */
    public long getTossWinnerTeamId() {
        return tossWinnerId;
    }

    public void setTossWinnerTeamId(long tossWinnerTeamId) {
        this.tossWinnerId = tossWinnerTeamId;
    }

    // ============================================================
    // TOSS DECISION
    // ============================================================

    public String getTossDecision() {
        return tossDecision;
    }

    public void setTossDecision(String tossDecision) {

        this.tossDecision =
                tossDecision == null
                        ? ""
                        : tossDecision;
    }

    // ============================================================
    // BATTING TEAM
    // ============================================================

    public long getBattingTeamId() {
        return battingTeamId;
    }

    public void setBattingTeamId(long battingTeamId) {
        this.battingTeamId = battingTeamId;
    }

    // ============================================================
    // BOWLING TEAM
    // ============================================================

    public long getBowlingTeamId() {
        return bowlingTeamId;
    }

    public void setBowlingTeamId(long bowlingTeamId) {
        this.bowlingTeamId = bowlingTeamId;
    }

    // ============================================================
    // RESULT
    // ============================================================

    public String getResult() {
        return result;
    }

    public void setResult(String result) {

        this.result =
                result == null ? "" : result;
    }

    /*
     * Compatibility method used by older screens.
     */
    public String getResultText() {
        return result;
    }

    public void setResultText(String resultText) {
        this.result =
                resultText == null ? "" : resultText;
    }

    // ============================================================
    // COMPLETED
    // ============================================================

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // ============================================================
    // MATCH DATE
    // ============================================================

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {

        this.matchDate =
                matchDate == null ? "" : matchDate;
    }

    // ============================================================
    // PLAYER OF MATCH
    // ============================================================

    /**
     * Returns the database ID of the Player of the Match.
     */
    public long getPlayerOfMatchId() {
        return playerOfMatchId;
    }

    /**
     * Sets the database ID of the Player of the Match.
     */
    public void setPlayerOfMatchId(long playerOfMatchId) {
        this.playerOfMatchId = playerOfMatchId;
    }

    /**
     * Returns the display name of the Player of the Match.
     */
    public String getPlayerOfMatchName() {
        return playerOfMatchName;
    }

    /**
     * Sets the display name of the Player of the Match.
     */
    public void setPlayerOfMatchName(
            String playerOfMatchName
    ) {

        this.playerOfMatchName =
                playerOfMatchName == null
                        ? ""
                        : playerOfMatchName;
    }

    // ============================================================
    // INNINGS 1
    // ============================================================

    public int getInn1Runs() {
        return innings1Runs;
    }

    public void setInn1Runs(int runs) {
        innings1Runs = Math.max(0, runs);
    }

    public int getInn1Wickets() {
        return innings1Wickets;
    }

    public void setInn1Wickets(int wickets) {
        innings1Wickets =
                Math.max(0, wickets);
    }

    public int getInn1Balls() {
        return innings1Balls;
    }

    public void setInn1Balls(int balls) {
        innings1Balls =
                Math.max(0, balls);
    }

    // ============================================================
    // INNINGS 2
    // ============================================================

    public int getInn2Runs() {
        return innings2Runs;
    }

    public void setInn2Runs(int runs) {
        innings2Runs =
                Math.max(0, runs);
    }

    public int getInn2Wickets() {
        return innings2Wickets;
    }

    public void setInn2Wickets(int wickets) {
        innings2Wickets =
                Math.max(0, wickets);
    }

    public int getInn2Balls() {
        return innings2Balls;
    }

    public void setInn2Balls(int balls) {
        innings2Balls =
                Math.max(0, balls);
    }

    // ============================================================
    // INNINGS 3
    // ============================================================

    public int getInn3Runs() {
        return innings3Runs;
    }

    public void setInn3Runs(int runs) {
        innings3Runs =
                Math.max(0, runs);
    }

    public int getInn3Wickets() {
        return innings3Wickets;
    }

    public void setInn3Wickets(int wickets) {
        innings3Wickets =
                Math.max(0, wickets);
    }

    public int getInn3Balls() {
        return innings3Balls;
    }

    public void setInn3Balls(int balls) {
        innings3Balls =
                Math.max(0, balls);
    }

    // ============================================================
    // INNINGS 4
    // ============================================================

    public int getInn4Runs() {
        return innings4Runs;
    }

    public void setInn4Runs(int runs) {
        innings4Runs =
                Math.max(0, runs);
    }

    public int getInn4Wickets() {
        return innings4Wickets;
    }

    public void setInn4Wickets(int wickets) {
        innings4Wickets =
                Math.max(0, wickets);
    }

    public int getInn4Balls() {
        return innings4Balls;
    }

    public void setInn4Balls(int balls) {
        innings4Balls =
                Math.max(0, balls);
    }

    // ============================================================
    // GENERIC INNINGS RUNS
    // ============================================================

    public int getInningsRuns(int innings) {

        switch (innings) {

            case 1:
                return innings1Runs;

            case 2:
                return innings2Runs;

            case 3:
                return innings3Runs;

            case 4:
                return innings4Runs;

            default:
                return 0;
        }
    }

    public void setInningsRuns(
            int innings,
            int runs
    ) {

        runs = Math.max(0, runs);

        switch (innings) {

            case 1:
                innings1Runs = runs;
                break;

            case 2:
                innings2Runs = runs;
                break;

            case 3:
                innings3Runs = runs;
                break;

            case 4:
                innings4Runs = runs;
                break;
        }
    }

    // ============================================================
    // GENERIC INNINGS WICKETS
    // ============================================================

    public int getInningsWickets(int innings) {

        switch (innings) {

            case 1:
                return innings1Wickets;

            case 2:
                return innings2Wickets;

            case 3:
                return innings3Wickets;

            case 4:
                return innings4Wickets;

            default:
                return 0;
        }
    }

    public void setInningsWickets(
            int innings,
            int wickets
    ) {

        wickets =
                Math.max(
                        0,
                        wickets
                );

        switch (innings) {

            case 1:
                innings1Wickets = wickets;
                break;

            case 2:
                innings2Wickets = wickets;
                break;

            case 3:
                innings3Wickets = wickets;
                break;

            case 4:
                innings4Wickets = wickets;
                break;
        }
    }

    // ============================================================
    // GENERIC INNINGS BALLS
    // ============================================================

    public int getInningsBalls(int innings) {

        switch (innings) {

            case 1:
                return innings1Balls;

            case 2:
                return innings2Balls;

            case 3:
                return innings3Balls;

            case 4:
                return innings4Balls;

            default:
                return 0;
        }
    }

    public void setInningsBalls(
            int innings,
            int balls
    ) {

        balls =
                Math.max(
                        0,
                        balls
                );

        switch (innings) {

            case 1:
                innings1Balls = balls;
                break;

            case 2:
                innings2Balls = balls;
                break;

            case 3:
                innings3Balls = balls;
                break;

            case 4:
                innings4Balls = balls;
                break;
        }
    }

    // ============================================================
    // INNINGS TEAM IDs
    // ============================================================

    public long getInningsTeamId(int innings) {

        switch (innings) {

            case 1:
                return getFirstBattingTeamId();

            case 2:
                return getSecondBattingTeamId();

            case 3:
                return innings3TeamId;

            case 4:
                return innings4TeamId;

            default:
                return -1;
        }
    }

    public void setInningsTeamId(
            int innings,
            long teamId
    ) {

        switch (innings) {

            case 1:
                innings1TeamId = teamId;
                break;

            case 2:
                innings2TeamId = teamId;
                break;

            case 3:
                innings3TeamId = teamId;
                break;

            case 4:
                innings4TeamId = teamId;
                break;
        }
    }

    // ============================================================
    // CURRENT INNINGS
    // ============================================================

    public int getCurrentInnings() {
        return currentInnings;
    }

    public void setCurrentInnings(
            int currentInnings
    ) {

        if (currentInnings < 1) {
            currentInnings = 1;
        }

        if (currentInnings > 4) {
            currentInnings = 4;
        }

        this.currentInnings =
                currentInnings;
    }

    public int getCurrentInningsRuns() {

        return getInningsRuns(
                currentInnings
        );
    }

    public int getCurrentInningsWickets() {

        return getInningsWickets(
                currentInnings
        );
    }

    public int getCurrentInningsBalls() {

        return getInningsBalls(
                currentInnings
        );
    }

    public long getCurrentInningsTeamId() {

        return getInningsTeamId(
                currentInnings
        );
    }

    // ============================================================
    // CURRENT DAY
    // ============================================================

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {

        this.currentDay = 1;
    }

    // ============================================================
    // CURRENT SESSION
    // ============================================================

    public int getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(
            int currentSession
    ) {

        this.currentSession =
                Math.max(
                        1,
                        currentSession
                );
    }

    // ============================================================
    // DECLARATION
    // ============================================================

    public boolean isDeclared() {
        return inningsDeclared;
    }

    public boolean isInningsDeclared() {
        return inningsDeclared;
    }

    public void setDeclared(boolean declared) {
        this.inningsDeclared =
                declared;
    }

    public void setInningsDeclared(
            boolean declared
    ) {

        this.inningsDeclared =
                declared;
    }

    // ============================================================
    // FOLLOW ON
    // ============================================================

    public boolean isFollowOn() {
        return followOn;
    }

    public void setFollowOn(boolean followOn) {
        this.followOn = followOn;
    }

    // ============================================================
    // OVERS
    // ============================================================

    public double getInningsOvers(
            int innings
    ) {

        return getInningsBalls(
                innings
        ) / 6.0;
    }

    public double getCurrentInningsOvers() {

        return getInningsOvers(
                currentInnings
        );
    }

    // ============================================================
    // INNINGS ALL OUT
    // ============================================================

    public boolean isInningsAllOut(
            int innings
    ) {

        return getInningsWickets(
                innings
        ) >= MAX_WICKETS_PER_INNINGS;
    }

    // ============================================================
    // INNINGS DECLARATION
    // ============================================================

    public boolean inningsDeclaredFor(
            int innings
    ) {

        return innings == currentInnings
                && inningsDeclared;
    }

    // ============================================================
    // INNINGS OVER LIMIT
    // ============================================================

    public boolean isInningsOverLimit(
            int innings
    ) {

        // All supported formats are limited-overs.
        // Custom matches use totalOvers supplied at match creation.


        if (totalOvers <= 0) {
            return false;
        }

        int balls =
                getInningsBalls(
                        innings
                );

        return balls >= totalOvers * 6;
    }

    public boolean isCurrentInningsOverLimit() {

        return isInningsOverLimit(
                currentInnings
        );
    }

    // ============================================================
    // FIRST BATTING TEAM
    // ============================================================

    public long getFirstBattingTeamId() {
        if (innings1TeamId != -1) {
            return innings1TeamId;
        }
        if ("Bowl".equalsIgnoreCase(tossDecision) || "Field".equalsIgnoreCase(tossDecision)) {
            return (tossWinnerId == teamAId) ? teamBId : teamAId;
        }
        if (battingTeamId != -1) {
            return battingTeamId;
        }
        return tossWinnerId != -1 ? tossWinnerId : teamAId;
    }

    // ============================================================
    // SECOND BATTING TEAM
    // ============================================================

    public long getSecondBattingTeamId() {
        if (innings2TeamId != -1) {
            return innings2TeamId;
        }
        long first = getFirstBattingTeamId();
        return (first == teamAId) ? teamBId : teamAId;
    }

    // ============================================================
    // OPPOSITION TEAM
    // ============================================================

    public long getOppositionTeamId(
            long battingTeam
    ) {

        if (battingTeam == teamAId) {
            return teamBId;
        }

        if (battingTeam == teamBId) {
            return teamAId;
        }

        return -1;
    }

    // ============================================================
    // CURRENT INNINGS COMPLETE
    // ============================================================

    public boolean isCurrentInningsComplete() {

        if (isInningsAllOut(
                currentInnings
        )) {
            return true;
        }

        if (inningsDeclared) {
            return true;
        }

        return isCurrentInningsOverLimit();
    }

    // ============================================================
    // MATCH COMPLETE
    // ============================================================

    public boolean isMatchComplete() {

        if (completed) {
            return true;
        }

        return currentInnings >= 2
                && isCurrentInningsComplete();
    }

    // ============================================================
    // RESET INNINGS
    // ============================================================

    public void resetInnings(
            int innings
    ) {

        switch (innings) {

            case 1:

                innings1Runs = 0;
                innings1Wickets = 0;
                innings1Balls = 0;

                break;

            case 2:

                innings2Runs = 0;
                innings2Wickets = 0;
                innings2Balls = 0;

                break;

            case 3:

                innings3Runs = 0;
                innings3Wickets = 0;
                innings3Balls = 0;

                break;

            case 4:

                innings4Runs = 0;
                innings4Wickets = 0;
                innings4Balls = 0;

                break;
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public long getStrikerId() { return strikerId; }
    public void setStrikerId(long strikerId) { this.strikerId = strikerId; }

    public long getNonStrikerId() { return nonStrikerId; }
    public void setNonStrikerId(long nonStrikerId) { this.nonStrikerId = nonStrikerId; }

    public long getBowlerId() { return bowlerId; }
    public void setBowlerId(long bowlerId) { this.bowlerId = bowlerId; }

    public long getPreviousBowlerId() { return previousBowlerId; }
    public void setPreviousBowlerId(long previousBowlerId) { this.previousBowlerId = previousBowlerId; }

    public int getLegalBallsThisOver() { return legalBallsThisOver; }
    public void setLegalBallsThisOver(int legalBallsThisOver) { this.legalBallsThisOver = Math.max(0, legalBallsThisOver); }

    // ============================================================
    // TO STRING
    // ============================================================

    @Override
    public String toString() {

        return teamAName
                + " vs "
                + teamBName
                + " - "
                + matchFormat;
    }
}