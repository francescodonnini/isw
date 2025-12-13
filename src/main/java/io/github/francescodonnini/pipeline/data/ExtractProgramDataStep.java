package io.github.francescodonnini.pipeline.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.collectors.DiffCollector;
import io.github.francescodonnini.collectors.ast.AbstractCounterFactoryImpl;
import io.github.francescodonnini.csv.CsvJavaClassApi;
import io.github.francescodonnini.csv.CsvJavaMethodApi;
import io.github.francescodonnini.data.CsvSmellLinker;
import io.github.francescodonnini.data.DataLoaderImpl;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtractProgramDataStep implements Step<ProjectInfo, ProjectInfo> {
    private final Logger logger = Logger.getLogger(ExtractProgramDataStep.class.getName());
    private final DataPipelineContext context;

    public ExtractProgramDataStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws Exception {
        try {
            var classes = new CsvJavaClassApi().getLocal(getCurrentClassPath());
            var methods = new CsvJavaMethodApi()
                    .getLocal(getCurrentMethodPath(), classes).stream()
                    .filter(m -> !m.isAfter(input.getProjectReleases().getLast()))
                    .toList();
            input.setClasses(classes);
            input.setMethods(methods);
        } catch (FileNotFoundException | RuntimeException e) {
            logger.log(Level.WARNING, "cannot find any classes/methods cached files", e);
            var factory = new AbstractCounterFactoryImpl();
            var source = context.getSources()
                    .resolve(context.getProjectName().toLowerCase());
            var report = context.getReports()
                    .resolve(context.getProjectName());
            var loader = new DataLoaderImpl(factory, input.getProjectReleases(), source, report);
            var classes = loader.getClasses();
            saveClasses(classes, "raw");
            saveMethods(loader.getMethods(), "raw");
            new CsvSmellLinker(report)
                    .link(classes);
            var methods = new DiffCollector(input.getProjectReleases(), loader.getMethods())
                    .collect();
            saveClasses(classes, "nolbl");
            saveMethods(methods, "nolbl");
            input.setClasses(classes);
            input.setMethods(methods);
        }
        return input;
    }

    private void saveClasses(List<JavaClass> classes, String name) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var classApi = new CsvJavaClassApi();
        var path = context.getCache()
                .resolve(context.getProjectName())
                .resolve("classes(%s).csv".formatted(name))
                .toString();
        classApi.saveLocal(classes, path);
    }


    private void saveMethods(List<JavaMethod> methods, String name) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var methodApi = new CsvJavaMethodApi();
        var path = context.getCache()
                .resolve(context.getProjectName())
                .resolve("methods(%s).csv".formatted(name))
                .toString();
        methodApi.saveLocal(methods, path);
    }

    private String getCurrentClassPath() {
        return context.getCache()
                .resolve(context.getProjectName())
                .resolve("classes(nolbl).csv")
                .toString();
    }

    private String getCurrentMethodPath() {
        return context.getCache()
                .resolve(context.getProjectName())
                .resolve("methods(nolbl).csv")
                .toString();
    }
}
