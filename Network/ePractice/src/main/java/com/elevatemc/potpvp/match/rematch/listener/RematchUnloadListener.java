package com.elevatemc.potpvp.match.rematch.listener;

import com.elevatemc.potpvp.PotPvPSI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class RematchUnloadListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PotPvPSI.getInstance().getRematchHandler().unloadRematchData(event.getPlayer());
    }

}