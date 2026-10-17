package com.example.cricketscorer.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

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
import java.util.HashSet;
import java.util.Set;
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

    private final Set<Long> retiredHurtBatters = new HashSet<>();
    private final Set<Long> retiredHurtBowlers = new HashSet<>();

    // ============================================================
    // CURRENT OVER
    // ============================================================

    private int legalBallsThisOver = 0;
    private int runsConcededThisOver = 0;

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
    private TextView tvPartnership;

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

        requestNotificationPermission();

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

        tvPartnership =
                findViewById(R.id.tvPartnership);
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

        if (battingIds != null) {

            for (long id : battingIds) {
                battingXIBackup.add(id);
            }
        } else {
            java.io.Serializable ser = getIntent().getSerializableExtra("batting_xi");
            if (ser instanceof ArrayList) {
                for (Object obj : (ArrayList<?>) ser) {
                    if (obj instanceof Number) {
                        battingXIBackup.add(((Number) obj).longValue());
                    }
                }
            }
        }

        long[] bowlingIds =
                getIntent().getLongArrayExtra(
                        "bowling_xi"
                );

        if (bowlingIds != null) {

            for (long id : bowlingIds) {
                bowlingXIBackup.add(id);
            }
        } else {
            java.io.Serializable ser = getIntent().getSerializableExtra("bowling_xi");
            if (ser instanceof ArrayList) {
                for (Object obj : (ArrayList<?>) ser) {
                    if (obj instanceof Number) {
                        bowlingXIBackup.add(((Number) obj).longValue());
                    }
                }
            }
        }

        if (battingXIBackup.isEmpty() && match != null) {
            List<Player> teamAPlayers = db.getPlayersByTeam(match.getTeamAId());
            if (teamAPlayers != null) {
                for (Player p : teamAPlayers) {
                    if (p != null) battingXIBackup.add(p.getId());
                }
            }
        }

        if (bowlingXIBackup.isEmpty() && match != null) {
            List<Player> teamBPlayers = db.getPlayersByTeam(match.getTeamBId());
            if (teamBPlayers != null) {
                for (Player p : teamBPlayers) {
                    if (p != null) bowlingXIBackup.add(p.getId());
                }
            }
        }

        cachePlayers(battingXIBackup);
        cachePlayers(bowlingXIBackup);

        for (long pid : battingXIBackup) {
            if (pid > 0 && match != null) {
                BattingStat stat = getBattingStat(pid);
                if (stat == null) {
                    stat = new BattingStat(match.getId(), match.getCurrentInnings(), pid);
                    Player p = playerCache.get(pid);
                    stat.setPlayerName(p != null ? safeName(p.getName()) : "Player " + pid);
                    battingStats.put(statKey(pid), stat);
                    db.saveBattingStat(stat);
                }
            }
        }
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

        findViewById(R.id.btnRetireHurt)
                .setOnClickListener(
                        v -> handleRetireHurt()
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

        findViewById(R.id.btnRetireHurt)
                .setOnClickListener(
                        v -> handleRetireHurt()
                );

        findViewById(R.id.btnRetireBowler)
                .setOnClickListener(
                        v -> handleRetireBowlerHurt()
                );

        findViewById(R.id.btnPauseMatch)
                .setOnClickListener(
                        v -> pauseMatch()
                );
    }

    private void pauseMatch() {
        if (match == null || isMatchFinished()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Pause Match")
                .setMessage("Save current match progress and return to dashboard? You can resume anytime from 'Matches to Continue'.")
                .setPositiveButton("Pause", (dialog, which) -> {
                    match.setPaused(true);
                    match.setStrikerId(strikerId);
                    match.setNonStrikerId(nonStrikerId);
                    match.setBowlerId(bowlerId);
                    match.setPreviousBowlerId(previousBowlerId);
                    match.setLegalBallsThisOver(legalBallsThisOver);
                    saveMatchLiveState();
                    Toast.makeText(this, "Match paused successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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

        BowlingStat bowlingStat = getOrCreateBowlingStat(bowlerId);
        if (bowlingStat != null && runsConcededThisOver == 0) {
            bowlingStat.setMaidens(bowlingStat.getMaidens() + 1);
            db.saveBowlingStat(bowlingStat);
        }

        runsConcededThisOver = 0;

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

            if (runs == 0 && !isBye && !isLegBye) {
                bowlingStat.setDotBalls(
                        bowlingStat.getDotBalls() + 1
                );
            }
        }

        /*
         * Normal batting runs.
         *
         * Byes and leg-byes do not belong
         * to the batter or bowler.
         */
        if (!isBye && !isLegBye) {

            runsConcededThisOver += runs;

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

        updatePartnership(runs, legalDelivery);

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

        runsConcededThisOver += extraRuns;

        bowlingStat.setRunsConceded(
                bowlingStat.getRunsConceded()
                        + extraRuns
        );

        bowlingStat.setExtrasConceded(
                bowlingStat.getExtrasConceded()
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
        updatePartnership(extraRuns, false);
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

        showCustomSelectionDialog(
                "How was the batter out?",
                R.drawable.ic_wicket,
                dismissals,
                (dialog, which) ->
                        processWicket(
                                dismissals[which]
                        )
        );
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

        BattingStat battingStat =
                getOrCreateBattingStat(
                        strikerId
                );

        BowlingStat bowlingStat =
                getOrCreateBowlingStat(
                        bowlerId
                );

        if ("Stumped".equals(dismissalType)) {
            long wkId = 0;
            for (Long pid : bowlingXI) {
                Player p = playerCache.get(pid);
                if (p != null && p.isWicketKeeper()) {
                    wkId = pid;
                    break;
                }
            }
            if (wkId == 0 && !bowlingXI.isEmpty()) {
                wkId = bowlingXI.get(0);
            }
            battingStat.setFielderId(wkId);
            finalizeWicket(battingStat, bowlingStat, dismissalType);
        } else if ("Caught".equals(dismissalType) || "Run Out".equals(dismissalType)) {
            promptFielderForDismissal(dismissalType, selectedFielderId -> {
                battingStat.setFielderId(selectedFielderId);
                finalizeWicket(battingStat, bowlingStat, dismissalType);
            });
        } else {
            battingStat.setFielderId(0);
            finalizeWicket(battingStat, bowlingStat, dismissalType);
        }
    }

    private void promptFielderForDismissal(String dismissalType, java.util.function.Consumer<Long> onFielderSelected) {
        if (bowlingXI.isEmpty()) {
            onFielderSelected.accept(0L);
            return;
        }
        List<Long> fielderIds = new ArrayList<>(bowlingXI);
        String[] names = namesFor(fielderIds);
        String title = "Who " + ("Caught".equals(dismissalType) ? "caught the ball?" : "executed the run out?");

        showCustomSelectionDialog(
                title,
                R.drawable.ic_player,
                names,
                (dialog, which) -> {
                    if (which >= 0 && which < fielderIds.size()) {
                        onFielderSelected.accept(fielderIds.get(which));
                    } else {
                        onFielderSelected.accept(0L);
                    }
                }
        );
    }

    private void showCustomSelectionDialog(
            String title,
            int iconResId,
            String[] items,
            android.content.DialogInterface.OnClickListener onClickListener
    ) {
        if (isFinishing() || isDestroyed()) return;

        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        titleLayout.setGravity(Gravity.CENTER_VERTICAL);
        int pLR = dpToPx(18);
        int pTB = dpToPx(14);
        titleLayout.setPadding(pLR, pTB, pLR, pTB);
        titleLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));

        ImageView icon = new ImageView(this);
        int iconSize = dpToPx(24);
        icon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
        icon.setImageResource(iconResId);
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(Color.WHITE));
        titleLayout.addView(icon);

        TextView titleTv = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMarginStart(dpToPx(12));
        titleTv.setLayoutParams(titleParams);
        titleTv.setText(title);
        titleTv.setTextSize(16);
        titleTv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleTv.setTextColor(Color.WHITE);
        titleLayout.addView(titleTv);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(titleLayout)
                .setItems(items, onClickListener)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);
        }
        dialog.show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void finalizeWicket(BattingStat battingStat, BowlingStat bowlingStat, String dismissalType) {
        pushSnapshot();

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

        battingStat.setBowlerId(bowlerId);
        Player bowlerPlayer = playerCache.get(bowlerId);
        battingStat.setBowlerName(bowlerPlayer != null ? bowlerPlayer.getName() : "");

        long fielderId = battingStat.getFielderId();
        Player fielderPlayer = playerCache.get(fielderId);
        battingStat.setFielderName(fielderPlayer != null ? fielderPlayer.getName() : "");

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

        resetPartnership();

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

    private void handleRetireHurt() {
        if (isMatchFinished()) {
            return;
        }
        if (strikerId == -1) {
            Toast.makeText(this, "No striker on crease", Toast.LENGTH_SHORT).show();
            return;
        }

        Player player = playerCache.get(strikerId);
        String name = player != null ? player.getName() : "Batter";

        new AlertDialog.Builder(this)
                .setTitle("Retire " + name + " hurt?")
                .setMessage("This batter will leave the crease without being out.")
                .setPositiveButton("Retire Hurt", (dialog, which) -> processRetireHurt())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void processRetireHurt() {
        if (strikerId == -1) {
            return;
        }

        pushSnapshot();

        retiredHurtBatters.add(strikerId);

        BattingStat battingStat = getOrCreateBattingStat(strikerId);
        battingStat.setOut(false);
        battingStat.setHowOut("Retired Hurt");
        battingStat.setFielderId(0);
        db.saveBattingStat(battingStat);

        outPlayers.add(strikerId);
        addChip("RT");

        strikerId = -1;
        resetPartnership();

        boolean allOut = wicketsInInnings() >= MatchInfo.MAX_WICKETS_PER_INNINGS
                || outPlayers.size() >= Math.max(0, battingXI.size() - 1);

        if (!allOut) {
            selectNewBatsman();
        }

        afterBallUpdate();
    }

    private void handleRetireBowlerHurt() {
        if (isMatchFinished()) {
            return;
        }
        if (bowlerId == -1) {
            Toast.makeText(this, "No bowler currently bowling", Toast.LENGTH_SHORT).show();
            return;
        }

        Player player = playerCache.get(bowlerId);
        String name = player != null ? safeName(player.getName()) : "Bowler";

        new AlertDialog.Builder(this)
                .setTitle("Retire " + name + " hurt?")
                .setMessage("Mark this bowler as retired hurt / unable to continue bowling.")
                .setPositiveButton("Retire Bowler", (dialog, which) -> processRetireBowlerHurt())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void processRetireBowlerHurt() {
        if (bowlerId == -1) {
            return;
        }

        pushSnapshot();

        retiredHurtBowlers.add(bowlerId);

        BowlingStat bowlingStat = getOrCreateBowlingStat(bowlerId);
        if (bowlingStat != null) {
            db.saveBowlingStat(bowlingStat);
        }

        Player player = playerCache.get(bowlerId);
        String name = player != null ? safeName(player.getName()) : "Bowler";
        Toast.makeText(this, name + " retired hurt", Toast.LENGTH_SHORT).show();

        bowlerId = -1;

        saveMatchLiveState();
        refreshScoreboard();

        selectReplacementBowler();
    }

    private void selectReplacementBowler() {
        if (bowlingXI.isEmpty()) {
            Toast.makeText(this, "No replacement bowlers available", Toast.LENGTH_LONG).show();
            return;
        }

        List<Long> options = new ArrayList<>();
        int maximumOvers = getMaximumOversPerBowler();

        for (Long playerId : bowlingXI) {
            if (playerId == null || playerId == -1) continue;

            if (retiredHurtBowlers.contains(playerId)) continue;

            if (legalBallsThisOver == 0 && playerId == previousBowlerId) continue;

            Player player = playerCache.get(playerId);
            if (player == null || player.isWicketKeeper() || !player.hasBowlingStyle()) continue;

            if (maximumOvers > 0) {
                BowlingStat stat = getBowlingStat(playerId);
                int ballsBowled = stat == null ? 0 : stat.getBalls();
                if (ballsBowled >= maximumOvers * 6) continue;
            }

            options.add(playerId);
        }

        if (options.isEmpty()) {
            for (Long playerId : bowlingXI) {
                if (playerId != null && !retiredHurtBowlers.contains(playerId)) {
                    Player p = playerCache.get(playerId);
                    if (p != null && !p.isWicketKeeper()) {
                        options.add(playerId);
                    }
                }
            }
        }

        if (options.isEmpty()) {
            Toast.makeText(this, "No eligible replacement bowler found", Toast.LENGTH_LONG).show();
            return;
        }

        String[] names = namesFor(options);
        String title = legalBallsThisOver > 0
                ? "Select bowler to finish over (Ball " + (legalBallsThisOver + 1) + " of 6)"
                : "Select bowler";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setCancelable(false)
                .setItems(names, (dialog, which) -> {
                    if (which >= 0 && which < options.size()) {
                        bowlerId = options.get(which);
                        ensureBowlingStat(bowlerId);
                        saveMatchLiveState();
                        refreshScoreboard();
                    }
                })
                .show();
    }

    private long currentBat1 = -1;
    private long currentBat2 = -1;
    private int partnershipRuns = 0;
    private int partnershipBalls = 0;

    private void updatePartnership(int runs, boolean legalBall) {
        long s = strikerId;
        long ns = nonStrikerId;
        if (s <= 0 || ns <= 0) return;

        if (!((currentBat1 == s && currentBat2 == ns) || (currentBat1 == ns && currentBat2 == s))) {
            currentBat1 = s;
            currentBat2 = ns;
            partnershipRuns = 0;
            partnershipBalls = 0;
        }
        partnershipRuns += runs;
        if (legalBall) {
            partnershipBalls++;
        }
        updatePartnershipView();
    }

    private void resetPartnership() {
        currentBat1 = strikerId;
        currentBat2 = nonStrikerId;
        partnershipRuns = 0;
        partnershipBalls = 0;
        updatePartnershipView();
    }

    private void updatePartnershipView() {
        if (tvPartnership != null) {
            tvPartnership.setText("Partnership: " + partnershipRuns + " runs (" + partnershipBalls + " balls)");
        }
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
    // AFTER BALL UPDATE
    // ============================================================

    private void afterBallUpdate() {

        if (match == null) {
            return;
        }

        saveMatchLiveState();

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
         * Test Match is intentionally unsupported.
         * Only limited-overs matches are allowed.
         */
        boolean testChased = false;

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
    // ============================================================
    // MATCH FORMAT
    // ============================================================

    private boolean isLimitedOversMatch() {
        return true;
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

        saveMatchLiveState();

        int current =
                match.getCurrentInnings();

        if (current == 1) {

            showLimitedOversInningsOneComplete();

        } else {

            finishLimitedOversMatch();
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
    // START NEXT INNINGS
    // ============================================================

    private void startNextInnings() {

        int nextInnings =
                match.getCurrentInnings() + 1;

        if (nextInnings > 2) {
            finishLimitedOversMatch();
            return;
        }

        /*
         * Save current state first.
         */
        saveMatchLiveState();

        match.setCurrentInnings(
                nextInnings
        );

        retiredHurtBatters.clear();
        retiredHurtBowlers.clear();
        runsConcededThisOver = 0;

        resetPartnership();

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

        for (long pid : battingXI) {
            if (pid > 0 && match != null) {
                BattingStat stat = getBattingStat(pid);
                if (stat == null) {
                    stat = new BattingStat(match.getId(), match.getCurrentInnings(), pid);
                    Player p = playerCache.get(pid);
                    stat.setPlayerName(p != null ? safeName(p.getName()) : "Player " + pid);
                    battingStats.put(statKey(pid), stat);
                    db.saveBattingStat(stat);
                }
            }
        }

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

        saveMatchLiveState();

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

        for (long pid : battingXI) {
            if (pid > 0 && match != null) {
                BattingStat stat = getBattingStat(pid);
                if (stat == null) {
                    stat = new BattingStat(match.getId(), match.getCurrentInnings(), pid);
                    Player p = playerCache.get(pid);
                    stat.setPlayerName(p != null ? safeName(p.getName()) : "Player " + pid);
                    battingStats.put(statKey(pid), stat);
                    db.saveBattingStat(stat);
                }
            }
        }

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

        strikerId = match.getStrikerId();
        nonStrikerId = match.getNonStrikerId();
        bowlerId = match.getBowlerId();
        previousBowlerId = match.getPreviousBowlerId();
        legalBallsThisOver = match.getLegalBallsThisOver() > 0 ? match.getLegalBallsThisOver() : (ballsBowledInInnings() % 6);

        rebuildOverChips();
        refreshScoreboard();

        if (strikerId > 0 && nonStrikerId > 0 && bowlerId > 0) {
            return;
        }

        if (wicketsInInnings() >= MatchInfo.MAX_WICKETS_PER_INNINGS) {
            endInnings();
            return;
        }

        if (strikerId <= 0 || nonStrikerId <= 0) {
            selectOpeningBatsmen();
        } else if (bowlerId <= 0) {
            selectBowler();
        }
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

        showCustomSelectionDialog(
                "Select opening striker",
                R.drawable.ic_player,
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

                    saveMatchLiveState();

                    selectNonStriker();
                }
        );
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

        showCustomSelectionDialog(
                "Select non-striker",
                R.drawable.ic_player,
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

                    saveMatchLiveState();

                    selectBowler();
                }
        );
    }

    // ============================================================
    // NEW BATSMAN
    // ============================================================

    private void selectNewBatsman() {

        List<Long> available = new ArrayList<>();
        for (Long pid : battingXI) {
            if (pid == null || pid == -1) continue;
            if (pid == strikerId || pid == nonStrikerId) continue;
            if (!outPlayers.contains(pid) || retiredHurtBatters.contains(pid)) {
                available.add(pid);
            }
        }

        if (available.isEmpty()) {
            return;
        }

        List<String> namesList = new ArrayList<>();
        for (Long pid : available) {
            Player p = playerCache.get(pid);
            String spec = p != null ? formatPlayerSpec(p) : "Player " + pid;
            if (retiredHurtBatters.contains(pid)) {
                spec += " [Retired Hurt - Return]";
            }
            namesList.add(spec);
        }

        String[] names = namesList.toArray(new String[0]);

        showCustomSelectionDialog(
                "Select next batter",
                R.drawable.ic_player,
                names,
                (dialog, which) -> {

                    if (which < 0
                            || which >= available.size()) {
                        return;
                    }

                    long selectedId = available.get(which);

                    if (retiredHurtBatters.contains(selectedId)) {
                        retiredHurtBatters.remove(selectedId);
                        outPlayers.remove(selectedId);
                    }

                    strikerId = selectedId;

                    ensureBattingStat(
                            strikerId
                    );

                    resetPartnership();
                    saveMatchLiveState();
                    refreshScoreboard();
                }
        );
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

            if (retiredHurtBowlers.contains(playerId)) {
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

        showCustomSelectionDialog(
                "Select bowler",
                R.drawable.ic_match,
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

                    saveMatchLiveState();

                    refreshScoreboard();
                }
        );
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

    private String formatPlayerSpec(Player player) {
        if (player == null) {
            return "Unknown Player";
        }
        String name = safeName(player.getName());
        StringBuilder sb = new StringBuilder(name);
        if (player.getJerseyNumber() > 0) {
            sb.append(" (#").append(player.getJerseyNumber()).append(")");
        }

        List<String> specs = new ArrayList<>();
        if (player.getRole() != null && !player.getRole().trim().isEmpty()) {
            specs.add(player.getRole().trim());
        }
        if (player.getBattingHand() != null && !player.getBattingHand().trim().isEmpty()) {
            specs.add(player.getBattingHand().trim());
        }
        if (player.hasBowlingStyle()) {
            specs.add(player.getBowlingStyle().trim());
        }

        if (!specs.isEmpty()) {
            sb.append(" · ");
            for (int i = 0; i < specs.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(specs.get(i));
            }
        }
        return sb.toString();
    }

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

            names[i] = formatPlayerSpec(player);
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

    private void saveMatchLiveState() {
        if (match == null || db == null) {
            return;
        }
        db.updateMatchLiveState(match);
        for (BattingStat stat : battingStats.values()) {
            if (stat != null) {
                db.saveBattingStat(stat);
            }
        }
        for (BowlingStat stat : bowlingStats.values()) {
            if (stat != null) {
                db.saveBowlingStat(stat);
            }
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

        saveMatchLiveState();

        /*
         * Undo must make SQLite identical to the restored snapshot.
         * Simply calling save/update is NOT enough because the undone
         * delivery may have created a new batting/bowling row (for
         * example the first wicket or first appearance of a bowler).
         * Those rows must be deleted when they are absent from the
         * restored snapshot.
         */
        int innings = match.getCurrentInnings();

        db.replaceBattingStatsForInnings(
                match.getId(),
                innings,
                new ArrayList<>(battingStats.values())
        );

        db.replaceBowlingStatsForInnings(
                match.getId(),
                innings,
                new ArrayList<>(bowlingStats.values())
        );

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
        Set<Long> savedRetiredHurtBatters;
        Set<Long> savedRetiredHurtBowlers;
        int savedRunsConcededThisOver;
        int savedPartnershipRuns;
        int savedPartnershipBalls;
        long savedCurrentBat1;
        long savedCurrentBat2;

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

            savedRetiredHurtBatters = new HashSet<>(retiredHurtBatters);
            savedRetiredHurtBowlers = new HashSet<>(retiredHurtBowlers);
            savedRunsConcededThisOver = runsConcededThisOver;
            savedPartnershipRuns = partnershipRuns;
            savedPartnershipBalls = partnershipBalls;
            savedCurrentBat1 = currentBat1;
            savedCurrentBat2 = currentBat2;
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

            retiredHurtBatters.clear();
            retiredHurtBatters.addAll(savedRetiredHurtBatters);

            retiredHurtBowlers.clear();
            retiredHurtBowlers.addAll(savedRetiredHurtBowlers);

            runsConcededThisOver = savedRunsConcededThisOver;

            partnershipRuns = savedPartnershipRuns;
            partnershipBalls = savedPartnershipBalls;
            currentBat1 = savedCurrentBat1;
            currentBat2 = savedCurrentBat2;
            updatePartnershipView();

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

        saveMatchLiveState();

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

        sendMatchCompletedNotification(result);

        Intent intent = new Intent(this, MatchSummaryActivity.class);
        intent.putExtra("match_id", match.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sendMatchCompletedNotification(String result) {
        if (match != null) {
            com.example.cricketscorer.utils.NotificationHelper.sendMatchCompletedNotification(
                    this,
                    match.getId(),
                    result
            );
        }
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
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