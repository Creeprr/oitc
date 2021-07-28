package dev.slohth.oitc;

import dev.slohth.oitc.arena.manager.ArenaManager;
import dev.slohth.oitc.game.listener.GameListener;
import dev.slohth.oitc.game.manager.GameManager;
import dev.slohth.oitc.test.TestCommands;
import dev.slohth.oitc.user.listener.UserListener;
import dev.slohth.oitc.user.manager.UserManager;
import dev.slohth.oitc.utils.framework.Config;
import dev.slohth.oitc.utils.framework.assemble.Assemble;
import dev.slohth.oitc.utils.framework.assemble.adapter.ScoreboardAdapter;
import dev.slohth.oitc.utils.framework.command.Framework;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

public final class OITC extends JavaPlugin {

    private GameManager gameManager;
    private UserManager userManager;
    private ArenaManager arenaManager;

    private Framework framework;
    private Assemble assemble;

    private Location spawnLocation;

    @Override
    public void onEnable() {
        this.framework = new Framework(this);
        this.registerManagers();
        this.registerListeners();

        Bukkit.getScheduler().runTaskLater(this, () -> assemble = new Assemble(this, new ScoreboardAdapter()), 1);

        Bukkit.getOnlinePlayers().forEach(p -> {
            if (!OITC.this.userManager.register(p.getUniqueId())) p.kickPlayer("Failed to authenticate! Please rejoin.");
        });

        try {
            String[] data = Config.ARENAS.getString("lobby").split(",");
            this.spawnLocation = new Location(
                    Bukkit.getWorld(data[0]),
                    Double.parseDouble(data[1]),
                    Double.parseDouble(data[2]),
                    Double.parseDouble(data[3]),
                    Float.parseFloat(data[4]),
                    Float.parseFloat(data[5])
            );
        } catch (NullPointerException | NumberFormatException e) {
            this.spawnLocation = this.getServer().getWorlds().get(0).getSpawnLocation();
        }

        new TestCommands(this);
    }

    private void registerManagers() {
        this.arenaManager = new ArenaManager();
        this.gameManager = new GameManager(this);
        this.userManager = new UserManager();
    }

    private void registerListeners() {
        new UserListener(this);
        new GameListener(this);
    }

    @Nonnull
    public UserManager getUserManager() { return this.userManager; }

    @Nonnull
    public GameManager getGameManager() { return this.gameManager; }

    @Nonnull
    public ArenaManager getArenaManager() { return this.arenaManager; }

    @Nonnull
    public Framework getFramework() { return this.framework; }

    @Nonnull
    public Location getSpawnLocation() { return this.spawnLocation; }
}
