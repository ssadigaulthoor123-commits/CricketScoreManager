package com.example.cricketscorer.utils;

import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.BattingStat;
import com.example.cricketscorer.models.BowlingStat;
import com.example.cricketscorer.models.MatchInfo;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.models.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MomEvaluator {

    public static class MomResult {
        public final String manOfTheMatch;
        public final long playerId;
        public final double score;
        public final String reason;
        public final Map<String, Integer> performance;

        public MomResult(String manOfTheMatch, long playerId, double score, String reason, Map<String, Integer> performance) {
            this.manOfTheMatch = manOfTheMatch;
            this.playerId = playerId;
            this.score = score;
            this.reason = reason;
            this.performance = performance;
        }
    }

    public static MomResult evaluate(MatchInfo match, DatabaseHelper db) {
        if (match == null || db == null) {
            return new MomResult("None", 0, 0, "No performance data available", new HashMap<>());
        }

        int totalOvers = Math.max(1, match.getTotalOvers());
        String format = match.getMatchFormat() != null ? match.getMatchFormat().toUpperCase() : "T20";

        double oversScalingFactor = Math.pow(50.0 / totalOvers, 0.4);
        double expectedStrikeRate;
        if ("ODI".equalsIgnoreCase(format) || totalOvers >= 40) {
            expectedStrikeRate = 85.0;
        } else if ("T20".equalsIgnoreCase(format) || totalOvers >= 15) {
            expectedStrikeRate = 135.0;
        } else {
            expectedStrikeRate = Math.min(250.0, 140.0 + (100.0 / totalOvers));
        }

        long firstBattingTeamId = match.getFirstBattingTeamId();
        long secondBattingTeamId = match.getSecondBattingTeamId();

        int inn1Runs = match.getInn1Runs();
        int inn2Runs = match.getInn2Runs();

        long winningTeamId = -1;
        if (inn1Runs > inn2Runs) {
            winningTeamId = firstBattingTeamId;
        } else if (inn2Runs > inn1Runs) {
            winningTeamId = secondBattingTeamId;
        }

        List<BattingStat> allBatting = new ArrayList<>();
        allBatting.addAll(db.getBattingStats(match.getId(), 1));
        allBatting.addAll(db.getBattingStats(match.getId(), 2));

        List<BowlingStat> allBowling = new ArrayList<>();
        allBowling.addAll(db.getBowlingStats(match.getId(), 1));
        allBowling.addAll(db.getBowlingStats(match.getId(), 2));

        class CandidateScore {
            long playerId;
            String name;
            long teamId;
            int runs = 0;
            int balls = 0;
            int fours = 0;
            int sixes = 0;
            boolean notOut = false;

            int wickets = 0;
            int ballsBowled = 0;
            int runsConceded = 0;
            int maidens = 0;
            int dotBalls = 0;

            int catches = 0;
            int runOuts = 0;
            int stumpings = 0;

            double battingImpact = 0;
            double bowlingImpact = 0;
            double fieldingImpact = 0;
            double totalScore = 0;
        }

        Map<Long, CandidateScore> candidateMap = new HashMap<>();

        // Process Batting
        for (BattingStat b : allBatting) {
            if (b == null || b.getPlayerId() <= 0) continue;
            long pid = b.getPlayerId();
            CandidateScore c = candidateMap.computeIfAbsent(pid, k -> {
                CandidateScore newC = new CandidateScore();
                newC.playerId = pid;
                newC.name = b.getPlayerName();
                Player p = db.getPlayerById(pid);
                if (p != null) newC.teamId = p.getTeamId();
                return newC;
            });

            c.runs += b.getRuns();
            c.balls += b.getBalls();
            c.fours += b.getFours();
            c.sixes += b.getSixes();
            if (!b.isOut()) c.notOut = true;

            double scaledRuns = b.getRuns() * oversScalingFactor;
            double sr = b.getBalls() > 0 ? (b.getRuns() * 100.0 / b.getBalls()) : 0.0;
            double srFactor = b.getBalls() >= 3 ? (sr / expectedStrikeRate) : 1.0;

            double bImpact = scaledRuns * Math.min(2.0, Math.max(0.5, srFactor));
            bImpact += (b.getFours() * 1.5) + (b.getSixes() * 3.0);

            if (!b.isOut() && b.getRuns() >= (totalOvers * 1.5)) {
                bImpact *= 1.15;
            }
            if (b.getRuns() >= 50) bImpact += 15.0;
            if (b.getRuns() >= 100) bImpact += 30.0;

            c.battingImpact += bImpact;
        }

        // Process Bowling
        for (BowlingStat bw : allBowling) {
            if (bw == null || bw.getPlayerId() <= 0) continue;
            long pid = bw.getPlayerId();
            CandidateScore c = candidateMap.computeIfAbsent(pid, k -> {
                CandidateScore newC = new CandidateScore();
                newC.playerId = pid;
                newC.name = bw.getPlayerName();
                Player p = db.getPlayerById(pid);
                if (p != null) newC.teamId = p.getTeamId();
                return newC;
            });

            c.wickets += bw.getWickets();
            c.ballsBowled += bw.getBalls();
            c.runsConceded += bw.getRunsConceded();
            c.maidens += bw.getMaidens();
            c.dotBalls += bw.getDotBalls();

            double wImpact = bw.getWickets() * 25.0 * Math.pow(50.0 / totalOvers, 0.3);
            if (bw.getWickets() >= 3) wImpact += 15.0;
            if (bw.getWickets() >= 5) wImpact += 30.0;

            double parEconomy = (totalOvers <= 5) ? 12.0 : (totalOvers <= 20) ? 8.0 : 5.0;
            double econ = bw.getBalls() > 0 ? (bw.getRunsConceded() / (bw.getBalls() / 6.0)) : parEconomy;
            double econBonus = (parEconomy - econ) * (bw.getBalls() / 6.0) * 3.0;

            double bwImpact = wImpact + econBonus + (bw.getMaidens() * 12.0) + (bw.getDotBalls() * 1.5);
            c.bowlingImpact += Math.max(0, bwImpact);
        }

        // Process Fielding from BattingStats
        for (BattingStat b : allBatting) {
            if (b != null && b.getFielderId() > 0 && b.isOut()) {
                long fid = b.getFielderId();
                CandidateScore c = candidateMap.computeIfAbsent(fid, k -> {
                    CandidateScore newC = new CandidateScore();
                    newC.playerId = fid;
                    Player p = db.getPlayerById(fid);
                    if (p != null) {
                        newC.name = p.getName();
                        newC.teamId = p.getTeamId();
                    }
                    return newC;
                });

                String how = b.getHowOut() != null ? b.getHowOut() : "";
                if ("Caught".equalsIgnoreCase(how)) {
                    c.catches++;
                    c.fieldingImpact += 12.0;
                } else if ("Run Out".equalsIgnoreCase(how)) {
                    c.runOuts++;
                    c.fieldingImpact += 20.0;
                } else if ("Stumped".equalsIgnoreCase(how)) {
                    c.stumpings++;
                    c.fieldingImpact += 18.0;
                }
            }
        }

        CandidateScore winnerCandidate = null;

        for (CandidateScore c : candidateMap.values()) {
            double total = c.battingImpact + c.bowlingImpact + c.fieldingImpact;

            boolean hasBat = c.runs >= Math.max(10, totalOvers * 1.0);
            boolean hasBowl = c.wickets >= 1;
            boolean hasField = (c.catches + c.runOuts + c.stumpings) >= 1;

            if ((hasBat && hasBowl) || (hasBat && hasField) || (hasBowl && hasField)) {
                total *= 1.18;
            }

            if (winningTeamId > 0 && c.teamId == winningTeamId) {
                total *= 1.15;
            }

            c.totalScore = total;

            if (winnerCandidate == null || c.totalScore > winnerCandidate.totalScore) {
                winnerCandidate = c;
            }
        }

        if (winnerCandidate == null || winnerCandidate.playerId <= 0) {
            return new MomResult("None", 0, 0, "No standout performance", new HashMap<>());
        }

        Player momPlayer = db.getPlayerById(winnerCandidate.playerId);
        String teamName = "";
        if (momPlayer != null) {
            teamName = momPlayer.getTeamName();
            if ((teamName == null || teamName.trim().isEmpty()) && momPlayer.getTeamId() > 0) {
                Team team = db.getTeamById(momPlayer.getTeamId());
                if (team != null && team.getName() != null) {
                    teamName = team.getName().trim();
                }
            }
        }

        String displayName = winnerCandidate.name;
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = momPlayer != null ? momPlayer.getName() : "Player " + winnerCandidate.playerId;
        }

        if (teamName != null && !teamName.trim().isEmpty()) {
            displayName += " (" + teamName + ")";
        }

        List<String> summaryParts = new ArrayList<>();

        if (winnerCandidate.runs > 0) {
            String batText = winnerCandidate.runs + " (" + winnerCandidate.balls + ")";
            if (winnerCandidate.fours > 0 || winnerCandidate.sixes > 0) {
                batText += " with " + winnerCandidate.fours + "x4, " + winnerCandidate.sixes + "x6";
            }
            summaryParts.add(batText);
        }

        if (winnerCandidate.wickets > 0 || winnerCandidate.ballsBowled > 0) {
            String ov = ScoreUtils.formatOvers(winnerCandidate.ballsBowled);
            String bowlText = winnerCandidate.wickets + "/" + winnerCandidate.runsConceded + " (" + ov + " ov)";
            if (winnerCandidate.maidens > 0) {
                bowlText += " incl. " + winnerCandidate.maidens + " maiden";
            }
            summaryParts.add(bowlText);
        }

        if (winnerCandidate.catches > 0 || winnerCandidate.runOuts > 0 || winnerCandidate.stumpings > 0) {
            List<String> fieldParts = new ArrayList<>();
            if (winnerCandidate.catches > 0) fieldParts.add(winnerCandidate.catches + " catch" + (winnerCandidate.catches > 1 ? "es" : ""));
            if (winnerCandidate.runOuts > 0) fieldParts.add(winnerCandidate.runOuts + " run-out");
            if (winnerCandidate.stumpings > 0) fieldParts.add(winnerCandidate.stumpings + " stumping" + (winnerCandidate.stumpings > 1 ? "s" : ""));
            summaryParts.add(joinStrings(", ", fieldParts));
        }

        StringBuilder reasonSb = new StringBuilder();
        if (summaryParts.isEmpty()) {
            reasonSb.append("Outstanding match performance.");
        } else {
            reasonSb.append(displayName).append(" — ").append(joinStrings(" & ", summaryParts)).append(".");
        }

        Map<String, Integer> perf = new HashMap<>();
        perf.put("runs", winnerCandidate.runs);
        perf.put("balls", winnerCandidate.balls);
        perf.put("wickets", winnerCandidate.wickets);
        perf.put("runsConceded", winnerCandidate.runsConceded);
        perf.put("catches", winnerCandidate.catches);
        perf.put("runOuts", winnerCandidate.runOuts);
        perf.put("stumpings", winnerCandidate.stumpings);

        return new MomResult(displayName, winnerCandidate.playerId, Math.round(winnerCandidate.totalScore * 10.0) / 10.0, reasonSb.toString(), perf);
    }

    private static String joinStrings(String delimiter, List<String> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(items.get(i));
        }
        return sb.toString();
    }
}
