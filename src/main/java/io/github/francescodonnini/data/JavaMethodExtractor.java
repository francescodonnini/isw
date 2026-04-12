package io.github.francescodonnini.data;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import io.github.francescodonnini.collectors.ast.AbstractCounter;
import io.github.francescodonnini.collectors.ast.AstUtils;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.LineRange;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaMethodExtractor extends TreeScanner<Void, Void> {
    private final Logger logger = Logger.getLogger(JavaMethodExtractor.class.getName());
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private CompilationUnitTree compilationUnit;
    private SourcePositions sourcePositions;
    private final List<JavaClass> classes = new ArrayList<>();
    private ParseContext context;
    private JavaClass currentClass;
    private int depth = 0;
    private final List<AbstractCounter> counters;

    public JavaMethodExtractor(List<AbstractCounter> counters) {
        this.counters = counters;
    }

    public List<JavaClass> getClasses() {
        return new ArrayList<>(classes);
    }

    public void parse(ParseContext context) throws IOException {
        this.context = context;
        var file = new InMemoryFile(context.path().toString(), context.content());
        var task = (JavacTask) compiler.getTask(null, null, null, null, null, List.of(file));
        setSourcePositions(Trees.instance(task).getSourcePositions());
        for (var cu : task.parse()) {
            setCompilationUnit(cu);
            cu.accept(this, null);
        }
    }

    private void setSourcePositions(SourcePositions sourcePositions) {
        this.sourcePositions = sourcePositions;
    }

    private void setCompilationUnit(CompilationUnitTree cu) {
        this.compilationUnit = cu;
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void unused) {
        if (isAnonymousClass(node)) {
            depth++;
            var r = super.visitNewClass(node, unused);
            depth--;
            return r;
        }
        return super.visitNewClass(node, unused);
    }

    private boolean isAnonymousClass(NewClassTree node) {
        return node.getClassBody() != null;
    }

    @Override
    public Void visitClass(ClassTree node, Void unused) {
        if (isGenerated(node)) {
            logger.log(Level.INFO, () -> "skip class %s because it is generated (commit %s)".formatted(context.path(), context.commit().substring(0, 6)));
            return null;
        }
        if (isNamedClass(node)) {
            var parent = currentClass;
            setNamedClass(node, isPrimary(node));
            var r = super.visitClass(node, unused);
            classes.add(currentClass);
            collectMetrics(node, currentClass);
            currentClass = parent;
            return r;
        }
        return null;
    }

    private boolean isPrimary(ClassTree node) {
        var fileName = context.getAbsolutePath()
                .getFileName()
                .toString();
        var n = fileName.lastIndexOf('.');
        fileName = n >= 0 ? fileName.substring(0, n) : fileName;
        return currentClass == null && node.getSimpleName().toString().equals(fileName);
    }

    private boolean isGenerated(ClassTree node) {
        for (var annotation : node.getModifiers().getAnnotations()) {
            var annotationType = annotation.getAnnotationType();
            if (annotationType.toString().matches("(?i)generated\\b")) {
                return true;
            }
        }
        return false;
    }

    private void setNamedClass(ClassTree currentClass, boolean primary) {
        this.currentClass = JavaClass.builder()
                .trackingId(context.trackingId())
                .commit(context.commit())
                .parent(context.parent())
                .path(context.path())
                .name(currentClass.getSimpleName().toString())
                .time(context.time())
                .topLevel(primary)
                .create();
    }

    private boolean isNamedClass(ClassTree node) {
        return node.getSimpleName() != null && !node.getSimpleName().isEmpty();
    }

    private void collectMetrics(ClassTree node, JavaClass clazz) {
        counters.forEach(c -> {
            c.reset();
            c.visitClass(node, clazz);
        });
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        if (depth == 0) {
            int loc;
            var o = getContent(node);
            if (o.isPresent()) {
                var locCounter = new LineNumberCounter(o.get());
                loc = locCounter.count();
                var lineRange = getLineRange(node);
                var m = new JavaMethod(false, currentClass, AstUtils.getSignature(node), lineRange);
                m.getMetrics().setLineOfCode(loc);
            }
        }
        return super.visitMethod(node, unused);
    }

    private LineRange getLineRange(MethodTree node) {
        var startPos = sourcePositions.getStartPosition(compilationUnit, node);
        var endPos = sourcePositions.getEndPosition(compilationUnit, node);
        var lineMap = compilationUnit.getLineMap();
        var startLine = (int) lineMap.getLineNumber(startPos);
        var endLine = (int) lineMap.getLineNumber(endPos);
        return new LineRange(startLine, endLine);
    }

    private Optional<String> getContent(Tree node) {
        var start = sourcePositions.getStartPosition(compilationUnit, node);
        var end = sourcePositions.getEndPosition(compilationUnit, node);
        try {
            var source = compilationUnit.getSourceFile().getCharContent(true);
            return Optional.of(source.subSequence((int) start, (int) end).toString());
        } catch (IOException e) {
            logger.info(e.getMessage());
            return Optional.empty();
        }
    }
}
