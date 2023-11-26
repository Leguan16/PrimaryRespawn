package at.noahb.primaryrespawn.command;

import at.noahb.primaryrespawn.manager.Manager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ResetRespawnsCommand implements Command {

    private final Manager manager;
    private CommandAPICommand command;

    public ResetRespawnsCommand(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void init() {
        command = new CommandAPICommand("resetrespawns")
                .withPermission("primaryrespawn.reset")
                .withAliases("rr")
                .withArguments(
                        new PlayerArgument("player"))
                .withOptionalArguments(
                        new MultiLiteralArgument("type", "primary", "secondary"))
                .executesNative(this::execute);
        command.register();
    }

    @Override
    public void execute(CommandSender sender, CommandArguments args) {
        if (sender.hasPermission("resetrespawn.reset")) {
            Player player = (Player) args.get("player");
            assert player != null;

            Optional<String> type = args.getOptionalUnchecked("type");

            if (type.isEmpty()) {
                manager.resetRespawns(player.getUniqueId());
                sender.sendRichMessage("<green>Reset all respawns for <gold>" + player.getName());
                return;
            }

            Manager.LocationType locationType = Manager.LocationType.valueOf(type.get().toUpperCase());
            manager.removeLocation(player.getUniqueId(), locationType);
            sender.sendRichMessage("<green>Reset " + locationType.name().toLowerCase() + " respawn for <gold>" + player.getName());
        }
    }
}
