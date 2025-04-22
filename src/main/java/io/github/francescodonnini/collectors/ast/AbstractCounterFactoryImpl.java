package io.github.francescodonnini.collectors.ast;

public class AbstractCounterFactoryImpl implements AbstractCounterFactory {
    public <T> AbstractCounter build(Class<T> kind) {
        if (kind == CyclomaticComplexityCounter.class) {
            return new CyclomaticComplexityCounter();
        } else if (kind == InputParametersCounter.class) {
            return new InputParametersCounter();
        } else if (kind == StatementsCounter.class) {
            return new StatementsCounter();
        } else if (kind == ElseCounter.class) {
            return new ElseCounter();
        } else if (kind == NestingDepth.class) {
            return new NestingDepth();
        } else {
            throw new IllegalArgumentException("unknown kind: " + kind);
        }
    }
}
