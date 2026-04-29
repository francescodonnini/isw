package io.github.francescodonnini.pipeline.data;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.collectors.DiffCollector;
import io.github.francescodonnini.csv.CsvJavaClassApi;
import io.github.francescodonnini.data.JavaClassAnalyzerFactory;
import io.github.francescodonnini.data.smell.CsvSmellLinker;
import io.github.francescodonnini.data.RevisionDataExtractor;
import io.github.francescodonnini.model.ReleaseJavaClass;
import io.github.francescodonnini.model.RevisionJavaClass;
import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.DataPipelineContext;
import io.github.francescodonnini.pipeline.inputs.ProjectInfo;
import io.github.francescodonnini.pipeline.Step;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtractProgramDataStep implements Step<ProjectInfo, ProjectInfo> {
    private static final String NO_LABEL = "nolbl";
    private final Logger logger = Logger.getLogger(ExtractProgramDataStep.class.getName());
    private final DataPipelineContext context;

    public ExtractProgramDataStep(DataPipelineContext context) {
        this.context = context;
    }

    @Override
    public ProjectInfo execute(ProjectInfo input) throws PipelineException {
        try {
            var classes = new CsvJavaClassApi()
                    .getReleaseClasses(cachedClassesPath(input, NO_LABEL));
            input.setReleaseClasses(classes);
        } catch (FileNotFoundException | RuntimeException e) {
            logger.log(Level.WARNING, "cannot find any classes/methods cached files", e);
            tryGetCommitData(input);
        } catch (Exception e) {
            // Catch any other unexpected CSV/IO errors and wrap them
            throw new PipelineException("Unexpected error reading cached program data", e);
        }
        return input;
    }

    private void tryGetCommitData(ProjectInfo info) throws PipelineException {
        try {
            var classes = new CsvJavaClassApi()
                    .getRevisionClasses(destinationPath("classes", "revisions", info));
            info.setRevisionClasses(classes);
            calculateChanges(info);
        } catch (FileNotFoundException unused) {
            loadRevisionData(info);
        } catch (RuntimeException e) {
            throw new PipelineException(e);
        }
    }

    private void loadRevisionData(ProjectInfo info) throws PipelineException {
        try {
            var source = context.getSources()
                    .resolve(info.getProject().toLowerCase());
            var report = context.getReports()
                    .resolve(info.getProject());
            var loader = new RevisionDataExtractor(
                    JavaClassAnalyzerFactory.defaultFactory(),
                    info.getAllReleases(),
                    source,
                    report);
            var classes = loader.getRevisionClasses();
            saveRevisionClasses(cachedClassesPath(info, "revisions"), classes);
            info.setRevisionClasses(classes);
            calculateChanges(info);
        } catch (IOException e) {
            throw new PipelineException("cannot load raw data", e);
        }
    }

    private void calculateChanges(ProjectInfo info) throws PipelineException {
        var report = context.getReports()
                .resolve(info.getProject());
        new CsvSmellLinker(report)
                .link(info.getRevisionClasses());
        var classes = new DiffCollector(info.getAllReleases(), info.getRevisionClasses(), info.isFromStart())
                .collect();
        saveReleaseClasses(cachedClassesPath(info, NO_LABEL), classes);
    }

    private void saveReleaseClasses(String path, List<ReleaseJavaClass> classes) throws PipelineException {
        try {
            new CsvJavaClassApi()
                    .saveReleaseClasses(path, classes);
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException e) {
            throw new PipelineException(e);
        }
    }

    private void saveRevisionClasses(String path, List<RevisionJavaClass> classes) throws PipelineException {
        try {
            new CsvJavaClassApi()
                    .saveRevisionClasses(path, classes);
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException e) {
            throw new PipelineException(e);
        }
    }

    private String cachedClassesPath(ProjectInfo info, String desc) {
        return destinationPath("classes", desc, info);
    }

    private String destinationPath(String prefix, String description, ProjectInfo input) {
        if (input.isFromStart() && !description.equals("revisions")) {
            description += "_fromStart";
        }
        return context.getCache()
                .resolve(input.getProject())
                .resolve("%s(%s).csv".formatted(prefix, description))
                .toString();
    }
}
