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
    private final List<JavaMethod> methods = new ArrayList<>();
    private final List<JavaClass> classes = new ArrayList<>();
    private ParseContext context;
    private JavaClass currentClass;
    private int anonymousClassCounter = 0;
    private final List<AbstractCounter> counters;

    public JavaMethodExtractor(List<AbstractCounter> counters) {
        this.counters = counters;
    }

    public void reset() {
        anonymousClassCounter = 0;
        classes.clear();
        methods.clear();
    }

    public List<JavaClass> getClasses() {
        return new ArrayList<>(classes);
    }

    public void setClass(JavaClass clazz) {
        currentClass = clazz;
        anonymousClassCounter = 0;
    }

    public void parse(ParseContext context) throws IOException {
        this.context = context;
        var units = compiler
                .getStandardFileManager(null, null, null)
                .getJavaFileObjects(context.getAbsolutePath());
        var task = (JavacTask) compiler.getTask(null, null, null, null, null, units);
        setSourcePositions(Trees.instance(task).getSourcePositions());
        for (var cu : task.parse()) {
            setCompilationUnit(cu);
            cu.accept(this, null);
        }
    }

    private void setCompilationUnit(CompilationUnitTree cu) {
        this.compilationUnit = cu;
    }

    private void setSourcePositions(SourcePositions sourcePositions) {
        this.sourcePositions = sourcePositions;
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void unused) {
        if (isAnonymousClass(node)) {
            ++anonymousClassCounter;
            var parent = currentClass;
            currentClass = createAnonymousClass();
            var rv = super.visitNewClass(node, unused);
            counters.forEach(c -> c.visitNewClass(node, currentClass));
            classes.add(currentClass);
            currentClass = parent;
            return rv;
        }
        return super.visitNewClass(node, unused);
    }

    private boolean isAnonymousClass(NewClassTree node) {
        return node.getClassBody() != null;
    }

    @Override
    public Void visitClass(ClassTree node, Void unused) {
        if (isNamedClass(node)) {
            setNamedClass(node);
            var r = super.visitClass(node, unused);
            classes.add(currentClass);
            collectMetrics(node, currentClass);
            return r;
        }
        return null;
    }

    private void setNamedClass(ClassTree currentClass) {
        this.currentClass = new JavaClass(
                context.commit(),
                context.parent(),
                context.path(),
                currentClass.getSimpleName().toString(),
                context.time()
        );
    }

    private boolean isNamedClass(ClassTree node) {
        return node.getSimpleName() != null && !node.getSimpleName().isEmpty();
    }

    private void collectMetrics(ClassTree node, JavaClass clazz) {
        counters.forEach(c -> c.visitClass(node, clazz));
    }

    private JavaClass createAnonymousClass() {
        return new JavaClass(
                currentClass.getCommit(),
                currentClass.getParent(),
                currentClass.getPath(),
                "#" + anonymousClassCounter,
                currentClass.getTime());
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        long loc;
        var o = getContent(node);
        if (o.isPresent()) {
            var locCounter = new LineNumberCounter(o.get());
            loc = locCounter.count();
            var lineRange = getLineRange(node);
            var m = new JavaMethod(false, currentClass, AstUtils.getSignature(node), lineRange);
            m.getMetrics().setLineOfCode(loc);
            methods.add(m);
        }
        return super.visitMethod(node, unused);
    }

    private LineRange getLineRange(MethodTree node) {
        var startPos = sourcePositions.getStartPosition(compilationUnit, node);
        var endPos = sourcePositions.getEndPosition(compilationUnit, node);
        var lineMap = compilationUnit.getLineMap();
        var startLine = lineMap.getLineNumber(startPos);
        var endLine = lineMap.getLineNumber(endPos);
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
        }
        return Optional.empty();
    }
}
