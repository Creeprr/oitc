package dev.slohth.oitc.game;

import dev.slohth.oitc.OITC;
import dev.slohth.oitc.arena.Arena;
import dev.slohth.oitc.user.User;
import dev.slohth.oitc.utils.CC;
import dev.slohth.oitc.utils.ItemBuilder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Game {

    private final OITC core;
    private final UUID id;

    private final Arena arena;
    private final BukkitTask hotbarMessage;

    private final Map<User, Integer> players;
    private final int minPlayers;
    private final int maxPlayers;

    private GameState state;
    private BukkitTask countdown;

    /**
     * Initiates a new Game which players can join
     * @param core Instance of the plugin
     * @param uuid The ID of the game
     * @param arena The arena the game will take place in
     * @param minPlayers The minimum players required for the game to start
     * @param maxPlayers The maximum players allowed in the game (excluding forcejoin)
     */
    public Game(@Nonnull OITC core, @Nonnull UUID uuid, @Nonnull Arena arena, int minPlayers, int maxPlayers) {
        this.core = core;
        this.id = uuid;
        this.arena = arena;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.players = new HashMap<>();
        this.state = GameState.OPEN;
        this.hotbarMessage = new BukkitRunnable() {
            @Override
            public void run() {
                if (Game.this.state == GameState.ACTIVE) {
                    for (User u : Game.this.players.keySet()) {
                        u.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(CC.trns("&7Kills: " + Game.this.players.get(u))));
                    }
                }
            }
        }.runTaskTimer(core, 20, 20);
    }

    /**
     * Adds a user to the game
     * @param user The user to add
     * @return If they successfully joined
     */
    public boolean addUser(@Nonnull User user) {
        if ((this.players.size() >= maxPlayers || this.state == GameState.ACTIVE)
                && !user.getPlayer().hasPermission("oitc.forcejoin")) return false;
        if (this.players.containsKey(user)) return false;

        this.players.put(user, 0);
        user.cacheInventory();
        user.getPlayer().setGameMode(GameMode.ADVENTURE);

        if (this.players.size() == this.minPlayers && this.state == GameState.OPEN) this.initiateCountdown();
        if (this.state == GameState.ACTIVE) this.handleRespawn(user, null);

        for (User u : Game.this.players.keySet()) u.msg("&3&l(!) &7" + user.getPlayer().getName() + " joined the queue");
        return true;
    }

    /**
     * Removes a user from the game
     * @param user The user to remove
     */
    public void removeUser(@Nonnull User user) {
        this.players.remove(user);
        if (user.getPlayer().isGlowing()) user.getPlayer().setGlowing(false);

        if (user.getCached() != null) {
            user.getPlayer().getInventory().setContents(user.getCached().getContents());
        } else { user.getPlayer().getInventory().clear(); }

        user.getPlayer().setGameMode(GameMode.ADVENTURE);
        user.getPlayer().teleport(core.getSpawnLocation());
        user.removeFromGame();

        if (this.state == GameState.COUNTDOWN && this.players.size() < this.minPlayers) {
            this.countdown.cancel();
            for (User u : Game.this.players.keySet()) u.msg("&3&l(!) &7Not enough players to start!");
        }

        if (this.players.size() == 1 && this.state != GameState.ENDED) this.end(this.players.keySet().stream().findFirst().get());
    }

    /**
     * Handles respawns, giving items and updating stats
     * @param killed The user who was killed
     * @param killer The killer (null if none)
     */
    public void handleRespawn(@Nonnull User killed, @Nullable User killer) {
        if (killer != null && this.players.containsKey(killer)) {
            this.players.replace(killer, this.players.get(killer) + 1);
            if (this.players.get(killer) >= 20) this.end(killer);
            killer.getPlayer().setHealth(20.0);
            killer.getPlayer().setSaturation(20.0F);
            killer.getPlayer().getInventory().addItem(new ItemBuilder(Material.ARROW).build());
        }

        killed.setKillstreak(0);
        killed.getPlayer().getInventory().clear();
        killed.getPlayer().setGameMode(GameMode.SPECTATOR);
        killed.getPlayer().sendTitle(CC.trns("&cYou died!"), CC.trns("&7Respawning..."), 20, 20, 20);

        if (killer != null) {
            killer.getPlayer().playEffect(killed.getPlayer().getLocation().add(0, 0.5, 0), Effect.MOBSPAWNER_FLAMES, 1);
            killer.getPlayer().playSound(killer.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100, 0);
            killer.setKillstreak(killer.getKillstreak() + 1);
            if (killer.getKillstreak() % 5 == 0) killer.getPlayer().getWorld().strikeLightningEffect(killer.getPlayer().getLocation());
            for (User u : this.players.keySet())
                u.msg("&3" + killed.getPlayer().getName() + " &7was killed by &3" + killer.getPlayer().getName() +
                        (killer.getKillstreak() == 1 ? "" : "&7 (" + (killer.getKillstreak() % 5 == 0 ? "&6" : "") + killer.getKillstreak() + "x&7)"));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Game.this.state == GameState.ENDED) { this.cancel(); return; }
                Game.this.handleInventory(killed);
                killed.getPlayer().setGameMode(GameMode.ADVENTURE);
                killed.getPlayer().teleport(Game.this.arena.getRandomSpawn());
            }
        }.runTaskLater(core, 20 * 5);
    }

    /**
     * Handles giving items to user on respawn
     * @param user The user to give items to
     */
    private void handleInventory(@Nonnull User user) {
        user.getPlayer().getInventory().clear();
        if (this.state == GameState.ENDED) return;
        user.getPlayer().getInventory().addItem(
                new ItemBuilder(Material.WOODEN_SWORD).unbreakable(true).build(),
                new ItemBuilder(Material.BOW).unbreakable(true).build(),
                new ItemBuilder(Material.ARROW).build()
        );
        user.getPlayer().setHealth(20.0);
        user.getPlayer().setSaturation(20.0F);
        user.getPlayer().setFoodLevel(20);
    }

    /**
     * Starts the game countdown
     */
    private void initiateCountdown() {
        this.state = GameState.COUNTDOWN;
        if (this.countdown != null && !this.countdown.isCancelled()) this.countdown.cancel();
        this.countdown = new BukkitRunnable() {
            int time = 15;
            @Override
            public void run() {
                if (time == 0) { Game.this.start(); this.cancel(); return; }
                if (!(time > 5) || time == 15) {
                    for (User u : Game.this.players.keySet())
                        u.msg("&3&l(!) &7Starting in " + time + (time == 1 ? " second..." : " seconds..."));
                }
                time--;
            }
        }.runTaskTimer(core, 20L, 20L);
    }

    /**
     * Starts the game (called after countdown ends)
     */
    private void start() {
        this.state = GameState.ACTIVE;
        for (User u : this.players.keySet()) {
            u.getPlayer().teleport(this.arena.getRandomSpawn());
            this.handleInventory(u);
        }
    }

    /**
     * Ends the game
     * @param winner The user who won the game
     */
    private void end(@Nonnull User winner) {
        this.state = GameState.ENDED;
        this.hotbarMessage.cancel();

        List<String> message = new ArrayList<>(Collections.singletonList("&7Top killers this game:"));
        int index = 1;
        for (Map.Entry<User, Integer> entry : this.getTopKills().entrySet()) {
            message.add("&3" + index + ": &7" + entry.getKey().getPlayer().getName() + " - " + entry.getValue()
                    + (entry.getValue() == 1 ? " kill" : " kills"));
            index++;
        }

        for (User u : this.players.keySet()) {
            if (!u.equals(winner)) u.getPlayer().setGameMode(GameMode.SPECTATOR);
            u.getPlayer().sendTitle(ChatColor.GOLD + winner.getPlayer().getName(), "Won the game!", 20, 20, 20);
            Bukkit.getScheduler().runTaskLater(core, () -> u.msg(CC.trns(message).toArray(new String[0])), 20);
            Bukkit.getScheduler().runTaskLater(core, () -> Game.this.removeUser(u), 20 * 5);
        }

        winner.getPlayer().getInventory().setHelmet(new ItemBuilder(Material.GOLDEN_HELMET).enchantment(Enchantment.DURABILITY).build());
        winner.getPlayer().setGlowing(true);
    }

    /**
     * Returns the top killers of the game, with their kills
     * @return The top killers
     */
    @Nonnull
    public LinkedHashMap<User, Integer> getTopKills() {
        LinkedHashMap<User, Integer> topKills = new LinkedHashMap<>();
        List<User> keys = new ArrayList<>();
        this.players.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> {
            keys.add(e.getKey());
        });
        Collections.reverse(keys);
        for (User u : keys) topKills.put(u, this.players.get(u));
        return topKills;
    }

    @Nonnull
    public UUID getId() { return this.id; }

    @Nonnull
    public GameState getState() { return this.state; }

}
