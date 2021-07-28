package dev.slohth.oitc.utils.framework.assemble;

import dev.slohth.oitc.utils.framework.assemble.events.AssembleBoardCreateEvent;
import dev.slohth.oitc.utils.framework.assemble.events.AssembleBoardDestroyEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AssembleListener implements Listener {

	private final Assemble assemble;

	public AssembleListener(Assemble assemble) {
		this.assemble = assemble;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		AssembleBoardCreateEvent createEvent = new AssembleBoardCreateEvent(e.getPlayer());
		Bukkit.getServer().getPluginManager().callEvent(createEvent);

		if (createEvent.isCancelled())
			return;

		this.assemble.getBoards().put(e.getPlayer().getUniqueId(), new AssembleBoard(e.getPlayer(), this.assemble));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		AssembleBoardDestroyEvent destroyEvent = new AssembleBoardDestroyEvent(e.getPlayer());
		Bukkit.getServer().getPluginManager().callEvent(destroyEvent);

		if (destroyEvent.isCancelled())
			return;

		this.assemble.getBoards().remove(e.getPlayer().getUniqueId());
		e.getPlayer().setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
	}

}
