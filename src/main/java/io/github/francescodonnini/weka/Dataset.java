package io.github.francescodonnini.weka;

import io.github.francescodonnini.weka.preprocessing.PreprocessingFunction;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Dataset {
    private static final int BUGGY_ATTR_INDEX = 0;
    private static final String RELEASE_ATTR = "release";
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private Instances data;
    private final Map<Integer, Integer> releaseStartIndices = new HashMap<>();
    private final Map<Integer, Integer> releaseGroupCount = new HashMap<>();
    private final List<Integer> releases = new ArrayList<>();
    private final List<Integer> trainingIndices = new ArrayList<>();
    private final List<Integer> testIndices = new ArrayList<>();

    public Dataset(
            Path dataPath,
            Set<String> features,
            double trainingSplit,
            double dropFactor) throws Exception {
        var source = new ConverterUtils.DataSource(dataPath.toString());
        loadDataset(source.getDataSet(), features, trainingSplit, dropFactor);
    }

    private void loadDataset(
            Instances data,
            Set<String> features,
            double trainingSplit,
            double dropFactor) throws Exception {
        data.setClassIndex(BUGGY_ATTR_INDEX);
        if (!features.isEmpty()) {
            var indices = Collections.list(data.enumerateAttributes())
                    .stream()
                    .filter(attr -> !attr.name().equals(RELEASE_ATTR))
                    .filter(attr -> !features.contains(attr.name()))
                    .mapToInt(Attribute::index)
                    .toArray();
            var removeFeature = new Remove();
            removeFeature.setAttributeIndicesArray(indices);
            removeFeature.setInputFormat(data);
            data = Filter.useFilter(data, removeFeature);
        }

        var releaseAttr = data.attribute(RELEASE_ATTR);
        var distinct = new TreeSet<Integer>();
        for (var i : data) {
            distinct.add((int) i.value(releaseAttr));
        }

        this.releases.addAll(distinct.stream()
                .filter(d -> d < Math.ceil(distinct.size() * (1 - dropFactor)))
                .toList());
        logger.log(Level.INFO, "{0} distinct releases have been found: {1}", new Object[] { releases.size(),  String.join(",", releases.stream().map(String::valueOf).toList()) });

        data.sort(releaseAttr);

        var currentRelease = -1;
        var count = 0;
        var total = 0;
        for (var i = 0; i < data.numInstances(); ++i) {
            var r = (int) data.get(i).value(releaseAttr);
            if (r > currentRelease) {
                if (currentRelease != -1) {
                    releaseGroupCount.put(currentRelease, count);
                }
                if (r > releases.getLast()) {
                    break;
                }
                currentRelease = r;
                releaseStartIndices.put(currentRelease, i);
                count = 0;
            }
            count++;
            total++;
        }
        if (currentRelease != -1) {
            releaseGroupCount.put(currentRelease, count);
        }

        this.data = new Instances(data, 0, total);

        var removeReleaseAttr = new Remove();
        removeReleaseAttr.setAttributeIndicesArray(new int[] { releaseAttr.index() });
        removeReleaseAttr.setInputFormat(data);
        this.data = Filter.useFilter(this.data, removeReleaseAttr);

        var trainingSize = (int)Math.floor(releases.size() * trainingSplit);
        trainingIndices.addAll(releases.subList(0, trainingSize));
        testIndices.addAll(releases.subList(trainingSize, releases.size()));
    }

    private Instances slice(int releaseStart, int releaseEndExcl) {
        var start = releaseStartIndices.get(releaseStart);
        if (start == null) {
            return new Instances(data, 0);
        }
        var count = releaseGroupCount.entrySet().stream()
                .filter(r -> r.getKey() >= releaseStart && r.getKey() < releaseEndExcl)
                .mapToInt(Map.Entry::getValue)
                .sum();
        return new Instances(data, start, count);
    }

    public List<Integer> getReleases() {
        return releases;
    }

    public Set<Attribute> features() {
        return Collections.list(data.enumerateAttributes())
                .stream()
                .collect(Collectors.toUnmodifiableSet());
    }

    public Attribute classAttribute() {
        return data.classAttribute();
    }

    public int classIndex() {
        return data.classIndex();
    }

    public List<Integer> trainingRange() {
        return trainingIndices;
    }

    public Instances trainingSet() {
        return slice(0, trainingRange().size());
    }

    public Instances trainingSet(int start, int endExclusive) {
        return slice(start, endExclusive);
    }

    public Instances validationSet(int start) throws Exception {
        return validationSet(start, start + 1);
    }

    public Instances validationSet(int start, int endExclusive) {
        return slice(start, endExclusive);
    }

    public void preprocess(PreprocessingFunction f) {
        for (var it = data.enumerateAttributes(); it.hasMoreElements();) {
            var attr = it.nextElement();
            f.preprocess(data, attr.index());
        }
    }
}
