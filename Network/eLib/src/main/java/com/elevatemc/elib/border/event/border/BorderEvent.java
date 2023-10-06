package com.elevatemc.elib.border.event.border;

import lombok.Getter;
import com.elevatemc.elib.border.Border;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BorderEvent extends Event {

    @Getter private static final HandlerList handlerList = new HandlerList();

    @Getter private Border border;

    public BorderEvent(Border border) {
        this.border = border;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }

    public Border getBorder() {
        return this.border;
    }

}

