package io.github.francescodonnini.data.smell;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import io.github.francescodonnini.model.RevisionJavaClass;

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

    protected AbstractSmellLinker(Path reportsDir) {
        this.reportsDir = reportsDir;
    }

    @Override
    public void link(List<RevisionJavaClass> classes) {
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

    protected abstract void prepareIndex(List<RevisionJavaClass> classes);

    protected abstract void processReport(String fileName, List<CsvReportEntity> entities);

    protected void link(CsvReportEntity e, Map<String, RevisionJavaClass> index) {
        var cls = index.get(e.getFilePath());
        if (cls == null) {
            return;
        }
        cls.getMetrics().incSmellCount();
    }
}
