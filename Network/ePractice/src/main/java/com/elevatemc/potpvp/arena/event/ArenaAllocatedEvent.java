package com.elevatemc.potpvp.arena.event;

import lombok.Getter;
import com.elevatemc.potpvp.arena.Arena;
import org.bukkit.event.HandlerList;

/**
 * Called when an {@link Arena} is allocated for use by a
 * {@link com.elevatemc.potpvp.match.Match}
 */
public final class ArenaAllocatedEvent extends ArenaEvent {

    @Getter private static HandlerList handlerList = new HandlerList();

    public ArenaAllocatedEvent(Arena arena) {
        super(arena);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}