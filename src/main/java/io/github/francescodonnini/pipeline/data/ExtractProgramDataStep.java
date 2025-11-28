package io.github.francescodonnini.pipeline.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.collectors.DiffCollector;
import io.github.francescodonnini.collectors.ast.AbstractCounterFactoryImpl;
import io.github.francescodonnini.csv.CsvJavaClassApi;
import io.github.francescodonnini.csv.CsvJavaMethodApi;
import io.github.francescodonnini.data.CsvSmellLinker;
import io.github.francescodonnini.data.DataLoaderImpl;
import io.github.francescodonnini.data.LabelMakerImpl;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.pipeline.DataPipelineContext;
import io.github.francescodonnini.pipeline.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.utils.GitUtils;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class ExtractProgramDataStep implements Step<ProjectInfo, ProjectInfo> {
    private final Logger logger = Logger.getLogger(ExtractProgramDataStep.class.getName());
    private final DataPipelineContext context;

    public ExtractProgramDataStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws Exception {
        if (context.useCache()) {
            var classApi = new CsvJavaClassApi();
            var classes = classApi.getLocal(getCurrentClassPath());
            input.setClasses(classes);
            var methodApi = new CsvJavaMethodApi();
            var methods = methodApi.getLocal(getCurrentMethodPath(), classes);
            input.setMethods(methods);
        } else {
            var factory = new AbstractCounterFactoryImpl();
            var source = context.getSources()
                    .resolve(context.getProjectName().toLowerCase());
            var report = context.getReports()
                    .resolve(context.getProjectName())
                    .toString();
            var loader = new DataLoaderImpl(source.toString(), factory, report, input.getProjectReleases());
            var classes = loader.getClasses();
            new CsvSmellLinker(report).link(classes);
            var methods = loader.getMethods();
            methods = new DiffCollector(input.getProjectReleases(), loader.getMethods())
                    .collect();
            try (var git = GitUtils.createGit(source)) {
                methods = new LabelMakerImpl(git, input.getIssues(), methods, input.getProjectReleases())
                        .makeLabels();
                input.setMethods(methods);
                saveClasses(classes);
                saveMethods(methods);
            }
        }
        return input;
    }

    private void saveClasses(List<JavaClass> classes) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var classApi = new CsvJavaClassApi();
        var path = context.getCache()
                .resolve(context.getProjectName())
                .resolve("classes.csv")
                .toString();
        classApi.saveLocal(classes, path);
    }


    private void saveMethods(List<JavaMethod> methods) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        var methodApi = new CsvJavaMethodApi();
        var path = context.getCache()
                .resolve(context.getProjectName())
                .resolve("methods.csv")
                .toString();
        methodApi.saveLocal(methods, path);
    }

    private String getCurrentClassPath() {
        return context.getCache()
                .resolve(context.getProjectName())
                .resolve("classes.csv")
                .toString();
    }

    private String getCurrentMethodPath() {
        return context.getCache()
                .resolve(context.getProjectName())
                .resolve("methods.csv")
                .toString();
    }
}
