package com.elevatemc.potpvp.party.command;

import com.elevatemc.potpvp.PotPvPSI;
import com.elevatemc.potpvp.party.Party;
import com.elevatemc.potpvp.party.PartyHandler;
import com.elevatemc.potpvp.party.PartyInvite;
import com.elevatemc.elib.command.Command;
import com.elevatemc.elib.command.param.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class PartyJoinCommand {

    // default value for password parameter used to detect that password
    // wasn't provided. No Optional<String> :(
    private static final String NO_PASSWORD_PROVIDED = "pelado";

    @Command(names = {"party join", "p join", "t join", "team join", "f join"}, permission = "")
    public static void partyJoin(Player sender, @Parameter(name = "player") Player target, @Parameter(name = "password", defaultValue = NO_PASSWORD_PROVIDED) String providedPassword) {
        PartyHandler partyHandler = PotPvPSI.getInstance().getPartyHandler();
        Party targetParty = partyHandler.getParty(target);

        if (partyHandler.hasParty(sender)) {
            sender.sendMessage(ChatColor.DARK_RED + "✖ " + ChatColor.RED + "You are already in a party. You must leave your current party first.");
            return;
        }

        if (targetParty == null) {
            sender.sendMessage(ChatColor.DARK_RED + "✖ " + ChatColor.RED + target.getName() + " is not in a party.");
            return;
        }

        PartyInvite invite = targetParty.getInvite(sender.getUniqueId());

        switch (targetParty.getAccessRestriction()) {
            case PUBLIC:
                targetParty.join(sender);
                break;
            case INVITE_ONLY:
                if (invite != null) {
                    targetParty.join(sender);
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "✖ " + ChatColor.RED + "You don't have an invitation to this party.");
                }

                break;
            case PASSWORD:
                if (providedPassword.equals(NO_PASSWORD_PROVIDED) && invite == null) {
                    sender.sendMessage(ChatColor.DARK_RED + "✖ " + ChatColor.RED + "You need the password or an invitation to join this party.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "Ω " + ChatColor.AQUA + "To join with a password, use " + ChatColor.DARK_AQUA + "/party join " + target.getName() + " <password>");
                    return;
                }

                String correctPassword = targetParty.getPassword();

                if (invite == null && !correctPassword.equals(providedPassword)) {
                    sender.sendMessage(ChatColor.DARK_RED + "✖ " + ChatColor.RED + "Invalid password.");
                } else {
                    targetParty.join(sender);
                }

                break;
            default:
                break;
        }
    }

}