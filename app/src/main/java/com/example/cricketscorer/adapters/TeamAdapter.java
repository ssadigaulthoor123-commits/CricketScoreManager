package com.example.cricketscorer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeamAdapter
        extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    public interface OnTeamActionListener {

        void onEditTeam(Team team);

        void onDeleteTeam(Team team);
    }

    private final List<Team> teams;

    private final OnTeamActionListener listener;

    public TeamAdapter(
            List<Team> teams,
            OnTeamActionListener listener
    ) {

        this.teams =
                teams == null
                        ? new ArrayList<>()
                        : teams;

        this.listener = listener;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_team,
                        parent,
                        false
                );

        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TeamViewHolder holder,
            int position
    ) {

        Team team =
                teams.get(position);

        if (team == null) {
            return;
        }

        // ========================================================
        // TEAM NAME
        // ========================================================

        String teamName =
                team.getName();

        if (
                teamName == null ||
                        teamName.trim().isEmpty()
        ) {

            teamName = "Unnamed Team";
        }

        holder.tvTeamName.setText(
                teamName
        );

        // ========================================================
        // SHORT NAME
        // ========================================================

        String shortName =
                team.getShortName();

        if (
                shortName == null ||
                        shortName.trim().isEmpty()
        ) {

            shortName =
                    generateInitials(
                            teamName
                    );
        }

        shortName =
                shortName.toUpperCase(
                        Locale.getDefault()
                );

        holder.tvTeamShort.setText(
                shortName
        );

        // ========================================================
        // INITIALS
        // ========================================================

        holder.tvInitials.setText(
                shortName
        );

        // ========================================================
        // TEAM LOGO
        // ========================================================

        /*
         * The current XML contains a logo ImageView but it is
         * hidden by default. Keep it hidden because the current
         * Team model does not provide a logo resource.
         */
        holder.teamLogo.setVisibility(
                View.GONE
        );

        holder.tvInitials.setVisibility(
                View.VISIBLE
        );

        // ========================================================
        // EDIT
        // ========================================================

        holder.btnEditTeam.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onEditTeam(
                                team
                        );
                    }
                }
        );

        // ========================================================
        // DELETE
        // ========================================================

        holder.btnDeleteTeam.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onDeleteTeam(
                                team
                        );
                    }
                }
        );

        // ========================================================
        // CARD CLICK
        // ========================================================

        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onEditTeam(
                                team
                        );
                    }
                }
        );
    }

    // ============================================================
    // GENERATE INITIALS
    // ============================================================

    private String generateInitials(
            String teamName
    ) {

        if (
                teamName == null ||
                        teamName.trim().isEmpty()
        ) {

            return "TM";
        }

        String[] words =
                teamName.trim()
                        .split("\\s+");

        if (words.length == 1) {

            String word =
                    words[0];

            if (word.length() >= 3) {

                return word.substring(
                        0,
                        3
                ).toUpperCase(
                        Locale.getDefault()
                );
            }

            return word.toUpperCase(
                    Locale.getDefault()
            );
        }

        StringBuilder result =
                new StringBuilder();

        for (
                String word : words
        ) {

            if (
                    word != null &&
                            !word.isEmpty()
            ) {

                result.append(
                        Character.toUpperCase(
                                word.charAt(0)
                        )
                );
            }

            if (result.length() == 3) {
                break;
            }
        }

        return result.length() == 0
                ? "TM"
                : result.toString();
    }

    // ============================================================
    // ITEM COUNT
    // ============================================================

    @Override
    public int getItemCount() {

        return teams.size();
    }

    // ============================================================
    // UPDATE DATA
    // ============================================================

    public void setTeams(
            List<Team> newTeams
    ) {

        teams.clear();

        if (newTeams != null) {

            teams.addAll(
                    newTeams
            );
        }

        notifyDataSetChanged();
    }

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    static class TeamViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvInitials;

        ImageView teamLogo;

        TextView tvTeamName;

        TextView tvTeamShort;

        ImageButton btnEditTeam;

        ImageButton btnDeleteTeam;

        TeamViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvInitials =
                    itemView.findViewById(
                            R.id.tvInitials
                    );

            teamLogo =
                    itemView.findViewById(
                            R.id.teamLogo
                    );

            tvTeamName =
                    itemView.findViewById(
                            R.id.tvTeamName
                    );

            tvTeamShort =
                    itemView.findViewById(
                            R.id.tvTeamShort
                    );

            btnEditTeam =
                    itemView.findViewById(
                            R.id.btnEditTeam
                    );

            btnDeleteTeam =
                    itemView.findViewById(
                            R.id.btnDeleteTeam
                    );
        }
    }
}