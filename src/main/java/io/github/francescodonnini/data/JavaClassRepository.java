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

    public JavaClassRepository(DataLoader factory, CsvJavaClassApi localSource) {
        this.localSource = localSource;
        this.factory = factory;
    }

    @Override
    public List<JavaClass> getClasses() {
        try {
            var classes = localSource.getLocal();
            if (classes.isEmpty()) {
                classes = factory.getClasses();
                saveLocal(classes);
            }
            return classes;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            var data = factory.getClasses();
            saveLocal(data);
            return data;
        }
    }

    private void saveLocal(List<JavaClass> classes) {
        try {
            localSource.saveLocal(classes);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
    }
}