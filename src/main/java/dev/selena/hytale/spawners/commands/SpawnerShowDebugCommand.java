package dev.selena.hytale.spawners.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.Size;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import dev.selena.core.util.PlaceholderUtil;
import dev.selena.hytale.spawners.blockstates.SpawnerBlock;
import dev.selena.hytale.spawners.util.SpawnerUtil;
import dev.selena.hytale.spawners.util.config.Config;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SpawnerShowDebugCommand extends AbstractAsyncPlayerCommand {

    private final OptionalArg<RelativeIntPosition> location;
    public SpawnerShowDebugCommand() {
        super("debug-spawner", "Shows debug info about the spawner at a location");
        location = withOptionalArg("location", "The location of the spawner", ArgTypes.RELATIVE_BLOCK_POSITION);
        requirePermission("spawner.admin.debug");
        addAliases("debug");
    }

    @NotNull
    @Override
    protected CompletableFuture<Void> executeAsync(@NotNull CommandContext commandContext, @NotNull Store<EntityStore> store, @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {

        return CompletableFuture.runAsync(() -> {
            Vector3i position = location.provided(commandContext) ? location.get(commandContext).getBlockPosition(commandContext, store) :
                    TargetUtil.getTargetBlock(ref, 10.0d, store);
            if (position == null) {
                playerRef.sendMessage(PlaceholderUtil.parsePlaceholdersToMessage("No block in sight within 10 blocks"));
                return;
            }
            SpawnerBlock spawner = SpawnerUtil.getSpawnerAtLocation(position, world);
            if (spawner == null) {
                playerRef.sendMessage(PlaceholderUtil.parsePlaceholdersToMessage("No spawner found at {x}, {y}, {z}",
                        "{x}", String.valueOf(position.getX()),
                        "{y}", String.valueOf(position.getY()),
                        "{z}", String.valueOf(position.getZ())));
                return;
            }

            Size spawnArea = spawner.getSpawnRadius();
            Vector3d spawnAreaShape = new Vector3d(spawnArea.width * 2 + 1, spawnArea.height * 2 + 1, spawnArea.width * 2 + 1);
            showCube(world, DebugUtils.COLOR_GREEN, position, spawnAreaShape);

            if (!Config.get().isCheckNearbyEntities())
                return;
            Size nearbyCheckArea = Config.get().getNearbyEntitiesCheckRadius();
            if (nearbyCheckArea.height == spawnArea.height && nearbyCheckArea.width == spawnArea.width)
                return;

            Vector3d checkAreaShape = new Vector3d(nearbyCheckArea.width * 2 + 1, nearbyCheckArea.height * 2 + 1, nearbyCheckArea.width * 2 + 1);
            showCube(world, DebugUtils.COLOR_RED, position, checkAreaShape);

        }, world);
    }

    private void showCube(World world, Vector3f color, Vector3i position, Vector3d size) {
        Matrix4d matrix = new Matrix4d();
        matrix.identity();
        matrix.translate(position.toVector3d().add(0.5, 0.5, 0.5));
        matrix.scale(size.x, size.y, size.z);
        int flag = buildFlags(true, true, false);
        DebugUtils.add(world, DebugShape.Cube, matrix, color, 0.25f, 30, flag);
    }

    // From com.hypixel.hytale.server.core.modules.debug.commands.DebugShapeSubCommand
    private int buildFlags(boolean fadeFlag, boolean noWireframeFlag, boolean noSolidFlag) {
        int flags = 0;
        if (fadeFlag) {
            flags = DebugUtils.FLAG_FADE;
        }
        if (noWireframeFlag) {
            flags |= DebugUtils.FLAG_NO_WIREFRAME;
        }
        if (noSolidFlag) {
            flags |= DebugUtils.FLAG_NO_SOLID;
        }
        return flags;
    }


}
