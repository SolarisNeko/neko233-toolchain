package com.neko233.toolchain.wal;

import com.alibaba.fastjson2.JSON;
import com.neko233.toolchain.common.file.FileUtils233;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author SolarisNeko
 * Date on 2023-01-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class WalMetadata {

    public static final long BEGIN_NUMBER = 1;
    public static final long MAX_SEQUENCE_ID = 50;

    private Long currentSequenceId; // 当前指向的 sequenceId 是未刷盘的
    private String fileTemplate;
    private File currentWalFile;
    private long previousFlushMs;

    public static WalMetadata createFirst(String fileTemplate) {
        return WalMetadata.builder()
                .currentSequenceId(BEGIN_NUMBER)
                .fileTemplate(fileTemplate)
                .currentWalFile(new File(fileTemplate + "." + BEGIN_NUMBER))
                .build();
    }

    public Long getCurrentSequenceId() {
        long historySequenceId = currentSequenceId;
        // auto fix | if MAX_SEQUENCE_ID happen change
        this.currentSequenceId = currentSequenceId > MAX_SEQUENCE_ID ? MAX_SEQUENCE_ID : currentSequenceId;
        this.currentWalFile = newWalFile();
        return historySequenceId;
    }

    /**
     * 当达到某种条件后, 进行迭代 metadata
     *
     * @return 更新后的 metadata
     * @throws IOException IO 异常
     */
    public WalMetadata nextSequence() throws IOException {
        this.currentSequenceId += 1;
        // loop
        if (this.currentSequenceId > getMaxSequenceId()) {
            this.currentSequenceId = 1L;
        }
        this.currentWalFile = new File(buildDataFileName(this.currentSequenceId));
        FileUtils233.createFileIfNotExists(this.currentWalFile);
        return this;
    }

    public static long getMaxSequenceId() {
        return MAX_SEQUENCE_ID;
    }

    @NotNull
    public String buildDataFileName(long sequenceId) {
        return fileTemplate + "." + sequenceId;
    }


    /**
     * WAL core
     *
     * @param dataFile dataFile
     */
    public void writeAheadLogToFile(final File dataFile) {
        if (dataFile == null) {
            log.error("why your WAL data file is null ? please check.");
            return;
        }
        String json = JSON.toJSONString(this);
        try {
            FileUtils233.write(dataFile, json, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            log.error("write to metadata file error! file = {}, data = {}", dataFile, json, e);
        }
    }

    public File newWalFile() {
        return new File(fileTemplate + "." + this.currentSequenceId);
    }

    public boolean isNeedRollingByBytes(long fileRollingByteSize) {
        return currentWalFile.length() >= fileRollingByteSize;
    }

    public boolean isNeedRollingByTimeMsAndHaveData(long keepWalFileMs) {
        return (System.currentTimeMillis() - previousFlushMs) > keepWalFileMs;
    }


    public void updateCurrentWalFile(File walFile) throws IOException {
        this.currentWalFile = walFile;
        FileUtils233.createFileIfNotExists(walFile);
    }
}
