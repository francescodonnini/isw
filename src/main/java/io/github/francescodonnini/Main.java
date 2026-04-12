package io.github.francescodonnini;

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
        switch (args[0]) {
            case "data":
                new CommandLine(new DataCli()).execute(slice);
                break;
            case "ml":
                new CommandLine(new MLCli()).execute(slice);
                break;
            default:
                throw new IllegalArgumentException("Unknown option: " + args[0]);
        }
    }
}
