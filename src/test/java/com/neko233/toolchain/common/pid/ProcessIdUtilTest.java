package com.neko233.toolchain.common.pid;

import com.neko233.toolchain.common.base.StringUtils233;
import com.neko233.toolchain.common.pid.ProcessIdUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author SolarisNeko
 * Date on 2022-12-17
 */
public class ProcessIdUtilTest {

    @Test
    public void test() {
        String processId = ProcessIdUtil.getProcessId();

        Assert.assertTrue(StringUtils233.isNotBlank(processId));
    }

}