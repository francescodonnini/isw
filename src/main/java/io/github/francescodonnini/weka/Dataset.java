package io.github.francescodonnini.weka;

import weka.core.Attribute;
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
import java.util.stream.IntStream;

public class Dataset {
    private static final int BUGGY_ATTR_INDEX = 0;
    private static final String RELEASE_ATTR = "release";
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private Instances trainingSet;
    private Instances testSet;
    private IntPair trainingRange;
    private IntPair testRange;

    public Dataset(Path dataPath, double trainingSplit, double dropFactor) throws Exception {
        var source = new ConverterUtils.DataSource(dataPath.toString());
        loadDataset(source.getDataSet(), trainingSplit, dropFactor);
    }

    private void loadDataset(Instances data, double trainingSplit, double dropFactor) {
        data.setClassIndex(BUGGY_ATTR_INDEX);
        var distinct = new TreeSet<Integer>();
        for (var i : data) {
            distinct.add((int) i.value(data.attribute(Dataset.RELEASE_ATTR)));
        }
        var releases = distinct.stream()
                .filter(d -> d < Math.ceil(distinct.size() * (1 - dropFactor)))
                .toList();
        logger.log(Level.INFO, "{0}", "%d distinct releases has been found [%s]".formatted(releases.size(), String.join(",", releases.stream().map(String::valueOf).toList())));
        var trainingSize = (int)Math.floor(releases.size() * trainingSplit);
        trainingRange = new IntPair(releases.getFirst(), releases.get(trainingSize));
        trainingSet = select(data, byReleaseRange(data, trainingRange.start(), trainingRange.endExcl()));
        testRange = new IntPair(releases.get(trainingSize), releases.size());
        testSet = select(data, byReleaseRange(data, testRange.start(), testRange.endExcl()));
    }

    public Set<Attribute> getFeatures() {
        var attributes = new HashSet<Attribute>();
        for (var it = trainingSet.enumerateAttributes().asIterator(); it.hasNext(); ) {
            var attr = it.next();
            if (!attr.name().equals(Dataset.RELEASE_ATTR)) {
                attributes.add(attr);
            }
        }
        return attributes;
    }

    public Attribute getClassAttribute() {
        return trainingSet.classAttribute();
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
        return removeReleaseAttribute(select(trainingSet, byReleaseRange(trainingSet, start, endExclusive)));
    }

    public Instances validationSet(int start) throws Exception {
        return validationSet(start, testRange.start());
    }

    public Instances validationSet(int start, int endExclusive) throws Exception {
        return trainingSet(start, endExclusive);
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
        result.setClassIndex(BUGGY_ATTR_INDEX);
        return result;
    }

    private static Instances removeReleaseAttribute(Instances data) throws Exception {
        var remove = new RemoveByName();
        remove.setExpression(Dataset.RELEASE_ATTR);
        remove.setInputFormat(data);
        var result = Filter.useFilter(data, remove);
        result.setClassIndex(BUGGY_ATTR_INDEX);
        return result;
    }

    public void preprocess(PreprocessingFunction f) {
        var classIndex = trainingSet.classIndex();
        var releaseIndex = trainingSet.attribute(Dataset.RELEASE_ATTR).index();
        var attrIndices = IntStream.range(0, trainingSet.numAttributes())
                .filter(i -> i != classIndex)
                .filter(i -> i != releaseIndex)
                .toArray();
        for (var attrIdx : attrIndices) {
            f.preprocess(trainingSet, attrIdx);
            f.preprocess(testSet, attrIdx);
        }
    }
}
