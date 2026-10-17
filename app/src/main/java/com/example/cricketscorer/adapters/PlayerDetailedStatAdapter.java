package com.example.cricketscorer.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerDetailedStatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_PLAYER = 1;

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
    }

    private final List<Object> displayItems = new ArrayList<>();
    private final OnPlayerClickListener listener;

    public PlayerDetailedStatAdapter(
            List<Player> batsmen,
            List<Player> bowlers,
            List<Player> allRounders,
            String searchQuery,
            List<Player> allPlayers,
            OnPlayerClickListener listener
    ) {
        this.listener = listener;
        buildItems(batsmen, bowlers, allRounders, searchQuery, allPlayers);
    }

    public void updateData(
            List<Player> batsmen,
            List<Player> bowlers,
            List<Player> allRounders,
            String searchQuery,
            List<Player> allPlayers
    ) {
        buildItems(batsmen, bowlers, allRounders, searchQuery, allPlayers);
        notifyDataSetChanged();
    }

    private void buildItems(
            List<Player> batsmen,
            List<Player> bowlers,
            List<Player> allRounders,
            String query,
            List<Player> all
    ) {
        displayItems.clear();
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            displayItems.add("Search Results");
            boolean found = false;
            if (all != null) {
                for (Player p : all) {
                    if (p != null && p.getName() != null && p.getName().toLowerCase().contains(q)) {
                        displayItems.add(p);
                        found = true;
                    }
                }
            }
            if (!found) {
                // Add a placeholder or empty note if needed
            }
            return;
        }

        if (batsmen != null && !batsmen.isEmpty()) {
            displayItems.add("Top 10 Batsmen");
            displayItems.addAll(batsmen);
        }
        if (bowlers != null && !bowlers.isEmpty()) {
            displayItems.add("Top 10 Bowlers");
            displayItems.addAll(bowlers);
        }
        if (allRounders != null && !allRounders.isEmpty()) {
            displayItems.add("Top 10 All-Rounders");
            displayItems.addAll(allRounders);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (displayItems.get(position) instanceof String) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_PLAYER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            Context context = parent.getContext();
            TextView header = new TextView(context);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            header.setLayoutParams(params);
            header.setPadding(dp(context, 18), dp(context, 18), dp(context, 18), dp(context, 6));
            header.setGravity(Gravity.CENTER_VERTICAL);
            header.setTextSize(15);
            header.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            header.setTextColor(ContextCompat.getColor(context, R.color.primary));
            return new HeaderViewHolder(header);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stat_player_detailed, parent, false);
        return new DetailedStatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayItems.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).textView.setText(String.valueOf(item));
            return;
        }
        if (!(holder instanceof DetailedStatViewHolder)) return;
        DetailedStatViewHolder pHolder = (DetailedStatViewHolder) holder;
        Player player = (Player) item;

        String name = player.getName() != null ? player.getName() : "Unknown Player";
        if (player.getJerseyNumber() > 0) {
            name += " (#" + player.getJerseyNumber() + ")";
        }
        pHolder.tvPlayerName.setText(name);

        String role = player.getRole() != null ? player.getRole() : "Player";
        String team = player.getTeamName() != null && !player.getTeamName().trim().isEmpty() ? player.getTeamName().trim() : "";
        if (!team.isEmpty()) {
            pHolder.tvPlayerRole.setText(role + " • " + team);
        } else {
            pHolder.tvPlayerRole.setText(role);
        }

        pHolder.tvStatMat.setText(String.valueOf(player.getMatches()));
        pHolder.tvStatRuns.setText(String.valueOf(player.getRuns()));

        double avg = player.getTimesOut() > 0 ? (double) player.getRuns() / player.getTimesOut() : player.getRuns();
        pHolder.tvStatAvg.setText(String.format(Locale.US, "%.2f", avg));

        double sr = player.getBalls() > 0 ? ((double) player.getRuns() / player.getBalls()) * 100.0 : 0.0;
        pHolder.tvStatSR.setText(String.format(Locale.US, "%.2f", sr));

        pHolder.tvStatWkts.setText(String.valueOf(player.getWickets()));

        double econ = player.getBallsBowled() > 0 ? ((double) player.getRunsConceded() / player.getBallsBowled()) * 6.0 : 0.0;
        pHolder.tvStatEcon.setText(String.format(Locale.US, "%.2f", econ));

        pHolder.tvStatCatches.setText(String.valueOf(player.getCatches()));

        if (player.isWicketKeeper()) {
            pHolder.labelStumps.setVisibility(View.VISIBLE);
            pHolder.tvStatStumps.setVisibility(View.VISIBLE);
            pHolder.tvStatStumps.setText(String.valueOf(player.getStumpings()));
        } else {
            pHolder.labelStumps.setVisibility(View.GONE);
            pHolder.tvStatStumps.setVisibility(View.GONE);
        }

        if (player.getMomCount() > 0) {
            pHolder.labelMom.setVisibility(View.VISIBLE);
            pHolder.tvStatMom.setVisibility(View.VISIBLE);
            pHolder.tvStatMom.setText(String.valueOf(player.getMomCount()));
        } else {
            pHolder.labelMom.setVisibility(View.GONE);
            pHolder.tvStatMom.setVisibility(View.GONE);
        }

        pHolder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayerClick(player);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    private int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

    public static class DetailedStatViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlayerName, tvPlayerRole;
        TextView tvStatMat, tvStatRuns, tvStatAvg, tvStatSR, tvStatWkts, tvStatEcon, tvStatCatches, tvStatStumps, tvStatMom;
        TextView labelStumps, labelMom;

        public DetailedStatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvPlayerRole = itemView.findViewById(R.id.tvPlayerRole);
            tvStatMat = itemView.findViewById(R.id.tvStatMat);
            tvStatRuns = itemView.findViewById(R.id.tvStatRuns);
            tvStatAvg = itemView.findViewById(R.id.tvStatAvg);
            tvStatSR = itemView.findViewById(R.id.tvStatSR);
            tvStatWkts = itemView.findViewById(R.id.tvStatWkts);
            tvStatEcon = itemView.findViewById(R.id.tvStatEcon);
            tvStatCatches = itemView.findViewById(R.id.tvStatCatches);
            tvStatStumps = itemView.findViewById(R.id.tvStatStumps);
            labelStumps = itemView.findViewById(R.id.labelStumps);
            tvStatMom = itemView.findViewById(R.id.tvStatMom);
            labelMom = itemView.findViewById(R.id.labelMom);
        }
    }
}
