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

    public int count() {
        var eolMet = false;
        var count = 0;
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
                eolMet = true;
                count++;
            }
            nextChar();
        }
        if (!eolMet) {
            return 1;
        }
        return count;
    }

    private void skipMultiLineComment() {
        while (notEof()) {
            if (current == '*') {
                nextChar();
                if (current == '/') {
                    nextChar();
                    break;
                }
            } else {
                nextChar();
            }
        }
    }

    private void skipSingleLineComment() {
        while (notEof() && current != '\n') {
            nextChar();
        }
        if (current == '\n') {
            nextChar();
        }
    }

    private boolean notEof() {
        return readPos < source.length();
    }
}
