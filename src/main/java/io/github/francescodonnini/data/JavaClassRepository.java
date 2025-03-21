package io.github.francescodonnini.data;

import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.sqlite.SQLiteClassApi;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaClassRepository implements JavaClassApi {
    private final Logger logger = Logger.getLogger(JavaClassRepository.class.getName());
    private final SQLiteClassApi localSource;
    private final JavaClassFactory factory;

    public JavaClassRepository(JavaClassFactory factory, SQLiteClassApi localSource) {
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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return List.of();
        }
    }

    private void saveLocal(List<JavaClass> classes) {
        try {
            localSource.saveLocal(classes);
        } catch (SQLException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
    }
}