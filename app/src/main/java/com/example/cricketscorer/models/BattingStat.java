package com.example.cricketscorer.models;

public class BattingStat {

    private long id;
    private long matchId;
    private int innings;
    private long playerId;

    private String playerName;

    private int runs;
    private int balls;
    private int fours;
    private int sixes;

    private boolean out;
    private String howOut;
    private long bowlerId;
    private String bowlerName = "";
    private long fielderId;
    private String fielderName = "";

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public BattingStat() {
        this.playerName = "";
        this.howOut = "";
        this.runs = 0;
        this.balls = 0;
        this.fours = 0;
        this.sixes = 0;
        this.out = false;
    }

    public BattingStat(
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
    // RUNS
    // ============================================================

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {

        this.runs =
                Math.max(0, runs);
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
    // FOURS
    // ============================================================

    public int getFours() {
        return fours;
    }

    public void setFours(int fours) {

        this.fours =
                Math.max(0, fours);
    }

    // ============================================================
    // SIXES
    // ============================================================

    public int getSixes() {
        return sixes;
    }

    public void setSixes(int sixes) {

        this.sixes =
                Math.max(0, sixes);
    }

    // ============================================================
    // OUT
    // ============================================================

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }

    // ============================================================
    // HOW OUT
    // ============================================================

    public String getHowOut() {
        return howOut;
    }

    public void setHowOut(String howOut) {

        this.howOut =
                howOut == null
                        ? ""
                        : howOut;
    }

    public long getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(long bowlerId) {
        this.bowlerId = bowlerId;
    }

    public String getBowlerName() {
        return bowlerName == null ? "" : bowlerName;
    }

    public void setBowlerName(String bowlerName) {
        this.bowlerName = bowlerName == null ? "" : bowlerName;
    }

    public long getFielderId() {
        return fielderId;
    }

    public void setFielderId(long fielderId) {
        this.fielderId = fielderId;
    }

    public String getFielderName() {
        return fielderName == null ? "" : fielderName;
    }

    public void setFielderName(String fielderName) {
        this.fielderName = fielderName == null ? "" : fielderName;
    }

    // ============================================================
    // STRIKE RATE
    // ============================================================

    public double getStrikeRate() {

        if (balls <= 0) {
            return 0.0;
        }

        return (runs * 100.0) / balls;
    }

    // ============================================================
    // AVERAGE
    // ============================================================

    /*
     * Average for one innings is simply the runs scored.
     *
     * Career average is calculated by DatabaseHelper because
     * it depends on multiple innings and dismissals.
     */
    public double getAverage() {
        return runs;
    }

    // ============================================================
    // OVERS FACED
    // ============================================================

    public String getOversFaced() {

        int completedOvers =
                balls / 6;

        int remainingBalls =
                balls % 6;

        return completedOvers
                + "."
                + remainingBalls;
    }

    // ============================================================
    // FIFTY
    // ============================================================

    public boolean isFifty() {

        return runs >= 50
                && runs < 100;
    }

    // ============================================================
    // CENTURY
    // ============================================================

    public boolean isCentury() {

        return runs >= 100;
    }

    // ============================================================
    // BOUNDARIES
    // ============================================================

    public int getBoundaryRuns() {

        return (fours * 4)
                + (sixes * 6);
    }

    // ============================================================
    // NOT OUT
    // ============================================================

    public boolean isNotOut() {

        return !out;
    }

    // ============================================================
    // DISPLAY
    // ============================================================

    public String getDismissalText() {

        if (!out) {
            return "Not Out";
        }

        if (howOut == null
                || howOut.trim().isEmpty()) {

            return "Out";
        }

        return howOut;
    }

    // ============================================================
    // RESET
    // ============================================================

    public void reset() {

        runs = 0;
        balls = 0;
        fours = 0;
        sixes = 0;
        out = false;
        howOut = "";
    }
}