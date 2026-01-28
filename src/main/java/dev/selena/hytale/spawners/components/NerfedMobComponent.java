package dev.selena.hytale.spawners.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class NerfedMobComponent implements Component<EntityStore> {

    @Getter
    @Setter
    private static ComponentType<EntityStore, NerfedMobComponent> componentType;

    public static final BuilderCodec<NerfedMobComponent> CODEC = BuilderCodec.builder(NerfedMobComponent.class, NerfedMobComponent::new)
            .append(new KeyedCodec<>("DropListID", Codec.STRING),
                    (component, integer) -> {
                        component.drops = integer;
                    }, component -> component.drops
            ).add().build();

    @Getter
    @Setter
    private String drops;

    public NerfedMobComponent() {
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        NerfedMobComponent clone = new NerfedMobComponent();
        clone.setDrops(this.drops);
        return clone;
    }
}
