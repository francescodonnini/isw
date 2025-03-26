package io.github.francescodonnini.ast;

import com.sun.source.tree.MethodTree;

public class AstUtils {
    public static String getSignature(MethodTree method) {
        var rt = method.getReturnType();
        return "%s %s".formatted(rt == null ? "void" : rt.toString(), method.getName());
    }
}
