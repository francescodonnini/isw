package io.github.francescodonnini.pipeline.data;

import io.github.francescodonnini.collectors.DiffCollector;
import io.github.francescodonnini.collectors.ast.AbstractCounterFactoryImpl;
import io.github.francescodonnini.csv.CsvJavaClassApi;
import io.github.francescodonnini.csv.CsvJavaMethodApi;
import io.github.francescodonnini.data.CsvSmellLinker;
import io.github.francescodonnini.data.DataLoaderImpl;
import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalculateChanges implements Step<ProjectInfo, ProjectInfo> {
    private final Logger logger = Logger.getLogger(CalculateChanges.class.getName());
    private final DataPipelineContext context;

    public CalculateChanges(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws Exception {
        try {
            var classesPath = context.getCache()
                    .resolve(context.getProjectName())
                    .resolve("classes(raw).csv")
                    .toString();
            var classes = new CsvJavaClassApi()
                    .getLocal(classesPath);
            var methodsPath = context.getCache()
                    .resolve(context.getProjectName())
                    .resolve("methods(raw).csv")
                    .toString();
            var methods = new CsvJavaMethodApi()
                    .getLocal(methodsPath, classes).stream()
                    .filter(m -> !m.isAfter(input.getProjectReleases().getLast()))
                    .toList();
            methods = new DiffCollector(input.getProjectReleases(), methods)
                    .collect();
            input.setClasses(classes);
            input.setMethods(methods);
        } catch (FileNotFoundException | RuntimeException e) {
            logger.log(Level.WARNING, "cannot find any classes/methods cached files", e);
        }
        return input;
    }
}
