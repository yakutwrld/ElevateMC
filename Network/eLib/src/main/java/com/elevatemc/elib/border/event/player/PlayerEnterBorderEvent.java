package com.elevatemc.elib.border.event.player;

import lombok.Getter;
import com.elevatemc.elib.border.Border;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerEnterBorderEvent extends PlayerBorderEvent {

    @Getter private final Location from;
    @Getter private final Location to;

    public PlayerEnterBorderEvent(Border border, Player player, Location from, Location to) {
        super(border, player);
        this.from = from;
        this.to = to;
    }


}
