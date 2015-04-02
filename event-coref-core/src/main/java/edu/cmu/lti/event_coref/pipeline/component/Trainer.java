package edu.cmu.lti.event_coref.pipeline.component;

import com.google.common.base.Joiner;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.event_coref.utils.ml.WekaFeatureFactory;
import edu.cmu.lti.event_coref.utils.ml.WekaRandomForestWrapper;
import edu.cmu.lti.event_coref.utils.ml.WekaUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The train version only call the training procedures, it will use the test
 * documents to do some have a evaluation on the same time
 *
 * @author Zhengzhong Liu, Hector
 */
public class Trainer {
    private static final Logger logger = LoggerFactory.getLogger(Trainer.class);

    public void train(String featureNameFile, String trainingFilePath, boolean doTuning, boolean useCv, int numberOfFolds, String modelOutputPath) throws Exception {

        File modelDir = new File(modelOutputPath).getParentFile();
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }

        Map<String, Triplet<Integer, Boolean, String>> featureNameMap = FeatureUtils.getFeatureInfoMap(featureNameFile);
        WekaFeatureFactory topTestingFeatureFactory = new WekaFeatureFactory(featureNameMap);
        Instances fullTrainingData = WekaUtils.readArffData(trainingFilePath, topTestingFeatureFactory);
        WekaRandomForestWrapper overallClsWrapper = new WekaRandomForestWrapper(fullTrainingData, doTuning, numberOfFolds, useCv);
        SerializationHelper.write(modelOutputPath, overallClsWrapper);
    }

    public static void main(String[] args) throws Exception {
        final String className = Trainer.class.getSimpleName();

        String paramParentInputDir = "data/processed/IC_domain/IC_domain_65_articles";
        String featureNameFile = "data/resources/feature_lists/featureNames_IC_full.txt";

        // Parameters about generated features
        String modelPath = "data/resources/models/weka_model_random_forest";
        String paramFeatureBaseDir = "after_generating_training_testing_features";
        String paramFeatureFileSuffix = "txt";
        String paramTrainingFileName = "trainingFeatures_arff.txt";
        int featurelizedStep = 2;

        Boolean doTuning = true; // tuning only active when training active
        Boolean useCvForTuning = true;
        Integer numberOfFolds = 5;

        List<String> dirNameSegments = new ArrayList<String>();
        dirNameSegments.add(String.format("%02d", featurelizedStep));
        dirNameSegments.add(String.format(paramFeatureFileSuffix));
        dirNameSegments.add(paramFeatureBaseDir);

        File resourceDir = new File(paramParentInputDir + "/" + Joiner.on("_").join(dirNameSegments));

        Trainer trainer = new Trainer();
        trainer.train(featureNameFile, new File(resourceDir, paramTrainingFileName).getAbsolutePath(), doTuning, useCvForTuning, numberOfFolds, modelPath);

        logger.info(className + " successfully completed.");
    }
}