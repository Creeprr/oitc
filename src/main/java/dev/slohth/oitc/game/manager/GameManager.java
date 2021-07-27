package dev.slohth.oitc.game.manager;

import dev.slohth.oitc.OITC;
import dev.slohth.oitc.arena.Arena;
import dev.slohth.oitc.game.Game;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GameManager {

    private final OITC core;
    private final Map<UUID, Game> games;

    public GameManager(@Nonnull OITC core) {
        this.core = core;
        this.games = new HashMap<>();
    }

    @Nonnull
    public Game register(@Nonnull Arena arena, int minPlayers, int maxPlayers) {
        Game game = new Game(core, UUID.randomUUID(), arena, minPlayers, maxPlayers);
        this.games.put(game.getId(), game);
        return game;
    }

    public void unregister(@Nonnull Game game) {
        if (this.games.get(game.getId()) == null) return;
        this.games.remove(game.getId());
    }

    @Nonnull
    public Optional<Game> findById(@Nonnull UUID id) {
        return Optional.ofNullable(games.get(id));
    }

}
