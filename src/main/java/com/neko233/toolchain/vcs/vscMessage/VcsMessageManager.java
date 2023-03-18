package com.neko233.toolchain.vcs.vscMessage;

import com.neko233.toolchain.vcs.vscMessage.data.VcsMessage;
import com.neko233.toolchain.vcs.vscMessage.exception.VersionDiffTooLargeException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户 VCS data 管理
 *
 * @param <T> type
 * @author SolarisNeko on 2023-01-01
 */
@Slf4j
public class VcsMessageManager<T> {


    private int maxSize;
    private final Map<String, Integer> versionNumberMap;

    private final Map<String, LinkedList<VcsMessage<T>>> vcsDataRouteMap;

    public VcsMessageManager() {
        this.maxSize = 20;
        this.versionNumberMap = new ConcurrentHashMap<>();
        this.vcsDataRouteMap = new ConcurrentHashMap<>();
    }


    public int add(String routePath, T data) {
        synchronized (this) {
            LinkedList<VcsMessage<T>> vcsMessageList = vcsDataRouteMap.computeIfAbsent(routePath, k -> new LinkedList<>());
            int newVersion = versionNumberMap.merge(routePath, 1, (v1, v2) -> v1 + 1);

            final VcsMessage<T> vcsMessage = (VcsMessage<T>) VcsMessage.builder()
                    .routePath(routePath)
                    .version(newVersion)
                    .data(data)
                    .build();

            if (vcsMessageList.size() >= maxSize) {
                vcsMessageList.removeFirst();
            }
            vcsMessageList.add(vcsMessage);

            // 版本
            return newVersion;
        }
    }


    /**
     * diff( client.server.version, server.server.version ) return vcs-data list
     *
     * @param clientHoldServerVersion 客户端持有 server 的版本号
     * @return client.server.version 和 server.server.version 差异版本的消息
     */
    public List<VcsMessage<T>> getDiffVcsDataList(String routePath, Integer clientHoldServerVersion)
            throws VersionDiffTooLargeException {
        synchronized (this) {
            LinkedList<VcsMessage<T>> vcsMessageList = vcsDataRouteMap.computeIfAbsent(routePath, k -> new LinkedList<>());
            Integer serverEndVersion = versionNumberMap.getOrDefault(routePath, 1);

            if (clientHoldServerVersion == null) {
                return new ArrayList<>(0);
            }
            if (clientHoldServerVersion.equals(serverEndVersion)) {
                return null;
            }
            if (vcsMessageList.isEmpty()) {
                return null;
            }

            int serverStartVersion = vcsMessageList.getFirst().getVersion();
            if (serverStartVersion > clientHoldServerVersion || clientHoldServerVersion > serverEndVersion) {
                log.error("vcs data manager, version diff too large. client.server.version = {}, server.head.version = {}, server.end.version = {}"
                        , clientHoldServerVersion, serverStartVersion, serverEndVersion);
                throw new VersionDiffTooLargeException();
            }

            return vcsMessageList.stream()
                    .filter(vcsMessage -> vcsMessage.getVersion() >= clientHoldServerVersion)
                    .collect(Collectors.toList());
        }
    }

}
