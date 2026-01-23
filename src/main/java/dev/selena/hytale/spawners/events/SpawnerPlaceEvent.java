package dev.selena.hytale.spawners.events;

import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import lombok.Getter;
import lombok.Setter;

public class SpawnerPlaceEvent implements IEvent<Void>, ICancellable {
    @Getter
    @Setter
    private boolean cancelled;
    @Getter
    @Setter
    private String spawnType;
    @Getter
    private final World world;
    @Getter
    private final Vector3i blockPosition;

    public SpawnerPlaceEvent(String spawnType, World world, Vector3i blockPosition) {
        this.cancelled = false;
        this.spawnType = spawnType;
        this.world = world;
        this.blockPosition = blockPosition;
    }

}
