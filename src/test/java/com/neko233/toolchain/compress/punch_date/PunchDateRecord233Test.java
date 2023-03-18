package com.neko233.toolchain.compress.punch_date;

import com.neko233.toolchain.common.base.DateTimeUtils233;
import com.neko233.toolchain.common.compress.punch_date.PunchDateRecord233;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

public class PunchDateRecord233Test {


    @Test
    public void test_base() {
        PunchDateRecord233 punchDateRecord = new PunchDateRecord233();

        LocalDateTime parse = DateTimeUtils233.parse("2020-01-01", "yyyy-MM-dd");

        int handleCount = 10000;

        // punch
        for (int i = 0; i < handleCount; i++) {
            LocalDateTime from = parse.plusDays(i);
            punchDateRecord.punchDateByJdkDateTime(from);
        }

        // serialize & deserialize
        byte[] serializeByes = punchDateRecord.getSerializeByes();

        PunchDateRecord233 fromRecord = PunchDateRecord233.from(serializeByes);

        Assert.assertEquals(handleCount, fromRecord.cardinality());

        List<String> allPunchDateString = fromRecord.getAllPunchDateTimeString();
        Assert.assertEquals(handleCount, new HashSet<>(allPunchDateString).size());
    }

}