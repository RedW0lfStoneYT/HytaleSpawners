package dev.selena.hytale.spawners.util.dynamictooltips.providers;

import dev.selena.hytale.spawners.util.SpawnerUtil;
import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * FROM <a href="https://github.com/Herolias/TooltipExample">https://github.com/Herolias/TooltipExample</a>
 */

public class RenameTooltipProvider implements TooltipProvider {

    @Nonnull
    @Override
    public String getProviderId() {
        return "spawners:name";
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT;
    }

    @Nullable
    @Override
    public TooltipData getTooltipData(@Nonnull String itemId, @Nullable String metadata) {
        if (metadata == null || !metadata.contains("rename")) return null;

        String customName = SpawnerUtil.extractStringValue(metadata, "rename");
        if (customName == null) return null;

        return TooltipData.builder()
                .nameOverride(customName)
                .hashInput("rename:" + customName)
                .build();
    }
}
