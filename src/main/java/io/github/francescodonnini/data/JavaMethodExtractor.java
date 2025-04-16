package io.github.francescodonnini.data;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import io.github.francescodonnini.ast.AbstractCounter;
import io.github.francescodonnini.ast.AstUtils;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JavaMethodExtractor extends TreeScanner<Void, Void> {
    private final Logger logger = Logger.getLogger(JavaMethodExtractor.class.getName());
    private CompilationUnitTree cu;
    private final List<JavaMethod> methods = new ArrayList<>();
    private JavaClass currentClass;
    private final List<JavaClass> innerClasses = new ArrayList<>();
    private SourcePositions sourcePositions;
    private int anonymousClassCounter = -1;
    private final List<AbstractCounter> counters;
    private final boolean getContent;

    public JavaMethodExtractor(List<AbstractCounter> counters, boolean getContent) {
        this.counters = counters;
        this.getContent = getContent;
    }

    public void reset() {
        innerClasses.clear();
        methods.clear();
    }

    public List<JavaClass> getInnerClasses() {
        return innerClasses;
    }

    public List<JavaMethod> getMethods() {
        return methods;
    }

    public void setClass(JavaClass clazz) {
        currentClass = clazz;
        anonymousClassCounter = -1;
    }

    public void setCompilationUnit(CompilationUnitTree cu) {
        this.cu = cu;
    }

    public void setSourcePositions(SourcePositions sourcePositions) {
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
                innerClasses.add(currentClass);
                var rv = super.visitClass(node, unused);
                counters.forEach(c -> c.visitClass(node, currentClass));
                currentClass = parent;
                return rv;
            }
        }
        var rv = super.visitClass(node, unused);
        counters.forEach(c -> c.visitClass(node, currentClass));
        return rv;
    }

    private Optional<JavaClass> createInnerClass(ClassTree innerNode) {
        var content = "";
        if (getContent) {
            var o = getContent(innerNode);
            if (o.isPresent()) {
                content = o.get();
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(new JavaClass(
                currentClass.getParent(),
                innerClassPath(currentClass, innerNode),
                currentClass.getRelease(),
                content));
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
        var content = "";
        if (getContent) {
            var o = getContent(node);
            if (o.isPresent()) {
                content = o.get();
            }
        }
        var m = new JavaMethod(false, currentClass, AstUtils.getSignature(node), 0L, -1L, content);
        methods.add(m);
        return super.visitMethod(node, unused);
    }

    private Optional<String> getContent(Tree node) {
        var start = sourcePositions.getStartPosition(cu, node);
        var end = sourcePositions.getEndPosition(cu, node);
        try {
            var source = cu.getSourceFile().getCharContent(true);
            return Optional.of(source.subSequence((int) start, (int) end).toString());
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return Optional.empty();
    }

}
