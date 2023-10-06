package com.elevatemc.elib.tab;


import com.elevatemc.elib.eLib;
import com.elevatemc.elib.tab.data.TabList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author ImHacking
 * @date 4/10/2022
 */
public class TabListManager implements Listener {

    @Getter
    @Setter
    private TabList tabList;

    private List<UUID> players = new ArrayList<>();

    public TabListManager() {
        eLib.getInstance().getServer().getPluginManager().registerEvents(this, eLib.getInstance());
        new TabRunnable().runTaskTimerAsynchronously(eLib.getInstance(), 2L, 20L);
    }

    /**
     * Update the players slot in a given row/column with value
     *
     * @param player  the player we are updating
     * @param row     the row of the slot we are updating
     * @param column  the column of the slot we are updating
     * @param value   the value we want put in the slot. Use color codes we don't translate
     */
    public void updateSlot(Player player, int row, int column, String value) {
        if (players.contains(player.getUniqueId())) {
            tabList.updateSlot(player, row, column, value);
        }
    }

    /**
     * Update the players slot with a given UUID in the given row/column with value
     *
     * @param uuid    the uuid of the player we are updating
     * @param row     the row of the slot we are updating
     * @param column  the column of the slot we are updating
     * @param value   the value we want put in the slot. Use color codes we don't translate
     */
    public void updateSlot(UUID uuid, int row, int column, String value) {
        if (players.contains(uuid)) {
            Player player = eLib.getInstance().getServer().getPlayer(uuid);
            if (player != null) {
                tabList.updateSlot(player, row, column, value);
            }
        }
    }

    /**
     * Update every player that has their TabList setup with the value in the given row/column
     *
     * @param row     the row of the slot we ar updating
     * @param column  the column of the slot we are updating
     * @param value   the value we want put in the slot. Use color codes we don't translate
     */
    public void updateGlobally(int row, int column, String value) {
        for (UUID uuid : players) {
            updateSlot(uuid, row, column, value);
        }
    }

    /**
     * Clear the slot of the player at the given row/column
     *
     * @param player  the player we are clearing the slot of
     * @param row     the row of the slot we are clearing
     * @param column  the column of the slot we are clearing
     */
    public void clearSlot(Player player, int row, int column) {
        if (players.contains(player.getUniqueId())) {
            tabList.updateSlot(player, row, column, "");
        }
    }

    /**
     * Clear the slot of the player at the given row/column
     *
     * @param uuid    the uuid of the player we are clearing the slot of
     * @param row     the row of the slot we are clearing
     * @param column  the column of the slot we are clearing
     */
    public void clearSlot(UUID uuid, int row, int column) {
        Player player = eLib.getInstance().getServer().getPlayer(uuid);
        if (player != null) {
            clearSlot(player, row, column);
        }
    }

    /**
     * Clear the slot of every player with the given row/column
     *
     * @param row     the row of the slot we are clearing
     * @param column  the column of the slot we are clearing
     */
    public void clearGlobally(int row, int column) {
        for (UUID uuid : players) {
            clearSlot(uuid, row, column);
        }
    }

    public void removePlayer(UUID uuid) {
        this.players.remove(uuid);
    }

}
