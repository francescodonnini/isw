package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import io.github.francescodonnini.weka.factories.CostSensitiveModelFactory;
import io.github.francescodonnini.weka.factories.ModelFactoryException;
import io.github.francescodonnini.weka.factories.SimpleModelFactory;
import io.github.francescodonnini.weka.training.TrainingException;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EvaluationStep  implements Step<MLWorkloadInfo, MLWorkloadInfo> {
    @Override
    public MLWorkloadInfo execute(MLWorkloadInfo input) throws PipelineException {
        var dataset = input.getDataset();
        var tr = dataset.trainingSet();
        Classifier model = model(input.getModel(), tr, input.useClassWeights());
        try {
            var eval = train(model, tr, dataset.testSet());
            var parent = Files.createDirectories(input.getResultsPath());
            ReportingUtils.summary(parent.resolve("SUMMARY.txt"), input);
            save(parent.resolve("evaluations.csv"), eval, dataset.classIndex());
        } catch (IOException | TrainingException e) {
            throw new PipelineException(e);
        }
        return input;
    }

    private Classifier model(String modelName, Instances tr, boolean useClassWeights) throws PipelineException {
        Classifier model;
        var factory = new SimpleModelFactory();
        if (useClassWeights) {
            try {
                model = new CostSensitiveModelFactory(factory)
                        .setClassWeights(tr)
                        .create(modelName);
            } catch (ModelFactoryException e) {
                throw new PipelineException(e);
            }
        } else {
            model = factory.create(modelName);
        }
        return model;
    }

    private Evaluation train(Classifier model, Instances tr, Instances val) throws TrainingException {
        try {
            model.buildClassifier(tr);
            var eval = new Evaluation(val);
            eval.evaluateModel(model, val);
            return eval;
        } catch (Exception e) {
            throw new TrainingException(e);
        }
    }

    private void save(Path path, Evaluation eval, int classIndex) throws PipelineException {
        try (var writer = new FileWriter(path.toFile())) {
            writer.write("precision,recall,F-score,kappa,AUC\n");
            writer.write(eval.precision(classIndex) + "," +
                    eval.recall(classIndex) + "," +
                    eval.fMeasure(classIndex) + "," +
                    eval.kappa() + "," +
                    eval.areaUnderROC(classIndex) + "\n");
        } catch (IOException e) {
            throw new PipelineException(e);
        }
    }
}
