package io.github.francescodonnini.weka.arff;

import io.github.francescodonnini.model.ReleaseJavaClass;
import io.github.francescodonnini.utils.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class JavaMethodArffSerializer {
    public void toArff(Path path, List<ReleaseJavaClass> classes) throws IOException {
        FileUtils.createDirectory(path.getParent());
        try (var off = new FileWriter(path.toFile())) {
            off.write("@relation classes\n\n");
            buggyAttribute(off);
            numericAttribute(off, "cyclomatic_complexity");
            numericAttribute(off, "loc");
            numericAttribute(off, "smell_count");
            numericAttribute(off, "stmt_count");
            numericAttribute(off, "nesting_depth");
            numericAttribute(off, "fan_in");
            numericAttribute(off, "fan_out");
            numericAttribute(off, "lcom");
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
            numericAttribute(off, "avg_change_time");
            numericAttribute(off, "entropy");
            numericAttribute(off, "entropy_avg");
            numericAttribute(off, "entropy_max");
            numericAttribute(off, "release");
            off.write("@DATA\n");
            for (var c : classes) {
                row(off, c);
            }
        }
    }

    private void buggyAttribute(Writer writer) throws IOException {
        writer.write("@ATTRIBUTE buggy\t\t{0,1}\n");
    }

    private void numericAttribute(Writer writer, String name) throws IOException {
        writer.write("@ATTRIBUTE %s\t\tNUMERIC%n".formatted(name));
    }

    private void row(Writer writer, ReleaseJavaClass c) throws IOException {
        var complexity = c.getComplexityMetrics();
        var process = c.getProcessMetrics();
        var s = (c.isBuggy() ? "1" : "0") + "," +
                complexity.getCc() + "," +
                complexity.getLoc() + "," +
                complexity.getSmellCount() + "," +
                complexity.getStatementCount() + "," +
                complexity.getNestingDepth() + "," +
                complexity.getFanIn() + "," +
                complexity.getFanOut() + "," +
                complexity.getCohesion() + "," +
                process.getChurn() + "," +
                process.getAvgChurn() + "," +
                process.getMaxChurn() + "," +
                process.getLocAdded() + "," +
                process.getAvgLocAdded() + "," +
                process.getMaxLocAdded() + "," +
                process.getNumOfRevisions() + "," +
                process.getNumOfFixes() + "," +
                process.getNumOfAuthors() + "," +
                process.getChangeSet() + "," +
                process.getAvgChangeSet() + "," +
                process.getMaxChangeSet() + "," +
                process.getAge().toDays() + "," +
                process.getAverageChangeTime().toDays() + "," +
                process.getEntropy() + "," +
                process.getAvgEntropy() + "," +
                process.getMaxEntropy() + "," +
                c.getOrder() + "\n";
        writer.write(s);
    }
}