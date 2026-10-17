package com.example.cricketscorer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatAdapter
        extends RecyclerView.Adapter<PlayerStatAdapter.PlayerStatViewHolder> {

    private final List<Player> playerList;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public PlayerStatAdapter(List<Player> playerList) {

        this.playerList =
                playerList == null
                        ? new ArrayList<>()
                        : playerList;
    }

    // ============================================================
    // CREATE VIEW HOLDER
    // ============================================================

    @NonNull
    @Override
    public PlayerStatViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_stat_player,
                        parent,
                        false
                );

        return new PlayerStatViewHolder(view);
    }

    // ============================================================
    // BIND VIEW HOLDER
    // ============================================================

    @Override
    public void onBindViewHolder(
            @NonNull PlayerStatViewHolder holder,
            int position
    ) {

        if (position < 0
                || position >= playerList.size()) {

            return;
        }

        Player player =
                playerList.get(position);

        if (player == null) {
            return;
        }

        // --------------------------------------------------------
        // PLAYER NAME
        // --------------------------------------------------------

        String playerName =
                player.getName();

        if (playerName == null
                || playerName.trim().isEmpty()) {

            playerName = "Unknown Player";
        }

        holder.tvPlayerName.setText(
                playerName
        );

        // --------------------------------------------------------
        // PLAYER ROLE
        // --------------------------------------------------------

        String playerRole =
                player.getRole();

        if (playerRole == null
                || playerRole.trim().isEmpty()) {

            playerRole = "Player";
        }

        holder.tvPlayerRole.setText(
                playerRole
        );

        // --------------------------------------------------------
        // MATCHES
        // --------------------------------------------------------

        holder.tvMatches.setText(
                String.valueOf(
                        player.getMatches()
                )
        );

        // --------------------------------------------------------
        // RUNS
        // --------------------------------------------------------

        holder.tvRuns.setText(
                String.valueOf(
                        player.getRuns()
                )
        );

        // --------------------------------------------------------
        // WICKETS
        // --------------------------------------------------------

        holder.tvWickets.setText(
                String.valueOf(
                        player.getWickets()
                )
        );
    }

    // ============================================================
    // ITEM COUNT
    // ============================================================

    @Override
    public int getItemCount() {

        return playerList.size();
    }

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    public static class PlayerStatViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvPlayerName;
        TextView tvPlayerRole;
        TextView tvMatches;
        TextView tvRuns;
        TextView tvWickets;

        public PlayerStatViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvPlayerName =
                    itemView.findViewById(
                            R.id.tvPlayerName
                    );

            tvPlayerRole =
                    itemView.findViewById(
                            R.id.tvPlayerRole
                    );

            tvMatches =
                    itemView.findViewById(
                            R.id.tvMatches
                    );

            tvRuns =
                    itemView.findViewById(
                            R.id.tvRuns
                    );

            tvWickets =
                    itemView.findViewById(
                            R.id.tvWickets
                    );
        }
    }
}
