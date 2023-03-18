package com.neko233.toolchain.action.actionChain;

@FunctionalInterface
public interface ExceptionAction {

    void handleException(Throwable e);

}