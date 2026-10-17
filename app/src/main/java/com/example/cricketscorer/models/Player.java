package com.example.cricketscorer.models;

import java.io.Serializable;

public class Player implements Serializable {

    private long id;

    private String name;
    private long teamId;
    private String teamName;

    private int jerseyNumber;

    private String role;

    private String battingHand = "Right Hand";

    private String bowlingStyle = "None";

    private String imageUri;

    // ============================================================
    // CAREER BATTING STATISTICS
    // ============================================================

    private int matches;
    private int innings;
    private int bowlingInnings;
    private int runs;
    private int balls;
    private int highestScore;
    private int fours;
    private int sixes;
    private int ducks;
    private int timesOut;

    // ============================================================
    // CAREER BOWLING STATISTICS
    // ============================================================

    private int ballsBowled;
    private int wickets;
    private int runsConceded;
    private int maidens;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public Player() {
    }

    public Player(
            long id,
            String name,
            long teamId,
            String teamName,
            int jerseyNumber,
            String role
    ) {

        this.id = id;
        this.name = name;
        this.teamId = teamId;
        this.teamName = teamName;
        this.jerseyNumber = jerseyNumber;
        this.role = role;
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
    // NAME
    // ============================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ============================================================
    // TEAM ID
    // ============================================================

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    // ============================================================
    // TEAM NAME
    // ============================================================

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    // ============================================================
    // JERSEY NUMBER
    // ============================================================

    public int getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(int jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    // ============================================================
    // ROLE
    // ============================================================

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ============================================================
    // WICKETKEEPER
    // ============================================================

    public boolean isWicketKeeper() {

        if (role == null) {
            return false;
        }

        String value =
                role.trim().toLowerCase();

        return value.equals("wicketkeeper")
                || value.equals("wicket keeper")
                || value.equals("wk")
                || value.contains("wicketkeeper")
                || value.contains("wicket keeper");
    }

    // ============================================================
    // BATTING HAND
    // ============================================================

    public String getBattingHand() {
        return battingHand;
    }

    public void setBattingHand(String battingHand) {

        if (battingHand == null
                || battingHand.trim().isEmpty()) {

            this.battingHand = "Right Hand";

        } else {

            this.battingHand =
                    battingHand.trim();
        }
    }

    public boolean isLeftHanded() {

        return "Left Hand".equalsIgnoreCase(
                battingHand
        );
    }

    // ============================================================
    // BOWLING STYLE
    // ============================================================

    public String getBowlingStyle() {
        return bowlingStyle;
    }

    public void setBowlingStyle(String bowlingStyle) {

        if (bowlingStyle == null
                || bowlingStyle.trim().isEmpty()) {

            this.bowlingStyle = "None";

            return;
        }

        String value =
                bowlingStyle.trim();

        if ("none".equalsIgnoreCase(value)) {

            this.bowlingStyle = "None";

        } else {

            this.bowlingStyle = value;
        }
    }

    public boolean hasBowlingStyle() {

        return bowlingStyle != null
                && !bowlingStyle.trim().isEmpty()
                && !"None".equalsIgnoreCase(
                bowlingStyle.trim()
        );
    }

    // ============================================================
    // IMAGE URI
    // ============================================================

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    // ============================================================
    // MATCHES
    // ============================================================

    public int getMatches() {
        return matches;
    }

    public void setMatches(int matches) {
        this.matches = Math.max(matches, 0);
    }

    // ============================================================
    // INNINGS
    // ============================================================

    public int getInnings() {
        return innings;
    }

    public void setInnings(int innings) {
        this.innings = Math.max(innings, 0);
    }

    // ============================================================
    // BOWLING INNINGS
    // ============================================================

    public int getBowlingInnings() {
        return bowlingInnings;
    }

    public void setBowlingInnings(int bowlingInnings) {
        this.bowlingInnings = Math.max(bowlingInnings, 0);
    }

    // ============================================================
    // RUNS
    // ============================================================

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = Math.max(runs, 0);
    }

    // ============================================================
    // BALLS FACED
    // ============================================================

    public int getBalls() {
        return balls;
    }

    public void setBalls(int balls) {
        this.balls = Math.max(balls, 0);
    }

    // ============================================================
    // HIGHEST SCORE
    // ============================================================

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore =
                Math.max(highestScore, 0);
    }

    // ============================================================
    // FOURS
    // ============================================================

    public int getFours() {
        return fours;
    }

    public void setFours(int fours) {
        this.fours = Math.max(fours, 0);
    }

    // ============================================================
    // SIXES
    // ============================================================

    public int getSixes() {
        return sixes;
    }

    public void setSixes(int sixes) {
        this.sixes = Math.max(sixes, 0);
    }

    // ============================================================
    // DUCKS
    // ============================================================

    public int getDucks() {
        return ducks;
    }

    public void setDucks(int ducks) {
        this.ducks = Math.max(ducks, 0);
    }

    // ============================================================
    // TIMES OUT
    // ============================================================

    public int getTimesOut() {
        return timesOut;
    }

    public void setTimesOut(int timesOut) {
        this.timesOut =
                Math.max(timesOut, 0);
    }

    // ============================================================
    // BALLS BOWLED
    // ============================================================

    public int getBallsBowled() {
        return ballsBowled;
    }

    public void setBallsBowled(int ballsBowled) {
        this.ballsBowled =
                Math.max(ballsBowled, 0);
    }

    // ============================================================
    // WICKETS
    // ============================================================

    public int getWickets() {
        return wickets;
    }

    public void setWickets(int wickets) {
        this.wickets =
                Math.max(wickets, 0);
    }

    // ============================================================
    // RUNS CONCEDED
    // ============================================================

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(int runsConceded) {
        this.runsConceded =
                Math.max(runsConceded, 0);
    }

    // ============================================================
    // MAIDENS
    // ============================================================

    private int notOuts;
    private int thirties;
    private int fifties;
    private int centuries;
    private int momCount;
    private int extrasConceded;
    private int dotBalls;
    private int threeWicketHauls;
    private int fiveWicketHauls;
    private String bestBowling = "0/0";
    private int catches;
    private int runOuts;
    private int stumpings;

    public int getMaidens() {
        return maidens;
    }

    public void setMaidens(int maidens) {
        this.maidens = Math.max(maidens, 0);
    }

    public int getNotOuts() { return notOuts; }
    public void setNotOuts(int notOuts) { this.notOuts = Math.max(0, notOuts); }
    public int getThirties() { return thirties; }
    public void setThirties(int thirties) { this.thirties = Math.max(0, thirties); }
    public int getFifties() { return fifties; }
    public void setFifties(int fifties) { this.fifties = Math.max(0, fifties); }
    public int getCenturies() { return centuries; }
    public void setCenturies(int centuries) { this.centuries = Math.max(0, centuries); }
    public int getMomCount() { return momCount; }
    public void setMomCount(int momCount) { this.momCount = Math.max(0, momCount); }
    public int getExtrasConceded() { return extrasConceded; }
    public void setExtrasConceded(int extrasConceded) { this.extrasConceded = Math.max(0, extrasConceded); }
    public int getDotBalls() { return dotBalls; }
    public void setDotBalls(int dotBalls) { this.dotBalls = Math.max(0, dotBalls); }
    public int getThreeWicketHauls() { return threeWicketHauls; }
    public void setThreeWicketHauls(int threeWicketHauls) { this.threeWicketHauls = Math.max(0, threeWicketHauls); }
    public int getFiveWicketHauls() { return fiveWicketHauls; }
    public void setFiveWicketHauls(int fiveWicketHauls) { this.fiveWicketHauls = Math.max(0, fiveWicketHauls); }
    public String getBestBowling() { return bestBowling == null ? "0/0" : bestBowling; }
    public void setBestBowling(String bestBowling) { this.bestBowling = bestBowling == null ? "0/0" : bestBowling; }
    public int getCatches() { return catches; }
    public void setCatches(int catches) { this.catches = Math.max(0, catches); }
    public int getRunOuts() { return runOuts; }
    public void setRunOuts(int runOuts) { this.runOuts = Math.max(0, runOuts); }
    public int getStumpings() { return stumpings; }
    public void setStumpings(int stumpings) { this.stumpings = Math.max(0, stumpings); }

    // ============================================================
    // STRING
    // ============================================================

    @Override
    public String toString() {

        if (name == null || name.trim().isEmpty()) {
            return "Unknown Player";
        }

        return name;
    }
}