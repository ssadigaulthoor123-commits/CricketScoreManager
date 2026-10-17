package com.example.cricketscorer.activities;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cricketscorer.R;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.Team;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditTeamActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private TextInputEditText etTeamName;
    private TextInputEditText etTeamShort;
    private MaterialButton btnSaveTeam;

    private long teamId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_edit_team);

        // ========================================================
        // DATABASE
        // ========================================================

        db = DatabaseHelper.getInstance(this);

        // ========================================================
        // VIEW REFERENCES
        // ========================================================

        Toolbar toolbar = findViewById(R.id.toolbar);

        etTeamName = findViewById(R.id.etTeamName);
        etTeamShort = findViewById(R.id.etTeamShort);
        btnSaveTeam = findViewById(R.id.btnSaveTeam);

        // ========================================================
        // SHORT NAME FILTER
        // ========================================================

        etTeamShort.setFilters(
                new InputFilter[]{
                        new InputFilter.LengthFilter(5)
                }
        );

        // ========================================================
        // TOOLBAR
        // ========================================================

        if (toolbar != null) {

            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {

                getSupportActionBar()
                        .setDisplayHomeAsUpEnabled(true);

                getSupportActionBar()
                        .setTitle("Add Team");
            }

            toolbar.setNavigationOnClickListener(
                    v -> finish()
            );
        }

        // ========================================================
        // CHECK EDIT MODE
        // ========================================================

        teamId = getIntent().getLongExtra(
                "team_id",
                -1
        );

        if (teamId > 0) {

            loadTeamForEditing();
        }

        // ========================================================
        // SAVE BUTTON
        // ========================================================

        btnSaveTeam.setOnClickListener(
                v -> saveTeam()
        );
    }

    // ============================================================
    // LOAD TEAM FOR EDITING
    // ============================================================

    private void loadTeamForEditing() {

        Team team =
                db.getTeamById(teamId);

        if (team == null) {

            Toast.makeText(
                    this,
                    "Team not found",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }

        etTeamName.setText(
                team.getName()
        );

        etTeamShort.setText(
                team.getShortName()
        );

        if (getSupportActionBar() != null) {

            getSupportActionBar()
                    .setTitle("Edit Team");
        }
    }

    // ============================================================
    // SAVE TEAM
    // ============================================================

    private void saveTeam() {

        String teamName =
                etTeamName.getText()
                        .toString()
                        .trim();

        String shortName =
                etTeamShort.getText()
                        .toString()
                        .trim()
                        .toUpperCase();

        // ========================================================
        // TEAM NAME VALIDATION
        // ========================================================

        if (TextUtils.isEmpty(teamName)) {

            etTeamName.setError(
                    "Enter team name"
            );

            etTeamName.requestFocus();

            return;
        }

        // ========================================================
        // SHORT NAME VALIDATION
        // ========================================================

        if (TextUtils.isEmpty(shortName)) {

            etTeamShort.setError(
                    "Enter short name"
            );

            etTeamShort.requestFocus();

            return;
        }

        // ========================================================
        // SHORT NAME CHARACTER VALIDATION
        // ========================================================

        if (!shortName.matches("[A-Z0-9]+")) {

            etTeamShort.setError(
                    "Use only letters or numbers"
            );

            etTeamShort.requestFocus();

            return;
        }

        // ========================================================
        // SHORT NAME LENGTH
        // ========================================================

        if (shortName.length() > 5) {

            etTeamShort.setError(
                    "Maximum 5 characters"
            );

            etTeamShort.requestFocus();

            return;
        }

        // ========================================================
        // CREATE TEAM MODEL
        // ========================================================

        Team team =
                new Team();

        team.setName(teamName);

        team.setShortName(shortName);

        // ========================================================
        // UPDATE EXISTING TEAM
        // ========================================================

        if (teamId > 0) {

            team.setId(teamId);

            db.updateTeam(team);

            Toast.makeText(
                    this,
                    "Team updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

        }

        // ========================================================
        // CREATE NEW TEAM
        // ========================================================

        else {

            long newId =
                    db.addTeam(team);

            if (newId <= 0) {

                Toast.makeText(
                        this,
                        "Unable to save team",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "Team created successfully",
                    Toast.LENGTH_SHORT
            ).show();
        }

        // ========================================================
        // RETURN TO TEAM LIST
        // ========================================================

        setResult(
                RESULT_OK
        );

        finish();
    }
}