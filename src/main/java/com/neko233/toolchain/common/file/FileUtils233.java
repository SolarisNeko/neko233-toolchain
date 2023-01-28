package com.neko233.toolchain.common.file;


import com.neko233.toolchain.common.base.CollectionUtils233;
import com.neko233.toolchain.common.base.DataSizeUtils233;
import com.neko233.toolchain.common.file.visitor.FileVisitorForAccumulate;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author SolarisNeko
 */
@Slf4j
public class FileUtils233 {

    public static List<String> readLines(final File file, final Charset charset) throws IOException {
        try (InputStream inputStream = openInputStream(file)) {
            return readLines(inputStream, chooseCharset(charset));
        }
    }

    public static List<String> readLines(final InputStream input, final Charset charset) throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, chooseCharset(charset));
        return readLines(reader);
    }

    public static List<String> readLines(final Reader reader) throws IOException {
        final BufferedReader bufReader = toBufferedReader(reader);
        final List<String> lineList = new ArrayList<>();
        String line;
        while ((line = bufReader.readLine()) != null) {
            lineList.add(line);
        }
        return lineList;
    }


    // ----------- base ---------------

    public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static FileInputStream openInputStream(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        return new FileInputStream(file);
    }

    private static Charset chooseCharset(Charset charset) {
        return charset == null ? Charset.defaultCharset() : charset;
    }

    /**
     * 读取 properties file
     *
     * @param propsFilename 文件名
     * @return Properties
     */
    public static Properties readPropertiesFromFilename(String propsFilename) {
        Properties archProps = new Properties();
        LinkedHashSet<ClassLoader> list = Stream.of(
                        Thread.currentThread().getContextClassLoader(),
                        ClassLoader.getSystemClassLoader(),
                        FileUtils233.class.getClassLoader()
                )
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (ClassLoader loader : list) {
            if (readPropertiesFromClassLoader(propsFilename, archProps, loader)) {
                return archProps;
            }
        }
        log.warn("Failed to load configuration file from classloader: {}", propsFilename);
        return archProps;
    }


    /**
     * ClassLoader 加载 properties 文件
     *
     * @param propsFilename
     * @param archProps
     * @param loader
     * @return
     */
    private static boolean readPropertiesFromClassLoader(String propsFilename, Properties archProps,
                                                         ClassLoader loader) {
        if (loader == null) {
            return false;
        }
        // Load the configuration file from the classLoader
        try {
            List<URL> resources = Collections.list(loader.getResources(propsFilename));
            if (resources.isEmpty()) {
                log.debug("No {} file found from ClassLoader {}", propsFilename, loader);
                return false;
            }
            if (resources.size() > 1) {
                log.warn("Configuration conflict: there is more than one {} file on the classpath: {}", propsFilename,
                        resources);
            }
            try (InputStream in = resources.get(0).openStream()) {
                if (in != null) {
                    archProps.load(in);
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 显示文件
     *
     * @param fileOrDir   文件/目录
     * @param isRecursive 是否递归
     * @return 纯文件 (不包含 dir)
     * @throws IOException
     */
    public static List<File> showFiles(final File fileOrDir) throws IOException {
        return showFiles(fileOrDir, false, null);
    }

    public static List<File> showFiles(final File fileOrDir, final boolean isRecursive) throws IOException {
        return showFiles(fileOrDir, isRecursive, null);
    }

    public static List<File> showFiles(final File fileOrDir, final boolean isRecursive, final List<String> suffixes)
            throws IOException {
        final List<String> suffixList = Optional.ofNullable(suffixes).orElse(new ArrayList<>());

        if (fileOrDir == null) {
            return null;
        }

        File dir = fileOrDir;
        if (fileOrDir.isFile()) {
            return Collections.singletonList(fileOrDir);
        }

        Path dirPath = dir.toPath();

        // single
        if (!isRecursive) {
            File[] array = dir.listFiles();
            if (array == null) {
                return null;
            }
            return Arrays.stream(array).filter(File::isFile).collect(Collectors.toList());
        }


        final EnumSet<FileVisitOption> fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

        final FileVisitorForAccumulate visitor = new FileVisitorForAccumulate();
        Files.walkFileTree(dirPath, fileVisitOptions, Integer.MAX_VALUE, visitor);
        List<File> justFileList = visitor.getJustFileList();
        return Optional.ofNullable(justFileList)
                .orElse(new ArrayList<>())
                .stream()
                .filter(file -> {
                    if (CollectionUtils233.isEmpty(suffixList)) {
                        return true;
                    }
                    // filter suffix
                    for (String fileSuffix : suffixList) {
                        return file.getName().endsWith(fileSuffix);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    private static String getFileTreeString(File f, int level) {
        // 返回一个抽象路径名数组，这些路径名表示此抽象路径名所表示目录中地文件
        File[] childs = f.listFiles();
        StringBuilder fileTreeBuilder = new StringBuilder();
        for (int i = 0; i < childs.length; i++) {
            // 打印前缀
            for (int j = 0; j < level; j++) {
                if (j == 0) {
                    fileTreeBuilder.append("|＿");
                } else {
                    fileTreeBuilder.append("＿");
                }
            }

            if (childs[i].isDirectory()) { //
                fileTreeBuilder.append(childs[i].getName());// 打印子文件地名字
                fileTreeBuilder.append(System.lineSeparator());

                String subFileTree = getFileTreeString(childs[i], level + 1);
                fileTreeBuilder.append(subFileTree);
            } else {
                // 如果是文件把大小也打印出来
                fileTreeBuilder.append(childs[i].getName()).append("\t\t\t\t\t")
                        // size
                        .append(DataSizeUtils233.toHumanFormatByByte(childs[i].length()));
                fileTreeBuilder.append(System.lineSeparator());
            }

        }
        return fileTreeBuilder.toString();
    }


    /**
     * 线性化文件树， Tree -> List
     *
     * @param startDir 需要遍历的文件夹
     * @return file tree -> all file List
     */
    public static List<File> liner(String startDir) {
        List<File> fileList = new ArrayList<>();

        File directory = new File(startDir);
        if (!directory.exists() || !directory.isDirectory()) {
            return fileList;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                // loop
                List<File> liner = liner(file.getPath());
                fileList.addAll(liner);
                continue;
            }
            if (!file.isFile()) {
                continue;
            }
            if (file.length() == 0) {
                continue;
            }
            fileList.add(file);
        }
        return fileList;
    }


}
