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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsvJavaMethodApi {
    private String key(JavaClass clazz) {
        return String.format("%s%s%s", clazz.getPath(), clazz.getName(), clazz.getCommit());
    }

    private String key(JavaMethodLocalEntity clazz) {
        return String.format("%s%s%s", clazz.getClassPath(), clazz.getClassName(), clazz.getCommit());
    }

    public List<JavaMethod> getLocal(String path, List<JavaClass> classes) throws FileNotFoundException {
        return getEntries(path, classes.stream()
                .map(c -> Map.entry(key(c), c))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a)));
    }

    private List<JavaMethod> getEntries(String path, Map<String, JavaClass> classes) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<JavaMethodLocalEntity>(new FileReader(path))
                .withType(JavaMethodLocalEntity.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
        var methods = new ArrayList<JavaMethod>();
        beans.forEach(bean -> fromCsv(bean, classes).ifPresent(methods::add));
        return methods;
    }

    private Optional<JavaMethod> fromCsv(JavaMethodLocalEntity bean, Map<String, JavaClass> classes) {
        var o = Optional.ofNullable(classes.get(key(bean)));
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

    private void addMetrics(JavaMethod m, JavaMethodLocalEntity bean) {
        m.getMetrics().setCyclomaticComplexity(bean.getCyclomaticComplexity());
        m.getMetrics().setParametersCount(bean.getParametersCount());
        m.getMetrics().setLineOfCode(bean.getLineOfCode());
        m.getMetrics().setLocAdded(bean.getLocAdded());
        m.getMetrics().setMaxLocAdded(bean.getMaxLocAdded());
        m.getMetrics().setAvgLocAdded(bean.getAvgLocAdded());
        m.getMetrics().setLocDeleted(bean.getLocDeleted());
        m.getMetrics().setAvgLocDeleted(bean.getAvgLocDeleted());
        m.getMetrics().setMaxLocDeleted(bean.getMaxLocDeleted());
        m.getMetrics().setStatementsCount(bean.getStatementsCount());
        m.getMetrics().setStatementsAdded(bean.getStatementsAdded());
        m.getMetrics().setAvgStatementsAdded(bean.getAvgStatementsAdded());
        m.getMetrics().setMaxStatementsAdded(bean.getMaxStatementsAdded());
        m.getMetrics().setStatementsDeleted(bean.getStatementsDeleted());
        m.getMetrics().setAvgStatementsDeleted(bean.getAvgStatementsDeleted());
        m.getMetrics().setMaxStatementsDeleted(bean.getMaxStatementsDeleted());
        m.getMetrics().setNestingDepth(bean.getNestingDepth());
        m.getMetrics().setLocAdded(bean.getLocAdded());
        m.getMetrics().setLocDeleted(bean.getLocDeleted());
        m.getMetrics().setAvgChurn(bean.getAvgChurn());
        m.getMetrics().setChurn(bean.getChurn());
        m.getMetrics().setElseAdded(bean.getElseAdded());
        m.getMetrics().setElseDeleted(bean.getElseDeleted());
        m.getMetrics().setElseCount(bean.getElseCount());
        m.getMetrics().setStatementsAdded(bean.getStatementsAdded());
        m.getMetrics().setStatementsDeleted(bean.getStatementsDeleted());
        m.getMetrics().setMaxChurn(bean.getMaxChurn());
        m.getMetrics().setCodeSmells(bean.getCodeSmells());
        bean.getAuthors().forEach(a -> m.getMetrics().addAuthor(a));
        m.getMetrics().setCodeDuplcation(bean.getCodeDuplication());
    }

    public void saveLocal(List<JavaMethod> entries, String path) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(entries, path);
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
        bean.setClassName(model.getJavaClass().getName());
        bean.setCommit(model.getJavaClass().getCommit());
        bean.setLineStart(model.getStartLine());
        bean.setLineEnd(model.getEndLine());
        bean.setAvgLocAdded(model.getMetrics().getAvgLocAdded());
        bean.setMaxLocAdded(model.getMetrics().getMaxLocAdded());
        bean.setAvgLocDeleted(model.getMetrics().getAvgLocDeleted());
        bean.setMaxLocDeleted(model.getMetrics().getMaxLocDeleted());
        bean.setAvgStatementsAdded(model.getMetrics().getAvgStatementsAdded());
        bean.setMaxStatementsAdded(model.getMetrics().getMaxStatementsAdded());
        bean.setMaxStatementsDeleted(model.getMetrics().getMaxStatementsDeleted());
        bean.setAvgStatementsDeleted(model.getMetrics().getAvgStatementsDeleted());
        bean.setMaxChurn(model.getMetrics().getMaxChurn());
        bean.setLineOfCode(model.getMetrics().getLineOfCode());
        bean.setLocAdded(model.getMetrics().getLocAdded());
        bean.setLocDeleted(model.getMetrics().getLocDeleted());
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
        bean.setAuthors(new ArrayList<>(model.getMetrics().getAuthors()));
        bean.setCodeSmells(model.getMetrics().getCodeSmells());
        bean.setCodeDuplication(model.getMetrics().getCodeDuplication());
        return bean;
    }
}
