package edu.cmu.lti.event_coref.utils.eval;


import edu.cmu.lti.utils.general.MathUtils;

/**
 * This class calculates the pairwise scores.  We assume the following confusion matrix for
 * the calculation.
 * 
 *                           System output
 *                           yes  no
 * Gold standard class  yes  n11  n10
 *                      no   n01  n00
 *                      
 * n11 = tp, n10 = fn, n01 = fp, n00=tn
 * 
 * For more information on the scores, see:
 * 
 * Marta Recasens and Eduard Hovy. 2011. BLANC: Implementing the Rand index for coreference
 * evaluation. Natural Language Engineering, 17(4):485-510, Cambridge University Press 2010.
 * 
 * @author Jun Araki
 */
public class PairwiseScore extends AbstractFscore {

  private double n11;

  private double n10;

  private double n01;

  private double n00;

  public PairwiseScore() {
    name = "Pairwise";
  }

  /**
   * Constructor.
   * 
   * @param n11 = tp
   * @param n10 = fn
   * @param n01 = fp
   * @param n00 = tn
   */
  public PairwiseScore(double n11, double n10, double n01, double n00) {
    this();
    this.n11 = n11;
    this.n10 = n10;
    this.n01 = n01;
    this.n00 = n00;
    calculateScores(DEFAULT_BETA_ONE);
  }

  /**
   * Constructor with the specified beta.
   * 
   * @param n11
   * @param n10
   * @param n01
   * @param n00
   * @param beta
   */
  public PairwiseScore(double n11, double n10, double n01, double n00, double beta) {
    this(n11, n10, n01, n00);
    calculateScores(beta);
  }

  /**
   * Constructor.
   * 
   * @param precision
   * @param recall
   */
  public PairwiseScore(Double precision, Double recall) {
    this();
    this.precision = precision;
    this.recall = recall;
  }

  /**
   * Constructor.
   * 
   * @param precision
   * @param recall
   * @param f1
   */
  public PairwiseScore(Double precision, Double recall, Double f1) {
    this(precision, recall);
    this.f1 = f1;
  }

  /**
   * Calculates the Pairwise scores with the specified beta.
   * 
   * @param beta
   */
  private void calculateScores(double beta) {
    // Note that we intentionally set null to precision, recall, and f1 in
    // boundary cases, since we need to take averages of those scores over
    // documents.

    // Boundary case (0)
    if (n11 == 0) {
      precision = null;
      recall = null;
      setFscore(null, beta);
      return;
    }

    // Non-boundary case
    precision = 100 * n11 / (n11 + n01);
    recall = 100 * n11 / (n11 + n10);
    Double score = MathUtils.getHarmonicMean(precision, recall, beta);
    setFscore(score, beta);

    // Ordinary implementation
    /*
    boolean boundaryCaseFlag = false;

    // Boundary case (1)
    if (n11 + n01 == 0) {
      boundaryCaseFlag = true;
      precision = 0.0;
      if (n11 + n10 == 0) {
        recall = 0.0;
      } else {
        recall = 100 * n11 / (n11 + n10);
      }
    }

    // Boundary case (2)
    if (n11 + n10 == 0) {
      boundaryCaseFlag = true;
      recall = 0.0;
      if (n11 + n01 == 0) {
        precision = 0.0;
      } else {
        precision = 100 * n11 / (n11 + n01);
      }
    }

    if (boundaryCaseFlag) {
      setFscore(0.0, beta);
      return;
    }

    // Non-boundary case
    precision = 100 * n11 / (n11 + n01);
    recall = 100 * n11 / (n11 + n10);
    Double score = MathUtils.getHarmonicMean(precision, recall, beta);
    setFscore(score, beta);
    */
  }

  // A simple tester.
  public static void main(String[] args) {
//    PairwiseScore ps = new PairwiseScore(0, 1, 1, 89);

//    PairwiseScore ps = new PairwiseScore(279,314,637,17609);
    
    PairwiseScore ps = new PairwiseScore(269,324,591,17655);
            
//    PairwiseScore ps = new PairwiseScore(156,155,344,14668);
    Double pwF1 = ps.getF1();
    Double pwPrecision = ps.getPrecision();
    Double pwRecall = ps.getRecall();

    System.out.println("pwF1: " + pwF1);
    System.out.println("pwPrecision: " + pwPrecision);
    System.out.println("pwRecall: " + pwRecall);
  }

}
