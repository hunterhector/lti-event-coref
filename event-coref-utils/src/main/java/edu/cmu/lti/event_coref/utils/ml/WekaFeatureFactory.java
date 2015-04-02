package edu.cmu.lti.event_coref.utils.ml;

import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.FeatureType;
import org.javatuples.Triplet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Factory for creating Weka features, to use this factory, first we need to construct feature
 * information by provide a feature map, which can be get from
 * FeatureUtils.getFeatureIdMap. An {@link Instances} object (the dataset) will be created and all
 * instances created by this factory will be added to this dataset. You could get the dataset by
 * {@link #getDataset()} method
 *
 * @author Zhengzhong Liu, Hector
 */
public class WekaFeatureFactory {
    private Map<String, Triplet<Integer, Boolean, String>> featureNameMap;

    private FastVector binaryVals;

    private FastVector classVal;

    // Positive feature value, could also be changed
    private String positiveFeatureVal = "pos";

    // Negative feature value, could be changed
    private String negativeFeatureVal = "neg";

    private Instances dataSet;

    // private Instances filteredDataset;

    private final int dummyClassIndex = -1;

    // Positive Class label used for full coreference case
    private static String fullCoreferenceLabelName = "full";

    // Negative Class label, could be changed to anything you like
    private final static String otherLabel = "other";

    private String positiveLabel;

    /*
     * Check whether the corresponding feature of that instance is positive
     * TODO: This one didn't perform binary feature validation currently
     */
    public boolean isPositiveFeatureValue(Instance instance, String featureName) {
        double featureValue = getFeatureValue(instance, featureName);
        int integerize = featureValue == 1.0 ? 1 : 0;
        return binaryVals.indexOf("pos") == integerize;
    }

    public double getFeatureValue(Instance instance, String featureName) {
        int featureId = featureNameMap.get(featureName).getValue0();
        double featureValue = instance.value(featureId - 1);

        return featureValue;
    }

    /**
     * A simplified constructor that use the default full coreference class name, and a default name
     * for label attributes. This creates a binary class. To use multi class use the full version of
     * the constructor
     *
     * @param featureNameMap
     */
    public WekaFeatureFactory(Map<String, Triplet<Integer, Boolean, String>> featureNameMap) {
        this(featureNameMap, getClassLabels(fullCoreferenceLabelName), "Binary_Classes");
        positiveLabel = fullCoreferenceLabelName;
    }

    /**
     * A simplified constructor to get the provided label as positive label, and provided name
     * for label attributes. This creates a binary class. To use multi class use the full version of
     * the constructor.
     *
     * @param featureNameMap
     */
    public WekaFeatureFactory(Map<String, Triplet<Integer, Boolean, String>> featureNameMap,
                              String postiveLabel) {
        this(featureNameMap, getClassLabels(postiveLabel), "Binary_Classes");
        this.positiveLabel = postiveLabel;
    }

    /**
     * A "sort of" generic constructor which is possible to initailize a multi-class feature
     * definition, however multi-class construction is not tested yet
     *
     * @param featureNameMap     Mapping from feature name to its feature id, defaultZero and feature type
     * @param labels             Labels used to denote each class
     * @param labelAttributeName A descriptive name for the instance class, e.g "Full_vs_No_Classes", or
     *                           "Full_Sub_Member_No_Classes"
     * @throws Exception
     */
    public WekaFeatureFactory(Map<String, Triplet<Integer, Boolean, String>> featureNameMap,
                              List<String> labels, String labelAttributeName) {
        this.featureNameMap = featureNameMap;

        FastVector atts = new FastVector();
        binaryVals = new FastVector();
        binaryVals.addElement(negativeFeatureVal); // add this first to make it zero
        binaryVals.addElement(positiveFeatureVal); // add this later to make it one

        for (Entry<String, Triplet<Integer, Boolean, String>> featureEntry : featureNameMap.entrySet()) {
            String featureName = featureEntry.getKey();
            String featureType = featureEntry.getValue().getValue2();
            if (featureType.equals(FeatureType.BINARY.name())) {
                atts.addElement(new Attribute(featureName, binaryVals)); // add binary as nominal
            } else if (featureType.equals(FeatureType.NUMERIC.name())) {
                atts.addElement(new Attribute(featureName)); // add as numeric
            }
        }

        // create the class label
        classVal = new FastVector(labels.size());
        for (String label : labels) {
            classVal.addElement(label);
        }

        // add class attribute as the last element
        Attribute classAttribute = new Attribute(labelAttributeName, classVal);
        atts.addElement(classAttribute);

        dataSet = new Instances("AllFeatures", atts, 0);
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
    }

    private static List<String> getClassLabels(String postiveLabel) {
        List<String> labels = new ArrayList<String>();
        labels.add(otherLabel); // this one is 0
        labels.add(postiveLabel); // this one is 1
        return labels;
    }

    public Instances getDataset() {
        // if (attributeFilterOn)
        // return filteredDataset;
        // else
        return dataSet;
    }

    /**
     * Create only instance with binary label
     *
     * @param featuresMap
     * @param isPositive
     * @param weight
     * @return
     */
    public Instance addInstance(Map<String, Double> featuresMap, boolean isPositive, int weight) {
        return createInstance(featuresMap,
                isPositive ? classVal.indexOf(positiveLabel) : classVal.indexOf(otherLabel), weight);
    }

    /**
     * Create instance without class and filled class value with a dummy value (-1)
     *
     * @param featuresMap
     * @param weight
     * @return
     */
    public Instance createInstance(Map<String, Double> featuresMap, int weight) {
        return createInstance(featuresMap, dummyClassIndex, weight);
    }

    /**
     * Create instance by using label string, make sure label is defined in dataset
     *
     * @param featuresMap
     * @param label
     * @param weight
     * @return
     */
    public Instance createInstance(Map<String, Double> featuresMap, String label, int weight) {
        return createInstance(featuresMap, classVal.indexOf(label), weight);
    }

    /**
     * Create a instance which is associated with the {@link #dataSet}, make sure the label is defined
     * in class labels when construction
     *
     * @param featuresMap
     * @param label
     * @param weight
     * @return
     */
    public Instance createInstance(Map<String, Double> featuresMap, int classValIndex, int weight) {
        double[] featureAttValues = new double[dataSet.numAttributes()];

        for (Entry<String, Triplet<Integer, Boolean, String>> featureEntry : featureNameMap.entrySet()) {

            String featureName = featureEntry.getKey();
            boolean defaultZero = featureEntry.getValue().getValue1();
            int featureId = featureEntry.getValue().getValue0();
            String featureType = featureEntry.getValue().getValue2();

            Double score = null;
            if (featuresMap.containsKey(featureName)) {
                score = featuresMap.get(featureName);
            } else if (defaultZero) {
                score = 0.0;
            }

            if (score != null) {
                if (featureType.equals(FeatureType.BINARY.name())) {
                    if (score == 1.0) {
                        featureAttValues[featureId - 1] = binaryVals.indexOf(positiveFeatureVal);
                    } else {
                        featureAttValues[featureId - 1] = binaryVals.indexOf(negativeFeatureVal);
                    }
                } else if (featureType.equals(FeatureType.NUMERIC.name())) {
                    featureAttValues[featureId - 1] = score;
                }
            } else {
                featureAttValues[featureId - 1] = Instance.missingValue();
            }
        }

        featureAttValues[dataSet.numAttributes() - 1] = classValIndex;

        Instance instance = new Instance(weight, featureAttValues);

        addInstance(instance, classValIndex == dummyClassIndex);

        return instance;
    }

    /**
     * Simply add one instance to this factory, it is useful when we copy dataset
     */
    public void addInstance(Instance instance, boolean classMissing) {
        dataSet.add(instance);
        instance.setDataset(dataSet);

        if (classMissing) {
            instance.setClassMissing();
        }
    }
}