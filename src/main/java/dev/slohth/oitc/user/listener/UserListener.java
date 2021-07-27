package dev.slohth.oitc.user.listener;

import dev.slohth.oitc.OITC;
import dev.slohth.oitc.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nonnull;

public class UserListener implements Listener {

    private final OITC core;

    public UserListener(@Nonnull OITC core) {
        this.core = core;
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        core.getUserManager().register(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        core.getUserManager().findById(e.getPlayer().getUniqueId()).ifPresent(p -> {
            User user = core.getUserManager().findById(e.getPlayer().getUniqueId()).get();
            if (user.isInGame()) user.getGame().removeUser(user);
            core.getUserManager().unregister(user.getUuid());
        });
    }

}
