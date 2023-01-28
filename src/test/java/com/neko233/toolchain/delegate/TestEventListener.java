package com.neko233.toolchain.delegate;

import com.neko233.toolchain.mockData.MockDataDto;
import com.neko233.toolchain.event.delegate.EventListener;

public class TestEventListener implements EventListener<MockDataDto> {

    @Override
    public void handle(MockDataDto event) {
        System.out.println(event);
    }

}