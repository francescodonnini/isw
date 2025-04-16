package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.entities.JavaMethodLocalEntity;
import io.github.francescodonnini.metrics.IntMetric;
import io.github.francescodonnini.metrics.LongMetric;
import io.github.francescodonnini.metrics.Metric;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvJavaMethodApi {
    private final String defaultPath;
    private final List<JavaClass> classes;

    public CsvJavaMethodApi(String defaultPath, List<JavaClass> classes) {
        this.defaultPath = defaultPath;
        this.classes = classes;
    }

    public List<JavaMethod> getLocal(String path) throws FileNotFoundException {
        return getEntries(path);
    }

    public List<JavaMethod> getLocal() throws FileNotFoundException {
        return getEntries(defaultPath);
    }

    private List<JavaMethod> getEntries(String path) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<JavaMethodLocalEntity>(new FileReader(path))
                .withType(JavaMethodLocalEntity.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
        var methods = new ArrayList<JavaMethod>();
        beans.forEach(bean -> fromCsv(bean).ifPresent(methods::add));
        return methods;
    }

    private Optional<JavaMethod> fromCsv(JavaMethodLocalEntity bean) {
        var o = classes.stream()
                .filter(c -> filter(c, bean))
                .findFirst()
                .flatMap(clazz -> Optional.of(new JavaMethod(
                        bean.isBuggy(),
                        clazz,
                        bean.getSignature(),
                        bean.getLineStart(),
                        bean.getLineEnd())));
        o.ifPresent(m -> addMetrics(m, bean));
        return o;
    }

    private void addMetrics(JavaMethod m, JavaMethodLocalEntity bean) {
        m.addMetric("lineOfCode", bean.getLineOfCode());
        m.addMetric("cyclomaticComplexity", bean.getCyclomaticComplexity());
        m.addMetric("parametersCount", bean.getParametersCount());
        m.addMetric("statementsCount", bean.getStatementsCount());
    }

    private boolean filter(JavaClass clazz, JavaMethodLocalEntity bean) {
        return clazz.getPath().equals(Path.of(bean.getClassPath()))
                && clazz.getRelease().id().equals(bean.getReleaseId());
    }

    public void saveLocal(List<JavaMethod> entries, String path) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(entries, path);
    }

    public void saveLocal(List<JavaMethod> entries) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(entries, defaultPath);
    }

    private void save(List<JavaMethod> entries, String path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = entries.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<JavaMethodLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        }
    }

    private JavaMethodLocalEntity toCsv(JavaMethod model) {
        var bean = new JavaMethodLocalEntity();
        bean.setBuggy(model.isBuggy());
        bean.setClassPath(model.getPath().toString());
        bean.setSignature(model.getSignature());
        bean.setReleaseId(model.getRelease().id());
        bean.setLineStart(model.getStartLine());
        bean.setLineEnd(model.getEndLine());
        model.getMetric("lineOfCode").flatMap(this::getLongMetric).ifPresent(bean::setLineOfCode);
        model.getMetric("cyclomaticComplexity").flatMap(this::getIntMetric).ifPresent(bean::setCyclomaticComplexity);
        model.getMetric("parametersCount").flatMap(this::getIntMetric).ifPresent(bean::setParametersCount);
        model.getMetric("statementsCount").flatMap(this::getLongMetric).ifPresent(bean::setStatementsCount);
        return bean;
    }

    private Optional<Integer> getIntMetric(Metric m) {
        if (m instanceof IntMetric im) {
            return Optional.of(im.getValue());
        }
        return Optional.empty();
    }

    private Optional<Long> getLongMetric(Metric m) {
        if (m instanceof LongMetric lm) {
            return Optional.of(lm.getValue());
        }
        return Optional.empty();
    }
}
