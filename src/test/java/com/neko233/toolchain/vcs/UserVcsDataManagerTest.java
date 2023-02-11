package com.neko233.toolchain.vcs;

import com.neko233.toolchain.common.base.CollectionUtils233;
import com.neko233.toolchain.testDto.TestDto;
import com.neko233.toolchain.vcs.data.UserVcsDataManager;
import com.neko233.toolchain.vcs.data.VcsData;
import com.neko233.toolchain.vcs.data.VersionDiffTooLargeException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UserVcsDataManagerTest {

    UserVcsDataManager<TestDto> manager = new UserVcsDataManager<>();

    @Test
    public void add_getDiff() throws VersionDiffTooLargeException {
        for (int i = 0; i < 10; i++) {
            TestDto abc = TestDto.builder()
                    .data("abc" + i)
                    .build();
            int returnVersion = manager.add("default", abc);

            final List<VcsData<TestDto>> list = manager.getDiffVcsDataList("default", returnVersion);
            Assert.assertTrue(CollectionUtils233.isEmpty(list));
        }
    }
}