package io.github.francescodonnini.data;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;

public class CsvSmellLinker implements SmellLinker {
    private final Logger logger = Logger.getLogger(CsvSmellLinker.class.getName());
    private final String reportDirectory;

    public CsvSmellLinker(String reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    @Override
    public void link(List<JavaClass> classes) {
        var index = createCommitClassIndex(classes);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(reportDirectory))) {
            for (Path path : stream) {
                var fileName = path.getFileName().toString();
                if (fileName.endsWith(".csv")) {
                    var commit = fileName
                            .replace(".csv", "");
                    parse(path).forEach(e -> fillCodeSmells(e, index.getOrDefault(commit, Map.of())));
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    private void fillCodeSmells(CsvReportEntity e,Map<String, JavaClass> classes) {
        var clazz = classes.get(e.getFilePath());
        if (clazz == null) {
            return;
        }
        clazz.getMethods().stream()
                .filter(m -> isBetween(m, e.getLine()))
                .forEach(m -> m.getMetrics().incCodeSmells());
    }

    private Map<String, Map<String, JavaClass>> createCommitClassIndex(List<JavaClass> classes) {
        var index = new HashMap<String, Map<String, JavaClass>>();
        for (var c : classes) {
            var classSet = index.computeIfAbsent(c.getCommit(), k -> new HashMap<>());
            classSet.put(c.getAbsolutePath().toString(), c);
            var oldPath = c.getOldPath();
            oldPath.ifPresent(path -> classSet.put(path.toString(), c));
        }
        return index;
    }

    private boolean isBetween(JavaMethod m, int line) {
        return m.getStartLine() <= line && m.getEndLine() >= line;
    }
    
    private List<CsvReportEntity> parse(Path path) throws FileNotFoundException {
        return new CsvToBeanBuilder<CsvReportEntity>(new FileReader(path.toFile()))
                .withType(CsvReportEntity.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
    }
}
