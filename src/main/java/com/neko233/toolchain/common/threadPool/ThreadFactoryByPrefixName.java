package com.neko233.toolchain.common.threadPool;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryByPrefixName implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger counter = new AtomicInteger(1);

    public ThreadFactoryByPrefixName(String prefix) {
        this.prefix = prefix;
    }

    public static ThreadFactoryByPrefixName create(String prefix) {
        return new ThreadFactoryByPrefixName(prefix);
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread t = new Thread(r);
        t.setName(prefix + "-" + counter.getAndIncrement());
        return t;
    }
}
