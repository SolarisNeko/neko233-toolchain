package com.neko233.toolchain.common.threadPool;

import com.neko233.toolchain.common.threadPool.PrefixThreadFactory;
import com.neko233.toolchain.common.threadPool.ThreadPoolBuilder;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolBuilderTest {

    @Test
    public void test1() {
        ThreadPoolExecutor poolExecutor = ThreadPoolBuilder.builder()
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(1)
                .keepAliveTimeUnit(TimeUnit.SECONDS)
                .threadFactory(PrefixThreadFactory.create("test"))
                .taskQueue(new LinkedBlockingQueue<>())
                .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .build();

        poolExecutor.shutdownNow();
    }

}