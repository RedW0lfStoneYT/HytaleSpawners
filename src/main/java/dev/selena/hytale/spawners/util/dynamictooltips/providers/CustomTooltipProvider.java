package dev.selena.hytale.spawners.util.dynamictooltips.providers;

import dev.selena.hytale.spawners.util.SpawnerUtil;
import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomTooltipProvider implements TooltipProvider {

    @Nonnull
    @Override
    public String getProviderId() {
        return "spawners:lore";
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT;
    }

    @Nullable
    @Override
    public TooltipData getTooltipData(@Nonnull String itemId, @Nullable String metadata) {
        if (metadata == null) return null;

        String tooltipDesc = SpawnerUtil.extractStringValue(metadata, "tooltip_desc");
        String tooltipLines = SpawnerUtil.extractStringValue(metadata, "tooltip_lines");

        if (tooltipDesc == null && tooltipLines == null) return null;

        TooltipData.Builder builder = getBuilder(tooltipDesc, tooltipLines);

        return builder.build();
    }

    @NotNull
    private static TooltipData.Builder getBuilder(String tooltipDesc, String tooltipLines) {
        TooltipData.Builder builder = TooltipData.builder();

        if (tooltipDesc != null) {

            builder.descriptionOverride(tooltipDesc);
            builder.hashInput("desc:" + tooltipDesc);
        } else {
            String[] lines = tooltipLines.split("\\|");
            for (String line : lines) {
                builder.addLine(line);
            }
            builder.hashInput("lines:" + tooltipLines);
        }
        return builder;
    }
}