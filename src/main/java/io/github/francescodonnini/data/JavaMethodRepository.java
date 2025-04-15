package io.github.francescodonnini.data;

import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.sqlite.SQLiteMethodApi;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaMethodRepository implements JavaMethodApi {
    private final Logger logger = Logger.getLogger(JavaClassRepository.class.getName());
    private final SQLiteMethodApi localSource;
    private final DataLoader factory;

    public JavaMethodRepository(DataLoader factory, SQLiteMethodApi localSource) {
        this.localSource = localSource;
        this.factory = factory;
    }

    @Override
    public List<JavaMethod> getMethods() {
        try {
            var methods = localSource.getLocal();
            if (methods.isEmpty()) {
                methods = factory.getMethods();
                printDuplicates(methods);
                saveLocal(methods);
            }
            return methods;
        } catch (SQLException | DataLoaderException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return List.of();
        }
    }

    private static void printDuplicates(List<JavaMethod> methods) {
        var set = new HashSet<JavaMethod>();
        for (JavaMethod method : methods) {
            if (!set.add(method)) {
                System.out.printf("Duplicate method:%n%s %s %s%n", method.getJavaClass().getRelease().id(), method.getJavaClass().getPath(), method.getSignature());
            }
        }
    }

    private void saveLocal(List<JavaMethod> classes) {
        try {
            localSource.saveLocal(classes);
        } catch (SQLException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
    }
}