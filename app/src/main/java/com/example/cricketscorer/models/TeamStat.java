package com.example.cricketscorer.models;

import java.io.Serializable;

public class TeamStat implements Serializable {
    private long teamId;
    private String teamName;
    private int matchesPlayed;
    private int wins;
    private int losses;
    private int ties;

    public TeamStat() {}

    public long getTeamId() { return teamId; }
    public void setTeamId(long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName == null ? "" : teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName == null ? "" : teamName; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = Math.max(0, matchesPlayed); }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = Math.max(0, wins); }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = Math.max(0, losses); }
    public int getTies() { return ties; }
    public void setTies(int ties) { this.ties = Math.max(0, ties); }
}
