package edu.cmu.lti.event_coref.utils.ml;

import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.FeatureType;
import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.PairwiseEventFeatureInfo;
import org.javatuples.Triplet;

import java.util.*;
import java.util.Map.Entry;

public class FeatureImputator {

    private int imputationMethod = 0;

    private Map<String, Triplet<Integer, Boolean, String>> featureNameMap;

    public FeatureImputator(int imputationMethod,
                            Map<String, Triplet<Integer, Boolean, String>> featureNameMap) {
        this.imputationMethod = imputationMethod;
        this.featureNameMap = featureNameMap;
    }

    // features that starts with the following are default to be 0, not N/A
    public final String[] featruesDefaultZero = {PairwiseEventFeatureInfo.VERB_OCEAN.toString(),
            "eventRelation"};

    private Map<String, Double> numericFeatureAverages = new HashMap<String, Double>();

    private Map<String, Integer> binaryFeaturePositiveCounts = new HashMap<String, Integer>();

    private Map<String, Integer> binaryFeatureNegativeCounts = new HashMap<String, Integer>();

    public List<Map<String, Double>> imputeAll(List<Map<String, Double>> allFeatures) {
        if (imputationMethod == 0)
            return noImpute(allFeatures);
        else if (imputationMethod == 1)
            return zeroImpute(allFeatures);
        else if (imputationMethod == 2)
            return averageImpute(allFeatures);
        else
            throw new IllegalArgumentException("Imputation method not recognized!");
    }

    public List<Map<String, Double>> noImpute(List<Map<String, Double>> allFeatures) {
        List<Map<String, Double>> allImputedFeatures = new ArrayList<Map<String, Double>>();
        for (Map<String, Double> features : allFeatures) {
            allImputedFeatures.add(imputeOne(features, featureNameMap.keySet()));
        }
        return allImputedFeatures;
    }

    public List<Map<String, Double>> zeroImpute(List<Map<String, Double>> allFeatures) {
        List<Map<String, Double>> allImputedFeatures = new ArrayList<Map<String, Double>>();
        for (Map<String, Double> features : allFeatures) {
            allImputedFeatures.add(imputeOne(features, featureNameMap.keySet()));
        }
        return allImputedFeatures;
    }

    public List<Map<String, Double>> averageImpute(List<Map<String, Double>> allFeatures) {
        Map<String, Integer> featureCounts = new HashMap<String, Integer>();

        List<Map<String, Double>> allImputedFeatures = new ArrayList<Map<String, Double>>();

        for (Map<String, Double> features : allFeatures) {
            for (String featureName : featureNameMap.keySet()) {
                if (features.containsKey(featureName)) {
                    double score = features.get(featureName);
                    String featureType = featureNameMap.get(featureName).getValue2();

                    if (featureType.equals(FeatureType.BINARY.name())) {
                        if (score == 1.0) {
                            if (binaryFeaturePositiveCounts.containsKey(featureName))
                                binaryFeaturePositiveCounts.put(featureName,
                                        binaryFeaturePositiveCounts.get(featureName) + 1);
                            else
                                binaryFeaturePositiveCounts.put(featureName, 1);
                        } else {
                            if (binaryFeatureNegativeCounts.containsKey(featureName))
                                binaryFeatureNegativeCounts.put(featureName,
                                        binaryFeatureNegativeCounts.get(featureName) + 1);
                            else
                                binaryFeatureNegativeCounts.put(featureName, 1);
                        }
                    } else if (featureType.equals(FeatureType.NUMERIC.name())) {
                        if (numericFeatureAverages.containsKey(featureName)) {
                            double oldSum = numericFeatureAverages.get(featureName);
                            numericFeatureAverages.put(featureName, oldSum + score);
                            int oldCount = featureCounts.get(featureName);
                            featureCounts.put(featureName, oldCount + 1);
                        } else {
                            numericFeatureAverages.put(featureName, score);
                            featureCounts.put(featureName, 1);
                        }
                    }
                }
            }
        }

        for (Entry<String, Double> featureAver : numericFeatureAverages.entrySet()) {
            String featureName = featureAver.getKey();
            numericFeatureAverages.put(featureName,
                    featureAver.getValue() / featureCounts.get(featureName));
        }

        for (Map<String, Double> features : allFeatures) {
            Map<String, Double> imputedFeature = new LinkedHashMap<String, Double>();

            for (String featureName : featureNameMap.keySet()) {
                if (features.containsKey(featureName)) {
                    imputedFeature.put(featureName, features.get(featureName));
                } else {
                    if (isFeatureDefaultZero(featureName)) {
                        imputedFeature.put(featureName, 0.0);
                    } else {
                        imputedFeature.put(featureName, getImputedValueAverage(featureName));
                    }
                }
            }
            allImputedFeatures.add(imputedFeature);
        }

        return allImputedFeatures;
    }

    public Map<String, Double> imputeOne(Map<String, Double> features, Set<String> featureNames) {
        if (imputationMethod == 0) {
            return imputeOneNo(features, featureNames);
        } else if (imputationMethod == 1) {
            return imputeOneZero(features, featureNames);
        } else if (imputationMethod == 2) {
            return imputeOneAverage(features, featureNames);
        } else {
            throw new IllegalArgumentException("Imputation method not recognized");
        }
    }

    public Map<String, Double> imputeOneNo(Map<String, Double> features, Set<String> featureNames) {
        Map<String, Double> imputedFeature = new LinkedHashMap<String, Double>();
        for (String featureName : featureNames) {
            if (features.containsKey(featureName)) {
                imputedFeature.put(featureName, features.get(featureName));
            }
        }
        return imputedFeature;
    }

    public Map<String, Double> imputeOneZero(Map<String, Double> features, Set<String> featureNames) {
        Map<String, Double> imputedFeature = new LinkedHashMap<String, Double>();
        for (String featureName : featureNames) {
            if (features.containsKey(featureName)) {
                imputedFeature.put(featureName, features.get(featureName));
            } else {
                imputedFeature.put(featureName, 0.0);
            }
        }

        return imputedFeature;
    }

    public Map<String, Double> imputeOneAverage(Map<String, Double> features, Set<String> featureNames) {
        Map<String, Double> imputedFeature = new LinkedHashMap<String, Double>();
        for (String featureName : featureNames) {
            if (features.containsKey(featureName)) {
                imputedFeature.put(featureName, features.get(featureName));
            } else {
                if (isFeatureDefaultZero(featureName)) {
                    imputedFeature.put(featureName, 0.0);
                } else {
                    imputedFeature.put(featureName, getImputedValueAverage(featureName));
                }
            }
        }

        return imputedFeature;
    }

    private Double getImputedValueAverage(String featureName) {
        String featureType = featureNameMap.get(featureName).getValue2();

        if (featureType.equals(FeatureType.NUMERIC.name())) {
            return numericFeatureAverages.get(featureName);
        } else if (featureType.equals(FeatureType.BINARY.name())) {
            if (binaryFeaturePositiveCounts.get(featureName) > binaryFeatureNegativeCounts
                    .get(featureName)) {
                return 1.0;
            } else {
                return 0.0;
            }
        } else {
            throw new IllegalArgumentException("Wrong feature type: " + featureType);
        }
    }

    private boolean isFeatureDefaultZero(String featureName) {
        return featureNameMap.get(featureName).getValue1();
    }

}
