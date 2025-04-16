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

    public JavaMethodRepository(DataLoader factory, CsvJavaMethodApi localSource) {
        this.factory = factory;
        this.localSource = localSource;
    }

    @Override
    public List<JavaMethod> getMethods() {
        try {
            var methods = localSource.getLocal();
            if (methods.isEmpty()) {
                methods = factory.getMethods();
                saveLocal(methods);
            }
            return methods;
        } catch (DataLoaderException | FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            var data = factory.getMethods();
            saveLocal(data);
            return data;
        }
    }

    private void saveLocal(List<JavaMethod> classes) {
        try {
            localSource.saveLocal(classes);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
    }
}