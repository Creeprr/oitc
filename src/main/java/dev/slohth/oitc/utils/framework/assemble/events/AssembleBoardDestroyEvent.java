package dev.slohth.oitc.utils.framework.assemble.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AssembleBoardDestroyEvent extends Event implements Cancellable {

    public static HandlerList handlerList = new HandlerList();

    private final Player player;
    private boolean cancelled = false;

    public AssembleBoardDestroyEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
