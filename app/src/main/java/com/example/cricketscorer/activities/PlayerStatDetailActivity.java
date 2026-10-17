package com.example.cricketscorer.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cricketscorer.R;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.models.Team;
import com.example.cricketscorer.utils.ScoreUtils;

import java.util.Locale;

public class PlayerStatDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private long playerId;

    // ============================================================
    // HEADER
    // ============================================================

    private TextView tvJersey;
    private TextView tvName;
    private TextView tvMeta;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_player_stat_detail
        );

        db =
                DatabaseHelper.getInstance(this);

        playerId =
                getIntent().getLongExtra(
                        "player_id",
                        -1
                );

        initializeViews();
        setupToolbar();

        if (playerId <= 0) {
            finish();
            return;
        }

        loadPlayer();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (db != null && playerId > 0) {
            loadPlayer();
        }
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        tvJersey =
                findViewById(
                        R.id.tvJersey
                );

        tvName =
                findViewById(
                        R.id.tvName
                );

        tvMeta =
                findViewById(
                        R.id.tvMeta
                );
    }

    // ============================================================
    // SET STAT HELPER
    // ============================================================

    private void setStat(
            int containerId,
            String value,
            String label
    ) {

        android.view.View container =
                findViewById(
                        containerId
                );

        if (container != null) {

            TextView tvVal =
                    container.findViewById(
                            R.id.statValue
                    );

            TextView tvLab =
                    container.findViewById(
                            R.id.statLabel
                    );

            if (tvVal != null) {
                tvVal.setText(
                        value == null
                                ? "—"
                                : value
                );
            }

            if (tvLab != null) {
                tvLab.setText(
                        label
                );
            }
        }
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
                    v -> finish()
            );
        }
    }

    // ============================================================
    // LOAD PLAYER
    // ============================================================

    private void loadPlayer() {

        if (db != null) {
            db.recalculateCareerStatistics();
        }

        Player player =
                db.getPlayerById(
                        playerId
                );

        if (player == null) {
            finish();
            return;
        }

        if (player.getTeamId() > 0) {
            Team team = db.getTeamById(player.getTeamId());
            if (team != null && team.getName() != null && !team.getName().trim().isEmpty()) {
                player.setTeamName(team.getName().trim());
            }
        }

        displayHeader(player);

        displayAwards(player);

        displayBattingStatistics(player);

        displayBowlingStatistics(player);

        displayFieldingStatistics(player);
    }

    // ============================================================
    // HEADER
    // ============================================================

    private void displayHeader(
            Player player
    ) {

        int jersey =
                player.getJerseyNumber();

        if (jersey > 0) {

            tvJersey.setText(
                    String.valueOf(jersey)
            );

        } else {

            tvJersey.setText("—");
        }

        tvName.setText(
                safe(
                        player.getName(),
                        "Unknown Player"
                )
        );

        String team =
                safe(
                        player.getTeamName(),
                        "No Team"
                );

        String role =
                safe(
                        player.getRole(),
                        "Player"
                );

        tvMeta.setText(
                team
                        + " • "
                        + role
        );
    }

    // ============================================================
    // AWARDS
    // ============================================================

    private void displayAwards(Player player) {
        setStat(
                R.id.statMoM,
                String.valueOf(player.getMomCount()),
                "Man of Match Awards"
        );
    }

    // ============================================================
    // BATTING STATISTICS
    // ============================================================

    private void displayBattingStatistics(
            Player player
    ) {

        int matches =
                player.getMatches();

        int innings =
                player.getInnings();

        int runs =
                player.getRuns();

        int balls =
                player.getBalls();

        int highestScore =
                player.getHighestScore();

        int fours =
                player.getFours();

        int sixes =
                player.getSixes();

        int ducks =
                player.getDucks();

        int notOuts =
                player.getNotOuts();

        int thirties =
                player.getThirties();

        int fifties =
                player.getFifties();

        int centuries =
                player.getCenturies();

        int timesOut =
                player.getTimesOut();

        double average =
                calculateAverage(
                        runs,
                        timesOut
                );

        double strikeRate =
                calculateStrikeRate(
                        runs,
                        balls
                );

        setStat(
                R.id.statMatches,
                String.valueOf(matches),
                "Matches"
        );

        setStat(
                R.id.statInnings,
                String.valueOf(innings),
                "Innings"
        );

        setStat(
                R.id.statRuns,
                String.valueOf(runs),
                "Runs"
        );

        setStat(
                R.id.statNotOuts,
                String.valueOf(notOuts),
                "Not Outs"
        );

        setStat(
                R.id.statAvg,
                formatDecimal(average),
                "Average"
        );

        setStat(
                R.id.statSR,
                formatDecimal(strikeRate),
                "Strike Rate"
        );

        setStat(
                R.id.statHS,
                String.valueOf(highestScore),
                "Best Score"
        );

        setStat(
                R.id.statFours,
                String.valueOf(fours),
                "4s"
        );

        setStat(
                R.id.statSixes,
                String.valueOf(sixes),
                "6s"
        );

        setStat(
                R.id.statThirties,
                String.valueOf(thirties),
                "30s"
        );

        setStat(
                R.id.statFifties,
                String.valueOf(fifties),
                "50s"
        );

        setStat(
                R.id.statCenturies,
                String.valueOf(centuries),
                "100s"
        );

        setStat(
                R.id.statDucks,
                String.valueOf(ducks),
                "Ducks"
        );
    }

    // ============================================================
    // BOWLING STATISTICS
    // ============================================================

    private void displayBowlingStatistics(
            Player player
    ) {

        int ballsBowled =
                player.getBallsBowled();

        int wickets =
                player.getWickets();

        int runsConceded =
                player.getRunsConceded();

        int maidens =
                player.getMaidens();

        int extras =
                player.getExtrasConceded();

        int dotBalls =
                player.getDotBalls();

        int threeW =
                player.getThreeWicketHauls();

        int fiveW =
                player.getFiveWicketHauls();

        String bestBowling =
                player.getBestBowling();

        double economy =
                calculateEconomy(
                        ballsBowled,
                        runsConceded
                );

        String overs =
                ScoreUtils.formatOvers(
                        ballsBowled
                );

        setStat(
                R.id.statOvers,
                overs,
                "Overs"
        );

        setStat(
                R.id.statWickets,
                String.valueOf(wickets),
                "Wickets"
        );

        setStat(
                R.id.statBowlingInnings,
                String.valueOf(player.getBowlingInnings()),
                "Bowling Innings"
        );

        setStat(
                R.id.statRunsGiven,
                String.valueOf(runsConceded),
                "Runs Given"
        );

        setStat(
                R.id.statEconomy,
                formatDecimal(economy),
                "Economy"
        );

        setStat(
                R.id.statMaidens,
                String.valueOf(maidens),
                "Maidens"
        );

        setStat(
                R.id.statExtras,
                String.valueOf(extras),
                "Extras"
        );

        setStat(
                R.id.statDotBalls,
                String.valueOf(dotBalls),
                "Dot Balls"
        );

        setStat(
                R.id.stat3W,
                String.valueOf(threeW),
                "3W Hauls"
        );

        setStat(
                R.id.stat5W,
                String.valueOf(fiveW),
                "5W Hauls"
        );

        setStat(
                R.id.statBestBowling,
                bestBowling,
                "Best Bowling"
        );
    }

    // ============================================================
    // FIELDING STATISTICS
    // ============================================================

    private void displayFieldingStatistics(
            Player player
    ) {

        setStat(
                R.id.statCatches,
                String.valueOf(player.getCatches()),
                "Catches"
        );

        setStat(
                R.id.statRunOuts,
                String.valueOf(player.getRunOuts()),
                "Run Outs"
        );

        setStat(
                R.id.statStumpings,
                String.valueOf(player.getStumpings()),
                "Stumpings"
        );
    }

    // ============================================================
    // BATTING AVERAGE
    // ============================================================

    private double calculateAverage(
            int runs,
            int timesOut
    ) {

        if (timesOut <= 0) {

            if (runs <= 0) {
                return 0.0;
            }

            return runs;
        }

        return (double) runs
                / timesOut;
    }

    // ============================================================
    // STRIKE RATE
    // ============================================================

    private double calculateStrikeRate(
            int runs,
            int balls
    ) {

        if (balls <= 0) {
            return 0.0;
        }

        return (
                (double) runs
                        / balls
        ) * 100.0;
    }

    // ============================================================
    // ECONOMY
    // ============================================================

    private double calculateEconomy(
            int balls,
            int runs
    ) {

        if (balls <= 0) {
            return 0.0;
        }

        return (
                (double) runs
                        / balls
        ) * 6.0;
    }

    // ============================================================
    // DECIMAL FORMAT
    // ============================================================

    private String formatDecimal(
            double value
    ) {

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }

    // ============================================================
    // SAFE TEXT
    // ============================================================

    private String safe(
            String value,
            String fallback
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return fallback;
        }

        return value;
    }
}
