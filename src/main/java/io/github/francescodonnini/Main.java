package io.github.francescodonnini;

import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.jira.JsonReleaseApi;
import io.github.francescodonnini.jira.JsonVersionApi;
import io.github.francescodonnini.jira.RestApi;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.sqlite.SQLiteApi;
import io.github.francescodonnini.sqlite.SQLiteClassApi;
import io.github.francescodonnini.sqlite.SQLiteMethodApi;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws ConfigurationException, IOException, SQLException {
        if (args.length != 2) {
            System.exit(-1);
        }
        var projectName = args[1].toUpperCase();
        // regex "<project name>-d+" ("%s-\\d+") è presente in tutti i commit che chiudono un ticket di JIRA
        var settings = new IniSettings(args[0]);
        var restApi = new RestApi();
        var projectPath = Path.of(settings.getString("gitBasePath"), projectName.toLowerCase()).toString();
        var path = Path.of(settings.getString("dataPath"), projectName).toString();
        var remoteVersionApi = new JsonVersionApi(projectName, restApi);
        var localVersionApi = new CsvVersionApi(Path.of(path, "versions.csv").toString());
        var versionApi = new VersionRepository(remoteVersionApi, localVersionApi);
        var remoteReleaseApi = new JsonReleaseApi(versionApi);
        var localReleaseApi = new CsvReleaseApi(Path.of(path, "releases.csv").toString());
        var releaseApi = new ReleaseRepository(remoteReleaseApi, localReleaseApi);
        var releases = releaseApi.getReleases();
        releases = releases.subList(0, (int) (releases.size() * 0.5));
        var factory = new DataLoaderImpl(projectPath, releases);
        var sqliteApi = new SQLiteApi(Path.of(path, "classes.db"));
        var localClassApi = new SQLiteClassApi(sqliteApi, releases);
        var classApi = new JavaClassRepository(factory, localClassApi);
        var classes = classApi.getClasses();
        var localMethodApi = new SQLiteMethodApi(sqliteApi, classes);
        var methodApi = new JavaMethodRepository(factory, localMethodApi);
        var methods = methodApi.getMethods();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        IntStream.range(0, 20).forEach(_ -> {
            var i = random.nextInt(classes.size());
            var clazz = classes.get(i);
            System.out.println(clazz);
            methods.stream().filter(m -> m.getJavaClass().equals(clazz))
                    .forEach(System.out::println);
        });
    }
}