package dev.selena.hytale.spawners.util.objects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SpawnerSpawnAttemptReturn {

    @Getter
    @Setter
    private boolean success;
    @Getter
    private List<Pair<Ref<EntityStore>, NPCEntity>> spawnedEntityPair;

    public SpawnerSpawnAttemptReturn(boolean success, List<Pair<Ref<EntityStore>, NPCEntity>> spawnedEntityPair) {
        this.success = success;
        this.spawnedEntityPair = spawnedEntityPair;
    }

    public SpawnerSpawnAttemptReturn(boolean success, Pair<Ref<EntityStore>, NPCEntity> spawnedEntityPair) {
        this.success = success;
        this.spawnedEntityPair = List.of(spawnedEntityPair);
    }

    public SpawnerSpawnAttemptReturn() {
        this.success = false;
        this.spawnedEntityPair = new java.util.ArrayList<>();
    }

    public void addSpawnedEntityPair(Pair<Ref<EntityStore>, NPCEntity> pair) {
        this.spawnedEntityPair.add(pair);
    }

}
