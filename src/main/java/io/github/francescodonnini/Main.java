package io.github.francescodonnini;

import io.github.francescodonnini.collectors.VcsCollector;
import io.github.francescodonnini.collectors.ast.CyclomaticComplexityCounter;
import io.github.francescodonnini.collectors.ast.InputParametersCounter;
import io.github.francescodonnini.collectors.ast.StatementsCounter;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvJavaClassApi;
import io.github.francescodonnini.csv.CsvJavaMethodApi;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.jira.JsonReleaseApi;
import io.github.francescodonnini.jira.JsonVersionApi;
import io.github.francescodonnini.jira.RestApi;
import io.github.francescodonnini.model.JavaMethod;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.Git;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ConfigurationException, IOException {
        if (args.length != 2) {
            System.exit(-1);
        }
        var projectName = args[1].toUpperCase();
        // regex "<project name>-d+" ("%s-\\d+") è presente in tutti i commit che chiudono un ticket di JIRA
        var settings = new IniSettings(args[0]);
        var useCache = settings.getBool("useCache");
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
        var counters = List.of(
                new CyclomaticComplexityCounter(),
                new InputParametersCounter(),
                new StatementsCounter()
        );
        var releaseRange = releases.subList(0, lastRelease);
        var factory = new DataLoaderImpl(projectPath, releaseRange, counters, false);
        var localClassApi = new CsvJavaClassApi(Path.of(path, "classes.csv").toString(), releases);
        var classApi = new JavaClassRepository(factory, localClassApi, useCache);
        var classes = classApi.getClasses();
        var localMethodApi = new CsvJavaMethodApi(Path.of(path, "methods.csv").toString(), classes);
        var methodApi = new JavaMethodRepository(factory, localMethodApi, useCache);
        var methods = methodApi.getMethods();
        System.out.printf("#classes=%d\t#methods=%d\n", classes.size(), methods.size());
        methods.forEach(Main::printMetrics);
        var gitPath = Path.of(settings.getString("gitBasePath"), projectName.toLowerCase(), ".git");
        var vscCollector = new VcsCollector(releaseRange, gitPath);
        vscCollector.calculate(methods);
    }

    private static void printMetrics(JavaMethod m) {
        System.out.println(m.getSignature());
        System.out.println(m.getMetrics());
    }

}