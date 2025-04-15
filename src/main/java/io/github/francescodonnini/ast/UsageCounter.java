package io.github.francescodonnini.ast;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageCounter extends TreeScanner<Void, Void> {
    static class ReceiverClass extends TreeScanner<String, Void> {
        @Override
        public String visitMemberReference(MemberReferenceTree node, Void unused) {
            return super.visitMemberReference(node, unused);
        }
    }
    private final Map<String, List<String>> usages = new HashMap<>();
    private String currentMethod;

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        currentMethod = AstUtils.getSignature(node);
        return super.visitMethod(node, unused);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
        node.getMethodSelect().accept(this, unused);
        return super.visitMethodInvocation(node, unused);
    }
}
