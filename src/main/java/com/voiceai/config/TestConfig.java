package com.voiceai.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for test properties
 */
public class TestConfig {

    private static TestConfig instance;
    private Properties properties;

    private TestConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static TestConfig getInstance() {
        if (instance == null) {
            synchronized (TestConfig.class) {
                if (instance == null) {
                    instance = new TestConfig();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // API Configuration
    public String getBaseUrl() {
        return properties.getProperty("api.base.url", "http://localhost:8080");
    }

    public String getChatEndpoint() {
        return properties.getProperty("api.chat.endpoint", "/api/chat");
    }

    public String getSessionEndpoint() {
        return properties.getProperty("api.session.endpoint", "/api/session");
    }

    public String getHistoryEndpoint() {
        return properties.getProperty("api.history.endpoint", "/api/history");
    }

    public String getAuthToken() {
        return properties.getProperty("api.auth.token", "");
    }

    // Latency Thresholds
    public int getAcceptableLatency() {
        return Integer.parseInt(properties.getProperty("latency.acceptable", "500"));
    }

    public int getMaximumLatency() {
        return Integer.parseInt(properties.getProperty("latency.maximum", "2000"));
    }

    public int getSimpleQueryLatency() {
        return Integer.parseInt(properties.getProperty("latency.simple.query", "300"));
    }

    // Database Configuration
    public String getDbUrl() {
        return properties.getProperty("db.url", "");
    }

    public String getDbUsername() {
        return properties.getProperty("db.username", "");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password", "");
    }

    // Confidence Thresholds
    public double getMinimumConfidence() {
        return Double.parseDouble(properties.getProperty("confidence.minimum", "0.5"));
    }

    public double getAcceptableConfidence() {
        return Double.parseDouble(properties.getProperty("confidence.acceptable", "0.7"));
    }

    public double getHighConfidence() {
        return Double.parseDouble(properties.getProperty("confidence.high", "0.9"));
    }

    // Logging
    public String getLogFilePath() {
        return properties.getProperty("log.file.path", "logs/test.log");
    }
}
