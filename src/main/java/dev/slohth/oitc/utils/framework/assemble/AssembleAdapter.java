package dev.slohth.oitc.utils.framework.assemble;

import org.bukkit.entity.Player;

import java.util.List;

public interface AssembleAdapter {

	String getTitle(Player player);

	List<String> getLines(Player player);

}
