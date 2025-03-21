package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.entities.ReleaseLocalEntity;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvReleaseApi {
    private final String defaultPath;

    public CsvReleaseApi(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public List<Release> getLocal() throws FileNotFoundException {
        return getReleases(defaultPath);
    }

    public List<Release> getLocal(String path) throws FileNotFoundException {
        return getReleases(path);
    }

    public List<Release> getReleases(String path) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<ReleaseLocalEntity>(new FileReader(path))
                .withType(ReleaseLocalEntity.class)
                .build()
                .parse();
        return beans.stream().map(this::fromCsv).toList();
    }

    public void saveLocal(List<Release> releases) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        saveLocal(releases, defaultPath);
    }

    private void saveLocal(List<Release> releases, String path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = releases.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<ReleaseLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        }
    }

    protected ReleaseLocalEntity toCsv(Release model) {
        var bean = new ReleaseLocalEntity();
        bean.setReleaseNumber(model.number());
        bean.setName(model.name());
        bean.setId(model.id());
        bean.setReleaseDate(model.releaseDate());
        return bean;
    }

    protected Release fromCsv(ReleaseLocalEntity bean) {
        return new Release(bean.getReleaseNumber(), bean.getId(), bean.getName(), bean.getReleaseDate());
    }
}
