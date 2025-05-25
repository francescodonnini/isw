package io.github.francescodonnini.workflow;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public abstract class Node implements Callable<Boolean> {
    private final String id;
    private final String name;
    private final List<String> dependencies;

    protected Node(String id, String name) {
        this(id, name, List.of());
    }

    protected Node(String id, String name, List<String> dependencies) {
        this.id = id;
        this.name = name;
        this.dependencies = dependencies;
    }

    public static Node create(String id, String name, Callable<Boolean> callable) {
        return new Node(id, name) {
            @Override
            public Boolean call() throws Exception {
                return callable.call();
            }
        };
    }

    public static Node create(String id, String name, List<String> dependencies, Callable<Boolean> callable) {
        return new Node(id, name, dependencies) {
            @Override
            public Boolean call() throws Exception {
                return callable.call();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node node)) return false;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "{%s, %s}".formatted(id, name);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}
