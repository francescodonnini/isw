package io.github.francescodonnini.ast;

import com.sun.source.tree.MethodTree;

import java.util.stream.Collectors;

public class AstUtils {
    private AstUtils() {}

    public static String getSignature(MethodTree method) {
        var s = new StringBuilder();
        if (!method.getName().contentEquals("<init>")) {
            var rt = method.getReturnType();
            if (rt != null) {
                s.append(rt);
            } else {
                s.append("void");
            }
            s.append(" ");
        }
        s.append(method.getName());
        s.append("(");
        var params = method.getParameters()
                .stream()
                .map(param -> param.getType().toString())
                .collect(Collectors.joining(","));
        s.append(params);
        s.append(")");
        return s.toString();
    }
}
