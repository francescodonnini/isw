package io.github.francescodonnini;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.collectors.DiffCollector;
import io.github.francescodonnini.collectors.ast.AbstractCounterFactoryImpl;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvJavaClassApi;
import io.github.francescodonnini.csv.CsvJavaMethodApi;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.jira.JsonReleaseApi;
import io.github.francescodonnini.jira.JsonVersionApi;
import io.github.francescodonnini.jira.RestApi;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws ConfigurationException, IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        if (args.length != 2) {
            System.exit(-1);
        }
        var projectName = args[1].toUpperCase();
        // regex "<project name>-d+" ("%s-\\d+") è presente in tutti i commit che chiudono un ticket di JIRA
        var settings = new IniSettings(args[0]);
        var useCache = true;
        var restApi = new RestApi();
        var projectPath = Path.of(settings.getString("gitBasePath"), projectName.toLowerCase()).toString();
        var path = Path.of(settings.getString("dataPath"), projectName).toString();
        var remoteVersionApi = new JsonVersionApi(projectName, restApi);
        var localVersionApi = new CsvVersionApi(Path.of(path, "versions.csv").toString());
        var versionApi = new VersionRepository(remoteVersionApi, localVersionApi, useCache);
        var remoteReleaseApi = new JsonReleaseApi(versionApi);
        var localReleaseApi = new CsvReleaseApi(Path.of(path, "releases.csv").toString());
        var releaseApi = new ReleaseRepository(remoteReleaseApi, localReleaseApi, useCache);
        var releases = releaseApi.getReleases();
        var lastRelease = releases.size() / 3;
        var factory = new DataLoaderImpl(projectPath, new AbstractCounterFactoryImpl(), releases.get(lastRelease).releaseDate());
        var localClassApi = new CsvJavaClassApi(Path.of(path, "classes.csv").toString());
        var classApi = new JavaClassRepository(factory, localClassApi, useCache);
        var classes = classApi.getClasses();
        var localMethodApi = new CsvJavaMethodApi(Path.of(path, "methods.csv").toString(), classes);
        var methodApi = new JavaMethodRepository(factory, localMethodApi, useCache);
        var methods = methodApi.getMethods();
        var diff = new DiffCollector(releases, methods);
        localMethodApi.saveLocal(diff.collect(), String.valueOf(Path.of(path, "methods_complete.csv")));
    }
}