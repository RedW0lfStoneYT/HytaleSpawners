package dev.selena.hytale.spawners.debug.hud;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.selena.core.util.DebugLogger;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.util.SpawnerUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class DebugHud extends CustomUIHud {
    @Getter
    private final SpawnerBlock spawner;
    private final PlayerRef playerRef;

    public DebugHud(@NotNull PlayerRef playerRef, SpawnerBlock spawner) {
        super(playerRef);
        this.playerRef = playerRef;
        this.spawner = spawner;

    }

    @Override
    protected void build(@NotNull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Hud/Spawners/Debug/debugHud.ui");
        uiCommandBuilder.set("#SpawnerType.Text", "Spawn Type: " + spawner.getSpawnType());

        uiCommandBuilder.set("#TimeLeft.Seconds", setTimeLeftSeconds());
    }

    public int setTimeLeftSeconds() {
        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        DebugLogger.debugInfo("Updating time left");
        return SpawnerUtil.getTimeRemainingSeconds(spawner, world);
    }

}
