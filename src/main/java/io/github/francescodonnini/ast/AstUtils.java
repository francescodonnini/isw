package io.github.francescodonnini.ast;

import com.sun.source.tree.MethodTree;

import java.util.stream.Collectors;

public class AstUtils {
    private AstUtils() {}

    public static String getSignature(MethodTree method) {
        var rt = method.getReturnType();
        var params = method.getParameters()
                .stream()
                .map(param -> param.getType().toString())
                .collect(Collectors.joining(","));
        return "%s %s(%s)".formatted(rt == null ? "void" : rt.toString(), method.getName(), params);
    }


}
