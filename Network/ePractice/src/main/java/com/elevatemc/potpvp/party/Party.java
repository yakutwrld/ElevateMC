package com.elevatemc.potpvp.party;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import com.elevatemc.potpvp.PotPvPSI;
import com.elevatemc.potpvp.party.event.*;
import com.elevatemc.potpvp.pvpclasses.PvPClasses;
import com.elevatemc.potpvp.util.InventoryUtils;
import com.elevatemc.potpvp.util.VisibilityUtils;
import com.elevatemc.potpvp.validation.PotPvPValidation;
import com.elevatemc.elib.eLib;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Represents a collection of players which can perform
 * various actions (ex queue, have elo, etc) together.
 *
 * All members, the leader, and all {@link PartyInvite}
 * targets (although not senders) are guaranteed to be online.
 */
public final class Party {

    /**
     * {@link UUID} of this party
     * 
     * New UUID = new Party
     */
    @Getter private final UUID partyId = new UUID(PotPvPSI.RANDOM.nextLong(), PotPvPSI.RANDOM.nextLong());

    // the maximum party size for non-op leaders
    public static final int MAX_SIZE = 50;

    /**
     * Leader of the party, given permission to perform
     * administrative commands (and perform actions like queueing)
     * on behalf of the party. Guaranteed to be online.
     */
    @Getter private UUID leader;

    @Getter private Map<UUID, PvPClasses> kits = new HashMap<>();

    /**
     * All players who are currently part of this party.
     * Each player will only be a member of one party at a time.
     * Guaranteed to all be online.
     */
    private final Set<UUID> members = Sets.newLinkedHashSet();

    /**
     * All active (non-expired) {@link PartyInvite}s. Players can have
     * active invitations from more than one party at a time. All targets
     * (but not senders) are guaranteed to be online.
     */
    private final Set<PartyInvite> invites = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Current access restriction in place for joining this party
     * @see PartyAccessRestriction
     */
    @Getter @Setter private PartyAccessRestriction accessRestriction = PartyAccessRestriction.INVITE_ONLY;

    /**
     * Password requires to join this party, only active if
     * {@link #accessRestriction} is {@link PartyAccessRestriction#PASSWORD}.
     * @see PartyAccessRestriction#PASSWORD
     */
    @Getter @Setter private String password = null;

    Party(UUID leader) {
        this.leader = Preconditions.checkNotNull(leader, "leader");
        this.members.add(leader);

        PotPvPSI.getInstance().getPartyHandler().updatePartyCache(leader, this);
        Bukkit.getPluginManager().callEvent(new PartyCreateEvent(this));
    }

    /**
     * Checks if the player provided is a member of this party
     * @param playerUuid the player to check
     * @return true if the player provided is a member of this party,
     *          false otherwise.
     */
    public boolean isMember(UUID playerUuid) {
        return members.contains(playerUuid);
    }

    /**
     * Checks if the player provided is the leader of this party
     * @param playerUuid the player to check
     * @return true if the player provided is the leader of this party,
     *          false otherwise.
     */
    public boolean isLeader(UUID playerUuid) {
        return leader.equals(playerUuid);
    }

    /**
     * Gets an immutable set of all players currently
     * in this party.
     * @see Party#members
     * @return immutable set of all members
     */
    public Set<UUID> getMembers() {
        return ImmutableSet.copyOf(members);
    }

    /**
     * Gets an immutable set of all active {@link PartyInvite}s
     * @return immutable set of all active invites
     */
    public Set<PartyInvite> getInvites() {
        return ImmutableSet.copyOf(invites);
    }

    /**
     * Finds an active {@link PartyInvite} whose target is equal to the
     * player provided
     * @param target player who must match the result's {@link PartyInvite#getTarget()}
     * @return a PartyInvite targeting the player provided, if one exists
     */
    public PartyInvite getInvite(UUID target) {
        for (PartyInvite invite : invites) {
            if (invite.getTarget().equals(target)) {
                return invite;
            }
        }

        return null;
    }

    public void revokeInvite(PartyInvite invite) {
        invites.remove(invite);
    }

    public void invite(Player target) {
        PartyInvite invite = new PartyInvite(this, target.getUniqueId());

        target.spigot().sendMessage(PartyLang.inviteAcceptPrompt(this));
        message(ChatColor.DARK_AQUA + "✎ " + ChatColor.AQUA + target.getName() + " has been invited to join your party.");

        invites.add(invite);
        Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), () -> invites.remove(invite), PartyHandler.INVITE_EXPIRATION_SECONDS * 20);
    }

    public void join(Player player) {
        if (members.contains(player.getUniqueId())) {
            return;
        }

        if (!PotPvPValidation.canJoinParty(player, this)) {
            return;
        }

        PartyInvite invite = getInvite(player.getUniqueId());

        if (invite != null) {
            revokeInvite(invite);
        }

        Player leaderBukkit = Bukkit.getPlayer(leader);
        player.sendMessage(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "✔ " + ChatColor.GREEN + "You have joined " + leaderBukkit.getName() + "'s party.");

        message(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "✔ " + ChatColor.DARK_AQUA + player.getName() +  ChatColor.AQUA + " has joined your party.");

        members.add(player.getUniqueId());
        PotPvPSI.getInstance().getPartyHandler().updatePartyCache(player.getUniqueId(), this);

        Bukkit.getPluginManager().callEvent(new PartyMemberJoinEvent(player, this));

        forEachOnline(VisibilityUtils::updateVisibility);
        resetInventoriesDelayed();
    }

    public void leave(Player player) {
        if (isLeader(player.getUniqueId()) && members.size() == 1) {
            disband();
            return;
        }

        if (!members.remove(player.getUniqueId())) {
            return;
        }

        PotPvPSI.getInstance().getPartyHandler().updatePartyCache(player.getUniqueId(), null);

        // randomly elect new leader if needed
        if (leader.equals(player.getUniqueId())) {
            UUID[] membersArray = members.toArray(new UUID[members.size()]);
            Player newLeader = Bukkit.getPlayer(membersArray[PotPvPSI.RANDOM.nextInt(membersArray.length)]);

            this.leader = newLeader.getUniqueId();
            message(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "♛ " + ChatColor.DARK_AQUA + newLeader.getName() + ChatColor.AQUA + " has been randomly promoted to leader of your party.");
        }

        player.sendMessage(org.bukkit.ChatColor.DARK_RED + "✖ " + ChatColor.RED + "You have left your party.");
        message(ChatColor.DARK_AQUA + "✖ " + player.getName() + ChatColor.AQUA + " has left your party.");

        VisibilityUtils.updateVisibility(player);
        forEachOnline(VisibilityUtils::updateVisibility);

        Bukkit.getPluginManager().callEvent(new PartyMemberLeaveEvent(player, this));

        InventoryUtils.resetInventoryDelayed(player);
        resetInventoriesDelayed();
    }

    public void setLeader(Player player) {
        this.leader = player.getUniqueId();

        message(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "♛ " + ChatColor.DARK_AQUA + player.getName() + ChatColor.AQUA + " has been promoted to leader of your party.");
        resetInventoriesDelayed();
    }

    public void disband() {
        Bukkit.getPluginManager().callEvent(new PartyDisbandEvent(this));
        PotPvPSI.getInstance().getPartyHandler().unregisterParty(this);

        forEachOnline(player -> {
            VisibilityUtils.updateVisibility(player);
            PotPvPSI.getInstance().getPartyHandler().updatePartyCache(player.getUniqueId(), null);
        });

        message(ChatColor.DARK_RED + "✖ " + ChatColor.RED + "Your party has been disbanded.");
        resetInventoriesDelayed();
    }

    public void kick(Player player) {
        if (!members.remove(player.getUniqueId())) {
            return;
        }

        PotPvPSI.getInstance().getPartyHandler().updatePartyCache(player.getUniqueId(), null);

        player.sendMessage(ChatColor.DARK_RED + "✖ " + ChatColor.RED + "You have been kicked from your party.");
        message(ChatColor.DARK_AQUA + "✎ " + ChatColor.AQUA + player.getName() + " has been kicked from your party.");

        VisibilityUtils.updateVisibility(player);
        forEachOnline(VisibilityUtils::updateVisibility);

        Bukkit.getPluginManager().callEvent(new PartyMemberKickEvent(player, this));

        InventoryUtils.resetInventoryDelayed(player);
        resetInventoriesDelayed();
    }

    /**
     * Sends a basic chat message to all members
     * @param message the message to send
     */
    public void message(String message) {
        forEachOnline(p -> p.sendMessage(message));
    }

    /**
     * Plays a sound for all members
     * @param sound the Sound to play
     * @param pitch the pitch to play the provided sound at
     */
    public void playSound(Sound sound, float pitch) {
        forEachOnline(p -> p.playSound(p.getLocation(), sound, 10F, pitch));
    }

    /**
     * Resets all members' inventories
     * @see InventoryUtils#resetInventoryDelayed(Player)
     */
    public void resetInventoriesDelayed() {
        // we use one runnable and then call resetInventoriesNow instead of
        // directly using to InventoryUtils#resetInventoryDelayed to reduce
        // the number of tasks we submit to the scheduler
        Bukkit.getScheduler().runTaskLater(PotPvPSI.getInstance(), this::resetInventoriesNow, InventoryUtils.RESET_DELAY_TICKS);
    }

    /**
     * Resets all members' inventories
     * @see InventoryUtils#resetInventoryNow(Player)
     */
    public void resetInventoriesNow() {
        forEachOnline(InventoryUtils::resetInventoryNow);
    }

    private void forEachOnline(Consumer<Player> consumer) {
        for (UUID member : members) {
            Player memberBukkit = Bukkit.getPlayer(member);

            if (memberBukkit != null) {
                consumer.accept(memberBukkit);
            }
        }
    }

}