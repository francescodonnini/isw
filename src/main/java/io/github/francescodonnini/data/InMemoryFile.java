package io.github.francescodonnini.data;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class InMemoryFile  extends SimpleJavaFileObject {
    private final String content;

    public InMemoryFile(String name, String content) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}