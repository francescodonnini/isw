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
import java.nio.file.Path;
import java.util.List;

public class CsvReleaseApi {
    private final Path cachePath;

    public CsvReleaseApi(Path cachePath) {
        this.cachePath = cachePath;
    }

    public List<Release> getLocal(String projectName) throws FileNotFoundException {
        return getReleases(projectName);
    }

    public List<Release> getReleases(String projectName) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<ReleaseLocalEntity>(new FileReader(cachePath.resolve(projectName).resolve("releases.csv").toFile()))
                .withType(ReleaseLocalEntity.class)
                .build()
                .parse();
        return beans.stream().map(this::fromCsv).toList();
    }

    public void saveLocal(List<Release> releases, String projectName) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        if (releases.isEmpty()) {
            return;
        }
        saveLocal(releases, cachePath.resolve(projectName).resolve("releases.csv"));
    }

    private void saveLocal(List<Release> releases, Path path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = releases.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path.toString());
        try (var writer = new FileWriter(path.toFile())) {
            var beanToCsv = new StatefulBeanToCsvBuilder<ReleaseLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        }
    }

    protected ReleaseLocalEntity toCsv(Release model) {
        var bean = new ReleaseLocalEntity();
        bean.setOrder(model.order());
        bean.setName(model.name());
        bean.setId(model.id());
        bean.setReleaseDate(model.releaseDate());
        return bean;
    }

    protected Release fromCsv(ReleaseLocalEntity bean) {
        return new Release(bean.getId(), bean.getName(), bean.getReleaseDate(), bean.getOrder());
    }
}
