package io.github.francescodonnini.weka;

import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class JavaMethodArffSerializer {
    private final Path parent;

    public JavaMethodArffSerializer(Path parent) {
        this.parent = parent;
    }

    public void toArff(List<Release> releases, List<JavaMethod> methods) throws IOException {
        FileUtils.createDirectory(parent.toString());
        try (var off = new FileWriter(parent.resolve("methods.arff").toFile())) {
            off.write("@relation methods\n\n");
            booleanAttribute(off, "buggy");
            numericAttribute(off, "cyclomatic_complexity");
            numericAttribute(off, "parameters_count");
            numericAttribute(off, "loc");
            numericAttribute(off, "loc_added");
            numericAttribute(off, "loc_added_max");
            numericAttribute(off, "loc_added_avg");
            numericAttribute(off, "loc_deleted");
            numericAttribute(off, "loc_deleted_max");
            numericAttribute(off, "loc_deleted_avg");
            numericAttribute(off, "stmt_count");
            numericAttribute(off, "stmt_added");
            numericAttribute(off, "stmt_added_max");
            numericAttribute(off, "stmt_added_avg");
            numericAttribute(off, "stmt_deleted");
            numericAttribute(off, "stmt_deleted_max");
            numericAttribute(off, "stmt_deleted_avg");
            numericAttribute(off, "churn");
            numericAttribute(off, "churn_max");
            numericAttribute(off, "churn_avg");
            numericAttribute(off, "nesting_depth");
            numericAttribute(off, "else_count");
            numericAttribute(off, "else_added");
            numericAttribute(off, "else_deleted");
            numericAttribute(off, "authors_count");
            numericAttribute(off, "smell_count");
            numericAttribute(off, "duplication");
            numericAttribute(off, "release");
            off.write("@DATA\n");
            var releaseNumber = 0;
            for (var i = 0; i < releases.size(); i++) {
                final var r = releases.get(i);
                List<JavaMethod> current;
                if (i == 0) {
                    current = methods.stream()
                            .filter(m -> between(m, null, r))
                            .toList();
                } else {
                    final var last = releases.get(i - 1);
                    current = methods.stream()
                            .filter(m -> between(m, last, r))
                            .toList();
                }
                for (var m : current) {
                    row(off, m, r);
                }
            }
        }
    }

    private void booleanAttribute(Writer writer, String name) throws IOException {
        writer.write("@ATTRIBUTE %s\t\t{0,1}\n".formatted(name));
    }

    private void numericAttribute(Writer writer, String name) throws IOException {
        writer.write("@ATTRIBUTE %s\t\tNUMERIC\n".formatted(name));
    }

    private boolean between(JavaMethod method, Release last, Release current) {
        if (last == null) {
            return !method.getJavaClass().getTime().isAfter(current.releaseDate().atStartOfDay());
        } else {
            return method.getJavaClass().getTime().isAfter(last.releaseDate().atStartOfDay())
                    && !method.getJavaClass().getTime().isAfter(current.releaseDate().atStartOfDay());
        }
    }

    private void row(Writer writer, JavaMethod m, Release r) throws IOException {
        var metrics = m.getMetrics();
        var s = new StringBuilder()
                .append(m.isBuggy() ? "1" : "0").append(",")
                .append(metrics.getCyclomaticComplexity()).append(",")
                .append(metrics.getParametersCount()).append(",")
                .append(metrics.getLineOfCode()).append(",")
                .append(metrics.getLocAdded()).append(",")
                .append(metrics.getMaxLocAdded()).append(",")
                .append(metrics.getAvgLocAdded()).append(",")
                .append(metrics.getLocDeleted()).append(",")
                .append(metrics.getMaxLocDeleted()).append(",")
                .append(metrics.getAvgLocDeleted()).append(",")
                .append(metrics.getStatementsCount()).append(",")
                .append(metrics.getStatementsAdded()).append(",")
                .append(metrics.getMaxStatementsAdded()).append(",")
                .append(metrics.getAvgStatementsAdded()).append(",")
                .append(metrics.getStatementsDeleted()).append(",")
                .append(metrics.getMaxStatementsDeleted()).append(",")
                .append(metrics.getAvgStatementsDeleted()).append(",")
                .append(metrics.getChurn()).append(",")
                .append(metrics.getMaxChurn()).append(",")
                .append(metrics.getAvgChurn()).append(",")
                .append(metrics.getNestingDepth()).append(",")
                .append(metrics.getElseCount()).append(",")
                .append(metrics.getElseAdded()).append(",")
                .append(metrics.getElseDeleted()).append(",")
                .append(metrics.getAuthorsCount()).append(",")
                .append(metrics.getCodeSmells()).append(",")
                .append(metrics.getCodeDuplication()).append(",")
                .append(r.order()).append("\n")
                .toString();
        writer.write(s);
    }
}
