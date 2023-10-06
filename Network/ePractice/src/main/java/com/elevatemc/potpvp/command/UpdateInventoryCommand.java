package com.elevatemc.potpvp.command;

import com.elevatemc.potpvp.util.InventoryUtils;
import com.elevatemc.elib.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * /updateInventory command, typically only used for debugging inventory
 * issues. Available to all players to enforce the constraint that
 * {@link com.elevatemc.potpvp.util.InventoryUtils#resetInventoryDelayed(Player)}
 * can always be called at any time.
 */
public final class UpdateInventoryCommand {

    @Command(names = {"updateinventory", "updateinv", "upinv", "ui"}, permission = "")
    public static void updateInventory(Player sender) {
        InventoryUtils.resetInventoryDelayed(sender);
        sender.sendMessage(ChatColor.GREEN + "Updated your inventory.");
    }

}