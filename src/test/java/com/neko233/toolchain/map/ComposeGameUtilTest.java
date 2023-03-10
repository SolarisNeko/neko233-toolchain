package com.neko233.toolchain.map;

import com.alibaba.fastjson2.JSON;
import com.neko233.toolchain.game.map.Grid;
import com.neko233.toolchain.game.map.Map3D;
import com.neko233.toolchain.game.map.key.Map3DKey;
import com.neko233.toolchain.game.map.printer.Map3DPrinter;
import com.neko233.toolchain.game.map.util.ComposeGameUtil;
import com.neko233.toolchain.game.map.util.ScanGridResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SolarisNeko
 * Date on 2022-12-10
 */
public class ComposeGameUtilTest {


    @Test
    public void printMap3DTest() {
        Map<Map3DKey, Grid> mapConfig = new HashMap<>();
        for (int generateX = 0; generateX < 3; generateX++) {
            for (int generateY = 0; generateY < 3; generateY++) {
                mapConfig.put(Map3DKey.from(generateX, generateY, 0),
                        Grid.builder()
                                .isOpen(true)
                                .thingId(1)
                                .build());
            }
        }
        mapConfig.put(Map3DKey.from(0, 0, 0), Grid.builder().thingId(999).build());
        Map3D map3D = new Map3D(mapConfig);

        String s = Map3DPrinter.printMap2D(map3D);
        System.out.println(s);

    }

    @Test
    public void test1() {
//        Compose3GridGameMapUtil compose3GridGameMapUtil = new Compose3GridGameMapUtil();
//        demo(compose3GridGameMapUtil);

        mapTest();
    }


    public void demo(ComposeGameUtil composeGameUtil) {
        // 九宫格序号
        List<String> nineSpaceList = composeGameUtil.generateCsvForNineAreaList(1, 1, 1, 1);
        nineSpaceList.forEach(System.out::println);
    }


    public static void mapTest() {

        // init
        Map<Map3DKey, Grid> mapConfig = new HashMap<>();
        for (int generateX = 0; generateX < 3; generateX++) {
            for (int generateY = 0; generateY < 3; generateY++) {
                mapConfig.put(Map3DKey.from(generateX, generateY, 0), Grid.builder().thingId(1).build());
            }
        }
        mapConfig.put(Map3DKey.from(0, 0, 0), Grid.builder().thingId(999).build());
        Map3D map3D = new Map3D(mapConfig);

        System.out.println(JSON.toJSONString(map3D));


        // 目标位置

        // swap
        Map3D.swapGrid(map3D, 0, 0, 0, 1, 1, 0);
        // 结算
        ScanGridResult aggGridMap = ComposeGameUtil.scanTargetPointAround9Grid(
                map3D,
                1,
                1
        );

        System.out.println("nine grid = \n" + JSON.toJSONString(aggGridMap));

        List<Map3DKey> canMergeList = ComposeGameUtil.findCanMergeList(aggGridMap);
        System.out.println(JSON.toJSONString(canMergeList));


    }

}