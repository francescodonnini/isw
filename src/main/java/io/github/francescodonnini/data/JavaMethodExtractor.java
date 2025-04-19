package io.github.francescodonnini.data;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JavaMethodExtractor extends TreeScanner<Void, Void> {
    private final Logger logger = Logger.getLogger(JavaMethodExtractor.class.getName());
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private CompilationUnitTree compilationUnit;
    private SourcePositions sourcePositions;
    private final List<JavaMethod> methods = new ArrayList<>();
    private final List<JavaClass> classes = new ArrayList<>();
    private JavaClass currentClass;
    private int anonymousClassCounter = -1;
    private final List<AbstractCounter> counters;

    public JavaMethodExtractor(List<AbstractCounter> counters) {
        this.counters = counters;
    }

    public void reset() {
        classes.clear();
        methods.clear();
    }

    public List<JavaClass> getClasses() {
        return classes;
    }

    public List<JavaMethod> getMethods() {
        return methods;
    }

    public void setClass(JavaClass clazz) {
        currentClass = clazz;
        anonymousClassCounter = -1;
    }

    public void parse() throws IOException {
        var units = compiler
                .getStandardFileManager(null, null, null)
                .getJavaFileObjects(currentClass.getRealPath());
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
    public Void visitClass(ClassTree node, Void unused) {
        ++anonymousClassCounter;
        if (anonymousClassCounter > 0) {
            var parent = currentClass;
            var o = createInnerClass(node);
            if (o.isPresent()) {
                currentClass = o.get();
                classes.add(currentClass);
                var rv = super.visitClass(node, unused);
                counters.forEach(c -> c.visitClass(node, currentClass));
                classes.add(currentClass);
                currentClass = parent;
                return rv;
            }
        }
        var rv = super.visitClass(node, unused);
        classes.add(currentClass);
        counters.forEach(c -> c.visitClass(node, currentClass));
        return rv;
    }

    private Optional<JavaClass> createInnerClass(ClassTree innerNode) {
        return Optional.of(new JavaClass(
                currentClass.getCommit(),
                currentClass.getParent(),
                innerClassPath(currentClass, innerNode),
                currentClass.getTime()));
    }

    private Path innerClassPath(JavaClass container, ClassTree innerNode) {
        var s = container.getPath().toString().replace(".java", "");
        var name = innerNode.getSimpleName().toString();
        if (!name.isEmpty()) {
            s += "#" + name;
        }
        s += "#" + anonymousClassCounter + ".java";
        return Path.of(s);
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
