package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.adapters.PlayerAdapter;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.models.Team;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayerListActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private RecyclerView recyclerPlayers;
    private View emptyState;
    private FloatingActionButton fabAddPlayer;
    private SearchView searchView;

    private PlayerAdapter adapter;

    private final List<Player> rawPlayers = new ArrayList<>();
    private final List<Player> playerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);

        db = DatabaseHelper.getInstance(this);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadPlayers();
    }

    private void initializeViews() {
        recyclerPlayers = findViewById(R.id.recyclerPlayers);
        emptyState = findViewById(R.id.emptyState);
        fabAddPlayer = findViewById(R.id.fabAddPlayer);
        searchView = findViewById(R.id.searchView);

        recyclerPlayers.setLayoutManager(new LinearLayoutManager(this));

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterPlayers(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterPlayers(newText);
                    return true;
                }
            });
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) return;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Players");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        if (recyclerPlayers == null) return;

        adapter = new PlayerAdapter(
                playerList,
                new PlayerAdapter.OnPlayerClickListener() {
                    @Override
                    public void onPlayerClick(Player player) {
                        editPlayer(player);
                    }

                    @Override
                    public void onEditClick(Player player) {
                        editPlayer(player);
                    }

                    @Override
                    public void onDeleteClick(Player player) {
                        confirmDeletePlayer(player);
                    }
                }
        );

        recyclerPlayers.setAdapter(adapter);
    }

    private void setupListeners() {
        if (fabAddPlayer != null) {
            fabAddPlayer.setOnClickListener(v -> addPlayer());
        }
    }

    private void loadPlayers() {
        if (db == null) return;

        List<Player> loadedPlayers = db.getAllPlayers();
        rawPlayers.clear();

        if (loadedPlayers != null) {
            for (Player player : loadedPlayers) {
                if (player == null) continue;
                resolvePlayerTeam(player);
                rawPlayers.add(player);
            }
        }

        sortPlayers(rawPlayers);
        filterPlayers(searchView != null ? searchView.getQuery().toString() : "");
    }

    private void filterPlayers(String query) {
        playerList.clear();
        if (query == null || query.trim().isEmpty()) {
            playerList.addAll(rawPlayers);
        } else {
            String q = query.trim().toLowerCase();
            for (Player p : rawPlayers) {
                if (p != null && p.getName() != null && p.getName().toLowerCase().contains(q)) {
                    playerList.add(p);
                }
            }
        }
        if (adapter != null) {
            adapter.setPlayers(playerList);
        }
        updateEmptyState();
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

    private void sortPlayers(List<Player> list) {
        Collections.sort(list, (first, second) -> {
            String firstTeam = getSafeTeamName(first);
            String secondTeam = getSafeTeamName(second);

            boolean firstHasTeam = !firstTeam.isEmpty();
            boolean secondHasTeam = !secondTeam.isEmpty();

            if (firstHasTeam && !secondHasTeam) return -1;
            if (!firstHasTeam && secondHasTeam) return 1;
            if (!firstHasTeam) return comparePlayerNames(first, second);

            int teamCompare = firstTeam.compareToIgnoreCase(secondTeam);
            if (teamCompare != 0) return teamCompare;

            return comparePlayerNames(first, second);
        });
    }

    private String getSafeTeamName(Player player) {
        if (player == null || player.getTeamName() == null) return "";
        return player.getTeamName().trim();
    }

    private int comparePlayerNames(Player first, Player second) {
        String firstName = first != null && first.getName() != null ? first.getName().trim() : "";
        String secondName = second != null && second.getName() != null ? second.getName().trim() : "";
        return firstName.compareToIgnoreCase(secondName);
    }

    private void updateEmptyState() {
        boolean empty = playerList.isEmpty();
        if (recyclerPlayers != null) {
            recyclerPlayers.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (emptyState != null) {
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    private void addPlayer() {
        Intent intent = new Intent(this, AddEditPlayerActivity.class);
        startActivityForResult(intent, 100);
    }

    private void editPlayer(Player player) {
        if (player == null || player.getId() <= 0) return;
        Intent intent = new Intent(this, AddEditPlayerActivity.class);
        intent.putExtra("player_id", player.getId());
        intent.putExtra("team_id", player.getTeamId());
        startActivityForResult(intent, 101);
    }

    private void confirmDeletePlayer(Player player) {
        if (player == null || player.getId() <= 0) return;
        String playerName = player.getName() != null && !player.getName().trim().isEmpty() ? player.getName() : "this player";

        new AlertDialog.Builder(this)
                .setTitle("Delete Player")
                .setMessage("Delete \"" + playerName + "\" from the player list?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deletePlayer(player))
                .show();
    }

    private void deletePlayer(Player player) {
        if (player == null) return;
        try {
            db.deletePlayer(player.getId());
            Toast.makeText(this, "Player deleted", Toast.LENGTH_SHORT).show();
            loadPlayers();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to delete player", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadPlayers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) {
            loadPlayers();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
