package com.elevatemc.potpvp.events.bukkit.event;

import com.elevatemc.potpvp.events.game.Game;
import com.elevatemc.potpvp.events.game.GameState;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class GameStateChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final GameState to;

    public GameStateChangeEvent(Game game, GameState to) {
        this.game = game;
        this.to = to;
        game.setState(to);
    }


    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
