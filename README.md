# Cricket Score Manager

An offline, feature-rich native Android application (**Java + Material Design 3 + SQLite**) designed for ball-by-ball cricket scoring, team and player management, match history tracking, advanced career statistics, pause/resume match support, and system notifications.

Developed as a native Android project for Mobile Application Development.

---

## Table of Contents
1. [Overview & Highlights](#overview--highlights)
2. [Key Application Modules & Features](#key-application-modules--features)
   - [Dashboard & Hamburger Navigation Drawer](#1-dashboard--hamburger-navigation-drawer)
   - [Team & Player Management](#2-team--player-management)
   - [Match Setup & Playing XI Selection](#3-match-setup--playing-xi-selection)
   - [Live Scoring & Pause/Resume Facility](#4-live-scoring--pauseresume-facility)
   - [Match Summary & Man of the Match (MOM)](#5-match-summary--man-of-the-match-mom)
   - [Detailed Scorecard](#6-detailed-scorecard)
   - [Match History](#7-match-history)
   - [Statistics & Independent Career Metrics](#8-statistics--independent-career-metrics)
   - [Settings & Theme Preferences](#9-settings--theme-preferences)
   - [Android Notification System](#10-android-notification-system)
3. [Project Architecture & Tech Stack](#project-architecture--tech-stack)
4. [Database Schema & Migrations](#database-schema--migrations)
5. [Prerequisites & System Requirements](#prerequisites--system-requirements)
6. [Building and Running the Application](#building-and-running-the-application)
   - [Using Android Studio](#a-using-android-studio)
   - [Using Gradle CLI](#b-using-gradle-cli)
   - [Deploying APK to Device / Emulator](#c-deploying-apk-to-device--emulator)
7. [Testing & Verification](#testing--verification)

---

## Overview & Highlights

* **100% Offline Capability**: Works fully locally on the device using SQLite without requiring cloud services, external APIs, or internet connectivity.
* **Format Flexibility**: Supports T20, ODI, and Custom Limited-Overs matches (from 1-over ultra-short games up to 50-over matches).
* **Precise Cricket Rules Engine**:
  - Automatic strike rotation on odd runs, over completion, and wickets.
  - Ongoing partnership tracking.
  - Retire Hurt support for both **Batters** and **Bowlers** with mid-over replacement and returning retired batter capabilities.
  - Accurate Extras Attribution: Wides & No-Balls credited to bowler runs & bowler extras; Byes & Leg-Byes credited to team extras only.
  - Dismissal Fielder Attribution: Prompts for fielders on Catches and Run-Outs, automatically credits designated Wicketkeepers on Stumpings.
* **Pause / Resume Match Facility**: Temporarily pause any in-progress match and resume scoring anytime exactly where you left off without prompting for player re-selection.
* **Isolated Career Statistics**: Permanent player statistics (`TABLE_PLAYER`) are updated **ONLY** when a match is fully completed (`completed = 1`), keeping in-progress/paused match statistics strictly temporary.
* **Independent Bowling Innings (`bowling_innings`)**: Tracks bowling innings separately from batting innings and matches played.
* **Modular Man of the Match (MOM) Evaluator**: Evaluates complete match impact across batting, bowling, fielding, all-round synergy, and match context with dynamic overs scaling and natural language performance explanations.
* **Theme Switching**: Supports System Default, Light Mode, and Dark Mode themes using `AppCompatDelegate` and `SharedPreferences`.

---

## Key Application Modules & Features

### 1. Dashboard & Hamburger Navigation Drawer
* **App Launcher Icon**: Custom vector icon featuring Stumps, Bails, Bat, and Red Leather Ball on a rich cricket pitch green gradient background.
* **Hamburger Menu (`☰`)**: Tapping the toolbar menu icon slides open a modern Navigation Drawer (`DrawerLayout` / `NavigationView`) for quick access to Teams, Players, Match History, Statistics, and Settings.
* **Dashboard Tiles (2x2 Grid)**:
  - **Teams**: Create and manage cricket teams.
  - **Players**: Add and manage player rosters with unique constraint checks.
  - **Match History**: View past completed matches.
  - **Statistics**: Player & team performance leaderboards.
* **Quick Overview Card**: Displays live summary counts for Teams, Players, and Completed Matches.

### 2. Team & Player Management
* **Team Management**: Add, edit, or delete teams with custom names, short names, and logos/initials.
* **Player Management**: Add, edit, or delete players. Enforces unique player names and jersey numbers per team. Role-based rules automatically configure wicketkeepers and bowling styles.

### 3. Match Setup & Playing XI Selection
* **Match Configuration**: Choose Team A & Team B, match format (T20, ODI, Custom), overs configuration, toss winner, and toss decision.
* **Playing XI & Squad Selection**: Select squad (15 players) and Playing XI (11 players) with pre-checked validation.

### 4. Live Scoring & Pause/Resume Facility
* **Scoreboard Panel**: Displays Batting Team, Score, Overs `(X.Y / Z ov)`, CRR, Required Target, Striker (*), Non-Striker, Current Bowler, and Partnership info.
* **Scoring Buttons**: Runs (`0`–`6`), Extras (`WIDE`, `NO BALL`, `BYE`, `LEG BYE`), Wicket dismissal types, and polished **[Undo Icon] UNDO** button located right beside OUT.
* **Pause Match Facility**: Pause scoring anytime. Saves complete active state (score, over, ball, striker, non-striker, bowler, chips, retired hurt states). Paused matches appear on the Dashboard under **"Matches to Continue"**. Tapping **Continue Match** resumes scoring instantly without asking for player selection again.
* **Retired Hurt Support**:
  - **Batter Retire Hurt**: Batter leaves crease without being out and can return later to resume their innings.
  - **Bowler Retire Hurt**: Bowler retires mid-over. Prompt selects an eligible replacement bowler to complete the remaining balls of that specific over.

### 5. Match Summary & Man of the Match (MOM)
* Displays winner result, final team scores, toss info, and the **Man of the Match**.
* Evaluates complete impact (batting, bowling, fielding, all-rounder bonus $+18\%$, winning team boost $+15\%$) and generates natural language performance reasons.

### 6. Detailed Scorecard
* **Tabbed Innings**: Separate tabs for Innings 1 and Innings 2.
* **Team Allocation**: Innings 1 displays Team 1 batters & Team 2 bowlers; Innings 2 displays Team 2 batters & Team 1 bowlers.
* **Batting Table**: Displays Batter Name, Not-Out Indicator (`*`), Detailed Dismissal Text (`c Fielder b Bowler`, `st WK b Bowler`, etc.), Runs ($R$), Balls ($B$), and Strike Rate ($SR$).
* **Bowling Table**: Displays Bowler Name, Overs ($O$), Maidens ($M$), Runs Conceded ($R$), Wickets ($W$).

### 7. Statistics & Independent Career Metrics
* **Team Stats Tab**: Leaderboard showing teams, matches played, wins count, and losses.
* **Player Stats Tab**: Categorized sections for **Top 10 Batsmen**, **Top 10 Bowlers**, and **Top 10 All-Rounders** with real-time search.
* **Career Metrics**:
  - **Matches Played**: Distinct completed matches participated in.
  - **Batting Innings**: Innings where player actually batted.
  - **Bowling Innings**: Distinct completed match innings where player actually bowled ($\ge 1$ ball).
  - Runs, Average, Strike Rate, Wickets, Economy, Maidens, Dot Balls, Extras Conceded, Catches, Stumpings, and MOM award counts.

### 8. Android Notification System
* **Channels & Permissions**: Configured with high/default importance channels and runtime `POST_NOTIFICATIONS` permission checks.
* **Events Notified**:
  - Match Completion (with deep link to Match Summary).
  - Theme Changes.
  - Match History Resets.

---

## Project Architecture & Tech Stack

* **Language**: Native Java (JDK 17+)
* **UI Framework**: Native Android Layouts (XML) with Material Components (M3)
* **Local Database**: SQLite (`SQLiteOpenHelper`) via `DatabaseHelper.java` (`DB_VERSION = 10`)
* **Architecture Pattern**: Activity-based navigation with decoupled data access, modular utils (`MomEvaluator.java`, `ScoreUtils.java`, `NotificationHelper.java`), custom adapters, and snapshot undo management.

---

## Database Schema & Migrations

The application manages SQLite tables defined in `DatabaseHelper.java` (`DB_VERSION = 10`):

1. **`Team`**: `id`, `name`, `short_name`, `icon_uri`.
2. **`Player`**: `id`, `name`, `jersey_number`, `team_id`, `role`, `batting_hand`, `bowling_style`, `image_uri`, `matches`, `innings`, `bowling_innings`, `runs`, `balls`, `highest_score`, `fours`, `sixes`, `ducks`, `times_out`, `not_outs`, `thirties`, `fifties`, `centuries`, `mom_count`, `balls_bowled`, `wickets`, `runs_conceded`, `maidens`, `dot_balls`, `extras_conceded`, `three_wicket_hauls`, `five_wicket_hauls`, `best_bowling`, `catches`, `run_outs`, `stumpings`.
3. **`Match`**: `id`, `team_a_id`, `team_b_id`, `total_overs`, `match_format`, `toss_winner_team_id`, `toss_decision`, `batting_team_id`, `bowling_team_id`, `inn1_runs`, `inn1_wickets`, `inn1_balls`, `inn2_runs`, `inn2_wickets`, `inn2_balls`, `current_innings`, `completed`, `is_paused`, `striker_id`, `non_striker_id`, `bowler_id`, `previous_bowler_id`, `legal_balls_this_over`, `result_text`, `match_date`, `player_of_match_id`, `innings1_team_id`, `innings2_team_id`.
4. **`Batting`**: `id`, `match_id`, `innings`, `player_id`, `runs`, `balls`, `fours`, `sixes`, `is_out`, `how_out`, `fielder_id`, `bowler_id`, `bowler_name`, `fielder_name`.
5. **`Bowling`**: `id`, `match_id`, `innings`, `player_id`, `balls`, `runs_conceded`, `wickets`, `maidens`, `dot_balls`, `extras_conceded`.

### Migration Safeguards
`DatabaseHelper.onUpgrade()` performs safe in-place schema upgrades up to `DB_VERSION = 10` using `addColumnIfMissing()` to preserve 100% of existing user data during updates.

---

## Prerequisites & System Requirements

* **Operating System**: macOS, Windows, or Linux
* **Development IDE**: Android Studio (Giraffe, Hedgehog, Iguana, or newer)
* **JDK**: Java Development Kit (JDK 17 or higher)
* **Android SDK**: Minimum SDK Version 24 (Android 7.0 Nougat), Target SDK Version 34+
* **Gradle**: Gradle 8.x with Android Gradle Plugin (AGP)

---

## Building and Running the Application

### A. Using Android Studio
1. Launch **Android Studio**.
2. Select **`File` -> `Open...`** and navigate to the project root directory (`CricketScoreManager`).
3. Allow Android Studio to sync Gradle project dependencies.
4. Connect an Android Physical Device via USB (with USB Debugging enabled) or start an Android Virtual Device (Emulator).
5. Select the **`app`** run configuration and click **`Run`** (or press `Shift + F10`).

### B. Using Gradle CLI
You can compile and build the APK directly from the terminal:

```bash
# Navigate to project root
cd /path/to/CricketScoreManager

# Clean and assemble debug APK
./gradlew clean assembleDebug
```

Upon successful build, the compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Testing & Verification

1. **Pause & Resume**: Score a few balls, tap **Pause Match**, return to Dashboard, tap **Continue Match** under "Matches to Continue" $\rightarrow$ verifies exact state is restored without player selection dialogs.
2. **Career Statistics Isolation**: Verify that in-progress/paused matches do not affect permanent player stats until the match is fully completed.
3. **Bowling Innings**: Verify bowling innings increments independently of batting innings.
4. **Undo**: Verify the polished **UNDO** button beside OUT.
5. **Notifications**: Verify system notifications on match completion, theme changes, and match history resets.
