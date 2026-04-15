package io.github.francescodonnini.data.smell;

import io.github.francescodonnini.model.JavaClass;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CsvSmellLinker extends AbstractSmellLinker {
    private Map<String, Map<String, JavaClass>> index;

    public CsvSmellLinker(Path reportDirectory) {
        super(reportDirectory);
    }

    @Override
    protected void prepareIndex(List<JavaClass> classes) {
        index = new HashMap<>();
        for (var c : classes) {
            var classSet = index.computeIfAbsent(c.getCommit(), s -> new HashMap<>());
            classSet.put(c.getPath().toString(), c);
        }
    }

    @Override
    protected void processReport(String fileName, List<CsvReportEntity> entities) {
        var commit = fileName.replace(".csv", "");
        var commitClasses = index.getOrDefault(commit, Map.of());
        entities.forEach(e -> link(e, commitClasses));
    }
}
