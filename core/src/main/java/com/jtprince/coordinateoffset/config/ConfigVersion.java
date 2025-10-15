package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Files;
import java.nio.file.Path;

@NullMarked
public class ConfigVersion {
    public static final int CURRENT = 5;
    public static final int ASSUMED_VERSION_IF_MISSING = 4;

    /**
     * @return true if after running this function, it is safe to write to the configPath; false if something went
     *         wrong and we should avoid writing
     */
    static boolean onLoadBaseConfig(Path configPath, CoordinateOffsetConfigBase config) {
        if (config.configVersion == null) {
            config.configVersion = ASSUMED_VERSION_IF_MISSING;
        }

        if (config.configVersion < CURRENT) {
            // Outdated config; Make backup of previous config before allowing any file writes
            try {
                Path dst = getBackupPath(configPath, config.configVersion);
                CoordinateOffsetCore.get().getLogger().info("Detected new configuration version " + CURRENT +
                    "; backing up " + configPath.getFileName() + " to " + dst.getFileName());
                Files.copy(configPath, dst);
            } catch (Exception e) {
                CoordinateOffsetCore.get().getLogger().severe("Failed to back up CoordinateOffset config; leaving file untouched");
                return false;
            }
        }

        if (config.configVersion > CURRENT) {
            CoordinateOffsetCore.get().getLogger().warning("Found config version " + config.configVersion +
                " which is higher than the version of the plugin; I'll try my best to interpret it but I won't try to" +
                " upgrade it to the latest config standard.");
            return false;
        }

        return true;
    }

    static Path getBackupPath(Path configPath, int version) {
        String newName;
        String fileName = configPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            newName = "v" + version + ".old." + fileName;
        } else {
            newName = fileName.substring(0, dotIndex) + ".v" + version + ".old" + fileName.substring(dotIndex);
        }
        return configPath.resolveSibling(newName);
    }
}
