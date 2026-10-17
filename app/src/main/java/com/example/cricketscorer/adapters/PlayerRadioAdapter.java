package com.example.cricketscorer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerRadioAdapter
        extends RecyclerView.Adapter<PlayerRadioAdapter.PlayerViewHolder> {

    private final List<Player> players;

    /*
     * Only one player can be selected at a time.
     */
    private long selectedPlayerId = -1;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public PlayerRadioAdapter(List<Player> players) {

        this.players =
                players == null
                        ? new ArrayList<>()
                        : players;
    }

    // ============================================================
    // CREATE VIEW HOLDER
    // ============================================================

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_player_radio,
                        parent,
                        false
                );

        return new PlayerViewHolder(view);
    }

    // ============================================================
    // BIND VIEW HOLDER
    // ============================================================

    @Override
    public void onBindViewHolder(
            @NonNull PlayerViewHolder holder,
            int position
    ) {

        Player player =
                players.get(position);

        if (player == null) {
            return;
        }

        long playerId =
                player.getId();

        // --------------------------------------------------------
        // REMOVE OLD LISTENER
        // --------------------------------------------------------

        holder.radioButton.setOnCheckedChangeListener(
                null
        );

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

        String role =
                player.getRole();

        if (role == null
                || role.trim().isEmpty()) {

            role = "Player";
        }

        if (holder.tvPlayerRole != null) {

            holder.tvPlayerRole.setText(
                    role
            );
        }

        // --------------------------------------------------------
        // JERSEY NUMBER
        // --------------------------------------------------------

        if (holder.tvJerseyNumber != null) {

            int jersey =
                    player.getJerseyNumber();

            if (jersey > 0) {

                holder.tvJerseyNumber.setText(
                        "#" + jersey
                );

                holder.tvJerseyNumber.setVisibility(
                        View.VISIBLE
                );

            } else {

                holder.tvJerseyNumber.setVisibility(
                        View.GONE
                );
            }
        }

        // --------------------------------------------------------
        // SELECTED STATE
        // --------------------------------------------------------

        holder.radioButton.setChecked(
                playerId == selectedPlayerId
        );

        // --------------------------------------------------------
        // RADIO BUTTON LISTENER
        // --------------------------------------------------------

        holder.radioButton.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (!isChecked) {
                        return;
                    }

                    long oldSelectedId =
                            selectedPlayerId;

                    selectedPlayerId =
                            playerId;

                    /*
                     * Refresh previously selected player.
                     */
                    if (oldSelectedId != -1
                            && oldSelectedId != playerId) {

                        int oldPosition =
                                findPlayerPosition(
                                        oldSelectedId
                                );

                        if (oldPosition >= 0) {

                            notifyItemChanged(
                                    oldPosition
                            );
                        }
                    }

                    /*
                     * Refresh current player.
                     */
                    int currentPosition =
                            findPlayerPosition(
                                    playerId
                            );

                    if (currentPosition >= 0) {

                        notifyItemChanged(
                                currentPosition
                        );
                    }
                }
        );

        // --------------------------------------------------------
        // WHOLE ROW CLICK
        // --------------------------------------------------------

        holder.itemView.setOnClickListener(
                v -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }

                    Player clickedPlayer =
                            players.get(adapterPosition);

                    if (clickedPlayer == null) {
                        return;
                    }

                    long clickedPlayerId =
                            clickedPlayer.getId();

                    if (selectedPlayerId == clickedPlayerId) {
                        return;
                    }

                    long oldSelectedId =
                            selectedPlayerId;

                    selectedPlayerId =
                            clickedPlayerId;

                    // ------------------------------------------------
                    // REFRESH OLD SELECTION
                    // ------------------------------------------------

                    if (oldSelectedId != -1) {

                        int oldPosition =
                                findPlayerPosition(
                                        oldSelectedId
                                );

                        if (oldPosition >= 0) {

                            notifyItemChanged(
                                    oldPosition
                            );
                        }
                    }

                    // ------------------------------------------------
                    // REFRESH NEW SELECTION
                    // ------------------------------------------------

                    notifyItemChanged(
                            adapterPosition
                    );
                }
        );
    }

    // ============================================================
    // FIND PLAYER POSITION
    // ============================================================

    private int findPlayerPosition(
            long playerId
    ) {

        for (int i = 0;
             i < players.size();
             i++) {

            Player player =
                    players.get(i);

            if (player != null
                    && player.getId() == playerId) {

                return i;
            }
        }

        return -1;
    }

    // ============================================================
    // ITEM COUNT
    // ============================================================

    @Override
    public int getItemCount() {

        return players.size();
    }

    // ============================================================
    // GET SELECTED PLAYER ID
    // ============================================================

    public long getSelectedPlayerId() {

        return selectedPlayerId;
    }

    // ============================================================
    // SET SELECTED PLAYER
    // ============================================================

    public void setSelectedPlayerId(
            long playerId
    ) {

        if (playerId == selectedPlayerId) {
            return;
        }

        long oldSelectedId =
                selectedPlayerId;

        selectedPlayerId =
                -1;

        // --------------------------------------------------------
        // CLEAR OLD SELECTION
        // --------------------------------------------------------

        if (oldSelectedId != -1) {

            int oldPosition =
                    findPlayerPosition(
                            oldSelectedId
                    );

            if (oldPosition >= 0) {

                notifyItemChanged(
                        oldPosition
                );
            }
        }

        // --------------------------------------------------------
        // SET NEW SELECTION
        // --------------------------------------------------------

        if (playerId != -1) {

            int newPosition =
                    findPlayerPosition(
                            playerId
                    );

            if (newPosition >= 0) {

                selectedPlayerId =
                        playerId;

                notifyItemChanged(
                        newPosition
                );
            }
        }
    }

    // ============================================================
    // CLEAR SELECTION
    // ============================================================

    public void clearSelection() {

        setSelectedPlayerId(-1);
    }

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    static class PlayerViewHolder
            extends RecyclerView.ViewHolder {

        RadioButton radioButton;

        TextView tvPlayerName;
        TextView tvPlayerRole;
        TextView tvJerseyNumber;

        PlayerViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            radioButton =
                    itemView.findViewById(
                            R.id.radioPlayer
                    );

            tvPlayerName =
                    itemView.findViewById(
                            R.id.tvPlayerName
                    );

            tvPlayerRole =
                    itemView.findViewById(
                            R.id.tvPlayerRole
                    );

            tvJerseyNumber =
                    itemView.findViewById(
                            R.id.tvJerseyNumber
                    );
        }
    }
}