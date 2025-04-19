package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.entities.JavaClassLocalEntity;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvJavaClassApi {
    private final String defaultPath;

    public CsvJavaClassApi(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public List<JavaClass> getLocal(String path) throws FileNotFoundException {
        return getEntries(path);
    }

    public List<JavaClass> getLocal() throws FileNotFoundException {
        return getEntries(defaultPath);
    }

    private List<JavaClass> getEntries(String path) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<JavaClassLocalEntity>(new FileReader(path))
                .withType(JavaClassLocalEntity.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
        var methods = new ArrayList<JavaClass>();
        beans.forEach(bean -> fromCsv(bean).ifPresent(methods::add));
        return methods;
    }

    private Optional<JavaClass> fromCsv(JavaClassLocalEntity bean) {
        var clazz = new JavaClass(
                bean.getAuthor().orElse(""),
                Path.of(bean.getParent()),
                Path.of(bean.getPath()),
                bean.getTime());
        bean.getOldPath().ifPresent(p -> clazz.setOldPath(Path.of(p)));
        return Optional.of(clazz);
    }


    public void saveLocal(List<JavaClass> entries, String path) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(entries, path);
    }

    public void saveLocal(List<JavaClass> entries) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(entries, defaultPath);
    }

    private void save(List<JavaClass> entries, String path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = entries.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<JavaClassLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        }
    }

    private JavaClassLocalEntity toCsv(JavaClass model) {
        var bean = new JavaClassLocalEntity();
        bean.setTime(model.getTime());
        model.getOldPath().ifPresent(model::setOldPath);
        bean.setPath(model.getPath().toString());
        bean.setParent(model.getParent().toString());
        model.getAuthor().ifPresent(bean::setAuthor);
        return bean;
    }
}
