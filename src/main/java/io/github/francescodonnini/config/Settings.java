package io.github.francescodonnini.config;

import java.util.List;

public interface Settings {
    // Inside IniSettings.java
    boolean hasKey(String key);
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
    <T> List<T> getList(String key, Class<T> cls);
}
