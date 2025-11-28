package io.github.francescodonnini.pipeline;

public interface Step<I,O> {
    O execute(I input) throws Exception;
}
