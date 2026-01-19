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
import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
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
            var classes = new CsvJavaClassApi()
                    .getLocal(cachedClassesPath(input, "nolbl"));
            var methods = new CsvJavaMethodApi()
                    .getLocal(cachedMethodsPath(input, "nolbl"), classes).stream()
                    .filter(m -> !m.isAfter(input.getProjectReleases().getLast()))
                    .toList();
            input.setClasses(classes);
            input.setMethods(methods);
        } catch (FileNotFoundException | RuntimeException e) {
            logger.log(Level.WARNING, "cannot find any classes/methods cached files", e);
            tryGetRawData(input);
        }
        return input;
    }

    private void tryGetRawData(ProjectInfo info) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        try {
            var classes = new CsvJavaClassApi()
                    .getLocal(destinationPath("classes", "raw", info));
            var methods = new CsvJavaMethodApi()
                    .getLocal(destinationPath("methods","raw", info), classes).stream()
                    .filter(m -> !m.isAfter(info.getProjectReleases().getLast()))
                    .toList();
            info.setClasses(classes);
            info.setMethods(methods);
            calculateChanges(info);
        } catch (FileNotFoundException | RuntimeException e) {
            var factory = new AbstractCounterFactoryImpl();
            var source = context.getSources()
                    .resolve(info.getProject().toLowerCase());
            var report = context.getReports()
                    .resolve(info.getProject());
            var loader = new DataLoaderImpl(factory, info.getProjectReleases(), source, report);
            var classes = loader.getClasses();
            saveClasses(classes, cachedClassesPath(info, "raw"));
            var methods = loader.getMethods();
            saveMethods(methods, cachedMethodsPath(info, "raw"));
            info.setClasses(classes);
            info.setMethods(methods);
            calculateChanges(info);
        }
    }

    private void calculateChanges(ProjectInfo info) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var report = context.getReports()
                .resolve(info.getProject());
        new CsvSmellLinker(report)
                .link(info.getClasses());
        var methods = new DiffCollector(info.getProjectReleases(), info.getMethods(), info.isFromStart())
                .collect();
        saveClasses(info.getClasses(), cachedClassesPath(info, "nolbl"));
        saveMethods(methods, cachedMethodsPath(info, "nolbl"));
        info.setMethods(methods);
    }

    private void saveClasses(List<JavaClass> classes, String path) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        new CsvJavaClassApi()
                .saveLocal(classes, path);
    }


    private void saveMethods(List<JavaMethod> methods, String path) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        new CsvJavaMethodApi()
                .saveLocal(methods, path);
    }

    private String cachedClassesPath(ProjectInfo info, String desc) {
        return destinationPath("classes", desc, info);
    }

    private String cachedMethodsPath(ProjectInfo info, String desc) {
        return destinationPath("methods", desc, info);
    }

    private String destinationPath(String prefix, String description, ProjectInfo input) {
        if (input.isFromStart() && !description.equals("raw")) {
            description += "_fromStart";
        }
        return context.getCache()
                .resolve(input.getProject())
                .resolve("%s(%s).csv".formatted(prefix, description))
                .toString();
    }
}
