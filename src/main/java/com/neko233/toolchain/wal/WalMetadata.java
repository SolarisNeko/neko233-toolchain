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

    private volatile Long currentSequenceId; // 当前指向的 sequenceId 是未刷盘的
    private volatile File currentDataFile;
    private volatile String fileTemplate;

    public static WalMetadata createFirst(String fileTemplate) {
        return WalMetadata.builder()
                .currentSequenceId(BEGIN_NUMBER)
                .fileTemplate(fileTemplate)
                .currentDataFile(new File(fileTemplate + "." + BEGIN_NUMBER))
                .build();
    }

    /**
     * 当达到某种条件后, 进行迭代 metadata
     *
     * @return 更新后的 metadata
     * @throws IOException IO 异常
     */
    public WalMetadata next() throws IOException {
        this.currentSequenceId += 1;
        // loop
        if (this.currentSequenceId == Long.MAX_VALUE) {
            this.currentSequenceId = 1L;
        }
        this.currentDataFile = new File(buildDataFileName(this.currentSequenceId));
        FileUtils233.createFileIfNotExists(this.currentDataFile);
        return this;
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
    public void writeAheadLogToFile(File dataFile) {
        String json = JSON.toJSONString(this);
        try {
            FileUtils233.write(dataFile, json, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            log.error("write to metadata file error! file = {}, data = {}", dataFile, json, e);
        }
    }

    public File newSeqFile() {
        return new File(fileTemplate + "." + this.getCurrentSequenceId());
    }
}
