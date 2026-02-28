package dev.selena.hytale.spawners.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.selena.hytale.spawners.SpawnerMain;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends CommandBase {
    protected ReloadCommand() {
        super("reload", "Reloads the spawner configs");
        requirePermission("spawner.admin.reload");
    }

    @Override
    protected void executeSync(@NotNull CommandContext commandContext) {
        SpawnerMain.get().loadConfig();
        commandContext.sender().sendMessage(Message.raw("Reloaded all config files"));
    }
}
