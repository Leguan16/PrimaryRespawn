package at.noahb.primaryrespawn.command;

import at.noahb.primaryrespawn.domain.PlayerSpawns;
import at.noahb.primaryrespawn.manager.Manager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowRespawnsCommand implements Command {


    private final Manager manager;
    private CommandAPICommand command;

    public ShowRespawnsCommand(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void init() {
        command = new CommandAPICommand("showrespawns")
                .withPermission("primaryrespawn.show")
                .withAliases("sr")
                .executesPlayer(this::execute);
        command.register();
    }

    @Override
    public void execute(CommandSender sender, CommandArguments args) {
        PlayerSpawns playerSpawns = manager.getSpawnsForPlayer(((Player) sender).getUniqueId());
        sender.sendRichMessage("<green>Primary respawn: <gold>" + playerSpawns.getPrimary());
        sender.sendRichMessage("<green>Secondary respawn: <gold>" + playerSpawns.getSecondary());
        sender.sendRichMessage("<red>Only players can use this command!");
    }
}
