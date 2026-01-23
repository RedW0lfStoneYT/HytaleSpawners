package dev.selena.hytale.spawners;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.selena.core.HytaleCore;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.commands.SpawnerGiveCommand;
import dev.selena.hytale.spawners.systems.SpawnerBlockSystem;
import dev.selena.hytale.spawners.util.config.Configs;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SpawnerMain extends JavaPlugin {

    @Getter
    private ComponentType<ChunkStore, SpawnerBlock> spawnerBlockComponentType;
    private static SpawnerMain instance;

    public SpawnerMain(@NotNull JavaPluginInit init) {
        super(init);
        instance = this;
        HytaleCore.setupCore(this);
        HytaleCore.loadAllConfigs(Configs.class);
    }

    @Override
    protected void setup() {

        this.spawnerBlockComponentType = getChunkStoreRegistry().registerComponent(SpawnerBlock.class, "Spawner", SpawnerBlock.CODEC);
        getEntityStoreRegistry().registerSystem(new SpawnerBlockSystem.SpawnerPlaceSystem());
        getChunkStoreRegistry().registerSystem(new SpawnerBlockSystem.Ticking());
        getChunkStoreRegistry().registerSystem(new SpawnerBlockSystem.OnSpawnerBlockAdd());
        getCommandRegistry().registerCommand(new SpawnerGiveCommand());

    }


    public static SpawnerMain get() {
        return instance;
    }
}
