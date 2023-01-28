package com.neko233.toolchain.wal;

import com.neko233.toolchain.common.base.ThreadUtils233;
import com.neko233.toolchain.common.file.FileUtils233;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author SolarisNeko
 * Date on 2023-01-28
 */
@Slf4j
public class WAL {

    public static void main(String[] args) {
//        WAL wal = new WAL(path, Writable.class);
//        wal.writeEvent(event, 1);
//        wal.writeEvent(event, 2);
//        wal.writeSequenceID(1);
//        wal.writeEvent(event, 3);
//
//        System crashes or shuts down...
//
//        WAL wal = new WAL(path, Writable.class);
//  [Event 2, Event 3]  = wal.replay();
    }

    /**
     * use this template for : /path/to/file -> /path/to/file.${millisSeconds}
     */
    File fileTemplate;
    String fileNameTemplate;
    List<File> historyWalFiles = new ArrayList<>();

    long rollSize; // Number of bytes before we roll the file.
    long maxLogsSize; // Number of bytes, to keep before we start pruning logs.
    long minLogRentionPeriod; //
    long workerInterval; // How often in ms the background worker runs


    public WAL(File file,
               long rollSize,
               long maxLogsSize,
               long minLogRetentionPeriod,
               long workerIntervalMs) throws IllegalAccessException {
        if (file == null) {
            throw new IllegalArgumentException("file can't not null");
        }

        this.fileNameTemplate = file.getName();
        try {
            final List<File> historyFiles = FileUtils233.showFiles(file);

            this.historyWalFiles.addAll(historyFiles);
        } catch (IOException e) {
            throw new IllegalAccessException("can not visit file/dir = " + file.getAbsolutePath());
        }

        Thread thread = ThreadUtils233.newThread("async-wal-flush-" + file.getName(),
                () -> {
                    while (true) {
                        try {
                            // TODO do something..


                            TimeUnit.MILLISECONDS.sleep(workerIntervalMs);
                        } catch (InterruptedException e) {
                            log.error("WAL async thread happen interrupted! but you can't interrupted it!", e);
                        }
                    }
                });
        thread.start();
    }


    void close() {

    }

    void setMaxLogsSize(long maxLogsSize) {

    }

    void setMinLogRetentionPeriod(long minLogRetentionPeriod) {

    }

    void setRollSize(long rollSize) {

    }

    void setWorkerInterval(long workerInterval) {

    }


    void writeSequenceID(long sequenceID) {

    }

    void writeSequenceIDs(List<Long> sequenceIDs) {

    }

    void write(String content) {

    }


}