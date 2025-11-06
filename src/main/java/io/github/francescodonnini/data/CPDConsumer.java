package io.github.francescodonnini.data;

import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.LineRange;
import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.cpd.Mark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class CPDConsumer implements Consumer<CPDReport> {
    private final Map<String, JavaClass> index = new HashMap<>();

    CPDConsumer(List<JavaClass> classes) {
        classes.forEach(c -> this.index.put(c.getAbsolutePath().toString(), c));
    }

    @Override
    public void accept(CPDReport report) {
        for (var match : report.getMatches() ) {
            for (var mark : match.getMarkSet()) {
                updateCodeDuplication(mark);
            }
        }
    }

    private void updateCodeDuplication(Mark mark) {
        var clazz = index.get(mark.getLocation().getFileId().getAbsolutePath());
        if (clazz == null) {
            return;
        }
        var clone = new LineRange(mark.getLocation().getStartLine(), mark.getLocation().getEndLine());
        for (var m : clazz.getMethods()) {
            if (clone.contains(m.getRange())) {
                m.getMetrics().updateCodeDuplication(1.0);
            } else if (m.getRange().contains(clone)) {
                m.getMetrics().updateCodeDuplication((double) clone.length() / m.getRange().length());
            } else if (m.getRange().intersects(clone)) {
                var start = Math.max(m.getRange().start(), clone.start());
                var end = Math.min(m.getRange().end(), clone.end());
                m.getMetrics().updateCodeDuplication((double) (end - start) / m.getRange().length());
            }
        }
    }
}