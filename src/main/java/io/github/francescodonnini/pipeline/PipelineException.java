package io.github.francescodonnini.pipeline;

public class PipelineException extends Exception {

    public PipelineException(String message, Throwable cause) {
        super(message, cause);
    }

    public PipelineException(Throwable cause) {
        super(cause);
    }

    public PipelineException(String message) {
        super(message);
    }
}