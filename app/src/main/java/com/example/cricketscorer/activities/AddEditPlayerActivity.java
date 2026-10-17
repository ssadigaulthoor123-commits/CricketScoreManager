package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cricketscorer.R;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.models.Team;

import java.util.ArrayList;
import java.util.List;

public class AddEditPlayerActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etJerseyNumber;

    private Spinner spinnerTeam;
    private Spinner spinnerRole;
    private Spinner spinnerBattingHand;
    private Spinner spinnerBowlingStyle;

    private Button btnSave;

    private DatabaseHelper db;

    private long playerId = -1;

    private long preselectedTeamId = -1;

    private boolean teamLocked = false;

    // ============================================================
    // PLAYER ROLES
    // ============================================================

    private final String[] roles = {
            "Batsman",
            "Bowler",
            "All-Rounder",
            "Wicket Keeper"
    };

    // ============================================================
    // BATTING HAND
    // ============================================================

    private final String[] battingHands = {
            "Right Hand",
            "Left Hand"
    };

    // ============================================================
    // BOWLING STYLES
    // ============================================================

    private final String[] bowlingStyles = {
            "None",
            "Right-arm Fast",
            "Left-arm Fast",
            "Right-arm Medium Fast",
            "Left-arm Medium Fast",
            "Right-arm Medium",
            "Left-arm Medium",
            "Right-arm Off Spin",
            "Left-arm Off Spin",
            "Right-arm Leg Spin",
            "Left-arm Leg Spin",
            "Right-arm Spinner",
            "Left-arm Spinner"
    };

    private final List<Team> teams =
            new ArrayList<>();

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_add_edit_player
        );

        db =
                DatabaseHelper.getInstance(this);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        readIntentData();

        initializeViews();

        setupSpinners();

        loadTeams();

        if (playerId != -1) {

            loadPlayer();

        } else {

            setDefaultValues();
        }

        setupListeners();
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

        playerId =
                intent.getLongExtra(
                        "player_id",
                        -1
                );

        preselectedTeamId =
                intent.getLongExtra(
                        "team_id",
                        intent.getLongExtra(
                                "teamId",
                                -1
                        )
                );

        teamLocked =
                intent.getBooleanExtra(
                        "team_locked",
                        false
                );
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        etName =
                findViewById(
                        R.id.etPlayerName
                );

        etJerseyNumber =
                findViewById(
                        R.id.etJerseyNumber
                );

        spinnerTeam =
                findViewById(
                        R.id.spinnerTeam
                );

        spinnerRole =
                findViewById(
                        R.id.spinnerRole
                );

        spinnerBattingHand =
                findViewById(
                        R.id.spinnerBattingHand
                );

        spinnerBowlingStyle =
                findViewById(
                        R.id.spinnerBowlingStyle
                );

        btnSave =
                findViewById(
                        R.id.btnSave
                );
    }

    // ============================================================
    // SETUP SPINNERS
    // ============================================================

    private void setupSpinners() {

        ArrayAdapter<String> roleAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        roles
                );

        roleAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerRole.setAdapter(
                roleAdapter
        );

        ArrayAdapter<String> battingAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        battingHands
                );

        battingAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerBattingHand.setAdapter(
                battingAdapter
        );

        ArrayAdapter<String> bowlingAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        bowlingStyles
                );

        bowlingAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerBowlingStyle.setAdapter(
                bowlingAdapter
        );
    }

    // ============================================================
    // LOAD TEAMS
    // ============================================================

    private void loadTeams() {

        teams.clear();

        List<Team> loadedTeams =
                db.getAllTeams();

        if (loadedTeams != null) {

            teams.addAll(
                    loadedTeams
            );
        }

        List<String> teamNames =
                new ArrayList<>();

        for (Team team : teams) {

            if (team != null) {

                teamNames.add(
                        team.getName()
                );
            }
        }

        ArrayAdapter<String> teamAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        teamNames
                );

        teamAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTeam.setAdapter(
                teamAdapter
        );

        if (preselectedTeamId != -1) {

            selectTeam(
                    preselectedTeamId
            );
        }

        spinnerTeam.setEnabled(
                !teamLocked
        );
    }

    // ============================================================
    // SELECT TEAM
    // ============================================================

    private void selectTeam(
            long teamId
    ) {

        for (int i = 0;
             i < teams.size();
             i++) {

            Team team =
                    teams.get(i);

            if (team != null &&
                    team.getId() == teamId) {

                spinnerTeam.setSelection(
                        i
                );

                return;
            }
        }
    }

    // ============================================================
    // DEFAULT VALUES
    // ============================================================

    private void setDefaultValues() {

        spinnerRole.setSelection(0);

        spinnerBattingHand.setSelection(0);

        spinnerBowlingStyle.setSelection(0);

        if (preselectedTeamId != -1) {

            selectTeam(
                    preselectedTeamId
            );
        }

        spinnerBowlingStyle.setEnabled(
                true
        );
    }

    // ============================================================
    // LOAD EXISTING PLAYER
    // ============================================================

    private void loadPlayer() {

        Player player =
                db.getPlayerById(
                        playerId
                );

        if (player == null) {

            Toast.makeText(
                    this,
                    "Player not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        etName.setText(
                player.getName()
        );

        if (player.getJerseyNumber() > 0) {

            etJerseyNumber.setText(
                    String.valueOf(
                            player.getJerseyNumber()
                    )
            );
        }

        selectTeam(
                player.getTeamId()
        );

        selectSpinnerValue(
                spinnerRole,
                roles,
                player.getRole()
        );

        selectSpinnerValue(
                spinnerBattingHand,
                battingHands,
                player.getBattingHand()
        );

        selectSpinnerValue(
                spinnerBowlingStyle,
                bowlingStyles,
                player.getBowlingStyle()
        );
    }

    // ============================================================
    // SELECT SPINNER VALUE
    // ============================================================

    private void selectSpinnerValue(
            Spinner spinner,
            String[] values,
            String value
    ) {

        if (spinner == null ||
                values == null ||
                value == null) {

            return;
        }

        for (int i = 0;
             i < values.length;
             i++) {

            if (values[i].equalsIgnoreCase(
                    value.trim()
            )) {

                spinner.setSelection(
                        i
                );

                return;
            }
        }
    }

    // ============================================================
    // LISTENERS
    // ============================================================

    private void setupListeners() {

        if (btnSave != null) {

            btnSave.setOnClickListener(
                    v -> savePlayer()
            );
        }

        spinnerRole.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {

                        if (position < 0 ||
                                position >= roles.length) {

                            return;
                        }

                        String role =
                                roles[position];

                        if (isWicketKeeperRole(
                                role
                        )) {

                            spinnerBowlingStyle.setSelection(
                                    0
                            );

                            spinnerBowlingStyle.setEnabled(
                                    false
                            );

                        } else {

                            spinnerBowlingStyle.setEnabled(
                                    true
                            );
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );
    }

    // ============================================================
    // WICKET KEEPER ROLE CHECK
    // ============================================================

    private boolean isWicketKeeperRole(
            String role
    ) {

        if (role == null) {
            return false;
        }

        String normalized =
                role.trim()
                        .toLowerCase();

        return normalized.equals(
                "wicket keeper"
        )
                ||
                normalized.equals(
                        "wicket-keeper"
                )
                ||
                normalized.equals(
                        "wicketkeeper"
                )
                ||
                normalized.equals(
                        "wk"
                )
                ||
                normalized.equals(
                        "w/k"
                );
    }

    // ============================================================
    // BOWLING ROLE CHECK
    // ============================================================

    private boolean requiresBowlingStyle(
            String role
    ) {

        if (role == null) {
            return false;
        }

        String normalized =
                role.trim()
                        .toLowerCase();

        return normalized.equals(
                "bowler"
        )
                ||
                normalized.equals(
                        "all-rounder"
                )
                ||
                normalized.equals(
                        "all rounder"
                )
                ||
                normalized.equals(
                        "allrounder"
                );
    }

    // ============================================================
    // VALID BOWLING STYLE CHECK
    // ============================================================

    private boolean hasValidBowlingStyle(
            String bowlingStyle
    ) {

        if (bowlingStyle == null) {
            return false;
        }

        String normalized =
                bowlingStyle.trim();

        if (normalized.isEmpty()) {
            return false;
        }

        return !normalized.equalsIgnoreCase(
                "None"
        );
    }

    // ============================================================
    // SAVE PLAYER
    // ============================================================

    private void savePlayer() {

        String name =
                etName.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(
                name
        )) {

            etName.setError(
                    "Enter player name"
            );

            etName.requestFocus();

            return;
        }

        if (teams.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please create a team first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int teamPosition =
                spinnerTeam.getSelectedItemPosition();

        if (teamPosition < 0 ||
                teamPosition >= teams.size()) {

            Toast.makeText(
                    this,
                    "Please select a team",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Team selectedTeam =
                teams.get(
                        teamPosition
                );

        if (selectedTeam == null) {

            Toast.makeText(
                    this,
                    "Invalid team selection",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String jerseyText =
                etJerseyNumber.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(jerseyText)) {

            etJerseyNumber.setError(
                    "Enter jersey number"
            );

            etJerseyNumber.requestFocus();

            return;
        }

        int jerseyNumber = 0;

        try {

            jerseyNumber =
                    Integer.parseInt(
                            jerseyText
                    );

        } catch (NumberFormatException e) {

            etJerseyNumber.setError(
                    "Enter a valid jersey number"
            );

            etJerseyNumber.requestFocus();

            return;
        }

        if (jerseyNumber < 0 ||
                jerseyNumber > 999) {

            etJerseyNumber.setError(
                    "Jersey number must be between 0 and 999"
            );

            etJerseyNumber.requestFocus();

            return;
        }

        List<Player> teamPlayers = db.getPlayersByTeam(selectedTeam.getId());
        if (teamPlayers != null) {
            for (Player existing : teamPlayers) {
                if (existing == null || existing.getId() == playerId) {
                    continue;
                }
                if (existing.getName() != null && existing.getName().trim().equalsIgnoreCase(name)) {
                    etName.setError("A player with this name already exists in this team");
                    etName.requestFocus();
                    Toast.makeText(this, "A player with this name already exists in this team", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (existing.getJerseyNumber() == jerseyNumber) {
                    etJerseyNumber.setError("A player with jersey number #" + jerseyNumber + " already exists in this team");
                    etJerseyNumber.requestFocus();
                    Toast.makeText(this, "A player with jersey number #" + jerseyNumber + " already exists in this team", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        String role =
                spinnerRole.getSelectedItem() == null
                        ? ""
                        : spinnerRole
                        .getSelectedItem()
                        .toString()
                        .trim();

        if (role.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select a player role",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String battingHand =
                spinnerBattingHand.getSelectedItem() == null
                        ? "Right Hand"
                        : spinnerBattingHand
                        .getSelectedItem()
                        .toString();

        String bowlingStyle =
                spinnerBowlingStyle.getSelectedItem() == null
                        ? "None"
                        : spinnerBowlingStyle
                        .getSelectedItem()
                        .toString()
                        .trim();

        if (isWicketKeeperRole(
                role
        )) {

            bowlingStyle =
                    "None";
        }

        if (requiresBowlingStyle(
                role
        )) {

            if (!hasValidBowlingStyle(
                    bowlingStyle
            )) {

                Toast.makeText(
                        this,
                        "Bowling style is required for "
                                + role,
                        Toast.LENGTH_LONG
                ).show();

                spinnerBowlingStyle.requestFocus();

                return;
            }
        }

        Player player;

        if (playerId != -1) {

            player =
                    db.getPlayerById(
                            playerId
                    );

            if (player == null) {

                Toast.makeText(
                        this,
                        "Unable to load player",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

        } else {

            player =
                    new Player();
        }

        player.setId(
                playerId
        );

        player.setName(
                name
        );

        player.setJerseyNumber(
                jerseyNumber
        );

        player.setTeamId(
                selectedTeam.getId()
        );

        player.setRole(
                role
        );

        player.setBattingHand(
                battingHand
        );

        player.setBowlingStyle(
                bowlingStyle
        );

        if (playerId == -1) {

            long result =
                    db.addPlayer(
                            player
                    );

            if (result <= 0) {

                Toast.makeText(
                        this,
                        "Unable to add player",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "Player added",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            db.updatePlayer(
                    player
            );

            Toast.makeText(
                    this,
                    "Player updated",
                    Toast.LENGTH_SHORT
            ).show();
        }

        finish();
    }
}
