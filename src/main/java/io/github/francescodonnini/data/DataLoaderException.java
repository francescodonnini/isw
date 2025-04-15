package io.github.francescodonnini.data;

public class DataLoaderException extends RuntimeException {
    public DataLoaderException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return getCause().getMessage();
    }
}
