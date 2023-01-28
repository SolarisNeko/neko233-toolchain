package com.neko233.toolchain.common.cache.objects;

import java.util.concurrent.ConcurrentMap;

/**
 * @author SolarisNeko on 2022-12-14
 **/
public interface FlushListener<K, V> {

    void handle(ConcurrentMap<K, V> snapshot);

}
