package edu.cmu.lti.event_coref.pipeline;

import edu.cmu.lti.event_coref.analysis_engine.eval.ConllFormatWriter;
import edu.cmu.lti.event_coref.analysis_engine.resoluter.FullCoreferenceClusterTransitivityCreator;
import edu.cmu.lti.event_coref.analysis_engine.resoluter.SudokuUnificationAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.resoluter.WekaArffBasedClassifier;
import edu.cmu.lti.event_coref.io.ReaderWriterFactory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test version only run the testing procedures
 *
 * @author Zhengzhong Liu, Hector
 */
public class SudokuWekaBasedInferencePipelineTestVersion {
    private static final Logger logger = LoggerFactory.getLogger(SudokuWekaBasedInferencePipelineTestVersion.class);

    public static void main(String[] args)
            throws ResourceInitializationException {
        final String className = SudokuWekaBasedInferencePipelineTestVersion.class
                .getSimpleName();

        // ////////////// Parameter Setting for corefernece engine
        // /////////////////
        // Note that you should change the parameters below for your
        // configuration.
        // /////////////////////////////////////////////////////////////////////////
        // Parameters for the initial reader
        String paramParentInputDir = "../edu.cmu.lti.event_coref.system/data/event_coreference_resolution/simpson_sample";
        // String paramParentInputDir =
        // "../edu.cmu.lti.event_coref.system/data/event_coreference_resolution/FrameNet";
        // String paramParentInputDir =
        // "../edu.cmu.lti.event_coref.system/data/bio_extraction/Bio_domain_yukari_corrected";

        // String paramTrainDocumentDirName = "xmi_of_training_documents";
        // String paramTestDocumentDirName = "xmi_of_testing_documents";
        String paramTestDocumentDirName = "feature_generated";

        String dataSplitDate = "20141013"; // data of splitting the training and
        // testing documents
        Integer testingDocumentStepNumber = 5;

        String paramTypeSystemDescriptor = "EventCoreferenceAllTypeSystem";
        Boolean paramFailUnknown = false;
        String paramParentOutputDir = paramParentInputDir;
        String paramBaseOutputDirName = "xmi_after_sudoku_inference_with_weka";
        String paramOutputFileSuffix = ""; // already xmi extension

        String featureNameFile = "../edu.cmu.lti.event_coref.ml.PairwiseEventFeatureGenerator/resources/feature_lists/featureNames_IC_full.txt";

        // Parameters about generated features
        // String paramModelName = "weka_model_random_forest_for_Firestone";
        // String paramModelName = "weka_model_random_forest_yukari_old";
        String paramModelName = "../edu.cmu.lti.event_coref.system/data/event_coreference_resolution/IC_train_test_model/weka_model_random_forest_for_full_top";
        String paramFeatureBaseDir = "testing_features_full";
        String featureWriteOutDate = "20141013"; // date of getting the features
        String paramFeatureFileSuffix = "arff";

        int featurelizedStep = testingDocumentStepNumber + 1;

        // evaluation parameters
        Boolean verboseEvaluation = true;

        // and then output a CoNLL file for other evaluation
        String paramConllEvalFileDir = "conll_format";
        String conllOutputFilePrefix = "txt";

        // training parameters
        /**
         * case 0: no impute, 1: zero impute, 2: average impute
         */
        Integer imputationMethod = 0;
        Boolean reTraining = false;
        Boolean doTuning = true; // tuning only active when training active
        Boolean useCvForTuning = true;
        Integer numberOfFolds = 5;

        // unification parameters
        Boolean doUnification = true;
        Boolean needToUpdateFeature = true;
        Float unificationConfidenceThreshold = (float) 0.5;
        Boolean useFiltering = false;
        Integer unificationVerboseLevel = 1;
        Integer clusterMethod = 0;

        int maxIter = 2; // running it 1s means not iterating

        // Conll output parameters
        String paramGoldViewName = "GoldStandard";
        String paramSystemPrefix = "System";
        Boolean paramInludeSingleton = true;

        // /////////////////////////////////////////////////////////////////////////

        TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription(paramTypeSystemDescriptor);

        // Run the iterations.
        try {
            int iter = 0;

            // Initial input/output setting
            String paramDate = dataSplitDate;
            int paramInputStepNumber = testingDocumentStepNumber;
            int paramOutputStepNumber = featurelizedStep + 1;
            String coreferenceInputBase = paramTestDocumentDirName;
            String unificationInputBase;
            String finalDirectoryBase = null;

            // Do sudoku iteration
            logger.info("Sudoku Iteration started");

            while (iter < maxIter) {
                // Coreference write to unification input
                unificationInputBase = paramBaseOutputDirName + "_iter_" + iter
                        + "_coreference";

                // initialialize the coreference engine
                CollectionReaderDescription resoluterReader = ReaderWriterFactory
                        .createXmiReader(paramParentInputDir,
                                coreferenceInputBase,
                                paramInputStepNumber, paramFailUnknown);

                AnalysisEngineDescription resoluterEngine = AnalysisEngineFactory
                        .createEngineDescription(
                                WekaArffBasedClassifier.class, typeSystemDescription,
                                WekaArffBasedClassifier.PARAM_PARENT_RESOURCE_INPUT_PATH, paramParentInputDir,
                                WekaArffBasedClassifier.PARAM_BASE_RESOURCE_INPUT_DIR_NAME, paramFeatureBaseDir,
                                WekaArffBasedClassifier.PARAM_RESOURCE_INPUT_STEP_NUMBER, featurelizedStep,
                                WekaArffBasedClassifier.PARAM_RESOURCE_INPUT_FILE_SUFFIX, paramFeatureFileSuffix,
                                WekaArffBasedClassifier.PARAM_NEED_TRAINING, reTraining,
                                WekaArffBasedClassifier.PARAM_ENABLE_FILTERING, useFiltering,
                                WekaArffBasedClassifier.PARAM_DO_TUNING, doTuning,
                                WekaArffBasedClassifier.PARAM_IMPUTATHION_METHOD, imputationMethod,
                                WekaArffBasedClassifier.PARAM_USE_CROSS_VALIDATION_FOR_TUNING, useCvForTuning,
                                WekaArffBasedClassifier.PARAM_NUMBER_OF_FOLDS, numberOfFolds,
                                WekaArffBasedClassifier.PARAM_FEAUTURE_NAME_PATH, featureNameFile,
                                WekaArffBasedClassifier.PARAM_PRE_SAVED_MODEL_FILE_NAME, paramModelName
                        );

                AnalysisEngineDescription resoluterWriter = ReaderWriterFactory
                        .createXmiWriter(paramParentOutputDir,
                                unificationInputBase, paramOutputStepNumber,
                                paramOutputFileSuffix);

                SimplePipeline.runPipeline(resoluterReader, resoluterEngine,
                        resoluterWriter);

                // because we are looping, change some parameters accordingly
                paramDate = null;
                paramInputStepNumber = paramOutputStepNumber;

                // use the saved model, don't train again
                reTraining = false;

                // Do unification
                finalDirectoryBase = unificationInputBase;

                // Unification write to coreference input
                coreferenceInputBase = paramBaseOutputDirName + "_iter_" + iter
                        + "_unification";

                logger.info("Unification from " + unificationInputBase);

                // Instantiate the unification engine
                CollectionReaderDescription uniReader = ReaderWriterFactory.createXmiReader(paramParentInputDir,
                        unificationInputBase, paramInputStepNumber, paramFailUnknown);
                AnalysisEngineDescription uniEngine = AnalysisEngineFactory.createEngineDescription(
                        SudokuUnificationAnnotator.class, typeSystemDescription,
                        SudokuUnificationAnnotator.PARAM_UPDATE_FEATURES, needToUpdateFeature,
                        SudokuUnificationAnnotator.PARAM_UNIFICATION_CONFIDENCE_THRESHOLD, unificationConfidenceThreshold,
                        SudokuUnificationAnnotator.PARAM_DO_UNIFICATION, doUnification,
                        SudokuUnificationAnnotator.PARAM_VERBOSE_LEVEL, unificationVerboseLevel,
                        SudokuUnificationAnnotator.PARAM_CLUSTER_METHOD, clusterMethod);
                AnalysisEngineDescription uniWriter = ReaderWriterFactory.createXmiWriter(paramParentOutputDir,
                        coreferenceInputBase, paramOutputStepNumber, paramOutputFileSuffix);

                SimplePipeline.runPipeline(uniReader, uniEngine, uniWriter);

                finalDirectoryBase = coreferenceInputBase;
                if (SudokuInferencePipelineControllerPool.unifiedEventCount == 0) {
                    System.err.println("Stop when no new unificiation");
                    break;
                }

                iter++;

                // also dump the temporary result without transitive closure
                SudokuInferencePipelineControllerPool.dump();
            }

//            // finally, do a evaluation
//            CollectionReaderDescription evalReader = ReaderWriterFactory
//                    .createXmiReader(paramParentInputDir,
//                            finalDirectoryBase,
//                            paramOutputStepNumber, paramFailUnknown);
//            AnalysisEngineDescription evalEngine = EventCorefAnalysisEngineFactory
//                    .createAnalysisEngine(FullCoreferenceEvaluator.class,
//                            typeSystemDescription,
//                            FullCoreferenceEvaluator.PARAM_VERBOSE,
//                            verboseEvaluation);
//            SimplePipeline.runPipeline(evalReader, evalEngine);

            paramInputStepNumber = paramOutputStepNumber;
            paramOutputStepNumber++;

            CollectionReaderDescription resultReader = ReaderWriterFactory
                    .createXmiReader(paramParentInputDir,
                            finalDirectoryBase,
                            paramInputStepNumber, paramFailUnknown);

            AnalysisEngineDescription conllWriter = AnalysisEngineFactory
                    .createEngineDescription(ConllFormatWriter.class, typeSystemDescription,
                            ConllFormatWriter.PARAM_PARENT_OUTPUT_PATH, paramParentOutputDir,
                            ConllFormatWriter.PARAM_OUTPUT_STEP_NUMBER, paramOutputStepNumber,
                            ConllFormatWriter.PARAM_BASE_OUTPUT_DIR_NAME, paramConllEvalFileDir,
                            ConllFormatWriter.PARAM_OUTPUT_FILE_SUFFIX, conllOutputFilePrefix,
                            ConllFormatWriter.PARAM_INCLUDE_SINGLETON, paramInludeSingleton,
                            ConllFormatWriter.PARAM_GOLD_STANDARD_VIEWNAME, "GoldStandard",
                            ConllFormatWriter.PARAM_GOLD_COMPONENT_ID_PREFIX, paramGoldViewName,
                            ConllFormatWriter.PARAM_SYSTEM_COMPONENT_ID_PREFIX, paramSystemPrefix
                    );

            AnalysisEngineDescription clusterEngine = AnalysisEngineFactory
                    .createEngineDescription(
                            FullCoreferenceClusterTransitivityCreator.class,
                            typeSystemDescription);

            AnalysisEngineDescription clusterWriter = ReaderWriterFactory
                    .createXmiWriter(paramParentOutputDir, "xmi_after_annotating_system_clusters", paramOutputStepNumber, "");

            // ConllFormatWriter.PARAM_GOLD_STANDARD_VIEWNAME, "GoldStandard");
            SimplePipeline.runPipeline(resultReader, clusterEngine,
                    conllWriter, clusterWriter);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        logger.info(className + " successfully completed.");
    }
}