package io.github.francescodonnini;

import io.github.francescodonnini.cli.CLI;
import io.github.francescodonnini.cli.DataCli;
import io.github.francescodonnini.cli.MLCli;
import picocli.CommandLine;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length <= 1) {
            throw new IllegalArgumentException("expected at least two arguments but got " + args.length);
        }
        var slice = Arrays.copyOfRange(args, 1, args.length);
        switch (CLI.from(args[0])) {
            case CLI.DATA:
                new CommandLine(new DataCli()).execute(slice);
                break;
            case CLI.ML:
                new CommandLine(new MLCli()).execute(slice);
                break;
        }
    }
}
