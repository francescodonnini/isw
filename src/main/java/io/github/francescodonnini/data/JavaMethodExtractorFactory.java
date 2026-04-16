package io.github.francescodonnini.data;

import io.github.francescodonnini.collectors.ast.*;

import java.util.ArrayList;
import java.util.List;

public class JavaMethodExtractorFactory {
    private final AbstractCounterFactory factory;
    private final List<AbstractCounter> counters = new ArrayList<>();

    public static JavaMethodExtractorFactory defaultFactory(AbstractCounterFactory factory) {
        return new JavaMethodExtractorFactory(factory)
                .counter(CyclomaticComplexityCounter.class)
                .counter(InputParametersCounter.class)
                .counter(StatementsCounter.class)
                .counter(ElseCounter.class)
                .counter(NestingDepth.class)
                .counter(HalsteadComplexityCounter.class);
    }

    public JavaMethodExtractorFactory(AbstractCounterFactory factory) {
        this.factory = factory;
    }

    public <T> JavaMethodExtractorFactory counter(Class<T> clazz) {
        counters.add(factory.build(clazz));
        return this;
    }

    public JavaMethodExtractor create() {
        return new JavaMethodExtractor(counters);
    }
}
