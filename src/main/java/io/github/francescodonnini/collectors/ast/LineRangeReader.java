package io.github.francescodonnini.collectors.ast;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import io.github.francescodonnini.model.LineRange;

import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class LineRangeReader extends TreeScanner<Void, Void> {
    private final String source;

    public LineRangeReader(String source) {
        this.source = source;
    }

    public Map<String, LineRange> read() throws IOException {
        var map = new HashMap<String, LineRange>();
        var file = Files.createTempFile(null, ".java").toFile();
        var compiler = ToolProvider.getSystemJavaCompiler();
        var units = compiler
            .getStandardFileManager(null, null, null)
            .getJavaFileObjects(file);
        var task = (JavacTask) compiler.getTask(null, null, null, null, null, units);
        for (var cu : task.parse()) {

        }
        return map;
    }
}
