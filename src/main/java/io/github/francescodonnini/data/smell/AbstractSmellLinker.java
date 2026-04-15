package io.github.francescodonnini.data.smell;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractSmellLinker implements SmellLinker {
    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected final Path reportsDir;

    public AbstractSmellLinker(Path reportsDir) {
        this.reportsDir = reportsDir;
    }

    @Override
    public void link(List<JavaClass> classes) {
        prepareIndex(classes);
        try (var stream = Files.newDirectoryStream(reportsDir)) {
            for (var path : stream) {
                var file = path.getFileName().toString();
                processReport(file, parse(path));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static List<CsvReportEntity> parse(Path path) throws FileNotFoundException {
        return new CsvToBeanBuilder<CsvReportEntity>(new FileReader(path.toFile()))
                .withType(CsvReportEntity.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
    }

    protected abstract void prepareIndex(List<JavaClass> classes);

    protected abstract void processReport(String fileName, List<CsvReportEntity> entities);

    protected void link(CsvReportEntity e, Map<String, JavaClass> index) {
        var cls = index.get(e.getFilePath());
        if (cls == null) {
            return;
        }
        cls.getMethods().stream()
                .filter(m -> isBetween(m, e.getLine()))
                .forEach(m -> m.getMetrics().incCodeSmells());
    }

    private boolean isBetween(JavaMethod m, int line) {
        return m.getStartLine() <= line && m.getEndLine() >= line;
    }
}
