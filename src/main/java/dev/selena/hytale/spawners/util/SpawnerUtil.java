package dev.selena.hytale.spawners.util;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.selena.hytale.spawners.SpawnerMain;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.events.SpawnerSpawnEvent;
import dev.selena.hytale.spawners.util.objects.SpawnerSpawnAttemptReturn;

import java.time.Instant;

public class SpawnerUtil {

    public static void tickSpawnerBlock(CommandBuffer<ChunkStore> commandBuffer, BlockChunk blockChunk, BlockSection blockSection, Ref<ChunkStore> sectionRef, Ref<ChunkStore> blockRef, SpawnerBlock spawnerBlock, int x, int y, int z, boolean initialTick) {

        World world = commandBuffer.getExternalData().getWorld();
        WorldTimeResource worldTimeResource = world.getEntityStore().getStore().getResource(WorldTimeResource.getResourceType());
        Instant currentTime = worldTimeResource.getGameTime();
        long elapsed = currentTime.getEpochSecond() - spawnerBlock.getLastSpawnGameTick().getEpochSecond();

        world.execute(() -> {
            ChunkSection section = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
            int worldX = ChunkUtil.worldCoordFromLocalCoord(section.getX(), x);
            int worldY = ChunkUtil.worldCoordFromLocalCoord(section.getY(), y);
            int worldZ = ChunkUtil.worldCoordFromLocalCoord(section.getZ(), z);
            spawnerBlock.updatePreviewEntity(commandBuffer, new Vector3i(worldX, worldY, worldZ), false);
            if (elapsed >= spawnerBlock.getCurrentSpawnIntervalTicks()) {
                SpawnerSpawnEvent.Pre pre = HytaleServer.get().getEventBus().dispatchFor(SpawnerSpawnEvent.Pre.class)
                        .dispatch(new SpawnerSpawnEvent.Pre(spawnerBlock, spawnerBlock.getSpawnType(), world));
                if (pre.isCancelled()) {
                    return;
                }
                SpawnerSpawnAttemptReturn spawned = new SpawnerSpawnAttemptReturn();
                for (int i = 0; i < spawnerBlock.getMaxSpawnAttempts(); i++) {
                    SpawnerMain.get().getLogger().atInfo().log("Spawn attempt " + i);


                    spawned = spawnerBlock.trySpawn(world, worldX, worldY, worldZ, pre.getEntityType());

                    if (spawned.isSuccess()) {
                        break;
                    }
                }
                HytaleServer.get().getEventBus().dispatchFor(SpawnerSpawnEvent.Post.class)
                        .dispatch(new SpawnerSpawnEvent.Post(spawnerBlock, spawnerBlock.getSpawnType(), spawned, world));
                spawnerBlock.setLastSpawnGameTick(currentTime);
                spawnerBlock.setSpawnInterval();
            }
        });

    }
}
