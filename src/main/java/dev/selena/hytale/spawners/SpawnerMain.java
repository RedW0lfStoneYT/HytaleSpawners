package dev.selena.hytale.spawners;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.selena.core.HytaleCore;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.commands.SpawnerGiveCommand;
import dev.selena.hytale.spawners.components.DisplayEntityComponent;
import dev.selena.hytale.spawners.components.NerfedMobComponent;
import dev.selena.hytale.spawners.systems.SpawnerBlockSystem;
import dev.selena.hytale.spawners.util.config.Config;
import dev.selena.hytale.spawners.util.config.Configs;
import dev.selena.hytale.spawners.util.config.Lang;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SpawnerMain extends JavaPlugin {

    @Getter
    private ComponentType<ChunkStore, SpawnerBlock> spawnerBlockComponentType;
    @Getter
    private ComponentType<EntityStore, DisplayEntityComponent> spawnerEntityComponentType;
    private static SpawnerMain instance;
    @Getter
    private Config config;
    @Getter
    private Lang lang;

    public SpawnerMain(@NotNull JavaPluginInit init) {
        super(init);
        instance = this;
        HytaleCore.setupCore(this);
        HytaleCore.loadAllConfigs(Configs.class);
        config = Configs.CONFIG.getConfig();
        lang = Configs.LANG.getConfig();
    }

    @Override
    protected void setup() {

        this.spawnerBlockComponentType = getChunkStoreRegistry().registerComponent(SpawnerBlock.class, "Spawner", SpawnerBlock.CODEC);
        this.spawnerEntityComponentType = getEntityStoreRegistry().registerComponent(DisplayEntityComponent.class, "DisplayEntity", DisplayEntityComponent.CODEC);
        NerfedMobComponent.setComponentType(getEntityStoreRegistry().registerComponent(NerfedMobComponent.class, "NerfedMob", NerfedMobComponent.CODEC));
        getEntityStoreRegistry().registerSystem(new SpawnerBlockSystem.SpawnerPlaceSystem());
        if (Config.get().isRotatePreviewEntity())
            getEntityStoreRegistry().registerSystem(new SpawnerBlockSystem.PreviewRotating());
        getChunkStoreRegistry().registerSystem(new SpawnerBlockSystem.Ticking());
        getChunkStoreRegistry().registerSystem(new SpawnerBlockSystem.OnSpawnerBlockAdd());
        getEntityStoreRegistry().registerSystem(new SpawnerBlockSystem.NerfedMobDeath());
        getCommandRegistry().registerCommand(new SpawnerGiveCommand());

    }


    public static SpawnerMain get() {
        return instance;
    }
}
