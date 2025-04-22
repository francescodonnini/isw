package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.entities.JavaMethodLocalEntity;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.LineRange;
import io.github.francescodonnini.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CsvJavaMethodApi {
    private final String defaultPath;
    private final Map<String, JavaClass> classMap = new HashMap<>();

    public CsvJavaMethodApi(String defaultPath, List<JavaClass> classes) {
        this.defaultPath = defaultPath;
        classes.forEach(c -> classMap.put(key(c), c));
    }

    private String key(JavaClass clazz) {
        return String.format("%s%s", clazz.getPath(), clazz.getCommit());
    }

    private String key(JavaMethodLocalEntity clazz) {
        return String.format("%s%s", clazz.getClassPath(), clazz.getCommit());
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
        var o = getJavaClass(bean);
        if (o.isEmpty()) {
            return Optional.empty();
        }
        var m = new JavaMethod(
            bean.isBuggy(),
            o.get(),
            bean.getSignature(),
            new LineRange(bean.getLineStart(), bean.getLineEnd()));
        addMetrics(m, bean);
        return Optional.of(m);
    }

    private Optional<JavaClass> getJavaClass(JavaMethodLocalEntity bean) {
        return Optional.ofNullable(classMap.get(key(bean)));
    }

    private void addMetrics(JavaMethod m, JavaMethodLocalEntity bean) {
        m.getMetrics().setLineOfCode(bean.getLineOfCode());
        m.getMetrics().setCyclomaticComplexity(bean.getCyclomaticComplexity());
        m.getMetrics().setParametersCount(bean.getParametersCount());
        m.getMetrics().setStatementsCount(bean.getStatementsCount());
        m.getMetrics().setStatementsCount(bean.getStatementsCount());
        m.getMetrics().setNestingDepth(bean.getNestingDepth());
        m.getMetrics().setLocAdded(bean.getLocAdded());
        m.getMetrics().setLocDeleted(bean.getLocDeleted());
        m.getMetrics().setAvgChurn(bean.getAvgChurn());
        m.getMetrics().setChurn(bean.getChurn());
        m.getMetrics().setElseAdded(bean.getElseAdded());
        m.getMetrics().setElseDeleted(bean.getElseDeleted());
        m.getMetrics().setStatementsAdded(bean.getStatementsAdded());
        m.getMetrics().setStatementsDeleted(bean.getStatementsDeleted());
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
        bean.setCommit(model.getJavaClass().getCommit());
        bean.setLineStart(model.getStartLine());
        bean.setLineEnd(model.getEndLine());
        bean.setLineOfCode(model.getMetrics().getLineOfCode());
        bean.setCyclomaticComplexity(model.getMetrics().getCyclomaticComplexity());
        bean.setParametersCount(model.getMetrics().getParametersCount());
        bean.setStatementsCount(model.getMetrics().getStatementsCount());
        bean.setElseCount(model.getMetrics().getElseCount());
        bean.setNestingDepth(model.getMetrics().getNestingDepth());
        bean.setElseAdded(model.getMetrics().getElseAdded());
        bean.setElseDeleted(model.getMetrics().getElseDeleted());
        bean.setStatementsAdded(model.getMetrics().getStatementsAdded());
        bean.setStatementsDeleted(model.getMetrics().getStatementsDeleted());
        bean.setChurn(model.getMetrics().getChurn());
        bean.setAvgChurn(model.getMetrics().getAvgChurn());
        return bean;
    }
}
