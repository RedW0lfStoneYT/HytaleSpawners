package dev.selena.hytale.spawners.events;

import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.util.objects.SpawnerSpawnAttemptReturn;
import lombok.Getter;
import lombok.Setter;

public class SpawnerSpawnEvent implements IEvent<Void> {

    @Getter
    SpawnerBlock spawnerBlock;
    @Getter
    String entityType;
    @Getter
    World world;

    public static class Pre extends SpawnerSpawnEvent implements ICancellable {

        @Getter
        @Setter
        private boolean cancelled;

        public Pre(SpawnerBlock spawnerBlock, String entityType, World world) {
            this.spawnerBlock = spawnerBlock;
            this.entityType = entityType;
            this.cancelled = false;
            this.world = world;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

    }

    public static class Post extends SpawnerSpawnEvent {

        @Getter
        SpawnerSpawnAttemptReturn spawnAttemptReturn;

        public Post(SpawnerBlock spawnerBlock, String entityType, SpawnerSpawnAttemptReturn spawnAttemptReturn, World world) {
            this.spawnerBlock = spawnerBlock;
            this.entityType = entityType;
            this.spawnAttemptReturn = spawnAttemptReturn;
            this.world = world;
        }
    }
}
