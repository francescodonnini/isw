package io.github.francescodonnini.workflow;

import java.util.List;
import java.util.logging.Logger;

public class WorkflowImpl implements Workflow {
    private static final Logger logger = Logger.getLogger(WorkflowImpl.class.getName());
    private final List<Node> schedule;

    public WorkflowImpl(List<Node> schedule) {
        this.schedule = schedule;
    }

    @Override
    public void execute() {
        for (Node node : schedule) {
            try {
                node.call();
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }
    }
}
