package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.adapters.PlayerDetailedStatAdapter;
import com.example.cricketscorer.adapters.TeamStatAdapter;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.models.Team;
import com.example.cricketscorer.models.TeamStat;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView recyclerStats;
    private TabLayout tabLayout;
    private SearchView searchView;

    private final List<Player> players = new ArrayList<>();
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        db = DatabaseHelper.getInstance(this);

        initializeViews();
        setupToolbar();
        setupTabs();
        loadPlayers();
        showStats(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null && recyclerStats != null) {
            loadPlayers();
            showStats(tabLayout != null && tabLayout.getSelectedTabPosition() == 0);
        }
    }

    private void initializeViews() {
        recyclerStats = findViewById(R.id.recyclerStats);
        tabLayout = findViewById(R.id.tabLayout);
        searchView = findViewById(R.id.searchView);

        recyclerStats.setLayoutManager(new LinearLayoutManager(this));

        if (searchView != null) {
            searchView.setVisibility(View.GONE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentQuery = query;
                    showStats(false);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentQuery = newText;
                    if (tabLayout != null && tabLayout.getSelectedTabPosition() == 1) {
                        showStats(false);
                    }
                    return true;
                }
            });
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupTabs() {
        if (tabLayout == null) return;

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean isTeamTab = (tab.getPosition() == 0);
                if (searchView != null) {
                    searchView.setVisibility(isTeamTab ? View.GONE : View.VISIBLE);
                }
                showStats(isTeamTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                boolean isTeamTab = (tab.getPosition() == 0);
                if (searchView != null) {
                    searchView.setVisibility(isTeamTab ? View.GONE : View.VISIBLE);
                }
                showStats(isTeamTab);
            }
        });
    }

    private void loadPlayers() {
        if (db != null) {
            db.recalculateCareerStatistics();
        }
        List<Player> loadedPlayers = db.getAllPlayers();
        players.clear();
        if (loadedPlayers != null) {
            for (Player player : loadedPlayers) {
                if (player == null) continue;
                resolvePlayerTeam(player);
                players.add(player);
            }
        }
    }

    private void resolvePlayerTeam(Player player) {
        if (player == null) return;
        long teamId = player.getTeamId();
        if (teamId <= 0) {
            player.setTeamName("");
            return;
        }
        try {
            Team team = db.getTeamById(teamId);
            if (team != null && team.getName() != null && !team.getName().trim().isEmpty()) {
                player.setTeamName(team.getName().trim());
                return;
            }
        } catch (Exception ignored) {}
        player.setTeamName("");
    }

    private void showStats(boolean teamStatsTab) {
        if (teamStatsTab) {
            List<TeamStat> stats = db.getTeamStats();
            TeamStatAdapter adapter = new TeamStatAdapter(stats);
            recyclerStats.setAdapter(adapter);
        } else {
            List<Player> all = new ArrayList<>(players);

            // Top 10 Batsmen (sorted by runs desc)
            List<Player> batsmen = new ArrayList<>(all);
            batsmen.removeIf(p -> p == null || p.getRuns() <= 0);
            batsmen.sort((a, b) -> Integer.compare(b.getRuns(), a.getRuns()));
            if (batsmen.size() > 10) {
                batsmen = batsmen.subList(0, 10);
            }

            // Top 10 Bowlers (sorted by wickets desc, then economy asc)
            List<Player> bowlers = new ArrayList<>(all);
            bowlers.removeIf(p -> p == null || p.getWickets() <= 0);
            bowlers.sort((a, b) -> {
                int wComp = Integer.compare(b.getWickets(), a.getWickets());
                if (wComp != 0) return wComp;
                double econA = a.getBallsBowled() > 0 ? ((double) a.getRunsConceded() / a.getBallsBowled()) * 6.0 : 99.0;
                double econB = b.getBallsBowled() > 0 ? ((double) b.getRunsConceded() / b.getBallsBowled()) * 6.0 : 99.0;
                return Double.compare(econA, econB);
            });
            if (bowlers.size() > 10) {
                bowlers = bowlers.subList(0, 10);
            }

            // Top 10 All-Rounders (sorted by runs + wickets * 20 desc)
            List<Player> allRounders = new ArrayList<>(all);
            allRounders.removeIf(p -> p == null || (p.getRuns() < 50 && p.getWickets() < 3));
            allRounders.sort((a, b) -> {
                int scoreA = a.getRuns() + a.getWickets() * 20;
                int scoreB = b.getRuns() + b.getWickets() * 20;
                return Integer.compare(scoreB, scoreA);
            });
            if (allRounders.size() > 10) {
                allRounders = allRounders.subList(0, 10);
            }

            PlayerDetailedStatAdapter adapter = new PlayerDetailedStatAdapter(
                    batsmen,
                    bowlers,
                    allRounders,
                    currentQuery,
                    all,
                    this::openPlayerStatistics
            );
            recyclerStats.setAdapter(adapter);
        }
    }

    private void openPlayerStatistics(Player player) {
        if (player == null || player.getId() <= 0) return;
        Intent intent = new Intent(this, PlayerStatDetailActivity.class);
        intent.putExtra("player_id", player.getId());
        startActivity(intent);
    }
}
