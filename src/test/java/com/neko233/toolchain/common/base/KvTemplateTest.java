package com.neko233.toolchain.common.base;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author SolarisNeko
 * Date on 2023-04-13
 */
public class KvTemplateTest {

    @Test
    public void test_base() {
        String input = "Hello, ${name}! Welcome to ${city}.";

        String build = KvTemplate.builder(input)
                .put("name", "SolarisNeko")
                .put("city", "BlackHole")
                .build();

        Assert.assertEquals(build, "Hello, SolarisNeko! Welcome to BlackHole.");
    }


    @Test
    public void test_parse() {
        String input = "Hello, ${name}! Welcome to ${city}.";
        List<String> placeholders = KvTemplate.parsePlaceHolder(input);
        boolean v1 = placeholders.contains("name");
        boolean v2 = placeholders.contains("city");

        Assert.assertTrue(v1 && v2);
    }


}