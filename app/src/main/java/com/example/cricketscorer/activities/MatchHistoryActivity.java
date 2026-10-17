package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.adapters.MatchHistoryAdapter;
import com.example.cricketscorer.database.DatabaseHelper;
import com.example.cricketscorer.models.MatchInfo;

import java.util.List;

public class MatchHistoryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView recyclerHistory;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_history);

        db = DatabaseHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(
                    v -> finish()
            );
        }

        recyclerHistory =
                findViewById(R.id.recyclerHistory);

        emptyState =
                findViewById(R.id.emptyState);

        recyclerHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadMatchHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null
                && recyclerHistory != null) {
            loadMatchHistory();
        }
    }

    private void loadMatchHistory() {

        List<MatchInfo> matches =
                db.getCompletedMatches();

        if (matches == null) {
            matches = new java.util.ArrayList<>();
        }

        MatchHistoryAdapter adapter =
                new MatchHistoryAdapter(
                        matches,
                        match -> openScorecard(match)
                );

        recyclerHistory.setAdapter(adapter);

        if (emptyState != null) {

            emptyState.setVisibility(
                    matches.isEmpty()
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        recyclerHistory.setVisibility(
                matches.isEmpty()
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    private void openScorecard(
            MatchInfo match
    ) {

        if (match == null
                || match.getId() <= 0) {
            return;
        }

        Intent intent =
                new Intent(
                        this,
                        MatchSummaryActivity.class
                );

        intent.putExtra(
                "match_id",
                match.getId()
        );

        startActivity(intent);
    }
}