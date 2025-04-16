package io.github.francescodonnini.ast;

import com.sun.source.tree.MethodTree;
import io.github.francescodonnini.model.JavaClass;

public class InputParametersCounter extends AbstractCounter {
    public InputParametersCounter() {
        super("parametersCount");
    }

    @Override
    public Void visitMethod(MethodTree node, JavaClass unused) {
        update(AstUtils.getSignature(node), node.getParameters().size());
        return super.visitMethod(node, null);
    }
}
