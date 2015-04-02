package edu.cmu.lti.event_coref.utils.eval;

import edu.cmu.lti.event_coref.utils.EventCoreferenceMiscUtils;
import edu.cmu.lti.event_coref.utils.eval.FscoreConstants.ScoreLabel;
import edu.cmu.lti.utils.general.MathUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractFscore implements IFscore {
    protected String name;
    protected Double fscore;
    protected Double f1;
    protected Double precision;
    protected Double recall;

    /**
     * The default value of beta to calculate F1
     */
    protected static final double DEFAULT_BETA_ONE = 1.0;

    /**
     * Constructor.
     */
    public AbstractFscore() {
        name = "";
    }

    /**
     * Constructor.
     *
     * @param precision
     * @param recall
     */
    public AbstractFscore(Double precision, Double recall) {
        this();
        this.precision = precision;
        this.recall = recall;
        calculateFscore(DEFAULT_BETA_ONE);
    }

    /**
     * Constructor with a variable beta.
     *
     * @param precision
     * @param recall
     * @param beta
     */
    public AbstractFscore(Double precision, Double recall, double beta) {
        this();
        this.precision = precision;
        this.recall = recall;
        calculateFscore(beta);
    }

    /**
     * Constructor with an F1 score.
     *
     * @param precision
     * @param recall
     * @param f1
     */
    public AbstractFscore(Double precision, Double recall, Double f1) {
        this();
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;
    }

    public String getName() {
        return name;
    }

    /**
     * Calculates the F score. This method assumes that precision and recall are already given.
     *
     * @param beta
     */
    public void calculateFscore(double beta) {
        Double fscore = MathUtils.getHarmonicMean(precision, recall, beta);
        setFscore(fscore, beta);
    }

    public void setFscore(Double fscore, double beta) {
        if (beta == DEFAULT_BETA_ONE) {
            this.f1 = fscore;
        }
        this.fscore = fscore;
    }

    public Double getFscore() {
        return fscore;
    }

    /**
     * Recalculates an F score using the specified beta, and returns it.
     */
    public Double getFscore(double beta) {
        calculateFscore(beta);
        return fscore;
    }

    /**
     * Returns the F1 score.  We assume that the F1 score was already calculated.
     */
    public Double getF1() {
        return f1;
    }

    public Double getPrecision() {
        return precision;
    }

    public Double getRecall() {
        return recall;
    }

    /**
     * Returns a list of score labels including F1.
     *
     * @return a list of score labels including F1
     */
    public List<String> getScoreLabelsWithF1() {
        return ScoreLabel.getLabelListWithF1();
    }

    /**
     * Returns a list of scores including F1.
     *
     * @return a list of scores including F1
     */
    public List<Double> getScoresWithF1() {
        List<Double> scoreList = new ArrayList<Double>();
        scoreList.add(precision);
        scoreList.add(recall);
        scoreList.add(f1);

        return scoreList;
    }

    /**
     * Returns a list of formatted scores including F1.
     *
     * @return a list of formatted scores including F1
     */
    public List<String> getFormattedScoresWithF1() {
        List<String> scoreList = new ArrayList<String>();
        scoreList.add(EventCoreferenceMiscUtils.getFormattedScore(precision, 2));
        scoreList.add(EventCoreferenceMiscUtils.getFormattedScore(recall, 2));
        scoreList.add(EventCoreferenceMiscUtils.getFormattedScore(f1, 2));

        return scoreList;
    }

    /**
     * Prints scores rounded with the specified decimal place.
     *
     * @param decimalPlace
     */
    public void printRoundedScores(int decimalPlace) {
        String roundedPrecision = MathUtils.getRoundedDecimalNumber(precision, decimalPlace, true);
        String roundedRecall = MathUtils.getRoundedDecimalNumber(recall, decimalPlace, true);
        String roundedF1 = MathUtils.getRoundedDecimalNumber(f1, decimalPlace, true);

        System.out.println("Evaluation Metrics: " + name);
        System.out.println(ScoreLabel.PRECISION.toString() + ": " + roundedPrecision);
        System.out.println(ScoreLabel.RECALL.toString() + ": " + roundedRecall);
        System.out.println(ScoreLabel.F1.toString() + ": " + roundedF1);
    }

}
