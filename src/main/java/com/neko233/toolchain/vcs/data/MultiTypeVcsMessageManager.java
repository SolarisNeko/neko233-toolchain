package com.neko233.toolchain.vcs.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多种类型 - 数据版本控制
 * 场景: push / pull, client 多线程 A, B, C
 *
 * @param <T>
 */
public class MultiTypeVcsMessageManager<T> {

    private final Map<String, VcsMessageManager<T>> multiTypeToManagerMap = new ConcurrentHashMap<>();

    public int add(String type, String routePath, T data) {
        VcsMessageManager<T> tVcsMessageManager = getVcsDataManagerByType(type);
        return tVcsMessageManager.add(routePath, data);
    }


    public List<VcsMessage<T>> getDiffVcsDataList(String type, String routePath, int otherVersion) throws VersionDiffTooLargeException {
        VcsMessageManager<T> tVcsMessageManager = getVcsDataManagerByType(type);
        return tVcsMessageManager.getDiffVcsDataList(routePath, otherVersion);
    }

    @NotNull
    private VcsMessageManager<T> getVcsDataManagerByType(String type) {
        return multiTypeToManagerMap.computeIfAbsent(type, k -> new VcsMessageManager<T>());
    }


}
