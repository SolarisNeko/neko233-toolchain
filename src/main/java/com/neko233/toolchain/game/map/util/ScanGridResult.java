package com.neko233.toolchain.game.map.util;

import com.neko233.toolchain.game.map.Grid;
import com.neko233.toolchain.game.map.key.Map3DKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author SolarisNeko
 * Date on 2022-12-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanGridResult {

    private Grid centerGrid;
    private Map<Grid, List<Map3DKey>> aggByGridMap;


}
