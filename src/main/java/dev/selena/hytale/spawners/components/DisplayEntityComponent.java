package dev.selena.hytale.spawners.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.Nullable;

public class DisplayEntityComponent implements Component<EntityStore> {

    public static final BuilderCodec<DisplayEntityComponent> CODEC = BuilderCodec.builder(DisplayEntityComponent.class, DisplayEntityComponent::new)
            .build();

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new DisplayEntityComponent();
    }
}
