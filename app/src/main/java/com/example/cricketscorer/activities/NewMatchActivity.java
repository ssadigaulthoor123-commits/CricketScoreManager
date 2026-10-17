package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cricketscorer.R;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.Team;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class NewMatchActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private Spinner spinnerTeamA;
    private Spinner spinnerTeamB;
    private Spinner spinnerTossWinner;
    private Spinner spinnerFormat;

    private TextInputLayout layoutOvers;
    private TextInputEditText etOvers;

    private RadioGroup radioDecision;

    private MaterialButton btnProceed;

    private List<Team> teams;

    private static final String FORMAT_T20 = "T20";
    private static final String FORMAT_ODI = "ODI";
    private static final String FORMAT_CUSTOM = "Custom";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_match);

        db = DatabaseHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);

        spinnerTeamA = findViewById(R.id.spinnerTeamA);
        spinnerTeamB = findViewById(R.id.spinnerTeamB);
        spinnerTossWinner = findViewById(R.id.spinnerTossWinner);
        spinnerFormat = findViewById(R.id.spinnerFormat);

        layoutOvers = findViewById(R.id.layoutOvers);
        etOvers = findViewById(R.id.etOvers);

        radioDecision = findViewById(R.id.radioDecision);

        btnProceed = findViewById(R.id.btnProceed);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("New Match");
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        layoutOvers.setVisibility(View.VISIBLE);

        etOvers.setText("20");
        etOvers.setEnabled(false);

        loadTeams();

        setupFormatSpinner();

        setupTeamListeners();

        btnProceed.setOnClickListener(v -> startMatchSetup());
    }

    private void loadTeams() {

        teams = db.getAllTeams();

        if (teams == null) {
            teams = new ArrayList<>();
        }

        if (teams.size() < 2) {

            Toast.makeText(
                    this,
                    "Create at least 2 teams before starting a match",
                    Toast.LENGTH_LONG
            ).show();

            btnProceed.setEnabled(false);

            return;
        }

        ArrayAdapter<Team> teamAAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        teams
                );

        teamAAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTeamA.setAdapter(teamAAdapter);

        ArrayAdapter<Team> teamBAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        teams
                );

        teamBAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTeamB.setAdapter(teamBAdapter);

        if (teams.size() > 1) {
            spinnerTeamB.setSelection(1);
        }

        updateTossWinner();
    }

    private void setupFormatSpinner() {

        List<String> formats = new ArrayList<>();

        formats.add(FORMAT_T20);
        formats.add(FORMAT_ODI);
        formats.add(FORMAT_CUSTOM);

        ArrayAdapter<String> formatAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        formats
                );

        formatAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerFormat.setAdapter(formatAdapter);

        spinnerFormat.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        Object selectedItem =
                                parent.getItemAtPosition(position);

                        if (selectedItem == null) {
                            handleFormatChange(FORMAT_T20);
                            return;
                        }

                        handleFormatChange(
                                selectedItem.toString()
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {

                        handleFormatChange(FORMAT_T20);
                    }
                }
        );
    }

    private void handleFormatChange(String format) {

        if (format == null) {
            format = FORMAT_T20;
        }

        if (FORMAT_T20.equalsIgnoreCase(format)) {

            layoutOvers.setVisibility(View.VISIBLE);
            layoutOvers.setHint("Number of overs");
            etOvers.setText("20");
            etOvers.setEnabled(false);
            return;
        }

        if (FORMAT_ODI.equalsIgnoreCase(format)) {

            layoutOvers.setVisibility(View.VISIBLE);
            layoutOvers.setHint("Number of overs");
            etOvers.setText("50");
            etOvers.setEnabled(false);
            return;
        }

        if (FORMAT_CUSTOM.equalsIgnoreCase(format)) {

            layoutOvers.setVisibility(View.VISIBLE);
            layoutOvers.setHint("Enter number of overs");
            etOvers.setText("");
            etOvers.setEnabled(true);
        }
    }

    private void setupTeamListeners() {

        spinnerTeamA.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        updateTossWinner();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );

        spinnerTeamB.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        updateTossWinner();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    private void updateTossWinner() {

        if (teams == null || teams.size() < 2) {
            return;
        }

        Team teamA =
                (Team) spinnerTeamA.getSelectedItem();

        Team teamB =
                (Team) spinnerTeamB.getSelectedItem();

        if (teamA == null || teamB == null) {
            return;
        }

        List<Team> tossTeams =
                new ArrayList<>();

        tossTeams.add(teamA);

        if (teamB.getId() != teamA.getId()) {
            tossTeams.add(teamB);
        }

        ArrayAdapter<Team> tossAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        tossTeams
                );

        tossAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTossWinner.setAdapter(tossAdapter);
    }

    private void startMatchSetup() {

        Team teamA =
                (Team) spinnerTeamA.getSelectedItem();

        Team teamB =
                (Team) spinnerTeamB.getSelectedItem();

        if (teamA == null || teamB == null) {

            Toast.makeText(
                    this,
                    "Please select both teams",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (teamA.getId() == teamB.getId()) {

            Toast.makeText(
                    this,
                    "Team A and Team B must be different",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Object selectedFormat =
                spinnerFormat.getSelectedItem();

        String format =
                selectedFormat == null
                        ? FORMAT_T20
                        : selectedFormat.toString();

        int totalOvers;

        String oversText =
                etOvers.getText()
                        .toString()
                        .trim();

        if (oversText.isEmpty()) {

            etOvers.setError(
                    "Enter number of overs"
            );

            etOvers.requestFocus();

            return;
        }

        try {

            totalOvers =
                    Integer.parseInt(oversText);

        } catch (NumberFormatException e) {

            etOvers.setError(
                    "Enter a valid number"
            );

            etOvers.requestFocus();

            return;
        }

        if (totalOvers < 1 || totalOvers > 999) {

            etOvers.setError(
                    "Overs must be between 1 and 999"
            );

            etOvers.requestFocus();

            return;
        }

        Team tossWinner =
                (Team) spinnerTossWinner.getSelectedItem();

        if (tossWinner == null) {

            Toast.makeText(
                    this,
                    "Please select toss winner",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int checkedId =
                radioDecision.getCheckedRadioButtonId();

        String tossDecision;

        if (checkedId == R.id.radioBowl) {
            tossDecision = "Bowl";
        } else {
            tossDecision = "Bat";
        }

        long battingTeamId;
        long bowlingTeamId;

        if ("Bat".equalsIgnoreCase(tossDecision)) {

            battingTeamId =
                    tossWinner.getId();

            if (tossWinner.getId() == teamA.getId()) {
                bowlingTeamId = teamB.getId();
            } else {
                bowlingTeamId = teamA.getId();
            }

        } else {

            bowlingTeamId =
                    tossWinner.getId();

            if (tossWinner.getId() == teamA.getId()) {
                battingTeamId = teamB.getId();
            } else {
                battingTeamId = teamA.getId();
            }
        }

        List<com.example.cricketscorer.models.Player> teamAPlayers =
                db.getPlayersByTeam(teamA.getId());

        if (teamAPlayers == null || teamAPlayers.size() < 15) {

            Toast.makeText(
                    this,
                    teamA.getName()
                            + " must have at least 15 players",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        List<com.example.cricketscorer.models.Player> teamBPlayers =
                db.getPlayersByTeam(teamB.getId());

        if (teamBPlayers == null || teamBPlayers.size() < 15) {

            Toast.makeText(
                    this,
                    teamB.getName()
                            + " must have at least 15 players",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int inningsPerTeam = 1;

        Intent intent =
                new Intent(
                        NewMatchActivity.this,
                        SelectPlayingXIActivity.class
                );

        intent.putExtra(
                "team_a_id",
                teamA.getId()
        );

        intent.putExtra(
                "team_b_id",
                teamB.getId()
        );

        intent.putExtra(
                "team_a_name",
                teamA.getName()
        );

        intent.putExtra(
                "team_b_name",
                teamB.getName()
        );

        intent.putExtra(
                "match_format",
                format
        );

        intent.putExtra(
                "total_overs",
                totalOvers
        );

        intent.putExtra(
                "innings_per_team",
                inningsPerTeam
        );

        intent.putExtra(
                "toss_winner_id",
                tossWinner.getId()
        );

        intent.putExtra(
                "toss_decision",
                tossDecision
        );

        intent.putExtra(
                "batting_team_id",
                battingTeamId
        );

        intent.putExtra(
                "bowling_team_id",
                bowlingTeamId
        );

        startActivity(intent);
    }

    @Override
    protected void onDestroy() {

        /*
         * DatabaseHelper is a singleton.
         * Do not close it here.
         */

        super.onDestroy();
    }
}