package com.example.cricketscorer.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cricketscorer.R;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.BattingStat;
import com.example.cricketscorer.models.BowlingStat;
import com.example.cricketscorer.models.MatchInfo;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.models.Team;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScorecardActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private long matchId;

    private MatchInfo match;

    // ============================================================
    // VIEWS
    // ============================================================

    private Toolbar toolbar;

    private TabLayout tabLayout;

    private TextView tvInningsHeader;

    private LinearLayout layoutBatting;

    private LinearLayout layoutBowling;

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_scorecard
        );

        db =
                DatabaseHelper.getInstance(this);

        matchId =
                getIntent().getLongExtra(
                        "match_id",
                        -1
                );

        initializeViews();

        setupToolbar();

        if (matchId <= 0) {
            finish();
            return;
        }

        loadScorecard();
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );

        tabLayout =
                findViewById(
                        R.id.tabLayout
                );

        tvInningsHeader =
                findViewById(
                        R.id.tvInningsHeader
                );

        layoutBatting =
                findViewById(
                        R.id.layoutBatting
                );

        layoutBowling =
                findViewById(
                        R.id.layoutBowling
                );
    }

    // ============================================================
    // TOOLBAR
    // ============================================================

    private void setupToolbar() {

        if (toolbar != null) {

            toolbar.setNavigationOnClickListener(
                    v -> finish()
            );
        }
    }

    // ============================================================
    // LOAD SCORECARD
    // ============================================================

    private void loadScorecard() {

        match =
                db.getMatchById(
                        matchId
                );

        if (match == null) {

            finish();

            return;
        }

        setupInningsTabs();
    }

    // ============================================================
    // SETUP INNINGS TABS
    // ============================================================

    private void setupInningsTabs() {

        tabLayout.removeAllTabs();

        // All supported formats are limited-overs matches.
        // There are exactly two innings: one per team.
        int inningsCount = 2;

        for (
                int innings = 1;
                innings <= inningsCount;
                innings++
        ) {

            String teamName =
                    getInningsTeamName(
                            innings
                    );

            TabLayout.Tab tab =
                    tabLayout.newTab();

            tab.setText(
                    teamName
                            + " Innings"
            );

            tabLayout.addTab(
                    tab
            );
        }

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {

                    @Override
                    public void onTabSelected(
                            @NonNull TabLayout.Tab tab
                    ) {

                        displayInnings(
                                tab.getPosition()
                                        + 1
                        );
                    }

                    @Override
                    public void onTabUnselected(
                            @NonNull TabLayout.Tab tab
                    ) {
                        // Nothing required.
                    }

                    @Override
                    public void onTabReselected(
                            @NonNull TabLayout.Tab tab
                    ) {

                        displayInnings(
                                tab.getPosition()
                                        + 1
                        );
                    }
                }
        );

        if (tabLayout.getTabCount() > 0) {

            TabLayout.Tab firstTab =
                    tabLayout.getTabAt(0);

            if (firstTab != null) {

                firstTab.select();
            }
        }
    }

    // ============================================================
    // DISPLAY INNINGS
    // ============================================================

    private void displayInnings(
            int innings
    ) {

        if (match == null) {
            return;
        }

        if (innings < 1
                || innings > 2) {

            return;
        }

        int maximumInnings = 2;

        if (innings > maximumInnings) {
            return;
        }

        int runs =
                match.getInningsRuns(
                        innings
                );

        int wickets =
                match.getInningsWickets(
                        innings
                );

        int balls =
                match.getInningsBalls(
                        innings
                );

        String teamName =
                getInningsTeamName(
                        innings
                );

        // --------------------------------------------------------
        // HEADER
        // --------------------------------------------------------

        tvInningsHeader.setText(
                teamName
                        + " innings – "
                        + runs
                        + "/"
                        + wickets
                        + " ("
                        + formatOvers(balls)
                        + ")"
        );

        // --------------------------------------------------------
        // CLEAR OLD DATA
        // --------------------------------------------------------

        layoutBatting.removeAllViews();

        layoutBowling.removeAllViews();

        // --------------------------------------------------------
        // LOAD DATABASE STATISTICS
        // --------------------------------------------------------

        List<BattingStat> battingStats =
                db.getBattingStats(
                        matchId,
                        innings
                );

        List<BowlingStat> bowlingStats =
                db.getBowlingStats(
                        matchId,
                        innings
                );

        // --------------------------------------------------------
        // BATTING
        // --------------------------------------------------------

        buildBattingRows(
                battingStats
        );

        // --------------------------------------------------------
        // BOWLING
        // --------------------------------------------------------

        buildBowlingRows(
                bowlingStats
        );
    }

    // ============================================================
    // BATTING ROWS
    // ============================================================

    private void buildBattingRows(
            List<BattingStat> stats
    ) {

        if (stats == null
                || stats.isEmpty()) {

            addMessageRow(
                    layoutBatting,
                    "No batting data"
            );

            return;
        }

        List<BattingStat> actualBatting = new ArrayList<>();
        for (BattingStat stat : stats) {
            if (stat == null) continue;
            if (stat.getBalls() > 0 || stat.getRuns() > 0 || stat.isOut() ||
                    (stat.getHowOut() != null && !stat.getHowOut().trim().isEmpty())) {
                actualBatting.add(stat);
            }
        }

        if (actualBatting.isEmpty()) {
            addMessageRow(
                    layoutBatting,
                    "No batting data"
            );
            return;
        }

        for (BattingStat stat : actualBatting) {

            addBattingRow(
                    stat
            );
        }
    }

    // ============================================================
    // SINGLE BATTING ROW
    // ============================================================

    private String getDetailedDismissalText(BattingStat stat) {
        if (!stat.isOut()) {
            if ("Retired Hurt".equalsIgnoreCase(stat.getHowOut())) {
                return "retired hurt";
            }
            return "not out";
        }
        String how = stat.getHowOut() != null ? stat.getHowOut().trim() : "out";
        String bowler = stat.getBowlerName();
        if (bowler == null || bowler.trim().isEmpty()) {
            bowler = getPlayerName(stat.getBowlerId(), "");
        }
        String fielder = stat.getFielderName();
        if (fielder == null || fielder.trim().isEmpty()) {
            fielder = getPlayerName(stat.getFielderId(), "");
        }

        switch (how.toLowerCase()) {
            case "bowled":
                return "b " + (bowler.isEmpty() ? "Bowler" : bowler);
            case "lbw":
                return "lbw b " + (bowler.isEmpty() ? "Bowler" : bowler);
            case "caught":
                if (!fielder.isEmpty() && !bowler.isEmpty()) {
                    return "c " + fielder + " b " + bowler;
                } else if (!fielder.isEmpty()) {
                    return "c " + fielder;
                } else if (!bowler.isEmpty()) {
                    return "c & b " + bowler;
                }
                return "caught";
            case "stumped":
                return "st " + (fielder.isEmpty() ? "Wicketkeeper" : fielder) + " b " + (bowler.isEmpty() ? "Bowler" : bowler);
            case "run out":
                return "run out (" + (fielder.isEmpty() ? "Fielder" : fielder) + ")";
            case "hit wicket":
                return "hit wicket b " + (bowler.isEmpty() ? "Bowler" : bowler);
            default:
                return how;
        }
    }

    private void addBattingRow(BattingStat stat) {
        if (stat == null) return;

        android.util.Log.d("CRIC_DEBUG", "UI is drawing row for player: " + stat.getPlayerName() + " | Runs: " + stat.getRuns());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setPadding(8, 8, 8, 8);

        // 1. Player Name & Dismissal
        LinearLayout playerLayout = new LinearLayout(this);
        playerLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));
        playerLayout.setOrientation(LinearLayout.VERTICAL);

        String playerName = getPlayerName(stat.getPlayerId(), stat.getPlayerName());
        if (!stat.isOut()) {
            if ("Retired Hurt".equalsIgnoreCase(stat.getHowOut())) {
                playerName += " (retired hurt)";
            } else {
                playerName += " *";
            }
        }

        TextView tvPlayerName = new TextView(this);
        tvPlayerName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvPlayerName.setText(playerName);
        tvPlayerName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tvPlayerName.setTextSize(13);
        tvPlayerName.setTextColor(getResources().getColor(R.color.text_primary));
        playerLayout.addView(tvPlayerName);

        if (stat.isOut() || "Retired Hurt".equalsIgnoreCase(stat.getHowOut())) {
            TextView tvDismissal = new TextView(this);
            tvDismissal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvDismissal.setText(getDetailedDismissalText(stat));
            tvDismissal.setTextSize(11);
            tvDismissal.setTextColor(getResources().getColor(R.color.text_secondary));
            playerLayout.addView(tvDismissal);
        }
        row.addView(playerLayout);

        // 2. Runs
        TextView tvRuns = new TextView(this);
        LinearLayout.LayoutParams paramsRuns = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        tvRuns.setLayoutParams(paramsRuns);
        tvRuns.setGravity(Gravity.CENTER);
        tvRuns.setText(String.valueOf(stat.getRuns()));
        row.addView(tvRuns);

        // 3. Balls
        TextView tvBalls = new TextView(this);
        tvBalls.setLayoutParams(paramsRuns);
        tvBalls.setGravity(Gravity.CENTER);
        tvBalls.setText(String.valueOf(stat.getBalls()));
        row.addView(tvBalls);

        // 4. Strike Rate
        TextView tvSR = new TextView(this);
        tvSR.setLayoutParams(paramsRuns);
        tvSR.setGravity(Gravity.CENTER);
        tvSR.setText(String.format(Locale.US, "%.1f", stat.getStrikeRate()));
        row.addView(tvSR);

        if (layoutBatting != null) {
            layoutBatting.setVisibility(View.VISIBLE);
            layoutBatting.addView(row);
        }
    }

    // ============================================================
    // BOWLING ROWS
    // ============================================================

    private void buildBowlingRows(
            List<BowlingStat> stats
    ) {

        if (stats == null
                || stats.isEmpty()) {

            addMessageRow(
                    layoutBowling,
                    "No bowling data"
            );

            return;
        }

        List<BowlingStat> actualBowling = new ArrayList<>();
        for (BowlingStat stat : stats) {
            if (stat == null) continue;
            if (stat.getBalls() > 0 || stat.getWickets() > 0 || stat.getRunsConceded() > 0) {
                actualBowling.add(stat);
            }
        }

        if (actualBowling.isEmpty()) {
            addMessageRow(
                    layoutBowling,
                    "No bowling data"
            );
            return;
        }

        for (BowlingStat stat : actualBowling) {

            addBowlingRow(
                    stat
            );
        }
    }

    // ============================================================
    // SINGLE BOWLING ROW
    // ============================================================

    private void addBowlingRow(
            BowlingStat stat
    ) {

        LinearLayout row =
                createRow();

        // --------------------------------------------------------
        // PLAYER
        // --------------------------------------------------------

        String playerName =
                getPlayerName(
                        stat.getPlayerId(),
                        stat.getPlayerName()
                );

        TextView player =
                createCell(
                        playerName,
                        2.0f,
                        Gravity.START
                );

        row.addView(player);

        // --------------------------------------------------------
        // OVERS
        // --------------------------------------------------------

        TextView overs =
                createCell(
                        stat.getOversFormatted(),
                        1.0f,
                        Gravity.CENTER
                );

        row.addView(overs);

        // --------------------------------------------------------
        // MAIDENS
        // --------------------------------------------------------

        TextView maidens =
                createCell(
                        String.valueOf(
                                stat.getMaidens()
                        ),
                        1.0f,
                        Gravity.CENTER
                );

        row.addView(maidens);

        // --------------------------------------------------------
        // RUNS CONCEDED
        // --------------------------------------------------------

        TextView runs =
                createCell(
                        String.valueOf(
                                stat.getRunsConceded()
                        ),
                        1.0f,
                        Gravity.CENTER
                );

        row.addView(runs);

        // --------------------------------------------------------
        // WICKETS
        // --------------------------------------------------------

        TextView wickets =
                createCell(
                        String.valueOf(
                                stat.getWickets()
                        ),
                        1.0f,
                        Gravity.CENTER
                );

        row.addView(wickets);

        layoutBowling.addView(
                row
        );
    }

    // ============================================================
    // CREATE ROW
    // ============================================================

    private LinearLayout createRow() {

        LinearLayout row =
                new LinearLayout(this);

        row.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                dp(8),
                0,
                dp(8)
        );

        return row;
    }

    // ============================================================
    // CREATE CELL
    // ============================================================

    private TextView createCell(
            String text,
            float weight,
            int gravity
    ) {

        TextView view =
                new TextView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        weight
                );

        view.setLayoutParams(
                params
        );

        view.setText(
                text == null
                        ? ""
                        : text
        );

        view.setGravity(
                gravity
        );

        view.setTextSize(
                13
        );

        view.setTextColor(
                getResources().getColor(
                        R.color.text_primary
                )
        );

        return view;
    }

    // ============================================================
    // MESSAGE ROW
    // ============================================================

    private void addMessageRow(
            LinearLayout parent,
            String message
    ) {

        TextView view =
                new TextView(this);

        view.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        view.setText(
                message
        );

        view.setTextSize(
                13
        );

        view.setGravity(
                Gravity.CENTER
        );

        view.setPadding(
                0,
                dp(12),
                0,
                dp(12)
        );

        view.setTextColor(
                getResources().getColor(
                        R.color.text_secondary
                )
        );

        parent.addView(
                view
        );
    }

    // ============================================================
    // GET INNINGS TEAM NAME
    // ============================================================

    private String getInningsTeamName(
            int innings
    ) {

        long teamId =
                match.getInningsTeamId(
                        innings
                );

        String fallback;

        if (teamId == match.getTeamAId()) {

            fallback =
                    match.getTeamAName();

        } else if (
                teamId == match.getTeamBId()
        ) {

            fallback =
                    match.getTeamBName();

        } else {

            fallback =
                    innings % 2 == 1
                            ? match.getTeamAName()
                            : match.getTeamBName();
        }

        return getTeamName(
                teamId,
                fallback
        );
    }

    // ============================================================
    // GET TEAM NAME
    // ============================================================

    private String getTeamName(
            long teamId,
            String fallback
    ) {

        if (teamId <= 0) {

            return fallback == null
                    ? ""
                    : fallback;
        }

        Team team =
                db.getTeamById(
                        teamId
                );

        if (team != null) {

            String name =
                    team.getName();

            if (name != null
                    && !name.trim().isEmpty()) {

                return name;
            }
        }

        return fallback == null
                ? ""
                : fallback;
    }

    // ============================================================
    // GET PLAYER NAME
    // ============================================================

    private String getPlayerName(
            long playerId,
            String fallback
    ) {

        if (playerId > 0) {

            Player player =
                    db.getPlayerById(
                            playerId
                    );

            if (player != null) {

                String name =
                        player.getName();

                if (name != null
                        && !name.trim().isEmpty()) {

                    return name;
                }
            }
        }

        if (fallback != null
                && !fallback.trim().isEmpty()) {

            return fallback;
        }

        return "Unknown";
    }

    // ============================================================
    // FORMAT OVERS
    // ============================================================

    private String formatOvers(
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
    // DP TO PIXELS
    // ============================================================

    private int dp(
            int value
    ) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}