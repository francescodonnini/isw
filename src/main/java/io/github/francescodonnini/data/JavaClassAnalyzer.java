package io.github.francescodonnini.data;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import io.github.francescodonnini.collectors.ast.AbstractCounter;
import io.github.francescodonnini.model.ComplexityClassMetrics;
import io.github.francescodonnini.model.RevisionJavaClass;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JavaClassAnalyzer extends TreeScanner<Void, Void> {
    private final Logger logger = Logger.getLogger(JavaClassAnalyzer.class.getName());
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private CompilationUnitTree compilationUnit;
    private SourcePositions sourcePositions;
    private final List<AbstractCounter> counters;
    private RevisionJavaClass current;
    private final List<RevisionJavaClass> classes = new ArrayList<>();
    private Path path;

    public JavaClassAnalyzer(List<AbstractCounter> counters) {
        this.counters = counters;
    }

    public List<RevisionJavaClass> run(Path path, String content) throws IOException {
        this.path = path;
        var file = new InMemoryFile(path.toString(), content);
        var task = (JavacTask) compiler.getTask(null, null, null, null, null, List.of(file));
        setSourcePositions(Trees.instance(task).getSourcePositions());
        for (var cu : task.parse()) {
            setCompilationUnit(cu);
            cu.accept(this, null);
        }
        return classes;
    }

    private void setSourcePositions(SourcePositions sourcePositions) {
        this.sourcePositions = sourcePositions;
    }

    private void setCompilationUnit(CompilationUnitTree cu) {
        this.compilationUnit = cu;
    }

    @Override
    public Void visitClass(ClassTree node, Void unused) {
        if (isGenerated(node)) {
            return null;
        }

        if (isNamedClass(node)) {
            var parent = current;
            var content = getContent(node).orElse("");
            current = RevisionJavaClass.builder()
                    .path(path)
                    .name(className(node))
                    .topLevel(isPrimary(node))
                    .metrics(new ComplexityClassMetrics())
                    .build();
            var r = super.visitClass(node, unused);
            classes.add(current);
            collectMetrics(node, current, content);
            current = parent;
            return r;
        }
        return null;
    }

    private String className(ClassTree node) {
        return node.getSimpleName().toString();
    }

    private boolean isPrimary(ClassTree node) {
        var fileName = path
                .getFileName()
                .toString();
        var n = fileName.lastIndexOf('.');
        fileName = n >= 0 ? fileName.substring(0, n) : fileName;
        return current == null && node.getSimpleName().toString().equals(fileName);
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

    private boolean isNamedClass(ClassTree node) {
        return node.getSimpleName() != null && !node.getSimpleName().isEmpty();
    }

    private void collectMetrics(ClassTree node, RevisionJavaClass clazz, String content) {
        counters.forEach(c -> {
            c.reset();
            c.visitClass(node, clazz);
        });
        clazz.getMetrics().setLoc(new LineNumberCounter(content).count());
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
