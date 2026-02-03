package dev.selena.hytale.spawners.util.config;

import com.google.gson.annotations.Expose;
import dev.selena.core.config.Comment;
import dev.selena.hytale.spawners.SpawnerMain;
import lombok.Getter;

public class Lang {

    @Getter
    @Expose
    @Comment("The message sent when target player is null")
    private String nullTarget = "&cError: Target player is null.";

    @Getter
    @Expose
    @Comment("The message sent when the target role/entity is not found")
    private String roleNotFound = "&cError: Role/Entity not found.";

    @Getter
    @Expose
    @Comment("The message sent to sender when the spawner is successfully given")
    private String spawnerGiven = "&aSuccessfully given {amount}x {spawner_type} spawners to {player}.";

    @Getter
    @Expose
    @Comment("The message sent to target player when they receive a spawner from")
    private String spawnerReceived = "&aYou have received &e{amount}x {spawner_type}&a spawners.";

    @Getter
    @Expose
    @Comment("The name format of the spawner item - NOT SUPPORTED YET - FUTURE PLANS")
    private String spawnerItemName = "&6{spawner_type} Spawner";

    @Getter
    @Expose
    @Comment("The lore format of the spawner item - NOT SUPPORTED YET - FUTURE PLANS")
    private String spawnerItemLore = "&7Place to spawn &e{spawner_type}&7.";

    public static Lang get() {
        return SpawnerMain.get().getLang();
    }
}
