package com.example.cricketscorer.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * PlayerAdapter
 *
 * Displays players grouped by team.
 *
 * The actual player card continues to use:
 * item_player.xml
 *
 * Team headers are created programmatically so
 * no additional XML file is required.
 */
public class PlayerAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ============================================================
    // VIEW TYPES
    // ============================================================

    private static final int VIEW_TYPE_TEAM_HEADER = 0;

    private static final int VIEW_TYPE_PLAYER = 1;

    // ============================================================
    // LISTENER
    // ============================================================

    public interface OnPlayerClickListener {

        void onPlayerClick(
                Player player
        );

        void onEditClick(
                Player player
        );

        void onDeleteClick(
                Player player
        );
    }

    // ============================================================
    // DATA
    // ============================================================

    private final List<Player> players =
            new ArrayList<>();

    private final List<Object> displayItems =
            new ArrayList<>();

    private final OnPlayerClickListener listener;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public PlayerAdapter(
            List<Player> players,
            OnPlayerClickListener listener
    ) {

        this.listener = listener;

        setPlayers(players);
    }

    // ============================================================
    // SET PLAYERS
    // ============================================================

    public void setPlayers(
            List<Player> newPlayers
    ) {

        players.clear();

        if (newPlayers != null) {

            players.addAll(
                    newPlayers
            );
        }

        rebuildDisplayItems();

        notifyDataSetChanged();
    }

    // ============================================================
    // BUILD GROUPED LIST
    // ============================================================

    private void rebuildDisplayItems() {

        displayItems.clear();

        String previousTeam =
                null;

        for (Player player : players) {

            if (player == null) {
                continue;
            }

            String teamName =
                    getTeamName(player);

            /*
             * A new section begins whenever
             * the team changes.
             */
            if (previousTeam == null
                    || !previousTeam.equalsIgnoreCase(
                    teamName
            )) {

                displayItems.add(
                        teamName
                );

                previousTeam =
                        teamName;
            }

            displayItems.add(
                    player
            );
        }
    }

    // ============================================================
    // TEAM NAME
    // ============================================================

    private String getTeamName(
            Player player
    ) {

        if (player == null) {
            return "No Team";
        }

        String teamName =
                player.getTeamName();

        if (teamName == null
                || teamName.trim().isEmpty()) {

            return "No Team";
        }

        return teamName.trim();
    }

    // ============================================================
    // VIEW TYPE
    // ============================================================

    @Override
    public int getItemViewType(
            int position
    ) {

        Object item =
                displayItems.get(position);

        if (item instanceof String) {

            return VIEW_TYPE_TEAM_HEADER;
        }

        return VIEW_TYPE_PLAYER;
    }

    // ============================================================
    // CREATE VIEW HOLDER
    // ============================================================

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        if (viewType == VIEW_TYPE_TEAM_HEADER) {

            return createTeamHeaderHolder(
                    parent
            );
        }

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_player,
                        parent,
                        false
                );

        return new PlayerViewHolder(
                view
        );
    }

    // ============================================================
    // CREATE TEAM HEADER
    // ============================================================

    private TeamHeaderViewHolder createTeamHeaderHolder(
            ViewGroup parent
    ) {

        Context context =
                parent.getContext();

        TextView header =
                new TextView(context);

        RecyclerView.LayoutParams params =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        header.setLayoutParams(params);

        /*
         * Clean spacing around team sections.
         */
        header.setPadding(
                dp(context, 18),
                dp(context, 18),
                dp(context, 18),
                dp(context, 7)
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setTextSize(
                14
        );

        header.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        try {

            header.setTextColor(
                    ContextCompat.getColor(
                            context,
                            R.color.primary
                    )
            );

        } catch (Exception e) {

            header.setTextColor(
                    0xFF167A3F
            );
        }

        return new TeamHeaderViewHolder(
                header
        );
    }

    // ============================================================
    // BIND VIEW HOLDER
    // ============================================================

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {

        Object item =
                displayItems.get(position);

        // --------------------------------------------------------
        // TEAM HEADER
        // --------------------------------------------------------

        if (holder instanceof TeamHeaderViewHolder) {

            TeamHeaderViewHolder headerHolder =
                    (TeamHeaderViewHolder) holder;

            headerHolder.textView.setText(
                    String.valueOf(item)
            );

            return;
        }

        // --------------------------------------------------------
        // PLAYER
        // --------------------------------------------------------

        if (!(holder instanceof PlayerViewHolder)) {
            return;
        }

        PlayerViewHolder playerHolder =
                (PlayerViewHolder) holder;

        Player player =
                (Player) item;

        bindPlayer(
                playerHolder,
                player
        );
    }

    // ============================================================
    // BIND PLAYER
    // ============================================================

    private void bindPlayer(
            PlayerViewHolder holder,
            Player player
    ) {

        if (player == null) {
            return;
        }

        // --------------------------------------------------------
        // PLAYER NAME
        // --------------------------------------------------------

        String name =
                player.getName();

        if (name == null
                || name.trim().isEmpty()) {

            name = "Unknown Player";
        }

        holder.tvPlayerName.setText(
                name.trim()
        );

        // --------------------------------------------------------
        // JERSEY NUMBER
        // --------------------------------------------------------

        int jersey =
                player.getJerseyNumber();

        if (jersey > 0) {

            holder.tvJersey.setText(
                    "#" + jersey
            );

            holder.tvJersey.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.tvJersey.setVisibility(
                    View.GONE
            );
        }

        // --------------------------------------------------------
        // TEAM + ROLE
        // --------------------------------------------------------

        String teamName =
                getTeamName(player);

        String role =
                player.getRole();

        if (role == null
                || role.trim().isEmpty()) {

            role = "Player";
        }

        /*
         * The XML has one metadata TextView:
         *
         * team · role
         */
        if ("No Team".equalsIgnoreCase(teamName)) {

            holder.tvPlayerMeta.setText(
                    "No team · " + role.trim()
            );

        } else {

            holder.tvPlayerMeta.setText(
                    teamName + " · " + role.trim()
            );
        }

        // --------------------------------------------------------
        // WHOLE CARD CLICK
        // --------------------------------------------------------

        holder.itemView.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onPlayerClick(
                                player
                        );
                    }
                }
        );

        // --------------------------------------------------------
        // EDIT BUTTON
        // --------------------------------------------------------

        holder.btnEditPlayer.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onEditClick(
                                player
                        );
                    }
                }
        );

        // --------------------------------------------------------
        // DELETE BUTTON
        // --------------------------------------------------------

        holder.btnDeletePlayer.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onDeleteClick(
                                player
                        );
                    }
                }
        );
    }

    // ============================================================
    // ITEM COUNT
    // ============================================================

    @Override
    public int getItemCount() {

        return displayItems.size();
    }

    // ============================================================
    // DP HELPER
    // ============================================================

    private int dp(
            Context context,
            int value
    ) {

        float density =
                context.getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                value * density
        );
    }

    // ============================================================
    // TEAM HEADER HOLDER
    // ============================================================

    private static class TeamHeaderViewHolder
            extends RecyclerView.ViewHolder {

        final TextView textView;

        TeamHeaderViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            textView =
                    (TextView) itemView;
        }
    }

    // ============================================================
    // PLAYER HOLDER
    // ============================================================

    private static class PlayerViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvJersey;
        TextView tvPlayerName;
        TextView tvPlayerMeta;

        ImageButton btnEditPlayer;
        ImageButton btnDeletePlayer;

        PlayerViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            /*
             * These IDs exactly match the current
             * item_player.xml.
             */

            tvJersey =
                    itemView.findViewById(
                            R.id.tvJersey
                    );

            tvPlayerName =
                    itemView.findViewById(
                            R.id.tvPlayerName
                    );

            tvPlayerMeta =
                    itemView.findViewById(
                            R.id.tvPlayerMeta
                    );

            btnEditPlayer =
                    itemView.findViewById(
                            R.id.btnEditPlayer
                    );

            btnDeletePlayer =
                    itemView.findViewById(
                            R.id.btnDeletePlayer
                    );
        }
    }
}