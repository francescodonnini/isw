package io.github.francescodonnini.cli;

import io.github.francescodonnini.collectors.ast.AbstractCounterFactoryImpl;
import io.github.francescodonnini.data.*;
import io.github.francescodonnini.data.smell.OneShotSmellLinker;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@CommandLine.Command(
        name = "analyze",
        mixinStandardHelpOptions = true,
        description = "")
public class AnalysisCli implements Callable<Integer> {
    private final Logger logger = Logger.getLogger(AnalysisCli.class.getName());

    @CommandLine.Parameters(arity = "1..*", description = "Paths of the .java files")
    private List<File> javaFiles;

    @Override
    public Integer call() throws Exception {
        Path reportsDir = Files.createTempDirectory(Paths.get("."), "pmd-reports");
        Runtime.getRuntime()
                .addShutdownHook(Thread.ofVirtual().unstarted(() -> deleteOnExit(reportsDir)));

        var allClasses = new HashMap<String, List<JavaClass>>();
        for (var javaFile : javaFiles) {
            var outputPath = javaFile.getPath() + ".csv";
            var projectPath = javaFile.getParentFile().toPath();
            var loader = new JavaFileAnalyzer(
                    JavaMethodExtractorFactory.defaultFactory(new AbstractCounterFactoryImpl()),
                    projectPath,
                    reportsDir);
            var classes = loader.analyzeFile(javaFile.toPath());
            if (classes.isEmpty()) {
                logger.log(Level.INFO, "No classes found");
                return 0;
            }
            new OneShotSmellLinker(reportsDir)
                    .link(classes);
            allClasses.put(outputPath, classes);
        }

        try {
            for (var entry : allClasses.entrySet()) {
                save(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return 1;
        }
        return 0;
    }

    private void deleteOnExit(Path directory) {
        try (var walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(file -> {
                    if (!file.delete()) {
                        logger.log(Level.WARNING, "Failed to delete " + file);
                    }
                });
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void save(String outputPath, List<JavaClass> classes) throws IOException {
        try (var writer = new FileWriter(outputPath)) {
            writer.write("path,class,signature,loc,halstead,stmt_count,nesting_depth,cyclomatic_complexity,smells,else_count,duplication,parameters_count\n");
            var s = new StringBuilder();
            for (var c : classes) {
                for (var m : c.getMethods()) {
                    s.append(c.getPath().toString())
                     .append(",")
                     .append(c.getName())
                     .append(",")
                     .append('"').append(m.getSignature()).append('"')
                     .append(",");
                    metrics(s, m).append("\n");
                    writer.write(s.toString());
                    s.setLength(0);
                }
            }
        }
    }

    private StringBuilder metrics(StringBuilder s, JavaMethod m) {
        s.append(m.getMetrics().getLineOfCode())
         .append(",")
         .append(m.getMetrics().getHalsteadEffort())
         .append(",")
         .append(m.getMetrics().getStatementsCount())
         .append(",")
         .append(m.getMetrics().getNestingDepth())
         .append(",")
         .append(m.getMetrics().getCyclomaticComplexity())
         .append(",")
         .append(m.getMetrics().getCodeSmells())
         .append(",")
         .append(m.getMetrics().getElseCount())
         .append(",")
         .append(m.getMetrics().getCodeDuplication())
         .append(",")
         .append(m.getMetrics().getParametersCount());
        return s;
    }
}
