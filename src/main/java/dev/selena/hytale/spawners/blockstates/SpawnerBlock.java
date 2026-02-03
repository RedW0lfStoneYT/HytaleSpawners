package dev.selena.hytale.spawners.blockstates;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.Size;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import dev.selena.hytale.spawners.SpawnerMain;
import dev.selena.hytale.spawners.components.NerfedMobComponent;
import dev.selena.hytale.spawners.util.config.Config;
import dev.selena.hytale.spawners.util.objects.SpawnerSpawnAttemptReturn;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SpawnerBlock implements Component<ChunkStore> {

    public static final BuilderCodec<SpawnerBlock> CODEC = BuilderCodec.builder(SpawnerBlock.class, SpawnerBlock::new)
            .append(new KeyedCodec<>("SpawnType", Codec.STRING),
                    (spawner, type) -> spawner.spawnType = type,
                    spawner -> spawner.spawnType).add()
            .append(new KeyedCodec<>("SpawnCount", ProtocolCodecs.RANGE),
                    (spawner, count) -> spawner.spawnCount = count,
                    spawner -> spawner.spawnCount).add()
            .append(new KeyedCodec<>("SpawnRadius", ProtocolCodecs.SIZE),
                    (spawner, radius) -> spawner.spawnRadius = radius,
                    spawner -> spawner.spawnRadius).add()
            .append(new KeyedCodec<>("SpawnInterval", ProtocolCodecs.RANGE),
                    (spawner, minInterval) -> spawner.spawnIntervalTicks = minInterval, spawner -> spawner.spawnIntervalTicks).add()
            .append(new KeyedCodec<>("MaxSpawnAttempts", Codec.INTEGER),
                    (spawner, attempts) -> spawner.maxSpawnAttempts = attempts,
                    spawner -> spawner.maxSpawnAttempts).add()
            .append(new KeyedCodec<>("LastGameTick", Codec.INSTANT),
                    (spawner, lastTick) -> spawner.lastSpawnGameTick = lastTick,
                    spawner -> spawner.lastSpawnGameTick).add()
            .append(new KeyedCodec<>("CurrentSpawnIntervalTicks", Codec.INTEGER),
                    (spawner, currentInterval) -> spawner.currentSpawnIntervalTicks = currentInterval,
                    spawner -> spawner.currentSpawnIntervalTicks).add()
            .append(new KeyedCodec<>("PreviewEntityUUID", Codec.UUID_BINARY),
                    (spawner, uuid) -> spawner.previewEntityUUID = uuid,
                    spawner -> spawner.previewEntityUUID).add()
            .build();

    @Getter
    @Setter
    private String spawnType;
    @Getter
    @Setter
    private Range spawnCount;
    @Getter
    @Setter
    private Size spawnRadius;
    @Getter
    @Setter
    private Range spawnIntervalTicks;
    @Getter
    @Setter
    private int maxSpawnAttempts;
    @Getter
    @Setter
    private Instant lastSpawnGameTick;
    @Getter
    @Setter
    private int currentSpawnIntervalTicks;
    @Getter
    @Setter
    private UUID previewEntityUUID;
    @Getter
    private Ref<EntityStore> previewEntity;
    private final Random random = new Random();


    public SpawnerBlock() {
        this("Pig");
    }

    public SpawnerBlock(String type) {
        this.spawnType = type;
        this.spawnCount = Config.get().getSpawnRange();
        this.spawnRadius = Config.get().getSpawnRadius();
        this.spawnIntervalTicks = Config.get().getSpawnRange();
        this.maxSpawnAttempts = Config.get().getMaxSpawnAttempts();
    }

    @Nullable
    @Override
    public Component<ChunkStore> clone() {
        SpawnerBlock newSpawner = new SpawnerBlock();
        newSpawner.maxSpawnAttempts = this.maxSpawnAttempts;
        newSpawner.spawnCount = this.spawnCount;
        newSpawner.spawnIntervalTicks = this.spawnIntervalTicks;
        newSpawner.spawnRadius = this.spawnRadius;
        newSpawner.spawnType = this.spawnType;
        newSpawner.previewEntity = this.previewEntity;
        newSpawner.previewEntityUUID = this.previewEntityUUID;
        return newSpawner;
    }

    @Override
    public String toString() {
        return "SpawnerBlockState{" +
                "spawnType='" + spawnType + '\'' +
                ", spawnCount=" + spawnCount +
                ", spawnRadius=" + spawnRadius +
                ", spawnIntervalTicks=" + spawnIntervalTicks +
                ", maxSpawnAttempts=" + maxSpawnAttempts +
                ", lastSpawnGameTick=" + lastSpawnGameTick +
                ", currentSpawnIntervalTicks=" + currentSpawnIntervalTicks +
                '}';
    }

    public void setSpawnInterval() {
        this.currentSpawnIntervalTicks = random.nextInt(spawnIntervalTicks.min, spawnIntervalTicks.max + 1);
    }

    public SpawnerSpawnAttemptReturn trySpawn(World world, int blockX, int blockY, int blockZ, String type, int max) {
        Vector3i blockPosition = new Vector3i(blockX, blockY, blockZ);
        int count = getRandom(spawnCount.min, spawnCount.max);
        if (max != -1 && count > max) {
            count = max;
        }
        SpawnerSpawnAttemptReturn spawnAttemptReturn = new SpawnerSpawnAttemptReturn();
        for (int i = 0; i < count; i++) {
            int offsetX = getRandom(-spawnRadius.width, spawnRadius.width);
            int offsetY = getRandom(-spawnRadius.height, spawnRadius.height);
            int offsetZ = getRandom(-spawnRadius.width, spawnRadius.width);
            Vector3d spawnPos = new Vector3d(blockPosition.x + offsetX, blockPosition.y + offsetY, blockPosition.z + offsetZ);
            spawnPos = getValidSpawnPoint(world, spawnPos, 10, blockPosition.y + spawnRadius.height);
            if (spawnPos == null) {
                spawnAttemptReturn.setSuccess(false);
                return spawnAttemptReturn;
            }
            int roleIndex = NPCPlugin.get().getIndex(type);
            Builder<Role> roleBuilder = NPCPlugin.get().tryGetCachedValidRole(roleIndex);
            if (roleBuilder == null) {
                spawnAttemptReturn.setSuccess(false);
                return spawnAttemptReturn;
            }
            ISpawnableWithModel spawnable = (ISpawnableWithModel) roleBuilder;
            if (!roleBuilder.isSpawnable()) {
                throw new IllegalArgumentException("Abstract role templates cannot be spawned directly - a variant needs to be created!");
            }
            SpawningContext spawningContext = new SpawningContext();
            if (!spawningContext.setSpawnable(spawnable)) {
                throw new GeneralCommandException(Message.translation("server.commands.npc.spawn.cantSetRolebuilder"));
            }
            Model model = spawningContext.getModel();

            Pair<Ref<EntityStore>, NPCEntity> spawned = NPCPlugin.get().spawnEntity(world.getEntityStore().getStore(), roleIndex, spawnPos.add(0.5, 0, 0.5), new Vector3f(0, 0, 0), model, null);
            if (spawned == null) {
                spawnAttemptReturn.setSuccess(false);
                return spawnAttemptReturn;
            }
            if (Config.get().isNerfSpawnerMobs()) {
                Ref<EntityStore> entityRef = spawned.first();
                NPCEntity entity = spawned.second();
                int emptyRoleIndex = NPCPlugin.get().getIndex("Spawner_Role");
                NerfedMobComponent nerfedComponent = new NerfedMobComponent();
                nerfedComponent.setDrops(entity.getRole().getDropListId());
                RoleChangeSystem.requestRoleChange(entityRef, entity.getRole(), emptyRoleIndex, false, entityRef.getStore());
                entityRef.getStore().addComponent(entityRef, NerfedMobComponent.getComponentType(), nerfedComponent);
            }
            float timer;
            if ((timer = Config.get().getSpawnedMobDespawnTimeSeconds()) != -1) {
                spawned.second().setDespawnRemainingSeconds(timer);
                spawned.second().setDespawning(true);
            }

            spawnAttemptReturn.addSpawnedEntityPair(spawned);
        }
        spawnAttemptReturn.setSuccess(true);


        return spawnAttemptReturn;
    }

    private Vector3d getValidSpawnPoint(World world, Vector3d origin, int limit, int maxY) {
        for (int i = 0; i < limit; i++) {
            if (origin.toVector3i().y > maxY)
                return null;
            BlockType type = world.getBlockType(origin.toVector3i());
            if (type == null || type.equals(BlockType.EMPTY))
                return origin;
            origin.add(0, 1, 0);

        }
        return null;
    }

    private int getRandom(int min, int max) {
        return random.nextInt(min, max + 1);
    }


    public void handleBlockBroken(World world, Store<EntityStore> store, int worldX, int worldY, int worldZ) {
        Vector3d dropPosition = new Vector3d(worldX + 0.5f, worldY, worldZ + 0.5f);
        Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(store, List.of(getItemStack()), dropPosition, Vector3f.ZERO);
        if (itemEntityHolders.length > 0) {
            world.execute(() -> store.addEntities(itemEntityHolders, AddReason.SPAWN));
        }
    }

    public ItemStack getItemStack() {
        SpawnerBlock spawnerBlock = (SpawnerBlock) clone();
        spawnerBlock.previewEntityUUID = null;
        spawnerBlock.previewEntity = null;
        return new ItemStack("Spawner").withMetadata("SpawnerType", CODEC, spawnerBlock);
    }

    public void updatePreviewEntity(CommandBuffer<ChunkStore> commandBuffer, Vector3i blockPos, boolean removeIfExist) {
        if (!Config.get().isRenderMobModel())
            return;

        Store<EntityStore> entityStore = commandBuffer.getExternalData().getWorld().getEntityStore().getStore();

        if (previewEntity == null && previewEntityUUID != null) {
            previewEntity = commandBuffer.getExternalData().getWorld().getEntityStore().getRefFromUUID(previewEntityUUID);
        }
        if (previewEntity != null && !previewEntity.isValid()) {
            previewEntity = null;
            previewEntityUUID = null;
        }

        if (previewEntity != null && previewEntity.isValid()) {
            if (!removeIfExist)
                return;
            entityStore.removeEntity(previewEntity, RemoveReason.REMOVE);
            previewEntity = null;
            previewEntityUUID = null;
        } else {
            List<Ref<EntityStore>> results = new ArrayList<>();
            SpatialResource<Ref<EntityStore>, EntityStore> entitySpatialResource = entityStore.getResource(EntityModule.get().getEntitySpatialResourceType());

            Vector3d center = blockPos.toVector3d().add(0.5, 0.5, 0.5);
            entitySpatialResource.getSpatialStructure().collect(center, 1.0, results);


            if (!results.isEmpty() && results.getFirst() != null) {
                previewEntity = results.getFirst();
                entityStore.removeEntity(previewEntity, RemoveReason.REMOVE);
                previewEntity = null;
                previewEntityUUID = null;
            }
        }


        Model model = Model.createStaticScaledModel(ModelAsset.getAssetMap().getAsset(getSpawnType()), 1f);
        double maxDimension = Math.max(
                Math.max(model.getBoundingBox().dimension(Axis.X), model.getBoundingBox().dimension(Axis.Y)),
                model.getBoundingBox().dimension(Axis.Z)
        );
        float scaleFactor = (float) (0.5 / maxDimension);
        model = Model.createStaticScaledModel(ModelAsset.getAssetMap().getAsset(getSpawnType()), scaleFactor);

        Holder<EntityStore> preview = entityStore.getRegistry().newHolder();
        preview.addComponent(NetworkId.getComponentType(), new NetworkId(entityStore.getExternalData().takeNextNetworkId()));
        preview.addComponent(TransformComponent.getComponentType(), new TransformComponent(blockPos.toVector3d().add(.5f, .25f, .5f), Vector3f.ZERO));
        preview.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        preview.addComponent(PersistentModel.getComponentType(), new PersistentModel(new Model.ModelReference(getSpawnType(), scaleFactor, null, true)));
        preview.addComponent(HeadRotation.getComponentType(), new HeadRotation(Vector3f.ZERO));
        preview.addComponent(PropComponent.getComponentType(), PropComponent.get());
        preview.ensureComponent(UUIDComponent.getComponentType());
        preview.ensureComponent(SpawnerMain.get().getSpawnerEntityComponentType());

        previewEntity = entityStore.addEntity(preview, AddReason.SPAWN);

        UUIDComponent uuidComponent = entityStore.getComponent(previewEntity, UUIDComponent.getComponentType());
        if (uuidComponent != null) {
            previewEntityUUID = uuidComponent.getUuid();
        }

    }

    public void removePreviewEntity(CommandBuffer<ChunkStore> commandBuffer) {
        Store<EntityStore> entityStore = commandBuffer.getExternalData().getWorld().getEntityStore().getStore();
        if (previewEntity != null && previewEntity.isValid()) {
            entityStore.removeEntity(previewEntity, RemoveReason.REMOVE);
            previewEntity = null;
        }
    }
}
