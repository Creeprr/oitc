package dev.slohth.oitc.user;

import dev.slohth.oitc.game.Game;
import dev.slohth.oitc.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private Game game;

    private PlayerInventory cached;
    private int killstreak = 0;

    public User(@Nonnull UUID uuid) { this.uuid = uuid; }

    public void msg(String... m) { for (String s : m) if (s != null) this.getPlayer().sendMessage(CC.trns(s)); }

    public boolean isInGame() { return this.game != null; }

    public void removeFromGame() { this.game = null; this.killstreak = 0; }

    public boolean joinGame(@Nonnull Game game) {
        if (this.isInGame()) return false;
        if (game.addUser(this)) { this.game = game; return true; }
        return false;
    }

    public void cacheInventory() { this.cached = this.getPlayer().getInventory(); }

    @Nonnull
    public UUID getUuid() { return this.uuid; }

    @Nonnull
    public Player getPlayer() { return Objects.requireNonNull(Bukkit.getPlayer(this.uuid)); }

    @Nullable
    public Game getGame() { return this.game; }

    public int getKillstreak() { return this.killstreak; }

    public void setKillstreak(int killstreak) { this.killstreak = killstreak; }

    @Nullable
    public PlayerInventory getCached() { return this.cached; }

}
