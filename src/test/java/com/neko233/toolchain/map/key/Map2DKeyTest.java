package com.neko233.toolchain.map.key;

import com.neko233.toolchain.game.map.key.Map2DKey;
import org.junit.Test;

import java.util.List;

/**
 * @author SolarisNeko
 * Date on 2022-12-17
 */
public class Map2DKeyTest {

    @Test
    public void test() {
        List<Map2DKey> output = Map2DKey.generateXyAroundGridList(Map2DKey.builder()
                .mapId(0)
                .x(0)
                .y(0)
                .build()
        );

        System.out.println(output);
    }

}