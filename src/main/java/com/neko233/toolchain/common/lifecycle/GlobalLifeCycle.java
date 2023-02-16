package com.neko233.toolchain.common.lifecycle;

/**
 * @author SolarisNeko on 2022-12-07
 **/
public interface GlobalLifeCycle {

    /**
     * init
     */
    void init() throws Throwable;

    /**
     * create Part
     */
    default void preCreate() {
    }

    void create() throws Throwable;

    default void postCreate() {
    }

    /**
     * destroy Part
     */
    default void preDestroy() {
    }

    void destroy();

    default void postDestroy() {
    }

    /**
     * shutdown must successfully
     */
    void shutdown();

}
