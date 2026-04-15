package io.github.francescodonnini.data.pmd;

import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.cpd.Mark;

import java.util.*;
import java.util.function.Consumer;

public class CPDConsumer implements Consumer<CPDReport> {
    private final Map<String, JavaClass> index = new HashMap<>();

    public CPDConsumer(List<JavaClass> classes) {
        classes.forEach(c -> this.index.put(c.getPath().toString(), c));
    }

    @Override
    public void accept(CPDReport report) {
        var duplicatedLinesMap = new HashMap<JavaMethod, Set<Integer>>();
        for (var match : report.getMatches()) {
            for (var mark : match.getMarkSet()) {
                var clsPath = mark.getLocation().getFileId().getOriginalPath();
                var cls = index.get(clsPath);
                if (cls != null) {
                    parseMark(cls, mark, duplicatedLinesMap);
                }
            }
        }

        for (var entry : duplicatedLinesMap.entrySet()) {
            var m = entry.getKey();
            var duplicatedLines = entry.getValue();
            var methodLen = m.getRange().length();
            if (methodLen > 0) {
                var ratio = Math.min(duplicatedLines.size() / (double) methodLen, 1.0);
                m.getMetrics().updateCodeDuplication(ratio);
            }
        }
    }

    private void parseMark(JavaClass cls, Mark mark, Map<JavaMethod, Set<Integer>> map) {
        var start = mark.getLocation().getStartLine();
        var end = mark.getLocation().getEndLine();
        for (var m : cls.getMethods()) {
            var mRange = m.getRange();
            if (start <= mRange.end() && end >= mRange.start()) {
                var overlapStart = Math.max(mRange.start(), start);
                var overlapEnd = Math.min(mRange.end(), end);
                var duplicatedLines = map.computeIfAbsent(m, unused -> new HashSet<>());
                for (var i = overlapStart; i <= overlapEnd; i++) {
                    duplicatedLines.add(i);
                }
            }
        }
    }
}