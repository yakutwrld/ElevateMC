package com.elevatemc.potpvp.arena.command;

import com.elevatemc.potpvp.PotPvPSI;
import com.elevatemc.elib.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class ArenaFreeCommand {

    @Command(names = { "arena free" }, permission = "op", description = "Free the arena grid")
    public static void arenaFree(Player sender) {
        PotPvPSI.getInstance().getArenaHandler().getGrid().free();
        sender.sendMessage(ChatColor.GREEN + "Arena grid has been freed.");
    }

}