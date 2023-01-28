package com.neko233.toolchain.common.base;

import com.neko233.toolchain.common.base.RegexUtils233;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author SolarisNeko
 * Date on 2023-01-28
 */
public class RegexUtils233Test {

    @Test
    public void convertTextForNotRegex() {
        // I want split by string "a|b"
        String regex = RegexUtils233.convertTextForNotRegex("a|b");

        String toSplit = "x1a|bx2";
        String[] split = toSplit.split(regex);

        assertEquals(2, split.length);
    }
}