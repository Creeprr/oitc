package dev.slohth.oitc.game.listener;

import dev.slohth.oitc.OITC;
import dev.slohth.oitc.game.GameState;
import dev.slohth.oitc.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import javax.annotation.Nonnull;
import java.util.Optional;

public class GameListener implements Listener {

    private final OITC core;

    public GameListener(@Nonnull OITC core) {
        this.core = core;
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    /**
     * Stops players from dropping items if they are in a game
     * @param e The PlayerDropItemEvent
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        core.getUserManager().findById(e.getPlayer().getUniqueId()).ifPresent(p -> e.setCancelled(true));
    }

    /**
     * Handles arrows for one-hit kill, and removes the arrow entity once landed
     * @param e The ProjectileHitEvent
     */
    @EventHandler
    public void onShoot(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player && core.getUserManager().findById(((Player) e.getEntity().getShooter()).getUniqueId()).isPresent()) {
            Optional<User> killer = core.getUserManager().findById(((Player) e.getEntity().getShooter()).getUniqueId());

            if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
                Optional<User> killed = core.getUserManager().findById(e.getHitEntity().getUniqueId());

                if (!killed.isPresent() || !killer.isPresent()) return;

                if (killed.get().isInGame() && killer.get().isInGame() && killed.get().getGame().getState() == GameState.ACTIVE && killed.get().getGame().equals(killer.get().getGame())) {
                    killed.get().getGame().handleRespawn(killed.get(), killer.get());
                    e.getEntity().remove();
                }

            } else {
                if (killer.isPresent() && killer.get().isInGame()) e.getEntity().remove();
            }
        }
    }

    /**
     * Handles melee deaths & cancels damage in lobby
     * @param e The EntityDamageByEntityEvent
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Optional<User> killed = core.getUserManager().findById(e.getEntity().getUniqueId());
            Optional<User> killer = core.getUserManager().findById(e.getDamager().getUniqueId());

            if (!killed.isPresent() || !killer.isPresent()) return;
            if (!killed.get().isInGame() || !killer.get().isInGame() || killed.get().getGame().getState() != GameState.ACTIVE) { e.setCancelled(true); return; }

            if (killed.get().getGame().equals(killer.get().getGame())) {
                if (e.getFinalDamage() >= killed.get().getPlayer().getHealth()) {
                    e.setCancelled(true);
                    killed.get().getGame().handleRespawn(killed.get(), killer.get());
                }
            }
        }
    }

}
