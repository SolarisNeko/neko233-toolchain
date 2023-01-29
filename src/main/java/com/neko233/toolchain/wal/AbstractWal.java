package com.neko233.toolchain.wal;

import com.alibaba.fastjson2.JSON;
import com.neko233.toolchain.common.annotation.Experimental;
import com.neko233.toolchain.common.base.*;
import com.neko233.toolchain.common.file.FileUtils233;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * WAL = Write Ahead Log
 * write data to WAL, used WAL to implementation a Log record. then use Consumer to handle data flush disk.
 * 写入 WAL 之前, 需要确保你的数据经过 validate and must right, 并保证写入的顺序性.
 * 1. WAL 提供一个高速持久化能力, 同时将刷盘从 single write -> batch write
 * 2. 提供了异步顺写的机制 see {@link Consumer<Collection> asyncFlushDataConsumer}
 *
 * @author SolarisNeko
 * Date on 2023-01-28
 */
@Experimental(comment = "Async WAL and default 100ms/flush by your provider consumer API")
@Slf4j
public abstract class AbstractWal<D> {

    // ASA batch size
    public static final int MAX_DRAIN_ELEMENTS_SIZE = 4000;
    // flush data ThreadPool
    private final ThreadPoolExecutor flushDataThreadPool;

    public static final String WAL_METADATA_FILE_SUFFIX = ".wal.metadata";

    /**
     * use this template for : /path/to/file -> /path/to/file.${millisSeconds}
     */
    private final AtomicBoolean isClose = new AtomicBoolean(false);
    private final Class<D> dataSchema;
    private final String fileTemplate;
    private final File currentMetadataFile;
    private final String fileTemplateName;
    private volatile WalMetadata metadata;

    // async
    private Thread walThread;

    // retention policy
    private final long fileRollingByteSize; // Number of bytes before we roll the file.
    private final long keepWalFileMs; // Number of bytes before we roll the file.
    public static final long DEFAULT_KEEP_WAL_FILE_MS_1S = TimeUnit.SECONDS.toMillis(1);
    private final int keepWalFileCount; // 保留多少个 WAL 文件
    public static final int DEFAULT_MAX_WAL_FILE_COUNT_10 = 10;
    // thread
    private final int workerIntervalMs; // How often in ms the background worker runs

    // lock
    private final Lock lock = new ReentrantLock();
    // data
    private final BlockingQueue<D> dataQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<D> walFileDataQueue = new LinkedBlockingQueue<>();

    /**
     * @param templateFilePath    模板文件 full path, 不需要存在, 只是作为前缀
     * @param dataSchema          数据结构
     * @param fileRollingByteSize 文件大小滚动
     * @param keepWalFileMs       日志保存毫秒数
     * @param keepWalFileCount    最大 WAL 数据文件数量
     * @param workerIntervalMs    线程间隔
     * @throws Exception 参数检查异常
     */
    public AbstractWal(final File templateFilePath,
                       final Class<D> dataSchema,
                       final Long fileRollingByteSize,
                       final Long keepWalFileMs,
                       final Integer keepWalFileCount,
                       final Integer workerIntervalMs
    ) throws Exception {
        if (templateFilePath == null) {
            throw new IllegalArgumentException("file can not null");
        }
        if (dataSchema == null) {
            throw new IllegalArgumentException("WAL Data class can not null");
        }

        this.dataSchema = dataSchema;
        this.fileTemplate = templateFilePath.getAbsolutePath();
        this.fileTemplateName = templateFilePath.getName();

        // metadata
        this.currentMetadataFile = new File(templateFilePath.getAbsolutePath() + WAL_METADATA_FILE_SUFFIX);

        // record
        this.fileRollingByteSize = ObjectUtils233.getOrDefault(fileRollingByteSize, 1024L);
        this.keepWalFileMs = ObjectUtils233.getOrDefault(keepWalFileMs, DEFAULT_KEEP_WAL_FILE_MS_1S);
        this.keepWalFileCount = ObjectUtils233.getOrDefault(keepWalFileCount, DEFAULT_MAX_WAL_FILE_COUNT_10);
        this.workerIntervalMs = ObjectUtils233.getOrDefault(workerIntervalMs, 100);
        // tp
        this.flushDataThreadPool = ThreadPoolHelper233.ioThreadPool("WAL-" + templateFilePath.getName(), new ThreadPoolExecutor.CallerRunsPolicy());

        initAndCheck();
    }

    void initAndCheck() throws IOException {

        boolean isNewCreate = FileUtils233.createFileIfNotExists(this.currentMetadataFile);
        if (isNewCreate) {
            metadata = WalMetadata.createFirst(this.fileTemplate);
        }
        final String content = FileUtils233.readAllContent(this.currentMetadataFile);
        boolean isFirstInit = false;
        if (StringUtils233.isBlank(content)) {
            this.metadata = WalMetadata.createFirst(this.fileTemplate);
            isFirstInit = true;
        }

        if (!isFirstInit) {
            this.metadata = JSON.parseObject(content, WalMetadata.class);
        }
        PreconditionUtils233.checkNotNull(this.metadata, "your metadata is null!");
        flushMetaData();


        // ------------- init finish --------------

        // check previous file
        restoreHistoryMemoryData();


        // WAL thread
        this.walThread = createWalThread();
        walThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            close();
        }));
    }

    private List<File> getCurrentDirFiles() throws IllegalAccessException {
        File dir = new File(fileTemplate).getParentFile();
        try {
            return FileUtils233.showFiles(dir);
        } catch (IOException e) {
            throw new IllegalAccessException("can not visit file/dir = " + dir.getAbsolutePath());
        }
    }

    /**
     * 恢复历史未刷盘的内存数据
     *
     * @throws IOException IO 异常
     */
    private void restoreHistoryMemoryData() throws IOException {
        // recover data
        final String historyWalFileAbsPath = this.metadata.buildDataFileName(this.metadata.getCurrentSequenceId());
        final File historyWalFile = new File(historyWalFileAbsPath);
        this.metadata.updateCurrentWalFile(historyWalFile);

        final List<String> jsonLines = FileUtils233.readLines(historyWalFile, StandardCharsets.UTF_8);
        final List<WalDumpData> collect = Optional.of(jsonLines).orElse(new ArrayList<>())
                .stream()
                .map(str -> JSON.parseObject(str, WalDumpData.class))
                .collect(Collectors.toList());

        for (WalDumpData obj : collect) {
            String data = obj.getDataJson();
            D d = JSON.parseObject(data, dataSchema);
            if (d == null) {
                log.error("parse JSON object to class = {} error. json = {}", dataSchema, data);
                continue;
            }

            // produce
            addDataToMemory(d);
        }
    }


    /**
     * WAL Thread
     *
     * @return WAL 线程, not start
     */
    @NotNull
    private Thread createWalThread() {
        return ThreadUtils233.newThread("wal-thread-" + fileTemplateName,
                () -> {
                    final AbstractWal<D> wal = this;
                    // never error
                    while (true) {
                        // core
                        lock.lock();
                        try {
                            writeDataToWalFile();

                            flushWalDataToConsumer();

                            wal.cleanHistoryDataFile();

                            wal.flushMetaData();
                        } catch (Throwable e) {
                            log.error("WAL flush happen error. please check!", e);
                        } finally {
                            lock.unlock();
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(workerIntervalMs);
                        } catch (Throwable e) {
                            log.error("WAL async thread happen interrupted! but you can't interrupted it!", e);
                        }
                    }
                });
    }

    private void writeDataToWalFile() {
        final List<D> dataList = new ArrayList<>();
        walFileDataQueue.drainTo(dataList, MAX_DRAIN_ELEMENTS_SIZE);

        // wal
        final File currentWalFile = this.metadata.getCurrentWalFile();
        if (currentWalFile == null) {
            log.error("please check your WAL data file why null.");
            return;
        }
        final List<WalDumpData> bufferDumpData = dataList.stream()
                .map(JSON::toJSONString)
                .map(dataJson -> WalDumpData.builder()
                        .sequenceId(this.metadata.getCurrentSequenceId())
                        .timestampMs(System.currentTimeMillis())
                        .dataJson(dataJson)
                        .build())
                .collect(Collectors.toList());
        try {
            final List<String> dumpJsonList = bufferDumpData.stream()
                    .map(JSON::toJSONString)
                    .collect(Collectors.toList());
            FileUtils233.writeLines(currentWalFile, dumpJsonList, true);
        } catch (IOException e) {
            log.error("WAL happen error");
        }
    }

    /**
     * flush Data
     */
    private void flushWalDataToConsumer() {
        // double lock for concurrent
        lock.lock();
        try {
            if (!isNeedRollingNextWalFile()) {
                return;
            }

            this.flushDataToConsumer();

            this.nextSequenceAndUpdate();

        } catch (Throwable e) {
            log.error("WAL flush happen error. please check!", e);
        } finally {
            lock.unlock();
        }


    }

    /**
     * 清理历史数据
     */
    private synchronized void cleanHistoryDataFile() {
        // just keep current file template sequence id in range File
        final Long currentSeqId = this.metadata.getCurrentSequenceId();
        // to keep files
        final List<String> keepSeqFileAbsolutePath = new ArrayList<>();
        for (long i = 0; i < keepWalFileCount; i++) {
            // for loop | + 1 is because zero not use
            long keepSeqId = currentSeqId - i < 0 ? WalMetadata.getMaxSequenceId() + currentSeqId - i + 1 : currentSeqId - i;
            final String dataFileName = this.metadata.buildDataFileName(keepSeqId);
            keepSeqFileAbsolutePath.add(dataFileName);
        }
        final List<File> currentDirFiles = new ArrayList<>();
        try {
            currentDirFiles.addAll(getCurrentDirFiles());
        } catch (IllegalAccessException e) {
            log.error("get all file from dir error. file dir = {} ", fileTemplate, e);
        }

        final List<File> targetFiles = currentDirFiles.stream()
                .filter(file -> {
                    if (file.getName().contains(WAL_METADATA_FILE_SUFFIX)) {
                        return false;
                    }
                    return file.getAbsolutePath().contains(fileTemplate);
                })
                .collect(Collectors.toList());
        for (File currentDirFile : targetFiles) {
            String absolutePath = currentDirFile.getAbsolutePath();
            if (keepSeqFileAbsolutePath.contains(absolutePath)) {
                continue;
            }

            deleteHistoryWalFile(currentDirFile);
        }

    }


    /**
     * 删除历史 WAL 文件
     *
     * @param toDeleteFile
     */
    private void deleteHistoryWalFile(File toDeleteFile) {
        FileUtils233.deleteQuietly(toDeleteFile);
    }

    private void flushMetaData() {
        this.metadata.writeAheadLogToFile(this.currentMetadataFile);
    }

    /**
     * it will clear all memory Data in Queue, then trigger Consumer
     */
    public void flushDataToConsumer() {
        // async callback
        this.flushDataThreadPool.execute(() -> {
            lock.lock();
            try {
                if (dataQueue.isEmpty()) {
                    return;
                }

                final List<D> objects = new ArrayList<>();
                dataQueue.drainTo(objects, MAX_DRAIN_ELEMENTS_SIZE);

                if (CollectionUtils233.isEmpty(objects)) {
                    return;
                }
                // data
                List<D> toConsumeDataList = Collections.unmodifiableList(objects);


                try {
                    flush(toConsumeDataList);
                } catch (Exception e) {
                    log.error("flush data error. please check! json = {} ", JSON.toJSONString(toConsumeDataList), e);
                }

            } finally {
                lock.unlock();
            }
        });
    }


    /**
     * 外部获取 buffer 中的数据. 相当于 Linux Dirty Page Data
     *
     * @return 内存未刷盘数据
     */
    public List<D> getMemoryData() {
        return Collections.unmodifiableList(new ArrayList<>(dataQueue));
    }


    public void close() {
        isClose.compareAndSet(false, true);
        long doubleMs = workerIntervalMs * 2L;
        log.info("WAL close waiting ms = {}", doubleMs);
        try {
            TimeUnit.MILLISECONDS.sleep(doubleMs);
        } catch (Throwable e) {
            log.error("WAL async thread happen interrupted! but you can't interrupted it!", e);
        }
        log.info("WAL close done");
    }


    public long getCurrentSequenceId() {
        lock.lock();
        try {
            return this.metadata.getCurrentSequenceId();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新 metadata
     *
     * @return next sequence Id
     */
    private long nextSequenceAndUpdate() {
        // not modify when close
        Long notChangeSeqId = this.metadata.getCurrentSequenceId();
        if (isClose.get()) {
            return notChangeSeqId;
        }

        // if
        try {
            List<String> lines = FileUtils233.readLines(this.metadata.getCurrentWalFile(), StandardCharsets.UTF_8);
            if (CollectionUtils233.isEmpty(lines)) {
                return notChangeSeqId;
            }
        } catch (IOException e) {
            log.error("read WAL file error!", e);
            return notChangeSeqId;
        }

        // update to next
        try {
            return this.metadata.nextSequence().getCurrentSequenceId();
        } catch (IOException e) {
            log.error("next Sequence Id and data file error", e);
            return notChangeSeqId;
        }

    }

    /**
     * 写入数据
     *
     * @param data 数据
     * @throws IllegalAccessException 非法访问异常 (系统结束)
     */
    public boolean inputData(D data) throws IllegalAccessException {
        if (isClose.get()) {
            throw new IllegalAccessException("System is going to close, you can't write data to WAL");
        }
        if (data == null) {
            return false;
        }

        // produce
        addDataToMemory(data);

        return true;
    }

    private void addDataToMemory(D data) {
        dataQueue.add(data);
        walFileDataQueue.add(data);
    }

    /**
     * 是否需要滚动
     *
     * @return true = rolling
     */
    private boolean isNeedRollingNextWalFile() {
        // time
        boolean isNeedRollingByTimeMs = this.metadata.isNeedRollingByTimeMsAndHaveData(keepWalFileMs);
        if (isNeedRollingByTimeMs) {
            return true;
        }
        // byte size
        boolean isRollingByByteSize = this.metadata.isNeedRollingByBytes(fileRollingByteSize);
        if (isRollingByByteSize) {
            return true;
        }

        return false;
    }

    // -------------------- API -----------------------


    public abstract void flush(List<D> toConsumeDataList) throws Exception;


    // -------------------- /API -----------------------

}