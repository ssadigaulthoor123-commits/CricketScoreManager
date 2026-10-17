package com.example.cricketscorer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.TeamStat;

import java.util.ArrayList;
import java.util.List;

public class TeamStatAdapter extends RecyclerView.Adapter<TeamStatAdapter.TeamStatViewHolder> {

    private final List<TeamStat> teamStats = new ArrayList<>();

    public TeamStatAdapter(List<TeamStat> stats) {
        setStats(stats);
    }

    public void setStats(List<TeamStat> stats) {
        teamStats.clear();
        if (stats != null) {
            teamStats.addAll(stats);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamStatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stat_team, parent, false);
        return new TeamStatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamStatViewHolder holder, int position) {
        TeamStat stat = teamStats.get(position);
        if (stat == null) return;
        holder.tvTeamName.setText(stat.getTeamName());
        holder.tvPlayed.setText(String.valueOf(stat.getMatchesPlayed()));
        holder.tvWins.setText(String.valueOf(stat.getWins()));
        holder.tvLosses.setText(String.valueOf(stat.getLosses()));
    }

    @Override
    public int getItemCount() {
        return teamStats.size();
    }

    public static class TeamStatViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName, tvPlayed, tvWins, tvLosses;

        public TeamStatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvPlayed = itemView.findViewById(R.id.tvPlayed);
            tvWins = itemView.findViewById(R.id.tvWins);
            tvLosses = itemView.findViewById(R.id.tvLosses);
        }
    }
}
