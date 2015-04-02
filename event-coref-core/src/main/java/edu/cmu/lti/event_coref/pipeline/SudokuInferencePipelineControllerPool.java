package edu.cmu.lti.event_coref.pipeline;


import edu.cmu.lti.event_coref.utils.ml.FeatureImputator;
import weka.classifiers.Classifier;

public class SudokuInferencePipelineControllerPool {
  public static int numberNewCoreference = -1;

  public static int truePositiveCount = 0;

  public static int falsePositiveCount = 0;

  public static int trueNegativeCount = 0;

  public static int falseNegativeCount = 0;

  public static int preFilterErrorCount = 0;

  public static int unifiedEventCount = 0;

  // public static ClassifierLearner learner;
  public static Classifier classifier;

  public static FeatureImputator imputer;

  public static void dump() {
    double precision = truePositiveCount * 1.0 / (truePositiveCount + falsePositiveCount);
    double recall = truePositiveCount * 1.0 / (truePositiveCount + falseNegativeCount);

    double f1 = 2.0 * precision * recall / (precision + recall);

    System.out.println("Non-transitive closure result by control pool: ");
    System.out.println("||= True postive =||= False positive =||= True Negative =||= False Negative =||= Pre filter error =||= Number of Unification =||= Precision =||= Recall =||= F1 =||");
    // System.out.println(" ||" + truePositiveCount + " ||" + falsePositiveCount + " ||" +
    // trueNegativeCount
    // + " ||" + falseNegativeCount + " ||" + preFilterErrorCount + " ||" + unifiedEventCount
    // + " ||" + precision * 100 + " ||" + recall * 100 + " ||" + f1 * 100 + " ||");

    System.out.println(String.format("||Method ||%d ||%d ||%d ||%d ||%d ||%d ||%.2f ||%.2f ||%.2f ||",
            truePositiveCount, falsePositiveCount, trueNegativeCount, falseNegativeCount,
            preFilterErrorCount, unifiedEventCount, precision * 100, recall * 100, f1 * 100));
  }
}
