package io.github.francescodonnini.data;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
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
                currentClass = parent;
                return rv;
            }
        }
        return super.visitClass(node, unused);
    }

    private Optional<JavaClass> createInnerClass(ClassTree innerNode) {
        return getContent(innerNode)
                .map(s -> new JavaClass(
                currentClass.getParent(),
                Path.of(currentClass.getPath().toString().replace(".java", "#%d.java".formatted(anonymousClassCounter))),
                currentClass.getRelease(),
                s));
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        getContent(node).ifPresent(content -> methods.add(new JavaMethod(false, currentClass, AstUtils.getSignature(node), content)));
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
