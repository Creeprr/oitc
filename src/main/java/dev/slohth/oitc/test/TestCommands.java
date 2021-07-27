package dev.slohth.oitc.test;

import dev.slohth.oitc.OITC;
import dev.slohth.oitc.user.User;
import dev.slohth.oitc.utils.framework.command.Args;
import dev.slohth.oitc.utils.framework.command.Command;
import org.bukkit.Bukkit;

public class TestCommands {

    private final OITC core;

    public TestCommands(OITC core) {
        this.core = core;
        core.getFramework().registerCommands(this);
    }

    @Command(name = "game", inGameOnly = true)
    public void gameCommand(Args args) {
        User user = core.getUserManager().findById(args.getPlayer().getUniqueId()).get();
        user.joinGame(core.getGameManager().register(core.getArenaManager().getArena("basic").get(), 2, 8));
    }

    @Command(name = "join", inGameOnly = true)
    public void joinCommand(Args args) {
        User user = core.getUserManager().findById(args.getPlayer().getUniqueId()).get();
        User target = core.getUserManager().findById(Bukkit.getPlayer(args.getArgs(0)).getUniqueId()).get();
        user.joinGame(target.getGame());
    }

}
