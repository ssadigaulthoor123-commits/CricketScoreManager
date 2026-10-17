package com.example.cricketscorer.models;

import java.io.Serializable;

/**
 * Represents one ball delivered during a cricket match.
 *
 * This model is intentionally independent from the database so it can
 * also be used for temporary in-memory ball-by-ball scoring.
 */
public class BallEvent implements Serializable {

    private long id;

    private long matchId;

    private int innings;

    private int overNumber;

    private int ballNumber;

    private long strikerId;

    private long nonStrikerId;

    private long bowlerId;

    private String strikerName;

    private String nonStrikerName;

    private String bowlerName;

    private int runsOffBat;

    private int totalRuns;

    private int extras;

    private int wides;

    private int noBalls;

    private int byes;

    private int legByes;

    private int penaltyRuns;

    private boolean wicket;

    private String wicketType;

    private long dismissedPlayerId;

    private String dismissedPlayerName;

    private boolean legalDelivery;

    private String description;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public BallEvent() {

        id = 0;

        matchId = 0;

        innings = 1;

        overNumber = 0;

        ballNumber = 0;

        strikerId = 0;

        nonStrikerId = 0;

        bowlerId = 0;

        strikerName = "";

        nonStrikerName = "";

        bowlerName = "";

        runsOffBat = 0;

        totalRuns = 0;

        extras = 0;

        wides = 0;

        noBalls = 0;

        byes = 0;

        legByes = 0;

        penaltyRuns = 0;

        wicket = false;

        wicketType = "";

        dismissedPlayerId = 0;

        dismissedPlayerName = "";

        legalDelivery = true;

        description = "";
    }

    public BallEvent(
            long matchId,
            int innings,
            int overNumber,
            int ballNumber
    ) {

        this();

        this.matchId = matchId;
        this.innings = innings;
        this.overNumber = overNumber;
        this.ballNumber = ballNumber;
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
    // MATCH
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
    // OVER / BALL
    // ============================================================

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = Math.max(0, overNumber);
    }

    public int getBallNumber() {
        return ballNumber;
    }

    public void setBallNumber(int ballNumber) {
        this.ballNumber = Math.max(0, ballNumber);
    }

    /**
     * Returns the display format such as 4.3 for
     * fourth over and third legal delivery.
     */
    public String getBallDisplay() {

        return overNumber + "." + ballNumber;
    }

    // ============================================================
    // PLAYERS
    // ============================================================

    public long getStrikerId() {
        return strikerId;
    }

    public void setStrikerId(long strikerId) {
        this.strikerId = strikerId;
    }

    public long getNonStrikerId() {
        return nonStrikerId;
    }

    public void setNonStrikerId(long nonStrikerId) {
        this.nonStrikerId = nonStrikerId;
    }

    public long getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(long bowlerId) {
        this.bowlerId = bowlerId;
    }

    public String getStrikerName() {
        return strikerName;
    }

    public void setStrikerName(String strikerName) {
        this.strikerName =
                strikerName == null ? "" : strikerName;
    }

    public String getNonStrikerName() {
        return nonStrikerName;
    }

    public void setNonStrikerName(String nonStrikerName) {
        this.nonStrikerName =
                nonStrikerName == null ? "" : nonStrikerName;
    }

    public String getBowlerName() {
        return bowlerName;
    }

    public void setBowlerName(String bowlerName) {
        this.bowlerName =
                bowlerName == null ? "" : bowlerName;
    }

    // ============================================================
    // BAT RUNS
    // ============================================================

    public int getRunsOffBat() {
        return runsOffBat;
    }

    public void setRunsOffBat(int runsOffBat) {
        this.runsOffBat = Math.max(0, runsOffBat);
        recalculateTotalRuns();
    }

    // ============================================================
    // TOTAL RUNS
    // ============================================================

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = Math.max(0, totalRuns);
    }

    // ============================================================
    // EXTRAS
    // ============================================================

    public int getExtras() {
        return extras;
    }

    public void setExtras(int extras) {
        this.extras = Math.max(0, extras);
        recalculateTotalRuns();
    }

    public int getWides() {
        return wides;
    }

    public void setWides(int wides) {
        this.wides = Math.max(0, wides);
        recalculateExtras();
    }

    public int getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(int noBalls) {
        this.noBalls = Math.max(0, noBalls);
        recalculateExtras();
    }

    public int getByes() {
        return byes;
    }

    public void setByes(int byes) {
        this.byes = Math.max(0, byes);
        recalculateExtras();
    }

    public int getLegByes() {
        return legByes;
    }

    public void setLegByes(int legByes) {
        this.legByes = Math.max(0, legByes);
        recalculateExtras();
    }

    public int getPenaltyRuns() {
        return penaltyRuns;
    }

    public void setPenaltyRuns(int penaltyRuns) {
        this.penaltyRuns = Math.max(0, penaltyRuns);
        recalculateExtras();
    }

    // ============================================================
    // WICKET
    // ============================================================

    public boolean isWicket() {
        return wicket;
    }

    public void setWicket(boolean wicket) {
        this.wicket = wicket;
    }

    public String getWicketType() {
        return wicketType;
    }

    public void setWicketType(String wicketType) {
        this.wicketType =
                wicketType == null ? "" : wicketType;
    }

    public long getDismissedPlayerId() {
        return dismissedPlayerId;
    }

    public void setDismissedPlayerId(long dismissedPlayerId) {
        this.dismissedPlayerId = dismissedPlayerId;
    }

    public String getDismissedPlayerName() {
        return dismissedPlayerName;
    }

    public void setDismissedPlayerName(
            String dismissedPlayerName
    ) {

        this.dismissedPlayerName =
                dismissedPlayerName == null
                        ? ""
                        : dismissedPlayerName;
    }

    // ============================================================
    // DELIVERY TYPE
    // ============================================================

    public boolean isLegalDelivery() {
        return legalDelivery;
    }

    public void setLegalDelivery(boolean legalDelivery) {
        this.legalDelivery = legalDelivery;
    }

    // ============================================================
    // DESCRIPTION
    // ============================================================

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description =
                description == null
                        ? ""
                        : description;
    }

    // ============================================================
    // CALCULATIONS
    // ============================================================

    private void recalculateExtras() {

        extras =
                wides
                        + noBalls
                        + byes
                        + legByes
                        + penaltyRuns;

        recalculateTotalRuns();
    }

    private void recalculateTotalRuns() {

        totalRuns =
                runsOffBat + extras;
    }

    /**
     * Returns true when the delivery is a wide.
     */
    public boolean isWide() {
        return wides > 0;
    }

    /**
     * Returns true when the delivery is a no-ball.
     */
    public boolean isNoBall() {
        return noBalls > 0;
    }

    /**
     * Returns true when the delivery contains byes.
     */
    public boolean isBye() {
        return byes > 0;
    }

    /**
     * Returns true when the delivery contains leg-byes.
     */
    public boolean isLegBye() {
        return legByes > 0;
    }

    /**
     * A legal delivery is one that counts toward the over.
     */
    public boolean countsAsLegalBall() {
        return legalDelivery
                && !isWide()
                && !isNoBall();
    }

    // ============================================================
    // DISPLAY HELPERS
    // ============================================================

    public String getRunsDisplay() {

        if (isWide()) {
            return wides + "Wd";
        }

        if (isNoBall()) {
            return noBalls + "Nb";
        }

        if (isBye()) {
            return byes + "B";
        }

        if (isLegBye()) {
            return legByes + "Lb";
        }

        if (runsOffBat == 0) {
            return "0";
        }

        return String.valueOf(runsOffBat);
    }

    public String getWicketDisplay() {

        if (!wicket) {
            return "";
        }

        if (wicketType == null
                || wicketType.trim().isEmpty()) {

            return "W";
        }

        return "W (" + wicketType + ")";
    }

    // ============================================================
    // RESET
    // ============================================================

    public void reset() {

        id = 0;

        matchId = 0;

        innings = 1;

        overNumber = 0;

        ballNumber = 0;

        strikerId = 0;

        nonStrikerId = 0;

        bowlerId = 0;

        strikerName = "";

        nonStrikerName = "";

        bowlerName = "";

        runsOffBat = 0;

        totalRuns = 0;

        extras = 0;

        wides = 0;

        noBalls = 0;

        byes = 0;

        legByes = 0;

        penaltyRuns = 0;

        wicket = false;

        wicketType = "";

        dismissedPlayerId = 0;

        dismissedPlayerName = "";

        legalDelivery = true;

        description = "";
    }

    // ============================================================
    // STRING
    // ============================================================

    @Override
    public String toString() {

        return getBallDisplay()
                + " - "
                + getRunsDisplay()
                + (wicket ? " W" : "");
    }
}