package com.neko233.toolchain.common.dataStruct.bloomfilter;

import java.io.Serializable;

/**
 * @author SolarisNeko
 * Date on 2023-01-28
 */
public interface BloomFilterApi<T> extends Serializable {

    void add(T value);

    boolean isMightContains(T value);

}
