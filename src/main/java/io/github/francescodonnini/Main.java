package io.github.francescodonnini;

import io.github.francescodonnini.cli.AnalysisCli;
import io.github.francescodonnini.cli.CLI;
import io.github.francescodonnini.cli.DataCli;
import picocli.CommandLine;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length <= 1) {
            throw new IllegalArgumentException("expected at least two arguments but got " + args.length);
        }
        var slice = Arrays.copyOfRange(args, 1, args.length);
        if (CLI.from(args[0]).equals(CLI.ANALYZE)) {
            new CommandLine(new AnalysisCli()).execute(slice);
        } else {
            new CommandLine(new DataCli()).execute(slice);
        }
    }
}
