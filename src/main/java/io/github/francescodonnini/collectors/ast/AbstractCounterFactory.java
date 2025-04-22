package io.github.francescodonnini.collectors.ast;

public interface AbstractCounterFactory {
    <T> AbstractCounter build(Class<T> kind);
}
