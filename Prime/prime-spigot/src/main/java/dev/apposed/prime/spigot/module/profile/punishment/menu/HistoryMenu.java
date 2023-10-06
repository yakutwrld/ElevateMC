package dev.apposed.prime.spigot.module.profile.punishment.menu;

import com.elevatemc.elib.menu.Button;
import com.elevatemc.elib.menu.pagination.PaginatedMenu;
import com.elevatemc.elib.util.UUIDUtils;
import com.google.common.collect.ImmutableList;
import dev.apposed.prime.spigot.Prime;
import dev.apposed.prime.spigot.PrimeConstants;
import dev.apposed.prime.spigot.module.profile.Profile;
import dev.apposed.prime.spigot.module.profile.ProfileHandler;
import dev.apposed.prime.spigot.module.profile.punishment.Punishment;
import dev.apposed.prime.spigot.module.profile.punishment.type.PunishmentType;
import dev.apposed.prime.spigot.util.Color;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HistoryMenu extends PaginatedMenu {

    private final Prime plugin = Prime.getInstance();
    private final ProfileHandler profileHandler = plugin.getModuleHandler().getModule(ProfileHandler.class);
    private final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm");

    private final PunishmentType type;
    private final Profile profile;
    private final List<Punishment> punishments;
    private final boolean showReceived;

    public HistoryMenu(PunishmentType type, Profile profile, List<Punishment> punishments, boolean showReceived) {
        this.profile = profile;
        this.type = type;
        this.punishments = punishments;
        this.showReceived = showReceived;
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return profile.getUsername() + "'s " + type.getMenu();
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        final AtomicInteger slot = new AtomicInteger(0);
        this.punishments.stream()
                .filter(punishment -> punishment.getType() == this.type)
                .sorted(Comparator.comparingLong(Punishment::getAddedAt).reversed())
                .forEach(punishment -> {
                    buttons.put(slot.getAndIncrement(), new Button() {
                        @Override
                        public String getName(Player player) {
                            return ChatColor.GOLD + format.format(punishment.getAddedAt());
                        }

                        @Override
                        public List<String> getDescription(Player player) {
                            final List<String> lore = new ArrayList<>();
                            lore.add(Color.SPACER_SHORT);
                            if(showReceived) {
                                lore.add("&ePlayer: &c" + UUIDUtils.name(punishment.getPlayer()));
                            }
                            lore.addAll(Arrays.asList(
                                    "&eBy: &c" + (punishment.getAddedBy().equals(PrimeConstants.CONSOLE_UUID) ?
                                            "&4&lConsole" : UUIDUtils.name(punishment.getAddedBy())),
                                    "&eReason: &c" + punishment.getAddedReason(),
                                    "&eRemaining: &c" + punishment.formatDuration(),
                                    Color.SPACER_SHORT
                            ));

                            if(punishment.isRemoved()) {
                                lore.add("&c&lRemoved");
                                lore.add(" ");
                                lore.add("&eBy: &c" + (punishment.getRemovedBy().equals(PrimeConstants.CONSOLE_UUID) ?
                                        "&4&lConsole" : UUIDUtils.name(punishment.getRemovedBy())));
                                lore.add("&eReason: &c" + punishment.getRemovedReason());
                                lore.add(" ");
                                lore.add(ChatColor.GOLD + format.format(punishment.getRemovedAt()));
                                lore.add(Color.SPACER_SHORT);
                            }

                            lore.add("&eClick to view evidence.");
                            if(player.hasPermission("prime.history.detailed") && punishment.hasIp()) {
                                lore.add("&eIP: &c" + punishment.getIp());
                            }
                            lore.add(Color.SPACER_SHORT);

                            return Color.translate(lore);
                        }

                        @Override
                        public Material getMaterial(Player player) {
                            return punishment.getItemStack().getType();
                        }

                        @Override
                        public byte getDamageValue(Player player) {
                            return (byte) punishment.getItemStack().getDurability();
                        }

                        @Override
                        public void clicked(Player player, int slot, ClickType clickType) {
                            new PunishmentEvidenceMenu(profile, punishment).openMenu(player);
                        }
                    });
                });

        return buttons;
    }
}
