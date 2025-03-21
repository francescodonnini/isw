package io.github.francescodonnini.config;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.FileReader;
import java.io.IOException;

public class IniSettings implements Settings {
    private final INIConfiguration ini;

    public IniSettings(String filePath) throws IOException, ConfigurationException {
        try (var reader = new FileReader(filePath)) {
            ini = new INIConfiguration();
            ini.read(reader);
        }
    }

    @Override
    public boolean getBool(String key) {
        return ini.getBoolean(key);
    }

    @Override
    public boolean getBool(String key, boolean defaultValue) {
        return ini.getBoolean(key, defaultValue);
    }

    @Override
    public double getDouble(String key) {
        return ini.getDouble(key);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return ini.getDouble(key, defaultValue);
    }

    @Override
    public float getFloat(String key) {
        return ini.getFloat(key);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return ini.getFloat(key, defaultValue);
    }

    @Override
    public int getInt(String key) {
        return ini.getInt(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return ini.getInt(key, defaultValue);
    }

    @Override
    public String getString(String key) {
        return ini.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return ini.getString(key, defaultValue);
    }
}
