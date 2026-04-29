package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.entities.ClassComplexityMetricsLocalEntity;
import io.github.francescodonnini.csv.entities.ClassProcessMetricsLocalEntity;
import io.github.francescodonnini.csv.entities.ReleaseClassLocalEntity;
import io.github.francescodonnini.csv.entities.RevisionClassLocalEntity;
import io.github.francescodonnini.model.ComplexityClassMetrics;
import io.github.francescodonnini.model.ProcessClassMetrics;
import io.github.francescodonnini.model.ReleaseJavaClass;
import io.github.francescodonnini.model.RevisionJavaClass;
import io.github.francescodonnini.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CsvJavaClassApi {
    public List<RevisionJavaClass> getRevisionClasses(String path) throws FileNotFoundException {
        return getEntries(path, RevisionClassLocalEntity.class, this::from);
    }
    public List<ReleaseJavaClass> getReleaseClasses(String path) throws FileNotFoundException {
        return getEntries(path, ReleaseClassLocalEntity.class, this::from);
    }

    private <E, T> List<T> getEntries(
            String path,
            Class<E> beanClass,
            Function<E, T> factory) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<E>(new FileReader(path))
                .withType(beanClass)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
        var methods = new ArrayList<T>();
        beans.forEach(bean -> methods.add(factory.apply(bean)));
        return methods;
    }

    private ReleaseJavaClass from(ReleaseClassLocalEntity bean) {
        var c = ReleaseJavaClass.builder()
                .name(bean.getName())
                .path(Path.of(bean.getPath()))
                .order(bean.getOrder())
                .time(bean.getTime())
                .commits(bean.getCommits())
                .complexity(from(bean.getComplexityMetrics()))
                .process(from(bean.getProcessMetrics()))
                .build();
        c.setBuggy(bean.isBuggy());
        return c;
    }

    private ComplexityClassMetrics from(ClassComplexityMetricsLocalEntity bean) {
        var c = new ComplexityClassMetrics();
        c.setLoc(bean.getLoc());
        c.setCc(bean.getCc());
        c.setElseCount(bean.getElseCount());
        c.setNestingDepth(bean.getNestingDepth());
        c.setStatementCount(bean.getStatementCount());
        c.setChangeSetSize(bean.getChangeSetSize());
        for (int i = 0; i < bean.getSmellCount(); ++i) {
            c.incSmellCount();
        }
        return c;
    }

    private ProcessClassMetrics from(ClassProcessMetricsLocalEntity bean) {
        var c = new ProcessClassMetrics();
        c.setLocTouched(bean.getLocTouched());
        c.setNumOfRevisions(bean.getNumOfRevisions());
        c.setNumOfFixes(bean.getNumOfFixes());
        c.setNumOfAuthors(bean.getNumOfAuthors());
        c.setLocAdded(bean.getLocAdded());
        c.setMaxLocAdded(bean.getMaxLocAdded());
        c.setAvgLocAdded(bean.getAvgLocAdded());
        c.setChurn(bean.getChurn());
        c.setMaxChurn(bean.getMaxChurn());
        c.setAvgChurn(bean.getAvgChurn());
        c.setChangeSet(bean.getChangeSet());
        c.setMaxChangeSet(bean.getMaxChangeSet());
        c.setAvgChangeSet(bean.getAvgChangeSet());
        c.setAge(Duration.ofDays(bean.getAgeDays()));
        c.setAverageChangeTime(Duration.ofDays(bean.getAvgChangeTimeDays()));
        return c;
    }

    private RevisionJavaClass from(RevisionClassLocalEntity bean) {
        var r = RevisionJavaClass.builder()
                .name(bean.getName())
                .path(Path.of(bean.getPath()))
                .topLevel(bean.isTopLevel())
                .metrics(from(bean.getMetrics()))
                .build();
        r.setTime(bean.getTime());
        r.setCommit(bean.getCommit());
        r.setTrackingId(bean.getTrackingId());
        bean.getAuthor().ifPresent(r::setAuthor);
        return r;
    }

    public void saveReleaseClasses(String path, List<ReleaseJavaClass> classes) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(path, classes, this::to);
    }

    public void saveRevisionClasses(String path, List<RevisionJavaClass> classes) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(path, classes, this::to);
    }

    private <T, E> void save(String path, List<T> entries, Function<T, E> factory) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = entries.stream()
                .map(factory)
                .toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var builder = new StatefulBeanToCsvBuilder<E>(writer).build();
            for (var b : beans) {
                builder.write(b);
            }
        }
    }

    private ReleaseClassLocalEntity to(ReleaseJavaClass c) {
        var bean = new ReleaseClassLocalEntity();
        bean.setOrder(c.getOrder());
        bean.setPath(c.getPath().toString());
        bean.setName(c.getName());
        bean.setTime(c.getTime());
        bean.setBuggy(c.isBuggy());
        bean.setComplexityMetrics(to(c.getComplexityMetrics()));
        bean.setProcessMetrics(to(c.getProcessMetrics()));
        bean.setCommits(c.getCommits());
        return bean;
    }

    private ClassComplexityMetricsLocalEntity to(ComplexityClassMetrics c) {
        var bean = new ClassComplexityMetricsLocalEntity();
        bean.setLoc(c.getLoc());
        bean.setCc(c.getCc());
        bean.setElseCount(c.getElseCount());
        bean.setNestingDepth(c.getNestingDepth());
        bean.setStatementCount(c.getStatementCount());
        bean.setSmellCount(c.getSmellCount());
        bean.setChangeSetSize(c.getChangeSetSize());
        return bean;
    }

    private ClassProcessMetricsLocalEntity to(ProcessClassMetrics c) {
        var bean = new ClassProcessMetricsLocalEntity();
        bean.setLocTouched(c.getLocTouched());
        bean.setNumOfRevisions(c.getNumOfRevisions());
        bean.setNumOfFixes(c.getNumOfFixes());
        bean.setNumOfAuthors(c.getNumOfAuthors());
        bean.setLocAdded(c.getLocAdded());
        bean.setMaxLocAdded(c.getMaxLocAdded());
        bean.setAvgLocAdded(c.getAvgLocAdded());
        bean.setChurn(c.getChurn());
        bean.setMaxChurn(c.getMaxChurn());
        bean.setAvgChurn(c.getAvgChurn());
        bean.setChangeSet(c.getChangeSet());
        bean.setMaxChangeSet(c.getMaxChangeSet());
        bean.setAvgChangeSet(c.getAvgChangeSet());
        bean.setAgeDays(c.getAge().toDays());
        bean.setAvgChangeTimeDays(c.getAverageChangeTime().toDays());
        return bean;
    }

    private RevisionClassLocalEntity to(RevisionJavaClass c) {
        var bean = new RevisionClassLocalEntity();
        bean.setName(c.getName());
        bean.setBuggy(false);
        bean.setMetrics(to(c.getMetrics()));
        bean.setTime(c.getTime());
        bean.setTopLevel(c.isTopLevel());
        bean.setPath(c.getPath().toString());
        bean.setTrackingId(c.getTrackingId());
        c.getAuthor().ifPresent(bean::setAuthor);
        bean.setCommit(c.getCommit());
        return bean;
    }
}
