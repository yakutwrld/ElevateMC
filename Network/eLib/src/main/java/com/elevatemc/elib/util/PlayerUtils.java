package com.elevatemc.elib.util;

import com.elevatemc.elib.eLib;

import com.elevatemc.spigot.viaversion.api.Via;
import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTitle;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.potion.PotionEffect;
import org.github.paperspigot.Title;

import java.lang.reflect.Field;
import java.util.*;


public final class PlayerUtils {

    private static Field STATUS_PACKET_ID_FIELD;
    private static Field STATUS_PACKET_STATUS_FIELD;
    private static Field SPAWN_PACKET_ID_FIELD;

    private static Map<UUID, Integer> cpsCount = new HashMap<>();

    // Static utility class -- cannot be created.
    private PlayerUtils() {
    }

    /**
     * Resets a player's inventory (and other associated data, such as health, food, etc) to their default state.
     *
     * @param player The player to reset
     */
    public static void resetInventory(Player player) {
        resetInventory(player, null);
    }
    public static int getProtocol(Player player) {
        return 47;
    }
    public static boolean hasOpenInventory(Player player) {
        return hasOwnInventoryOpen(player) || hasOtherInventoryOpen(player);
    }

    public static boolean hasOwnInventoryOpen(Player player) {
        return InventoryAdapter.getCurrentlyOpen().contains(player.getUniqueId());
    }


    /**
     * Resets a player's inventory (and other associated data, such as health, food, etc) to their default state.
     *
     * @param player   The player to reset
     * @param gameMode The gamemode to reset the player to. null if their current gamemode should be kept.
     */
    public static void resetInventory(Player player, GameMode gameMode) {
        ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0);
        player.setHealth(player.getMaxHealth());
        player.setFallDistance(0F);
        player.setFoodLevel(20);
        player.setSaturation(10F);
        player.setLevel(0);
        player.setExp(0F);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setFireTicks(0);

        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }

        if (gameMode != null && player.getGameMode() != gameMode) {
            player.setGameMode(gameMode);
        }
    }

    public static Player getDamageSource(Entity damager) {

        Player playerDamager = null;

        if (damager instanceof Player) {
            playerDamager = (Player) damager;
        } else if (damager instanceof Projectile) {

            final Projectile projectile = (Projectile) damager;

            if (projectile.getShooter() instanceof Player) {
                playerDamager = (Player) projectile.getShooter();
            }
        }

        return playerDamager;
    }

    public static boolean hasOtherInventoryOpen(Player player) {
        return ((CraftPlayer) player).getHandle().activeContainer.windowId != 0;
    }

     public static int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    public static void animateDeath(Player player) {

        final int entityId = EntityUtils.getFakeEntityId();
        final PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle());
        final PacketPlayOutEntityStatus statusPacket = new PacketPlayOutEntityStatus();

        try {
            SPAWN_PACKET_ID_FIELD.set(spawnPacket, entityId);
            STATUS_PACKET_ID_FIELD.set(statusPacket, entityId);
            STATUS_PACKET_STATUS_FIELD.set(statusPacket, (byte) 3);

            final int radius = MinecraftServer.getServer().getPlayerList().d();
            final Set<Player> sentTo = new HashSet<>();

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {

                if (!(entity instanceof Player)) {
                    continue;
                }

                final Player watcher = (Player) entity;

                if (watcher.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }

                ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(spawnPacket);
                ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(statusPacket);

                sentTo.add(watcher);
            }

            eLib.getInstance().getServer().getScheduler().runTaskLater(eLib.getInstance(), () -> {

                for (Player watcher : sentTo) {
                    ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityId));
                }

            }, 40L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void animateDeath(Player player, Player watcher) {

        final int entityId = EntityUtils.getFakeEntityId();
        final PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(((CraftPlayer) player).getHandle());
        final PacketPlayOutEntityStatus statusPacket = new PacketPlayOutEntityStatus();

        try {
            SPAWN_PACKET_ID_FIELD.set(spawnPacket, entityId);
            STATUS_PACKET_ID_FIELD.set(statusPacket, entityId);
            STATUS_PACKET_STATUS_FIELD.set(statusPacket, (byte) 3);

            ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(spawnPacket);
            ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(statusPacket);

            eLib.getInstance().getServer().getScheduler().runTaskLater(eLib.getInstance(), () -> ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(new int[]{entityId})), 40L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendTitle(Player player, Title title) {
        // If the player is on 1.7 we try to send the LunarClient title packet
        if (Via.getAPI().getPlayerVersion(player.getUniqueId()) == 5) {
            if (title.getTitle() != null) {
                LunarClientAPI.getInstance().sendPacket(player, new LCPacketTitle("title", BaseComponent.toLegacyText(title.getTitle()), title.getStay() * 50L, title.getFadeIn() * 50L, title.getFadeOut() * 50L));
            }
            if (title.getSubtitle() != null) {
                LunarClientAPI.getInstance().sendPacket(player, new LCPacketTitle("subtitle", BaseComponent.toLegacyText(title.getSubtitle()), title.getStay() * 50L, title.getFadeIn() * 50L, title.getFadeOut() * 50L));
            }
        } else {
            player.sendTitle(title);
        }
    }

    static {
        try {
            STATUS_PACKET_ID_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("a");
            STATUS_PACKET_ID_FIELD.setAccessible(true);
            STATUS_PACKET_STATUS_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("b");
            STATUS_PACKET_STATUS_FIELD.setAccessible(true);
            SPAWN_PACKET_ID_FIELD = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("a");
            SPAWN_PACKET_ID_FIELD.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }

    }
}