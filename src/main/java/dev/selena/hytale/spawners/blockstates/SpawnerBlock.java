package dev.selena.hytale.spawners.blockstates;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
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
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import dev.selena.hytale.spawners.SpawnerMain;
import dev.selena.hytale.spawners.util.config.Config;
import dev.selena.hytale.spawners.util.objects.SpawnerSpawnAttemptReturn;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public class SpawnerBlock implements Component<ChunkStore> {

    public static final BuilderCodec<SpawnerBlock> CODEC = BuilderCodec.builder(SpawnerBlock.class, SpawnerBlock::new)
            .append(new KeyedCodec<>("SpawnType", Codec.STRING), (spawner, type) -> {
                spawner.spawnType = type;
            }, spawner -> spawner.spawnType).add()
            .append(new KeyedCodec<>("SpawnCount", ProtocolCodecs.RANGE), (spawner, count) -> {
                spawner.spawnCount = count;
            }, spawner -> spawner.spawnCount).add()
            .append(new KeyedCodec<>("SpawnRadius", ProtocolCodecs.SIZE), (spawner, radius) -> {
                spawner.spawnRadius = radius;
            }, spawner -> spawner.spawnRadius).add()
            .append(new KeyedCodec<>("SpawnInterval", ProtocolCodecs.RANGE), (spawner, minInterval) -> {
                spawner.spawnIntervalTicks = minInterval;
            }, spawner -> spawner.spawnIntervalTicks).add()
            .append(new KeyedCodec<>("MaxSpawnAttempts", Codec.INTEGER), (spawner, attempts) -> {
                spawner.maxSpawnAttempts = attempts;
            }, spawner -> spawner.maxSpawnAttempts).add()
            .append(new KeyedCodec<>("LastGameTick", Codec.INSTANT), (spawner, lastTick) -> {
                spawner.lastSpawnGameTick = lastTick;
            }, spawner -> spawner.lastSpawnGameTick).add()
            .append(new KeyedCodec<>("CurrentSpawnIntervalTicks", Codec.INTEGER), (spawner, currentInterval) -> {
                spawner.currentSpawnIntervalTicks = currentInterval;
            }, spawner -> spawner.currentSpawnIntervalTicks).add()
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
    private Ref<EntityStore> previewEntityRef;


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
        this.currentSpawnIntervalTicks = (int) (Math.random() * (spawnIntervalTicks.max - spawnIntervalTicks.min + 1)) + spawnIntervalTicks.min;
    }

    public SpawnerSpawnAttemptReturn trySpawn(World world, int blockX, int blockY, int blockZ, String type) {
        Vector3i blockPosition = new Vector3i(blockX, blockY, blockZ);
        int count = getRandom(spawnCount.min, spawnCount.max);
        SpawnerMain.get().getLogger().atInfo().log("Spawning " + count + " of type " + spawnType + " within radius " + spawnRadius);
        SpawnerSpawnAttemptReturn spawnAttemptReturn = new SpawnerSpawnAttemptReturn();
        for (int i = 0; i < count; i++) {
            int offsetX = getRandom(-spawnRadius.width, spawnRadius.width);
            int offsetY = getRandom(-spawnRadius.height, spawnRadius.height);
            int offsetZ = getRandom(-spawnRadius.width, spawnRadius.width);
            Vector3d spawnPos = new Vector3d(blockPosition.x + offsetX, blockPosition.y + offsetY, blockPosition.z + offsetZ);
            spawnPos = getValidSpawnPoint(world, spawnPos, 10);
            if (spawnPos == null) {
                spawnAttemptReturn.setSuccess(false);
                return spawnAttemptReturn;
            }
            int roleIndex = NPCPlugin.get().getIndex(spawnType);
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

            Pair<Ref<EntityStore>, NPCEntity> spawned = NPCPlugin.get().spawnEntity(world.getEntityStore().getStore(), roleIndex, spawnPos.add(0.5,0,0.5), new Vector3f(0, 0, 0), model, null);
            if (spawned == null) {
                spawnAttemptReturn.setSuccess(false);
                return spawnAttemptReturn;
            }
            spawnAttemptReturn.addSpawnedEntityPair(spawned);
        }
        spawnAttemptReturn.setSuccess(true);


        return spawnAttemptReturn;
    }

    private Vector3d getValidSpawnPoint(World world, Vector3d origin, int limit) {
        for (int i = 0; i < limit; i++) {
            BlockType type = world.getBlockType(origin.toVector3i());
            if (type == null || type.equals(BlockType.EMPTY))
                return origin;
            origin.add(0, 1, 0);

        }
        return null;
    }

    private int getRandom(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }


    public void handleBlockBroken(World world, Store<EntityStore> store, int worldX, int worldY, int worldZ) {
        Vector3d dropPosition = new Vector3d(worldX + 0.5f, worldY, worldZ + 0.5f);
        Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(store, List.of(getItemStack()), dropPosition, Vector3f.ZERO);
        if (itemEntityHolders.length > 0) {
            world.execute(() -> {
                store.addEntities(itemEntityHolders, AddReason.SPAWN);
            });
        }
    }

    public ItemStack getItemStack() {
        return new ItemStack("Spawner").withMetadata("SpawnerType", CODEC, (SpawnerBlock) clone());
    }

    public void updatePreviewEntity(CommandBuffer<ChunkStore> commandBuffer, Vector3i blockPos, boolean removeIfExist) {
        if (!Config.get().isRenderMobModel())
            return;
        Store<EntityStore> entityStore = commandBuffer.getExternalData().getWorld().getEntityStore().getStore();

        if (previewEntityRef != null && removeIfExist) {
            entityStore.removeEntity(previewEntityRef, RemoveReason.REMOVE);
            previewEntityRef = null;
        }
        else if (previewEntityRef != null && previewEntityRef.isValid()) {
            return;
        }

        Model model = Model.createStaticScaledModel(ModelAsset.getAssetMap().getAsset(getSpawnType()), 0.5f);

        Holder<EntityStore> preview = entityStore.getRegistry().newHolder();
        preview.addComponent(NetworkId.getComponentType(), new NetworkId(entityStore.getExternalData().takeNextNetworkId()));
        preview.addComponent(TransformComponent.getComponentType(), new TransformComponent(blockPos.toVector3d().add(.5f, .125f, .5f), Vector3f.ZERO));
        preview.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        preview.addComponent(PersistentModel.getComponentType(), new PersistentModel(new Model.ModelReference(getSpawnType(), .5f, null, true)));
        preview.addComponent(HeadRotation.getComponentType(), new HeadRotation(Vector3f.ZERO));
        preview.addComponent(PropComponent.getComponentType(), PropComponent.get());
        preview.ensureComponent(UUIDComponent.getComponentType());
        previewEntityRef = entityStore.addEntity(preview, AddReason.SPAWN);
    }

    public void removePreviewEntity(CommandBuffer<ChunkStore> commandBuffer) {
        Store<EntityStore> entityStore = commandBuffer.getExternalData().getWorld().getEntityStore().getStore();
        if (previewEntityRef != null && previewEntityRef.isValid()) {
            entityStore.removeEntity(previewEntityRef, RemoveReason.REMOVE);
            previewEntityRef = null;
        }
    }
}
