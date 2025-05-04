package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import io.github.francescodonnini.model.JavaClass;

import java.util.ArrayList;
import java.util.List;

public class CodeDuplicationCounter extends AbstractCounter {
    private final List<Tree.Kind> flatTree = new ArrayList<>();

    public List<Tree.Kind> getFlatTree() {
        return flatTree;
    }

    @Override
    public Void scan(Tree tree, JavaClass javaClass) {
        flatTree.add(tree.getKind());
        return super.scan(tree, javaClass);
    }

    @Override
    public Void visitMethod(MethodTree node, JavaClass javaClass) {
        scan(node, javaClass);
        return super.visitMethod(node, javaClass);
    }
}
