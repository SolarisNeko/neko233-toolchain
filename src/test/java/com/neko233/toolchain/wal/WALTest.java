package com.neko233.toolchain.wal;

import com.alibaba.fastjson2.JSON;
import com.neko233.toolchain.common.base.PreconditionUtils233;
import com.neko233.toolchain.testDto.TestDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;

/**
 * @author SolarisNeko
 * Date on 2023-01-28
 */
@Slf4j
public class WALTest {

//    @Test
    public void notATest() throws InterruptedException, IllegalAccessException {
        File file = new File("/Users/samo/Desktop/Log/Test-WAL/test-1/demo.data");
        WAL<TestDto> wal = null;
        try {
            int count = 0;
            wal = new WAL<>(file, TestDto.class, (dataList) -> {
                for (TestDto testDto : dataList) {
                    log.warn("data = {}", JSON.toJSONString(testDto));
                }
            },
                    null, null, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PreconditionUtils233.checkNotNull(wal, "wal init error!");


        for (int i = 0; i < 1000; i++) {
            TestDto testDto = new TestDto(String.valueOf(i));
            wal.writeData(testDto);
        }


        Thread.sleep(100 * 1000);
    }
}