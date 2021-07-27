package dev.slohth.oitc.arena;

import dev.slohth.oitc.utils.framework.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Arena {

    private final String name;
    private final List<Location> spawnLocations;

    public Arena(@Nonnull String name) {
        this.name = name;
        this.spawnLocations = new ArrayList<>();
    }

    public Arena load() {
        try {
            for (String l : Config.ARENAS.getStringList("arenas." + this.name + ".spawn-locations")) {
                String[] data = l.split(",");
                Location location = new Location(
                        Bukkit.getWorld(data[0]),
                        Double.parseDouble(data[1]),
                        Double.parseDouble(data[2]),
                        Double.parseDouble(data[3]),
                        Float.parseFloat(data[4]),
                        Float.parseFloat(data[5])
                );
                this.spawnLocations.add(location);
            }
        } catch (NullPointerException ignored) {}
        return this;
    }

    @Nonnull
    public Location getRandomSpawn() {
        if (spawnLocations.isEmpty()) return Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
        return this.spawnLocations.get(ThreadLocalRandom.current().nextInt(this.spawnLocations.size()));
    }

}
