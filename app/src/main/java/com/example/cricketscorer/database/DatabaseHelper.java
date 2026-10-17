package com.example.cricketscorer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.cricketscorer.models.BattingStat;
import com.example.cricketscorer.models.BowlingStat;
import com.example.cricketscorer.models.MatchInfo;
import com.example.cricketscorer.models.Player;
import com.example.cricketscorer.models.Team;
import com.example.cricketscorer.models.TeamStat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central SQLite database helper for Cricket Score Manager.
 *
 * Supports:
 *
 * 1. Teams
 * 2. Players
 * 3. Matches
 * 4. Batting statistics
 * 5. Bowling statistics
 * 6. Fixtures
 * 7. Tournaments
 * 8. Test Match - 4 innings
 * 9. Player batting hand
 * 10. Player bowling style
 *
 * Database upgrades are NON-DESTRUCTIVE.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ============================================================
    // DATABASE
    // ============================================================

    private static final String DB_NAME =
            "cricket_score_manager.db";

    /*
     * Version history:
     *
     * V1 - Original database
     * V2 - Team icon, match format, Test state, fixtures,
     *      tournaments
     * V3 - Test innings 3 and 4
     * V4 - Player batting hand + bowling style
     *
     * We are keeping the database at V4 here because the actual
     * required statistics can be calculated from Batting/Bowling
     * records and do not require a duplicate statistics table.
     */
    private static final int DB_VERSION = 10;

    // ============================================================
    // TABLE NAMES
    // ============================================================

    public static final String TABLE_TEAM =
            "Team";

    public static final String TABLE_PLAYER =
            "Player";

    public static final String TABLE_MATCH =
            "\"Match\"";

    public static final String TABLE_BATTING =
            "Batting";

    public static final String TABLE_BOWLING =
            "Bowling";

    public static final String TABLE_FIXTURE =
            "Fixture";

    public static final String TABLE_TOURNAMENT =
            "Tournament";

    public static final String TABLE_TOURNAMENT_TEAM =
            "TournamentTeam";

    private static DatabaseHelper instance;

    // ============================================================
    // SINGLETON
    // ============================================================

    public static synchronized DatabaseHelper getInstance(
            Context context
    ) {

        if (instance == null) {

            instance =
                    new DatabaseHelper(
                            context.getApplicationContext()
                    );
        }

        return instance;
    }

    private DatabaseHelper(Context context) {

        super(
                context,
                DB_NAME,
                null,
                DB_VERSION
        );
    }

    // ============================================================
    // DATABASE CREATE
    // ============================================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        // ========================================================
        // TEAM
        // ========================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_TEAM +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "name TEXT NOT NULL," +

                        "short_name TEXT," +

                        "icon_uri TEXT" +

                        ")"
        );

        // ========================================================
        // PLAYER
        // ========================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_PLAYER +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "name TEXT NOT NULL," +

                        "team_id INTEGER NOT NULL," +

                        "jersey_number INTEGER DEFAULT 0," +

                        "role TEXT," +

                        // Player information
                        "batting_hand TEXT DEFAULT 'Right Hand'," +

                        "bowling_style TEXT DEFAULT 'None'," +

                        // Career batting
                        "matches INTEGER DEFAULT 0," +

                        "innings INTEGER DEFAULT 0," +

                        "runs INTEGER DEFAULT 0," +

                        "balls INTEGER DEFAULT 0," +

                        "highest_score INTEGER DEFAULT 0," +

                        "fours INTEGER DEFAULT 0," +

                        "sixes INTEGER DEFAULT 0," +

                        "ducks INTEGER DEFAULT 0," +

                        "times_out INTEGER DEFAULT 0," +

                        "not_outs INTEGER DEFAULT 0," +

                        "thirties INTEGER DEFAULT 0," +

                        "fifties INTEGER DEFAULT 0," +

                        "centuries INTEGER DEFAULT 0," +

                        "mom_count INTEGER DEFAULT 0," +

                        // Career bowling
                        "balls_bowled INTEGER DEFAULT 0," +

                        "bowling_innings INTEGER DEFAULT 0," +

                        "wickets INTEGER DEFAULT 0," +

                        "runs_conceded INTEGER DEFAULT 0," +

                        "maidens INTEGER DEFAULT 0," +

                        "dot_balls INTEGER DEFAULT 0," +

                        "extras_conceded INTEGER DEFAULT 0," +

                        "three_wicket_hauls INTEGER DEFAULT 0," +

                        "five_wicket_hauls INTEGER DEFAULT 0," +

                        "best_bowling TEXT DEFAULT '0/0'," +

                        // Fielding
                        "catches INTEGER DEFAULT 0," +

                        "run_outs INTEGER DEFAULT 0," +

                        "stumpings INTEGER DEFAULT 0," +

                        "FOREIGN KEY(team_id) REFERENCES " +
                        TABLE_TEAM +
                        "(id)" +

                        ")"
        );

        // ========================================================
        // MATCH
        // ========================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_MATCH +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "team_a_id INTEGER," +

                        "team_b_id INTEGER," +

                        /*
                         * T20 / ODI / CUSTOM use this field.
                         *
                         * Test matches have no fixed innings
                         * over limit, so this can be 0.
                         */
                        "total_overs INTEGER DEFAULT 20," +

                        /*
                         * T20
                         * ODI
                         * TEST
                         * CUSTOM
                         */
                        "match_format TEXT DEFAULT 'T20'," +

                        "toss_winner_team_id INTEGER," +

                        "toss_decision TEXT," +

                        "batting_team_id INTEGER," +

                        "bowling_team_id INTEGER," +

                        // ------------------------------------------------
                        // INNINGS 1
                        // ------------------------------------------------

                        "inn1_runs INTEGER DEFAULT 0," +

                        "inn1_wickets INTEGER DEFAULT 0," +

                        "inn1_balls INTEGER DEFAULT 0," +

                        // ------------------------------------------------
                        // INNINGS 2
                        // ------------------------------------------------

                        "inn2_runs INTEGER DEFAULT 0," +

                        "inn2_wickets INTEGER DEFAULT 0," +

                        "inn2_balls INTEGER DEFAULT 0," +

                        // ------------------------------------------------
                        // INNINGS 3
                        // TEST
                        // ------------------------------------------------

                        "inn3_runs INTEGER DEFAULT 0," +

                        "inn3_wickets INTEGER DEFAULT 0," +

                        "inn3_balls INTEGER DEFAULT 0," +

                        // ------------------------------------------------
                        // INNINGS 4
                        // TEST
                        // ------------------------------------------------

                        "inn4_runs INTEGER DEFAULT 0," +

                        "inn4_wickets INTEGER DEFAULT 0," +

                        "inn4_balls INTEGER DEFAULT 0," +

                        // ------------------------------------------------
                        // CURRENT MATCH STATE
                        // ------------------------------------------------

                        "current_innings INTEGER DEFAULT 1," +

                        "completed INTEGER DEFAULT 0," +

                        "innings1_team_id INTEGER DEFAULT -1," +

                        "innings2_team_id INTEGER DEFAULT -1," +

                        "result_text TEXT," +

                        "player_of_match_id INTEGER DEFAULT 0," +

                        "match_date TEXT," +

                        // ------------------------------------------------
                        // TEST MATCH STATE
                        // ------------------------------------------------

                        "current_day INTEGER DEFAULT 1," +

                        "current_session TEXT DEFAULT 'Morning'," +

                        "declared INTEGER DEFAULT 0," +

                        "follow_on INTEGER DEFAULT 0," +
                        "is_paused INTEGER DEFAULT 0," +
                        "striker_id INTEGER DEFAULT -1," +
                        "non_striker_id INTEGER DEFAULT -1," +
                        "bowler_id INTEGER DEFAULT -1," +
                        "previous_bowler_id INTEGER DEFAULT -1," +
                        "legal_balls_this_over INTEGER DEFAULT 0" +

                        ")"
        );

        // ========================================================
        // BATTING
        // ========================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_BATTING +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "match_id INTEGER," +

                        "innings INTEGER," +

                        "player_id INTEGER," +

                        "runs INTEGER DEFAULT 0," +

                        "balls INTEGER DEFAULT 0," +

                        "fours INTEGER DEFAULT 0," +

                        "sixes INTEGER DEFAULT 0," +

                        "is_out INTEGER DEFAULT 0," +

                        "how_out TEXT," +

                        "fielder_id INTEGER DEFAULT 0," +

                        "bowler_id INTEGER DEFAULT 0," +

                        "bowler_name TEXT," +

                        "fielder_name TEXT," +

                        "FOREIGN KEY(match_id) REFERENCES " +
                        TABLE_MATCH +
                        "(id)," +

                        "FOREIGN KEY(player_id) REFERENCES " +
                        TABLE_PLAYER +
                        "(id)" +

                        ")"
        );

        // ========================================================
        // BOWLING
        // ========================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_BOWLING +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "match_id INTEGER," +

                        "innings INTEGER," +

                        "player_id INTEGER," +

                        "balls INTEGER DEFAULT 0," +

                        "runs_conceded INTEGER DEFAULT 0," +

                        "wickets INTEGER DEFAULT 0," +

                        "maidens INTEGER DEFAULT 0," +

                        "dot_balls INTEGER DEFAULT 0," +

                        "extras_conceded INTEGER DEFAULT 0," +

                        "FOREIGN KEY(match_id) REFERENCES " +
                        TABLE_MATCH +
                        "(id)," +

                        "FOREIGN KEY(player_id) REFERENCES " +
                        TABLE_PLAYER +
                        "(id)" +

                        ")"
        );

        // ========================================================
        // FUTURE / TOURNAMENT TABLES
        // ========================================================

        createFutureFeatureTables(db);
    }

    // ============================================================
    // DATABASE UPGRADE
    // ============================================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        /*
         * IMPORTANT:
         *
         * Existing data is preserved.
         *
         * We NEVER DROP Team/Player/Match/Batting/Bowling
         * simply because the application version changed.
         */

        // ========================================================
        // VERSION 2
        // ========================================================

        if (oldVersion < 2) {

            addColumnIfMissing(
                    db,
                    TABLE_TEAM,
                    "icon_uri",
                    "TEXT"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "match_format",
                    "TEXT DEFAULT 'T20'"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "current_day",
                    "INTEGER DEFAULT 1"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "current_session",
                    "TEXT DEFAULT 'Morning'"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "declared",
                    "INTEGER DEFAULT 0"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "follow_on",
                    "INTEGER DEFAULT 0"
            );
        }

        // ========================================================
        // VERSION 3
        // TEST INNINGS 3 + 4
        // ========================================================

        if (oldVersion < 3) {

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "inn3_runs",
                    "INTEGER DEFAULT 0"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "inn3_wickets",
                    "INTEGER DEFAULT 0"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "inn3_balls",
                    "INTEGER DEFAULT 0"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "inn4_runs",
                    "INTEGER DEFAULT 0"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "inn4_wickets",
                    "INTEGER DEFAULT 0"
            );

            addColumnIfMissing(
                    db,
                    TABLE_MATCH,
                    "inn4_balls",
                    "INTEGER DEFAULT 0"
            );
        }

        // ========================================================
        // VERSION 4
        // PLAYER BATTING HAND + BOWLING STYLE
        // ========================================================

        if (oldVersion < 4) {

            addColumnIfMissing(
                    db,
                    TABLE_PLAYER,
                    "batting_hand",
                    "TEXT DEFAULT 'Right Hand'"
            );

            addColumnIfMissing(
                    db,
                    TABLE_PLAYER,
                    "bowling_style",
                    "TEXT DEFAULT 'None'"
            );

            /*
             * Existing players:
             *
             * Batting hand -> Right Hand
             * Bowling -> None
             */
            db.execSQL(
                    "UPDATE " +
                            TABLE_PLAYER +
                            " SET batting_hand='Right Hand' " +
                            "WHERE batting_hand IS NULL " +
                            "OR TRIM(batting_hand)=''"
            );

            db.execSQL(
                    "UPDATE " +
                            TABLE_PLAYER +
                            " SET bowling_style='None' " +
                            "WHERE bowling_style IS NULL " +
                            "OR TRIM(bowling_style)=''"
            );
        }

        // ========================================================
        // VERSION 5
        // COMPREHENSIVE STATS + FIELDING + RETIRE HURT
        // ========================================================

        if (oldVersion < 5) {
            addColumnIfMissing(db, TABLE_PLAYER, "not_outs", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "thirties", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "fifties", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "centuries", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "mom_count", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "maidens", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "extras_conceded", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "dot_balls", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "three_wicket_hauls", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "five_wicket_hauls", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "best_bowling", "TEXT DEFAULT '0/0'");
            addColumnIfMissing(db, TABLE_PLAYER, "catches", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "run_outs", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PLAYER, "stumpings", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_BATTING, "fielder_id", "INTEGER DEFAULT 0");
        }

        // ========================================================
        // VERSION 6
        // BATTING DISMISSAL BOWLER & FIELDER INFO
        // ========================================================

        if (oldVersion < 6) {
            addColumnIfMissing(db, TABLE_BATTING, "bowler_id", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_BATTING, "bowler_name", "TEXT");
            addColumnIfMissing(db, TABLE_BATTING, "fielder_id", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_BATTING, "fielder_name", "TEXT");
        }

        // ========================================================
        // VERSION 7
        // INNINGS TEAM IDs
        // ========================================================

        if (oldVersion < 7) {
            addColumnIfMissing(db, TABLE_MATCH, "innings1_team_id", "INTEGER DEFAULT -1");
            addColumnIfMissing(db, TABLE_MATCH, "innings2_team_id", "INTEGER DEFAULT -1");
        }

        // ========================================================
        // VERSION 8
        // BOWLING DOT BALLS & EXTRAS CONCEDED
        // ========================================================

        if (oldVersion < 8) {
            addColumnIfMissing(db, TABLE_BOWLING, "maidens", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_BOWLING, "dot_balls", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_BOWLING, "extras_conceded", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_BATTING, "bowler_id", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_BATTING, "bowler_name", "TEXT");
            addColumnIfMissing(db, TABLE_BATTING, "fielder_id", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_BATTING, "fielder_name", "TEXT");
        }

        // ========================================================
        // VERSION 9
        // PAUSED MATCH SUPPORT
        // ========================================================

        if (oldVersion < 9) {
            addColumnIfMissing(db, TABLE_MATCH, "is_paused", "INTEGER DEFAULT 0");
        }

        // ========================================================
        // VERSION 10
        // BOWLING INNINGS & LIVE MATCH STATE COLUMNS
        // ========================================================

        if (oldVersion < 10) {
            addColumnIfMissing(db, TABLE_PLAYER, "bowling_innings", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_MATCH, "striker_id", "INTEGER DEFAULT -1");
            addColumnIfMissing(db, TABLE_MATCH, "non_striker_id", "INTEGER DEFAULT -1");
            addColumnIfMissing(db, TABLE_MATCH, "bowler_id", "INTEGER DEFAULT -1");
            addColumnIfMissing(db, TABLE_MATCH, "previous_bowler_id", "INTEGER DEFAULT -1");
            addColumnIfMissing(db, TABLE_MATCH, "legal_balls_this_over", "INTEGER DEFAULT 0");
        }

        // ========================================================
        // MAKE SURE FUTURE TABLES EXIST
        // ========================================================

        createFutureFeatureTables(db);
    }

    // ============================================================
    // SAFE COLUMN ADD
    // ============================================================

    private void addColumnIfMissing(
            SQLiteDatabase db,
            String tableName,
            String columnName,
            String columnDefinition
    ) {

        Cursor cursor = null;

        try {

            cursor =
                    db.rawQuery(
                            "PRAGMA table_info(" +
                                    tableName +
                                    ")",
                            null
                    );

            boolean exists = false;

            int nameIndex =
                    cursor.getColumnIndex("name");

            while (cursor.moveToNext()) {

                if (nameIndex >= 0) {

                    String existingName =
                            cursor.getString(nameIndex);

                    if (
                            columnName.equalsIgnoreCase(
                                    existingName
                            )
                    ) {

                        exists = true;
                        break;
                    }
                }
            }

            if (!exists) {

                db.execSQL(
                        "ALTER TABLE " +
                                tableName +
                                " ADD COLUMN " +
                                columnName +
                                " " +
                                columnDefinition
                );
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ============================================================
    // FUTURE FEATURE TABLES
    // ============================================================

    private void createFutureFeatureTables(
            SQLiteDatabase db
    ) {

        // ========================================================
        // FIXTURE
        // ========================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_FIXTURE +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "team_a_id INTEGER NOT NULL," +

                        "team_b_id INTEGER NOT NULL," +

                        "fixture_date TEXT," +

                        "fixture_time TEXT," +

                        "venue TEXT," +

                        "format TEXT DEFAULT 'T20'," +

                        "status TEXT DEFAULT 'Scheduled'," +

                        "match_id INTEGER DEFAULT 0," +

                        "tournament_id INTEGER DEFAULT 0" +

                        ")"
        );

        // ========================================================
        // TOURNAMENT
        // ========================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_TOURNAMENT +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "name TEXT NOT NULL," +

                        /*
                         * Valid values will be enforced by the
                         * Tournament Activity:
                         *
                         * 4, 6, 8, 10
                         */
                        "number_of_teams INTEGER DEFAULT 4," +

                        "start_date TEXT," +

                        "end_date TEXT," +

                        "venue TEXT," +

                        "format TEXT DEFAULT 'T20'," +

                        "completed INTEGER DEFAULT 0" +

                        ")"
        );

        // ========================================================
        // TOURNAMENT TEAM
        // ========================================================

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_TOURNAMENT_TEAM +
                        " (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "tournament_id INTEGER NOT NULL," +

                        "team_id INTEGER NOT NULL," +

                        "points INTEGER DEFAULT 0," +

                        "played INTEGER DEFAULT 0," +

                        "wins INTEGER DEFAULT 0," +

                        "losses INTEGER DEFAULT 0," +

                        "ties INTEGER DEFAULT 0," +

                        "no_results INTEGER DEFAULT 0," +

                        "runs_for INTEGER DEFAULT 0," +

                        "runs_against INTEGER DEFAULT 0" +

                        ")"
        );
    }

    // ============================================================
    // TEAM CRUD
    // ============================================================

    public long addTeam(Team team) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "name",
                team.getName()
        );

        values.put(
                "short_name",
                team.getShortName()
        );

        values.put(
                "icon_uri",
                team.getIconUri()
        );

        return db.insert(
                TABLE_TEAM,
                null,
                values
        );
    }

    // ============================================================
    // TEAM STATS (WINS / LOSSES / MATCHES)
    // ============================================================

    public List<TeamStat> getTeamStats() {
        List<Team> teams = getAllTeams();
        List<TeamStat> stats = new ArrayList<>();
        if (teams == null) {
            return stats;
        }

        List<MatchInfo> completed = getCompletedMatches();

        Map<Long, Integer> playedMap = new HashMap<>();
        Map<Long, Integer> winsMap = new HashMap<>();
        Map<Long, Integer> lossesMap = new HashMap<>();
        Map<Long, Integer> tiesMap = new HashMap<>();

        if (completed != null) {
            for (MatchInfo m : completed) {
                if (m == null) continue;
                long tA = m.getTeamAId();
                long tB = m.getTeamBId();
                Integer pA = playedMap.get(tA);
                playedMap.put(tA, (pA != null ? pA : 0) + 1);
                Integer pB = playedMap.get(tB);
                playedMap.put(tB, (pB != null ? pB : 0) + 1);

                int r1 = m.getInn1Runs();
                int r2 = m.getInn2Runs();
                if (r1 > r2) {
                    long winner = m.getFirstBattingTeamId();
                    long loser = m.getSecondBattingTeamId();
                    Integer w = winsMap.get(winner);
                    winsMap.put(winner, (w != null ? w : 0) + 1);
                    Integer l = lossesMap.get(loser);
                    lossesMap.put(loser, (l != null ? l : 0) + 1);
                } else if (r2 > r1) {
                    long winner = m.getSecondBattingTeamId();
                    long loser = m.getFirstBattingTeamId();
                    Integer w = winsMap.get(winner);
                    winsMap.put(winner, (w != null ? w : 0) + 1);
                    Integer l = lossesMap.get(loser);
                    lossesMap.put(loser, (l != null ? l : 0) + 1);
                } else {
                    Integer t1 = tiesMap.get(tA);
                    tiesMap.put(tA, (t1 != null ? t1 : 0) + 1);
                    Integer t2 = tiesMap.get(tB);
                    tiesMap.put(tB, (t2 != null ? t2 : 0) + 1);
                }
            }
        }

        for (Team t : teams) {
            if (t == null) continue;
            TeamStat ts = new TeamStat();
            ts.setTeamId(t.getId());
            ts.setTeamName(t.getName());
            Integer p = playedMap.get(t.getId());
            ts.setMatchesPlayed(p != null ? p : 0);
            Integer w = winsMap.get(t.getId());
            ts.setWins(w != null ? w : 0);
            Integer l = lossesMap.get(t.getId());
            ts.setLosses(l != null ? l : 0);
            Integer ti = tiesMap.get(t.getId());
            ts.setTies(ti != null ? ti : 0);
            stats.add(ts);
        }

        stats.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));
        return stats;
    }

    public void updateTeam(Team team) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "name",
                team.getName()
        );

        values.put(
                "short_name",
                team.getShortName()
        );

        values.put(
                "icon_uri",
                team.getIconUri()
        );

        db.update(
                TABLE_TEAM,
                values,
                "id=?",
                new String[]{
                        String.valueOf(
                                team.getId()
                        )
                }
        );
    }

    public void deleteTeam(long teamId) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.execSQL(
                    "DELETE FROM " + TABLE_BATTING +
                            " WHERE player_id IN (SELECT id FROM " +
                            TABLE_PLAYER + " WHERE team_id=?)",
                    new String[]{String.valueOf(teamId)}
            );

            db.execSQL(
                    "DELETE FROM " + TABLE_BOWLING +
                            " WHERE player_id IN (SELECT id FROM " +
                            TABLE_PLAYER + " WHERE team_id=?)",
                    new String[]{String.valueOf(teamId)}
            );

            db.delete(TABLE_PLAYER, "team_id=?",
                    new String[]{String.valueOf(teamId)});
            db.delete(TABLE_TEAM, "id=?",
                    new String[]{String.valueOf(teamId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ============================================================
    // GET ALL TEAMS
    // ============================================================

    public List<Team> getAllTeams() {

        List<Team> list =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.query(
                        TABLE_TEAM,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "name ASC"
                );

        while (c.moveToNext()) {

            list.add(
                    teamFromCursor(c)
            );
        }

        c.close();

        return list;
    }

    // ============================================================
    // GET PAUSED / IN-PROGRESS MATCHES
    // ============================================================

    public List<MatchInfo> getPausedMatches() {

        List<MatchInfo> list =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.rawQuery(
                        "SELECT m.*, " +

                                "ta.name AS team_a_name, " +

                                "tb.name AS team_b_name, " +

                                "pom.name AS pom_name " +

                                "FROM " +
                                TABLE_MATCH +
                                " m " +

                                "LEFT JOIN " +
                                TABLE_TEAM +
                                " ta ON " +
                                "m.team_a_id=ta.id " +

                                "LEFT JOIN " +
                                TABLE_TEAM +
                                " tb ON " +
                                "m.team_b_id=tb.id " +

                                "LEFT JOIN " +
                                TABLE_PLAYER +
                                " pom ON " +
                                "m.player_of_match_id=pom.id " +

                                "WHERE m.completed=0 AND (m.is_paused=1 OR m.inn1_balls > 0 OR m.inn2_balls > 0) " +

                                "ORDER BY m.id DESC",
                        null
                );

        while (c.moveToNext()) {

            list.add(
                    matchFromCursor(c)
            );
        }

        c.close();

        return list;
    }

    // ============================================================
    // GET TEAM BY ID
    // ============================================================

    public Team getTeamById(long id) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.query(
                        TABLE_TEAM,
                        null,
                        "id=?",
                        new String[]{
                                String.valueOf(id)
                        },
                        null,
                        null,
                        null
                );

        Team team = null;

        if (c.moveToFirst()) {

            team =
                    teamFromCursor(c);
        }

        c.close();

        return team;
    }

    // ============================================================
    // TEAM CURSOR
    // ============================================================

    private Team teamFromCursor(Cursor c) {

        Team team =
                new Team();

        team.setId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "id"
                        )
                )
        );

        team.setName(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "name"
                        )
                )
        );

        team.setShortName(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "short_name"
                        )
                )
        );

        int iconIndex =
                c.getColumnIndex("icon_uri");

        if (
                iconIndex >= 0 &&
                        !c.isNull(iconIndex)
        ) {

            team.setIconUri(
                    c.getString(iconIndex)
            );
        }

        return team;
    }

    // ============================================================
    // PLAYER CRUD
    // ============================================================

    public long addPlayer(Player player) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "name",
                player.getName()
        );

        values.put(
                "team_id",
                player.getTeamId()
        );

        values.put(
                "jersey_number",
                player.getJerseyNumber()
        );

        values.put(
                "role",
                player.getRole()
        );

        values.put(
                "batting_hand",
                player.getBattingHand()
        );

        values.put(
                "bowling_style",
                player.getBowlingStyle()
        );

        return db.insert(
                TABLE_PLAYER,
                null,
                values
        );
    }

    public void updatePlayer(Player player) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "name",
                player.getName()
        );

        values.put(
                "team_id",
                player.getTeamId()
        );

        values.put(
                "jersey_number",
                player.getJerseyNumber()
        );

        values.put(
                "role",
                player.getRole()
        );

        values.put(
                "batting_hand",
                player.getBattingHand()
        );

        values.put(
                "bowling_style",
                player.getBowlingStyle()
        );

        db.update(
                TABLE_PLAYER,
                values,
                "id=?",
                new String[]{
                        String.valueOf(
                                player.getId()
                        )
                }
        );
    }

    public void deletePlayer(long playerId) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(TABLE_BATTING, "player_id=?",
                    new String[]{String.valueOf(playerId)});
            db.delete(TABLE_BOWLING, "player_id=?",
                    new String[]{String.valueOf(playerId)});
            db.delete(TABLE_PLAYER, "id=?",
                    new String[]{String.valueOf(playerId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ============================================================
    // GET ALL PLAYERS
    // ============================================================

    public List<Player> getAllPlayers() {

        return queryPlayers(
                null,
                null
        );
    }

    // ============================================================
    // GET PLAYERS BY TEAM
    // ============================================================

    public List<Player> getPlayersByTeam(
            long teamId
    ) {

        return queryPlayers(
                "team_id=?",
                new String[]{
                        String.valueOf(teamId)
                }
        );
    }

    // ============================================================
    // PLAYER BY ID
    // ============================================================

    public Player getPlayerById(
            long playerId
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.query(
                        TABLE_PLAYER,
                        null,
                        "id=?",
                        new String[]{
                                String.valueOf(playerId)
                        },
                        null,
                        null,
                        null
                );

        Player player = null;

        if (c.moveToFirst()) {

            player =
                    playerFromCursor(c);
        }

        c.close();

        return player;
    }

    // ============================================================
    // PLAYER QUERY
    // ============================================================

    private List<Player> queryPlayers(
            String selection,
            String[] selectionArgs
    ) {

        List<Player> list =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.query(
                        TABLE_PLAYER,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        "name ASC"
                );

        while (c.moveToNext()) {

            list.add(
                    playerFromCursor(c)
            );
        }

        c.close();

        return list;
    }

    // ============================================================
    // PLAYER CURSOR
    // ============================================================

    private Player playerFromCursor(
            Cursor c
    ) {

        Player player =
                new Player();

        player.setId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "id"
                        )
                )
        );

        player.setName(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "name"
                        )
                )
        );

        player.setTeamId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "team_id"
                        )
                )
        );

        player.setJerseyNumber(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "jersey_number"
                        )
                )
        );

        player.setRole(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "role"
                        )
                )
        );

        // --------------------------------------------------------
        // Batting hand
        // --------------------------------------------------------

        int battingHandIndex =
                c.getColumnIndex(
                        "batting_hand"
                );

        if (
                battingHandIndex >= 0 &&
                        !c.isNull(battingHandIndex)
        ) {

            player.setBattingHand(
                    c.getString(
                            battingHandIndex
                    )
            );
        } else {

            player.setBattingHand(
                    "Right Hand"
            );
        }

        // --------------------------------------------------------
        // Bowling style
        // --------------------------------------------------------

        int bowlingStyleIndex =
                c.getColumnIndex(
                        "bowling_style"
                );

        if (
                bowlingStyleIndex >= 0 &&
                        !c.isNull(bowlingStyleIndex)
        ) {

            player.setBowlingStyle(
                    c.getString(
                            bowlingStyleIndex
                    )
            );
        } else {

            player.setBowlingStyle(
                    "None"
            );
        }

        // --------------------------------------------------------
        // Career batting
        // --------------------------------------------------------

        player.setMatches(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "matches"
                        )
                )
        );

        player.setInnings(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "innings"
                        )
                )
        );

        player.setRuns(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "runs"
                        )
                )
        );

        player.setBalls(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "balls"
                        )
                )
        );

        player.setHighestScore(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "highest_score"
                        )
                )
        );

        player.setFours(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "fours"
                        )
                )
        );

        player.setSixes(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "sixes"
                        )
                )
        );

        player.setDucks(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "ducks"
                        )
                )
        );

        player.setTimesOut(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "times_out"
                        )
                )
        );

        // --------------------------------------------------------
        // Career bowling
        // --------------------------------------------------------

        player.setBallsBowled(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "balls_bowled"
                        )
                )
        );

        player.setBowlingInnings(getIntCol(c, "bowling_innings"));

        player.setWickets(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "wickets"
                        )
                )
        );

        player.setRunsConceded(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "runs_conceded"
                        )
                )
        );

        player.setNotOuts(getIntCol(c, "not_outs"));
        player.setThirties(getIntCol(c, "thirties"));
        player.setFifties(getIntCol(c, "fifties"));
        player.setCenturies(getIntCol(c, "centuries"));
        player.setMomCount(getIntCol(c, "mom_count"));
        player.setMaidens(getIntCol(c, "maidens"));
        player.setExtrasConceded(getIntCol(c, "extras_conceded"));
        player.setDotBalls(getIntCol(c, "dot_balls"));
        player.setThreeWicketHauls(getIntCol(c, "three_wicket_hauls"));
        player.setFiveWicketHauls(getIntCol(c, "five_wicket_hauls"));
        player.setBestBowling(getStringCol(c, "best_bowling", "0/0"));
        player.setCatches(getIntCol(c, "catches"));
        player.setRunOuts(getIntCol(c, "run_outs"));
        player.setStumpings(getIntCol(c, "stumpings"));

        return player;
    }

    private int getIntCol(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        if (idx >= 0 && !c.isNull(idx)) {
            return c.getInt(idx);
        }
        return 0;
    }

    private String getStringCol(Cursor c, String col, String fallback) {
        int idx = c.getColumnIndex(col);
        if (idx >= 0 && !c.isNull(idx)) {
            String val = c.getString(idx);
            return val != null ? val : fallback;
        }
        return fallback;
    }

    // ============================================================
    // CREATE MATCH
    // ============================================================

    public long createMatch(
            MatchInfo match
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "team_a_id",
                match.getTeamAId()
        );

        values.put(
                "team_b_id",
                match.getTeamBId()
        );

        values.put(
                "total_overs",
                match.getTotalOvers()
        );

        values.put(
                "match_format",
                normalizeFormat(
                        match.getMatchFormat()
                )
        );

        values.put(
                "toss_winner_team_id",
                match.getTossWinnerTeamId()
        );

        values.put(
                "toss_decision",
                match.getTossDecision()
        );

        values.put(
                "batting_team_id",
                match.getBattingTeamId()
        );

        values.put(
                "bowling_team_id",
                match.getBowlingTeamId()
        );

        values.put(
                "innings1_team_id",
                match.getInningsTeamId(1) > 0 ? match.getInningsTeamId(1) : match.getBattingTeamId()
        );

        values.put(
                "innings2_team_id",
                match.getInningsTeamId(2) > 0 ? match.getInningsTeamId(2) : match.getOppositionTeamId(match.getBattingTeamId())
        );

        values.put(
                "completed",
                match.isCompleted() ? 1 : 0
        );

        values.put(
                "is_paused",
                match.isPaused() ? 1 : 0
        );

        values.put("striker_id", match.getStrikerId());
        values.put("non_striker_id", match.getNonStrikerId());
        values.put("bowler_id", match.getBowlerId());
        values.put("previous_bowler_id", match.getPreviousBowlerId());
        values.put("legal_balls_this_over", match.getLegalBallsThisOver());

        values.put(
                "is_paused",
                match.isPaused() ? 1 : 0
        );

        values.put("striker_id", match.getStrikerId());
        values.put("non_striker_id", match.getNonStrikerId());
        values.put("bowler_id", match.getBowlerId());
        values.put("previous_bowler_id", match.getPreviousBowlerId());
        values.put("legal_balls_this_over", match.getLegalBallsThisOver());

        values.put(
                "inn1_runs",
                match.getInn1Runs()
        );

        values.put(
                "inn1_wickets",
                match.getInn1Wickets()
        );

        values.put(
                "inn1_balls",
                match.getInn1Balls()
        );

        values.put(
                "inn2_runs",
                match.getInn2Runs()
        );

        values.put(
                "inn2_wickets",
                match.getInn2Wickets()
        );

        values.put(
                "inn2_balls",
                match.getInn2Balls()
        );

        values.put(
                "inn3_runs",
                match.getInn3Runs()
        );

        values.put(
                "inn3_wickets",
                match.getInn3Wickets()
        );

        values.put(
                "inn3_balls",
                match.getInn3Balls()
        );

        values.put(
                "inn4_runs",
                match.getInn4Runs()
        );

        values.put(
                "inn4_wickets",
                match.getInn4Wickets()
        );

        values.put(
                "inn4_balls",
                match.getInn4Balls()
        );

        values.put(
                "current_innings",
                match.getCurrentInnings()
        );

        values.put(
                "completed",
                match.isCompleted() ? 1 : 0
        );

        values.put(
                "is_paused",
                match.isPaused() ? 1 : 0
        );

        values.put("striker_id", match.getStrikerId());
        values.put("non_striker_id", match.getNonStrikerId());
        values.put("bowler_id", match.getBowlerId());
        values.put("previous_bowler_id", match.getPreviousBowlerId());
        values.put("legal_balls_this_over", match.getLegalBallsThisOver());

        values.put(
                "result_text",
                match.getResultText()
        );

        values.put(
                "player_of_match_id",
                match.getPlayerOfMatchId()
        );

        values.put(
                "match_date",
                match.getMatchDate()
        );

        values.put(
                "current_day",
                match.getCurrentDay()
        );

        values.put(
                "current_session",
                match.getCurrentSession()
        );

        values.put(
                "declared",
                match.isDeclared() ? 1 : 0
        );

        values.put(
                "follow_on",
                match.isFollowOn() ? 1 : 0
        );

        values.put(
                "completed",
                match.isCompleted() ? 1 : 0
        );

        values.put(
                "is_paused",
                match.isPaused() ? 1 : 0
        );

        values.put("striker_id", match.getStrikerId());
        values.put("non_striker_id", match.getNonStrikerId());
        values.put("bowler_id", match.getBowlerId());
        values.put("previous_bowler_id", match.getPreviousBowlerId());
        values.put("legal_balls_this_over", match.getLegalBallsThisOver());

        values.put(
                "is_paused",
                match.isPaused() ? 1 : 0
        );

        values.put("striker_id", match.getStrikerId());
        values.put("non_striker_id", match.getNonStrikerId());
        values.put("bowler_id", match.getBowlerId());
        values.put("previous_bowler_id", match.getPreviousBowlerId());
        values.put("legal_balls_this_over", match.getLegalBallsThisOver());

        long id = db.insert(
                TABLE_MATCH,
                null,
                values
        );

        if (id != -1) {
            match.setId(id);
        }

        return id;
    }

    // ============================================================
    // GET MATCH BY ID
    // ============================================================

    public MatchInfo getMatchById(
            long matchId
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.rawQuery(
                        "SELECT m.*, " +

                                "ta.name AS team_a_name, " +

                                "tb.name AS team_b_name, " +

                                "pom.name AS pom_name " +

                                "FROM " +
                                TABLE_MATCH +
                                " m " +

                                "LEFT JOIN " +
                                TABLE_TEAM +
                                " ta ON " +
                                "m.team_a_id=ta.id " +

                                "LEFT JOIN " +
                                TABLE_TEAM +
                                " tb ON " +
                                "m.team_b_id=tb.id " +

                                "LEFT JOIN " +
                                TABLE_PLAYER +
                                " pom ON " +
                                "m.player_of_match_id=pom.id " +

                                "WHERE m.id=?",

                        new String[]{
                                String.valueOf(matchId)
                        }
                );

        MatchInfo match = null;

        if (c.moveToFirst()) {

            match =
                    matchFromCursor(c);
        }

        c.close();

        return match;
    }

    // ============================================================
    // MATCH CURSOR
    // ============================================================

    private MatchInfo matchFromCursor(
            Cursor c
    ) {

        MatchInfo match =
                new MatchInfo();

        match.setId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "id"
                        )
                )
        );

        match.setTeamAId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "team_a_id"
                        )
                )
        );

        match.setTeamBId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "team_b_id"
                        )
                )
        );

        int teamANameIndex =
                c.getColumnIndex("team_a_name");

        if (teamANameIndex >= 0) {

            match.setTeamAName(
                    c.getString(
                            teamANameIndex
                    )
            );
        }

        int teamBNameIndex =
                c.getColumnIndex("team_b_name");

        if (teamBNameIndex >= 0) {

            match.setTeamBName(
                    c.getString(
                            teamBNameIndex
                    )
            );
        }

        match.setTotalOvers(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "total_overs"
                        )
                )
        );

        match.setMatchFormat(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "match_format"
                        )
                )
        );

        match.setTossWinnerTeamId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "toss_winner_team_id"
                        )
                )
        );

        match.setTossDecision(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "toss_decision"
                        )
                )
        );

        match.setBattingTeamId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "batting_team_id"
                        )
                )
        );

        match.setBowlingTeamId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "bowling_team_id"
                        )
                )
        );

        int i1Idx = c.getColumnIndex("innings1_team_id");
        if (i1Idx >= 0 && !c.isNull(i1Idx)) {
            match.setInningsTeamId(1, c.getLong(i1Idx));
        } else {
            match.setInningsTeamId(1, match.getBattingTeamId());
        }

        int i2Idx = c.getColumnIndex("innings2_team_id");
        if (i2Idx >= 0 && !c.isNull(i2Idx)) {
            match.setInningsTeamId(2, c.getLong(i2Idx));
        } else {
            match.setInningsTeamId(2, match.getOppositionTeamId(match.getBattingTeamId()));
        }

        // --------------------------------------------------------
        // Innings 1
        // --------------------------------------------------------

        match.setInn1Runs(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn1_runs"
                        )
                )
        );

        match.setInn1Wickets(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn1_wickets"
                        )
                )
        );

        match.setInn1Balls(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn1_balls"
                        )
                )
        );

        // --------------------------------------------------------
        // Innings 2
        // --------------------------------------------------------

        match.setInn2Runs(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn2_runs"
                        )
                )
        );

        match.setInn2Wickets(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn2_wickets"
                        )
                )
        );

        match.setInn2Balls(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn2_balls"
                        )
                )
        );

        // --------------------------------------------------------
        // Innings 3
        // --------------------------------------------------------

        match.setInn3Runs(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn3_runs"
                        )
                )
        );

        match.setInn3Wickets(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn3_wickets"
                        )
                )
        );

        match.setInn3Balls(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn3_balls"
                        )
                )
        );

        // --------------------------------------------------------
        // Innings 4
        // --------------------------------------------------------

        match.setInn4Runs(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn4_runs"
                        )
                )
        );

        match.setInn4Wickets(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn4_wickets"
                        )
                )
        );

        match.setInn4Balls(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "inn4_balls"
                        )
                )
        );

        // --------------------------------------------------------
        // Current state
        // --------------------------------------------------------

        match.setCurrentInnings(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "current_innings"
                        )
                )
        );

        match.setCompleted(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "completed"
                        )
                ) == 1
        );

        match.setResultText(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "result_text"
                        )
                )
        );

        match.setPlayerOfMatchId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "player_of_match_id"
                        )
                )
        );

        int pomNameIndex =
                c.getColumnIndex("pom_name");

        if (pomNameIndex >= 0) {

            match.setPlayerOfMatchName(
                    c.getString(
                            pomNameIndex
                    )
            );
        }

        match.setMatchDate(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "match_date"
                        )
                )
        );

        // --------------------------------------------------------
        // Test state
        // --------------------------------------------------------

        match.setCurrentDay(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "current_day"
                        )
                )
        );

        int sessionIndex =
                c.getColumnIndex("current_session");

        if (
                sessionIndex >= 0 &&
                        !c.isNull(sessionIndex)
        ) {

            String sessionValue =
                    c.getString(sessionIndex);

            int sessionNumber = 1;

            /*
             * Older database versions stored the session as text:
             * Morning / Afternoon / Evening.
             *
             * MatchInfo now stores the session as an integer.
             * Accept both old text values and new numeric values.
             */
            if (sessionValue != null) {

                String normalized =
                        sessionValue.trim();

                try {

                    sessionNumber =
                            Integer.parseInt(
                                    normalized
                            );

                } catch (NumberFormatException e) {

                    switch (
                            normalized.toLowerCase()
                    ) {

                        case "afternoon":
                            sessionNumber = 2;
                            break;

                        case "evening":
                            sessionNumber = 3;
                            break;

                        case "night":
                            sessionNumber = 4;
                            break;

                        case "morning":
                        default:
                            sessionNumber = 1;
                            break;
                    }
                }
            }

            match.setCurrentSession(
                    sessionNumber
            );
        }

        match.setDeclared(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "declared"
                        )
                ) == 1
        );

        match.setFollowOn(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "follow_on"
                        )
                ) == 1
        );

        int completedIdx = c.getColumnIndex("completed");
        if (completedIdx >= 0 && !c.isNull(completedIdx)) {
            match.setCompleted(c.getInt(completedIdx) == 1);
        }

        int pausedIdx = c.getColumnIndex("is_paused");
        if (pausedIdx >= 0 && !c.isNull(pausedIdx)) {
            match.setPaused(c.getInt(pausedIdx) == 1);
        }

        int stIdx = c.getColumnIndex("striker_id");
        if (stIdx >= 0 && !c.isNull(stIdx)) match.setStrikerId(c.getLong(stIdx));

        int nsIdx = c.getColumnIndex("non_striker_id");
        if (nsIdx >= 0 && !c.isNull(nsIdx)) match.setNonStrikerId(c.getLong(nsIdx));

        int bwIdx = c.getColumnIndex("bowler_id");
        if (bwIdx >= 0 && !c.isNull(bwIdx)) match.setBowlerId(c.getLong(bwIdx));

        int pbIdx = c.getColumnIndex("previous_bowler_id");
        if (pbIdx >= 0 && !c.isNull(pbIdx)) match.setPreviousBowlerId(c.getLong(pbIdx));

        int lbIdx = c.getColumnIndex("legal_balls_this_over");
        if (lbIdx >= 0 && !c.isNull(lbIdx)) match.setLegalBallsThisOver(c.getInt(lbIdx));

        return match;
    }

    // ============================================================
    // UPDATE LIVE MATCH STATE
    // ============================================================

    public void updateMatchLiveState(
            MatchInfo match
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "inn1_runs",
                match.getInn1Runs()
        );

        values.put(
                "inn1_wickets",
                match.getInn1Wickets()
        );

        values.put(
                "inn1_balls",
                match.getInn1Balls()
        );

        values.put(
                "inn2_runs",
                match.getInn2Runs()
        );

        values.put(
                "inn2_wickets",
                match.getInn2Wickets()
        );

        values.put(
                "inn2_balls",
                match.getInn2Balls()
        );

        values.put(
                "inn3_runs",
                match.getInn3Runs()
        );

        values.put(
                "inn3_wickets",
                match.getInn3Wickets()
        );

        values.put(
                "inn3_balls",
                match.getInn3Balls()
        );

        values.put(
                "inn4_runs",
                match.getInn4Runs()
        );

        values.put(
                "inn4_wickets",
                match.getInn4Wickets()
        );

        values.put(
                "inn4_balls",
                match.getInn4Balls()
        );

        values.put(
                "current_innings",
                match.getCurrentInnings()
        );

        values.put(
                "current_day",
                match.getCurrentDay()
        );

        values.put(
                "current_session",
                match.getCurrentSession()
        );

        values.put(
                "declared",
                match.isDeclared() ? 1 : 0
        );

        values.put(
                "follow_on",
                match.isFollowOn() ? 1 : 0
        );

        values.put(
                "completed",
                match.isCompleted() ? 1 : 0
        );

        values.put(
                "is_paused",
                match.isPaused() ? 1 : 0
        );

        values.put("striker_id", match.getStrikerId());
        values.put("non_striker_id", match.getNonStrikerId());
        values.put("bowler_id", match.getBowlerId());
        values.put("previous_bowler_id", match.getPreviousBowlerId());
        values.put("legal_balls_this_over", match.getLegalBallsThisOver());

        values.put(
                "is_paused",
                match.isPaused() ? 1 : 0
        );

        values.put("striker_id", match.getStrikerId());
        values.put("non_striker_id", match.getNonStrikerId());
        values.put("bowler_id", match.getBowlerId());
        values.put("previous_bowler_id", match.getPreviousBowlerId());
        values.put("legal_balls_this_over", match.getLegalBallsThisOver());

        db.update(
                TABLE_MATCH,
                values,
                "id=?",
                new String[]{
                        String.valueOf(
                                match.getId()
                        )
                }
        );
    }

    // ============================================================
    // COMPLETE MATCH
    // ============================================================

    public void completeMatch(
            MatchInfo match
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        db.beginTransaction();

        try {

            ContentValues values =
                    new ContentValues();

            values.put(
                    "completed",
                    1
            );

            values.put(
                    "inn1_runs",
                    match.getInn1Runs()
            );

            values.put(
                    "inn1_wickets",
                    match.getInn1Wickets()
            );

            values.put(
                    "inn1_balls",
                    match.getInn1Balls()
            );

            values.put(
                    "inn2_runs",
                    match.getInn2Runs()
            );

            values.put(
                    "inn2_wickets",
                    match.getInn2Wickets()
            );

            values.put(
                    "inn2_balls",
                    match.getInn2Balls()
            );

            values.put(
                    "inn3_runs",
                    match.getInn3Runs()
            );

            values.put(
                    "inn3_wickets",
                    match.getInn3Wickets()
            );

            values.put(
                    "inn3_balls",
                    match.getInn3Balls()
            );

            values.put(
                    "inn4_runs",
                    match.getInn4Runs()
            );

            values.put(
                    "inn4_wickets",
                    match.getInn4Wickets()
            );

            values.put(
                    "inn4_balls",
                    match.getInn4Balls()
            );

            values.put(
                    "current_innings",
                    match.getCurrentInnings()
            );

            values.put(
                    "result_text",
                    match.getResultText()
            );

            values.put(
                    "player_of_match_id",
                    match.getPlayerOfMatchId()
            );

            values.put(
                    "match_date",
                    match.getMatchDate()
            );

            values.put(
                    "current_day",
                    match.getCurrentDay()
            );

            values.put(
                    "current_session",
                    match.getCurrentSession()
            );

            values.put(
                    "declared",
                    match.isDeclared() ? 1 : 0
            );

            values.put(
                    "follow_on",
                    match.isFollowOn() ? 1 : 0
            );

            db.update(
                    TABLE_MATCH,
                    values,
                    "id=?",
                    new String[]{
                            String.valueOf(
                                    match.getId()
                            )
                    }
            );

            /*
             * Recalculate career totals from all completed
             * match records.
             */
            recalculateCareerStatistics(
                    db
            );

            db.setTransactionSuccessful();

        } finally {

            db.endTransaction();
        }
    }

    // ============================================================
    // SET PLAYER OF THE MATCH
    // ============================================================

    /**
     * Stores the Player of the Match for a completed match.
     * A value of 0 clears the award.
     */
    public void setPlayerOfMatch(
            long matchId,
            long playerId
    ) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("player_of_match_id", playerId);

        db.update(
                TABLE_MATCH,
                values,
                "id=?",
                new String[]{
                        String.valueOf(matchId)
                }
        );
    }

    // ============================================================
    // GET COMPLETED MATCHES
    // ============================================================

    public List<MatchInfo> getCompletedMatches() {

        List<MatchInfo> list =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.rawQuery(
                        "SELECT m.*, " +

                                "ta.name AS team_a_name, " +

                                "tb.name AS team_b_name, " +

                                "pom.name AS pom_name " +

                                "FROM " +
                                TABLE_MATCH +
                                " m " +

                                "LEFT JOIN " +
                                TABLE_TEAM +
                                " ta ON " +
                                "m.team_a_id=ta.id " +

                                "LEFT JOIN " +
                                TABLE_TEAM +
                                " tb ON " +
                                "m.team_b_id=tb.id " +

                                "LEFT JOIN " +
                                TABLE_PLAYER +
                                " pom ON " +
                                "m.player_of_match_id=pom.id " +

                                "WHERE m.completed=1 " +

                                "ORDER BY m.id DESC",
                        null
                );

        while (c.moveToNext()) {

            list.add(
                    matchFromCursor(c)
            );
        }

        c.close();

        return list;
    }

    // ============================================================
    // BATTING STATISTICS
    // ============================================================

    private void ensureBattingTableColumns(SQLiteDatabase db) {
        addColumnIfMissing(db, TABLE_BATTING, "bowler_id", "INTEGER DEFAULT 0");
        addColumnIfMissing(db, TABLE_BATTING, "bowler_name", "TEXT");
        addColumnIfMissing(db, TABLE_BATTING, "fielder_id", "INTEGER DEFAULT 0");
        addColumnIfMissing(db, TABLE_BATTING, "fielder_name", "TEXT");
    }

    private void ensureBowlingTableColumns(SQLiteDatabase db) {
        addColumnIfMissing(db, TABLE_BOWLING, "maidens", "INTEGER DEFAULT 0");
        addColumnIfMissing(db, TABLE_BOWLING, "dot_balls", "INTEGER DEFAULT 0");
    }

    public void saveBattingStat(
            BattingStat batting
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ensureBattingTableColumns(db);

        ContentValues values =
                new ContentValues();

        values.put(
                "match_id",
                batting.getMatchId()
        );

        values.put(
                "innings",
                batting.getInnings()
        );

        values.put(
                "player_id",
                batting.getPlayerId()
        );

        values.put(
                "runs",
                batting.getRuns()
        );

        values.put(
                "balls",
                batting.getBalls()
        );

        values.put(
                "fours",
                batting.getFours()
        );

        values.put(
                "sixes",
                batting.getSixes()
        );

        values.put(
                "is_out",
                batting.isOut() ? 1 : 0
        );

        values.put(
                "how_out",
                batting.getHowOut()
        );

        values.put(
                "fielder_id",
                batting.getFielderId()
        );

        values.put(
                "bowler_id",
                batting.getBowlerId()
        );

        values.put(
                "bowler_name",
                batting.getBowlerName()
        );

        values.put(
                "fielder_name",
                batting.getFielderName()
        );

        try {
            Cursor existing = null;
            try {
                existing =
                        db.query(
                                TABLE_BATTING,
                                new String[]{"id"},
                                "match_id=? AND player_id=?",
                                new String[]{
                                        String.valueOf(
                                                batting.getMatchId()
                                        ),
                                        String.valueOf(
                                                batting.getPlayerId()
                                        )
                                },
                                null,
                                null,
                                null
                        );

                if (existing.moveToFirst()) {

                    long id =
                            existing.getLong(0);

                    db.update(
                            TABLE_BATTING,
                            values,
                            "id=?",
                            new String[]{
                                    String.valueOf(id)
                            }
                    );

                } else {

                    db.insertOrThrow(
                            TABLE_BATTING,
                            null,
                            values
                    );
                }
            } finally {
                if (existing != null) {
                    existing.close();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("CRIC_DB_ERROR", "Error saving batting stat: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // REPLACE BATTING STATS FOR ONE INNINGS
    // ============================================================

    /**
     * Makes the database batting rows exactly match the supplied
     * in-memory snapshot for one match innings.
     *
     * This is intentionally a DELETE + INSERT operation.  It is used
     * by Undo because an undone delivery may have created a brand-new
     * player row (for example the first ball faced by a batter).  A
     * normal UPDATE/INSERT cannot remove such a row.
     */
    public void replaceBattingStatsForInnings(
            long matchId,
            int innings,
            List<BattingStat> stats
    ) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            db.delete(
                    TABLE_BATTING,
                    "match_id=? AND innings=?",
                    new String[]{
                            String.valueOf(matchId),
                            String.valueOf(innings)
                    }
            );

            if (stats != null) {

                for (BattingStat stat : stats) {

                    if (stat == null) {
                        continue;
                    }

                    if (stat.getMatchId() <= 0) {
                        stat.setMatchId(matchId);
                    }
                    if (stat.getMatchId() != matchId) {
                        continue;
                    }
                    if (stat.getInnings() <= 0) {
                        stat.setInnings(innings);
                    }

                    ContentValues values = new ContentValues();

                    values.put("match_id", matchId);
                    values.put("innings", innings);
                    values.put("player_id", stat.getPlayerId());
                    values.put("runs", stat.getRuns());
                    values.put("balls", stat.getBalls());
                    values.put("fours", stat.getFours());
                    values.put("sixes", stat.getSixes());
                    values.put("is_out", stat.isOut() ? 1 : 0);
                    values.put("how_out", stat.getHowOut());
                    values.put("fielder_id", stat.getFielderId());
                    values.put("bowler_id", stat.getBowlerId());
                    values.put("bowler_name", stat.getBowlerName());
                    values.put("fielder_name", stat.getFielderName());

                    db.insertOrThrow(
                            TABLE_BATTING,
                            null,
                            values
                    );
                }
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    // ============================================================
    // GET BATTING STATS
    // ============================================================

    public List<BattingStat> getBattingStats(
            long matchId,
            int innings
    ) {

        android.util.Log.d("CRIC_DEBUG", "Database queried with MatchID: " + matchId + ", Innings: " + innings);

        Map<Long, BattingStat> uniqueMap = new LinkedHashMap<>();
        SQLiteDatabase db = getReadableDatabase();

        MatchInfo match = getMatchById(matchId);
        long targetBattingTeamId = match != null ? match.getInningsTeamId(innings) : -1;

        Cursor c =
                db.rawQuery(
                        "SELECT b.*, " +
                                "p.name AS player_name, " +
                                "p.team_id AS player_team_id " +

                                "FROM " +
                                TABLE_BATTING +
                                " b " +

                                "LEFT JOIN " +
                                TABLE_PLAYER +
                                " p ON " +
                                "b.player_id=p.id " +

                                "WHERE b.match_id=? " +

                                "ORDER BY b.id ASC",

                        new String[]{
                                String.valueOf(matchId)
                        }
                );

        List<BattingStat> allStats = new ArrayList<>();
        while (c.moveToNext()) {
            allStats.add(battingFromCursor(c));
        }
        c.close();

        android.util.Log.d("CRIC_DEBUG", "Total raw rows found in database for this match: " + allStats.size());

        List<BattingStat> candidateList = new ArrayList<>();

        if (targetBattingTeamId > 0) {
            for (BattingStat stat : allStats) {
                Player p = getPlayerById(stat.getPlayerId());
                if (p != null && p.getTeamId() == targetBattingTeamId) {
                    candidateList.add(stat);
                }
            }
        }

        if (candidateList.isEmpty()) {
            for (BattingStat stat : allStats) {
                if (innings == 1 && (stat.getInnings() == 1 || stat.getInnings() <= 0)) {
                    candidateList.add(stat);
                } else if (innings == 2 && (stat.getInnings() == 2 || stat.getInnings() >= 2)) {
                    candidateList.add(stat);
                }
            }
        }

        if (candidateList.isEmpty()) {
            candidateList.addAll(allStats);
        }

        for (BattingStat stat : candidateList) {
            long pid = stat.getPlayerId();
            if (pid > 0) {
                if (!uniqueMap.containsKey(pid) || stat.getRuns() >= uniqueMap.get(pid).getRuns() || stat.getBalls() >= uniqueMap.get(pid).getBalls()) {
                    uniqueMap.put(pid, stat);
                }
            } else {
                uniqueMap.put((long) uniqueMap.size() + 1000, stat);
            }
        }

        return new ArrayList<>(uniqueMap.values());
    }

    // ============================================================
    // BATTING CURSOR
    // ============================================================

    private BattingStat battingFromCursor(
            Cursor c
    ) {

        BattingStat stat =
                new BattingStat();

        stat.setId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "id"
                        )
                )
        );

        stat.setMatchId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "match_id"
                        )
                )
        );

        stat.setInnings(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "innings"
                        )
                )
        );

        stat.setPlayerId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "player_id"
                        )
                )
        );

        int nameIndex =
                c.getColumnIndex(
                        "player_name"
                );

        if (nameIndex >= 0) {

            stat.setPlayerName(
                    c.getString(nameIndex)
            );
        }

        stat.setRuns(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "runs"
                        )
                )
        );

        stat.setBalls(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "balls"
                        )
                )
        );

        stat.setFours(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "fours"
                        )
                )
        );

        stat.setSixes(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "sixes"
                        )
                )
        );

        stat.setOut(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "is_out"
                        )
                ) == 1
        );

        stat.setHowOut(
                c.getString(
                        c.getColumnIndexOrThrow(
                                "how_out"
                        )
                )
        );

        int fielderIdx = c.getColumnIndex("fielder_id");
        if (fielderIdx >= 0 && !c.isNull(fielderIdx)) {
            stat.setFielderId(c.getLong(fielderIdx));
        }
        int bowlerIdIdx = c.getColumnIndex("bowler_id");
        if (bowlerIdIdx >= 0 && !c.isNull(bowlerIdIdx)) {
            stat.setBowlerId(c.getLong(bowlerIdIdx));
        }
        int bowlerNameIdx = c.getColumnIndex("bowler_name");
        if (bowlerNameIdx >= 0 && !c.isNull(bowlerNameIdx)) {
            stat.setBowlerName(c.getString(bowlerNameIdx));
        }
        int fielderNameIdx = c.getColumnIndex("fielder_name");
        if (fielderNameIdx >= 0 && !c.isNull(fielderNameIdx)) {
            stat.setFielderName(c.getString(fielderNameIdx));
        }

        return stat;
    }

    // ============================================================
    // BOWLING STATISTICS
    // ============================================================

    public void saveBowlingStat(
            BowlingStat bowling
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ensureBowlingTableColumns(db);

        ContentValues values =
                new ContentValues();

        values.put(
                "match_id",
                bowling.getMatchId()
        );

        values.put(
                "innings",
                bowling.getInnings()
        );

        values.put(
                "player_id",
                bowling.getPlayerId()
        );

        values.put(
                "balls",
                bowling.getBalls()
        );

        values.put(
                "runs_conceded",
                bowling.getRunsConceded()
        );

        values.put(
                "wickets",
                bowling.getWickets()
        );

        values.put(
                "maidens",
                bowling.getMaidens()
        );

        values.put(
                "dot_balls",
                bowling.getDotBalls()
        );

        values.put(
                "extras_conceded",
                bowling.getExtrasConceded()
        );

        Cursor existing = null;
        try {
            existing =
                    db.query(
                            TABLE_BOWLING,
                            new String[]{"id"},
                            "match_id=? AND player_id=?",
                            new String[]{
                                    String.valueOf(
                                            bowling.getMatchId()
                                    ),
                                    String.valueOf(
                                            bowling.getPlayerId()
                                    )
                            },
                            null,
                            null,
                            null
                    );

            if (existing.moveToFirst()) {

                long id =
                        existing.getLong(0);

                db.update(
                        TABLE_BOWLING,
                        values,
                        "id=?",
                        new String[]{
                                String.valueOf(id)
                        }
                );

            } else {

                db.insertOrThrow(
                        TABLE_BOWLING,
                        null,
                        values
                );
            }
        } catch (Exception e) {
            android.util.Log.e("CRIC_DB_ERROR", "Error saving bowling stat: " + e.getMessage(), e);
            try {
                db.insert(TABLE_BOWLING, null, values);
            } catch (Exception ignored) {}
        } finally {
            if (existing != null) {
                existing.close();
            }
        }
    }

    // ============================================================
    // REPLACE BOWLING STATS FOR ONE INNINGS
    // ============================================================

    /**
     * Makes the database bowling rows exactly match the supplied
     * in-memory snapshot for one match innings.
     *
     * Required by Undo so that an undone wicket/ball/run cannot leave
     * behind a stale bowler record in SQLite.
     */
    public void replaceBowlingStatsForInnings(
            long matchId,
            int innings,
            List<BowlingStat> stats
    ) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            db.delete(
                    TABLE_BOWLING,
                    "match_id=? AND innings=?",
                    new String[]{
                            String.valueOf(matchId),
                            String.valueOf(innings)
                    }
            );

            if (stats != null) {

                for (BowlingStat stat : stats) {

                    if (stat == null) {
                        continue;
                    }

                    if (stat.getMatchId() <= 0) {
                        stat.setMatchId(matchId);
                    }
                    if (stat.getMatchId() != matchId) {
                        continue;
                    }
                    if (stat.getInnings() <= 0) {
                        stat.setInnings(innings);
                    }

                    ContentValues values = new ContentValues();

                    values.put("match_id", matchId);
                    values.put("innings", innings);
                    values.put("player_id", stat.getPlayerId());
                    values.put("balls", stat.getBalls());
                    values.put("runs_conceded", stat.getRunsConceded());
                    values.put("wickets", stat.getWickets());
                    values.put("maidens", stat.getMaidens());
                    values.put("dot_balls", stat.getDotBalls());
                    values.put("extras_conceded", stat.getExtrasConceded());

                    db.insertOrThrow(
                            TABLE_BOWLING,
                            null,
                            values
                    );
                }
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    // ============================================================
    // GET BOWLING STATS
    // ============================================================

    public List<BowlingStat> getBowlingStats(
            long matchId,
            int innings
    ) {

        Map<Long, BowlingStat> uniqueMap = new LinkedHashMap<>();
        SQLiteDatabase db = getReadableDatabase();

        MatchInfo match = getMatchById(matchId);
        long battingTeamId = match != null ? match.getInningsTeamId(innings) : -1;
        long targetBowlingTeamId = match != null ? match.getOppositionTeamId(battingTeamId) : -1;

        Cursor c =
                db.rawQuery(
                        "SELECT b.*, " +
                                "p.name AS player_name, " +
                                "p.team_id AS player_team_id " +

                                "FROM " +
                                TABLE_BOWLING +
                                " b " +

                                "LEFT JOIN " +
                                TABLE_PLAYER +
                                " p ON " +
                                "b.player_id=p.id " +

                                "WHERE b.match_id=? " +

                                "ORDER BY b.id ASC",

                        new String[]{
                                String.valueOf(matchId)
                        }
                );

        List<BowlingStat> allStats = new ArrayList<>();
        while (c.moveToNext()) {
            allStats.add(bowlingFromCursor(c));
        }
        c.close();

        List<BowlingStat> candidateList = new ArrayList<>();
        if (targetBowlingTeamId > 0) {
            for (BowlingStat stat : allStats) {
                Player p = getPlayerById(stat.getPlayerId());
                if (p != null && p.getTeamId() == targetBowlingTeamId) {
                    candidateList.add(stat);
                }
            }
        }

        if (candidateList.isEmpty()) {
            for (BowlingStat stat : allStats) {
                if (innings == 1 && (stat.getInnings() <= 1)) {
                    candidateList.add(stat);
                } else if (innings == 2 && (stat.getInnings() >= 2)) {
                    candidateList.add(stat);
                }
            }
        }

        if (candidateList.isEmpty()) {
            candidateList.addAll(allStats);
        }

        for (BowlingStat stat : candidateList) {
            long pid = stat.getPlayerId();
            if (pid > 0) {
                if (!uniqueMap.containsKey(pid) || stat.getBalls() >= uniqueMap.get(pid).getBalls()) {
                    uniqueMap.put(pid, stat);
                }
            } else {
                uniqueMap.put((long) uniqueMap.size() + 1000, stat);
            }
        }

        return new ArrayList<>(uniqueMap.values());
    }

    // ============================================================
    // BOWLING CURSOR
    // ============================================================

    private BowlingStat bowlingFromCursor(
            Cursor c
    ) {

        BowlingStat stat =
                new BowlingStat();

        stat.setId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "id"
                        )
                )
        );

        stat.setMatchId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "match_id"
                        )
                )
        );

        stat.setInnings(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "innings"
                        )
                )
        );

        stat.setPlayerId(
                c.getLong(
                        c.getColumnIndexOrThrow(
                                "player_id"
                        )
                )
        );

        int nameIndex =
                c.getColumnIndex(
                        "player_name"
                );

        if (nameIndex >= 0) {

            stat.setPlayerName(
                    c.getString(nameIndex)
            );
        }

        stat.setBalls(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "balls"
                        )
                )
        );

        stat.setRunsConceded(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "runs_conceded"
                        )
                )
        );

        stat.setWickets(
                c.getInt(
                        c.getColumnIndexOrThrow(
                                "wickets"
                        )
                )
        );

        int maidensIndex =
                c.getColumnIndex(
                        "maidens"
                );

        if (maidensIndex >= 0) {

            stat.setMaidens(
                    c.getInt(maidensIndex)
            );
        }

        int dotIndex = c.getColumnIndex("dot_balls");
        if (dotIndex >= 0 && !c.isNull(dotIndex)) {
            stat.setDotBalls(c.getInt(dotIndex));
        }

        return stat;
    }

    // ============================================================
    // ALL BATTING RECORDS FOR A MATCH
    // ============================================================

    public List<BattingStat> getBattingForMatch(long matchId) {

        List<BattingStat> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT b.*, p.name AS player_name " +
                        "FROM " + TABLE_BATTING + " b " +
                        "LEFT JOIN " + TABLE_PLAYER + " p ON b.player_id=p.id " +
                        "WHERE b.match_id=? ORDER BY b.innings ASC, b.id ASC",
                new String[]{String.valueOf(matchId)}
        );

        while (c.moveToNext()) {
            list.add(battingFromCursor(c));
        }
        c.close();
        return list;
    }

    // ============================================================
    // ALL BOWLING RECORDS FOR A MATCH
    // ============================================================

    public List<BowlingStat> getBowlingForMatch(long matchId) {

        List<BowlingStat> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT b.*, p.name AS player_name " +
                        "FROM " + TABLE_BOWLING + " b " +
                        "LEFT JOIN " + TABLE_PLAYER + " p ON b.player_id=p.id " +
                        "WHERE b.match_id=? ORDER BY b.innings ASC, b.id ASC",
                new String[]{String.valueOf(matchId)}
        );

        while (c.moveToNext()) {
            list.add(bowlingFromCursor(c));
        }
        c.close();
        return list;
    }

    // ============================================================
    // FORMAT NORMALIZER
    // ============================================================

    private String normalizeFormat(
            String format
    ) {

        if (
                format == null ||
                        format.trim().isEmpty()
        ) {

            return "T20";
        }

        String value =
                format.trim()
                        .toUpperCase();

        if (
                value.equals("TEST MATCH") ||
                        value.equals("TESTMATCH") ||
                        value.equals("TEST")
        ) {

            return "TEST";
        }

        if (value.equals("ONE DAY")) {
            return "ODI";
        }

        if (value.equals("T20I")) {
            return "T20";
        }

        return value;
    }

    // ============================================================
    // FORMAT-SPECIFIC BATTING RECORDS
    // ============================================================

    public List<BattingStat> getPlayerBattingByFormat(
            long playerId,
            String format
    ) {

        List<BattingStat> list =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.rawQuery(
                        "SELECT b.*, " +
                                "p.name AS player_name " +

                                "FROM " +
                                TABLE_BATTING +
                                " b " +

                                "JOIN " +
                                TABLE_MATCH +
                                " m ON " +
                                "b.match_id=m.id " +

                                "LEFT JOIN " +
                                TABLE_PLAYER +
                                " p ON " +
                                "b.player_id=p.id " +

                                "WHERE b.player_id=? " +

                                "AND UPPER(m.match_format)=? " +

                                "AND m.completed=1 " +

                                "ORDER BY b.runs DESC",

                        new String[]{
                                String.valueOf(playerId),
                                normalizeFormat(format)
                        }
                );

        while (c.moveToNext()) {

            list.add(
                    battingFromCursor(c)
            );
        }

        c.close();

        return list;
    }

    // ============================================================
    // FORMAT-SPECIFIC BOWLING RECORDS
    // ============================================================

    public List<BowlingStat> getPlayerBowlingByFormat(
            long playerId,
            String format
    ) {

        List<BowlingStat> list =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor c =
                db.rawQuery(
                        "SELECT b.*, " +
                                "p.name AS player_name " +

                                "FROM " +
                                TABLE_BOWLING +
                                " b " +

                                "JOIN " +
                                TABLE_MATCH +
                                " m ON " +
                                "b.match_id=m.id " +

                                "LEFT JOIN " +
                                TABLE_PLAYER +
                                " p ON " +
                                "b.player_id=p.id " +

                                "WHERE b.player_id=? " +

                                "AND UPPER(m.match_format)=? " +

                                "AND m.completed=1 " +

                                "ORDER BY b.wickets DESC, " +
                                "b.runs_conceded ASC",

                        new String[]{
                                String.valueOf(playerId),
                                normalizeFormat(format)
                        }
                );

        while (c.moveToNext()) {

            list.add(
                    bowlingFromCursor(c)
            );
        }

        c.close();

        return list;
    }

    // ============================================================
    // COUNT FIFTIES
    // ============================================================

    public int countFifties(
            long playerId,
            String format
    ) {

        int count = 0;

        List<BattingStat> stats =
                getPlayerBattingByFormat(
                        playerId,
                        format
                );

        for (
                BattingStat stat :
                stats
        ) {

            if (
                    stat.getRuns() >= 50 &&
                            stat.getRuns() < 100
            ) {

                count++;
            }
        }

        return count;
    }

    // ============================================================
    // COUNT CENTURIES
    // ============================================================

    public int countCenturies(
            long playerId,
            String format
    ) {

        int count = 0;

        List<BattingStat> stats =
                getPlayerBattingByFormat(
                        playerId,
                        format
                );

        for (
                BattingStat stat :
                stats
        ) {

            if (stat.getRuns() >= 100) {

                count++;
            }
        }

        return count;
    }

    // ============================================================
    // HIGHEST SCORE
    // ============================================================

    public int getHighestScore(
            long playerId,
            String format
    ) {

        int highest = 0;

        List<BattingStat> stats =
                getPlayerBattingByFormat(
                        playerId,
                        format
                );

        for (
                BattingStat stat :
                stats
        ) {

            if (
                    stat.getRuns() >
                            highest
            ) {

                highest =
                        stat.getRuns();
            }
        }

        return highest;
    }

    // ============================================================
    // FIVE-WICKET HAULS
    // ============================================================

    public int countFiveWicketHauls(
            long playerId,
            String format
    ) {

        int count = 0;

        List<BowlingStat> stats =
                getPlayerBowlingByFormat(
                        playerId,
                        format
                );

        for (
                BowlingStat stat :
                stats
        ) {

            if (
                    stat.getWickets() >= 5
            ) {

                count++;
            }
        }

        return count;
    }

    // ============================================================
    // BEST BOWLING FIGURES
    // ============================================================

    /*
     * Returns:
     *
     * result[0] = wickets
     * result[1] = runs conceded
     *
     * Example:
     *
     * 7 / 24
     */
    public int[] getBestBowlingFigures(
            long playerId,
            String format
    ) {

        int bestWickets = 0;

        int bestRuns = 0;

        List<BowlingStat> stats =
                getPlayerBowlingByFormat(
                        playerId,
                        format
                );

        for (
                BowlingStat stat :
                stats
        ) {

            int wickets =
                    stat.getWickets();

            int runs =
                    stat.getRunsConceded();

            if (
                    wickets > bestWickets
            ) {

                bestWickets =
                        wickets;

                bestRuns =
                        runs;

            } else if (
                    wickets == bestWickets &&
                            wickets > 0 &&
                            runs < bestRuns
            ) {

                bestRuns =
                        runs;
            }
        }

        return new int[]{
                bestWickets,
                bestRuns
        };
    }

    // ============================================================
    // RECALCULATE CAREER STATISTICS
    // ============================================================

    public void recalculateCareerStatistics() {
        SQLiteDatabase db = getWritableDatabase();
        recalculateCareerStatistics(db);
    }

    private void recalculateCareerStatistics(
            SQLiteDatabase db
    ) {

        ensureBattingTableColumns(db);
        ensureBowlingTableColumns(db);

        /*
         * First clear all calculated career values.
         */
        resetCareerStatisticsInternal(db);

        // ========================================================
        // 1. MATCHES PLAYED (Distinct completed matches in Batting OR Bowling)
        // ========================================================

        Cursor matchCount = db.rawQuery(
                "SELECT player_id, COUNT(DISTINCT match_id) FROM (" +
                        "SELECT b.match_id, b.player_id FROM " + TABLE_BATTING + " b JOIN " + TABLE_MATCH + " m ON b.match_id=m.id WHERE m.completed=1 " +
                        "UNION " +
                        "SELECT bw.match_id, bw.player_id FROM " + TABLE_BOWLING + " bw JOIN " + TABLE_MATCH + " m ON bw.match_id=m.id WHERE m.completed=1 " +
                        ") GROUP BY player_id",
                null
        );

        while (matchCount.moveToNext()) {
            long pid = matchCount.getLong(0);
            int mCount = matchCount.getInt(1);
            ContentValues values = new ContentValues();
            values.put("matches", mCount);
            db.update(TABLE_PLAYER, values, "id=?", new String[]{String.valueOf(pid)});
        }
        matchCount.close();

        // ========================================================
        // 2. BATTING INNINGS & BATTING CAREER STATS (Completed matches only)
        // ========================================================

        Cursor batting =
                db.rawQuery(
                        "SELECT " +
                                "player_id, " +
                                "COUNT(CASE WHEN balls > 0 OR runs > 0 OR is_out = 1 OR LOWER(how_out) = 'retired hurt' THEN 1 END), " +
                                "SUM(runs), " +
                                "SUM(balls), " +
                                "MAX(runs), " +
                                "SUM(fours), " +
                                "SUM(sixes), " +
                                "SUM(CASE WHEN runs=0 AND is_out=1 THEN 1 ELSE 0 END), " +
                                "SUM(is_out), " +
                                "SUM(CASE WHEN is_out=0 AND (how_out IS NULL OR LOWER(how_out) != 'retired hurt') AND (balls > 0 OR runs > 0) THEN 1 ELSE 0 END), " +
                                "SUM(CASE WHEN runs>=30 AND runs<50 THEN 1 ELSE 0 END), " +
                                "SUM(CASE WHEN runs>=50 AND runs<100 THEN 1 ELSE 0 END), " +
                                "SUM(CASE WHEN runs>=100 THEN 1 ELSE 0 END) " +

                                "FROM " + TABLE_BATTING + " b " +
                                "JOIN " + TABLE_MATCH + " m ON b.match_id=m.id " +
                                "WHERE m.completed=1 " +
                                "GROUP BY b.player_id",

                        null
                );

        while (batting.moveToNext()) {

            long playerId =
                    batting.getLong(0);

            ContentValues values =
                    new ContentValues();

            values.put("innings", batting.getInt(1)); // Batting Innings
            values.put("runs", batting.getInt(2));
            values.put("balls", batting.getInt(3));
            values.put("highest_score", batting.getInt(4));
            values.put("fours", batting.getInt(5));
            values.put("sixes", batting.getInt(6));
            values.put("ducks", batting.getInt(7));
            values.put("times_out", batting.getInt(8));
            values.put("not_outs", batting.getInt(9));
            values.put("thirties", batting.getInt(10));
            values.put("fifties", batting.getInt(11));
            values.put("centuries", batting.getInt(12));

            db.update(
                    TABLE_PLAYER,
                    values,
                    "id=?",
                    new String[]{
                            String.valueOf(
                                    playerId
                            )
                    }
            );
        }

        batting.close();

        // ========================================================
        // 3. BOWLING INNINGS & BOWLING CAREER STATS (Completed matches only)
        // ========================================================

        Cursor bowling =
                db.rawQuery(
                        "SELECT " +
                                "player_id, " +
                                "SUM(balls), " +
                                "SUM(wickets), " +
                                "SUM(runs_conceded), " +
                                "SUM(maidens), " +
                                "SUM(dot_balls), " +
                                "SUM(extras_conceded), " +
                                "SUM(CASE WHEN wickets>=3 AND wickets<5 THEN 1 ELSE 0 END), " +
                                "SUM(CASE WHEN wickets>=5 THEN 1 ELSE 0 END) " +

                                "FROM " + TABLE_BOWLING + " b " +
                                "JOIN " + TABLE_MATCH + " m ON b.match_id=m.id " +
                                "WHERE m.completed=1 AND balls > 0 " +
                                "GROUP BY b.player_id",

                        null
                );

        while (bowling.moveToNext()) {

            long playerId =
                    bowling.getLong(0);

            ContentValues values =
                    new ContentValues();

            values.put("balls_bowled", bowling.getInt(1));
            values.put("wickets", bowling.getInt(2));
            values.put("runs_conceded", bowling.getInt(3));
            values.put("maidens", bowling.getInt(4));
            values.put("dot_balls", bowling.getInt(5));
            values.put("extras_conceded", bowling.getInt(6));
            values.put("three_wicket_hauls", bowling.getInt(7));
            values.put("five_wicket_hauls", bowling.getInt(8));

            Cursor bestCur = db.rawQuery(
                    "SELECT wickets, runs_conceded FROM " + TABLE_BOWLING + " b " +
                            "JOIN " + TABLE_MATCH + " m ON b.match_id=m.id " +
                            "WHERE m.completed=1 AND player_id=? AND balls > 0 " +
                            "ORDER BY wickets DESC, runs_conceded ASC LIMIT 1",
                    new String[]{String.valueOf(playerId)}
            );
            if (bestCur.moveToFirst()) {
                values.put("best_bowling", bestCur.getInt(0) + "/" + bestCur.getInt(1));
            }
            bestCur.close();

            db.update(
                    TABLE_PLAYER,
                    values,
                    "id=?",
                    new String[]{
                            String.valueOf(
                                    playerId
                            )
                    }
            );
        }

        bowling.close();

        // ========================================================
        // BOWLING INNINGS (Distinct completed matches in Bowling where balls > 0)
        // ========================================================

        Cursor bowlInningsCur = db.rawQuery(
                "SELECT player_id, COUNT(DISTINCT match_id) " +
                        "FROM " + TABLE_BOWLING + " b " +
                        "JOIN " + TABLE_MATCH + " m ON b.match_id=m.id " +
                        "WHERE m.completed=1 AND balls > 0 " +
                        "GROUP BY player_id",
                null
        );

        while (bowlInningsCur.moveToNext()) {
            long pid = bowlInningsCur.getLong(0);
            int bInnings = bowlInningsCur.getInt(1);
            ContentValues values = new ContentValues();
            values.put("bowling_innings", bInnings);
            db.update(TABLE_PLAYER, values, "id=?", new String[]{String.valueOf(pid)});
        }
        bowlInningsCur.close();

        // ========================================================
        // FIELDING (Completed matches only)
        // ========================================================

        Cursor fielding = db.rawQuery(
                "SELECT fielder_id, how_out, COUNT(*) " +
                        "FROM " + TABLE_BATTING + " b " +
                        "JOIN " + TABLE_MATCH + " m ON b.match_id=m.id " +
                        "WHERE m.completed=1 AND fielder_id > 0 " +
                        "GROUP BY fielder_id, how_out",
                null
        );

        Map<Long, Integer> catchesMap = new HashMap<>();
        Map<Long, Integer> runOutsMap = new HashMap<>();
        Map<Long, Integer> stumpingsMap = new HashMap<>();

        while (fielding.moveToNext()) {
            long fielderId = fielding.getLong(0);
            String howOut = fielding.getString(1);
            int cnt = fielding.getInt(2);
            if ("Caught".equalsIgnoreCase(howOut)) {
                Integer val = catchesMap.get(fielderId);
                int cur = val != null ? val : 0;
                catchesMap.put(fielderId, cur + cnt);
            } else if ("Run Out".equalsIgnoreCase(howOut)) {
                Integer val = runOutsMap.get(fielderId);
                int cur = val != null ? val : 0;
                runOutsMap.put(fielderId, cur + cnt);
            } else if ("Stumped".equalsIgnoreCase(howOut)) {
                Integer val = stumpingsMap.get(fielderId);
                int cur = val != null ? val : 0;
                stumpingsMap.put(fielderId, cur + cnt);
            }
        }
        fielding.close();

        Set<Long> fieldingPlayerIds = new HashSet<>();
        fieldingPlayerIds.addAll(catchesMap.keySet());
        fieldingPlayerIds.addAll(runOutsMap.keySet());
        fieldingPlayerIds.addAll(stumpingsMap.keySet());

        for (long pid : fieldingPlayerIds) {
            ContentValues values = new ContentValues();
            Integer cVal = catchesMap.get(pid);
            Integer rVal = runOutsMap.get(pid);
            Integer sVal = stumpingsMap.get(pid);
            values.put("catches", cVal != null ? cVal : 0);
            values.put("run_outs", rVal != null ? rVal : 0);
            values.put("stumpings", sVal != null ? sVal : 0);
            db.update(TABLE_PLAYER, values, "id=?", new String[]{String.valueOf(pid)});
        }

        // ========================================================
        // MOM COUNT
        // ========================================================

        Cursor momCur = db.rawQuery(
                "SELECT player_of_match_id, COUNT(*) FROM " + TABLE_MATCH + " " +
                        "WHERE completed=1 AND player_of_match_id > 0 GROUP BY player_of_match_id",
                null
        );
        while (momCur.moveToNext()) {
            long pid = momCur.getLong(0);
            int mom = momCur.getInt(1);
            ContentValues values = new ContentValues();
            values.put("mom_count", mom);
            db.update(TABLE_PLAYER, values, "id=?", new String[]{String.valueOf(pid)});
        }
        momCur.close();

        // ========================================================
        // MATCH PARTICIPATION
        // ========================================================

        Cursor matchCounts = db.rawQuery(
                "SELECT player_id, COUNT(DISTINCT match_id) " +
                        "FROM (" +
                        "SELECT b.player_id, b.match_id " +
                        "FROM " + TABLE_BATTING + " b " +
                        "JOIN " + TABLE_MATCH + " m ON b.match_id=m.id " +
                        "WHERE m.completed=1 " +
                        "UNION " +
                        "SELECT b.player_id, b.match_id " +
                        "FROM " + TABLE_BOWLING + " b " +
                        "JOIN " + TABLE_MATCH + " m ON b.match_id=m.id " +
                        "WHERE m.completed=1" +
                        ") GROUP BY player_id",
                null
        );

        while (matchCounts.moveToNext()) {
            long playerId = matchCounts.getLong(0);
            int matches = matchCounts.getInt(1);

            ContentValues values = new ContentValues();
            values.put("matches", matches);

            db.update(TABLE_PLAYER, values, "id=?",
                    new String[]{String.valueOf(playerId)});
        }

        matchCounts.close();
    }

    // ============================================================
    // RESET CAREER STATISTICS
    // ============================================================

    private void resetCareerStatisticsInternal(
            SQLiteDatabase db
    ) {

        ContentValues values =
                new ContentValues();

        values.put("matches", 0);
        values.put("innings", 0);
        values.put("runs", 0);
        values.put("balls", 0);
        values.put("highest_score", 0);
        values.put("fours", 0);
        values.put("sixes", 0);
        values.put("ducks", 0);
        values.put("times_out", 0);
        values.put("not_outs", 0);
        values.put("thirties", 0);
        values.put("fifties", 0);
        values.put("centuries", 0);
        values.put("mom_count", 0);
        values.put("balls_bowled", 0);
        values.put("wickets", 0);
        values.put("runs_conceded", 0);
        values.put("maidens", 0);
        values.put("extras_conceded", 0);
        values.put("dot_balls", 0);
        values.put("three_wicket_hauls", 0);
        values.put("five_wicket_hauls", 0);
        values.put("best_bowling", "0/0");
        values.put("catches", 0);
        values.put("run_outs", 0);
        values.put("stumpings", 0);

        db.update(
                TABLE_PLAYER,
                values,
                null,
                null
        );
    }

    // ============================================================
    // CLEAR MATCH HISTORY
    // ============================================================

    public void clearMatchHistory() {

        SQLiteDatabase db =
                getWritableDatabase();

        db.beginTransaction();

        try {

            /*
             * Delete child statistics first.
             */
            db.delete(
                    TABLE_BOWLING,
                    null,
                    null
            );

            db.delete(
                    TABLE_BATTING,
                    null,
                    null
            );

            /*
             * Delete matches.
             */
            db.delete(
                    TABLE_MATCH,
                    null,
                    null
            );

            /*
             * IMPORTANT:
             *
             * Teams remain.
             * Players remain.
             * Player information remains.
             *
             * Only calculated career statistics reset.
             */
            resetCareerStatisticsInternal(
                    db
            );

            db.setTransactionSuccessful();

        } finally {

            db.endTransaction();
        }
    }

    // ============================================================
    // RESET PLAYER CAREER STATISTICS
    // ============================================================

    /**
     * Compatibility method used by SettingsActivity.
     * Keeps teams and player profiles and resets only career
     * statistics.
     */
    public void resetPlayerCareerStatistics() {

        resetPlayerRecords();
    }

    // ============================================================
    // RESET PLAYER RECORDS
    // ============================================================

    public void resetPlayerRecords() {

        SQLiteDatabase db =
                getWritableDatabase();

        db.beginTransaction();

        try {

            /*
             * Only career statistics are reset.
             *
             * Player:
             * - name
             * - jersey
             * - team
             * - role
             * - batting hand
             * - bowling style
             *
             * remain unchanged.
             */
            resetCareerStatisticsInternal(
                    db
            );

            db.setTransactionSuccessful();

        } finally {

            db.endTransaction();
        }
    }

    // ============================================================
    // RESET ALL DATA
    // ============================================================

    public void resetAllData() {

        SQLiteDatabase db =
                getWritableDatabase();

        db.beginTransaction();

        try {

            // ----------------------------------------------------
            // Match statistics
            // ----------------------------------------------------

            db.delete(
                    TABLE_BOWLING,
                    null,
                    null
            );

            db.delete(
                    TABLE_BATTING,
                    null,
                    null
            );

            db.delete(
                    TABLE_MATCH,
                    null,
                    null
            );

            // ----------------------------------------------------
            // Fixtures
            // ----------------------------------------------------

            db.delete(
                    TABLE_FIXTURE,
                    null,
                    null
            );

            // ----------------------------------------------------
            // Tournament teams
            // ----------------------------------------------------

            db.delete(
                    TABLE_TOURNAMENT_TEAM,
                    null,
                    null
            );

            // ----------------------------------------------------
            // Tournaments
            // ----------------------------------------------------

            db.delete(
                    TABLE_TOURNAMENT,
                    null,
                    null
            );

            // ----------------------------------------------------
            // Players
            // ----------------------------------------------------

            db.delete(
                    TABLE_PLAYER,
                    null,
                    null
            );

            // ----------------------------------------------------
            // Teams
            // ----------------------------------------------------

            db.delete(
                    TABLE_TEAM,
                    null,
                    null
            );

            db.setTransactionSuccessful();

        } finally {

            db.endTransaction();
        }
    }

    // ============================================================
    // CLOSE DATABASE
    // ============================================================

    public void closeDatabase() {

        close();
    }
}