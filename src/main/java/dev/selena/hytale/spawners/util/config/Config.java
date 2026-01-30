package dev.selena.hytale.spawners.util.config;

import com.google.gson.annotations.Expose;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.Size;
import dev.selena.core.config.Comment;
import lombok.Getter;

public class Config {

    @Getter
    @Expose
    @Comment("The default range of mobs to spawn")
    private Range spawnRange = new Range(1, 5);

    @Getter
    @Expose
    @Comment("The default spawn radius for spawners")
    private Size spawnRadius = new Size(3, 3);

    @Getter
    @Expose
    @Comment("The default range of ticks for mob spawns")
    private Range spawnTicksRange = new Range(200, 400);

    @Getter
    @Expose
    @Comment("The maximum spawn attempts")
    private int maxSpawnAttempts = 5;

    @Getter
    @Expose
    @Comment("Should the spawner render the model of the mob type")
    private boolean renderMobModel = true;

    @Getter
    @Expose
    @Comment("Maximum number of non player entities allowed nearby when spawning")
    private int maxNearbyEntities = 5;

    @Getter
    @Expose
    @Comment("Should the spawner check for nearby entities before spawning")
    private boolean checkNearbyEntities = true;

    @Getter
    @Expose
    @Comment("The radius to check for nearby entities")
    private Size nearbyEntitiesCheckRadius = new Size(5, 5);

    @Getter
    @Expose
    @Comment("Should the ticking rely on world time ticks")
    private boolean useWorldTimeTicks = true;

    @Getter
    @Expose
    @Comment("Should the preview entity rotate (EXPERIMENTAL)")
    private boolean rotatePreviewEntity = true;

    @Getter
    @Expose
    @Comment("The degrees per tick the preview entity should rotate (if enabled)")
    private float previewEntityRotationDegreesPerTick = 10.0f;

    @Getter
    @Expose
    @Comment("Should spawner spawned mobs be nerfed? (remove pathing and targeting)")
    private boolean nerfSpawnerMobs = false;

    @Getter
    @Expose
    @Comment("How long should the spawned mobs last before despawning (in seconds), -1 to disable")
    private float spawnedMobDespawnTimeSeconds = 300;


    public static Config get() {
        return Configs.CONFIG.getConfig();
    }
}
