package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.adapters.ContinueMatchAdapter;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.MatchInfo;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    private CardView tileTeams;
    private CardView tilePlayers;
    private CardView tileHistory;
    private CardView tileStats;

    private CardView cardNewMatch;

    private RecyclerView recyclerContinueMatches;
    private TextView tvContinueHeader;
    private ContinueMatchAdapter continueMatchAdapter;

    private TextView tvStatTotalTeams;
    private TextView tvStatTotalPlayers;
    private TextView tvStatTotalMatches;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_dashboard
        );

        db =
                DatabaseHelper.getInstance(this);

        com.example.cricketscorer.utils.NotificationHelper.checkAndRequestPermission(this);

        initializeViews();

        setupToolbar();

        setupTileContent();

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPausedMatches();
        loadQuickStats();
    }

    private void initializeViews() {

        toolbar =
                findViewById(
                        R.id.toolbar
                );

        drawerLayout =
                findViewById(
                        R.id.drawerLayout
                );

        navView =
                findViewById(
                        R.id.navView
                );

        cardNewMatch =
                findViewById(
                        R.id.cardNewMatch
                );

        tileTeams =
                findViewById(
                        R.id.tileTeams
                );

        tilePlayers =
                findViewById(
                        R.id.tilePlayers
                );

        tileHistory =
                findViewById(
                        R.id.tileHistory
                );

        tileStats =
                findViewById(
                        R.id.tileStats
                );

        recyclerContinueMatches =
                findViewById(
                        R.id.recyclerContinueMatches
                );

        tvContinueHeader =
                findViewById(
                        R.id.tvContinueHeader
                );

        tvStatTotalTeams =
                findViewById(
                        R.id.tvStatTotalTeams
                );

        tvStatTotalPlayers =
                findViewById(
                        R.id.tvStatTotalPlayers
                );

        tvStatTotalMatches =
                findViewById(
                        R.id.tvStatTotalMatches
                );
    }

    private void loadQuickStats() {
        if (db == null) return;
        int teamCount = db.getAllTeams() != null ? db.getAllTeams().size() : 0;
        int playerCount = db.getAllPlayers() != null ? db.getAllPlayers().size() : 0;
        int matchCount = db.getCompletedMatches() != null ? db.getCompletedMatches().size() : 0;

        if (tvStatTotalTeams != null) tvStatTotalTeams.setText(String.valueOf(teamCount));
        if (tvStatTotalPlayers != null) tvStatTotalPlayers.setText(String.valueOf(playerCount));
        if (tvStatTotalMatches != null) tvStatTotalMatches.setText(String.valueOf(matchCount));
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Cricket Score Manager");
            }
            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        }
        if (navView != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_teams) {
                    openActivity(TeamListActivity.class);
                } else if (id == R.id.nav_players) {
                    openActivity(PlayerListActivity.class);
                } else if (id == R.id.nav_history) {
                    openActivity(MatchHistoryActivity.class);
                } else if (id == R.id.nav_stats) {
                    openActivity(StatisticsActivity.class);
                } else if (id == R.id.nav_settings) {
                    openActivity(SettingsActivity.class);
                }
                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }
    }

    private void loadPausedMatches() {
        if (db == null) return;
        List<MatchInfo> pausedList = db.getPausedMatches();
        if (pausedList == null || pausedList.isEmpty()) {
            if (tvContinueHeader != null) tvContinueHeader.setVisibility(View.GONE);
            if (recyclerContinueMatches != null) recyclerContinueMatches.setVisibility(View.GONE);
        } else {
            if (tvContinueHeader != null) tvContinueHeader.setVisibility(View.VISIBLE);
            if (recyclerContinueMatches != null) {
                recyclerContinueMatches.setVisibility(View.VISIBLE);
                if (continueMatchAdapter == null) {
                    continueMatchAdapter = new ContinueMatchAdapter(pausedList, match -> {
                        if (match != null) {
                            Intent intent = new Intent(DashboardActivity.this, LiveScoringActivity.class);
                            intent.putExtra("match_id", match.getId());
                            startActivity(intent);
                        }
                    });
                    recyclerContinueMatches.setLayoutManager(new LinearLayoutManager(this));
                    recyclerContinueMatches.setAdapter(continueMatchAdapter);
                } else {
                    continueMatchAdapter.setMatches(pausedList);
                }
            }
        }
    }

    private void setupTileContent() {

        setupTile(
                tileTeams,
                "Teams",
                "Create & manage teams",
                R.drawable.ic_team
        );

        setupTile(
                tilePlayers,
                "Players",
                "Add & manage players",
                R.drawable.ic_player
        );

        setupTile(
                tileHistory,
                "Match History",
                "View past matches",
                R.drawable.ic_history
        );

        setupTile(
                tileStats,
                "Statistics",
                "Player & team stats",
                R.drawable.ic_stats
        );
    }

    private void setupTile(
            CardView tile,
            String label,
            String subtitle,
            int iconResource
    ) {

        if (tile == null) {
            return;
        }

        TextView tileLabel =
                tile.findViewById(
                        R.id.tileLabel
                );

        TextView tileSubtitle =
                tile.findViewById(
                        R.id.tileSubtitle
                );

        ImageView tileIcon =
                tile.findViewById(
                        R.id.tileIcon
                );

        if (tileLabel != null) {

            tileLabel.setText(
                    label
            );
        }

        if (tileSubtitle != null) {

            tileSubtitle.setText(
                    subtitle
            );
        }

        if (tileIcon != null) {

            tileIcon.setImageResource(
                    iconResource
            );
        }
    }

    private void setupClickListeners() {

        if (cardNewMatch != null) {

            cardNewMatch.setOnClickListener(
                    v -> openActivity(
                            NewMatchActivity.class
                    )
            );
        }

        if (tileTeams != null) {

            tileTeams.setOnClickListener(
                    v -> openActivity(
                            TeamListActivity.class
                    )
            );
        }

        if (tilePlayers != null) {

            tilePlayers.setOnClickListener(
                    v -> openActivity(
                            PlayerListActivity.class
                    )
            );
        }

        if (tileHistory != null) {

            tileHistory.setOnClickListener(
                    v -> openActivity(
                            MatchHistoryActivity.class
                    )
            );
        }

        if (tileStats != null) {

            tileStats.setOnClickListener(
                    v -> openActivity(
                            StatisticsActivity.class
                    )
            );
        }
    }

    private void openActivity(
            Class<?> activityClass
    ) {

        Intent intent =
                new Intent(
                        DashboardActivity.this,
                        activityClass
                );

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
