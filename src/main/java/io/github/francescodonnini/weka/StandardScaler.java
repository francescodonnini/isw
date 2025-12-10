package io.github.francescodonnini.weka;

import weka.core.Instances;

public class StandardScaler implements PreprocessingFunction {
    @Override
    public void preprocess(Instances data, int attrIdx) {
        var attr = data.attribute(attrIdx);
        if (!attr.isNumeric()) {
            return;
        }
        var trStats = data.attributeStats(attrIdx);
        double mean = trStats.numericStats.mean;
        double stDev = trStats.numericStats.stdDev;
        for (var i : data) {
            var val = i.value(attrIdx);
            i.setValue(attrIdx, (val - mean) / stDev);
        }
    }
}
