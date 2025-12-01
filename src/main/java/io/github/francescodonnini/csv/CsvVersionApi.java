package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.entities.VersionLocalEntity;
import io.github.francescodonnini.model.Version;
import io.github.francescodonnini.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvVersionApi {
    private final Logger logger = Logger.getLogger(CsvVersionApi.class.getName());
    private final Path cachePath;

    public CsvVersionApi(Path cachePath) {
        this.cachePath = cachePath;
    }

    public List<Version> getLocal(String projectName) throws FileNotFoundException {
        return getVersions(cachePath.resolve(projectName).resolve("versions.csv"));
    }

    private List<Version> getVersions(Path path) throws FileNotFoundException {
        try {
            var beans = new CsvToBeanBuilder<VersionLocalEntity>(new FileReader(path.toFile()))
                    .withType(VersionLocalEntity.class)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                    .build()
                    .parse();
            return beans.stream()
                    .map(this::fromCsv)
                    .toList();
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Missing required fields {0}", e.getMessage());
            return List.of();
        }
    }

    private VersionLocalEntity toCsv(Version model) {
        var bean = new VersionLocalEntity();
        bean.setArchived(model.archived());
        bean.setId(model.id());
        bean.setName(model.name());
        bean.setReleased(model.released());
        bean.setReleaseDate(model.releaseDate());
        return bean;
    }

    private Version fromCsv(VersionLocalEntity bean) {
        return new Version(bean.isArchived(), bean.getId(), bean.getName(), bean.isReleased(), bean.getReleaseDate());
    }

    public void saveLocal(List<Version> versions, String projectName) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        saveLocal(versions, cachePath.resolve(projectName).resolve("versions.csv"));
    }

    public void saveLocal(List<Version> versions, Path path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = versions.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path.toString());
        try (var writer = new FileWriter(path.toFile())) {
            var beanToCsv = new StatefulBeanToCsvBuilder<VersionLocalEntity>(writer)
                    .build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        }
    }
}
