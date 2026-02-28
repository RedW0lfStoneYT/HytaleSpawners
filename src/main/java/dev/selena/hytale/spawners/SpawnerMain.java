package dev.selena.hytale.spawners;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.selena.core.HytaleCore;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.commands.SpawnerAdminCommand;
import dev.selena.hytale.spawners.commands.SpawnerGiveCommand;
import dev.selena.hytale.spawners.components.DisplayEntityComponent;
import dev.selena.hytale.spawners.components.NerfedMobComponent;
import dev.selena.hytale.spawners.systems.SpawnerBlockSystem;
import dev.selena.hytale.spawners.util.config.Config;
import dev.selena.hytale.spawners.util.config.Configs;
import dev.selena.hytale.spawners.util.config.Lang;
import dev.selena.hytale.spawners.util.dynamictooltips.providers.CustomTooltipProvider;
import dev.selena.hytale.spawners.util.dynamictooltips.providers.RenameTooltipProvider;
import lombok.Getter;
import org.herolias.tooltips.DynamicTooltipsLib;
import org.herolias.tooltips.api.DynamicTooltipsApi;
import org.herolias.tooltips.api.DynamicTooltipsApiProvider;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

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
    @Getter
    private DynamicTooltipsApi tooltipsApi;

    public SpawnerMain(@NotNull JavaPluginInit init) {
        super(init);
        instance = this;
        HytaleCore.setupCore(this);
        loadConfig();
    }

    @Override
    protected void setup() {
        new DynamicTooltipsLib(this).setup();
        tooltipsApi = DynamicTooltipsApiProvider.get();
        if (tooltipsApi == null) {
            getLogger().at(Level.SEVERE).log("DynamicTooltipsLib API not available! Is the library installed?");
            return;
        }
        tooltipsApi.registerProvider(new RenameTooltipProvider());
        tooltipsApi.registerProvider(new CustomTooltipProvider());

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
        getCommandRegistry().registerCommand(new SpawnerAdminCommand());

    }

    public void loadConfig() {
        config = Configs.CONFIG.getConfig();
        lang = Configs.LANG.getConfig();
    }


    public static SpawnerMain get() {
        return instance;
    }
}
