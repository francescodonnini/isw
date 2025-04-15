package io.github.francescodonnini.data;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import io.github.francescodonnini.ast.AstUtils;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JavaMethodExtractor extends TreeScanner<Void, Void> {
    private final Logger logger = Logger.getLogger(JavaMethodExtractor.class.getName());
    private CompilationUnitTree cu;
    private final List<JavaMethod> methods = new ArrayList<>();
    private JavaClass clazz;
    private SourcePositions sourcePositions;

    public void reset() {
        methods.clear();
    }

    public List<JavaMethod> getMethods() {
        return methods;
    }

    public void setClass(JavaClass clazz) {
        this.clazz = clazz;
    }

    public void setCompilationUnit(CompilationUnitTree cu) {
        this.cu = cu;
    }

    public void setSourcePositions(SourcePositions sourcePositions) {
        this.sourcePositions = sourcePositions;
    }

    @Override
    public Void visitMethod(MethodTree node, Void unused) {
        getContent(node).ifPresent(content -> methods.add(new JavaMethod(false, clazz, AstUtils.getSignature(node), content)));
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
