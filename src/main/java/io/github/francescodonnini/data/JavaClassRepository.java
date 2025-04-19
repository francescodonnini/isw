package io.github.francescodonnini.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvJavaClassApi;
import io.github.francescodonnini.model.JavaClass;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaClassRepository implements JavaClassApi {
    private final Logger logger = Logger.getLogger(JavaClassRepository.class.getName());
    private final CsvJavaClassApi localSource;
    private final DataLoader factory;
    private final boolean useCache;

    public JavaClassRepository(DataLoader factory, CsvJavaClassApi localSource, boolean useCache) {
        this.localSource = localSource;
        this.factory = factory;
        this.useCache = useCache;
    }

    @Override
    public List<JavaClass> getClasses() {
        if (useCache) {
            return tryGetCache();
        } else {
            return tryGetFreshData();
        }
    }

    private List<JavaClass> tryGetCache() {
        try {
            var classes = localSource.getLocal();
            if (classes.isEmpty()) {
                classes = factory.getClasses();
                saveLocal(classes);
            }
            return classes;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return tryGetFreshData();
        }
    }

    private List<JavaClass> tryGetFreshData() {
        var data = factory.getClasses();
        saveLocal(data);
        return data;
    }

    @Override
    public void save(List<JavaClass> classes) {
        saveLocal(classes);
    }

    private void saveLocal(List<JavaClass> classes) {
        try {
            localSource.saveLocal(classes);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}