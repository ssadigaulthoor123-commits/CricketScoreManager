package com.example.cricketscorer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.MatchInfo;

import java.util.ArrayList;
import java.util.List;

public class MatchHistoryAdapter
        extends RecyclerView.Adapter<
        MatchHistoryAdapter.MatchViewHolder> {

    public interface OnMatchClickListener {

        void onMatchClick(
                MatchInfo match
        );
    }

    private final List<MatchInfo> matchList;

    private final OnMatchClickListener listener;

    public MatchHistoryAdapter(
            List<MatchInfo> matchList,
            OnMatchClickListener listener
    ) {

        this.matchList =
                matchList == null
                        ? new ArrayList<>()
                        : matchList;

        this.listener =
                listener;
    }

    // ============================================================
    // CREATE VIEW HOLDER
    // ============================================================

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_match_history,
                        parent,
                        false
                );

        return new MatchViewHolder(
                view
        );
    }

    // ============================================================
    // BIND VIEW HOLDER
    // ============================================================

    @Override
    public void onBindViewHolder(
            @NonNull MatchViewHolder holder,
            int position
    ) {

        if (
                position < 0 ||
                        position >= matchList.size()
        ) {

            return;
        }

        MatchInfo match =
                matchList.get(
                        position
                );

        holder.bind(
                match
        );
    }

    // ============================================================
    // ITEM COUNT
    // ============================================================

    @Override
    public int getItemCount() {

        return matchList.size();
    }

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    class MatchViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView tvMatchDate;

        private final TextView tvTeamsLine;

        private final TextView tvScoreLine;

        private final TextView tvResultLine;

        MatchViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvMatchDate =
                    itemView.findViewById(
                            R.id.tvMatchDate
                    );

            tvTeamsLine =
                    itemView.findViewById(
                            R.id.tvTeamsLine
                    );

            tvScoreLine =
                    itemView.findViewById(
                            R.id.tvScoreLine
                    );

            tvResultLine =
                    itemView.findViewById(
                            R.id.tvResultLine
                    );

            itemView.setOnClickListener(
                    v -> {

                        int position =
                                getBindingAdapterPosition();

                        if (
                                position ==
                                        RecyclerView.NO_POSITION
                        ) {

                            return;
                        }

                        if (listener != null) {

                            listener.onMatchClick(
                                    matchList.get(
                                            position
                                    )
                            );
                        }
                    }
            );
        }

        // ========================================================
        // BIND
        // ========================================================

        void bind(
                MatchInfo match
        ) {

            if (match == null) {
                return;
            }

            // ----------------------------------------------------
            // DATE
            // ----------------------------------------------------

            String date =
                    safe(
                            match.getMatchDate()
                    );

            if (date.isEmpty()) {

                date =
                        "Date unavailable";
            }

            tvMatchDate.setText(
                    date
            );

            // ----------------------------------------------------
            // TEAMS
            // ----------------------------------------------------

            String teamA =
                    safe(
                            match.getTeamAName()
                    );

            String teamB =
                    safe(
                            match.getTeamBName()
                    );

            if (teamA.isEmpty()) {
                teamA = "Team A";
            }

            if (teamB.isEmpty()) {
                teamB = "Team B";
            }

            tvTeamsLine.setText(
                    teamA
                            + "  vs  "
                            + teamB
            );

            // ----------------------------------------------------
            // SCORE
            // ----------------------------------------------------

            tvScoreLine.setText(
                    buildScore(
                            match
                    )
            );

            // ----------------------------------------------------
            // RESULT
            // ----------------------------------------------------

            String result =
                    safe(
                            match.getResult()
                    );

            if (result.isEmpty()) {

                result =
                        "Match completed";
            }

            tvResultLine.setText(
                    result
            );
        }

        // ========================================================
        // BUILD SCORE
        // ========================================================

        private String buildScore(
                MatchInfo match
        ) {

            // The application supports only limited-overs matches.
            // Every match has exactly two innings: one for each team.
            return inningsScore(
                    match,
                    1
            )
                    + "    /    "
                    + inningsScore(
                    match,
                    2
            );
        }

        // ========================================================
        // INNINGS SCORE
        // ========================================================

        private String inningsScore(
                MatchInfo match,
                int innings
        ) {

            int runs =
                    match.getInningsRuns(
                            innings
                    );

            int wickets =
                    match.getInningsWickets(
                            innings
                    );

            int balls =
                    match.getInningsBalls(
                            innings
                    );

            if (
                    runs == 0 &&
                            wickets == 0 &&
                            balls == 0
            ) {

                return "0/0";
            }

            return runs
                    + "/"
                    + wickets
                    + " ("
                    + oversFromBalls(
                    balls
            )
                    + ")";
        }

        // ========================================================
        // BALLS TO OVERS
        // ========================================================

        private String oversFromBalls(
                int balls
        ) {

            if (balls < 0) {
                balls = 0;
            }

            int overs =
                    balls / 6;

            int remaining =
                    balls % 6;

            return overs
                    + "."
                    + remaining;
        }

        // ========================================================
        // SAFE STRING
        // ========================================================

        private String safe(
                String value
        ) {

            return value == null
                    ? ""
                    : value.trim();
        }
    }
}