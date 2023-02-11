package com.neko233.toolchain.vcs.data;

import lombok.Builder;
import lombok.Data;

/**
 * @author SolarisNeko on 2023-01-01
 * @param <T> type
 */
@Data
@Builder
public class VcsData<T> {

    private final String routePath;
    private final int version;
    private final T data;


}