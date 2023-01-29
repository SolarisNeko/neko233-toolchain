package com.neko233.toolchain.wal;

import com.neko233.toolchain.common.base.PreconditionUtils233;
import com.neko233.toolchain.common.base.TimeCostUtils233;
import com.neko233.toolchain.testDto.TestDto;
import com.neko233.toolchain.wal.impl.LogDefaultWal;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;

/**
 * @author SolarisNeko
 * Date on 2023-01-28
 */
@Slf4j
public class LogDefaultWalTest {

//    @Test
    public void testSpeed() throws InterruptedException, IllegalAccessException {
        final File file = new File("/Users/samo/Desktop/Log/Test-WAL/test-1/demo.data");

        AbstractWal<TestDto> wal = null;
        try {
            int count = 0;
            wal = new LogDefaultWal<>(file, TestDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PreconditionUtils233.checkNotNull(wal, "wal init error!");


        long ms = TimeCostUtils233.executeFunctionSpendMs((tempWal) -> {
            for (int i = 0; i < 10000 * 10; i++) {
                TestDto testDto = new TestDto(String.valueOf(i));
                try {
                    tempWal.inputData(testDto);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }, wal);


        Thread.sleep(5 * 1000);

        // 10w data, 32 ms done
        log.info("ms = {}", ms);
    }
}