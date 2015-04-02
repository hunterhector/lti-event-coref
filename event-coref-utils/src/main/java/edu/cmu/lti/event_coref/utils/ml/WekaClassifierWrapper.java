package edu.cmu.lti.event_coref.utils.ml;

import edu.cmu.lti.event_coref.utils.eval.PairwiseScore;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.StratifiedRemoveFolds;

import java.io.Serializable;
import java.util.*;

/**
 * A wrapper that wrap weka classification and learning into this one implementation Subclasses need
 * to implement the {@link #configRawClassifier()} method to set up a suitable type of classifier,
 * and the {@link #tuneAndTrain(Classifier, Instances, int, boolean)} method to use the provided training and
 * development set to tune the classifier.
 * <p/>
 * When neccessary, override {@link #classify(Instance)} method to provide a custom classification
 * method, which might try to consider other parameters (for example, consider Naive Bayes
 * confidence for positive class)
 *
 * @author Zhengzhong Liu, Hector
 */
public abstract class WekaClassifierWrapper implements Serializable {
    protected static final Logger logger = LoggerFactory.getLogger(WekaClassifierWrapper.class);

    /**
     * Generated serialUID
     */
    private static final long serialVersionUID = 990584989685611722L;

    protected Classifier wrappedClassifier;

    private int numberOfFeatures;

    private double threshold;

    /**
     * Create a classifier wrapper with the training data
     *
     * @param trainingData  Provided training data, will be used as train and dev
     * @param fold          Number of folds used in tuning
     * @param tuneParameter Whether to do parameter tuning
     * @throws Exception
     */
    public WekaClassifierWrapper(Instances trainingData, boolean tuneParameter, int fold,
                                 boolean useCv) throws Exception {
        String[] options = prepareOptions(trainingData, tuneParameter, fold, useCv);
        Classifier rawCls = configRawClassifier();
        if (options != null)
            rawCls.setOptions(options);

        numberOfFeatures = trainingData.numAttributes();

        if (tuneParameter) {
            tuneAndTrain(rawCls, trainingData, fold, useCv);
        } else {
            train(rawCls, trainingData);
        }

    }

    /**
     * Return the randomized folded data
     *
     * @param allData
     * @param fold
     * @return
     */
    protected Instances randomizeData(Instances allData, int fold) {
        long seed = System.currentTimeMillis();
        Random rand = new Random(seed);
        Instances randData = new Instances(allData);
        randData.randomize(rand);
        randData.stratify(fold);

        return randData;
    }

    /**
     * Split data into two parts
     *
     * @param trainingData
     * @param fold         Number of folds
     * @return A list of two sets, index 0 is the majority (use as train), and index 1 is only 1 fold
     * (use as dev)
     * @throws Exception
     */
    protected List<Instances> splitData(Instances trainingData, int fold) throws Exception {
        long seed = 1;

        StratifiedRemoveFolds filter = new StratifiedRemoveFolds();
        filter.setInputFormat(trainingData);
        filter.setSeed(seed);
        filter.setNumFolds(fold);
        filter.setFold(fold);
        filter.setInvertSelection(true);
        Instances realTrain = Filter.useFilter(trainingData, filter);

        // there is a bug here, see http://comments.gmane.org/gmane.comp.ai.weka/26145
        // make sure to use the same seed
        filter = new StratifiedRemoveFolds();
        filter.setInputFormat(trainingData);
        filter.setSeed(seed);
        filter.setNumFolds(fold);
        filter.setFold(fold);

        filter.setInvertSelection(false);
        Instances holdout = Filter.useFilter(trainingData, filter);

        List<Instances> trainDev = new LinkedList<Instances>();

        trainDev.add(realTrain);
        trainDev.add(holdout);

        return trainDev;
    }

    protected abstract String[] prepareOptions(Instances trainingData, boolean tuneParameter,
                                               int fold, boolean useCv);

    /**
     * Implement this method to set up the raw classifiers with options
     *
     * @return an untrained classifier
     * @throws Exception
     */
    protected abstract Classifier configRawClassifier() throws Exception;

    /**
     * This method only returns the classifier, other tuned parameters are store with the wrapper
     * object
     *
     * @return
     * @throws Exception
     */
    public Classifier getClassifier() throws Exception {
        if (wrappedClassifier == null)
            throw new Exception("Classifer is not trained");

        return wrappedClassifier;
    }

    /**
     * Use the training data to tune the meta parameters of the given classifier
     * <p/>
     * The tuning parameters are meta-parameters, and will be stored in each implementation separately
     *
     * @param cls                The classifier
     * @param trainingAndDevData Training and dev data all together, the function will decide how to use it inside
     * @param useCv              Whether to use cross validation for parameter tuning
     * @throws Exception
     */
    protected abstract void tuneAndTrain(Classifier cls, Instances trainingAndDevData, int fold, boolean useCv) throws Exception;

    /**
     * Use the training data to train the given classifier
     *
     * @param trainingData
     * @throws Exception
     */
    protected void train(Classifier cls, Instances trainingData) throws Exception {
        logger.info("Training classifier ...");
        logger.info("Training instances " + trainingData.numInstances());
        cls.buildClassifier(trainingData);
        logger.info("Done.");
        this.wrappedClassifier = cls;
    }

    /**
     * This is built on the assumption that class value in WekaFeatureFactory are added in the right
     * way (add negative one first), use carefully!
     *
     * @param instance
     * @return
     * @throws Exception
     */
    public boolean classifyAsPositive(Instance instance) throws Exception {
        return classify(instance) == 1.0;
    }

    /**
     * Classify using the original setting
     *
     * @param instance
     * @return
     * @throws Exception
     */
    public double classify(Instance instance) throws Exception {
        return wrappedClassifier.classifyInstance(instance);
    }

    public double[] getClassDistribution(Instance instance) throws Exception {
        return wrappedClassifier.distributionForInstance(instance);
    }

    /**
     * Provide a default way to classify using the tuned threshold, call it when needed
     *
     * @param instance
     * @return
     * @throws Exception
     */
    protected double classifyWithThreshold(Instance instance) throws Exception {
        double predictedClassLabel;

        double[] distributions = wrappedClassifier.distributionForInstance(instance);

        double trueProb = distributions[1];

        if (trueProb < threshold) {
            predictedClassLabel = 0.0;
        } else {
            predictedClassLabel = 1.0;
        }

        return predictedClassLabel;
    }

    /**
     * As the name implies, only when there are only two classes you can use this, the distribution of
     * the SECOND class (index 1) will be returned. To get the negative one, do (1-return value). Note
     * that this is built on the assumption that class value in WekaFeatureFactory are added in the
     * right way (add negative one first), use carefully!
     *
     * @param instance
     * @return
     * @throws Exception
     */
    public double getPositiveClassDistribution(Instance instance) throws Exception {
        return wrappedClassifier.distributionForInstance(instance)[1];
    }

    /**
     * One frequently used tuning is to tune a probability cutoff to give the best F1 score
     * This can be modified to tune for other measures too
     *
     * @param cls
     * @param devData
     * @return
     * @throws Exception
     */
    protected Pair<Double, Double> findBestF1FromSortedProbs(Classifier cls, Instances devData) throws Exception {
        Pair<List<Pair<Double, Double>>, Integer> sortedProbsAndNumPos = getSortedProbs(cls, devData);
        List<Pair<Double, Double>> sortedProbs = sortedProbsAndNumPos.getValue0();
        int numPositive = sortedProbsAndNumPos.getValue1();
        int numNegative = sortedProbs.size() - numPositive;
        logger.info(String.format("Exhaustive to find best cutoff threshold from %d data", sortedProbs.size()));

        // try all thresholds
        double bestThreshold = 0;
        double bestF1 = 0;

        int tenPercent = sortedProbs.size() / 10;
        int percentCount = -1;

        int fn = 0;
        int tn = 0;

        for (int i = 0; i < sortedProbs.size(); i++) {
            Pair<Double, Double> probs = sortedProbs.get(i);
            double threshold = probs.getValue1();
            double trueLabel = probs.getValue0();

            if (trueLabel == 1.0) {
                fn++;
            } else {
                tn++;
            }
            int tp = numPositive - fn;
            int fp = numNegative - tn;

            double newF1 = calF1(tp, fp, tn, fn);

            if (newF1 > bestF1) {
                bestThreshold = threshold;
                bestF1 = newF1;
            }
            if (tenPercent != 0 && i % tenPercent == 0) {
                percentCount++;
                if (percentCount > 0) {
                    logger.info(String.format("%d %% (%d instances) finished now", percentCount * 10, i));
                }
            }
        }

        logger.info("Best threshold " + bestThreshold + " best F1 " + bestF1);
        return new Pair<Double, Double>(bestThreshold, bestF1);
    }

    /**
     * List of pair of <classlabel,probability> sorted by probability
     *
     * @param cls
     * @param devData
     * @return
     * @throws Exception
     */
    private Pair<List<Pair<Double, Double>>, Integer> getSortedProbs(Classifier cls, Instances devData)
            throws Exception {
        List<Pair<Double, Double>> unsortedProbs = new LinkedList<Pair<Double, Double>>();

        int numPositive = 0;

        for (int i = 0; i < devData.numInstances(); i++) {
            double trueClassLabel = devData.instance(i).classValue();

            if (trueClassLabel == 1.0) {
                numPositive++;
            }

            double[] distributions = cls.distributionForInstance(devData.instance(i));

            double trueProb = distributions[1];

            unsortedProbs.add(new Pair<Double, Double>(trueClassLabel, trueProb));
        }

        List<Pair<Double, Double>> sortedProbs = sortByPairElement2(unsortedProbs);

        logger.info(String.format("Total instances in holdout %d, containing %d positive instances.",
                sortedProbs.size(), numPositive));
        return new Pair<List<Pair<Double, Double>>, Integer>(sortedProbs, numPositive);
    }

    /**
     * Provide a default tuning method, it only works when the classifier output confidence score
     *
     * @param cls
     * @param trainingAndDevData
     * @param fold
     * @param useCv
     * @throws Exception
     */
    protected void tuneAndTrainWithThreshold(Classifier cls, Instances trainingAndDevData, int fold, boolean useCv) throws Exception {
        int numPositive = 0;
        for (int i = 0; i < trainingAndDevData.numInstances(); i++) {
            Instance trainInstance = trainingAndDevData.instance(i);
            if (trainInstance.classValue() == 1.0) {
                numPositive++;
            }
        }
        logger.info(String.format("Training and Tuning classifier [%s], total training and development instances: %d, positive examples : %s ",
                cls.getClass().getSimpleName(), trainingAndDevData.numInstances(), numPositive));

        Instances dataFolds = randomizeData(trainingAndDevData, fold);
        int cvIter = useCv ? fold : 1;

        double weightedThresholdSum = 0;
        double normalizer = 0;
        for (int iter = 0; iter < cvIter; iter++) {
            double tempThreshold = 0;
            double tempF1 = 0;
            Instances trainingData = dataFolds.trainCV(fold, iter);
            Instances devData = dataFolds.testCV(fold, iter);
            logger.debug(String.format("Tuning threshold with %d training data, %d dev data", trainingData.numInstances(), devData.numInstances()));
            cls.buildClassifier(trainingData);
            Pair<Double, Double> result = findBestF1FromSortedProbs(cls, devData);
            tempThreshold = result.getValue0();
            tempF1 = result.getValue1();
            normalizer += tempF1;
            weightedThresholdSum += tempThreshold * tempF1;
            logger.info("Best threshold find by hold out " + tempThreshold);
        }
        // get an average over the threshold
        threshold = weightedThresholdSum / normalizer;

        logger.info("Final threshold after tuning is " + threshold);

        // final training with all data
        train(cls, trainingAndDevData);
    }

    protected double calF1(int tp, int fp, int tn, int fn) {
        PairwiseScore pw = new PairwiseScore(tp, fn, fp, tn);
        return pw.getF1() != null ? pw.getF1() : 0;
    }

    protected <K, V extends Comparable<? super V>> List<Pair<K, V>> sortByPairElement2(List<Pair<K, V>> list) {
        Collections.sort(list, new Comparator<Pair<K, V>>() {
            public int compare(Pair<K, V> o1, Pair<K, V> o2) {
                return (o1.getValue1()).compareTo(o2.getValue1());
            }
        });

        return list;
    }

    public int getNumberOfFeatures() {
        return numberOfFeatures;
    }

    public String getClassifierType() {
        return wrappedClassifier.getClass().getSimpleName();
    }
}