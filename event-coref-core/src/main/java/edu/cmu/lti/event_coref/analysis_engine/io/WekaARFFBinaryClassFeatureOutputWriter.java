package edu.cmu.lti.event_coref.analysis_engine.io;

import edu.cmu.lti.event_coref.type.EventCoreferenceCluster;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventCoreferenceEvaluation;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.eval.CorefChecker;
import edu.cmu.lti.event_coref.utils.io.AbstractStepBasedFolderWriter;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.event_coref.utils.ml.WekaFeatureFactory;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This writer will write features using Weka ARFF format
 *
 * @author Zhengzhong Liu, Hector
 */
public class WekaARFFBinaryClassFeatureOutputWriter extends AbstractStepBasedFolderWriter {
    private static final Logger logger = LoggerFactory.getLogger(WekaARFFBinaryClassFeatureOutputWriter.class);

    public static final String PARAM_FEATURE_NAME_PATH = "FeatureName";

    public static final String PARAM_FEATURE_FILE_OUTPUT_FILE_NAME = "FeatureFileOutputFileName";

    // public static final String PARAM_FEATURE_SIEVE_OUTPUT_FILE_NAME =
    // "FeatureSieveOutputFileName";

    public static final String PARAM_COREF_TYPE = "CoreferenceType";

    public static final String PARAM_POSITIVE_CLASS_NAME = "PositiveClassName";

    public static final String PARAM_TARGET_PECE_COMPONENT_ID = "targetPeceComponentId";

    @ConfigurationParameter(name = PARAM_TARGET_PECE_COMPONENT_ID, mandatory = true, description = "Target Pece to output, give null will output everything")
    private String targetPeceComponentId;

    @ConfigurationParameter(name = PARAM_POSITIVE_CLASS_NAME, description = "Positive class name", mandatory = true)
    private String postiveClassName;

    @ConfigurationParameter(name = PARAM_COREF_TYPE, description = "Coreference types", mandatory = true)
    private Set<String> coreferenceTypes;

    @ConfigurationParameter(name = PARAM_FEATURE_NAME_PATH, mandatory = true)
    private String featureNamePath;

    @ConfigurationParameter(name = PARAM_FEATURE_FILE_OUTPUT_FILE_NAME, mandatory = true)
    private String featureOutputFileName;

    private Map<String, Triplet<Integer, Boolean, String>> featureNameMap;

    private String featureFileOutputFullPath;

    private int defaultWeight = 1;

    private WekaFeatureFactory featureFactory;

    private HashMap<EventMention, Integer> anaphoricPositions;

    @Override
    public void subInitialize() {
        try {
            featureFileOutputFullPath = outputDir.getCanonicalPath() + "/" + featureOutputFileName + "." + outputFileSuffix;

            logger.debug("Writing features to " + featureFileOutputFullPath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        featureNameMap = FeatureUtils.getFeatureInfoMap(featureNamePath);

        featureFactory = new WekaFeatureFactory(featureNameMap,
                postiveClassName);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);
        anaphoricPositions = new HashMap<EventMention, Integer>();

        for (EventCoreferenceCluster clusters : JCasUtil.select(aJCas, EventCoreferenceCluster.class)) {
            Collection<EventMention> mentionInCluster = FSCollectionFactory.create(clusters.getChildEventMentions(), EventMention.class);

            int anaphoricIndex = -1;
            for (EventMention evm : mentionInCluster) {
                int mentionIndex = evm.getEventMentionIndex();
                if (evm.getBegin() > 0) {
                    if (anaphoricIndex < 0) {
                        anaphoricIndex = mentionIndex;
                    } else if (mentionIndex < anaphoricIndex) {
                        anaphoricIndex = mentionIndex;
                    }
                }
            }

            if (anaphoricIndex != -1) {
                for (EventMention evm : mentionInCluster) {
                    if (evm.getBegin() > 0) {
                        anaphoricPositions.put(evm, anaphoricIndex);
                    }
                }
            }
        }

        for (PairwiseEventCoreferenceEvaluation pece : JCasUtil.select(aJCas, PairwiseEventCoreferenceEvaluation.class)) {
            if (targetPeceComponentId == null || pece.getComponentId().equals(targetPeceComponentId)) {
                FSList featuresFS = pece.getPairwiseEventFeatures();
                Map<String, Double> featuresMap = new HashMap<String, Double>();
                if (featuresFS != null) {
                    for (PairwiseEventFeature feature : FSCollectionFactory.create(featuresFS, PairwiseEventFeature.class)) {
                        featuresMap.put(feature.getName(), feature.getScore());
                    }
                }

                // the feature is automatically added inside the factory
                featureFactory.addInstance(featuresMap, coreferenceTypes.contains(CorefChecker.getCorefTypeGolden(pece)), defaultWeight);
            }
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(featureFactory.getDataset());
        try {
            saver.setFile(new File(featureFileOutputFullPath));
            saver.writeBatch();
            // featureSieveWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}