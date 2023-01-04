package com.neko233.common.base;


import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertiesUtils {

    private PropertiesUtils() {
    }

    public static Properties loadProperties(String justFileName) {
        if (StringUtils.isBlank(justFileName)) {
            return new Properties();
        }
        if (justFileName.startsWith("/")) {
            return loadProperties(justFileName, true);
        }
        return loadProperties("/" + justFileName, true);
    }

    public static Properties loadProperties(String fileOrFullPathName, boolean isFromClassPath) {
        Properties properties = new Properties();
        if (fileOrFullPathName == null) {
            return properties;
        }

        InputStream is = null;
        if (isFromClassPath) {
            try {
                is = PropertiesUtils.class.getResourceAsStream(fileOrFullPathName);
            } catch (Exception e) {
                log.warn("Can not load resource from classpath =  " + fileOrFullPathName, e);
            }
        } else {
            try {
                log.debug("Trying to load " + fileOrFullPathName + " from FileSystem.");
                is = new FileInputStream(fileOrFullPathName);
            } catch (FileNotFoundException e) {
                log.debug("Trying to load " + fileOrFullPathName + " from Classpath.");
            }
        }


        if (is == null) {
            log.warn("File " + fileOrFullPathName + " can't be loaded!");
        }

        try {
            properties.load(is);
        } catch (Exception e) {
            log.error("Exception occurred while loading " + fileOrFullPathName, e);
        } finally {
            CloseableUtils.autoClose(is);
        }

        return properties;
    }

}