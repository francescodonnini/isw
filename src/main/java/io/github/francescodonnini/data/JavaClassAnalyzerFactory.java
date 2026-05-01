package io.github.francescodonnini.data;

import io.github.francescodonnini.collectors.ast.AbstractCounterFactory;
import io.github.francescodonnini.collectors.ast.AbstractCounterFactoryImpl;
import io.github.francescodonnini.collectors.ast.CohesionCounter;
import io.github.francescodonnini.collectors.ast.CyclomaticComplexityCounter;
import io.github.francescodonnini.collectors.ast.ElseCounter;
import io.github.francescodonnini.collectors.ast.FanOutCounter;
import io.github.francescodonnini.collectors.ast.NestingDepth;
import io.github.francescodonnini.collectors.ast.StatementsCounter;

import java.util.List;

public class JavaClassAnalyzerFactory {
    public static JavaClassAnalyzerFactory defaultFactory() {
        return new JavaClassAnalyzerFactory(new AbstractCounterFactoryImpl());
    }

    private final AbstractCounterFactory factory;

    public JavaClassAnalyzerFactory(AbstractCounterFactory factory) {
        this.factory = factory;
    }

    public JavaClassAnalyzer create() {
        var counters = List.of(
                factory.build(CyclomaticComplexityCounter.class),
                factory.build(ElseCounter.class),
                factory.build(NestingDepth.class),
                factory.build(StatementsCounter.class),
                factory.build(FanOutCounter.class),
                factory.build(CohesionCounter.class)
        );
        return new JavaClassAnalyzer(counters);
    }
}
