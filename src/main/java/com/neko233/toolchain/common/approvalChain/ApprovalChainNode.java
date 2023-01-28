package com.neko233.toolchain.common.approvalChain;

/**
 * @author SolarisNeko
 * Date on 2022-12-15
 */
public interface ApprovalChainNode<INPUT, OUTPUT> {

    boolean isItHandle();

    OUTPUT handle(INPUT input);


}
