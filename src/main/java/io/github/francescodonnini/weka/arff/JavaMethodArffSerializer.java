package io.github.francescodonnini.weka.arff;

import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.ReleaseJavaClass;
import io.github.francescodonnini.utils.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class JavaMethodArffSerializer {
    public void toArff(Path path, List<Release> releases, List<ReleaseJavaClass> classes) throws IOException {
        FileUtils.createDirectory(path.getParent());
        try (var off = new FileWriter(path.toFile())) {
            off.write("@relation methods\n\n");
            buggyAttribute(off);
            numericAttribute(off, "cyclomatic_complexity");
            numericAttribute(off, "loc");
            numericAttribute(off, "smell_count");
            numericAttribute(off, "stmt_count");
            numericAttribute(off, "nesting_depth");
            numericAttribute(off, "churn");
            numericAttribute(off, "churn_avg");
            numericAttribute(off, "churn_max");
            numericAttribute(off, "loc_added");
            numericAttribute(off, "loc_added_avg");
            numericAttribute(off, "loc_added_max");
            numericAttribute(off, "num_of_revisions");
            numericAttribute(off, "num_of_fixes");
            numericAttribute(off, "num_of_authors");
            numericAttribute(off, "change_set");
            numericAttribute(off, "change_set_avg");
            numericAttribute(off, "change_set_max");
            numericAttribute(off, "age");
            numericAttribute(off, "release");
            off.write("@DATA\n");
            for (var i = 0; i < releases.size(); i++) {
                final var r = releases.get(i);
                List<ReleaseJavaClass> current;
                if (i == 0) {
                    current = classes.stream()
                            .filter(m -> between(m, null, r))
                            .toList();
                } else {
                    final var last = releases.get(i - 1);
                    current = classes.stream()
                            .filter(m -> between(m, last, r))
                            .toList();
                }
                for (var m : current) {
                    row(off, m, r);
                }
            }
        }
    }

    private void buggyAttribute(Writer writer) throws IOException {
        writer.write("@ATTRIBUTE buggy\t\t{0,1}\n");
    }

    private void numericAttribute(Writer writer, String name) throws IOException {
        writer.write("@ATTRIBUTE %s\t\tNUMERIC%n".formatted(name));
    }

    private boolean between(ReleaseJavaClass c, Release last, Release current) {
        if (last == null) {
            return !c.getTime().isAfter(current.releaseDate().atStartOfDay());
        } else {
            return c.getTime().isAfter(last.releaseDate().atStartOfDay())
                    && !c.getTime().isAfter(current.releaseDate().atStartOfDay());
        }
    }

    private void row(Writer writer, ReleaseJavaClass c, Release r) throws IOException {
        var complexity = c.getComplexityMetrics();
        var process = c.getProcessMetrics();
        var s = new StringBuilder()
                .append(c.isBuggy() ? "1" : "0").append(",")
                .append(complexity.getCc()).append(",")
                .append(complexity.getLoc()).append(",")
                .append(complexity.getSmellCount()).append(",")
                .append(complexity.getStatementCount()).append(",")
                .append(complexity.getNestingDepth()).append(",")
                .append(process.getChurn()).append(",")
                .append(process.getAvgChurn()).append(",")
                .append(process.getMaxChurn()).append(",")
                .append(process.getLocAdded()).append(",")
                .append(process.getAvgLocAdded()).append(",")
                .append(process.getMaxLocAdded()).append(",")
                .append(process.getNumOfRevisions()).append(",")
                .append(process.getNumOfFixes()).append(",")
                .append(process.getNumOfAuthors()).append(",")
                .append(process.getChangeSet()).append(",")
                .append(process.getAvgChangeSet()).append(",")
                .append(process.getMaxChangeSet()).append(",")
                .append(process.getAge().toDays()).append(",")
                .append(r.order()).append("\n")
                .toString();
        writer.write(s);
    }
}
