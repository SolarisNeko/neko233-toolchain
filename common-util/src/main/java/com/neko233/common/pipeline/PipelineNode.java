package com.neko233.common.pipeline;

/**
 * @author SolarisNeko
 * Date on 2022-12-15
 */
public interface PipelineNode<T> {

    T handle(T input);

}
