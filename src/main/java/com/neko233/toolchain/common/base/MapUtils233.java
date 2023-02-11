package com.neko233.toolchain.common.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtils233 {

    private MapUtils233() {
    }

    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(final Map<?, ?> collection) {
        return !isEmpty(collection);
    }


    public static <K, V> void putDataIntoList(Map<K, List<V>> map, K key, V data) {
        List<V> dataList = map.computeIfAbsent(key, k -> new ArrayList<V>());
        dataList.add(data);
    }
}
