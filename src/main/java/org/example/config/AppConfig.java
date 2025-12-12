package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads config from config.properties. Falls back to defaults if file missing.
 */
public class AppConfig {
    
    private static final String CONFIG_FILE = "config.properties";
    
    // Defaults
    private static final double DEFAULT_MIN_SALARY_MULTIPLIER = 1.20;
    private static final double DEFAULT_MAX_SALARY_MULTIPLIER = 1.50;
    private static final int DEFAULT_MAX_REPORTING_DEPTH = 4;
    
    private static AppConfig instance;
    
    private final double minSalaryMultiplier;
    private final double maxSalaryMultiplier;
    private final int maxReportingDepth;
    
    private AppConfig() {
        Properties props = loadProperties();
        
        this.minSalaryMultiplier = getDouble(props, "salary.min.multiplier", DEFAULT_MIN_SALARY_MULTIPLIER);
        this.maxSalaryMultiplier = getDouble(props, "salary.max.multiplier", DEFAULT_MAX_SALARY_MULTIPLIER);
        this.maxReportingDepth = getInt(props, "reporting.max.depth", DEFAULT_MAX_REPORTING_DEPTH);
    }
    
    /** Get the singleton instance. */
    public static AppConfig get() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
    
    /** For testing – reset the singleton so it reloads. */
    public static void reset() {
        instance = null;
    }
    
    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            // Use defaults
        }
        return props;
    }
    
    private double getDouble(Properties props, String key, double defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private int getInt(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    // Getters
    
    /** Min salary multiplier (e.g., 1.20 = at least 20% more). */
    public double getMinSalaryMultiplier() {
        return minSalaryMultiplier;
    }
    
    /** Max salary multiplier (e.g., 1.50 = at most 50% more). */
    public double getMaxSalaryMultiplier() {
        return maxSalaryMultiplier;
    }
    
    /** Max managers between anyone and CEO. */
    public int getMaxReportingDepth() {
        return maxReportingDepth;
    }
    
    /** Salary min as percentage (e.g., 20 for 20%). */
    public int getMinSalaryPercent() {
        return (int) ((minSalaryMultiplier - 1.0) * 100);
    }
    
    /** Salary max as percentage (e.g., 50 for 50%). */
    public int getMaxSalaryPercent() {
        return (int) ((maxSalaryMultiplier - 1.0) * 100);
    }
}

