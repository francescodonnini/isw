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
import java.nio.file.Files;
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
            var classApi = new CsvJavaClassApi();
            var classes = classApi.getLocal(getCurrentClassPath());
            input.setClasses(classes);
            var methodApi = new CsvJavaMethodApi();
            var methods = methodApi
                    .getLocal(getCurrentMethodPath(), classes).stream()
                    .filter(m -> !m.getJavaClass().getTime().isAfter(input.getProjectReleases().getLast().releaseDate().atStartOfDay()))
                    .toList();
            Files.write(
                    context.getData().resolve("main_tool_signatures.txt"),
                    classes.stream()
                            .flatMap(c -> c.getMethods().stream()) // Assuming JavaClass has getMethods()
                            .map(m -> m.getJavaClass().getName() + "::" + m.getSignature())
                            .sorted()
                            .toList());
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
            new CsvSmellLinker(report).link(classes);
            var methods = new DiffCollector(input.getProjectReleases(), loader.getMethods())
                    .collect();
            saveClasses(classes);
            saveMethods(methods);
            input.setMethods(methods);
        }
        return input;
    }

    private void saveClasses(List<JavaClass> classes) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var classApi = new CsvJavaClassApi();
        var path = context.getCache()
                .resolve(context.getProjectName())
                .resolve("classes(nolbl).csv")
                .toString();
        classApi.saveLocal(classes, path);
    }


    private void saveMethods(List<JavaMethod> methods) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var methodApi = new CsvJavaMethodApi();
        var path = context.getCache()
                .resolve(context.getProjectName())
                .resolve("methods(nolbl).csv")
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
