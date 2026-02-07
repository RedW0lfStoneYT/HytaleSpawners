package dev.selena.hytale.spawners.util;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.events.SpawnerSpawnEvent;
import dev.selena.hytale.spawners.util.config.Config;
import dev.selena.hytale.spawners.util.objects.SpawnerSpawnAttemptReturn;

import java.time.Instant;

public class SpawnerUtil {

    private static final int PLAYER_RADIUS = 16;
    private static final int MAX_MOBS_PER_CHUNK = 100;

    private static final int PREVIEW_UPDATE_INTERVAL = 10;

    public static void tickSpawnerBlock(
            CommandBuffer<ChunkStore> commandBuffer,
            BlockChunk blockChunk,
            BlockSection blockSection,
            Ref<ChunkStore> sectionRef,
            Ref<ChunkStore> blockRef,
            SpawnerBlock spawnerBlock,
            int x, int y, int z,
            boolean initialTick
    ) {

        World world = commandBuffer.getExternalData().getWorld();

        if (spawnerBlock.getCurrentSpawnIntervalTicks() <= 0) {
            spawnerBlock.setSpawnInterval();
            spawnerBlock.setLastSpawnTick(0);
            blockChunk.markNeedsSaving();
            return;
        }

        long tick = spawnerBlock.getLastSpawnTick() + 1;
        spawnerBlock.setLastSpawnTick(tick);

        if (tick < spawnerBlock.getCurrentSpawnIntervalTicks()) {

            if (tick % PREVIEW_UPDATE_INTERVAL == 0) {
                updatePreview(commandBuffer, sectionRef, spawnerBlock, x, y, z);
            }

            return;
        }

        ChunkSection section =
                commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());

        if (section == null) return;

        int worldX = ChunkUtil.worldCoordFromLocalCoord(section.getX(), x);
        int worldY = ChunkUtil.worldCoordFromLocalCoord(section.getY(), y);
        int worldZ = ChunkUtil.worldCoordFromLocalCoord(section.getZ(), z);

        if (!isPlayerNearby(world, worldX, worldY, worldZ, PLAYER_RADIUS)) {
            return;
        }

        int chunkMobCount = countChunkMobs(world, worldX, worldZ);

        if (chunkMobCount >= MAX_MOBS_PER_CHUNK) {
            return;
        }

        int maxSpawnAllowed = Math.max(MAX_MOBS_PER_CHUNK - chunkMobCount, 0);

        SpawnerSpawnEvent.Pre pre =
                HytaleServer.get()
                        .getEventBus()
                        .dispatchFor(SpawnerSpawnEvent.Pre.class)
                        .dispatch(new SpawnerSpawnEvent.Pre(
                                spawnerBlock,
                                spawnerBlock.getSpawnType(),
                                world,
                                maxSpawnAllowed
                        ));

        if (pre.isCancelled()) {
            return;
        }

        SpawnerSpawnAttemptReturn spawned = new SpawnerSpawnAttemptReturn();

        for (int i = 0; i < spawnerBlock.getMaxSpawnAttempts(); i++) {

            spawned = spawnerBlock.trySpawn(
                    world,
                    worldX,
                    worldY,
                    worldZ,
                    pre.getEntityType(),
                    pre.getMaxSpawnAmount()
            );

            if (spawned.isSuccess()) {
                break;
            }
        }

        HytaleServer.get()
                .getEventBus()
                .dispatchFor(SpawnerSpawnEvent.Post.class)
                .dispatch(new SpawnerSpawnEvent.Post(
                        spawnerBlock,
                        spawnerBlock.getSpawnType(),
                        spawned,
                        world
                ));

        if (spawned.isSuccess()) {

            spawnerBlock.setLastSpawnTick(0);

            spawnerBlock.setSpawnInterval();

            blockChunk.markNeedsSaving();
        }
    }

    private static void updatePreview(
            CommandBuffer<ChunkStore> commandBuffer,
            Ref<ChunkStore> sectionRef,
            SpawnerBlock spawnerBlock,
            int x, int y, int z
    ) {

        ChunkSection section =
                commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());

        if (section == null) return;

        int worldX = ChunkUtil.worldCoordFromLocalCoord(section.getX(), x);
        int worldY = ChunkUtil.worldCoordFromLocalCoord(section.getY(), y);
        int worldZ = ChunkUtil.worldCoordFromLocalCoord(section.getZ(), z);

        spawnerBlock.updatePreviewEntity(
                commandBuffer,
                new Vector3i(worldX, worldY, worldZ),
                false
        );
    }

    private static int countChunkMobs(World world, int worldX, int worldZ) {

        ComponentType<EntityStore, NPCEntity> npcType =
                NPCEntity.getComponentType();

        int chunkMinX = worldX & ~31;
        int chunkMinZ = worldZ & ~31;

        int chunkMaxX = chunkMinX + 32;
        int chunkMaxZ = chunkMinZ + 32;

        int count = 0;

        for (var entityRef : TargetUtil.getAllEntitiesInBox(
                new Vector3d(chunkMinX, 0, chunkMinZ),
                new Vector3d(chunkMaxX, 320, chunkMaxZ),
                world.getEntityStore().getStore()
        )) {

            if (entityRef == null || !entityRef.isValid()) continue;

            if (entityRef.getStore().getComponent(entityRef, npcType) != null) {
                count++;
                if (count >= MAX_MOBS_PER_CHUNK) {
                    return count;
                }
            }
        }

        return count;
    }

    public static boolean isPlayerNearby(World world, int x, int y, int z, double radius) {

        double r2 = radius * radius;

        for (PlayerRef player : world.getPlayerRefs()) {

            if (player == null || !player.isValid()) continue;

            Vector3d pos = player.getTransform().getPosition();

            double dx = pos.getX() - x;
            double dy = pos.getY() - y;
            double dz = pos.getZ() - z;

            if ((dx * dx + dy * dy + dz * dz) <= r2) {
                return true;
            }
        }

        return false;
    }
}