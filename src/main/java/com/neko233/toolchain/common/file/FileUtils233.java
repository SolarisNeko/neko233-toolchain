package com.neko233.toolchain.common.file;


import com.neko233.toolchain.common.base.CollectionUtils233;
import com.neko233.toolchain.common.base.DataSizeUtils233;
import com.neko233.toolchain.common.file.visitor.FileVisitorForAccumulate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
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
     * ?????? properties file
     *
     * @param propsFilename ?????????
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
     * ClassLoader ?????? properties ??????
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


    public static List<File> showFiles(final File fileOrDir) throws IOException {
        return showFiles(fileOrDir, false, null);
    }

    public static List<File> showFiles(final File fileOrDir, final boolean isRecursive) throws IOException {
        return showFiles(fileOrDir, isRecursive, null);
    }

    /**
     * ????????????
     *
     * @param fileOrDir   ??????/??????
     * @param isRecursive ????????????
     * @param suffixes    ????????????. example = ".txt"
     * @return ????????? (????????? dir)
     * @throws IOException
     */
    public static List<File> showFiles(final File fileOrDir, final boolean isRecursive, final List<String> suffixes)
            throws IOException {
        final List<String> suffixList = Optional.ofNullable(suffixes).orElse(new ArrayList<>());

        if (fileOrDir == null) {
            return new ArrayList<>();
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
                return new ArrayList<>();
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
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
        File[] childs = f.listFiles();
        StringBuilder fileTreeBuilder = new StringBuilder();
        for (int i = 0; i < childs.length; i++) {
            // ????????????
            for (int j = 0; j < level; j++) {
                if (j == 0) {
                    fileTreeBuilder.append("|???");
                } else {
                    fileTreeBuilder.append("???");
                }
            }

            if (childs[i].isDirectory()) { //
                fileTreeBuilder.append(childs[i].getName());// ????????????????????????
                fileTreeBuilder.append(System.lineSeparator());

                String subFileTree = getFileTreeString(childs[i], level + 1);
                fileTreeBuilder.append(subFileTree);
            } else {
                // ???????????????????????????????????????
                fileTreeBuilder.append(childs[i].getName()).append("\t\t\t\t\t")
                        // size
                        .append(DataSizeUtils233.toHumanFormatByByte(childs[i].length()));
                fileTreeBuilder.append(System.lineSeparator());
            }

        }
        return fileTreeBuilder.toString();
    }


    /**
     * ????????????????????? Tree -> List
     *
     * @param startDir ????????????????????????
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


    /**
     * ???????????? if ?????????
     *
     * @param newFile ?????????
     */
    public static boolean createFileIfNotExists(File newFile) throws IOException {
        if (newFile.exists()) {
            return false;
        }
        newFile.createNewFile();
        return true;
    }

    public static String readAllContent(File file) throws IOException {
        return readAllContent(file, StandardCharsets.UTF_8);
    }

    public static String readAllContent(File file, Charset charset) throws IOException {
        if (file == null || !file.exists()) {
            return null;
        }
        return readLines(file, charset)
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));
    }


    public static void write(final File file, final CharSequence data) throws IOException {
        write(file, data, StandardCharsets.UTF_8);
    }

    /**
     * default Append !
     *
     * @param file    ??????
     * @param data    ??????
     * @param charset ??????
     * @throws IOException
     */
    public static void write(final File file, final CharSequence data, final Charset charset) throws IOException {
        write(file, data, charset, true);
    }

    /**
     * Writes a CharSequence to a file creating the file if it does not exist.
     *
     * @param file    the file to write
     * @param data    the content to write to the file
     * @param charset the charset to use, {@code null} means platform default
     * @param append  if {@code true}, then the data will be added to the
     *                end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     */
    public static void write(final File file, final CharSequence data, final Charset charset, final boolean append)
            throws IOException {
        writeStringToFile(file, Objects.toString(data, null), charset, append);
    }


    /**
     * ?????????????????????
     *
     * @param file
     * @param data
     * @param charset
     * @param append
     * @throws IOException
     */
    public static void writeStringToFile(final File file, final String data, final Charset charset,
                                         final boolean append) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            IoUtils233.write(data, out, charset);
        }
    }

    public static FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
        Objects.requireNonNull(file, "file");
        if (file.exists()) {
            requireFile(file, "file");
            requireCanWrite(file, "file");
        } else {
            createParentDirectories(file);
        }
        return new FileOutputStream(file, append);
    }

    /**
     * Requires that the given {@code File} is a file.
     *
     * @param file The {@code File} to check.
     * @param name The parameter name to use in the exception message.
     * @return the given file.
     * @throws NullPointerException     if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static File requireFile(final File file, final String name) {
        Objects.requireNonNull(file, name);
        if (!file.isFile()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
        }
        return file;
    }

    /**
     * ?????????????????????
     *
     * @param file The file to test.
     * @param name The parameter name to use in the exception message.
     * @throws NullPointerException     if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the file is not writable.
     */
    private static void requireCanWrite(final File file, final String name) {
        Objects.requireNonNull(file, "file");
        if (!file.canWrite()) {
            throw new IllegalArgumentException("File parameter '" + name + " is not writable: '" + file + "'");
        }
    }

    /**
     * Creates all parent directories for a File object.
     *
     * @param file the File that may need parents, may be null.
     * @return The parent directory, or {@code null} if the given file does not name a parent
     * @throws IOException if the directory was not created along with all its parent directories.
     * @throws IOException if the given file object is not null and not a directory.
     * @since 2.9.0
     */
    public static File createParentDirectories(final File file) throws IOException {
        return createAllParentDir(getParentFile(file));
    }

    private static File getParentFile(final File file) {
        return file == null ? null : file.getParentFile();
    }

    /**
     * Calls {@link File#mkdirs()} and throws an exception on failure.
     *
     * @param directory the receiver for {@code mkdirs()}, may be null.
     * @return the given file, may be null.
     * @throws IOException       if the directory was not created along with all its parent directories.
     * @throws IOException       if the given file object is not a directory.
     * @throws SecurityException See {@link File#mkdirs()}.
     * @see File#mkdirs()
     */
    private static File createAllParentDir(final File directory) throws IOException {
        if ((directory != null)
                && (!directory.mkdirs()
                && !directory.isDirectory())) {
            throw new IOException("Cannot create directory '" + directory + "'.");
        }
        return directory;
    }


    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The default VM encoding and the default line ending will be used.
     *
     * @param file  the file to write to
     * @param lines the lines to write, {@code null} entries produce blank lines
     * @throws IOException in case of an I/O error
     * @since 1.3
     */
    public static void writeLines(final File file, final Collection<?> lines) throws IOException {
        writeLines(file, null, lines, null, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The default VM encoding and the default line ending will be used.
     *
     * @param file   the file to write to
     * @param lines  the lines to write, {@code null} entries produce blank lines
     * @param append if {@code true}, then the lines will be added to the
     *               end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeLines(final File file, final Collection<?> lines, final boolean append) throws IOException {
        writeLines(file, null, lines, null, append);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The default VM encoding and the specified line ending will be used.
     *
     * @param file       the file to write to
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @throws IOException in case of an I/O error
     * @since 1.3
     */
    public static void writeLines(final File file, final Collection<?> lines, final String lineEnding)
            throws IOException {
        writeLines(file, null, lines, lineEnding, false);
    }


    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The default VM encoding and the specified line ending will be used.
     *
     * @param file       the file to write to
     * @param lines      the lines to write, {@code null} entries produce blank lines
     * @param lineEnding the line separator to use, {@code null} is system default
     * @param append     if {@code true}, then the lines will be added to the
     *                   end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeLines(final File file, final Collection<?> lines, final String lineEnding,
                                  final boolean append) throws IOException {
        writeLines(file, null, lines, lineEnding, append);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The specified character encoding and the default line ending will be used.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file        the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines       the lines to write, {@code null} entries produce blank lines
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 1.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines)
            throws IOException {
        writeLines(file, charsetName, lines, null, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line, optionally appending.
     * The specified character encoding and the default line ending will be used.
     *
     * @param file        the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines       the lines to write, {@code null} entries produce blank lines
     * @param append      if {@code true}, then the lines will be added to the
     *                    end of the file rather than overwriting
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines,
                                  final boolean append) throws IOException {
        writeLines(file, charsetName, lines, null, append);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The specified character encoding and the line ending will be used.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     * </p>
     *
     * @param file        the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines       the lines to write, {@code null} entries produce blank lines
     * @param lineEnding  the line separator to use, {@code null} is system default
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 1.1
     */
    public static void writeLines(final File file, final String charsetName, final Collection<?> lines,
                                  final String lineEnding) throws IOException {
        writeLines(file, charsetName, lines, lineEnding, false);
    }

    /**
     * Writes the {@code toString()} value of each item in a collection to
     * the specified {@code File} line by line.
     * The specified character encoding and the line ending will be used.
     *
     * @param file        the file to write to
     * @param charsetName the name of the requested charset, {@code null} means platform default
     * @param lines       the lines to write, {@code null} entries produce blank lines
     * @param lineEnding  the line separator to use, {@code null} is system default
     * @param append      if {@code true}, then the lines will be added to the
     *                    end of the file rather than overwriting
     * @throws IOException                          in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     */
    public static void writeLines(final File file,
                                  final String charsetName,
                                  final Collection<?> lines,
                                  final String lineEnding,
                                  final boolean append
    ) throws IOException {
        try (OutputStream out = new BufferedOutputStream(openOutputStream(file, append))) {
            IOUtils.writeLines(lines, lineEnding, out, charsetName);
        }
    }

    public static boolean deleteQuietly(final File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (final Exception ignored) {
            // ignore
        }

        try {
            return file.delete();
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static void cleanDirectory(final File directory) throws IOException {
        List<File> files = showFiles(directory, true);

        final List<Exception> causeList = new ArrayList<>();
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                causeList.add(ioe);
            }
        }

        if (!causeList.isEmpty()) {
            throw new IOExceptionList(directory.toString(), causeList);
        }
    }

    public static void forceDelete(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        final Counters.PathCounters deleteCounters;
        try {
            deleteCounters = PathUtils.delete(file.toPath(), PathUtils.EMPTY_LINK_OPTION_ARRAY,
                    StandardDeleteOption.OVERRIDE_READ_ONLY);
        } catch (final IOException e) {
            throw new IOException("Cannot delete file: " + file, e);
        }

        if (deleteCounters.getFileCounter().get() < 1 && deleteCounters.getDirectoryCounter().get() < 1) {
            // didn't find a file to delete.
            throw new FileNotFoundException("File does not exist: " + file);
        }
    }

    public static void iterateLines(File file, Consumer<String> lineConsumer) throws IOException {
        //?????????????????????????????????????????????????????????utf-8
        InputStreamReader read = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(read);//???????????????????????????
        String line = null;

        while ((line = in.readLine()) != null) {
            lineConsumer.accept(line);
        }
        in.close();
    }

    /**
     * ?????????????????? = not exists / blank
     * @param fileAbsolutePath ?????????????????????
     * @return isBlank
     */
    public static boolean isFileBlank(String fileAbsolutePath) {
        File file = new File(fileAbsolutePath);
        if (!file.exists()) {
            return true;
        }
        if (file.length() == 0) {
            return true;
        }
        return false;
    }
}
