package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import io.github.francescodonnini.model.RevisionJavaClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CohesionCounter extends AbstractCounter {
    private final Set<String> classFields = new HashSet<>();
    private final Map<String, Set<String>> methodFieldAccesses = new HashMap<>();
    private final Set<String> localVariables = new HashSet<>();
    private String currentMethod;

    @Override
    public Void visitClass(ClassTree node, RevisionJavaClass javaClass) {
        for (var member : node.getMembers()) {
            if (member instanceof VariableTree varNode) {
                classFields.add(varNode.getName().toString());
            }
        }
        var unused = super.visitClass(node, javaClass);
        var cohesion = calculateCohesion();
        javaClass.getMetrics().setCohesion(cohesion);
        return unused;
    }

    @Override
    public Void visitMethod(MethodTree node, RevisionJavaClass revisionJavaClass) {
        var signature = AstUtils.getSignature(node);
        if (signature.contains("<init>")) {
            return super.visitMethod(node, revisionJavaClass);
        }

        var isTopLevelMethod = currentMethod == null;
        if (isTopLevelMethod) {
            currentMethod = signature;
            localVariables.clear();
            methodFieldAccesses.putIfAbsent(signature, new HashSet<>());
        }

        var unused = super.visitMethod(node, revisionJavaClass);

        if (isTopLevelMethod) {
            currentMethod = null;
            localVariables.clear();
        }
        return unused;
    }

    @Override
    public Void visitVariable(VariableTree node, RevisionJavaClass revisionJavaClass) {
        if (currentMethod != null) {
            localVariables.add(node.getName().toString());
        }
        return super.visitVariable(node, revisionJavaClass);
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, RevisionJavaClass revisionJavaClass) {
        trackAccess(node.getName().toString());
        return super.visitIdentifier(node, revisionJavaClass);
    }

    @Override
    public Void visitMemberSelect(MemberSelectTree node, RevisionJavaClass revisionJavaClass) {
        trackAccess(node.getIdentifier().toString());
        return super.visitMemberSelect(node, revisionJavaClass);
    }

    private void trackAccess(String ident) {
        if (currentMethod != null
            && classFields.contains(ident)
            && !localVariables.contains(ident)) {
            methodFieldAccesses.get(currentMethod).add(ident);
        }
    }

    private int calculateCohesion() {
        var p = 0;
        var q = 0;
        var accesses = new ArrayList<>(methodFieldAccesses.values());
        for (var i = 0; i < accesses.size(); i++) {
            for (var j = i + 1; j < accesses.size(); j++) {
                var methodA = accesses.get(i);
                var methodB = accesses.get(j);
                var sharedFields = new HashSet<>(methodA);
                sharedFields.retainAll(methodB);
                if (sharedFields.isEmpty()) {
                    p++;
                } else {
                    q++;
                }
            }
        }
        return Math.max(0, p - q);
    }

    @Override
    public void reset() {
        classFields.clear();
        methodFieldAccesses.clear();
        localVariables.clear();
        currentMethod = null;
    }
}
