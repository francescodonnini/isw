package io.github.francescodonnini.pipeline;

public class Pipeline<I,O> {
    private final Step<I,O> current;

    public Pipeline(Step<I, O> current) {
        this.current = current;
    }

    public static <I,O> Pipeline<I,O> start(Step<I, O> start) {
        return new Pipeline<>(start);
    }

    public <K> Pipeline<I,K> next(Step<O,K> next) {
        return new Pipeline<>(input -> {
            var output = this.current.execute(input);
            return next.execute(output);
        });
    }

    public O run(I input) throws Exception {
        return current.execute(input);
    }
}
