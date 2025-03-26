package io.github.francescodonnini.ast;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineOfCode extends TreeScanner<Void, CompilationUnitTree> {
    public record MethodLOC(String name, Long loc) {}

    private final SourcePositions sourcePositions;
    private final Map<String, Long> locs = new HashMap<>();

    public LineOfCode(SourcePositions positions) {
        sourcePositions = positions;
    }

    public List<MethodLOC> getLOC() {
        return locs.entrySet().stream().map(e -> new MethodLOC(e.getKey(), e.getValue())).toList();
    }

    @Override
    public Void visitMethod(MethodTree node, CompilationUnitTree cu) {
        var loc = sourcePositions.getEndPosition(cu, node) - sourcePositions.getStartPosition(cu, node);
        locs.put(AstUtils.getSignature(node), loc);
        return super.visitMethod(node, cu);
    }
}
