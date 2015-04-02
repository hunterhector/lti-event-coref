package edu.cmu.lti.event_coref.utils.ml;

import org.javatuples.Pair;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

public class WekaRandomForestWrapper extends WekaClassifierWrapper {
    /**
     * Generated uid
     */
    private static final long serialVersionUID = -2892077027606690443L;

    // weighted average value on a 5-fold cv
    double forestThreshold = 0.2337950409729744;

    public WekaRandomForestWrapper(Instances trainingData,
                                   boolean tuneParameter, int numberOfFold, boolean useCv)
            throws Exception {
        super(trainingData, tuneParameter, numberOfFold, useCv);
    }

    @Override
    protected String[] prepareOptions(Instances trainingData,
                                      boolean tuneParameter, int fold, boolean useCv) {
        String[] options = new String[2];
        options[0] = "-I";
        options[1] = "11";
        return options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.cmu.lti.evnet_coref.ml.weka.WekaClassifierWrapper#setUpRawClassifier
     * ()
     */
    @Override
    protected Classifier configRawClassifier() throws Exception {
        Classifier rf = new RandomForest();
        return rf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.cmu.lti.evnet_coref.ml.weka.WekaClassifierWrapper#tune(weka.classifiers
     * .Classifier, weka.core.Instances, weka.core.Instances)
     */
    @Override
    protected void tuneAndTrain(Classifier cls, Instances trainingAndDevData,
                                int fold, boolean useCv) throws Exception {
        int numPositive = 0;
        for (int i = 0; i < trainingAndDevData.numInstances(); i++) {
            Instance trainInstance = trainingAndDevData.instance(i);
            if (trainInstance.classValue() == 1.0) {
                numPositive++;
            }
        }
        logger.info(String.format("Training and Tuning RandomForest, total training and development instances: %d, " +
                "positive examples : %s ", trainingAndDevData.numInstances(), numPositive));

        Instances dataFolds = randomizeData(trainingAndDevData, fold);
        int cvIter = useCv ? fold : 1;

        double weightedThresholdSum = 0;
        double normalizer = 0;
        for (int iter = 0; iter < cvIter; iter++) {
            double tempThreshold = 0;
            double tempF1 = 0;
            Instances trainingData = dataFolds.trainCV(fold, iter);
            Instances devData = dataFolds.testCV(fold, iter);
            logger.debug(String.format("%d training data, %d dev data", trainingData.numInstances(), devData.numInstances()));
            cls.buildClassifier(trainingData);
            Pair<Double, Double> result = findBestF1FromSortedProbs(cls, devData);
            tempThreshold = result.getValue0();
            tempF1 = result.getValue1();
            normalizer += tempF1;
            weightedThresholdSum += tempThreshold * tempF1;
            logger.info("Best threshold find by hold out " + tempThreshold);
        }
        // get an average over the threshold
        forestThreshold = weightedThresholdSum / normalizer;

        logger.info("Final threshold after tuning is " + forestThreshold);

        // final training with all data
        train(cls, trainingAndDevData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.cmu.lti.evnet_coref.ml.weka.WekaClassifierWrapper#classify(weka.core
     * .Instance)
     */
    @Override
    public double classify(Instance instance) throws Exception {
        double predictedClassLabel;

        double[] distributions = wrappedClassifier
                .distributionForInstance(instance);

        double trueProb = distributions[1];

        if (trueProb < forestThreshold) {
            predictedClassLabel = 0.0;
        } else {
            predictedClassLabel = 1.0;
        }

        return predictedClassLabel;
    }

}