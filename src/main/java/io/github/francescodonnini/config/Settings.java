package io.github.francescodonnini.config;

public interface Settings {
    boolean getBool(String key);
    boolean getBool(String key, boolean defaultValue);
    double getDouble(String key);
    double getDouble(String key, double defaultValue);
    float getFloat(String key);
    float getFloat(String key, float defaultValue);
    int getInt(String key);
    int getInt(String key, int defaultValue);
    String getString(String key);
    String getString(String key, String defaultValue);
}
