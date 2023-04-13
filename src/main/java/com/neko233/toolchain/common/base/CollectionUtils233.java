package com.neko233.toolchain.common.base;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import java.util.*;
import java.util.stream.Collectors;

public class CollectionUtils233 {

    private CollectionUtils233() {
    }

    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(final Collection<?> collection) {
        return !isEmpty(collection);
    }


    public static <T> Collection<T> addAll(List<T> es, Enumeration<T> networkInterfaces) {
        if (networkInterfaces == null) {
            return es;
        }
        if (es == null) {
            es = new ArrayList<>();
        }
        while (networkInterfaces.hasMoreElements()) {
            es.add(networkInterfaces.nextElement());
        }
        return es;
    }

    public static <E> ArrayList<E> newArrayList(E... elements) {
        Preconditions.checkNotNull(elements);
        int capacity = computeArrayListCapacity(elements.length);
        ArrayList<E> list = new ArrayList(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    public static <E> Set<E> newHashSet(E... elements) {
        Preconditions.checkNotNull(elements);
        int capacity = computeArrayListCapacity(elements.length);
        Set<E> list = new HashSet<>(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    @VisibleForTesting
    static int computeArrayListCapacity(int arraySize) {
        checkSizeNonnegative(arraySize, "arraySize");
        return Ints.saturatedCast(5L + (long) arraySize + (long) (arraySize / 10));
    }

    /**
     * 检查非负数
     *
     * @param value
     * @param name
     * @return
     */
    static int checkSizeNonnegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
        }
        return value;
    }

    /**
     * 模拟 JDK 11+ 的 List.of
     *
     * @param objs 对象
     * @return List
     */
    @SafeVarargs
    public static <V> List<V> ofList(V... objs) {
        if (objs == null) {
            return new ArrayList<>(0);
        }
        return Arrays.stream(objs).collect(Collectors.toList());
    }

    /**
     * 模拟 JDK 11+ 的 Set.of
     *
     * @param objs 对象
     * @return Set
     */
    @SafeVarargs
    public static <V> Set<V> ofSet(V... objs) {
        if (objs == null) {
            return new HashSet<>(0);
        }
        return Arrays.stream(objs).collect(Collectors.toSet());
    }

}
