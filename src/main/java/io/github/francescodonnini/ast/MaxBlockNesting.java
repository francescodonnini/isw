package io.github.francescodonnini.ast;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaxBlockNesting extends TreeScanner<Void, Void> {
    public record MaxBlockNestingMethod(String name, int maxBlockNesting) {}

    private final Map<String, Integer> maxNesting = new HashMap<>();

    public List<MaxBlockNestingMethod> getMethods() {
        return maxNesting.entrySet().stream().map(e -> new MaxBlockNestingMethod(e.getKey(), e.getValue())).toList();
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        return super.visitMethod(node, unused);
    }
}
