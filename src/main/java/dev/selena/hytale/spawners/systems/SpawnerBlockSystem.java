package dev.selena.hytale.spawners.systems;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.selena.hytale.spawners.SpawnerMain;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.components.NerfedMobComponent;
import dev.selena.hytale.spawners.events.SpawnerPlaceEvent;
import dev.selena.hytale.spawners.util.SpawnerUtil;
import dev.selena.hytale.spawners.util.config.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class SpawnerBlockSystem {

    public static class SpawnerPlaceSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

        public SpawnerPlaceSystem() {
            super(PlaceBlockEvent.class);
        }

        @Override
        public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull PlaceBlockEvent placeBlockEvent) {
            if (placeBlockEvent.isCancelled()) {
                return;
            }
            ItemStack item = placeBlockEvent.getItemInHand();
            if (item == null) {
                return;
            }

            BlockType blockType = BlockType.getAssetMap().getAsset(item.getBlockKey());
            if (blockType == null) {
                return;
            }
            Holder<ChunkStore> blockEntity = blockType.getBlockEntity();
            if (blockEntity == null || blockEntity.getComponent(SpawnerMain.get().getSpawnerBlockComponentType()) == null) {
                return;
            }
            SpawnerBlock spawner = item.getFromMetadataOrNull(new KeyedCodec<>("SpawnerType", SpawnerBlock.CODEC));
            if (spawner == null) {
                spawner = new SpawnerBlock();
                spawner.setSpawnCount(Config.get().getSpawnRange());
                spawner.setSpawnRadius(Config.get().getSpawnRadius());
                spawner.setSpawnIntervalTicks(Config.get().getSpawnTicksRange());
                spawner.setMaxSpawnAttempts(Config.get().getMaxSpawnAttempts());
            }

            World world = store.getExternalData().getWorld();

            SpawnerPlaceEvent spawnerPlaceEvent = HytaleServer.get().getEventBus().dispatchFor(SpawnerPlaceEvent.class)
                    .dispatch(new SpawnerPlaceEvent(spawner.getSpawnType(), world, placeBlockEvent.getTargetBlock()));
            if (spawnerPlaceEvent.isCancelled()) {
                placeBlockEvent.setCancelled(true);
                return;
            }
            spawner.setSpawnType(spawnerPlaceEvent.getSpawnType());
            blockEntity.replaceComponent(SpawnerMain.get().getSpawnerBlockComponentType(), spawner);
            world.getChunkStore().getStore().addEntity(blockEntity, AddReason.SPAWN);

        }

        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }

    public static class OnSpawnerBlockAdd extends RefSystem<ChunkStore> {

        @Override
        public void onEntityAdded(@NotNull Ref<ChunkStore> ref, @NotNull AddReason addReason, @NotNull Store<ChunkStore> store, @NotNull CommandBuffer<ChunkStore> commandBuffer) {
            if (addReason != AddReason.SPAWN) {
                return;
            }
            SpawnerBlock spawnerBlock = commandBuffer.getComponent(ref, SpawnerMain.get().getSpawnerBlockComponentType());
            if (spawnerBlock == null) {
                return;
            }

            BlockModule.BlockStateInfo info = commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
            if (info == null) {
                return;
            }
            BlockChunk blockChunk = commandBuffer.getComponent(info.getChunkRef(), BlockChunk.getComponentType());
            if (blockChunk == null) {
                return;
            }
            if (spawnerBlock.getLastSpawnGameTick() == null) {
                spawnerBlock.setLastSpawnGameTick(store.getExternalData().getWorld().getEntityStore().getStore().getResource(WorldTimeResource.getResourceType()).getGameTime());
                spawnerBlock.setSpawnInterval();
                blockChunk.markNeedsSaving();
            }

            int x = ChunkUtil.xFromBlockInColumn(info.getIndex());
            int y = ChunkUtil.yFromBlockInColumn(info.getIndex());
            int z = ChunkUtil.zFromBlockInColumn(info.getIndex());
            BlockComponentChunk blockComponentChunk = commandBuffer.getComponent(info.getChunkRef(), BlockComponentChunk.getComponentType());
            if (blockComponentChunk == null) {
                throw new AssertionError();
            }
            ChunkColumn column = commandBuffer.getComponent(info.getChunkRef(), ChunkColumn.getComponentType());
            if (column == null) {
                throw new AssertionError();
            }
            Ref<ChunkStore> section = column.getSection(ChunkUtil.chunkCoordinate(y));
            if (section == null) {
                throw new AssertionError();
            }
            BlockSection blockSection = commandBuffer.getComponent(section, BlockSection.getComponentType());

            spawnerBlock.setSpawnInterval();
            SpawnerUtil.tickSpawnerBlock(commandBuffer, blockChunk, blockSection, section, ref, spawnerBlock, x, y, z, true);

        }

        @Override
        public void onEntityRemove(@NotNull Ref<ChunkStore> ref, @NotNull RemoveReason removeReason, @NotNull Store<ChunkStore> store, @NotNull CommandBuffer<ChunkStore> commandBuffer) {
            SpawnerBlock spawner;
            if (removeReason == RemoveReason.UNLOAD || (spawner = commandBuffer.getComponent(ref, SpawnerMain.get().getSpawnerBlockComponentType())) == null) {
                return;
            }
            BlockModule.BlockStateInfo info = commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
            if (info == null) {
                throw new AssertionError();
            }
            Store<EntityStore> entityStore = commandBuffer.getExternalData().getWorld().getEntityStore().getStore();
            int x = ChunkUtil.xFromBlockInColumn(info.getIndex());
            int y = ChunkUtil.yFromBlockInColumn(info.getIndex());
            int z = ChunkUtil.zFromBlockInColumn(info.getIndex());
            BlockChunk blockChunk = commandBuffer.getComponent(info.getChunkRef(), BlockChunk.getComponentType());
            if (blockChunk == null) {
                throw new AssertionError();
            }
            ChunkColumn column = commandBuffer.getComponent(info.getChunkRef(), ChunkColumn.getComponentType());
            if (column == null) {
                throw new AssertionError();
            }
            Ref<ChunkStore> sectionRef = column.getSection(ChunkUtil.chunkCoordinate(y));
            if (sectionRef == null) {
                throw new AssertionError();
            }
            BlockSection blockSection = commandBuffer.getComponent(sectionRef, BlockSection.getComponentType());
            if (blockSection == null) {
                throw new AssertionError();
            }
            ChunkSection chunkSection = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
            if (chunkSection == null) {
                throw new AssertionError();
            }
            int worldX = ChunkUtil.worldCoordFromLocalCoord(chunkSection.getX(), x);
            int worldY = ChunkUtil.worldCoordFromLocalCoord(chunkSection.getY(), y);
            int worldZ = ChunkUtil.worldCoordFromLocalCoord(chunkSection.getZ(), z);
            World world = commandBuffer.getExternalData().getWorld();
            world.execute(() -> spawner.removePreviewEntity(commandBuffer, new Vector3i(worldX, worldY, worldZ)));
            spawner.handleBlockBroken(world, entityStore, worldX, worldY, worldZ);

        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(BlockModule.BlockStateInfo.getComponentType(), SpawnerMain.get().getSpawnerBlockComponentType());
        }
    }


    public static class PreviewRotating extends EntityTickingSystem<EntityStore> {

        @Override
        public void tick(float v, int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
            Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(i);
            if (!entityRef.isValid()) {
                return;
            }

            TransformComponent transform = commandBuffer.getComponent(entityRef, TransformComponent.getComponentType());
            if (transform != null) {
                Vector3f rotation = transform.getRotation();
                float radians = (float) (Math.PI / 180) * Config.get().getPreviewEntityRotationDegreesPerTick();;
                rotation = rotation.add(0, radians, 0);
                transform.setRotation(rotation);
                commandBuffer.replaceComponent(entityRef, HeadRotation.getComponentType(), new HeadRotation(rotation));
                commandBuffer.replaceComponent(entityRef, TransformComponent.getComponentType(), transform);
            }
        }

        @Nullable
        @Override
        public Query<EntityStore> getQuery() {
            return Query.and(SpawnerMain.get().getSpawnerEntityComponentType(), PropComponent.getComponentType());
        }
    }

    public static class Ticking extends EntityTickingSystem<ChunkStore> {


        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            BlockSection blocks = archetypeChunk.getComponent(index, BlockSection.getComponentType());
            if (blocks == null) {
                throw new AssertionError();
            }
            if (blocks.getTickingBlocksCountCopy() == 0) {
                return;
            }
            ChunkSection section = archetypeChunk.getComponent(index, ChunkSection.getComponentType());
            if (section == null) {
                throw new AssertionError();
            }
            if (section.getChunkColumnReference() == null || !section.getChunkColumnReference().isValid()) {
                return;
            }
            BlockComponentChunk blockComponentChunk = commandBuffer.getComponent(section.getChunkColumnReference(), BlockComponentChunk.getComponentType());
            if (blockComponentChunk == null) {
                throw new AssertionError();
            }
            Ref<ChunkStore> ref = archetypeChunk.getReferenceTo(index);
            BlockChunk blockChunk = commandBuffer.getComponent(section.getChunkColumnReference(), BlockChunk.getComponentType());
            if (blockChunk == null) {
                throw new AssertionError();
            }
            blocks.forEachTicking(blockComponentChunk, commandBuffer, section.getY(), (blockComponentChunk1, commandBuffer1, localX, localY, localZ, blockId) -> {
                Ref<ChunkStore> blockRef = blockComponentChunk1.getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
                if (blockRef == null) {
                    return BlockTickStrategy.IGNORED;
                }
                SpawnerBlock spawner = commandBuffer1.getComponent(blockRef, SpawnerMain.get().getSpawnerBlockComponentType());
                if (spawner != null) {
                    SpawnerUtil.tickSpawnerBlock(commandBuffer1, blockChunk, blocks, ref, blockRef, spawner, localX, localY, localZ, false);
                    return BlockTickStrategy.CONTINUE;
                }
                return BlockTickStrategy.IGNORED;
            });

        }

        @Nullable
        @Override
        public Query<ChunkStore> getQuery() {
            return Query.and(BlockSection.getComponentType(), ChunkSection.getComponentType());
        }
    }

    public static final class NerfedMobDeath extends EntityTickingSystem<EntityStore> {
        private final ComponentType<EntityStore, NerfedMobComponent> nerfedType;
        private final ComponentType<EntityStore, DeathComponent> deathType;
        private final ComponentType<EntityStore, TransformComponent> transformType;
        private final ComponentType<EntityStore, HeadRotation> headRotType;
        private final Set<Ref<EntityStore>> processed = Collections.newSetFromMap(new WeakHashMap<>());

        public NerfedMobDeath() {
            this.nerfedType = NerfedMobComponent.getComponentType();
            this.deathType = DeathComponent.getComponentType();
            this.transformType = TransformComponent.getComponentType();
            this.headRotType = HeadRotation.getComponentType();
        }

        @Override
        public void tick(float v, int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
                         @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {

            int count = archetypeChunk.size();
            for (int idx = 0; idx < count; idx++) {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(idx);

                if (processed.contains(ref)) {
                    continue;
                }

                NerfedMobComponent nerfed = archetypeChunk.getComponent(idx, nerfedType);
                DeathComponent death = archetypeChunk.getComponent(idx, deathType);

                if (nerfed == null || death == null) {
                    continue;
                }

                String dropListID = nerfed.getDrops();
                List<ItemStack> itemsToDrop = new ObjectArrayList<>();

                if (dropListID != null) {
                    ItemModule itemModule = ItemModule.get();
                    if (itemModule.isEnabled()) {
                        itemsToDrop.addAll(itemModule.getRandomItemDrops(dropListID));
                    }
                }

                if (!itemsToDrop.isEmpty()) {
                    TransformComponent transform = archetypeChunk.getComponent(idx, transformType);
                    HeadRotation headRotation = archetypeChunk.getComponent(idx, headRotType);

                    if (transform != null && headRotation != null) {
                        Vector3d position = transform.getPosition();
                        Vector3d dropPosition = position.clone().add(0.0d, 1.0d, 0.0d);
                        Vector3f headRot = headRotation.getRotation();

                        Holder<EntityStore>[] drops = ItemComponent.generateItemDrops(store, itemsToDrop, dropPosition, headRot.clone());
                        commandBuffer.addEntities(drops, AddReason.SPAWN);
                    }
                }

                processed.add(ref);

                if (store.getComponent(ref, nerfedType) != null) {
                    try {
                        commandBuffer.removeComponent(ref, nerfedType);
                    } catch (IllegalArgumentException ignored) { }
                }
            }

        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return Query.and(NerfedMobComponent.getComponentType(), DeathComponent.getComponentType());
        }
    }

}
