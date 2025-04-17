package io.github.francescodonnini.collectors.ast;

import com.sun.source.tree.MethodTree;

import java.util.Objects;
import java.util.stream.Collectors;

public class AstUtils {
    private AstUtils() {}

    public static String getSignature(MethodTree method) {
        var s = new StringBuilder();
        var typeParameters = method.getTypeParameters();
        if (!typeParameters.isEmpty()) {
            s.append("<");
            s.append(typeParameters
                    .stream()
                    .map(tp -> {
                       var bounds = tp.getBounds()
                               .stream()
                               .map(Objects::toString)
                               .filter(b -> !b.equals("java.lang.Object"))
                               .collect(Collectors.joining(" & "));
                       var sb = new StringBuilder();
                       sb.append(tp.getName());
                       if (!bounds.isEmpty()) {
                           sb.append(" extends ");
                           sb.append(bounds);
                       }
                       return sb.toString();
                    }).collect(Collectors.joining(",")));
            s.append("> ");
        }
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
