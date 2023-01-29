package com.neko233.toolchain.common.yaml;

import com.neko233.toolchain.parser.yaml.YamlData;
import com.neko233.toolchain.parser.yaml.YamlUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author SolarisNeko
 * Date on 2023-01-08
 */
public class YamlUtilsTest {

    @Test
    public void read() {
        YamlData read = YamlUtils.read("application.yaml");
        String serverName = read.getString("server.name");
        Assert.assertEquals("neko233-server", serverName);
    }
}