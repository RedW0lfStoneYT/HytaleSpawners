package dev.selena.hytale.spawners.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.jetbrains.annotations.NotNull;

public class SpawnerAdminCommand extends CommandBase {
    public SpawnerAdminCommand() {
        super("SpawnerAdmin", "The base admin command for spawners");
        addAliases("sadmin");
        addSubCommand(new SpawnerShowDebugCommand());
        addSubCommand(new ReloadCommand());
        requirePermission("spawner.admin");
    }

    @Override
    protected void executeSync(@NotNull CommandContext commandContext) {

    }
}
