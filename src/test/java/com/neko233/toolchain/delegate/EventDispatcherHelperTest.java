package com.neko233.toolchain.delegate;

import com.neko233.toolchain.mockData.MockDataDto;
import com.neko233.toolchain.event.delegate.EventDispatcher;
import com.neko233.toolchain.event.delegate.EventDispatcherHelper;
import org.junit.Assert;
import org.junit.Test;

public class EventDispatcherHelperTest {


    MockDataDto data = MockDataDto.builder().name("neko233").build();

    @Test
    public void testBase() {
        String packageName = TestEventListener.class.getPackage().getName();

        EventDispatcher dispatcher = EventDispatcher.createDefault();
        EventDispatcherHelper.registerByMethodInPackageScan(dispatcher, packageName);

        Assert.assertTrue(dispatcher.getDispatcherMap().size() > 0);

        boolean isOk = true;
        try {
            dispatcher.sync(data);
        } catch (Exception e) {
            isOk = false;
        }
        Assert.assertTrue(isOk);
    }

}