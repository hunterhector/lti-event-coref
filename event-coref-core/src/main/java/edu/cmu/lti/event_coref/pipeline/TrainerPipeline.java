package edu.cmu.lti.event_coref.pipeline;

import edu.cmu.lti.event_coref.DefaultConfigs;
import edu.cmu.lti.event_coref.analysis_engine.features.FeatureListGenerator;
import edu.cmu.lti.event_coref.analysis_engine.features.PairwiseEventFeatureContainerGenerator;
import edu.cmu.lti.event_coref.analysis_engine.io.WekaARFFBinaryClassFeatureOutputWriter;
import edu.cmu.lti.event_coref.io.ReaderWriterFactory;
import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.pipeline.component.Trainer;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Hector
 *         <p/>
 *         Simply split the data, and write the file into two different directories, but it seems to
 *         be slow using UIMA, directly copying files are much faster
 */
public class TrainerPipeline {
    private static final Logger logger = LoggerFactory.getLogger(TrainerPipeline.class);

    public static void main(String[] args) throws Exception {
        final String className = TrainerPipeline.class.getSimpleName();
        final String resourceDir = "data/resources";

        String processDir = "data/processed/IC_domain/IC_domain_65_articles";
        String paramBaseInitialInputDirName = "xmi_processed";

        String paramTrainDocumentDirName = "xmi_of_training_documents";
        String paramTestDocumentDirName = "xmi_of_testing_documents";
        int featureGeneratedStepNumber = 1;
        int trainingTestStepNumber = featureGeneratedStepNumber + 1;

        String paramTypeSystemDescriptor = DefaultConfigs.TypeSystemDescriptorName;
        Boolean paramFailUnknown = false;
        String paramParentOutputDir = processDir; // Default: same as the input directory
        String paramOutputFileSuffix = ""; // already xmi extension

        Boolean doTuning = true; // tuning only active when training active
        Boolean useCvForTuning = true;
        Integer numberOfFolds = 5;

        Pair<Integer[], Integer[]> documentIndices = getRandomTrainingTestingList(65, 40);

        Integer[] trainingDocumentIndicesArr = documentIndices.getValue0();
        Integer[] testingDocumentIndicesArr = documentIndices.getValue1();

        Set<Integer> testingDocumentIndices = new HashSet<Integer>(
                Arrays.asList(testingDocumentIndicesArr));

        Set<Integer> trainingDocumentIndices = new HashSet<Integer>(
                Arrays.asList(trainingDocumentIndicesArr));

        TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription(paramTypeSystemDescriptor);

        String featureListPath = new File(resourceDir, DefaultConfigs.featureListName).getAbsolutePath(); //"data/resources/feature_lists/featureNames_IC_full.txt";
        String paramFeatureOutputDir = "features_full";

        String trainingDateFile = new File(processDir, "02_arff_features_full/trainingFeatures.arff").getAbsolutePath();
        String modelPath = new File(resourceDir, "models/weka_model_random_forest.ser").getAbsolutePath();

        Set<String> corefTypes = new HashSet<String>();
        corefTypes.add(EventCorefConstants.FULL_COREFERENCE_TYPE_IN_PECE);

        String labelForPositiveClass = EventCorefConstants.FULL_COREFERENCE_TYPE_IN_PECE;

        String targetPece = PairwiseEventFeatureContainerGenerator.ANNOTATOR_COMPONENT_ID;

        logger.info(className + " started...");

        // get feature names
        AnalysisEngineDescription featureNameEngine = AnalysisEngineFactory.createEngineDescription(
                FeatureListGenerator.class, typeSystemDescription,
                FeatureListGenerator.PARAM_OUTPUT_FILE_PATH, featureListPath,
                FeatureListGenerator.PARAM_TARGET_PECE_COMPONENT_ID, targetPece
        );

        // prepare training and testing documents
        CollectionReaderDescription trainTestSplitReader = ReaderWriterFactory.createXmiReader(typeSystemDescription,
                processDir, paramBaseInitialInputDirName, featureGeneratedStepNumber, paramFailUnknown);

        AnalysisEngineDescription trainSplitWriter = ReaderWriterFactory.createSelectiveXmiWriter(
                processDir, paramTrainDocumentDirName, trainingTestStepNumber, paramOutputFileSuffix, trainingDocumentIndices);

        AnalysisEngineDescription testSplitWriter = ReaderWriterFactory.createSelectiveXmiWriter(
                paramParentOutputDir, paramTestDocumentDirName, trainingTestStepNumber, paramOutputFileSuffix, testingDocumentIndices);

        CollectionReaderDescription trainReader = ReaderWriterFactory.createXmiReader(typeSystemDescription,
                processDir, paramTrainDocumentDirName, trainingTestStepNumber, paramFailUnknown);

        CollectionReaderDescription testReader = ReaderWriterFactory.createXmiReader(typeSystemDescription,
                processDir, paramTestDocumentDirName, trainingTestStepNumber, paramFailUnknown);

        AnalysisEngineDescription wekaStyleTrainingWriter = AnalysisEngineFactory.createEngineDescription(
                WekaARFFBinaryClassFeatureOutputWriter.class,
                typeSystemDescription,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_FEATURE_NAME_PATH,
                featureListPath,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_PARENT_OUTPUT_PATH,
                paramParentOutputDir,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_FEATURE_FILE_OUTPUT_FILE_NAME,
                "trainingFeatures",
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_BASE_OUTPUT_DIR_NAME,
                paramFeatureOutputDir,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_OUTPUT_STEP_NUMBER,
                trainingTestStepNumber,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_OUTPUT_FILE_SUFFIX,
                "arff",
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_COREF_TYPE,
                corefTypes,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_POSITIVE_CLASS_NAME,
                labelForPositiveClass,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_TARGET_PECE_COMPONENT_ID,
                targetPece);

        AnalysisEngineDescription wekaStyleTestingWriter = AnalysisEngineFactory.createEngineDescription(
                WekaARFFBinaryClassFeatureOutputWriter.class,
                typeSystemDescription,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_FEATURE_NAME_PATH,
                featureListPath,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_PARENT_OUTPUT_PATH,
                paramParentOutputDir,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_FEATURE_FILE_OUTPUT_FILE_NAME,
                "testingFeatures",
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_BASE_OUTPUT_DIR_NAME,
                paramFeatureOutputDir,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_OUTPUT_STEP_NUMBER,
                trainingTestStepNumber,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_OUTPUT_FILE_SUFFIX,
                "arff",
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_COREF_TYPE,
                corefTypes,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_POSITIVE_CLASS_NAME,
                labelForPositiveClass,
                WekaARFFBinaryClassFeatureOutputWriter.PARAM_TARGET_PECE_COMPONENT_ID,
                targetPece);


        logger.info("Preparing training and testing documents");
        SimplePipeline.runPipeline(trainTestSplitReader, featureNameEngine, trainSplitWriter, testSplitWriter);
        SimplePipeline.runPipeline(trainReader, wekaStyleTrainingWriter);
        SimplePipeline.runPipeline(testReader, wekaStyleTestingWriter);

        Trainer trainer = new Trainer();
        trainer.train(featureListPath, trainingDateFile, doTuning, useCvForTuning, numberOfFolds, modelPath);
    }

    private static Pair<Integer[], Integer[]> getRandomTrainingTestingList(int totalDocCount,
                                                                           int trainingDocCount) throws IllegalArgumentException {
        Integer[] trainingDocs = new Integer[trainingDocCount];
        Integer[] testingDocs = new Integer[totalDocCount - trainingDocCount];

        if (trainingDocCount > totalDocCount) {
            throw new IllegalArgumentException("More training documents than total number of documents");
        }

        int[] values = new int[totalDocCount];

        for (int i = 0; i < values.length; i++) {
            values[i] = i + 1;
        }

        // Generate a random list of documents
        for (int i = values.length - 1; i > 0; i--) {
            int rand = (int) (Math.random() * i);
            int temp = values[i];
            values[i] = values[rand];
            values[rand] = temp;
        }

        logger.debug("Training documents");
        for (int i = 0; i < trainingDocCount; i++) {
            trainingDocs[i] = values[i];
            logger.debug(values[i] + "");
        }

        logger.debug("Totally " + trainingDocs.length);

        logger.debug("Testing documents");
        for (int i = trainingDocCount; i < values.length; i++) {
            testingDocs[i - trainingDocCount] = values[i];
            logger.debug(values[i] + "");
        }
        logger.debug("Totally " + testingDocs.length);

        return new Pair<Integer[], Integer[]>(trainingDocs, testingDocs);
    }
}
