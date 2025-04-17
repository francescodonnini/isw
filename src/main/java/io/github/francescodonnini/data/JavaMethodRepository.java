package io.github.francescodonnini.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvJavaMethodApi;
import io.github.francescodonnini.model.JavaMethod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaMethodRepository implements JavaMethodApi {
    private final Logger logger = Logger.getLogger(JavaMethodRepository.class.getName());
    private final CsvJavaMethodApi localSource;
    private final DataLoader factory;
    private final boolean useCache;

    public JavaMethodRepository(DataLoader factory, CsvJavaMethodApi localSource, boolean useCache) {
        this.factory = factory;
        this.localSource = localSource;
        this.useCache = useCache;
    }

    @Override
    public List<JavaMethod> getMethods() {
        if (useCache) {
            return tryGetCache();
        } else {
            return tryGetFreshData();
        }
    }

    private List<JavaMethod> tryGetCache() {
        try {
            var methods = localSource.getLocal();
            if (methods.isEmpty()) {
                methods = factory.getMethods();
                saveLocal(methods);
            }
            return methods;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return tryGetFreshData();
        }
    }

    private List<JavaMethod> tryGetFreshData() {
        var data = factory.getMethods();
        saveLocal(data);
        return data;
    }

    private void saveLocal(List<JavaMethod> classes) {
        try {
            localSource.saveLocal(classes);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}