package at.noahb.primaryrespawn.command;

import at.noahb.primaryrespawn.PrimaryRespawn;

import java.util.List;

public class CommandManager {

    private final List<Command> commands;

    public CommandManager(PrimaryRespawn instance) {
        commands = List.of(
                new ResetRespawnsCommand(instance.getManager()),
                new ShowRespawnsCommand(instance.getManager())
        );
    }

    public void init() {
        commands.forEach(Command::init);
    }
}
