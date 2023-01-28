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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Write Ahead Log
 *
 * @author SolarisNeko
 * Date on 2023-01-28
 */
@Slf4j
@Experimental(comment = "just a experimental function")
public class WAL<D> {

    // flush data ThreadPool
    private final ThreadPoolExecutor flushDataThreadPool;

    public static final String WAL_METADATA_FILE_SUFFIX = ".wal.metadata";

    /**
     * use this template for : /path/to/file -> /path/to/file.${millisSeconds}
     */
    private final AtomicBoolean isClose = new AtomicBoolean(false);
    private final Class<D> dataClass;
    private final String fileTemplate;
    private final File currentMetadataFile;
    private final String fileTemplateName;
    private final List<File> historyWalFiles = new ArrayList<>();
    private volatile WalMetadata metadata;
    private Thread thread;
    private final Consumer<Collection<D>> flushDataConsumer;

    // retention policy
    private final long fileRollingByteSize; // Number of bytes before we roll the file.
    private final int maxWalFileCount; // Number of bytes, to keep before we start pruning logs.
    public static final int DEFAULT_MAX_WAL_FILE_COUNT = 10;
    // thread
    private final int workerIntervalMs; // How often in ms the background worker runs

    // lock
    private final Lock lock = new ReentrantLock();
    // data
    private BlockingQueue<D> dataQueue = new LinkedBlockingQueue<>();

    /**
     * @param templateFile        模板文件, 可不存在的
     * @param dataClass           数据结构
     * @param flushDataConsumer   刷盘消费者
     * @param fileRollingByteSize 文件大小滚动
     * @param logSaveMs           日志保存毫秒数
     * @param maxWalFileCount     最大 WAL 数据文件数量
     * @param workerIntervalMs    线程间隔
     * @throws Exception 参数检查异常
     */
    public WAL(final File templateFile,
               final Class<D> dataClass,
               final Consumer<Collection<D>> flushDataConsumer,
               final Long fileRollingByteSize,
               final Long logSaveMs,
               final Integer maxWalFileCount,
               final Integer workerIntervalMs
    ) throws Exception {
        if (templateFile == null) {
            throw new IllegalArgumentException("file can not null");
        }
        if (dataClass == null) {
            throw new IllegalArgumentException("WAL Data class can not null");
        }

        this.dataClass = dataClass;
        this.fileTemplate = templateFile.getAbsolutePath();
        this.fileTemplateName = templateFile.getName();
        this.flushDataConsumer = flushDataConsumer;

        // metadata
        this.currentMetadataFile = new File(templateFile.getAbsolutePath() + WAL_METADATA_FILE_SUFFIX);

        // record
        this.fileRollingByteSize = ObjectUtils233.getOrDefault(fileRollingByteSize, 1024L);
        this.maxWalFileCount = ObjectUtils233.getOrDefault(maxWalFileCount, DEFAULT_MAX_WAL_FILE_COUNT);
        this.workerIntervalMs = ObjectUtils233.getOrDefault(workerIntervalMs, 100);
        // tp
        this.flushDataThreadPool = ThreadPoolHelper233.ioThreadPool(templateFile.getName(), new ThreadPoolExecutor.CallerRunsPolicy());

        initAndCheck();
    }

    void initAndCheck() throws IllegalAccessException, IOException {
        if (flushDataConsumer == null) {
            throw new IllegalArgumentException("data consumer is null!");
        }

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


        File dir = new File(fileTemplate).getParentFile();
        try {
            final List<File> historyFiles = FileUtils233.showFiles(dir);

            this.historyWalFiles.addAll(historyFiles);
        } catch (IOException e) {
            throw new IllegalAccessException("can not visit file/dir = " + dir.getAbsolutePath());
        }

        final List<String> toHandleHistoryFileNames = this.getHistoryDataFileSimpleName();
        final Map<Integer, String> toHandlerNumber = this.getHistoryDataFileNumberMap(toHandleHistoryFileNames);

        int maxHistorySequenceId = 0;
        for (Map.Entry<Integer, String> entry : toHandlerNumber.entrySet()) {
            Integer number = entry.getKey();
            if (number > maxHistorySequenceId) {
                maxHistorySequenceId = number;
            }
        }

        // init finish

        // check need
        restoreHistoryMemoryData(maxHistorySequenceId);


        // thread
        this.thread = createWalWorker();
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            isClose.compareAndSet(false, true);
            long doubleMs = workerIntervalMs * 2L;
            log.info("WAL close waiting ms = {}", doubleMs);
            try {
                TimeUnit.MILLISECONDS.sleep(doubleMs);
            } catch (Throwable e) {
                log.error("WAL async thread happen interrupted! but you can't interrupted it!", e);
            }
            log.info("WAL close done");
        }));
    }

    /**
     * 恢复历史未刷盘的内存数据
     *
     * @param maxHistorySequenceId 最大历史序列 ID
     * @throws IOException IO 异常
     */
    private void restoreHistoryMemoryData(int maxHistorySequenceId) throws IOException {
        long subtract = maxHistorySequenceId - this.metadata.getCurrentSequenceId();
        // 0 = current not flush!
        if (subtract >= 0) {
            // happen discard data
            for (long i = 0; i < (subtract + 1); i++) {
                String historyDataFile = this.metadata
                        .buildDataFileName(this.metadata.getCurrentSequenceId() + i);
                File historyFile = new File(historyDataFile);
                List<String> jsonLines = FileUtils233.readLines(historyFile, StandardCharsets.UTF_8);
                final List<WalDumpData> collect = Optional.of(jsonLines).orElse(new ArrayList<>())
                        .stream()
                        .map(str -> JSON.parseObject(str, WalDumpData.class))
                        .collect(Collectors.toList());

                for (WalDumpData obj : collect) {
                    String data = obj.getDataJson();
                    D d = JSON.parseObject(data, dataClass);
                    if (d == null) {
                        log.error("parse JSON object to class = {} error. json = {}", dataClass, data);
                        continue;
                    }

                    // produce
                    addDataToMemoryBuffer(d);
                }
            }
        }
    }

    @NotNull
    private static Map<Integer, String> getHistoryDataFileNumberMap(List<String> toHandleHistoryFileNames) {
        final Map<Integer, String> toHandlerNumber = new HashMap<>();
        for (String fileName : toHandleHistoryFileNames) {
            String[] split = fileName.split("\\.");
            if (split.length < 2) {
                continue;
            }
            // last one
            String suffix = split[split.length - 1];
            if (StringUtils233.isNotNumber(suffix)) {
                continue;
            }
            int number = Integer.parseInt(suffix);
            toHandlerNumber.put(number, fileName);
        }
        return toHandlerNumber;
    }

    @NotNull
    private List<String> getHistoryDataFileSimpleName() {
        return this.historyWalFiles
                .stream()
                .map(File::getName)
                .filter(fileName -> fileName.contains(fileTemplateName) && fileName.contains("."))
                .collect(Collectors.toList());
    }

    @NotNull
    private Thread createWalWorker() {
        return ThreadUtils233.newThread("wal-worker-" + fileTemplateName,
                () -> {
                    final WAL<D> wal = this;
                    // never error
                    while (true) {
                        // core
                        lock.lock();
                        try {
                            if (isWalFileLteRollBytes()) {
                                wal.flushData();
                                wal.nextSequenceId();
                            } else {
                                wal.cleanHistoryDataFile();
                            }

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

    private void cleanHistoryDataFile() {

        final Map<Integer, File> numberFileMap = historyWalFiles.stream()
                .filter(file -> {
                    String fileName = file.getName();
                    String[] split = fileName.split("\\.");
                    if (split.length < 2) {
                        return false;
                    }
                    // last one
                    String suffix = split[split.length - 1];
                    if (StringUtils233.isNotNumber(suffix)) {
                        return false;
                    }
                    try {
                        int number = Integer.parseInt(suffix);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(file -> {
                    String fileName = file.getName();
                    String[] split = fileName.split("\\.");
                    return Integer.parseInt(split[split.length - 1]);
                }, Function.identity(), (v1, v2) -> v2));
        for (Map.Entry<Integer, File> entry : numberFileMap.entrySet()) {
            Integer historySeqId = entry.getKey();
            if (this.metadata.getCurrentSequenceId() - historySeqId > maxWalFileCount) {
                File toDeleteFile = entry.getValue();
                FileUtils233.deleteQuietly(toDeleteFile);
                this.historyWalFiles.remove(toDeleteFile);
            }
        }
    }

    private void flushMetaData() {
        this.metadata.writeAheadLogToFile(this.currentMetadataFile);
    }

    /**
     * it will clear all memory Data in Queue, then trigger Consumer
     */
    public void flushData() {
        final List<D> objects = new ArrayList<>();
        dataQueue.drainTo(objects);
        if (CollectionUtils233.isEmpty(objects)) {
            return;
        }
        // data
        List<D> unmodifiedList = Collections.unmodifiableList(objects);
        this.flushDataThreadPool.execute(() -> {
            this.flushDataConsumer.accept(unmodifiedList);
        });
    }

    /**
     * 外部获取 buffer 中的数据. 相当于 Linux Dirty Page Data
     *
     * @return 内存未刷盘数据
     */
    public List<D> getMemoryData() {
        return new ArrayList<>(dataQueue);
    }


    void close() {

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
     * 下一个 sequence Id & 更新 metadata
     *
     * @return next sequence Id
     */
    private long nextSequenceId() {
        if (isClose.get()) {
            // not modify
            return this.metadata.getCurrentSequenceId();
        }
        // update
        try {
            this.metadata.next();
        } catch (IOException e) {
            log.error("next Sequence Id and data file error");
        }

        this.historyWalFiles.add(this.metadata.newSeqFile());
        return this.metadata.getCurrentSequenceId();
    }


    void writeData(D data) throws IllegalAccessException {
        if (isClose.get()) {
            throw new IllegalAccessException("System is going to close, you can't write data to WAL");
        }
        if (data == null) {
            return;
        }
        // wal
        File dataFile = this.metadata.getCurrentDataFile();
        if (dataFile == null) {
            log.error("please check your WAL data file why null.");
            return;
        }
        String dataJson = JSON.toJSONString(data);
        WalDumpData build = WalDumpData.builder()
                .sequenceId(this.metadata.getCurrentSequenceId())
                .timestampMs(System.currentTimeMillis())
                .dataJson(dataJson)
                .build();
        try {
            String dumpDataJson = JSON.toJSONString(build);
            FileUtils233.writeLines(dataFile, Collections.singletonList(dumpDataJson), true);
        } catch (IOException e) {
            log.error("WAL happen error");
        }

        if (isWalFileLteRollBytes()) {
            flushData();
            nextSequenceId();
            flushMetaData();
        }

        // produce
        addDataToMemoryBuffer(data);
    }

    private void addDataToMemoryBuffer(D data) {
        dataQueue.add(data);
    }

    private boolean isWalFileLteRollBytes() {
        return this.metadata.getCurrentDataFile().length() >= fileRollingByteSize;
    }


}