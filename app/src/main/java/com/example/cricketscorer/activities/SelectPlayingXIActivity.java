package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.adapters.PlayerCheckboxAdapter;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.MatchInfo;
import com.example.cricketscorer.models.Player;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Select Playing XI / Squad
 *
 * Flow:
 *
 * Team A
 *   -> Select exactly 15 players
 *   -> Select exactly 11 Playing XI
 *
 * Team B
 *   -> Select exactly 15 players
 *   -> Select exactly 11 Playing XI
 *
 * Rules:
 *   - Squad = exactly 15
 *   - Squad = exactly 2 wicketkeepers
 *   - Playing XI = exactly 11
 *   - Playing XI = exactly 1 wicketkeeper
 *   - Remaining 4 players automatically become substitutes
 *   - Captain and Vice Captain are selected from Playing XI
 */
public class SelectPlayingXIActivity extends AppCompatActivity {

    // ============================================================
    // DATABASE
    // ============================================================

    private DatabaseHelper db;

    // ============================================================
    // VIEWS
    // ============================================================

    private Toolbar toolbar;

    private TextView tvTeamHeading;
    private TextView tvHint;
    private TextView tvSelectedCount;

    private RecyclerView recyclerPlayers;

    private MaterialButton btnConfirm;

    // ============================================================
    // ADAPTER
    // ============================================================

    private PlayerCheckboxAdapter adapter;

    // ============================================================
    // CURRENT PLAYER LIST
    // ============================================================

    private final List<Player> currentPlayers =
            new ArrayList<>();

    // ============================================================
    // TEAM A
    // ============================================================

    private final List<Player> teamAPlayers =
            new ArrayList<>();

    private final List<Player> teamAXI =
            new ArrayList<>();

    private final ArrayList<Long> teamASquadIds =
            new ArrayList<>();

    private final ArrayList<Long> teamAXIIds =
            new ArrayList<>();

    private final ArrayList<Long> teamASubstituteIds =
            new ArrayList<>();

    // ============================================================
    // TEAM B
    // ============================================================

    private final List<Player> teamBPlayers =
            new ArrayList<>();

    private final List<Player> teamBXI =
            new ArrayList<>();

    private final ArrayList<Long> teamBSquadIds =
            new ArrayList<>();

    private final ArrayList<Long> teamBXIIds =
            new ArrayList<>();

    private final ArrayList<Long> teamBSubstituteIds =
            new ArrayList<>();

    // ============================================================
    // TEAM INFORMATION
    // ============================================================

    private long teamAId = -1;
    private long teamBId = -1;

    private String teamAName = "Team A";
    private String teamBName = "Team B";

    // ============================================================
    // MATCH INFORMATION
    // ============================================================

    private String matchFormat = "T20";

    private int totalOvers = 20;

    private int inningsPerTeam = 1;

    // ============================================================
    // TOSS
    // ============================================================

    private long tossWinnerId = -1;

    private String tossDecision = "";

    private long battingTeamId = -1;

    private long bowlingTeamId = -1;

    // ============================================================
    // CURRENT STEP
    // ============================================================

    /*
     * 0 = Team A Squad
     * 1 = Team A Playing XI
     * 2 = Team B Squad
     * 3 = Team B Playing XI
     */
    private int currentStep = 0;

    // ============================================================
    // CAPTAIN / VICE CAPTAIN
    // ============================================================

    private long teamACaptainId = -1;

    private long teamAViceCaptainId = -1;

    private long teamBCaptainId = -1;

    private long teamBViceCaptainId = -1;

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_select_playing_xi
        );

        db =
                DatabaseHelper.getInstance(this);

        readIntentData();

        initializeViews();

        setupBackNavigation();

        if (!validateMatchData()) {

            finish();

            return;
        }

        loadStep();
    }

    // ============================================================
    // READ INTENT DATA
    // ============================================================

    private void readIntentData() {

        Intent intent =
                getIntent();

        if (intent == null) {
            return;
        }

        // --------------------------------------------------------
        // TEAM IDS
        // --------------------------------------------------------

        teamAId =
                intent.getLongExtra(
                        "team_a_id",
                        -1
                );

        teamBId =
                intent.getLongExtra(
                        "team_b_id",
                        -1
                );

        // --------------------------------------------------------
        // TEAM NAMES
        // --------------------------------------------------------

        String receivedTeamAName =
                intent.getStringExtra(
                        "team_a_name"
                );

        String receivedTeamBName =
                intent.getStringExtra(
                        "team_b_name"
                );

        if (receivedTeamAName != null
                && !receivedTeamAName.trim().isEmpty()) {

            teamAName =
                    receivedTeamAName;
        }

        if (receivedTeamBName != null
                && !receivedTeamBName.trim().isEmpty()) {

            teamBName =
                    receivedTeamBName;
        }

        // --------------------------------------------------------
        // FORMAT
        // --------------------------------------------------------

        String receivedFormat =
                intent.getStringExtra(
                        "match_format"
                );

        if (receivedFormat != null
                && !receivedFormat.trim().isEmpty()) {

            matchFormat =
                    receivedFormat;
        }

        // --------------------------------------------------------
        // OVERS
        // --------------------------------------------------------

        int defaultOvers = 20;

        if ("ODI".equalsIgnoreCase(matchFormat)) {

            defaultOvers = 50;

        } else if ("T20".equalsIgnoreCase(matchFormat)) {

            defaultOvers = 20;

        } else if ("Custom".equalsIgnoreCase(matchFormat)) {

            defaultOvers =
                    intent.getIntExtra(
                            "total_overs",
                            20
                    );
        }

        totalOvers =
                intent.getIntExtra(
                        "total_overs",
                        defaultOvers
                );

        // --------------------------------------------------------
        // LIMITED-OVERS MATCH ONLY
        // --------------------------------------------------------

        // Test matches are no longer supported.
        // Every match has exactly one innings per team.
        inningsPerTeam = 1;

        // --------------------------------------------------------
        // TOSS
        // --------------------------------------------------------

        tossWinnerId =
                intent.getLongExtra(
                        "toss_winner_id",
                        -1
                );

        String receivedTossDecision =
                intent.getStringExtra(
                        "toss_decision"
                );

        if (receivedTossDecision != null) {

            tossDecision =
                    receivedTossDecision;
        }

        // --------------------------------------------------------
        // BATTING / BOWLING TEAM
        // --------------------------------------------------------

        battingTeamId =
                intent.getLongExtra(
                        "batting_team_id",
                        -1
                );

        bowlingTeamId =
                intent.getLongExtra(
                        "bowling_team_id",
                        -1
                );
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );

        tvTeamHeading =
                findViewById(
                        R.id.tvTeamHeading
                );

        tvHint =
                findViewById(
                        R.id.tvHint
                );

        tvSelectedCount =
                findViewById(
                        R.id.tvSelectedCount
                );

        recyclerPlayers =
                findViewById(
                        R.id.recyclerPlayers
                );

        btnConfirm =
                findViewById(
                        R.id.btnConfirm
                );

        recyclerPlayers.setLayoutManager(
                new LinearLayoutManager(this)
        );

        if (toolbar != null) {

            toolbar.setNavigationOnClickListener(
                    v -> handleBack()
            );
        }

        if (btnConfirm != null) {

            btnConfirm.setOnClickListener(
                    v -> handleNext()
            );
        }
    }

    // ============================================================
    // BACK NAVIGATION
    // ============================================================

    private void setupBackNavigation() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                handleBack();
                            }
                        }
                );
    }

    // ============================================================
    // HANDLE BACK
    // ============================================================

    private void handleBack() {

        if (currentStep > 0) {

            currentStep--;

            loadStep();

        } else {

            finish();
        }
    }

    // ============================================================
    // VALIDATE MATCH DATA
    // ============================================================

    private boolean validateMatchData() {

        if (teamAId <= 0
                || teamBId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid teams",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (teamAId == teamBId) {

            Toast.makeText(
                    this,
                    "Both teams cannot be the same",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }

    // ============================================================
    // LOAD CURRENT STEP
    // ============================================================

    private void loadStep() {

        switch (currentStep) {

            case 0:

                loadTeamASquad();

                break;

            case 1:

                loadTeamAXI();

                break;

            case 2:

                loadTeamBSquad();

                break;

            case 3:

                loadTeamBXI();

                break;

            default:

                finishSelection();

                break;
        }
    }

    // ============================================================
    // TEAM A - SQUAD
    // ============================================================

    private void loadTeamASquad() {

        currentPlayers.clear();

        List<Player> players =
                db.getPlayersByTeam(
                        teamAId
                );

        if (players != null) {

            currentPlayers.addAll(
                    players
            );
        }

        if (!validateAvailableSquad(
                currentPlayers,
                teamAName
        )) {

            return;
        }

        preselectSquad(currentPlayers, teamASquadIds);

        tvTeamHeading.setText(
                teamAName +
                        " - Select Squad"
        );

        tvHint.setText(
                "Select exactly 15 players including exactly 2 wicketkeepers"
        );

        showPlayers(
                currentPlayers,
                teamASquadIds,
                15
        );

        btnConfirm.setText(
                "CONFIRM SQUAD (15)"
        );
    }

    // ============================================================
    // TEAM A - PLAYING XI
    // ============================================================

    private void loadTeamAXI() {

        currentPlayers.clear();

        /*
         * IMPORTANT:
         *
         * Get players from Team A's selected 15-player squad.
         * This prevents players outside the selected squad
         * from appearing in the Playing XI screen.
         */

        for (Player player : teamAPlayers) {

            if (player == null) {
                continue;
            }

            if (teamASquadIds.contains(
                    player.getId()
            )) {

                currentPlayers.add(
                        player
                );
            }
        }

        preselectXI(currentPlayers, teamAXIIds);

        tvTeamHeading.setText(
                teamAName +
                        " - Select Playing XI"
        );

        tvHint.setText(
                "Select exactly 11 players including exactly 1 wicketkeeper"
        );

        showPlayers(
                currentPlayers,
                teamAXIIds,
                11
        );

        btnConfirm.setText(
                "CONFIRM PLAYING XI (11)"
        );
    }

    // ============================================================
    // TEAM B - SQUAD
    // ============================================================

    private void loadTeamBSquad() {

        currentPlayers.clear();

        List<Player> players =
                db.getPlayersByTeam(
                        teamBId
                );

        if (players != null) {

            currentPlayers.addAll(
                    players
            );
        }

        if (!validateAvailableSquad(
                currentPlayers,
                teamBName
        )) {

            return;
        }

        preselectSquad(currentPlayers, teamBSquadIds);

        tvTeamHeading.setText(
                teamBName +
                        " - Select Squad"
        );

        tvHint.setText(
                "Select exactly 15 players including exactly 2 wicketkeepers"
        );

        showPlayers(
                currentPlayers,
                teamBSquadIds,
                15
        );

        btnConfirm.setText(
                "CONFIRM SQUAD (15)"
        );
    }

    // ============================================================
    // TEAM B - PLAYING XI
    // ============================================================

    private void loadTeamBXI() {

        currentPlayers.clear();

        for (Player player : teamBPlayers) {

            if (player == null) {
                continue;
            }

            if (teamBSquadIds.contains(
                    player.getId()
            )) {

                currentPlayers.add(
                        player
                );
            }
        }

        preselectXI(currentPlayers, teamBXIIds);

        tvTeamHeading.setText(
                teamBName +
                        " - Select Playing XI"
        );

        tvHint.setText(
                "Select exactly 11 players including exactly 1 wicketkeeper"
        );

        showPlayers(
                currentPlayers,
                teamBXIIds,
                11
        );

        btnConfirm.setText(
                "START MATCH"
        );
    }

    // ============================================================
    // CHECK AVAILABLE SQUAD
    // ============================================================

    private boolean validateAvailableSquad(
            List<Player> players,
            String teamName
    ) {

        if (players == null
                || players.size() < 15) {

            Toast.makeText(
                    this,
                    teamName +
                            " must have at least 15 players",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        int wicketKeepers =
                countWicketKeepers(
                        players
                );

        if (wicketKeepers < 2) {

            Toast.makeText(
                    this,
                    teamName +
                            " must have at least 2 wicketkeepers",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        return true;
    }

    // ============================================================
    // COUNT WICKETKEEPERS
    // ============================================================

    private int countWicketKeepers(
            List<Player> players
    ) {

        int count = 0;

        if (players == null) {
            return 0;
        }

        for (Player player : players) {

            if (player != null
                    && player.isWicketKeeper()) {

                count++;
            }
        }

        return count;
    }

    // ============================================================
    // SHOW PLAYERS
    // ============================================================

    private void showPlayers(
            List<Player> players,
            ArrayList<Long> selectedIds,
            int maxSelection
    ) {

        adapter =
                new PlayerCheckboxAdapter(
                        players,
                        maxSelection
                );

        /*
         * Restore selections if the user navigates
         * backwards and returns to this screen.
         */

        adapter.setSelectedIds(
                selectedIds
        );

        adapter.setOnSelectionChangedListener((count, max) -> {
            if (tvSelectedCount != null) {
                tvSelectedCount.setText("Selected: " + count + " / " + max);
            }
            if (btnConfirm != null) {
                if (max == 15) {
                    btnConfirm.setText("CONFIRM SQUAD (" + count + " / 15)");
                } else if (currentStep == 3) {
                    btnConfirm.setText("START MATCH (" + count + " / 11)");
                } else {
                    btnConfirm.setText("CONFIRM PLAYING XI (" + count + " / 11)");
                }
            }
        });

        if (tvSelectedCount != null) {
            tvSelectedCount.setText("Selected: " + adapter.getSelectedCount() + " / " + maxSelection);
        }

        if (btnConfirm != null) {
            if (maxSelection == 15) {
                btnConfirm.setText("CONFIRM SQUAD (" + adapter.getSelectedCount() + " / 15)");
            } else if (currentStep == 3) {
                btnConfirm.setText("START MATCH (" + adapter.getSelectedCount() + " / 11)");
            } else {
                btnConfirm.setText("CONFIRM PLAYING XI (" + adapter.getSelectedCount() + " / 11)");
            }
        }

        recyclerPlayers.setAdapter(
                adapter
        );
    }

    // ============================================================
    // GET SELECTED PLAYERS FROM ADAPTER
    // ============================================================

    private List<Player> getSelectedPlayers() {

        List<Player> selectedPlayers =
                new ArrayList<>();

        if (adapter == null) {

            return selectedPlayers;
        }

        List<Long> selectedIds =
                adapter.getSelectedIds();

        if (selectedIds == null
                || selectedIds.isEmpty()) {

            return selectedPlayers;
        }

        /*
         * Iterate through currentPlayers rather than
         * the HashSet order from the adapter.
         *
         * This keeps the player list in a stable,
         * database/list order.
         */

        for (Player player : currentPlayers) {

            if (player == null) {
                continue;
            }

            if (selectedIds.contains(
                    player.getId()
            )) {

                selectedPlayers.add(
                        player
                );
            }
        }

        return selectedPlayers;
    }

    // ============================================================
    // HANDLE NEXT
    // ============================================================

    private void handleNext() {

        List<Player> selected =
                getSelectedPlayers();

        switch (currentStep) {

            case 0:

                handleTeamASquad(
                        selected
                );

                break;

            case 1:

                handleTeamAXI(
                        selected
                );

                break;

            case 2:

                handleTeamBSquad(
                        selected
                );

                break;

            case 3:

                handleTeamBXI(
                        selected
                );

                break;
        }
    }

    // ============================================================
    // TEAM A SQUAD
    // ============================================================

    private void handleTeamASquad(
            List<Player> selected
    ) {

        if (!validateSquadSelection(
                selected,
                teamAName
        )) {

            return;
        }

        teamASquadIds.clear();

        for (Player player : selected) {

            teamASquadIds.add(
                    player.getId()
            );
        }

        teamAPlayers.clear();

        teamAPlayers.addAll(
                selected
        );

        /*
         * Move to Team A Playing XI.
         */

        currentStep = 1;

        loadStep();
    }

    // ============================================================
    // TEAM A PLAYING XI
    // ============================================================

    private void handleTeamAXI(
            List<Player> selected
    ) {

        if (!validateXISelection(
                selected,
                teamAName
        )) {

            return;
        }

        teamAXIIds.clear();

        for (Player player : selected) {

            teamAXIIds.add(
                    player.getId()
            );
        }

        teamAXI.clear();

        teamAXI.addAll(
                selected
        );

        /*
         * The four players not selected in the XI
         * automatically become substitutes.
         */

        teamASubstituteIds.clear();

        for (Player player : teamAPlayers) {

            if (player == null) {
                continue;
            }

            if (!teamAXIIds.contains(
                    player.getId()
            )) {

                teamASubstituteIds.add(
                        player.getId()
                );
            }
        }

        /*
         * Captain and vice captain must belong
         * to the Playing XI.
         */

        if (!selectCaptainAndViceCaptain(
                teamAPlayers,
                true
        )) {

            return;
        }

        /*
         * Move to Team B squad.
         */

        currentStep = 2;

        loadStep();
    }

    // ============================================================
    // TEAM B SQUAD
    // ============================================================

    private void handleTeamBSquad(
            List<Player> selected
    ) {

        if (!validateSquadSelection(
                selected,
                teamBName
        )) {

            return;
        }

        teamBSquadIds.clear();

        for (Player player : selected) {

            teamBSquadIds.add(
                    player.getId()
            );
        }

        teamBPlayers.clear();

        teamBPlayers.addAll(
                selected
        );

        /*
         * Move to Team B Playing XI.
         */

        currentStep = 3;

        loadStep();
    }

    // ============================================================
    // TEAM B PLAYING XI
    // ============================================================

    private void handleTeamBXI(
            List<Player> selected
    ) {

        if (!validateXISelection(
                selected,
                teamBName
        )) {

            return;
        }

        teamBXIIds.clear();

        for (Player player : selected) {

            teamBXIIds.add(
                    player.getId()
            );
        }

        teamBXI.clear();

        teamBXI.addAll(
                selected
        );

        /*
         * Remaining four players are substitutes.
         */

        teamBSubstituteIds.clear();

        for (Player player : teamBPlayers) {

            if (player == null) {
                continue;
            }

            if (!teamBXIIds.contains(
                    player.getId()
            )) {

                teamBSubstituteIds.add(
                        player.getId()
                );
            }
        }

        /*
         * Captain and vice captain.
         */

        if (!selectCaptainAndViceCaptain(
                teamBPlayers,
                false
        )) {

            return;
        }

        /*
         * Both teams are now completely configured.
         */

        finishSelection();
    }

    // ============================================================
    // VALIDATE 15 PLAYER SQUAD
    // ============================================================

    private boolean validateSquadSelection(
            List<Player> selected,
            String teamName
    ) {

        if (selected == null
                || selected.size() != 15) {

            Toast.makeText(
                    this,
                    "Select exactly 15 players for " +
                            teamName,
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        int wicketKeepers =
                countWicketKeepers(
                        selected
                );

        if (wicketKeepers != 2) {

            Toast.makeText(
                    this,
                    teamName +
                            " squad must contain exactly 2 wicketkeepers",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        return true;
    }

    // ============================================================
    // VALIDATE PLAYING XI
    // ============================================================

    private boolean validateXISelection(
            List<Player> selected,
            String teamName
    ) {

        if (selected == null
                || selected.size() != 11) {

            Toast.makeText(
                    this,
                    "Select exactly 11 players for " +
                            teamName,
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        int wicketKeepers =
                countWicketKeepers(
                        selected
                );

        if (wicketKeepers != 1) {

            Toast.makeText(
                    this,
                    teamName +
                            " Playing XI must contain exactly 1 wicketkeeper",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }

        /*
         * Make sure the selected XI really belongs
         * to the selected 15-player squad.
         */

        List<Player> allowedPlayers =
                teamName.equals(teamAName)
                        ? teamAPlayers
                        : teamBPlayers;

        for (Player player : selected) {

            if (player == null) {
                return false;
            }

            boolean belongsToSquad = false;

            for (Player squadPlayer : allowedPlayers) {

                if (squadPlayer != null
                        && squadPlayer.getId()
                        == player.getId()) {

                    belongsToSquad = true;

                    break;
                }
            }

            if (!belongsToSquad) {

                Toast.makeText(
                        this,
                        "Selected player is not part of the " +
                                teamName +
                                " squad",
                        Toast.LENGTH_LONG
                ).show();

                return false;
            }
        }

        return true;
    }

    // ============================================================
    // CAPTAIN / VICE CAPTAIN
    // ============================================================

    private boolean selectCaptainAndViceCaptain(
            List<Player> players,
            boolean teamA
    ) {

        if (players == null
                || players.size() != 15) {

            return false;
        }

        List<Player> xi =
                teamA
                        ? teamAXI
                        : teamBXI;

        if (xi.size() != 11) {

            return false;
        }

        /*
         * Current application flow automatically uses
         * the first two XI players.
         *
         * Both are guaranteed to be distinct and
         * members of the Playing XI.
         */

        Player captain =
                xi.get(0);

        Player viceCaptain =
                xi.get(1);

        if (captain == null
                || viceCaptain == null) {

            return false;
        }

        if (captain.getId()
                == viceCaptain.getId()) {

            return false;
        }

        if (teamA) {

            teamACaptainId =
                    captain.getId();

            teamAViceCaptainId =
                    viceCaptain.getId();

        } else {

            teamBCaptainId =
                    captain.getId();

            teamBViceCaptainId =
                    viceCaptain.getId();
        }

        return true;
    }

    // ============================================================
    // FINISH SELECTION
    // ============================================================

    private void finishSelection() {

        // --------------------------------------------------------
        // FINAL XI VALIDATION
        // --------------------------------------------------------

        if (teamAXI.size() != 11
                || teamBXI.size() != 11) {

            Toast.makeText(
                    this,
                    "Both teams must have exactly 11 players",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // --------------------------------------------------------
        // WICKETKEEPER VALIDATION
        // --------------------------------------------------------

        if (countWicketKeepers(teamAXI) != 1
                || countWicketKeepers(teamBXI) != 1) {

            Toast.makeText(
                    this,
                    "Each Playing XI must contain exactly 1 wicketkeeper",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // --------------------------------------------------------
        // SQUAD VALIDATION
        // --------------------------------------------------------

        if (teamASquadIds.size() != 15
                || teamBSquadIds.size() != 15) {

            Toast.makeText(
                    this,
                    "Both teams must have exactly 15 players",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // --------------------------------------------------------
        // SUBSTITUTE VALIDATION
        // --------------------------------------------------------

        if (teamASubstituteIds.size() != 4
                || teamBSubstituteIds.size() != 4) {

            Toast.makeText(
                    this,
                    "Each team must have exactly 4 substitutes",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // --------------------------------------------------------
        // CREATE MATCH
        // --------------------------------------------------------

        MatchInfo match =
                new MatchInfo();

        // --------------------------------------------------------
        // TEAMS
        // --------------------------------------------------------

        match.setTeamAId(
                teamAId
        );

        match.setTeamBId(
                teamBId
        );

        match.setTeamAName(
                teamAName
        );

        match.setTeamBName(
                teamBName
        );

        // --------------------------------------------------------
        // FORMAT
        // --------------------------------------------------------

        match.setMatchFormat(
                matchFormat
        );

        // Limited-overs only: exactly one innings per team.
        match.setTotalOvers(
                totalOvers
        );

        match.setInningsPerTeam(1);

        // --------------------------------------------------------
        // TOSS
        // --------------------------------------------------------

        match.setTossWinnerId(
                tossWinnerId
        );

        match.setTossDecision(
                tossDecision
        );

        match.setBattingTeamId(
                battingTeamId
        );

        match.setBowlingTeamId(
                bowlingTeamId
        );

        // --------------------------------------------------------
        // INITIAL MATCH STATE
        // --------------------------------------------------------

        match.setCurrentInnings(
                1
        );

        match.setCurrentDay(
                1
        );

        match.setCurrentSession(
                1
        );

        match.setDeclared(
                false
        );

        match.setFollowOn(
                false
        );

        // --------------------------------------------------------
        // MATCH DATE
        // --------------------------------------------------------

        /*
         * MatchInfo.setMatchDate() expects String,
         * not long.
         */

        String matchDate =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(
                        new Date()
                );

        match.setMatchDate(
                matchDate
        );

        // --------------------------------------------------------
        // SAVE MATCH
        // --------------------------------------------------------

        long matchId =
                db.createMatch(
                        match
                );

        if (matchId <= 0) {

            Toast.makeText(
                    this,
                    "Failed to create match",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ========================================================
        // OPEN LIVE SCORING
        // ========================================================

        Intent intent =
                new Intent(
                        this,
                        LiveScoringActivity.class
                );

        // --------------------------------------------------------
        // MATCH
        // --------------------------------------------------------

        intent.putExtra(
                "match_id",
                matchId
        );

        // --------------------------------------------------------
        // TEAMS
        // --------------------------------------------------------

        intent.putExtra(
                "team_a_id",
                teamAId
        );

        intent.putExtra(
                "team_b_id",
                teamBId
        );

        // --------------------------------------------------------
        // BATTING XI
        // --------------------------------------------------------

        intent.putExtra(
                "batting_xi",
                teamAXIIdsForBatting()
        );

        // --------------------------------------------------------
        // BOWLING XI
        // --------------------------------------------------------

        intent.putExtra(
                "bowling_xi",
                teamBXIIdsForBowling()
        );

        // --------------------------------------------------------
        // BATTING SUBSTITUTES
        // --------------------------------------------------------

        intent.putExtra(
                "batting_subs",
                battingTeamId == teamAId
                        ? new ArrayList<>(
                        teamASubstituteIds
                )
                        : new ArrayList<>(
                        teamBSubstituteIds
                )
        );

        // --------------------------------------------------------
        // BOWLING SUBSTITUTES
        // --------------------------------------------------------

        intent.putExtra(
                "bowling_subs",
                battingTeamId == teamAId
                        ? new ArrayList<>(
                        teamBSubstituteIds
                )
                        : new ArrayList<>(
                        teamASubstituteIds
                )
        );

        // --------------------------------------------------------
        // FORMAT
        // --------------------------------------------------------

        intent.putExtra(
                "match_format",
                matchFormat
        );

        intent.putExtra(
                "total_overs",
                totalOvers
        );

        intent.putExtra(
                "innings_per_team",
                1
        );

        // --------------------------------------------------------
        // BATTING / BOWLING TEAM
        // --------------------------------------------------------

        intent.putExtra(
                "batting_team_id",
                battingTeamId
        );

        intent.putExtra(
                "bowling_team_id",
                bowlingTeamId
        );

        // --------------------------------------------------------
        // TOSS
        // --------------------------------------------------------

        intent.putExtra(
                "toss_winner_id",
                tossWinnerId
        );

        intent.putExtra(
                "toss_decision",
                tossDecision
        );

        // --------------------------------------------------------
        // CAPTAIN / VICE CAPTAIN
        // --------------------------------------------------------

        intent.putExtra(
                "team_a_captain_id",
                teamACaptainId
        );

        intent.putExtra(
                "team_a_vice_captain_id",
                teamAViceCaptainId
        );

        intent.putExtra(
                "team_b_captain_id",
                teamBCaptainId
        );

        intent.putExtra(
                "team_b_vice_captain_id",
                teamBViceCaptainId
        );

        // --------------------------------------------------------
        // START LIVE SCORING
        // --------------------------------------------------------

        startActivity(
                intent
        );

        finish();
    }

    // ============================================================
    // BATTING XI IDS
    // ============================================================

    private ArrayList<Long> teamAXIIdsForBatting() {

        if (battingTeamId == teamAId) {

            return new ArrayList<>(
                    teamAXIIds
            );
        }

        return new ArrayList<>(
                teamBXIIds
        );
    }

    // ============================================================
    // BOWLING XI IDS
    // ============================================================

    private ArrayList<Long> teamBXIIdsForBowling() {

        if (bowlingTeamId == teamBId) {

            return new ArrayList<>(
                    teamBXIIds
            );
        }

        return new ArrayList<>(
                teamAXIIds
        );
    }

    private void preselectSquad(List<Player> players, ArrayList<Long> squadIds) {
        if (!squadIds.isEmpty()) return;
        List<Player> wks = new ArrayList<>();
        List<Player> others = new ArrayList<>();
        for (Player p : players) {
            if (p != null) {
                if (p.isWicketKeeper()) wks.add(p);
                else others.add(p);
            }
        }
        for (Player p : wks) {
            if (squadIds.size() < 15) squadIds.add(p.getId());
        }
        for (Player p : others) {
            if (squadIds.size() < 15) squadIds.add(p.getId());
        }
    }

    private void preselectXI(List<Player> squadPlayers, ArrayList<Long> xiIds) {
        if (!xiIds.isEmpty()) return;
        List<Player> wks = new ArrayList<>();
        List<Player> others = new ArrayList<>();
        for (Player p : squadPlayers) {
            if (p != null) {
                if (p.isWicketKeeper()) wks.add(p);
                else others.add(p);
            }
        }
        for (Player p : wks) {
            if (xiIds.size() < 11) xiIds.add(p.getId());
        }
        for (Player p : others) {
            if (xiIds.size() < 11) xiIds.add(p.getId());
        }
    }
}