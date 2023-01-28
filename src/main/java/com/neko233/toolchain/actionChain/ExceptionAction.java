package com.neko233.toolchain.actionChain;

@FunctionalInterface
public interface ExceptionAction {

    void handleException(Throwable e);

}