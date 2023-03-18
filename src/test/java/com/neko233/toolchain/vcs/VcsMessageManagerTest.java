package com.neko233.toolchain.vcs;

import com.neko233.toolchain.common.base.CollectionUtils233;
import com.neko233.toolchain.testDto.TestDto;
import com.neko233.toolchain.vcs.vscMessage.VcsMessageManager;
import com.neko233.toolchain.vcs.vscMessage.data.VcsMessage;
import com.neko233.toolchain.vcs.vscMessage.exception.VersionDiffTooLargeException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class VcsMessageManagerTest {

    VcsMessageManager<TestDto> manager = new VcsMessageManager<>();

    @Test
    public void add_getDiff() throws VersionDiffTooLargeException {
        for (int i = 0; i < 10; i++) {
            TestDto abc = TestDto.builder()
                    .data("abc" + i)
                    .build();
            int returnVersion = manager.add("default", abc);

            final List<VcsMessage<TestDto>> list = manager.getDiffVcsDataList("default", returnVersion);
            Assert.assertTrue(CollectionUtils233.isEmpty(list));
        }
    }
}