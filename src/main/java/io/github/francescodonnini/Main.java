package io.github.francescodonnini;

import io.github.francescodonnini.cli.DataCli;
import io.github.francescodonnini.cli.FeatureSelectionCli;
import io.github.francescodonnini.cli.MLCli;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        switch (args[0]) {
            case "data":
                new CommandLine(new DataCli()).execute(args);
                break;
            case "feature_selection":
                new CommandLine(new FeatureSelectionCli()).execute(args);
                break;
            case "ml":
                new CommandLine(new MLCli()).execute(args);
                break;
            default:
                throw new IllegalArgumentException("Unknown option: " + args[0]);
        }
    }
}
