package dev.selena.hytale.spawners.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.Size;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.role.Role;
import dev.selena.core.util.PlaceholderUtil;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.util.config.Config;
import dev.selena.hytale.spawners.util.config.Lang;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SpawnerGiveCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> spawnerTypeArg;
    private final RequiredArg<PlayerRef> playerArg;
    private final DefaultArg<Integer> amountArg;
    private final DefaultArg<Pair<Integer, Integer>> spawnAmountArg;
    private final DefaultArg<Pair<Integer, Integer>> spawnIntervalArg;
    private final DefaultArg<Pair<Integer, Integer>> spawnRadiusArg;

    public SpawnerGiveCommand() {
        super("spawner", "Test");
        this.requirePermission("spawner.give");
        spawnerTypeArg = this.withRequiredArg("spawnerType", "the type of spawner you want to give", ArgTypes.STRING);
        playerArg = this.withRequiredArg("player", "the player to give the spawner to", ArgTypes.PLAYER_REF);
        amountArg = this.withDefaultArg("amount", "the amount of spawners to give", ArgTypes.INTEGER, 1, "1 by default");
        Range spawnCount = Config.get().getSpawnRange();
        spawnAmountArg = this.withDefaultArg("spawnAmount", "the amount of entities the spawner will spawn at once", ArgTypes.INT_RANGE, Pair.of(spawnCount.min, spawnCount.max), "1-5 by default");
        Range spawnInterval = Config.get().getSpawnTicksRange();
        spawnIntervalArg = this.withDefaultArg("spawnInterval", "the interval at which the spawner will spawn entities", ArgTypes.INT_RANGE, Pair.of(spawnInterval.min, spawnInterval.max), "200-400 by default");
        Size spawnRadius = Config.get().getSpawnRadius();
        spawnRadiusArg = this.withDefaultArg("spawnRadius", "the radius in which the spawner will spawn entities", ArgTypes.INT_RANGE, Pair.of(spawnRadius.width, spawnRadius.height), "3x3 by default");
    }

    @NotNull
    @Override
    protected CompletableFuture<Void> executeAsync(@NotNull CommandContext commandContext) {
        PlayerRef playerRef = playerArg.get(commandContext);
        if (playerRef == null || playerRef.getReference() == null) {
            commandContext.sender().sendMessage(PlaceholderUtil.parsePlaceholdersToMessage(Lang.get().getNullTarget()));
            return CompletableFuture.completedFuture(null);
        }

        String spawnerType = spawnerTypeArg.get(commandContext);

        int roleIndex = NPCPlugin.get().getIndex(spawnerType);
        Builder<Role> roleBuilder = NPCPlugin.get().tryGetCachedValidRole(roleIndex);
        if (roleBuilder == null || !roleBuilder.isSpawnable()) {
            commandContext.sender().sendMessage(PlaceholderUtil.parsePlaceholdersToMessage(Lang.get().getRoleNotFound(), "{spawner_type}", spawnerType));
            throw new IllegalArgumentException("Abstract role templates cannot be spawned directly - a variant needs to be created!");
        }

        int amount = amountArg.get(commandContext);
        Pair<Integer, Integer> spawnAmount = spawnAmountArg.get(commandContext);
        Range spawnCountRange = new Range(spawnAmount.first(), spawnAmount.second());
        Pair<Integer, Integer> spawnInterval = spawnIntervalArg.get(commandContext);
        Range spawnIntervalRange = new Range(spawnInterval.first(), spawnInterval.second());
        Pair<Integer, Integer> spawnRadius = spawnRadiusArg.get(commandContext);
        Size spawnRadiusSize = new Size(spawnRadius.first(), spawnRadius.second());
        Store<EntityStore> store = playerRef.getReference().getStore();
        World world = store.getExternalData().getWorld();
        SpawnerBlock spawner = new SpawnerBlock(spawnerType);
        spawner.setSpawnCount(spawnCountRange);
        spawner.setSpawnIntervalTicks(spawnIntervalRange);
        spawner.setSpawnRadius(spawnRadiusSize);
        ItemStack spawnerItem = spawner.getItemStack().withQuantity(amount);

        assert spawnerItem != null;
        spawnerItem = spawnerItem.withMetadata("SpawnerType", SpawnerBlock.CODEC, spawner);

        ItemStack finalSpawnerItem = spawnerItem;

        return CompletableFuture.runAsync(() -> {

            Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());
            CombinedItemContainer inventory = new CombinedItemContainer(player.getInventory().getHotbar(), player.getInventory().getStorage());
            inventory.addItemStack(finalSpawnerItem);
            commandContext.sender().sendMessage(PlaceholderUtil.parsePlaceholdersToMessage(Lang.get().getSpawnerGiven(),
                    "{spawner_type}", spawnerType,
                    "{player}", playerRef.getUsername(),
                    "{amount}", String.valueOf(amount)
            ));
            player.sendMessage(PlaceholderUtil.parsePlaceholdersToMessage(Lang.get().getSpawnerReceived(),
                    "{spawner_type}", spawnerType,
                    "{amount}", String.valueOf(amount)
            ));
        }, world);
    }

}
