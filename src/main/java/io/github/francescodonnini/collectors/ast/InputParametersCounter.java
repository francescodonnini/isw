package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.MethodTree;
import io.github.francescodonnini.model.JavaClass;

public class InputParametersCounter extends AbstractCounter {
    @Override
    public Void visitMethod(MethodTree node, JavaClass unused) {
        update(AstUtils.getSignature(node), m -> {
            m.setParametersCount(node.getParameters().size());
            return null;
        });
        return super.visitMethod(node, null);
    }
}
