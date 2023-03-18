package com.neko233.toolchain.game.bag;

/**
 * 背包物品
 *
 * @author SolariNeko 2023-01-13
 **/
public interface BagItem {

    /**
     * @return 归属的用户 ID
     */
    Long userId();

    /**
     * @return 物品类型 ID
     */
    Long groupId();

    /**
     * @return 物品 ID
     */
    Long itemId();

    /**
     * @return 当前数量
     */
    Long count();


    /**
     * @return 最大数量 ｜ 默认 = 1
     */
    Long maxCount();


    /**
     * 部分道具需要唯一表示， 若不需要 == null
     *
     * @return 全局唯一物品追踪的 ID
     */
    Long globalTrackItemUniqueId();


    /**
     * @return 物品创建毫秒数
     */
    Long createMs();


    /**
     * @return 物品过期毫秒数
     */
    Long expireMs();

    /**
     * @return 物品扩展参数 JSON String
     */
    String extraParamJson();

}
