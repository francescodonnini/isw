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
import java.util.List;

public class CsvVersionApi {
    private final String defaultPath;

    public CsvVersionApi(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public List<Version> getLocal() throws FileNotFoundException {
        return getVersions(defaultPath);
    }

    public List<Version> getLocal(String path) throws FileNotFoundException {
        return getVersions(path);
    }

    private List<Version> getVersions(String path) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<VersionLocalEntity>(new FileReader(path))
                .withType(VersionLocalEntity.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
        return beans.stream().map(this::fromCsv).toList();
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

    public void saveLocal(List<Version> versions) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        saveLocal(versions, defaultPath);
    }

    public void saveLocal(List<Version> versions, String path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = versions.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<VersionLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        }
    }
}
