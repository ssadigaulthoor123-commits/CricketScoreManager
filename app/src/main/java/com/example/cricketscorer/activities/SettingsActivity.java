package com.example.cricketscorer.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.cricketscorer.R;
import com.example.cricketscorer.database.DatabaseHelper;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private Toolbar toolbar;

    private LinearLayout rowTheme;
    private LinearLayout rowClearHistory;
    private LinearLayout rowResetPlayerRecords;
    private LinearLayout rowResetAll;
    private LinearLayout rowAbout;
    private LinearLayout rowPrivacy;
    private LinearLayout rowTerms;

    private TextView tvCurrentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        db = DatabaseHelper.getInstance(this);

        com.example.cricketscorer.utils.NotificationHelper.checkAndRequestPermission(this);

        initializeViews();

        setupToolbar();

        setupClickListeners();
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        toolbar = findViewById(R.id.toolbar);

        rowTheme = findViewById(R.id.rowTheme);

        rowClearHistory = findViewById(R.id.rowClearHistory);

        rowResetPlayerRecords =
                findViewById(R.id.rowResetPlayerRecords);

        rowResetAll = findViewById(R.id.rowResetAll);

        rowAbout = findViewById(R.id.rowAbout);

        rowPrivacy = findViewById(R.id.rowPrivacy);

        rowTerms = findViewById(R.id.rowTerms);

        tvCurrentTheme =
                findViewById(R.id.tvCurrentTheme);

        android.content.SharedPreferences prefs =
                getSharedPreferences("app_prefs", MODE_PRIVATE);

        String savedTheme =
                prefs.getString("theme", "System default");

        if (tvCurrentTheme != null) {

            tvCurrentTheme.setText(
                    savedTheme
            );
        }
    }

    // ============================================================
    // TOOLBAR
    // ============================================================

    private void setupToolbar() {

        if (toolbar == null) {
            return;
        }

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);

            getSupportActionBar()
                    .setTitle("Settings");
        }

        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }

    // ============================================================
    // CLICK LISTENERS
    // ============================================================

    private void setupClickListeners() {

        if (rowTheme != null) {

            rowTheme.setOnClickListener(
                    v -> showThemeDialog()
            );
        }

        if (rowClearHistory != null) {

            rowClearHistory.setOnClickListener(
                    v -> showClearHistoryDialog()
            );
        }

        if (rowResetPlayerRecords != null) {

            rowResetPlayerRecords.setOnClickListener(
                    v -> showResetPlayerRecordsDialog()
            );
        }

        if (rowResetAll != null) {

            rowResetAll.setOnClickListener(
                    v -> showResetAllDialog()
            );
        }

        if (rowAbout != null) {

            rowAbout.setOnClickListener(
                    v -> showAboutDialog()
            );
        }

        if (rowPrivacy != null) {

            rowPrivacy.setOnClickListener(
                    v -> showPrivacyDialog()
            );
        }

        if (rowTerms != null) {

            rowTerms.setOnClickListener(
                    v -> showTermsDialog()
            );
        }
    }

    // ============================================================
    // THEME
    // ============================================================

    private void showThemeDialog() {

        String[] themes = {
                "System default",
                "Light",
                "Dark"
        };

        new AlertDialog.Builder(this)
                .setTitle("Choose Theme")
                .setItems(
                        themes,
                        (dialog, which) -> {

                            String selected =
                                    themes[which];

                            android.content.SharedPreferences prefs =
                                    getSharedPreferences("app_prefs", MODE_PRIVATE);

                            prefs.edit()
                                    .putString("theme", selected)
                                    .apply();

                            if (tvCurrentTheme != null) {

                                tvCurrentTheme.setText(
                                        selected
                                );
                            }

                            applyTheme(selected);

                            com.example.cricketscorer.utils.NotificationHelper.sendThemeChangedNotification(
                                    this,
                                    selected
                            );

                            Toast.makeText(
                                    this,
                                    "Theme: " + selected,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    private void applyTheme(String theme) {

        if ("Light".equalsIgnoreCase(theme)) {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
            );

        } else if ("Dark".equalsIgnoreCase(theme)) {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
            );

        } else {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            );
        }
    }

    // ============================================================
    // CLEAR MATCH HISTORY
    // ============================================================

    private void showClearHistoryDialog() {

        new AlertDialog.Builder(this)

                .setTitle("Clear Match History")

                .setMessage(
                        "Are you sure you want to clear all "
                                + "saved match history?\n\n"
                                + "Teams and players will NOT be deleted."
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Clear",
                        (dialog, which) ->
                                clearMatchHistory()
                )

                .show();
    }

    private void clearMatchHistory() {

        try {

            /*
             * Deletes saved match records and their
             * batting/bowling records.
             *
             * Teams and players remain.
             */
            db.clearMatchHistory();

            com.example.cricketscorer.utils.NotificationHelper.sendMatchHistoryResetNotification(
                    this
            );

            Toast.makeText(
                    this,
                    "Match history cleared",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to clear match history",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // RESET PLAYER RECORDS
    // ============================================================

    private void showResetPlayerRecordsDialog() {

        new AlertDialog.Builder(this)

                .setTitle("Reset Player Records")

                .setMessage(
                        "This will reset saved career statistics "
                                + "for players.\n\n"
                                + "Teams and player profiles will NOT "
                                + "be deleted."
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Reset",
                        (dialog, which) ->
                                resetPlayerRecords()
                )

                .show();
    }

    private void resetPlayerRecords() {

        try {

            /*
             * IMPORTANT:
             *
             * The actual DatabaseHelper method is:
             *
             * resetPlayerRecords()
             *
             * NOT:
             *
             * resetPlayerCareerStatistics()
             */
            db.resetPlayerRecords();

            Toast.makeText(
                    this,
                    "Player records reset",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to reset player records",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // RESET ALL
    // ============================================================

    private void showResetAllDialog() {

        new AlertDialog.Builder(this)

                .setTitle("Reset Match Data")

                .setMessage(
                        "This will delete saved matches and "
                                + "reset player career records.\n\n"
                                + "Your teams and player profiles "
                                + "will remain."
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Reset",
                        (dialog, which) ->
                                resetAllData()
                )

                .show();
    }

    private void resetAllData() {

        try {

            /*
             * clearMatchHistory() already:
             *
             * 1. Deletes batting records.
             * 2. Deletes bowling records.
             * 3. Deletes matches.
             * 4. Resets career statistics.
             *
             * Teams and players remain.
             */
            db.clearMatchHistory();

            com.example.cricketscorer.utils.NotificationHelper.sendMatchHistoryResetNotification(
                    this
            );

            Toast.makeText(
                    this,
                    "Match data reset",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to reset data",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ============================================================
    // ABOUT
    // ============================================================

    private void showAboutDialog() {

        new AlertDialog.Builder(this)

                .setTitle("Cricket Score Manager")

                .setMessage(
                        "Cricket Score Manager v1.0\n\n"
                                + "An offline Android cricket "
                                + "scoring application for "
                                + "managing teams, players, "
                                + "live matches, scorecards, "
                                + "match history and player "
                                + "statistics.\n\n"
                                + "Developed as an MCA Mobile "
                                + "Application Development "
                                + "project."
                )

                .setPositiveButton(
                        "OK",
                        null
                )

                .show();
    }

    // ============================================================
    // PRIVACY
    // ============================================================

    private void showPrivacyDialog() {

        new AlertDialog.Builder(this)

                .setTitle("Privacy Notice")

                .setMessage(
                        "Cricket Score Manager works offline. "
                                + "Team, player, match and statistics "
                                + "data are stored locally on the device. "
                                + "No login or cloud database is required."
                )

                .setPositiveButton(
                        "OK",
                        null
                )

                .show();
    }

    // ============================================================
    // TERMS
    // ============================================================

    private void showTermsDialog() {

        new AlertDialog.Builder(this)

                .setTitle("Terms of Service")

                .setMessage(
                        "This application is intended for "
                                + "educational and cricket "
                                + "score-management purposes. "
                                + "The scorer is responsible for "
                                + "entering accurate match information."
                )

                .setPositiveButton(
                        "OK",
                        null
                )

                .show();
    }

    // ============================================================
    // BACK
    // ============================================================

    @Override
    public void onBackPressed() {

        finish();
    }
}