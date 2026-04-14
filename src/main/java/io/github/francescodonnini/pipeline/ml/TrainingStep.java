package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.weka.factories.FilteredModelFactory;
import io.github.francescodonnini.weka.training.WalkForwardTrainer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TrainingStep implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    private final Logger logger = Logger.getLogger(TrainingStep.class.getName());

    public TrainingStep() throws IOException {
        var file = new FileHandler("training-step.log", true);
        file.setFormatter(new SimpleFormatter());
        logger.addHandler(file);
    }

    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws PipelineException {
        var factory = new FilteredModelFactory();
        factory.add(input.getDataset().features());
        factory.add(input.getDataset().classAttribute());
        var trainer = new WalkForwardTrainer(input.getDataset(), factory, input.useClassWeights());
        var history = trainer.train(input.getModel());

        try {
            var parent = Files.createDirectories(input.getResultsPath());
            createSummary(parent, input);
            var fileName = "%s-%s.csv".formatted(input.getModel(), input.useClassWeights() ? "W" : "NW");
            history.save(parent.resolve(fileName));
            return input;
        } catch (IOException e) {
            throw new PipelineException(e);
        }
    }

    private void createSummary(Path parent, MLWorkloadInfo info) throws PipelineException {
        try (var summary = new FileWriter(parent.resolve("SUMMARY").toFile())) {
            String s =
                    "Project:          " + info.getProject() + "\n" +
                    "Dataset Path:     " + info.getDataset().getPath() + "\n" +
                    "Proportion:       " + info.getProportion() + "\n" +
                    "Train-Test Split: " + info.getTrainTestSplit() + "\n" +
                    "Drop Factor:      " + info.getDropFactor() + "\n" +
                    "Model:            " + info.getModel() + "\n" +
                    "Class Weights:    " + (info.useClassWeights() ? "Y" : "N") + "\n" +
                    "Features:         " + String.join(",", info.getFeatures()) + "\n";
            logger.info(s);
            summary.write(s);
        } catch (IOException e) {
            throw new PipelineException(e);
        }
    }
}
