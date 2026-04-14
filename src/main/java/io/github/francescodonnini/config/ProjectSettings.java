package io.github.francescodonnini.config;

import java.util.List;

public class ProjectSettings implements Settings {

    private final Settings baseSettings;
    private final String projectName;

    public ProjectSettings(Settings baseSettings, String projectName) {
        this.baseSettings = baseSettings;
        this.projectName = projectName.toLowerCase();
    }

    private String resolveKey(String key) {
        String projectKey = projectName + "_" + key;
        if (baseSettings.hasKey(projectKey)) {
            return projectKey;
        }
        return key;
    }

    @Override
    public boolean hasKey(String key) {
        return baseSettings.hasKey(projectName + "_" + key) || baseSettings.hasKey(key);
    }

    @Override
    public boolean getBool(String key) {
        return baseSettings.getBool(resolveKey(key));
    }

    @Override
    public boolean getBool(String key, boolean defaultValue) {
        return baseSettings.getBool(resolveKey(key), defaultValue);
    }

    @Override
    public double getDouble(String key) {
        return baseSettings.getDouble(resolveKey(key));
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return baseSettings.getDouble(resolveKey(key), defaultValue);
    }

    @Override
    public float getFloat(String key) {
        return baseSettings.getFloat(resolveKey(key));
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return baseSettings.getFloat(resolveKey(key), defaultValue);
    }

    @Override
    public int getInt(String key) {
        return baseSettings.getInt(resolveKey(key));
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return baseSettings.getInt(resolveKey(key), defaultValue);
    }

    @Override
    public String getString(String key) {
        return baseSettings.getString(resolveKey(key));
    }

    @Override
    public String getString(String key, String defaultValue) {
        return baseSettings.getString(resolveKey(key), defaultValue);
    }

    @Override
    public <T> List<T> getList(String key, Class<T> cls) {
        return baseSettings.getList(resolveKey(key), cls);
    }
}