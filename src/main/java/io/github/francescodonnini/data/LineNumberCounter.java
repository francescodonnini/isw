package io.github.francescodonnini.data;

public class LineNumberCounter {
    private final String source;
    private char current;
    private int readPos;

    public LineNumberCounter(String source) {
        this.source = source;
        readPos = 0;
    }

    private void nextChar() {
        if (readPos < source.length()) {
            current = source.charAt(readPos);
            readPos++;
        } else {
            current = 0;
        }
    }

    private char peekChar() {
        if (readPos + 1 < source.length()) {
            return source.charAt(readPos + 1);
        }
        return 0;
    }

    public long count() {
        long count = 1;
        while (notEof()) {
            if (current == '/') {
                nextChar();
                if (current == '/') {
                    nextChar();
                    skipSingleLineComment();
                } else if (current == '*') {
                    nextChar();
                    skipMultiLineComment();
                }
            }
            if (current == '\n') {
                count++;
            }
            nextChar();
        }
        return count;
    }

    private void skipMultiLineComment() {
        while (notEof()) {
            if (current == '*' && peekChar() == '/') {
                nextChar();
                break;
            }
            nextChar();
        }
        while (notEof() && Character.isSpaceChar(current)) {
            if (current == '\n') {
                nextChar();
                break;
            }
            nextChar();
        }
    }

    private void skipSingleLineComment() {
        while (notEof() && current != '\n') {
            nextChar();
        }
        nextChar();
    }

    private boolean notEof() {
        return readPos < source.length();
    }
}
