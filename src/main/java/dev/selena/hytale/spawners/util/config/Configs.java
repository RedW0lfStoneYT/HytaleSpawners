package dev.selena.hytale.spawners.util.config;

import dev.selena.core.config.FileManager;
import dev.selena.core.config.IConfigs;
import lombok.Getter;

import java.io.File;

public enum Configs implements IConfigs {
    CONFIG(Config.class, "", "Config.json"),
    LANG(Lang.class, "", "Lang.json");

    @Getter
    private final File file;
    @Getter
    private final Class<?> clazz;

    <T> Configs(Class<T> clazz, String parent, String path) {
        this.clazz = clazz;
        this.file = FileManager.file(parent, path);
    }
}
