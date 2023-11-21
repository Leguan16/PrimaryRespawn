package at.noahb.primaryrespawn.command;

import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;

public interface Command {

    void init();

    void execute(CommandSender sender, CommandArguments args);

}
