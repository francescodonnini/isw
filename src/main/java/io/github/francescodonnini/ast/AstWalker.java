package io.github.francescodonnini.ast;


import com.sun.source.util.JavacTask;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.sqlite.SQLiteApi;
import io.github.francescodonnini.sqlite.SQLiteClassApi;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javax.tools.ToolProvider;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

public class AstWalker {
    public static void main(String[] args) throws ConfigurationException, IOException, SQLException {
        if (args.length != 2) {
            System.exit(-1);
        }
        var classes = getClasses(args[0], args[1].toUpperCase());
        var i = new Random().nextInt(classes.size());
        var clazz = classes.get(i);
        System.out.println(clazz.getContent());
        var file = createTempFile(clazz);
        var compiler = ToolProvider.getSystemJavaCompiler();
        var units = compiler
                .getStandardFileManager(null, null, null)
                .getJavaFileObjects(file);
        var task = (JavacTask) compiler.getTask(null, null, null, null, null, units);
        var cc = new CyclomaticComplexity();
        var loc = new LineOfCode();
        var ipc = new InputParametersCount();
        for (var cu : task.parse()) {
            cu.accept(loc, cu);
            loc.getLOC().forEach(System.out::println);
            cu.accept(cc, null);
            cc.getComplexity().forEach(System.out::println);
            cu.accept(ipc, null);
            ipc.getInputParametersCount().forEach(System.out::println);
        }
    }

    private static Path createTempFile(JavaClass clazz) throws IOException {
        var file = Files.createTempFile(null, ".java");
        try (var of = new FileWriter(file.toFile())) {
            of.write(clazz.getContent());
        }
        return file;
    }

    private static List<JavaClass> getClasses(String configFile, String projectName) throws ConfigurationException, IOException, SQLException {
        var settings = new IniSettings(configFile);
        var path = Path.of(settings.getString("dataPath"), projectName).toString();
        var localReleaseApi = getLocalReleaseApi(path);
        var releases = localReleaseApi.getLocal();
        var localClassApi = getSQLiteClassApi(path, releases.subList(0, releases.size() / 2));
        return localClassApi.getLocal();
    }

    private static CsvReleaseApi getLocalReleaseApi(String path) {
        return new CsvReleaseApi(Path.of(path, "releases.csv").toString());
    }

    private static SQLiteClassApi getSQLiteClassApi(String path, List<Release> releases) throws SQLException {
        return new SQLiteClassApi(new SQLiteApi(Path.of(path, "classes.db")), releases);
    }
}
