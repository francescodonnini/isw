package io.github.francescodonnini.pipeline.ml;

import io.github.francescodonnini.pipeline.PipelineException;
import io.github.francescodonnini.pipeline.Step;
import io.github.francescodonnini.pipeline.inputs.MLWorkloadInfo;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WhatIfStep implements Step<MLWorkloadInfo, Void> {
    private final String smellAttrName;

    public WhatIfStep(String smellAttrName) {
        this.smellAttrName = smellAttrName;
    }

    @Override
    public Void execute(MLWorkloadInfo input) throws PipelineException {
        var dataset = input.getDataset();
        var aDataset = dataset.trainingSet();
        var smellAttr = aDataset.attribute(smellAttrName);
        if (smellAttr == null) {
            throw new PipelineException("Attribute '" + smellAttrName + "' not found in dataset");
        }
        var bPlusDataset = new Instances(aDataset, 0);
        var bDataset = new Instances(aDataset, 0);
        var cDataset = new Instances(aDataset, 0);
        for (var i = 0; i < aDataset.numInstances(); ++i) {
            var inst = aDataset.instance(i);
            var smellCount = inst.value(smellAttr);
            if (smellCount > 0) {
                bPlusDataset.add(inst);
                var zeroedInst = (Instance) inst.copy();
                zeroedInst.setValue(smellAttr, 0);
                bDataset.add(zeroedInst);
            } else if (smellCount == 0) {
                cDataset.add(inst);
            }
        }

        try {
            var model = new RandomForest();
            model.buildClassifier(aDataset);

            var evalA = evaluate(model, aDataset);
            var evalB = evaluate(model, bDataset);
            var evalBPlus = evaluate(model, bPlusDataset);
            var evalC = evaluate(model, cDataset);

            var parent = Files.createDirectories(input.getResultsPath());
            ReportingUtils.summary(parent.resolve("SUMMARY.txt"), input);
            var positiveClassValueIndex = aDataset.classAttribute().indexOfValue("1");
            save(parent.resolve("what-if.csv"), evalA, evalB, evalBPlus, evalC, positiveClassValueIndex);
        } catch (Exception e) {
            throw new PipelineException(e);
        }
        return null;
    }

    private Evaluation evaluate(Classifier model, Instances val) throws Exception {
        var eval = new Evaluation(val);
        eval.evaluateModel(model, val);
        return eval;
    }

    private void save(
            Path path,
            Evaluation evalA,
            Evaluation evalB,
            Evaluation evalBPlus,
            Evaluation evalC, int classIndex) throws PipelineException {
        try (var writer = new FileWriter(path.toFile())) {
            writer.write("subset,true_positives,predicted_positives\n");
            writeRow(writer, "A", evalA, classIndex);
            writeRow(writer, "B", evalB, classIndex);
            writeRow(writer, "B+", evalBPlus, classIndex);
            writeRow(writer, "C", evalC, classIndex);
        } catch (IOException e) {
            throw new PipelineException(e);
        }
    }

    private void writeRow(FileWriter writer, String subset, Evaluation eval, int classIndex) throws IOException {
        var tp = eval.numTruePositives(classIndex);
        var predictedPositives = tp + eval.numFalsePositives(classIndex);
        writer.write(subset + "," + tp + "," + predictedPositives + "\n");
    }
}
