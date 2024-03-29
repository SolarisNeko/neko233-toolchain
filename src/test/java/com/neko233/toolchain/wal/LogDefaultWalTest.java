package com.neko233.toolchain.wal;

import com.neko233.toolchain.common.base.ListGenerator233;
import com.neko233.toolchain.common.base.PreconditionUtils233;
import com.neko233.toolchain.common.base.TimeCostUtils233;
import com.neko233.toolchain.storage.wal.AbstractWalV1;
import com.neko233.toolchain.testDto.TestDto;
import com.neko233.toolchain.storage.wal.impl.LogDefaultWalV1;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author SolarisNeko
 * Date on 2023-01-28
 */
@Slf4j
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@Fork(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class LogDefaultWalTest {
    public static void main(String[] args) throws RunnerException {
        Options jmhOptions = new OptionsBuilder()
                .include(LogDefaultWalTest.class.getSimpleName())
                .build();
        Runner jmhRunner = new Runner(jmhOptions);
        jmhRunner.run();
    }

    @Benchmark
    public void testSpeed() throws InterruptedException, IllegalAccessException {
        final File file = new File("/Users/neko233/Desktop/Log/Test-WAL/test-1/demo.data");

        AbstractWalV1<TestDto> wal = null;
        try {
            int count = 0;
            wal = new LogDefaultWalV1<>(file, TestDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PreconditionUtils233.checkNotNull(wal, "wal init error!");


        long ms = TimeCostUtils233.executeFunctionSpendMs((tempWal) -> {
            List<TestDto> testDtos = ListGenerator233.generateObjectByCount(100 * 1000, (i) -> new TestDto(String.valueOf(i)));

            try {
                tempWal.inputData(testDtos);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, wal);


        Thread.sleep(10 * 1000);

        // 10w data, 32 ms done
        log.info("write to WAL spend ms = {}", ms);
    }
}