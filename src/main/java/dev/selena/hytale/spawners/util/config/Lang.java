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
    private String spawnerGiven = "&aSuccessfully given {amount}x {spawner_type_name} spawners to {player}.";

    @Getter
    @Expose
    @Comment("The message sent to target player when they receive a spawner from")
    private String spawnerReceived = "&aYou have received &e{amount}x {spawner_type_name}&a spawners.";

    @Getter
    @Expose
    @Comment("""
            The name format of the spawner item
            Working placeholders:
            {spawner_type} - The raw type, example Pig_Undead
            {spawner_type_name} - The parsed name, example Undead Pig (Uses the translation key "server.npcRoles.<role_name>.name")
            """)
    private String spawnerItemName = "{spawner_type_name} Spawner";

    @Getter
    @Expose
    @Comment("""
            The lore format of the spawner item
            Working placeholders:
            {spawner_type} - The raw type, example Pig_Undead
            {spawner_type_name} - The parsed name, example Undead Pig (Uses the translation key "server.npcRoles.<role_name>.name")
            """)
    private String spawnerItemLore = "<color is=\"#AAAAAA\">Place to spawn <color is=\"#FFFF55\">{spawner_type_name}</color>s around the spawner.</color>";

    public static Lang get() {
        return SpawnerMain.get().getLang();
    }
}
