package io.github.francescodonnini.ast;

import com.sun.source.tree.MethodTree;
import io.github.francescodonnini.model.JavaClass;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.logging.Logger;

public class LineOfCodeCounter extends AbstractCounter {
    private final Logger logger = Logger.getLogger(LineOfCodeCounter.class.getName());

    public LineOfCodeCounter() {
        super("LOC");
    }

    @Override
    public Void visitMethod(MethodTree node, JavaClass unused) {
        var body = node.getBody();
        var loc = 1L;
        if (body != null) {
            try (var ln = new LineNumberReader(new StringReader(body.toString()))) {
                long skipped;
                do {
                    skipped = ln.skip(Long.MAX_VALUE);
                } while (skipped > 0);
                loc = ln.getLineNumber();
            } catch (IOException e) {
                logger.info(e.getMessage());
            }
        }
        update(AstUtils.getSignature(node), loc);
        return super.visitMethod(node, unused);
    }
}
