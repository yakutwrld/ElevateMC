package com.elevatemc.potpvp.kit.menu.editkit;

import com.elevatemc.potpvp.gamemode.GameMode;
import com.elevatemc.potpvp.gamemode.kit.GameModeKit;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.elevatemc.potpvp.PotPvPSI;
import com.elevatemc.elib.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

final class LoadDefaultKitButton extends Button {

    private final GameModeKit gameModeKit;

    LoadDefaultKitButton(GameModeKit gameModeKit) {
        this.gameModeKit = Preconditions.checkNotNull(gameModeKit, "gameMode");
    }

    @Override
    public String getName(Player player) {
        return ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + "Load default kit";
    }

    @Override
    public List<String> getDescription(Player player) {
        return ImmutableList.of(
            ChatColor.LIGHT_PURPLE + "Click this to load the default kit",
            ChatColor.LIGHT_PURPLE + "into the kit editing menu."
        );
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.STAINED_CLAY;
    }

    @Override
    public byte getDamageValue(Player player) {
        return DyeColor.PURPLE.getWoolData();
    }

    @Override
    public void clicked(final Player player, int slot, ClickType clickType) {
        /* Duplication fix. When players click this button we must set whatever they might have in their hand to air
         * Otherwise they can duplicate items infinitely. This exploits kits like archer and axe pvp. */
        player.setItemOnCursor(new ItemStack(Material.AIR));

        player.getInventory().setContents(gameModeKit.getDefaultInventory());

        Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), player::updateInventory, 1L);
    }

}