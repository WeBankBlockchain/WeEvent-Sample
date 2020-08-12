package com.webank.weevent.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesUtils {

    private static Properties props;
    private static final String APPLICATION_FILE = "application.properties";

    static {
        loadProps();
    }

    /**
     * load configuration file.
     */
    private static synchronized void loadProps() {
        props = new Properties();
        InputStream resourceAsStream = PropertiesUtils.class.getClassLoader()
                .getResourceAsStream(APPLICATION_FILE);
        try {
            props.load(resourceAsStream);
            log.info("loadProps finish...");
        } catch (IOException e) {
            log.error("loadProps error", e);
        }
    }

    /**
     * read the value in the configuration file according to key.
     *
     * @param key configured key
     * @return returns the value of key
     */
    public static String getProperty(String key) {
        if (null == props) {
            loadProps();
        }
        return props.getProperty(key);
    }

    /**
     * read the value in the configuration file according to key,
     * returns the default value when it is not available.
     *
     * @param key configured key
     * @param defaultValue default value
     * @return returns the value of key
     */
    public static String getProperty(String key, String defaultValue) {
        if (null == props) {
            loadProps();
        }
        return props.getProperty(key, defaultValue);
    }
}
