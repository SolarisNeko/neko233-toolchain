package com.neko233.toolchain.common.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author SolarisNeko
 * Date on 2023-04-23
 */
public class SetUtils233 {


    /**
     * 模拟 JDK 11+ 的 Set.of
     *
     * @param objs 对象
     * @return Set
     */
    @SafeVarargs
    public static <V> Set<V> of(V... objs) {
        if (objs == null) {
            return new HashSet<>(0);
        }
        return Arrays.stream(objs).collect(Collectors.toSet());
    }
}
