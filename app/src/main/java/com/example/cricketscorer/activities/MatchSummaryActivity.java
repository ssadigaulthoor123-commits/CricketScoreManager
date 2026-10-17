package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cricketscorer.R;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.MatchInfo;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.models.Team;
import com.example.cricketscorer.models.BattingStat;
import com.example.cricketscorer.models.BowlingStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.cricketscorer.utils.ScoreUtils;

public class MatchSummaryActivity extends AppCompatActivity {

    private long matchId;
    private DatabaseHelper db;

    private TextView tvResult;
    private TextView tvTeamAName;
    private TextView tvTeamBName;
    private TextView tvTeamAScore;
    private TextView tvTeamBScore;
    private TextView tvPlayerOfMatch;
    private TextView tvPlayerOfMatchDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_match_summary
        );

        db =
                DatabaseHelper.getInstance(this);

        initializeViews();
        setupToolbar();

        matchId =
                getIntent().getLongExtra(
                        "match_id",
                        -1
                );

        if (matchId <= 0) {
            finish();
            return;
        }

        loadMatchSummary();
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        tvResult =
                findViewById(
                        R.id.tvResult
                );

        tvTeamAName =
                findViewById(
                        R.id.tvTeamAName
                );

        tvTeamBName =
                findViewById(
                        R.id.tvTeamBName
                );

        tvTeamAScore =
                findViewById(
                        R.id.tvTeamAScore
                );

        tvTeamBScore =
                findViewById(
                        R.id.tvTeamBScore
                );

        tvPlayerOfMatch =
                findViewById(
                        R.id.tvPlayerOfMatch
                );

        tvPlayerOfMatchDetails =
                findViewById(
                        R.id.tvPlayerOfMatchDetails
                );

        findViewById(
                R.id.btnViewScorecard
        ).setOnClickListener(
                v -> openScorecard()
        );

        findViewById(
                R.id.btnBackHome
        ).setOnClickListener(
                v -> goHome()
        );
    }

    // ============================================================
    // TOOLBAR
    // ============================================================

    private void setupToolbar() {

        Toolbar toolbar =
                findViewById(
                        R.id.toolbar
                );

        if (toolbar != null) {

            toolbar.setNavigationOnClickListener(
                    v -> goHome()
            );
        }
    }

    // ============================================================
    // LOAD MATCH SUMMARY
    // ============================================================

    private void loadMatchSummary() {

        MatchInfo match =
                db.getMatchById(
                        matchId
                );

        if (match == null) {
            finish();
            return;
        }

        tvResult.setText(
                safeText(
                        match.getResultText(),
                        "Match completed"
                )
        );

        tvTeamAName.setText(
                safeText(
                        match.getTeamAName(),
                        "Team A"
                )
        );

        tvTeamBName.setText(
                safeText(
                        match.getTeamBName(),
                        "Team B"
                )
        );

        displayScores(match);

        MomResult momResult =
                resolveAndPersistPlayerOfMatch(match);

        tvPlayerOfMatch.setText(
                momResult.name == null
                        || momResult.name.trim().isEmpty()
                        ? "—"
                        : momResult.name
        );

        if (tvPlayerOfMatchDetails != null) {
            tvPlayerOfMatchDetails.setText(
                    momResult.details == null
                            ? ""
                            : momResult.details
            );
        }

        TextView tvTossInfo = findViewById(R.id.tvTossInfo);
        if (tvTossInfo != null) {
            long tossWinnerId = match.getTossWinnerId();
            String tossWinnerName = tossWinnerId == match.getTeamAId() ? match.getTeamAName() : match.getTeamBName();
            String decision = match.getTossDecision() != null ? match.getTossDecision().toLowerCase() : "bat";
            tvTossInfo.setText(safeText(tossWinnerName, "Team") + " won the toss and elected to " + decision + " first");
        }
    }


    // ============================================================
    // PLAYER OF THE MATCH
    // ============================================================

    /**
     * Calculates Player of the Match only from the winning team.
     *
     * Batting impact considers runs, strike rate and boundaries.
     * Bowling impact considers wickets and economy.
     * The weighting changes with match length so short matches
     * reward quick impact while ODI matches reward sustained
     * performance.
     */
    private MomResult resolveAndPersistPlayerOfMatch(
            MatchInfo match
    ) {

        try {
            com.example.cricketscorer.utils.MomEvaluator.MomResult result =
                    com.example.cricketscorer.utils.MomEvaluator.evaluate(match, db);

            if (result != null && result.playerId > 0) {

                db.setPlayerOfMatch(
                        match.getId(),
                        result.playerId
                );

                match.setPlayerOfMatchId(
                        result.playerId
                );

                match.setPlayerOfMatchName(
                        result.manOfTheMatch
                );

                db.recalculateCareerStatistics();

                return new MomResult(
                        result.manOfTheMatch,
                        result.reason
                );
            }
        } catch (Exception e) {
            android.util.Log.e("CRIC_MOM_ERROR", "Error evaluating MOM: " + e.getMessage(), e);
        }

        return new MomResult("", "");
    }

    private double calculateBattingImpact(
            BattingStat stat,
            int totalOvers
    ) {
        double runs = stat.getRuns();
        double strikeRate = stat.getStrikeRate();
        int fours = stat.getFours();
        int sixes = stat.getSixes();

        double srBonus = Math.max(0.0, strikeRate - 100.0);
        double boundaryBonus;
        double milestoneBonus = 0.0;
        double srMultiplier;
        double runWeight;

        if (totalOvers <= 5) {
            // 5-over match: fast boundaries & quick runs are heavily rewarded
            runWeight = 0.15;
            srMultiplier = 0.40;
            boundaryBonus = fours * 2.5 + sixes * 4.5;
            if (runs >= 30) milestoneBonus = 15.0;
        } else if (totalOvers <= 20) {
            // T20 match: high strike rate & runs
            runWeight = 0.08;
            srMultiplier = 0.30;
            boundaryBonus = fours * 1.5 + sixes * 3.0;
            if (runs >= 50) milestoneBonus = 20.0;
        } else {
            // ODI match: big century (100 runs) gets massive reward
            runWeight = 0.05;
            srMultiplier = 0.20;
            boundaryBonus = fours * 1.0 + sixes * 2.0;
            if (runs >= 100) {
                milestoneBonus = 50.0 + (runs - 100) * 0.5;
            } else if (runs >= 50) {
                milestoneBonus = 15.0;
            }
        }

        return runs * (1.0 + runWeight)
                + srBonus * srMultiplier
                + boundaryBonus
                + milestoneBonus;
    }

    private double calculateBowlingImpact(
            BowlingStat stat,
            int totalOvers
    ) {
        int wickets = stat.getWickets();
        double economy = stat.getEconomy();

        double wicketWeight;
        double multiWicketBonus = 0.0;

        if (totalOvers <= 5) {
            // 5-over match: 2 or 3 quick wickets win it
            wicketWeight = 35.0;
            if (wickets >= 2) multiWicketBonus = 20.0 * (wickets - 1);
        } else if (totalOvers <= 20) {
            // T20 match: 3+ wickets usually wins it
            wicketWeight = 28.0;
            if (wickets >= 3) multiWicketBonus = 25.0 * (wickets - 2);
        } else {
            // ODI match: 4 to 5 wickets gets the award
            wicketWeight = 25.0;
            if (wickets >= 4) {
                multiWicketBonus = 35.0 + (wickets - 4) * 15.0;
            } else if (wickets == 3) {
                multiWicketBonus = 15.0;
            }
        }

        double economyBonus = Math.max(0.0, 10.0 - economy) * 2.5;

        return wickets * wicketWeight
                + multiWicketBonus
                + economyBonus;
    }

    private MomCandidate chooseBetterCandidate(
            MomCandidate current,
            MomCandidate challenger
    ) {

        if (current == null) {
            return challenger;
        }

        double difference =
                challenger.impact - current.impact;

        if (difference > 0.0001) {
            return challenger;
        }

        if (Math.abs(difference) <= 0.0001) {

            // First tie-break: faster batting scoring rate.
            if (challenger.strikeRate
                    > current.strikeRate + 0.0001) {
                return challenger;
            }

            if (Math.abs(
                    challenger.strikeRate
                            - current.strikeRate
            ) <= 0.0001) {

                // Second tie-break: lower bowling economy.
                if (challenger.economy
                        < current.economy - 0.0001) {
                    return challenger;
                }

                if (Math.abs(
                        challenger.economy
                                - current.economy
                ) <= 0.0001) {

                    // Third tie-break: wickets, then runs.
                    if (challenger.wickets
                            > current.wickets) {
                        return challenger;
                    }

                    if (challenger.wickets
                            == current.wickets
                            && challenger.runs
                            > current.runs) {
                        return challenger;
                    }
                }
            }
        }

        return current;
    }

    private static class MomResult {
        final String name;
        final String details;

        MomResult(String name, String details) {
            this.name = name;
            this.details = details;
        }
    }

    private static class MomCandidate {

        long playerId;
        String name;
        double impact;
        int runs;
        double strikeRate;
        int wickets;
        double economy;
        String details;

        MomCandidate(
                long playerId,
                String name,
                double impact,
                int runs,
                double strikeRate,
                int wickets,
                double economy,
                String details
        ) {

            this.playerId = playerId;
            this.name = name;
            this.impact = impact;
            this.runs = runs;
            this.strikeRate = strikeRate;
            this.wickets = wickets;
            this.economy = economy;
            this.details = details;
        }
    }

    // ============================================================
    // DISPLAY SCORES
    // ============================================================

    private void displayScores(
            MatchInfo match
    ) {

        /*
         * Innings 1 always belongs to the team that batted first.
         * Innings 2 belongs to the other team.
         *
         * Do NOT assume Team A batted first: the toss can make
         * Team B the first batting team.
         */

        long firstBattingTeamId =
                match.getFirstBattingTeamId();

        long secondBattingTeamId =
                match.getSecondBattingTeamId();

        int firstRuns = match.getInn1Runs();
        int firstWickets = match.getInn1Wickets();
        int firstBalls = match.getInn1Balls();

        int secondRuns = match.getInn2Runs();
        int secondWickets = match.getInn2Wickets();
        int secondBalls = match.getInn2Balls();

        int teamARuns;
        int teamAWickets;
        int teamABalls;

        int teamBRuns;
        int teamBWickets;
        int teamBBalls;

        if (firstBattingTeamId == match.getTeamAId()) {

            teamARuns = firstRuns;
            teamAWickets = firstWickets;
            teamABalls = firstBalls;

            teamBRuns = secondRuns;
            teamBWickets = secondWickets;
            teamBBalls = secondBalls;

        } else if (secondBattingTeamId == match.getTeamAId()) {

            teamARuns = secondRuns;
            teamAWickets = secondWickets;
            teamABalls = secondBalls;

            teamBRuns = firstRuns;
            teamBWickets = firstWickets;
            teamBBalls = firstBalls;

        } else {

            // Defensive fallback for old matches.
            teamARuns = firstRuns;
            teamAWickets = firstWickets;
            teamABalls = firstBalls;

            teamBRuns = secondRuns;
            teamBWickets = secondWickets;
            teamBBalls = secondBalls;
        }

        tvTeamAScore.setText(
                formatScore(
                        teamARuns,
                        teamAWickets,
                        teamABalls
                )
        );

        tvTeamBScore.setText(
                formatScore(
                        teamBRuns,
                        teamBWickets,
                        teamBBalls
                )
        );
    }

    // ============================================================
    // FORMAT SCORE
    // ============================================================

    private String formatScore(
            int runs,
            int wickets,
            int balls
    ) {

        return runs
                + "/"
                + wickets
                + " ("
                + ScoreUtils.formatOvers(
                balls
        )
                + ")";
    }

    // ============================================================
    // SAFE TEXT
    // ============================================================

    private String safeText(
            String value,
            String fallback
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return fallback;
        }

        return value;
    }

    // ============================================================
    // OPEN SCORECARD
    // ============================================================

    private void openScorecard() {

        if (matchId <= 0) {
            return;
        }

        Intent intent =
                new Intent(
                        this,
                        ScorecardActivity.class
                );

        intent.putExtra(
                "match_id",
                matchId
        );

        startActivity(intent);
    }

    // ============================================================
    // GO HOME
    // ============================================================

    private void goHome() {

        Intent intent =
                new Intent(
                        this,
                        DashboardActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
        );

        startActivity(intent);

        finish();
    }

    // ============================================================
    // BACK
    // ============================================================

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goHome();
    }
}