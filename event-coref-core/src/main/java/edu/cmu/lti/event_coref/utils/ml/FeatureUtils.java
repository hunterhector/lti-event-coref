package edu.cmu.lti.event_coref.utils.ml;

import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.ConstantUtils;
import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.FeatureType;
import edu.cmu.lti.utils.general.ErrorUtils;
import edu.cmu.lti.utils.general.FileUtils;
import edu.cmu.lti.utils.general.StringUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.javatuples.Triplet;
import org.uimafit.util.FSCollectionFactory;

import java.util.*;

/**
 * Utilities for dealing with features
 *
 * @author Zhengzhong Liu, Hector
 * @author Jun Araki
 */
public class FeatureUtils {

    /**
     * Creates a pairwise numeric feature with the specified feature name, pairwise value and score.
     *
     * @param aJCas
     * @param featureName
     * @param score
     * @return a pairwise numeric feature with the specified feature name, pairwise value and score
     */
    public static PairwiseEventFeature createPairwiseEventNumericFeature(JCas aJCas,
                                                                         String featureName, Double score, boolean defaultZero) {
        PairwiseEventFeature feature = new PairwiseEventFeature(aJCas);
        feature.setName(featureName);
        feature.setScore(score);
        feature.setFeatureType(FeatureConstants.FeatureType.NUMERIC.name());
        feature.setDefaultZero(defaultZero);
        feature.addToIndexes(aJCas);
        return feature;
    }

    public static PairwiseEventFeature createPairwiseEventBinaryFeature(JCas aJCas,
                                                                        String featureName, boolean isPositive, boolean defaultZero) {
        PairwiseEventFeature feature = new PairwiseEventFeature(aJCas);
        feature.setName(featureName);

        double score = isPositive ? FeatureConstants.POSITIVE_BINARY_FEATURE_VALUE
                : FeatureConstants.NEGATIVE_BINARY_FEATURE_VALUE;

        feature.setScore(score);
        feature.setFeatureType(FeatureType.BINARY.name());
        feature.setDefaultZero(defaultZero);
        feature.addToIndexes(aJCas);
        return feature;
    }

    /**
     * Creates a pairwise event feature.
     *
     * @param aJCas
     * @param featureInfo
     * @param score
     * @param defaultZero
     * @return a pairwise event feature
     */
    public static PairwiseEventFeature createPairwiseEventFeature(JCas aJCas,
                                                                  FeatureConstants.PairwiseEventFeatureInfo featureInfo, Double score, boolean defaultZero) {
        String featureName = featureInfo.getFeatureName();
        if (featureInfo.isBinaryFeature()) {
            boolean isPositive = (score == FeatureConstants.POSITIVE_BINARY_FEATURE_VALUE);
            return createPairwiseEventBinaryFeature(aJCas, featureName, isPositive, defaultZero);
        } else if (featureInfo.isNumericFeature()) {
            return createPairwiseEventNumericFeature(aJCas, featureName, score, defaultZero);
        } else {
            ErrorUtils.terminate("Invalid feature: feature + " + featureName
                    + " is specified as neither binary nor numeric.");
            // Terminates here, but return null for avoiding a compile error.
            return null;
        }
    }

    /**
     * Adds the specified feature to the specified pairwise event coreference evaluation.
     *
     * @param aJCas
     * @param pece
     * @param newFeature
     */
    public static void addPairwiseEventFeatures(JCas aJCas, PairwiseEventCoreferenceEvaluation pece,
                                                PairwiseEventFeature newFeature) {
        List<PairwiseEventFeature> pairwiseEventFeatures = null;
        FSList pairwiseEventFeatureList = pece.getPairwiseEventFeatures();
        if (pairwiseEventFeatureList == null) {
            pairwiseEventFeatures = new ArrayList<PairwiseEventFeature>();
        } else {
            pairwiseEventFeatures = new ArrayList<PairwiseEventFeature>(FSCollectionFactory.create(
                    pairwiseEventFeatureList, PairwiseEventFeature.class));
        }

        pairwiseEventFeatures.add(newFeature);
        pairwiseEventFeatureList = FSCollectionFactory.createFSList(aJCas, pairwiseEventFeatures);
        pece.setPairwiseEventFeatures(pairwiseEventFeatureList);
        pece.addToIndexes();
    }

    public static SingleEventFeature createSingleEventBinaryFeature(JCas aJCas, String featureName,
                                                                    boolean isPositive, boolean defaultZero) {
        SingleEventFeature feature = new SingleEventFeature(aJCas);
        feature.setName(featureName);
        double score = isPositive ? FeatureConstants.POSITIVE_BINARY_FEATURE_VALUE
                : FeatureConstants.NEGATIVE_BINARY_FEATURE_VALUE;
        feature.setScore(score);
        feature.setDefaultZero(defaultZero);
        feature.setFeatureType(FeatureType.BINARY.name());
        feature.addToIndexes(aJCas);
        return feature;
    }

    public static SingleEventFeature createSingleEventNumericFeature(JCas aJCas, String featureName,
                                                                     double featureValue, boolean defaultZero) {
        SingleEventFeature feature = new SingleEventFeature(aJCas);
        feature.setName(featureName);
        feature.setScore(featureValue);
        feature.setDefaultZero(defaultZero);
        feature.setFeatureType(FeatureType.NUMERIC.name());
        feature.addToIndexes(aJCas);
        return feature;
    }

    /**
     * Reads feature info in the specified file, and returns a feature set. As input, the file has to
     * have the following four pieces of information: feature id, feature name, default zero, and
     * feature type.
     *
     * @param featureInfoFilePath
     * @return
     */
    public static FeatureSet getFeatureSet(String featureInfoFilePath) {
        FeatureSet featureSet = new FeatureSet();

        List<String> lines = FileUtils.readLines(featureInfoFilePath);
        for (String line : lines) {
            if (line.startsWith("#".toString())) {
                // Skip comments.
                continue;
            }

            String[] tmp = line.split(",");
            ErrorUtils.terminateIfFalse(tmp.length == 4, "Invalid feature info: " + line);

            String featureName = tmp[1];
            boolean defaultZero = tmp[2].equals("true") ? true : false;
            FeatureType featureType = ConstantUtils.getConstant(FeatureType.class, tmp[3]);

            Feature f = new Feature.Builder(featureName, featureType).defaultZero(defaultZero).build();
            featureSet.addFeature(f);
        }

        return featureSet;
    }

    /**
     * Reads the feature data, the map returned is sorted by the sequence in the feature file
     *
     * @param featureMapFile
     */
    public static Map<String, Triplet<Integer, Boolean, String>> getFeatureInfoMap(
            String featureMapFile) {
        // NOTE: it is important to create a LinkedHashMap here to ensure sequence of features is always
        // consistent in all reference to this. (Although hash value sequence is the same in current
        // Java implementation, it is not guaranteed in the long run.)
        Map<String, Triplet<Integer, Boolean, String>> featureInfoMap = new LinkedHashMap<String, Triplet<Integer, Boolean, String>>();

        List<String> lines = FileUtils.readLines(featureMapFile);

        int featureCount = 0;
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            featureCount++;

            String[] tmp = line.split(",");
            // String featureIdStr = tmp[0];
            String featureName = tmp[1];
            boolean featureDefaultZero = tmp[2].equals("true") ? true : false;
            String featureType = tmp[3];

            // ErrorUtils.terminateIfTrue(!StringUtils.isInteger(featureIdStr), "Invalid feature ID: "
            // + featureIdStr);
            //
            // int featureId = StringUtils.convertStringToInteger(featureIdStr);

            Triplet<Integer, Boolean, String> featureTriple = new Triplet<Integer, Boolean, String>(
                    featureCount, featureDefaultZero, featureType);

            featureInfoMap.put(featureName, featureTriple);
        }

        return featureInfoMap;
    }

    /**
     * Returns a map from feature ID to feature information, given the specified feature data file.
     *
     * @param featureMapFile
     */
    public static Map<Integer, Triplet<String, Boolean, String>> getFeatureIdToFeatureInfoMap(
            String featureMapFile) {
        // NOTE: it is important to create a LinkedHashMap here to ensure sequence of features is always
        // consistent in all reference to this. (Although hash value sequence is the same in current
        // Java implementation, it is not guaranteed in the long run.)
        Map<Integer, Triplet<String, Boolean, String>> featureInfoMap = new LinkedHashMap<Integer, Triplet<String, Boolean, String>>();

        List<String> lines = FileUtils.readLines(featureMapFile);
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }

            String[] tmp = line.split(",");
            String featureIdStr = tmp[0];
            String featureName = tmp[1];
            boolean featureDefaultZero = tmp[2].equals("true") ? true : false;
            String featureType = tmp[3];

            ErrorUtils.terminateIfTrue(!StringUtils.isInteger(featureIdStr), "Invalid feature ID: "
                    + featureIdStr);

            int featureId = StringUtils.convertStringToInteger(featureIdStr);

            Triplet<String, Boolean, String> featureTriple = new Triplet<String, Boolean, String>(
                    featureName, featureDefaultZero, featureType);

            featureInfoMap.put(featureId, featureTriple);
        }

        return featureInfoMap;
    }

    /**
     * Create a featureMap from PairwiseCoreferenceEvaluation, null is not checked, the pece must come
     * with a feature annotated
     *
     * @param pece
     * @return
     */
    public static Map<String, Double> getPairwiseFeatureMap(PairwiseEventCoreferenceEvaluation pece) {
        FSList featuresFS = pece.getPairwiseEventFeatures();

        HashMap<String, Double> featureMap = new HashMap<String, Double>();
        for (PairwiseEventFeature feature : FSCollectionFactory.create(featuresFS,
                PairwiseEventFeature.class)) {
            featureMap.put(feature.getName(), feature.getScore());
        }

        return featureMap;
    }

    public static Map<String, Double> createSingleEventFeatureMap(EventMention evm) {
        FSList featuresFs = evm.getSingleEventFeatures();
        Map<String, Double> featuresMap = new HashMap<String, Double>();

        if (featuresFs != null) {
            for (SingleEventFeature singleFeat : FSCollectionFactory.create(featuresFs,
                    SingleEventFeature.class)) {
                featuresMap.put(singleFeat.getName(), singleFeat.getScore());
            }
        }
        return featuresMap;
    }

    public static boolean highPrecisionFiltering(Map<String, Double> features) {
        boolean isambigousExample = false;

        if (features.containsKey("eventSurfaceSennaSimilarity")) {
            if (features.get("eventSurfaceSennaSimilarity") > 0.2)
                isambigousExample = true;
        }
        if (features.containsKey("eventSurfaceCannoicRelaxedDice")) {
            if (features.get("eventSurfaceCannoicRelaxedDice") > 0.2)
                isambigousExample = true;
        }
        if (features.containsKey("EventSurfaceWordNetSimilarity")) {
            if (features.get("EventSurfaceWordNetSimilarity") > 0.2)
                isambigousExample = true;
        }
        if (features.containsKey("semantic_database_similarity")) {
            if (features.get("semantic_database_similarity") == 1.0)
                isambigousExample = true;
        }

        return isambigousExample;
    }

    public static boolean isPositiveBinaryFeatureValue(double binaryFeatureValue) {
        return (binaryFeatureValue == FeatureConstants.POSITIVE_BINARY_FEATURE_VALUE);
    }

    public static boolean isNegativeBinaryFeatureValue(double binaryFeatureValue) {
        return (binaryFeatureValue == FeatureConstants.NEGATIVE_BINARY_FEATURE_VALUE);
    }

    public static String getFeatureEntry(PairwiseEventCoreferenceEvaluation pece, int classLabel,
                                         Map<String, Triplet<Integer, Boolean, String>> featureInfoMap) {
        StringBuilder buf = new StringBuilder();

        buf.append(classLabel);
        FSList pairwiseEventFeatureList = pece.getPairwiseEventFeatures();
        Collection<PairwiseEventFeature> pairwiseEventFeatures = FSCollectionFactory.create(
                pairwiseEventFeatureList, PairwiseEventFeature.class);
        for (PairwiseEventFeature pairwiseEventFeature : pairwiseEventFeatures) {
            String featureName = pairwiseEventFeature.getName();
            // Do nothing against feature names out of the list.
            if (!featureInfoMap.containsKey(featureName)) {
                continue;
            }

            double score = pairwiseEventFeature.getScore();

            int featureId = featureInfoMap.get(featureName).getValue0();

            buf.append(" ");
            buf.append(featureId);
            buf.append(":");
            buf.append(score);
        }

        return buf.toString();
    }

    public static String getFeatureEntryFromFeatureIdToFeatureInfoMap(
            PairwiseEventCoreferenceEvaluation pece, int classLabel,
            Map<Integer, Triplet<String, Boolean, String>> featureInfoMap) {
        StringBuilder buf = new StringBuilder();

        buf.append(classLabel);
        FSList pairwiseEventFeatureList = pece.getPairwiseEventFeatures();
        Collection<PairwiseEventFeature> pairwiseEventFeatures = FSCollectionFactory.create(
                pairwiseEventFeatureList, PairwiseEventFeature.class);
        for (PairwiseEventFeature pairwiseEventFeature : pairwiseEventFeatures) {
            String featureName = pairwiseEventFeature.getName();

            boolean foundFeature = false;
            int featureId = 0;
            for (int _featureId : featureInfoMap.keySet()) {
                String _featureName = featureInfoMap.get(_featureId).getValue0();
                if (featureName.equals(_featureName)) {
                    foundFeature = true;
                    featureId = _featureId;
                    break;
                }
            }

            // Do nothing against feature names out of the list.
            if (!foundFeature) {
                continue;
            }

            double score = pairwiseEventFeature.getScore();

            buf.append(" ");
            buf.append(featureId);
            buf.append(":");
            buf.append(score);
        }

        return buf.toString();
    }

    public static String getFeatureEntryWithComment(Article article,
                                                    PairwiseEventCoreferenceEvaluation pece, int classLabel,
                                                    Map<String, Triplet<Integer, Boolean, String>> featureInfoMap) {

        EventMention event1 = pece.getEventMentionI();
        EventMention event2 = pece.getEventMentionJ();

        StringBuilder buf = new StringBuilder();
        buf.append(article.getArticleName());
        buf.append("_");
        buf.append("E");
        buf.append(event1.getGoldStandardEventMentionId());
        buf.append("_");
        buf.append(event1.getCoveredText().replaceAll("\\s", "_"));
        buf.append("==");
        buf.append(article.getArticleName());
        buf.append("_");
        buf.append("E");
        buf.append(event2.getGoldStandardEventMentionId());
        buf.append("_");
        buf.append(event2.getCoveredText().replaceAll("\\s", "_"));
        String comment = buf.toString();

        buf = new StringBuilder();
        buf.append(getFeatureEntry(pece, classLabel, featureInfoMap));
        buf.append(" ");
        buf.append("#");
        buf.append(comment);

        return buf.toString();

    }

    public static boolean validBinaryFeature(
            Map<Integer, Triplet<String, Boolean, String>> featureInfoMap, int featureId,
            double featureValue) {
        ErrorUtils.terminateIfFalse(featureInfoMap.containsKey(featureId), "Invalid feature ID: "
                + featureId);
        if (FeatureType.BINARY.toString().equals(featureInfoMap.get(featureId).getValue2())) {
            if (featureValue != FeatureConstants.POSITIVE_BINARY_FEATURE_VALUE
                    && featureValue != FeatureConstants.NEGATIVE_BINARY_FEATURE_VALUE) {
                ErrorUtils.terminate("Invalid feature value for feature " + featureId);
            }

            return true;
        }

        return false;
    }

}
