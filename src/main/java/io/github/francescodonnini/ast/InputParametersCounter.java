package io.github.francescodonnini.ast;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputParametersCounter extends TreeScanner<Void, Void> {
    public record MethodIPC(String name, int complexity) {}

    private final Map<String, Integer> ipc = new HashMap<>();

    public List<InputParametersCounter.MethodIPC> getInputParametersCount() {
        return ipc.entrySet().stream().map(e -> new InputParametersCounter.MethodIPC(e.getKey(), e.getValue())).toList();
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        ipc.put(AstUtils.getSignature(node), node.getParameters().size());
        return super.visitMethod(node, null);
    }
}
