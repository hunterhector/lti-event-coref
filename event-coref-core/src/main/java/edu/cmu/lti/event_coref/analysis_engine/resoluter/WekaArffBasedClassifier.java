package edu.cmu.lti.event_coref.analysis_engine.resoluter;

import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.pipeline.SudokuInferencePipelineControllerPool;
import edu.cmu.lti.event_coref.type.PairwiseEventCoreferenceEvaluation;
import edu.cmu.lti.event_coref.utils.eval.BlancScore;
import edu.cmu.lti.event_coref.utils.eval.CorefChecker;
import edu.cmu.lti.event_coref.utils.eval.PairwiseScore;
import edu.cmu.lti.event_coref.utils.io.AbstractStepBasedFolderConsumer;
import edu.cmu.lti.event_coref.utils.ml.*;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Collection;
import java.util.Map;

/**
 * A Weka based classifier.
 *
 * @author Zhengzhong Liu, Hector
 */
public class WekaArffBasedClassifier extends AbstractStepBasedFolderConsumer {
    private static final Logger logger = LoggerFactory.getLogger(WekaArffBasedClassifier.class);

    public static final String PARAM_PRE_SAVED_MODEL_FILE_NAME = "modelFileName";

    public static final String PARAM_NEED_TRAINING = "needTraining";

    public static final String PARAM_DO_TUNING = "doTuning";

    public static final String PARAM_IMPUTATHION_METHOD = "imputeMethod";

    public static final String PARAM_ENABLE_FILTERING = "enableFiltering";

    public static final String PARAM_FEAUTURE_NAME_PATH = "FeatureName";

    public static final String PARAM_USE_CROSS_VALIDATION_FOR_TUNING = "CrossValidationForTuning";

    public static final String PARAM_NUMBER_OF_FOLDS = "NumberOfFoldForTuning";

    @ConfigurationParameter(name = PARAM_PRE_SAVED_MODEL_FILE_NAME)
    private String preSavedModelFileName;

    @ConfigurationParameter(name = PARAM_IMPUTATHION_METHOD)
    private Integer imputeMethod;

    @ConfigurationParameter(name = PARAM_ENABLE_FILTERING)
    private boolean enableFiltering;

    @ConfigurationParameter(name = PARAM_FEAUTURE_NAME_PATH)
    private String featureNamePath;

    private int trueFullPositiveCount = 0;

    private int falseFullPositiveCount = 0;

    private int trueNoCorefCount = 0;

    private int falseNoCorefCount = 0;

    private int preFilterErrorCount = 0;

    private Instances fullTrainingData;

    private String traingDirPath;

    private Map<String, Triplet<Integer, Boolean, String>> featureNameMap;

    private FeatureImputator imputer;

    private WekaClassifierWrapper overallClsWrapper;

    private WekaFeatureFactory topTestingFeatureFactory;

    private int defaultWeight = 1;

    private int testInstanceCount = 0;

    @Override
    public void subInitialize() throws Exception {
        featureNameMap = FeatureUtils.getFeatureInfoMap(featureNamePath);
        imputer = new FeatureImputator(imputeMethod, featureNameMap);

        logger.info("Number of features using " + featureNameMap.size());

        topTestingFeatureFactory = new WekaFeatureFactory(featureNameMap);

        logger.info("Loading classifier from file...");
        overallClsWrapper = WekaUtils.loadWrapper(preSavedModelFileName);

        logger.info("Initialized with " + overallClsWrapper.getClassifierType());
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        // will write results to each JCas
        UimaConvenience.printProcessLog(aJCas, logger);
        try {
            testByDocument(aJCas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException {
        // these numbers only concerns the number in this particular step
        SudokuInferencePipelineControllerPool.numberNewCoreference = trueFullPositiveCount
                + falseFullPositiveCount;
        SudokuInferencePipelineControllerPool.preFilterErrorCount = preFilterErrorCount;
        SudokuInferencePipelineControllerPool.truePositiveCount = trueFullPositiveCount;
        SudokuInferencePipelineControllerPool.trueNegativeCount = trueNoCorefCount;
        SudokuInferencePipelineControllerPool.falsePositiveCount = falseFullPositiveCount;
        SudokuInferencePipelineControllerPool.falseNegativeCount = falseNoCorefCount;

        logger.info("Doing a quick evaluation on overall");
        quickEval(trueFullPositiveCount, falseNoCorefCount,
                falseFullPositiveCount, trueNoCorefCount);
        super.collectionProcessComplete();
    }

    private void testByDocument(JCas aJCas) throws Exception {
        Collection<PairwiseEventCoreferenceEvaluation> allPairs = JCasUtil.select(aJCas, PairwiseEventCoreferenceEvaluation.class);

        for (PairwiseEventCoreferenceEvaluation pece : allPairs) {
            testInstanceCount++;

            // if (pece.getIsUnified())
            // continue;

            Map<String, Double> features = FeatureUtils
                    .getPairwiseFeatureMap(pece);

            // only do test on those not classified as coreference
            if (CorefChecker.coreferenceSystemNotSet(pece)) {
                boolean isGoldenFullCoref = CorefChecker.isFullGolden(pece);
                boolean topIsCoref;

                double confidence = 0;
                if (!enableFiltering
                        || FeatureUtils.highPrecisionFiltering(features)) {
                    Map<String, Double> normalizedFeaturesMap = imputer
                            .imputeOne(features, featureNameMap.keySet());

                    Instance instance = topTestingFeatureFactory
                            .createInstance(normalizedFeaturesMap,
                                    defaultWeight);

                    topIsCoref = overallClsWrapper.classifyAsPositive(instance);

                    // add positive class distribution add confidence
                    confidence = overallClsWrapper
                            .getPositiveClassDistribution(instance);

                    logger.info(pece.getEventMentionI().getCoveredText()
                            + " : " + pece.getEventMentionJ().getCoveredText()
                            + " : " + confidence);

                } else {
                    topIsCoref = false;
                    if (isGoldenFullCoref) {
                        preFilterErrorCount++;
                    }
                }

                // so no matter coref or not (judge by the classifier), we just
                // put the confidence score here, then we decide something later
                // during clustering, note that filtering will affect this, if
                // the instance is filtered, the confidence is set to 0
                pece.setConfidence(confidence);

                if (topIsCoref) {
                    pece.setEventCoreferenceRelationSystem(EventCorefConstants.FULL_COREFERENCE_TYPE_IN_PECE);
                    if (isGoldenFullCoref) {
                        trueFullPositiveCount++;
                    } else {
                        falseFullPositiveCount++;
                    }
                } else {
                    // this line is neccessary because during iterations,
                    // previous decision could be
                    // overwritten
                    // pece.setEventCoreferenceRelationSystem(EventCorefConstants.NO_COREFERENCE_TYPE);

                    if (isGoldenFullCoref) {
                        falseNoCorefCount++;
                    } else {
                        trueNoCorefCount++;
                    }
                }
            }
        }
    }

    private static void quickEval(int tp, int fn, int fp, int tn) {
        PairwiseScore pw = new PairwiseScore(tp, fn, fp, tn);
        BlancScore blanc = new BlancScore(tp, fp, fn, tn);
        logger.info("||= =||TP=||=FP=||=TN=||=FN=||=Prec=||=Recall||=F1=||=Prec=||=Recall=||=F1=||");
        logger.info(String.format("||Method name || %d || %d || %d || %d || %.2f || %.2f || %.2f || %.2f || %.2f || %.2f ||",
                tp, fp, tn, fn, pw.getPrecision(), pw.getRecall(), pw.getF1(),
                blanc.getPrecision(), blanc.getRecall(), blanc.getF1()));
    }
}