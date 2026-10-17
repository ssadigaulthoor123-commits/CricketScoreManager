package com.example.cricketscorer.models;

import java.util.Locale;

public class BowlingStat {

    private long id;
    private long matchId;
    private int innings;
    private long playerId;

    private String playerName;

    private int balls;
    private int runsConceded;
    private int wickets;
    private int maidens;
    private int dotBalls;
    private int extrasConceded;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public BowlingStat() {

        playerName = "";

        balls = 0;
        runsConceded = 0;
        wickets = 0;
        maidens = 0;
    }

    public BowlingStat(
            long matchId,
            int innings,
            long playerId
    ) {

        this();

        this.matchId = matchId;
        this.innings = innings;
        this.playerId = playerId;
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
    // MATCH ID
    // ============================================================

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    // ============================================================
    // INNINGS
    // ============================================================

    public int getInnings() {
        return innings;
    }

    public void setInnings(int innings) {
        this.innings = innings;
    }

    // ============================================================
    // PLAYER ID
    // ============================================================

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    // ============================================================
    // PLAYER NAME
    // ============================================================

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {

        this.playerName =
                playerName == null
                        ? ""
                        : playerName;
    }

    // ============================================================
    // BALLS
    // ============================================================

    public int getBalls() {
        return balls;
    }

    public void setBalls(int balls) {

        this.balls =
                Math.max(0, balls);
    }

    // ============================================================
    // RUNS CONCEDED
    // ============================================================

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(
            int runsConceded
    ) {

        this.runsConceded =
                Math.max(0, runsConceded);
    }

    // ============================================================
    // WICKETS
    // ============================================================

    public int getWickets() {
        return wickets;
    }

    public void setWickets(int wickets) {

        this.wickets =
                Math.max(0, wickets);
    }

    // ============================================================
    // MAIDENS
    // ============================================================

    public int getMaidens() {
        return maidens;
    }

    public void setMaidens(int maidens) {

        this.maidens =
                Math.max(0, maidens);
    }

    // ============================================================
    // OVERS
    // ============================================================

    public int getCompletedOvers() {

        return balls / 6;
    }

    public int getRemainingBalls() {

        return balls % 6;
    }

    /*
     * Cricket notation:
     *
     * 0 balls = 0.0 overs
     * 5 balls = 0.5 overs
     * 6 balls = 1.0 overs
     * 7 balls = 1.1 overs
     *
     * This is NOT a decimal mathematical conversion.
     */
    public String getOversFormatted() {

        return getCompletedOvers()
                + "."
                + getRemainingBalls();
    }

    /*
     * Compatibility method.
     */
    public String getOvers() {

        return getOversFormatted();
    }

    // ============================================================
    // ECONOMY
    // ============================================================

    public double getEconomy() {

        if (balls <= 0) {
            return 0.0;
        }

        /*
         * Economy =
         *
         * runs conceded / actual overs
         *
         * Actual overs = balls / 6.0
         */
        return runsConceded /
                (balls / 6.0);
    }

    // ============================================================
    // BEST BOWLING
    // ============================================================

    public String getBestBowlingFigures() {

        return wickets
                + "/"
                + runsConceded;
    }

    // ============================================================
    // DOT BALLS
    // ============================================================

    public int getDotBalls() {
        return dotBalls;
    }

    public void setDotBalls(int dotBalls) {
        this.dotBalls = Math.max(0, dotBalls);
    }

    // ============================================================
    // EXTRAS CONCEDED
    // ============================================================

    public int getExtrasConceded() {
        return extrasConceded;
    }

    public void setExtrasConceded(int extrasConceded) {
        this.extrasConceded = Math.max(0, extrasConceded);
    }

    // ============================================================
    // OVERS AS DECIMAL
    // ============================================================

    public double getOversAsDecimal() {

        return balls / 6.0;
    }

    // ============================================================
    // ECONOMY TEXT
    // ============================================================

    public String getEconomyFormatted() {

        return String.format(
                Locale.US,
                "%.2f",
                getEconomy()
        );
    }

    // ============================================================
    // BOWLING FIGURES TEXT
    // ============================================================

    public String getFiguresFormatted() {

        return wickets
                + "/"
                + runsConceded;
    }

    // ============================================================
    // RESET
    // ============================================================

    public void reset() {

        balls = 0;
        runsConceded = 0;
        wickets = 0;
        maidens = 0;
    }
}