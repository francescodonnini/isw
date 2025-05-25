package io.github.francescodonnini.workflow;

import java.util.*;
import java.util.stream.Collectors;

public class WorkflowBuilder {
    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<Node, List<Node>> adjacencyList = new HashMap<>();

    public Workflow build() {
        return new WorkflowImpl(topologicalSort());
    }

    private List<Node> topologicalSort() {
        final List<Node> sorted = new ArrayList<>();
        final Queue<Node> zeroInDegree = nodes.values().stream()
                .filter(node -> getInDegree(node) == 0)
                .collect(Collectors.toCollection(LinkedList::new));
        while (!zeroInDegree.isEmpty()) {
            var n = zeroInDegree.poll();
            sorted.add(n);
            for (var m : getOutcomingNodes(n)) {
                removeEdge(n, m);
                if (getInDegree(m) == 0) {
                    zeroInDegree.add(m);
                }
            }
        }
        return sorted;
    }

    private List<Node> getOutcomingNodes(Node n) {
        return new ArrayList<>(adjacencyList.getOrDefault(n, List.of()));
    }

    private long getInDegree(Node node) {
        return nodes.values().stream()
                .filter(other -> !other.equals(node))
                .map(adjacencyList::get)
                .filter(Objects::nonNull)
                .filter(list -> list.contains(node))
                .count();
    }

    private void removeEdge(Node n, Node m) {
        var list = adjacencyList.getOrDefault(n, List.of());
        if (!list.isEmpty()) {
            list.remove(m);
        }
    }

    public WorkflowBuilder addNode(Node node) {
        if (nodes.containsKey(node.getId())) {
            throw new IllegalArgumentException("a node with id " + node.getId() + " already exists");
        }
        nodes.put(node.getId(), node);
        getDependencies(node).forEach(n -> adjacencyList.computeIfAbsent(n, _ -> new ArrayList<>()).add(node));
        return this;
    }

    private List<Node> getDependencies(Node node) {
        var dependencies = new ArrayList<Node>();
        for (var depId : node.getDependencies()) {
            var dependency = nodes.getOrDefault(depId, null);
            if (dependency == null) {
                throw new IllegalArgumentException("a dependency node with id " + depId + " does not exist");
            }
            dependencies.add(dependency);
        }
        return dependencies;
    }
}
