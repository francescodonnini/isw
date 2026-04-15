package io.github.francescodonnini.data.smell;

import io.github.francescodonnini.model.JavaClass;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneShotSmellLinker extends AbstractSmellLinker {
    private Map<String, JavaClass> index;

    public OneShotSmellLinker(Path reportsDir) {
        super(reportsDir);
    }

    @Override
    protected void prepareIndex(List<JavaClass> classes) {
        index = new HashMap<>();
        for (var c : classes) {
            index.put(c.getPath().toString(), c);
        }
    }

    @Override
    protected void processReport(String fileName, List<CsvReportEntity> entities) {
        entities.forEach(e -> link(e, index));
    }
}
