package com.example.cricketscorer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cricketscorer.R;
import com.example.cricketscorer.models.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerCheckboxAdapter
        extends RecyclerView.Adapter<PlayerCheckboxAdapter.PlayerViewHolder> {

    // ============================================================
    // DATA
    // ============================================================

    private final List<Player> players;

    private final Set<Long> selectedIds =
            new HashSet<>();

    private final int maxSelection;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount, int maxSelection);
    }

    private OnSelectionChangedListener selectionChangedListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedIds.size(), maxSelection);
        }
    }

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public PlayerCheckboxAdapter(
            List<Player> players,
            int maxSelection
    ) {

        if (players == null) {

            this.players =
                    new ArrayList<>();

        } else {

            this.players =
                    new ArrayList<>(players);
        }

        this.maxSelection =
                Math.max(
                        1,
                        maxSelection
                );
    }

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    public static class PlayerViewHolder
            extends RecyclerView.ViewHolder {

        CheckBox checkboxPlayer;

        TextView tvPlayerName;

        TextView tvPlayerRole;

        public PlayerViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            checkboxPlayer =
                    itemView.findViewById(
                            R.id.cbPlayer
                    );

            tvPlayerName =
                    itemView.findViewById(
                            R.id.tvPlayerName
                    );

            tvPlayerRole =
                    itemView.findViewById(
                            R.id.tvPlayerRole
                    );
        }
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
                        R.layout.item_player_checkbox,
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

        if (position < 0 ||
                position >= players.size()) {

            return;
        }

        Player player =
                players.get(position);

        if (player == null) {
            return;
        }

        final long playerId =
                player.getId();

        // ========================================================
        // PLAYER NAME
        // ========================================================

        if (holder.tvPlayerName != null) {

            String name =
                    player.getName();

            if (name == null ||
                    name.trim().isEmpty()) {

                name = "Unknown Player";
            }

            holder.tvPlayerName.setText(
                    name
            );
        }

        // ========================================================
        // PLAYER ROLE
        // ========================================================

        if (holder.tvPlayerRole != null) {

            String role =
                    player.getRole();

            if (role == null ||
                    role.trim().isEmpty()) {

                role = "Player";
            }

            int jerseyNumber =
                    player.getJerseyNumber();

            if (jerseyNumber > 0) {

                role =
                        role
                                + " · #"
                                + jerseyNumber;
            }

            holder.tvPlayerRole.setText(
                    role
            );
        }

        // ========================================================
        // CHECKBOX
        // ========================================================

        final CheckBox checkbox =
                holder.checkboxPlayer;

        if (checkbox == null) {
            return;
        }

        checkbox.setOnCheckedChangeListener(null);

        checkbox.setChecked(
                selectedIds.contains(playerId)
        );

        // ========================================================
        // CHECKBOX LISTENER
        // ========================================================

        checkbox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        if (selectedIds.contains(playerId)) {
                            return;
                        }

                        if (selectedIds.size()
                                >= maxSelection) {

                            buttonView.setChecked(false);
                            return;
                        }

                        selectedIds.add(playerId);

                    } else {

                        selectedIds.remove(playerId);
                    }

                    notifySelectionChanged();
                }
        );

        // ========================================================
        // WHOLE ROW CLICK
        // ========================================================

        holder.itemView.setOnClickListener(
                v -> {

                    int adapterPosition =
                            holder.getBindingAdapterPosition();

                    if (adapterPosition ==
                            RecyclerView.NO_POSITION) {

                        return;
                    }

                    Player clickedPlayer =
                            players.get(
                                    adapterPosition
                            );

                    if (clickedPlayer == null) {
                        return;
                    }

                    long clickedPlayerId =
                            clickedPlayer.getId();

                    boolean currentlySelected =
                            selectedIds.contains(
                                    clickedPlayerId
                            );

                    if (currentlySelected) {

                        selectedIds.remove(
                                clickedPlayerId
                        );
                    } else {

                        if (selectedIds.size()
                                >= maxSelection) {

                            return;
                        }

                        selectedIds.add(
                                clickedPlayerId
                        );
                    }

                    checkbox.setOnCheckedChangeListener(null);

                    checkbox.setChecked(
                            selectedIds.contains(
                                    clickedPlayerId
                            )
                    );

                    checkbox.setOnCheckedChangeListener(
                            (buttonView, isChecked) -> {

                                if (isChecked) {

                                    if (selectedIds.contains(
                                            clickedPlayerId
                                    )) {

                                        return;
                                    }

                                    if (selectedIds.size()
                                            >= maxSelection) {

                                        buttonView.setChecked(
                                                false
                                        );

                                        return;
                                    }

                                    selectedIds.add(
                                            clickedPlayerId
                                    );

                                } else {

                                    selectedIds.remove(
                                            clickedPlayerId
                                    );
                                }

                                notifySelectionChanged();
                            }
                    );

                    notifySelectionChanged();
                }
        );
    }

    // ============================================================
    // ITEM COUNT
    // ============================================================

    @Override
    public int getItemCount() {

        return players.size();
    }

    // ============================================================
    // GET SELECTED IDS
    // ============================================================

    public List<Long> getSelectedIds() {

        return new ArrayList<>(
                selectedIds
        );
    }

    // ============================================================
    // GET SELECTED PLAYERS
    // ============================================================

    public List<Player> getSelectedPlayers() {

        List<Player> selectedPlayers =
                new ArrayList<>();

        for (Player player : players) {

            if (player == null) {
                continue;
            }

            if (selectedIds.contains(
                    player.getId()
            )) {

                selectedPlayers.add(
                        player
                );
            }
        }

        return selectedPlayers;
    }

    // ============================================================
    // SET SELECTED IDS
    // ============================================================

    public void setSelectedIds(
            List<Long> ids
    ) {

        selectedIds.clear();

        if (ids != null) {

            for (Long id : ids) {

                if (id == null) {
                    continue;
                }

                if (selectedIds.size()
                        >= maxSelection) {

                    break;
                }

                selectedIds.add(id);
            }
        }

        notifyDataSetChanged();
        notifySelectionChanged();
    }

    // ============================================================
    // CLEAR SELECTION
    // ============================================================

    public void clearSelection() {

        selectedIds.clear();

        notifyDataSetChanged();
        notifySelectionChanged();
    }

    // ============================================================
    // CHECK WHETHER PLAYER IS SELECTED
    // ============================================================

    public boolean isSelected(
            long playerId
    ) {

        return selectedIds.contains(
                playerId
        );
    }

    // ============================================================
    // SELECT PLAYER
    // ============================================================

    public boolean selectPlayer(
            long playerId
    ) {

        if (selectedIds.contains(
                playerId
        )) {

            return true;
        }

        if (selectedIds.size()
                >= maxSelection) {

            return false;
        }

        selectedIds.add(
                playerId
        );

        notifyDataSetChanged();
        notifySelectionChanged();

        return true;
    }

    // ============================================================
    // UNSELECT PLAYER
    // ============================================================

    public void unselectPlayer(
            long playerId
    ) {

        if (selectedIds.remove(
                playerId
        )) {

            notifyDataSetChanged();
            notifySelectionChanged();
        }
    }

    // ============================================================
    // SELECTED COUNT
    // ============================================================

    public int getSelectedCount() {

        return selectedIds.size();
    }

    // ============================================================
    // MAX SELECTION
    // ============================================================

    public int getMaxSelection() {

        return maxSelection;
    }
}
