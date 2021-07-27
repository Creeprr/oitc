package dev.slohth.oitc.arena.manager;

import dev.slohth.oitc.arena.Arena;
import dev.slohth.oitc.utils.framework.Config;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArenaManager {

    private final Map<String, Arena> arenas;

    public ArenaManager() {
        this.arenas = new HashMap<>();
        for (String a : Config.ARENAS.getConfig().getConfigurationSection("arenas").getKeys(false)) this.register(a);
    }

    public void register(@Nonnull String name) {
        this.arenas.put(name, new Arena(name).load());
    }

    @Nonnull
    public Optional<Arena> getArena(@Nonnull String name) {
        return Optional.ofNullable(arenas.get(name));
    }

}
