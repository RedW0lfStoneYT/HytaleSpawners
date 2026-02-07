package dev.selena.hytale.spawners.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.selena.hytale.spawners.SpawnerMain;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpawnerChunkWakeSystem extends EntityTickingSystem<EntityStore> {

    private final Map<UUID, Long> lastChunk = new ConcurrentHashMap<>();

    @Override
    public void tick(
            float dt,
            int index,
            ArchetypeChunk<EntityStore> chunk,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> buffer
    ) {

        Ref<EntityStore> ref = chunk.getReferenceTo(index);

        PlayerRef player = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null) return;

        UUID uuid = player.getUuid();

        int worldX = (int) player.getTransform().getPosition().getX();
        int worldZ = (int) player.getTransform().getPosition().getZ();

        int cx = ChunkUtil.chunkCoordinate(worldX);
        int cz = ChunkUtil.chunkCoordinate(worldZ);

        long key = (((long) cx) << 32) | (cz & 0xffffffffL);

        if (key == lastChunk.getOrDefault(uuid, Long.MIN_VALUE)) {
            return;
        }

        lastChunk.put(uuid, key);

        World world = store.getExternalData().getWorld();

        wakeChunkSpawners(world, cx, cz);
    }

    private void wakeChunkSpawners(World world, int cx, int cz) {

        Ref<ChunkStore> chunkRef =
                world.getChunkStore().getChunkReference(
                        ChunkUtil.indexChunk(cx, cz)
                );

        if (chunkRef == null) return;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();

        BlockComponentChunk blockChunk =
                chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());

        if (blockChunk == null) return;

        for (Ref<ChunkStore> blockRef : blockChunk.getEntityReferences().values()) {

            if (blockRef == null || !blockRef.isValid()) continue;

            SpawnerBlock spawner =
                    chunkStore.getComponent(
                            blockRef,
                            SpawnerMain.get().getSpawnerBlockComponentType()
                    );

            if (spawner != null) {
                spawner.setSleeping(false);
                spawner.setSpawnInterval();
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}