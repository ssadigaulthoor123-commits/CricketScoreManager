package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cricketscorer.R;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.BattingStat;
import com.example.cricketscorer.models.BowlingStat;
import com.example.cricketscorer.models.MatchInfo;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.utils.ScoreUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Live cricket scoring screen.
 *
 * Supports:
 *
 * T20
 * ODI
 * Custom limited overs
 * Test Match - 4 innings
 *
 * Features:
 * Runs
 * Wickets
 * Wides
 * No-balls
 * Byes
 * Leg-byes
 * Strike rotation
 * Over management
 * Bowler selection
 * Bowler over limits
 * No consecutive overs by same bowler
 * Bowling-style eligibility
 * Wicketkeeper bowling restriction
 * Undo
 * Match completion
 */
public class LiveScoringActivity extends AppCompatActivity {

    // ============================================================
    // DATABASE
    // ============================================================

    private DatabaseHelper db;
    private MatchInfo match;

    // ============================================================
    // PLAYING XI
    // ============================================================

    private final List<Long> battingXI =
            new ArrayList<>();

    private final List<Long> bowlingXI =
            new ArrayList<>();

    /*
     * Original Team A XI.
     */
    private final List<Long> battingXIBackup =
            new ArrayList<>();

    /*
     * Original Team B XI.
     */
    private final List<Long> bowlingXIBackup =
            new ArrayList<>();

    /*
     * Players dismissed in the current innings.
     */
    private final List<Long> outPlayers =
            new ArrayList<>();

    // ============================================================
    // STATISTICS
    // ============================================================

    /*
     * IMPORTANT:
     *
     * Key = innings + ":" + playerId
     *
     * This prevents Test innings 1 and innings 3
     * from overwriting each other.
     */
    private final Map<String, BattingStat> battingStats =
            new HashMap<>();

    private final Map<String, BowlingStat> bowlingStats =
            new HashMap<>();

    private final Map<Long, Player> playerCache =
            new HashMap<>();

    // ============================================================
    // CURRENT PLAYERS
    // ============================================================

    private long strikerId = -1;
    private long nonStrikerId = -1;
    private long bowlerId = -1;

    /*
     * Bowler who completed the previous over.
     *
     * Example:
     *
     * Over 1 = A
     * Over 2 = B
     * Over 3 = A
     *
     * A is allowed in over 3.
     */
    private long previousBowlerId = -1;

    // ============================================================
    // CURRENT OVER
    // ============================================================

    private int legalBallsThisOver = 0;

    private final List<String> thisOverChips =
            new ArrayList<>();

    // ============================================================
    // UNDO
    // ============================================================

    private final Deque<Snapshot> undoStack =
            new ArrayDeque<>();

    // ============================================================
    // VIEWS
    // ============================================================

    private TextView tvBattingTeam;
    private TextView tvScore;
    private TextView tvOvers;
    private TextView tvCRR;
    private TextView tvTarget;

    private TextView tvStrikerName;
    private TextView tvStrikerRuns;
    private TextView tvStrikerBalls;
    private TextView tvStrikerSR;

    private TextView tvNonStrikerName;
    private TextView tvNonStrikerRuns;
    private TextView tvNonStrikerBalls;
    private TextView tvNonStrikerSR;

    private TextView tvBowlerName;
    private TextView tvBowlerOvers;
    private TextView tvBowlerRuns;
    private TextView tvBowlerWickets;

    private LinearLayout layoutThisOver;

    // ============================================================
    // CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_live_scoring
        );

        db =
                DatabaseHelper.getInstance(this);

        setupToolbar();
        bindViews();
        setupButtons();

        loadMatch();

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                confirmExit();
                            }
                        }
                );
    }

    // ============================================================
    // TOOLBAR
    // ============================================================

    private void setupToolbar() {

        Toolbar toolbar =
                findViewById(R.id.toolbar);

        if (toolbar != null) {

            toolbar.setNavigationOnClickListener(
                    v -> confirmExit()
            );
        }
    }

    // ============================================================
    // VIEW BINDING
    // ============================================================

    private void bindViews() {

        tvBattingTeam =
                findViewById(R.id.tvBattingTeam);

        tvScore =
                findViewById(R.id.tvScore);

        tvOvers =
                findViewById(R.id.tvOvers);

        tvCRR =
                findViewById(R.id.tvCRR);

        tvTarget =
                findViewById(R.id.tvTarget);

        tvStrikerName =
                findViewById(R.id.tvStrikerName);

        tvStrikerRuns =
                findViewById(R.id.tvStrikerRuns);

        tvStrikerBalls =
                findViewById(R.id.tvStrikerBalls);

        tvStrikerSR =
                findViewById(R.id.tvStrikerSR);

        tvNonStrikerName =
                findViewById(R.id.tvNonStrikerName);

        tvNonStrikerRuns =
                findViewById(R.id.tvNonStrikerRuns);

        tvNonStrikerBalls =
                findViewById(R.id.tvNonStrikerBalls);

        tvNonStrikerSR =
                findViewById(R.id.tvNonStrikerSR);

        tvBowlerName =
                findViewById(R.id.tvBowlerName);

        tvBowlerOvers =
                findViewById(R.id.tvBowlerOvers);

        tvBowlerRuns =
                findViewById(R.id.tvBowlerRuns);

        tvBowlerWickets =
                findViewById(R.id.tvBowlerWickets);

        layoutThisOver =
                findViewById(R.id.layoutThisOver);
    }

    // ============================================================
    // LOAD MATCH
    // ============================================================

    private void loadMatch() {

        long matchId =
                getIntent().getLongExtra(
                        "match_id",
                        -1
                );

        if (matchId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid match",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        match =
                db.getMatchById(matchId);

        if (match == null) {

            Toast.makeText(
                    this,
                    "Match not found",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        loadPlayingXIFromIntent();

        if (battingXIBackup.isEmpty()
                || bowlingXIBackup.isEmpty()) {

            Toast.makeText(
                    this,
                    "Playing XI information is missing",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        /*
         * Match may already have a current innings.
         */
        restoreCurrentInnings();
    }

    // ============================================================
    // LOAD XI
    // ============================================================

    private void loadPlayingXIFromIntent() {

        long[] battingIds =
                getIntent().getLongArrayExtra(
                        "batting_xi"
                );

        long[] bowlingIds =
                getIntent().getLongArrayExtra(
                        "bowling_xi"
                );

        if (battingIds != null) {

            for (long id : battingIds) {
                battingXIBackup.add(id);
            }
        }

        if (bowlingIds != null) {

            for (long id : bowlingIds) {
                bowlingXIBackup.add(id);
            }
        }

        cachePlayers(battingXIBackup);
        cachePlayers(bowlingXIBackup);
    }

    // ============================================================
    // CACHE PLAYERS
    // ============================================================

    private void cachePlayers(
            List<Long> playerIds
    ) {

        for (Long playerId : playerIds) {

            if (playerId == null) {
                continue;
            }

            if (playerCache.containsKey(playerId)) {
                continue;
            }

            Player player =
                    db.getPlayerById(playerId);

            if (player != null) {
                playerCache.put(
                        playerId,
                        player
                );
            }
        }
    }

    // ============================================================
    // BUTTONS
    // ============================================================

    private void setupButtons() {

        findViewById(R.id.btn0)
                .setOnClickListener(
                        v -> bowlBall(
                                0,
                                true,
                                "0"
                        )
                );

        findViewById(R.id.btn1)
                .setOnClickListener(
                        v -> bowlBall(
                                1,
                                true,
                                "1"
                        )
                );

        findViewById(R.id.btn2)
                .setOnClickListener(
                        v -> bowlBall(
                                2,
                                true,
                                "2"
                        )
                );

        findViewById(R.id.btn3)
                .setOnClickListener(
                        v -> bowlBall(
                                3,
                                true,
                                "3"
                        )
                );

        findViewById(R.id.btn4)
                .setOnClickListener(
                        v -> bowlBall(
                                4,
                                true,
                                "4"
                        )
                );

        findViewById(R.id.btn6)
                .setOnClickListener(
                        v -> bowlBall(
                                6,
                                true,
                                "6"
                        )
                );

        findViewById(R.id.btnWicket)
                .setOnClickListener(
                        v -> handleWicket()
                );

        findViewById(R.id.btnUndo)
                .setOnClickListener(
                        v -> handleUndo()
                );

        findViewById(R.id.btnWide)
                .setOnClickListener(
                        v -> extraBall(
                                "Wide",
                                1
                        )
                );

        findViewById(R.id.btnNoBall)
                .setOnClickListener(
                        v -> extraBall(
                                "No Ball",
                                1
                        )
                );

        findViewById(R.id.btnBye)
                .setOnClickListener(
                        v -> bowlBall(
                                1,
                                true,
                                "Bye"
                        )
                );

        findViewById(R.id.btnLegBye)
                .setOnClickListener(
                        v -> bowlBall(
                                1,
                                true,
                                "Leg Bye"
                        )
                );

        findViewById(R.id.btnEndOver)
                .setOnClickListener(
                        v -> manuallyEndOver()
                );
    }

    // ============================================================
    // END OVER
    // ============================================================

    private void manuallyEndOver() {

        if (legalBallsThisOver != 6) {

            Toast.makeText(
                    this,
                    "Over isn't finished yet",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        completeOver();
    }

    private void completeOver() {

        if (bowlerId == -1) {
            return;
        }

        /*
         * Current bowler has now completed the over.
         */
        previousBowlerId =
                bowlerId;

        legalBallsThisOver = 0;

        clearOverChips();

        swapStrike();

        selectBowler();
    }

    // ============================================================
    // NORMAL BALL
    // ============================================================

    private void bowlBall(
            int runs,
            boolean legalDelivery,
            String label
    ) {

        if (isMatchFinished()) {
            return;
        }

        if (strikerId == -1
                || nonStrikerId == -1
                || bowlerId == -1) {

            Toast.makeText(
                    this,
                    "Select batsmen and bowler first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * In limited overs, don't allow balls
         * after the innings limit.
         */
        if (legalDelivery
                && isLimitedOversMatch()
                && match.getTotalOvers() > 0
                && ballsBowledInInnings()
                >= match.getTotalOvers() * 6) {

            return;
        }

        pushSnapshot();

        BattingStat battingStat =
                getOrCreateBattingStat(
                        strikerId
                );

        BowlingStat bowlingStat =
                getOrCreateBowlingStat(
                        bowlerId
                );

        boolean isBye =
                "Bye".equals(label);

        boolean isLegBye =
                "Leg Bye".equals(label);

        /*
         * Team score.
         */
        addRunsToTeam(runs);

        /*
         * Legal delivery.
         */
        if (legalDelivery) {

            addLegalBall();

            legalBallsThisOver++;

            bowlingStat.setBalls(
                    bowlingStat.getBalls() + 1
            );
        }

        /*
         * A legal delivery counts as a ball faced.
         */
        if (legalDelivery) {

            battingStat.setBalls(
                    battingStat.getBalls() + 1
            );
        }

        /*
         * Normal batting runs.
         *
         * Byes and leg-byes do not belong
         * to the batter or bowler.
         */
        if (!isBye && !isLegBye) {

            battingStat.setRuns(
                    battingStat.getRuns() + runs
            );

            if (runs == 4) {

                battingStat.setFours(
                        battingStat.getFours() + 1
                );
            }

            if (runs == 6) {

                battingStat.setSixes(
                        battingStat.getSixes() + 1
                );
            }

            bowlingStat.setRunsConceded(
                    bowlingStat.getRunsConceded()
                            + runs
            );
        }

        /*
         * Save.
         */
        db.saveBattingStat(
                battingStat
        );

        db.saveBowlingStat(
                bowlingStat
        );

        /*
         * Over display.
         */
        if (isBye) {

            addChip(
                    runs + "B"
            );

        } else if (isLegBye) {

            addChip(
                    runs + "LB"
            );

        } else {

            addChip(
                    String.valueOf(runs)
            );
        }

        /*
         * Odd runs rotate strike.
         */
        if (runs % 2 == 1) {
            swapStrike();
        }

        afterBallUpdate();
    }

    // ============================================================
    // WIDE / NO BALL
    // ============================================================

    private void extraBall(
            String type,
            int extraRuns
    ) {

        if (isMatchFinished()) {
            return;
        }

        if (bowlerId == -1) {

            Toast.makeText(
                    this,
                    "Select a bowler first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        pushSnapshot();

        BowlingStat bowlingStat =
                getOrCreateBowlingStat(
                        bowlerId
                );

        /*
         * Wide/no-ball extra goes to team
         * and bowler.
         */
        addRunsToTeam(extraRuns);

        bowlingStat.setRunsConceded(
                bowlingStat.getRunsConceded()
                        + extraRuns
        );

        db.saveBowlingStat(
                bowlingStat
        );

        if ("Wide".equals(type)) {

            addChip("WD");

        } else {

            addChip("NB");
        }

        /*
         * Wide and no-ball are not legal deliveries.
         */
        afterBallUpdate();
    }

    // ============================================================
    // WICKET
    // ============================================================

    private void handleWicket() {

        if (isMatchFinished()) {
            return;
        }

        if (strikerId == -1
                || bowlerId == -1) {
            return;
        }

        String[] dismissals = {
                "Bowled",
                "Caught",
                "LBW",
                "Run Out",
                "Stumped",
                "Hit Wicket"
        };

        new AlertDialog.Builder(this)
                .setTitle(
                        "How was the batter out?"
                )
                .setItems(
                        dismissals,
                        (dialog, which) ->
                                processWicket(
                                        dismissals[which]
                                )
                )
                .setNegativeButton(
                        R.string.cancel,
                        null
                )
                .show();
    }

    // ============================================================
    // PROCESS WICKET
    // ============================================================

    private void processWicket(
            String dismissalType
    ) {

        if (strikerId == -1
                || bowlerId == -1) {
            return;
        }

        if (outPlayers.contains(strikerId)) {
            return;
        }

        pushSnapshot();

        BattingStat battingStat =
                getOrCreateBattingStat(
                        strikerId
                );

        BowlingStat bowlingStat =
                getOrCreateBowlingStat(
                        bowlerId
                );

        /*
         * Wicket is a legal delivery.
         */
        addLegalBall();

        legalBallsThisOver++;

        battingStat.setBalls(
                battingStat.getBalls() + 1
        );

        battingStat.setOut(true);

        battingStat.setHowOut(
                dismissalType
        );

        incrementWicket();

        bowlingStat.setBalls(
                bowlingStat.getBalls() + 1
        );

        /*
         * Run out is not credited to bowler.
         */
        if (!"Run Out".equals(dismissalType)) {

            bowlingStat.setWickets(
                    bowlingStat.getWickets() + 1
            );
        }

        db.saveBattingStat(
                battingStat
        );

        db.saveBowlingStat(
                bowlingStat
        );

        outPlayers.add(
                strikerId
        );

        addChip("W");

        /*
         * Dismissed batter cannot continue.
         */
        strikerId = -1;

        boolean allOut =
                wicketsInInnings() >= MatchInfo.MAX_WICKETS_PER_INNINGS
                        || outPlayers.size() >= Math.max(0, battingXI.size() - 1);

        if (!allOut) {

            selectNewBatsman();
        }

        afterBallUpdate();
    }

    // ============================================================
    // TEAM RUNS
    // ============================================================

    private void addRunsToTeam(
            int runs
    ) {

        switch (match.getCurrentInnings()) {

            case 1:

                match.setInn1Runs(
                        match.getInn1Runs() + runs
                );

                break;

            case 2:

                match.setInn2Runs(
                        match.getInn2Runs() + runs
                );

                break;

            case 3:

                match.setInn3Runs(
                        match.getInn3Runs() + runs
                );

                break;

            case 4:

                match.setInn4Runs(
                        match.getInn4Runs() + runs
                );

                break;

            default:
                break;
        }
    }

    // ============================================================
    // LEGAL BALL
    // ============================================================

    private void addLegalBall() {

        switch (match.getCurrentInnings()) {

            case 1:

                match.setInn1Balls(
                        match.getInn1Balls() + 1
                );

                break;

            case 2:

                match.setInn2Balls(
                        match.getInn2Balls() + 1
                );

                break;

            case 3:

                match.setInn3Balls(
                        match.getInn3Balls() + 1
                );

                break;

            case 4:

                match.setInn4Balls(
                        match.getInn4Balls() + 1
                );

                break;

            default:
                break;
        }
    }

    // ============================================================
    // WICKET COUNT
    // ============================================================

    private void incrementWicket() {

        switch (match.getCurrentInnings()) {

            case 1:

                match.setInn1Wickets(
                        match.getInn1Wickets() + 1
                );

                break;

            case 2:

                match.setInn2Wickets(
                        match.getInn2Wickets() + 1
                );

                break;

            case 3:

                match.setInn3Wickets(
                        match.getInn3Wickets() + 1
                );

                break;

            case 4:

                match.setInn4Wickets(
                        match.getInn4Wickets() + 1
                );

                break;

            default:
                break;
        }
    }

    // ============================================================
    // CURRENT BALLS
    // ============================================================

    private int ballsBowledInInnings() {

        switch (match.getCurrentInnings()) {

            case 1:
                return match.getInn1Balls();

            case 2:
                return match.getInn2Balls();

            case 3:
                return match.getInn3Balls();

            case 4:
                return match.getInn4Balls();

            default:
                return 0;
        }
    }

    // ============================================================
    // CURRENT RUNS
    // ============================================================

    private int runsInInnings() {

        switch (match.getCurrentInnings()) {

            case 1:
                return match.getInn1Runs();

            case 2:
                return match.getInn2Runs();

            case 3:
                return match.getInn3Runs();

            case 4:
                return match.getInn4Runs();

            default:
                return 0;
        }
    }

    // ============================================================
    // CURRENT WICKETS
    // ============================================================

    private int wicketsInInnings() {

        switch (match.getCurrentInnings()) {

            case 1:
                return match.getInn1Wickets();

            case 2:
                return match.getInn2Wickets();

            case 3:
                return match.getInn3Wickets();

            case 4:
                return match.getInn4Wickets();

            default:
                return 0;
        }
    }

    // ============================================================
    // INNINGS RUNS
    // ============================================================

    private int runsForInnings(
            int innings
    ) {

        switch (innings) {

            case 1:
                return match.getInn1Runs();

            case 2:
                return match.getInn2Runs();

            case 3:
                return match.getInn3Runs();

            case 4:
                return match.getInn4Runs();

            default:
                return 0;
        }
    }

    // ============================================================
    // INNINGS WICKETS
    // ============================================================

    private int wicketsForInnings(
            int innings
    ) {

        switch (innings) {

            case 1:
                return match.getInn1Wickets();

            case 2:
                return match.getInn2Wickets();

            case 3:
                return match.getInn3Wickets();

            case 4:
                return match.getInn4Wickets();

            default:
                return 0;
        }
    }

    // ============================================================
    // INNINGS BALLS
    // ============================================================

    private int ballsForInnings(
            int innings
    ) {

        switch (innings) {

            case 1:
                return match.getInn1Balls();

            case 2:
                return match.getInn2Balls();

            case 3:
                return match.getInn3Balls();

            case 4:
                return match.getInn4Balls();

            default:
                return 0;
        }
    }

    // ============================================================
    // AFTER BALL UPDATE
    // ============================================================

    private void afterBallUpdate() {

        if (match == null) {
            return;
        }

        db.updateMatchLiveState(
                match
        );

        refreshScoreboard();

        /*
         * Ten wickets = innings complete.
         */
        boolean allOut =
                wicketsInInnings() >= MatchInfo.MAX_WICKETS_PER_INNINGS
                        || outPlayers.size() >= Math.max(0, battingXI.size() - 1);

        /*
         * Limited overs chase.
         */
        boolean limitedOversChased =
                isLimitedOversMatch()
                        && match.getCurrentInnings() == 2
                        && match.getInn2Runs()
                        >= match.getInn1Runs() + 1;

        /*
         * Test fourth-innings chase.
         */
        boolean testChased =
                isTestMatch()
                        && match.getCurrentInnings() == 4
                        && testTeamBRuns()
                        >= testTarget();

        /*
         * Limited-overs over limit.
         *
         * Test totalOvers = 0,
         * so this remains false.
         */
        boolean oversComplete =
                isLimitedOversMatch()
                        && match.getTotalOvers() > 0
                        && ballsBowledInInnings()
                        >= match.getTotalOvers() * 6;

        if (allOut
                || limitedOversChased
                || testChased
                || oversComplete) {

            endInnings();
            return;
        }

        /*
         * Automatic over completion.
         */
        if (legalBallsThisOver >= 6) {

            completeOver();
        }
    }

    // ============================================================
    // TEST TEAM A TOTAL
    // ============================================================

    private int testTeamARuns() {

        return match.getInn1Runs()
                + match.getInn3Runs();
    }

    // ============================================================
    // TEST TEAM B TOTAL
    // ============================================================

    private int testTeamBRuns() {

        return match.getInn2Runs()
                + match.getInn4Runs();
    }

    // ============================================================
    // TEST TARGET
    // ============================================================

    private int testTarget() {

        /*
         * Team A:
         *
         * Innings 1 + Innings 3
         *
         * Team B already scored:
         *
         * Innings 2
         *
         * Therefore Team B needs:
         *
         * A total - B first innings + 1
         */
        int target =
                testTeamARuns()
                        - match.getInn2Runs()
                        + 1;

        return Math.max(
                target,
                1
        );
    }

    // ============================================================
    // MATCH FORMAT
    // ============================================================

    private boolean isTestMatch() {

        if (match == null
                || match.getMatchFormat() == null) {

            return false;
        }

        String format =
                match.getMatchFormat()
                        .trim()
                        .toUpperCase();

        return "TEST".equals(format)
                || "TEST MATCH".equals(format);
    }

    private boolean isLimitedOversMatch() {

        return !isTestMatch();
    }

    // ============================================================
    // MATCH FINISHED
    // ============================================================

    private boolean isMatchFinished() {

        return match != null
                && match.isCompleted();
    }

    // ============================================================
    // CURRENT BATTING TEAM
    // ============================================================

    private String currentBattingTeamName() {

        if (match.getBattingTeamId()
                == match.getTeamAId()) {

            return safeName(
                    match.getTeamAName()
            );
        }

        return safeName(
                match.getTeamBName()
        );
    }

    // ============================================================
    // REFRESH SCOREBOARD
    // ============================================================

    private void refreshScoreboard() {

        if (match == null) {
            return;
        }

        int runs =
                runsInInnings();

        int wickets =
                wicketsInInnings();

        int balls =
                ballsBowledInInnings();

        tvBattingTeam.setText(
                currentBattingTeamName()
                        + " innings"
        );

        tvScore.setText(
                runs
                        + "/"
                        + wickets
        );

        String currentOvers =
                ScoreUtils.formatOvers(
                        balls
                );

        if (isLimitedOversMatch()
                && match.getTotalOvers() > 0) {

            tvOvers.setText(
                    "("
                            + currentOvers
                            + " / "
                            + match.getTotalOvers()
                            + " ov)"
            );

        } else {

            tvOvers.setText(
                    "("
                            + currentOvers
                            + " ov)"
            );
        }

        double crr =
                0.0;

        if (balls > 0) {

            crr =
                    runs * 6.0 / balls;
        }

        tvCRR.setText(
                String.format(
                        Locale.US,
                        "CRR: %.2f",
                        crr
                )
        );

        updateTarget();

        updateStrikerView();
        updateNonStrikerView();
        updateBowlerView();
    }

    // ============================================================
    // TARGET
    // ============================================================

    private void updateTarget() {

        if (tvTarget == null) {
            return;
        }

        /*
         * LIMITED OVERS
         */
        if (isLimitedOversMatch()
                && match.getCurrentInnings() == 2) {

            int target =
                    match.getInn1Runs() + 1;

            int need =
                    target - match.getInn2Runs();

            if (need <= 0) {

                tvTarget.setText(
                        "Target reached"
                );

                return;
            }

            if (match.getTotalOvers() > 0) {

                int ballsLeft =
                        match.getTotalOvers() * 6
                                - match.getInn2Balls();

                tvTarget.setText(
                        "Need "
                                + need
                                + " off "
                                + Math.max(
                                ballsLeft,
                                0
                        )
                                + " balls"
                );

            } else {

                tvTarget.setText(
                        "Need "
                                + need
                                + " runs"
                );
            }

            return;
        }

        /*
         * TEST
         */
        if (isTestMatch()) {

            int teamARuns =
                    testTeamARuns();

            int teamBRuns =
                    testTeamBRuns();

            if (match.getCurrentInnings() == 3) {

                int lead =
                        teamARuns
                                - match.getInn2Runs();

                if (lead >= 0) {

                    tvTarget.setText(
                            "Team A lead: "
                                    + lead
                                    + " runs"
                    );

                } else {

                    tvTarget.setText(
                            "Team B lead: "
                                    + Math.abs(lead)
                                    + " runs"
                    );
                }

            } else if (
                    match.getCurrentInnings() == 4
            ) {

                int need =
                        testTarget()
                                - match.getInn4Runs();

                if (need > 0) {

                    tvTarget.setText(
                            "Team B needs "
                                    + need
                                    + " runs"
                    );

                } else {

                    tvTarget.setText(
                            "Team B has taken the lead"
                    );
                }

            } else {

                tvTarget.setText("");
            }

            return;
        }

        tvTarget.setText("");
    }

    // ============================================================
    // STRIKER VIEW
    // ============================================================

    private void updateStrikerView() {

        BattingStat stat =
                getBattingStat(
                        strikerId
                );

        if (stat == null) {

            tvStrikerName.setText("");
            tvStrikerRuns.setText("0");
            tvStrikerBalls.setText("0");
            tvStrikerSR.setText("0.0");

            return;
        }

        Player player =
                playerCache.get(
                        strikerId
                );

        String name =
                player == null
                        ? "Unknown Player"
                        : safeName(player.getName());

        tvStrikerName.setText(
                "* " + name
        );

        tvStrikerRuns.setText(
                String.valueOf(
                        stat.getRuns()
                )
        );

        tvStrikerBalls.setText(
                String.valueOf(
                        stat.getBalls()
                )
        );

        tvStrikerSR.setText(
                String.format(
                        Locale.US,
                        "%.1f",
                        stat.getStrikeRate()
                )
        );
    }

    // ============================================================
    // NON-STRIKER VIEW
    // ============================================================

    private void updateNonStrikerView() {

        BattingStat stat =
                getBattingStat(
                        nonStrikerId
                );

        if (stat == null) {

            tvNonStrikerName.setText("");
            tvNonStrikerRuns.setText("0");
            tvNonStrikerBalls.setText("0");
            tvNonStrikerSR.setText("0.0");

            return;
        }

        Player player =
                playerCache.get(
                        nonStrikerId
                );

        String name =
                player == null
                        ? "Unknown Player"
                        : safeName(player.getName());

        tvNonStrikerName.setText(
                name
        );

        tvNonStrikerRuns.setText(
                String.valueOf(
                        stat.getRuns()
                )
        );

        tvNonStrikerBalls.setText(
                String.valueOf(
                        stat.getBalls()
                )
        );

        tvNonStrikerSR.setText(
                String.format(
                        Locale.US,
                        "%.1f",
                        stat.getStrikeRate()
                )
        );
    }

    // ============================================================
    // BOWLER VIEW
    // ============================================================

    private void updateBowlerView() {

        BowlingStat stat =
                getBowlingStat(
                        bowlerId
                );

        if (stat == null) {

            tvBowlerName.setText("");
            tvBowlerOvers.setText("0.0");
            tvBowlerRuns.setText("0");
            tvBowlerWickets.setText("0");

            return;
        }

        Player player =
                playerCache.get(
                        bowlerId
                );

        String name =
                player == null
                        ? "Unknown Player"
                        : safeName(player.getName());

        tvBowlerName.setText(
                name
        );

        tvBowlerOvers.setText(
                stat.getOversFormatted()
        );

        tvBowlerRuns.setText(
                String.valueOf(
                        stat.getRunsConceded()
                )
        );

        tvBowlerWickets.setText(
                String.valueOf(
                        stat.getWickets()
                )
        );
    }

    // ============================================================
    // END INNINGS
    // ============================================================

    private void endInnings() {

        if (match == null
                || match.isCompleted()) {

            return;
        }

        db.updateMatchLiveState(
                match
        );

        int current =
                match.getCurrentInnings();

        if (isLimitedOversMatch()) {

            if (current == 1) {

                showLimitedOversInningsOneComplete();

            } else {

                finishLimitedOversMatch();
            }

            return;
        }

        switch (current) {

            case 1:
            case 2:
            case 3:

                showTestInningsComplete(
                        current
                );

                break;

            case 4:

                finishTestMatch();

                break;

            default:
                break;
        }
    }

    // ============================================================
    // LIMITED OVERS INNINGS 1 COMPLETE
    // ============================================================

    private void showLimitedOversInningsOneComplete() {

        String teamName =
                currentBattingTeamName();

        String message =
                teamName
                        + " scored "
                        + match.getInn1Runs()
                        + "/"
                        + match.getInn1Wickets()
                        + " in "
                        + ScoreUtils.formatOvers(
                        match.getInn1Balls()
                )
                        + " overs.";

        new AlertDialog.Builder(this)
                .setTitle(
                        "Innings 1 complete"
                )
                .setMessage(
                        message
                )
                .setCancelable(false)
                .setPositiveButton(
                        "Start Innings 2",
                        (dialog, which) ->
                                startNextInnings()
                )
                .show();
    }

    // ============================================================
    // TEST INNINGS COMPLETE
    // ============================================================

    private void showTestInningsComplete(
            int inningsNumber
    ) {

        String teamName =
                currentBattingTeamName();

        int runs =
                runsForInnings(
                        inningsNumber
                );

        int wickets =
                wicketsForInnings(
                        inningsNumber
                );

        int balls =
                ballsForInnings(
                        inningsNumber
                );

        String title =
                "Test Innings "
                        + inningsNumber
                        + " complete";

        String message =
                teamName
                        + " scored "
                        + runs
                        + "/"
                        + wickets
                        + " in "
                        + ScoreUtils.formatOvers(
                        balls
                )
                        + " overs.";

        String nextButton =
                "Start Innings "
                        + (inningsNumber + 1);

        new AlertDialog.Builder(this)
                .setTitle(
                        title
                )
                .setMessage(
                        message
                )
                .setCancelable(false)
                .setPositiveButton(
                        nextButton,
                        (dialog, which) ->
                                startNextInnings()
                )
                .show();
    }

    // ============================================================
    // START NEXT INNINGS
    // ============================================================

    private void startNextInnings() {

        int nextInnings =
                match.getCurrentInnings() + 1;

        if (nextInnings > 4) {

            if (isTestMatch()) {
                finishTestMatch();
            } else {
                finishLimitedOversMatch();
            }

            return;
        }

        /*
         * Save current state first.
         */
        db.updateMatchLiveState(
                match
        );

        match.setCurrentInnings(
                nextInnings
        );

        /*
         * Swap batting and bowling teams.
         *
         * 1 -> Team A bats
         * 2 -> Team B bats
         * 3 -> Team A bats
         * 4 -> Team B bats
         */
        long oldBattingTeam =
                match.getBattingTeamId();

        long oldBowlingTeam =
                match.getBowlingTeamId();

        match.setBattingTeamId(
                oldBowlingTeam
        );

        match.setBowlingTeamId(
                oldBattingTeam
        );

        /*
         * Switch XIs.
         */
        battingXI.clear();

        bowlingXI.clear();

        if (nextInnings == 1
                || nextInnings == 3) {

            battingXI.addAll(
                    battingXIBackup
            );

            bowlingXI.addAll(
                    bowlingXIBackup
            );

        } else {

            battingXI.addAll(
                    bowlingXIBackup
            );

            bowlingXI.addAll(
                    battingXIBackup
            );
        }

        cachePlayers(battingXI);
        cachePlayers(bowlingXI);

        /*
         * Reset current innings state.
         */
        outPlayers.clear();

        battingStats.clear();

        bowlingStats.clear();

        undoStack.clear();

        clearOverChips();

        legalBallsThisOver = 0;

        previousBowlerId = -1;

        strikerId = -1;

        nonStrikerId = -1;

        bowlerId = -1;

        db.updateMatchLiveState(
                match
        );

        selectOpeningBatsmen();
    }

    // ============================================================
    // RESTORE CURRENT INNINGS
    // ============================================================

    private void restoreCurrentInnings() {

        int current =
                match.getCurrentInnings();

        /*
         * Odd innings:
         * Team that originally batted first.
         *
         * Even innings:
         * Team that originally bowled first.
         */
        if (current == 1
                || current == 3) {

            battingXI.clear();

            bowlingXI.clear();

            battingXI.addAll(
                    battingXIBackup
            );

            bowlingXI.addAll(
                    bowlingXIBackup
            );

        } else {

            battingXI.clear();

            bowlingXI.clear();

            battingXI.addAll(
                    bowlingXIBackup
            );

            bowlingXI.addAll(
                    battingXIBackup
            );
        }

        cachePlayers(battingXI);
        cachePlayers(bowlingXI);

        /*
         * Current striker/non-striker/bowler are not stored in
         * MatchInfo, so those are selected again. The actual
         * innings statistics ARE stored in SQLite and must be
         * restored before the user continues scoring.
         */
        outPlayers.clear();

        battingStats.clear();

        bowlingStats.clear();

        List<BattingStat> savedBatting =
                db.getBattingStats(
                        match.getId(),
                        current
                );

        if (savedBatting != null) {
            for (BattingStat stat : savedBatting) {
                if (stat == null) {
                    continue;
                }

                battingStats.put(
                        statKey(stat.getPlayerId()),
                        stat
                );

                if (stat.isOut()) {
                    outPlayers.add(stat.getPlayerId());
                }
            }
        }

        List<BowlingStat> savedBowling =
                db.getBowlingStats(
                        match.getId(),
                        current
                );

        if (savedBowling != null) {
            for (BowlingStat stat : savedBowling) {
                if (stat == null) {
                    continue;
                }

                bowlingStats.put(
                        statKey(stat.getPlayerId()),
                        stat
                );
            }
        }

        undoStack.clear();

        clearOverChips();

        legalBallsThisOver =
                ballsBowledInInnings() % 6;

        previousBowlerId = -1;

        strikerId = -1;

        nonStrikerId = -1;

        bowlerId = -1;

        refreshScoreboard();

        if (wicketsInInnings() >= MatchInfo.MAX_WICKETS_PER_INNINGS) {
            endInnings();
            return;
        }

        selectOpeningBatsmen();
    }

    // ============================================================
    // OPENING BATSMEN
    // ============================================================

    private void selectOpeningBatsmen() {

        List<Long> available =
                new ArrayList<>(
                        battingXI
                );

        available.removeAll(
                outPlayers
        );

        if (available.size() < 2) {
            return;
        }

        String[] names =
                namesFor(available);

        new AlertDialog.Builder(this)
                .setTitle(
                        "Select opening striker"
                )
                .setCancelable(false)
                .setItems(
                        names,
                        (dialog, which) -> {

                            if (which < 0
                                    || which >= available.size()) {
                                return;
                            }

                            strikerId =
                                    available.get(which);

                            ensureBattingStat(
                                    strikerId
                            );

                            selectNonStriker();
                        }
                )
                .show();
    }

    // ============================================================
    // NON STRIKER
    // ============================================================

    private void selectNonStriker() {

        List<Long> remaining =
                new ArrayList<>(
                        battingXI
                );

        remaining.remove(
                strikerId
        );

        remaining.removeAll(
                outPlayers
        );

        if (remaining.isEmpty()) {
            return;
        }

        String[] names =
                namesFor(remaining);

        new AlertDialog.Builder(this)
                .setTitle(
                        "Select non-striker"
                )
                .setCancelable(false)
                .setItems(
                        names,
                        (dialog, which) -> {

                            if (which < 0
                                    || which >= remaining.size()) {
                                return;
                            }

                            nonStrikerId =
                                    remaining.get(which);

                            ensureBattingStat(
                                    nonStrikerId
                            );

                            selectBowler();
                        }
                )
                .show();
    }

    // ============================================================
    // NEW BATSMAN
    // ============================================================

    private void selectNewBatsman() {

        List<Long> available =
                new ArrayList<>(
                        battingXI
                );

        available.removeAll(
                outPlayers
        );

        available.remove(
                nonStrikerId
        );

        if (available.isEmpty()) {
            return;
        }

        String[] names =
                namesFor(available);

        new AlertDialog.Builder(this)
                .setTitle(
                        "Select next batter"
                )
                .setCancelable(false)
                .setItems(
                        names,
                        (dialog, which) -> {

                            if (which < 0
                                    || which >= available.size()) {
                                return;
                            }

                            strikerId =
                                    available.get(which);

                            ensureBattingStat(
                                    strikerId
                            );

                            refreshScoreboard();
                        }
                )
                .show();
    }

    // ============================================================
    // MAXIMUM OVERS PER BOWLER
    // ============================================================

    private int getMaximumOversPerBowler() {

        if (match == null
                || match.getMatchFormat() == null) {

            return 0;
        }

        String format =
                match.getMatchFormat()
                        .trim()
                        .toUpperCase();

        switch (format) {

            case "T20":
                return 4;

            case "ODI":
                return 10;

            case "TEST":
            case "TEST MATCH":
            case "CUSTOM":
            default:
                return 0;
        }
    }

    // ============================================================
    // BOWLER SELECTION
    // ============================================================

    private void selectBowler() {

        if (bowlingXI.isEmpty()) {

            Toast.makeText(
                    this,
                    "No bowling players available",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        List<Long> options =
                new ArrayList<>();

        int maximumOvers =
                getMaximumOversPerBowler();

        for (Long playerId : bowlingXI) {

            if (playerId == null) {
                continue;
            }

            /*
             * Same bowler cannot bowl
             * consecutive overs.
             */
            if (playerId == previousBowlerId) {
                continue;
            }

            Player player =
                    playerCache.get(
                            playerId
                    );

            if (player == null) {
                continue;
            }

            /*
             * Wicketkeeper cannot bowl.
             */
            if (player.isWicketKeeper()) {
                continue;
            }

            /*
             * Player must have a bowling style.
             *
             * None = not shown.
             */
            if (!player.hasBowlingStyle()) {
                continue;
            }

            /*
             * T20 maximum = 4 overs.
             *
             * ODI maximum = 10 overs.
             *
             * Test/Custom = unlimited.
             */
            if (maximumOvers > 0) {

                BowlingStat stat =
                        getBowlingStat(
                                playerId
                        );

                int ballsBowled =
                        stat == null
                                ? 0
                                : stat.getBalls();

                if (ballsBowled
                        >= maximumOvers * 6) {

                    continue;
                }
            }

            options.add(
                    playerId
            );
        }

        /*
         * If no bowler remains because of the
         * consecutive-over restriction, allow
         * an eligible bowler only when absolutely
         * necessary.
         */
        if (options.isEmpty()) {

            for (Long playerId : bowlingXI) {

                if (playerId == null) {
                    continue;
                }

                Player player =
                        playerCache.get(
                                playerId
                        );

                if (player == null) {
                    continue;
                }

                if (player.isWicketKeeper()) {
                    continue;
                }

                if (!player.hasBowlingStyle()) {
                    continue;
                }

                if (maximumOvers > 0) {

                    BowlingStat stat =
                            getBowlingStat(
                                    playerId
                            );

                    int ballsBowled =
                            stat == null
                                    ? 0
                                    : stat.getBalls();

                    if (ballsBowled
                            >= maximumOvers * 6) {
                        continue;
                    }
                }

                options.add(playerId);
            }
        }

        if (options.isEmpty()) {

            Toast.makeText(
                    this,
                    "No eligible bowler available. Check bowling styles.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String[] names =
                namesFor(options);

        new AlertDialog.Builder(this)
                .setTitle(
                        "Select bowler"
                )
                .setCancelable(false)
                .setItems(
                        names,
                        (dialog, which) -> {

                            if (which < 0
                                    || which >= options.size()) {
                                return;
                            }

                            bowlerId =
                                    options.get(which);

                            ensureBowlingStat(
                                    bowlerId
                            );

                            refreshScoreboard();
                        }
                )
                .show();
    }

    // ============================================================
    // BATTING STAT
    // ============================================================

    private BattingStat getOrCreateBattingStat(
            long playerId
    ) {

        ensureBattingStat(
                playerId
        );

        return getBattingStat(
                playerId
        );
    }

    private void ensureBattingStat(
            long playerId
    ) {

        if (playerId == -1
                || match == null) {
            return;
        }

        String key =
                statKey(playerId);

        if (battingStats.containsKey(key)) {
            return;
        }

        BattingStat stat =
                new BattingStat();

        stat.setMatchId(
                match.getId()
        );

        stat.setInnings(
                match.getCurrentInnings()
        );

        stat.setPlayerId(
                playerId
        );

        Player player =
                playerCache.get(
                        playerId
                );

        stat.setPlayerName(
                player == null
                        ? "Unknown Player"
                        : safeName(player.getName())
        );

        battingStats.put(
                key,
                stat
        );

        db.saveBattingStat(
                stat
        );
    }

    // ============================================================
    // GET BATTING STAT
    // ============================================================

    private BattingStat getBattingStat(
            long playerId
    ) {

        if (playerId == -1) {
            return null;
        }

        return battingStats.get(
                statKey(playerId)
        );
    }

    // ============================================================
    // BOWLING STAT
    // ============================================================

    private BowlingStat getOrCreateBowlingStat(
            long playerId
    ) {

        ensureBowlingStat(
                playerId
        );

        return getBowlingStat(
                playerId
        );
    }

    private void ensureBowlingStat(
            long playerId
    ) {

        if (playerId == -1
                || match == null) {
            return;
        }

        String key =
                statKey(playerId);

        if (bowlingStats.containsKey(key)) {
            return;
        }

        BowlingStat stat =
                new BowlingStat();

        stat.setMatchId(
                match.getId()
        );

        stat.setInnings(
                match.getCurrentInnings()
        );

        stat.setPlayerId(
                playerId
        );

        Player player =
                playerCache.get(
                        playerId
                );

        stat.setPlayerName(
                player == null
                        ? "Unknown Player"
                        : safeName(player.getName())
        );

        bowlingStats.put(
                key,
                stat
        );

        db.saveBowlingStat(
                stat
        );
    }

    // ============================================================
    // GET BOWLING STAT
    // ============================================================

    private BowlingStat getBowlingStat(
            long playerId
    ) {

        if (playerId == -1) {
            return null;
        }

        return bowlingStats.get(
                statKey(playerId)
        );
    }

    // ============================================================
    // STAT KEY
    // ============================================================

    private String statKey(
            long playerId
    ) {

        return match.getCurrentInnings()
                + ":"
                + playerId;
    }

    // ============================================================
    // PLAYER NAMES
    // ============================================================

    private String[] namesFor(
            List<Long> ids
    ) {

        String[] names =
                new String[ids.size()];

        for (int i = 0;
             i < ids.size();
             i++) {

            Player player =
                    playerCache.get(
                            ids.get(i)
                    );

            if (player == null) {

                names[i] =
                        "Unknown Player";

                continue;
            }

            String name =
                    safeName(
                            player.getName()
                    );

            if (player.getJerseyNumber() > 0) {

                names[i] =
                        name
                                + " (#"
                                + player.getJerseyNumber()
                                + ")";

            } else {

                names[i] =
                        name;
            }
        }

        return names;
    }

    // ============================================================
    // SAFE NAME
    // ============================================================

    private String safeName(
            String name
    ) {

        if (name == null
                || name.trim().isEmpty()) {

            return "Unknown Player";
        }

        return name;
    }

    // ============================================================
    // STRIKE
    // ============================================================

    private void swapStrike() {

        if (strikerId == -1
                || nonStrikerId == -1) {
            return;
        }

        long temporary =
                strikerId;

        strikerId =
                nonStrikerId;

        nonStrikerId =
                temporary;
    }

    // ============================================================
    // OVER CHIP
    // ============================================================

    private void addChip(
            String label
    ) {

        thisOverChips.add(
                label
        );

        rebuildOverChips();
    }

    private void clearOverChips() {

        thisOverChips.clear();

        if (layoutThisOver != null) {

            layoutThisOver.removeAllViews();
        }
    }

    private void rebuildOverChips() {

        if (layoutThisOver == null) {
            return;
        }

        layoutThisOver.removeAllViews();

        for (String label :
                thisOverChips) {

            TextView chip =
                    (TextView) LayoutInflater
                            .from(this)
                            .inflate(
                                    R.layout.ball_chip,
                                    layoutThisOver,
                                    false
                            );

            chip.setText(
                    label
            );

            layoutThisOver.addView(
                    chip
            );
        }
    }

    // ============================================================
    // SAVE CURRENT STATS
    // ============================================================

    private void saveAllCurrentStats() {

        for (BattingStat stat :
                battingStats.values()) {

            db.saveBattingStat(
                    stat
            );
        }

        for (BowlingStat stat :
                bowlingStats.values()) {

            db.saveBowlingStat(
                    stat
            );
        }
    }

    // ============================================================
    // UNDO
    // ============================================================

    private void handleUndo() {

        if (undoStack.isEmpty()) {

            Toast.makeText(
                    this,
                    "Nothing to undo",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Snapshot snapshot =
                undoStack.pop();

        snapshot.restore();

        db.updateMatchLiveState(
                match
        );

        saveAllCurrentStats();

        refreshScoreboard();

        rebuildOverChips();
    }

    // ============================================================
    // PUSH SNAPSHOT
    // ============================================================

    private void pushSnapshot() {

        undoStack.push(
                new Snapshot()
        );
    }

    // ============================================================
    // SNAPSHOT
    // ============================================================

    private class Snapshot {

        int inn1Runs;
        int inn1Wickets;
        int inn1Balls;

        int inn2Runs;
        int inn2Wickets;
        int inn2Balls;

        int inn3Runs;
        int inn3Wickets;
        int inn3Balls;

        int inn4Runs;
        int inn4Wickets;
        int inn4Balls;

        long striker;
        long nonStriker;
        long bowler;

        long previousBowler;

        int legalBalls;

        List<Long> savedOutPlayers;

        Map<String, BattingStatState>
                battingStates;

        Map<String, BowlingStatState>
                bowlingStates;

        List<String> chips;

        Snapshot() {

            inn1Runs =
                    match.getInn1Runs();

            inn1Wickets =
                    match.getInn1Wickets();

            inn1Balls =
                    match.getInn1Balls();

            inn2Runs =
                    match.getInn2Runs();

            inn2Wickets =
                    match.getInn2Wickets();

            inn2Balls =
                    match.getInn2Balls();

            inn3Runs =
                    match.getInn3Runs();

            inn3Wickets =
                    match.getInn3Wickets();

            inn3Balls =
                    match.getInn3Balls();

            inn4Runs =
                    match.getInn4Runs();

            inn4Wickets =
                    match.getInn4Wickets();

            inn4Balls =
                    match.getInn4Balls();

            striker =
                    strikerId;

            nonStriker =
                    nonStrikerId;

            bowler =
                    bowlerId;

            previousBowler =
                    previousBowlerId;

            legalBalls =
                    legalBallsThisOver;

            savedOutPlayers =
                    new ArrayList<>(
                            outPlayers
                    );

            battingStates =
                    new HashMap<>();

            for (
                    Map.Entry<String, BattingStat> entry :
                    battingStats.entrySet()
            ) {

                battingStates.put(
                        entry.getKey(),
                        new BattingStatState(
                                entry.getValue()
                        )
                );
            }

            bowlingStates =
                    new HashMap<>();

            for (
                    Map.Entry<String, BowlingStat> entry :
                    bowlingStats.entrySet()
            ) {

                bowlingStates.put(
                        entry.getKey(),
                        new BowlingStatState(
                                entry.getValue()
                        )
                );
            }

            chips =
                    new ArrayList<>(
                            thisOverChips
                    );
        }

        void restore() {

            match.setInn1Runs(
                    inn1Runs
            );

            match.setInn1Wickets(
                    inn1Wickets
            );

            match.setInn1Balls(
                    inn1Balls
            );

            match.setInn2Runs(
                    inn2Runs
            );

            match.setInn2Wickets(
                    inn2Wickets
            );

            match.setInn2Balls(
                    inn2Balls
            );

            match.setInn3Runs(
                    inn3Runs
            );

            match.setInn3Wickets(
                    inn3Wickets
            );

            match.setInn3Balls(
                    inn3Balls
            );

            match.setInn4Runs(
                    inn4Runs
            );

            match.setInn4Wickets(
                    inn4Wickets
            );

            match.setInn4Balls(
                    inn4Balls
            );

            strikerId =
                    striker;

            nonStrikerId =
                    nonStriker;

            bowlerId =
                    bowler;

            previousBowlerId =
                    previousBowler;

            legalBallsThisOver =
                    legalBalls;

            outPlayers.clear();

            outPlayers.addAll(
                    savedOutPlayers
            );

            battingStats.clear();

            for (
                    Map.Entry<String, BattingStatState> entry :
                    battingStates.entrySet()
            ) {

                battingStats.put(
                        entry.getKey(),
                        entry.getValue()
                                .toBattingStat()
                );
            }

            bowlingStats.clear();

            for (
                    Map.Entry<String, BowlingStatState> entry :
                    bowlingStates.entrySet()
            ) {

                bowlingStats.put(
                        entry.getKey(),
                        entry.getValue()
                                .toBowlingStat()
                );
            }

            thisOverChips.clear();

            thisOverChips.addAll(
                    chips
            );
        }
    }

    // ============================================================
    // BATTING SNAPSHOT STATE
    // ============================================================

    private static class BattingStatState {

        long id;
        long matchId;
        int innings;
        long playerId;

        String playerName;

        int runs;
        int balls;
        int fours;
        int sixes;

        boolean out;
        String howOut;

        BattingStatState(
                BattingStat source
        ) {

            id =
                    source.getId();

            matchId =
                    source.getMatchId();

            innings =
                    source.getInnings();

            playerId =
                    source.getPlayerId();

            playerName =
                    source.getPlayerName();

            runs =
                    source.getRuns();

            balls =
                    source.getBalls();

            fours =
                    source.getFours();

            sixes =
                    source.getSixes();

            out =
                    source.isOut();

            howOut =
                    source.getHowOut();
        }

        BattingStat toBattingStat() {

            BattingStat stat =
                    new BattingStat();

            stat.setId(id);

            stat.setMatchId(
                    matchId
            );

            stat.setInnings(
                    innings
            );

            stat.setPlayerId(
                    playerId
            );

            stat.setPlayerName(
                    playerName
            );

            stat.setRuns(
                    runs
            );

            stat.setBalls(
                    balls
            );

            stat.setFours(
                    fours
            );

            stat.setSixes(
                    sixes
            );

            stat.setOut(
                    out
            );

            stat.setHowOut(
                    howOut
            );

            return stat;
        }
    }

    // ============================================================
    // BOWLING SNAPSHOT STATE
    // ============================================================

    private static class BowlingStatState {

        long id;
        long matchId;
        int innings;
        long playerId;

        String playerName;

        int balls;
        int runsConceded;
        int wickets;

        BowlingStatState(
                BowlingStat source
        ) {

            id =
                    source.getId();

            matchId =
                    source.getMatchId();

            innings =
                    source.getInnings();

            playerId =
                    source.getPlayerId();

            playerName =
                    source.getPlayerName();

            balls =
                    source.getBalls();

            runsConceded =
                    source.getRunsConceded();

            wickets =
                    source.getWickets();
        }

        BowlingStat toBowlingStat() {

            BowlingStat stat =
                    new BowlingStat();

            stat.setId(id);

            stat.setMatchId(
                    matchId
            );

            stat.setInnings(
                    innings
            );

            stat.setPlayerId(
                    playerId
            );

            stat.setPlayerName(
                    playerName
            );

            stat.setBalls(
                    balls
            );

            stat.setRunsConceded(
                    runsConceded
            );

            stat.setWickets(
                    wickets
            );

            return stat;
        }
    }

    // ============================================================
    // FINISH LIMITED OVERS
    // ============================================================

    private void finishLimitedOversMatch() {

        int teamARuns =
                match.getInn1Runs();

        int teamBRuns =
                match.getInn2Runs();

        String result;

        if (teamARuns > teamBRuns) {

            result =
                    match.getTeamAName()
                            + " won by "
                            + (teamARuns - teamBRuns)
                            + " runs";

        } else if (teamBRuns > teamARuns) {

            result =
                    match.getTeamBName()
                            + " won by "
                            + Math.max(
                            0,
                            10 - match.getInn2Wickets()
                    )
                            + " wickets";

        } else {

            result =
                    "Match tied";
        }

        match.setResultText(
                result
        );

        match.setCompleted(
                true
        );

        db.completeMatch(
                match
        );

        showMatchFinished(
                result
        );
    }

    // ============================================================
    // FINISH TEST
    // ============================================================

    private void finishTestMatch() {

        int teamARuns =
                testTeamARuns();

        int teamBRuns =
                testTeamBRuns();

        String result;

        /*
         * If Team B exceeds Team A aggregate,
         * Team B wins.
         */
        if (teamBRuns > teamARuns) {

            result =
                    match.getTeamBName()
                            + " won";

        } else if (teamARuns > teamBRuns) {

            result =
                    match.getTeamAName()
                            + " won";

        } else {

            result =
                    "Test match drawn";
        }

        match.setResultText(
                result
        );

        match.setCompleted(
                true
        );

        db.completeMatch(
                match
        );

        showMatchFinished(
                result
        );
    }

    // ============================================================
    // MATCH FINISHED DIALOG
    // ============================================================

    private void showMatchFinished(
            String result
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Match completed"
                )
                .setMessage(
                        result
                )
                .setCancelable(false)
                .setPositiveButton(
                        "OK",
                        (dialog, which) ->
                                finish()
                )
                .show();
    }

    // ============================================================
    // EXIT
    // ============================================================

    private void confirmExit() {

        if (match == null
                || match.isCompleted()) {

            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        "Exit live scoring?"
                )
                .setMessage(
                        "Your current score is saved."
                )
                .setNegativeButton(
                        R.string.cancel,
                        null
                )
                .setPositiveButton(
                        "Exit",
                        (dialog, which) ->
                                finish()
                )
                .show();
    }
}