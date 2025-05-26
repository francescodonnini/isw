package io.github.francescodonnini.data;

import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.LineRange;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.util.function.Predicate;

public class EditUtils {
    private EditUtils() {}

    public static boolean isTouched(JavaMethod m, EditList edits) {
        return isTouched(m, edits, _ -> true);
    }

    public static boolean isTouched(JavaMethod m, EditList edits, Predicate<Edit> predicate) {
        return edits.stream()
                .filter(predicate)
                .anyMatch(e -> overlaps(m, e));
    }

    private static boolean overlaps(JavaMethod m, Edit e) {
        e.extendB();
        var range = new LineRange(e.getBeginB(), e.getEndB());
        return m.getRange().intersects(range);
    }
}
