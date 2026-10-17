package com.example.cricketscorer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.MatchInfo;
import com.example.cricketscorer.utils.ScoreUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ContinueMatchAdapter extends RecyclerView.Adapter<ContinueMatchAdapter.ContinueViewHolder> {

    public interface OnMatchContinueListener {
        void onContinueMatch(MatchInfo match);
    }

    private final List<MatchInfo> matches = new ArrayList<>();
    private final OnMatchContinueListener listener;

    public ContinueMatchAdapter(List<MatchInfo> matchInfoList, OnMatchContinueListener listener) {
        this.listener = listener;
        setMatches(matchInfoList);
    }

    public void setMatches(List<MatchInfo> newMatches) {
        matches.clear();
        if (newMatches != null) {
            matches.addAll(newMatches);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContinueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_continue_match, parent, false);
        return new ContinueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContinueViewHolder holder, int position) {
        MatchInfo match = matches.get(position);
        if (match == null) return;

        String tA = match.getTeamAName() != null ? match.getTeamAName() : "Team A";
        String tB = match.getTeamBName() != null ? match.getTeamBName() : "Team B";
        holder.tvTeams.setText(tA + " vs " + tB);

        int currentInn = match.getCurrentInnings();
        int runs = (currentInn == 1) ? match.getInn1Runs() : (currentInn == 2) ? match.getInn2Runs() : 0;
        int wkts = (currentInn == 1) ? match.getInn1Wickets() : (currentInn == 2) ? match.getInn2Wickets() : 0;
        int balls = (currentInn == 1) ? match.getInn1Balls() : (currentInn == 2) ? match.getInn2Balls() : 0;

        holder.tvScore.setText("Score: " + runs + "/" + wkts);
        holder.tvOvers.setText("Over: " + ScoreUtils.formatOvers(balls));

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onContinueMatch(match);
            }
        };

        holder.btnResume.setOnClickListener(clickListener);
        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public static class ContinueViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeams, tvScore, tvOvers;
        MaterialButton btnResume;

        public ContinueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeams = itemView.findViewById(R.id.tvContinueTeams);
            tvScore = itemView.findViewById(R.id.tvContinueScore);
            tvOvers = itemView.findViewById(R.id.tvContinueOvers);
            btnResume = itemView.findViewById(R.id.btnResumeMatch);
        }
    }
}
