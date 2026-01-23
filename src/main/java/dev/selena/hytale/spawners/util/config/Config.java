package dev.selena.hytale.spawners.util.config;

import com.google.gson.annotations.Expose;
import com.hypixel.hytale.protocol.Size;
import dev.selena.core.config.Comment;
import lombok.Getter;
import com.hypixel.hytale.protocol.Range;

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


    public static Config get() {
        return Configs.CONFIG.getConfig();
    }
}
