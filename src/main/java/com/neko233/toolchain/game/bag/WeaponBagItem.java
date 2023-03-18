package com.neko233.toolchain.game.bag;

import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * @author SolarisNeko
 * Date on 2023-03-18
 */
@AllArgsConstructor
@Builder
public class WeaponBagItem implements BagItem {

    private Long userId;
    private Long groupId;
    private Long itemId;
    private Long createMs;


    @Override
    public Long userId() {
        return this.userId;
    }

    @Override
    public Long groupId() {
        return this.groupId;
    }

    @Override
    public Long itemId() {
        return this.itemId;
    }

    @Override
    public Long count() {
        return 1L;
    }

    @Override
    public Long maxCount() {
        return 1L;
    }

    @Override
    public Long globalTrackItemUniqueId() {
        return null;
    }

    @Override
    public Long createMs() {
        return this.createMs;
    }

    @Override
    public Long expireMs() {
        return Long.MAX_VALUE;
    }

    @Override
    public String extraParamJson() {
        return null;
    }
}
