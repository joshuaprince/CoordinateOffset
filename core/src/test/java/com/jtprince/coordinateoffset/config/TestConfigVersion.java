package com.jtprince.coordinateoffset.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class TestConfigVersion {
    @Test
    void testGetBackupPath() {
        Path p1 = Path.of("/abc/def/config.yml");
        Assertions.assertEquals(Path.of("/abc/def/config.v10.old.yml"), ConfigVersion.getBackupPath(p1, 10));

        Path p2 = Path.of("/abc/def/my.config.yml");
        Assertions.assertEquals(Path.of("/abc/def/my.config.v1.old.yml"), ConfigVersion.getBackupPath(p2, 1));

        Path p3 = Path.of("config.yml");
        Assertions.assertEquals(Path.of("config.v999.old.yml"), ConfigVersion.getBackupPath(p3, 999));

        Path p4 = Path.of("some.config.yml");
        Assertions.assertEquals(Path.of("some.config.v0.old.yml"), ConfigVersion.getBackupPath(p4, 0));
    }
}
