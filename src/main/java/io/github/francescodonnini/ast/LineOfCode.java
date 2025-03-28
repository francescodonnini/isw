package io.github.francescodonnini.ast;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class LineOfCode extends TreeScanner<Void, CompilationUnitTree> {
    public record MethodLOC(String name, Long loc) {}

    private final Logger LOGGER = Logger.getLogger(LineOfCode.class.getName());
    private final Map<String, Long> locs = new HashMap<>();

    public LineOfCode() {
    }

    public List<MethodLOC> getLOC() {
        return locs.entrySet().stream().map(e -> new MethodLOC(e.getKey(), e.getValue())).toList();
    }

    @Override
    public Void visitMethod(MethodTree node, CompilationUnitTree cu) {
        var body = node.getBody();
        var loc = 1L;
        if (body != null) {
            try (var ln = new LineNumberReader(new StringReader(body.toString()))) {
                long skipped;
                do {
                    skipped = ln.skip(Long.MAX_VALUE);
                } while (skipped > 0);
                loc = ln.getLineNumber();
            } catch (IOException e) {
                LOGGER.info(e.getMessage());
            }
        }
        locs.put(AstUtils.getSignature(node), loc);
        return super.visitMethod(node, cu);
    }
}
