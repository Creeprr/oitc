package dev.slohth.oitc.utils.framework.assemble.adapter;

import dev.slohth.oitc.OITC;
import dev.slohth.oitc.game.GameState;
import dev.slohth.oitc.user.User;
import dev.slohth.oitc.utils.framework.assemble.AssembleAdapter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ScoreboardAdapter implements AssembleAdapter {

    @Override
    public String getTitle(final Player player) {
        return "&3&lOITC";
    }

    @Override
    public List<String> getLines(final Player player) {
        OITC core = JavaPlugin.getPlugin(OITC.class);
        Optional<User> user = core.getUserManager().findById(player.getUniqueId());
        if (!user.isPresent() || !user.get().isInGame() || user.get().getGame().getState() != GameState.ACTIVE) return null;

        List<String> lines = new ArrayList<>();

        LinkedHashMap<User, Integer> topKills = user.get().getGame().getTopKills();
        if (topKills.size() <= 4) {
            for (Map.Entry<User, Integer> entry : topKills.entrySet()) {
                lines.add("&7" + entry.getKey().getPlayer().getName() + ": &3" + entry.getValue());
            }
        } else {
            int index = 1;
            for (Map.Entry<User, Integer> entry : topKills.entrySet()) {
                if (index == 4) break;
                lines.add("&7" + entry.getKey().getPlayer().getName() + ": &3" + entry.getValue());
                index++;
            }
        }

        return lines;
    }
}
