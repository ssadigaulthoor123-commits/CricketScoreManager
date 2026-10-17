package com.example.cricketscorer.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.adapters.TeamAdapter;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.Team;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TeamListActivity extends AppCompatActivity
        implements TeamAdapter.OnTeamActionListener {

    private DatabaseHelper db;

    private RecyclerView recyclerTeams;
    private View emptyState;
    private FloatingActionButton fabAddTeam;

    private TeamAdapter adapter;

    private final List<Team> teamList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_team_list);

        db = DatabaseHelper.getInstance(this);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        loadTeams();
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        recyclerTeams = findViewById(R.id.recyclerTeams);
        emptyState = findViewById(R.id.emptyState);
        fabAddTeam = findViewById(R.id.fabAddTeam);

        if (recyclerTeams == null) {
            Toast.makeText(
                    this,
                    "Unable to load team list",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }

    // ============================================================
    // TOOLBAR
    // ============================================================

    private void setupToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar == null) {
            return;
        }

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Teams");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }

    // ============================================================
    // RECYCLER VIEW
    // ============================================================

    private void setupRecyclerView() {

        recyclerTeams.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerTeams.setHasFixedSize(true);

        adapter = new TeamAdapter(
                teamList,
                this
        );

        recyclerTeams.setAdapter(adapter);
    }

    // ============================================================
    // LISTENERS
    // ============================================================

    private void setupListeners() {

        if (fabAddTeam != null) {

            fabAddTeam.setOnClickListener(
                    v -> openAddTeam()
            );
        }
    }

    // ============================================================
    // LOAD TEAMS
    // ============================================================

    private void loadTeams() {

        List<Team> teams = db.getAllTeams();

        teamList.clear();

        if (teams != null) {
            teamList.addAll(teams);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        updateEmptyState();
    }

    // ============================================================
    // EMPTY STATE
    // ============================================================

    private void updateEmptyState() {

        boolean isEmpty = teamList.isEmpty();

        if (recyclerTeams != null) {
            recyclerTeams.setVisibility(
                    isEmpty
                            ? View.GONE
                            : View.VISIBLE
            );
        }

        if (emptyState != null) {
            emptyState.setVisibility(
                    isEmpty
                            ? View.VISIBLE
                            : View.GONE
            );
        }
    }

    // ============================================================
    // ADD TEAM
    // ============================================================

    private void openAddTeam() {

        Intent intent =
                new Intent(
                        this,
                        AddEditTeamActivity.class
                );

        startActivityForResult(
                intent,
                100
        );
    }

    // ============================================================
    // EDIT TEAM
    // ============================================================

    @Override
    public void onEditTeam(Team team) {

        if (team == null || team.getId() <= 0) {
            return;
        }

        Intent intent =
                new Intent(
                        this,
                        AddEditTeamActivity.class
                );

        intent.putExtra(
                "team_id",
                team.getId()
        );

        startActivityForResult(
                intent,
                101
        );
    }

    // ============================================================
    // DELETE TEAM
    // ============================================================

    @Override
    public void onDeleteTeam(Team team) {

        if (team == null || team.getId() <= 0) {
            return;
        }

        showDeleteConfirmation(team);
    }

    private void showDeleteConfirmation(Team team) {

        String teamName = team.getName();

        new AlertDialog.Builder(this)
                .setTitle("Delete Team")
                .setMessage(
                        "Delete \"" +
                                teamName +
                                "\" and its players?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteTeam(team)
                )
                .show();
    }

    private void deleteTeam(Team team) {

        try {

            db.deleteTeam(
                    team.getId()
            );

            Toast.makeText(
                    this,
                    "Team deleted",
                    Toast.LENGTH_SHORT
            ).show();

            loadTeams();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to delete team",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // REFRESH WHEN RETURNING
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (db != null && adapter != null) {
            loadTeams();
        }
    }

    // ============================================================
    // ACTIVITY RESULT
    // ============================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (resultCode == RESULT_OK) {
            loadTeams();
        }
    }
}