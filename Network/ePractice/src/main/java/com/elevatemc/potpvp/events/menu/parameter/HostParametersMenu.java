package com.elevatemc.potpvp.events.menu.parameter;

import com.elevatemc.elib.menu.Button;
import com.elevatemc.elib.menu.Menu;
import com.elevatemc.potpvp.events.event.GameEvent;
import com.elevatemc.potpvp.events.game.Game;
import com.elevatemc.potpvp.events.game.GameQueue;
import com.elevatemc.potpvp.events.parameter.GameParameter;
import com.elevatemc.potpvp.events.parameter.GameParameterOption;
import com.elevatemc.potpvp.util.Color;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class HostParametersMenu extends Menu {

    private final GameEvent event;
    private final List<HostParameterButton> buttons = new ArrayList<>();

    public HostParametersMenu(GameEvent event) {
        setUpdateAfterClick(true);
        setPlaceholder(true);

        for (GameParameter parameter : event.getParameters()) {
            buttons.add(new HostParameterButton(parameter));
        }

        this.event = event;
    }

    @Override
    public String getTitle(Player player) {
        return Color.translate("&3" + event.getName() + " options");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> toReturn = new HashMap<>();

        for (HostParameterButton button : buttons) {
            toReturn.put(toReturn.size(), button);
        }

        toReturn.put(8, new Button() { // todo change although i doubt one event would ever have more than 8 parameters lol
            @Override
            public String getName(Player player) {
                return ChatColor.GREEN + "Start " + event.getName();
            }

            @Override
            public List<String> getDescription(Player player) {
                return Collections.singletonList(ChatColor.GRAY + "Click to start the event.");
            }

            @Override
            public Material getMaterial(Player player) {
                return Material.EMERALD;
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType) {
                for (Game game : GameQueue.INSTANCE.getGames()) {
                    if (game.getHost().equals(player)) {
                        player.sendMessage(ChatColor.RED + "You've already queued an event!");
                        player.closeInventory();
                        return;
                    }
                }

                if (GameQueue.INSTANCE.size() > 9) {
                    player.sendMessage(ChatColor.RED + "The game queue is currently full! Try again later.");
                } else {
                    List<GameParameterOption> options = new ArrayList<>();

                    for (HostParameterButton hostParameterButton : buttons) {
                        options.add(hostParameterButton.getSelectedOption());
                    }

                    GameQueue.INSTANCE.add(new Game(event, player, options));
                    player.sendMessage(ChatColor.GREEN + "You've added a " + event.getName().toLowerCase() + " event to the queue.");
                }

                player.closeInventory();
            }
        });

        return toReturn;
    }

}
