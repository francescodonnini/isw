package io.github.francescodonnini.weka;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.RemoveByName;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dataset {
    private static final String RELEASE_ATTR = "release";
    public record IntPair(int start, int endExcl) {}

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Path dataPath;
    private final double trainingSplit;
    private final List<Integer> releases = new ArrayList<>();
    private Instances trainingSet;
    private Instances testSet;
    private IntPair trainingRange;
    private IntPair testRange;

    public Dataset(Path dataPath, double trainingSplit) throws Exception {
        this.dataPath = dataPath;
        this.trainingSplit = trainingSplit;
        loadDataset();
    }

    private void loadDataset() throws Exception {
        var source = new ConverterUtils.DataSource(dataPath.toString());
        var data = source.getDataSet();
        data.setClassIndex(0);
        var distinct = new TreeSet<Integer>();
        for (var i : data) {
            distinct.add((int) i.value(data.attribute(Dataset.RELEASE_ATTR)));
        }
        releases.addAll(distinct);
        logger.log(Level.INFO, "{}", "%d distinct releases has been found [%s]".formatted(releases.size(), String.join(",", releases.stream().map(String::valueOf).toList())));
        var trainingSize = (int)Math.ceil(releases.size() * trainingSplit);
        trainingRange = new IntPair(releases.getFirst(), releases.get(trainingSize));
        trainingSet = select(data, byReleaseRange(data, trainingRange.start, trainingRange.endExcl));
        testRange = new IntPair(releases.get(trainingSize), releases.size());
        testSet = select(data, byReleaseRange(data, testRange.start, testRange.endExcl));
    }

    public int getClassIndex() {
        return trainingSet.classIndex();
    }

    public IntPair getTrainingRange() {
        return trainingRange;
    }

    public IntPair getTestRange() {
        return testRange;
    }

    public Instances trainingSet(int start, int endExclusive) throws Exception {
        var data = removeReleaseAttribute(select(trainingSet, byReleaseRange(trainingSet, start, endExclusive)));
        logCardinality(data, start, endExclusive);
        return data;
    }

    public Instances validationSet(int start) throws Exception {
        var data = trainingSet(start, testRange.start());
        logCardinality(data, start, testRange.start());
        return data;
    }

    private void logCardinality(Instances data, int start, int endExclusive) {
        logger.log(Level.INFO, "{0}", "Dataset[%d,%d)=%d".formatted(start, endExclusive, data.size()));
    }

    private Predicate<Instance> byReleaseRange(Instances data, int start, int endExclusive) {
        return instance -> {
            var value = instance.value(data.attribute(Dataset.RELEASE_ATTR));
            return value >= start && value < endExclusive;
        };
    }

    public Instances testSet() {
        return testSet;
    }

    private Instances select(Instances data, Predicate<Instance> i) {
        var result = new Instances(data, 0);
        data.stream().filter(i).forEach(result::add);
        return result;
    }

    private static Instances removeReleaseAttribute(Instances data) throws Exception {
        var remove = new RemoveByName();
        remove.setExpression(Dataset.RELEASE_ATTR);
        remove.setInputFormat(data);
        return Filter.useFilter(data, remove);
    }
}
