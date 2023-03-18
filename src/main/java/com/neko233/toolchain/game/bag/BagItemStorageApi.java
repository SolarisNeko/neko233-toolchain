package com.neko233.toolchain.game.bag;

import java.util.List;

/**
 * @author SolarisNeko
 * Date on 2023-03-18
 */
public interface BagItemStorageApi<T extends BagItem> {

    /**
     * save == insert + update
     *
     * @param bagItem 物品
     * @return bool
     */
    boolean save(BagItem bagItem);

    boolean delete(BagItem bagItem);

    T selectItem(Long userId, Long groupId, Long itemId);

    List<T> selectGroup(Long userId, Long groupId);


}
