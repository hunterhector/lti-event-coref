package edu.cmu.lti.event_coref.utils.eval;

import edu.cmu.lti.utils.general.MathUtils;

/**
 * This class implements the BLANC scores. For more information on the scores, see:
 * <p/>
 * Marta Recasens and Eduard Hovy. 2011. BLANC: Implementing the Rand index for coreference
 * evaluation. Natural Language Engineering, 17(4):485-510, Cambridge University Press 2010.
 *
 * @author Jun Araki
 */
public class BlancScore extends AbstractFscore {

    /**
     * The number of right corefenrence links
     */
    private double rc;

    /**
     * The number of wrong corefenrence links
     */
    private double wc;

    /**
     * The number of right non-corefenrence links
     */
    private double rn;

    /**
     * The number of wrong non-corefenrence links
     */
    private double wn;

    /**
     * Precision for coreference links
     */
    private double p_c;

    /**
     * Recall for coreference links
     */
    private double r_c;

    /**
     * Precision for non-coreference links
     */
    private double p_n;

    /**
     * Recall for non-coreference links
     */
    private double r_n;

    /**
     * F score for coreference links
     */
    private double f_c;

    /**
     * F score for non-coreference links
     */
    private double f_n;

    /**
     * The default value of alpha
     */
    private static final double DEFAULT_ALPHA = 0.5;

    private static final String LINE_BREAK = System.lineSeparator();

    public BlancScore() {
        name = "BLANC";
    }

    /**
     * Constructor.
     *
     * @param rc
     * @param wc
     * @param wn
     * @param rn
     */
    public BlancScore(double rc, double wc, double wn, double rn) {
        this();
        this.rc = rc;
        this.wc = wc;
        this.wn = wn;
        this.rn = rn;
        calculateScores(DEFAULT_ALPHA, DEFAULT_BETA_ONE);
    }

    /**
     * Constructor with the specified alpha.
     *
     * @param rc
     * @param wc
     * @param wn
     * @param rn
     * @param alpha
     */
    public BlancScore(double rc, double wc, double wn, double rn, double alpha) {
        this(rc, wc, wn, rn);
        calculateScores(alpha, DEFAULT_BETA_ONE);
    }

    /**
     * Constructor with the specified alpha and beta.
     *
     * @param rc
     * @param wc
     * @param wn
     * @param rn
     * @param alpha
     * @param beta
     */
    public BlancScore(double rc, double wc, double wn, double rn, double alpha, double beta) {
        this(rc, wc, wn, rn);
        calculateScores(alpha, beta);
    }

    public BlancScore(Double precision, Double recall, Double f1) {
        this();
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;
    }

    public BlancScore(Double precision, Double recall, Double f1, double f_c, double f_n, double p_c,
                      double p_n, double r_c, double r_n) {
        this(precision, recall, f1);
        this.f_c = f_c;
        this.f_n = f_n;
        this.p_c = p_c;
        this.p_n = p_n;
        this.r_c = r_c;
        this.r_n = r_n;
    }

    /**
     * Calculates the BLANC scores with the specified alpha and beta.
     *
     * @param alpha
     * @param beta
     */
    private void calculateScores(double alpha, double beta) {
        // LogUtils.log("rc: " + rc + ", wc: " + wc + ", wn: " + wn + ", rn: " + rn);

        boolean boundaryCaseFlag = false;

        // Boundary case (1): SYS contains a single entity
        if (wn == 0 && rn == 0) {
            if (wc == 0 && rn == 0) {
                // LogUtils.log("Boundary case (1-1)");

                p_c = 1.0;
                r_c = 1.0;
                p_n = 0.0;
                r_n = 0.0;
                f_c = 1.0;
                f_n = 0.0;

                precision = 100.0;
                recall = 100.0;
                setFscore(100.0, beta);
                return;
            }

            if (rc == 0 && wn == 0) {
                // LogUtils.log("Boundary case (1-2)");

                p_c = 0.0;
                r_c = 0.0;
                p_n = 1.0;
                r_n = 1.0;
                f_c = 0.0;
                f_n = 1.0;

                precision = 0.0;
                recall = 0.0;
                setFscore(0.0, beta);
                return;
            }

            // The case in which GOLD contains links of both types
            if (rc + wn > 0 && wc + rn > 0) {
                // LogUtils.log("Boundary case (1-3)");
                boundaryCaseFlag = true;

                p_c = rc / (rc + wc);
                r_c = rc / (rc + wn);
                p_n = 0.0;
                r_n = 0.0;
                f_c = MathUtils.getHarmonicMean(p_c, r_c, beta);
                f_n = 0.0;
            }
        }

        // Boundary case (2): SYS contains only singletons
        if (rc == 0 && wc == 0) {
            if (rc == 0 && wn == 0) {
                // LogUtils.log("Boundary case (2-1)");

                p_c = 0.0;
                r_c = 0.0;
                p_n = 1.0;
                r_n = 1.0;
                f_c = 0.0;
                f_n = 1.0;

                precision = 100.0;
                recall = 100.0;
                setFscore(100.0, beta);
                return;
            }

            if (wc == 0 && rn == 0) {
                // LogUtils.log("Boundary case (2-2)");

                p_c = 1.0;
                r_c = 1.0;
                p_n = 0.0;
                r_n = 0.0;
                f_c = 1.0;
                f_n = 0.0;

                precision = 0.0;
                recall = 0.0;
                setFscore(0.0, beta);
                return;
            }

            // The case in which GOLD contains links of both types
            if (rc + wn > 0 && wc + rn > 0) {
                // LogUtils.log("Boundary case (2-3)");
                boundaryCaseFlag = true;

                p_c = 0.0;
                r_c = 0.0;
                p_n = rn / (rn + wn);
                r_n = rn / (rn + wc);
                f_c = 0.0;
                f_n = MathUtils.getHarmonicMean(p_n, r_n, beta);
            }
        }

        // Boundary case (3): GOLD includes links of both types
        if (rc + wn > 0 && wc + rn > 0) {

            // The case in which SYS contains no right coreference link
            if (rc == 0) {
                // LogUtils.log("Boundary case (3-1)");
                boundaryCaseFlag = true;

                p_c = 0.0;
                r_c = 0.0;
                p_n = rn / (rn + wn);
                r_n = rn / (rn + wc);
                f_c = 0.0;
                f_n = MathUtils.getHarmonicMean(p_n, r_n, beta);
            }

            // The case in which SYS contains no right non-coreference link
            if (rn == 0) {
                // LogUtils.log("Boundary case (3-2)");
                boundaryCaseFlag = true;

                p_c = rc / (rc + wc);
                r_c = rc / (rc + wn);
                p_n = 0.0;
                r_n = 0.0;
                f_c = MathUtils.getHarmonicMean(p_c, r_c, beta);
                f_n = 0.0;
            }
        }

        // Boundary case (4): SYS contains links of both types
        if (rc + wc > 0 && wn + rn > 0) {

            // The case in which GOLD contains a single entity
            if (wc == 0 && rn == 0) {
                // LogUtils.log("Boundary case (4-1)");

                p_c = rc / (rc + wc);
                r_c = rc / (rc + wn);
                p_n = 0.0;
                r_n = 0.0;
                f_c = MathUtils.getHarmonicMean(p_c, r_c, beta);
                f_n = 0.0;

                precision = 100 * p_c;
                recall = 100 * r_c;
                setFscore(100 * f_c, beta);
                return;
            }

            // The case in which GOLD contains only singletons
            if (rc == 0 && wn == 0) {
                // LogUtils.log("Boundary case (4-2)");

                p_c = 0.0;
                r_c = 0.0;
                p_n = rn / (rn + wn);
                r_n = rn / (rn + wc);
                f_c = 0.0;
                f_n = MathUtils.getHarmonicMean(p_n, r_n, beta);

                precision = 100 * p_n;
                recall = 100 * r_n;
                setFscore(100 * f_n, beta);
                return;
            }
        }

        // Ordinary cases
        if (!boundaryCaseFlag) {
            p_c = rc / (rc + wc);
            r_c = rc / (rc + wn);
            p_n = rn / (rn + wn);
            r_n = rn / (rn + wc);
            f_c = MathUtils.getHarmonicMean(p_c, r_c, beta);
            f_n = MathUtils.getHarmonicMean(p_n, r_n, beta);
        }

        precision = 100 * (p_c + p_n) / 2.0;
        recall = 100 * (r_c + r_n) / 2.0;
        Double score = 100 * (alpha * f_c + (1 - alpha) * f_n);
        setFscore(score, beta);
    }

    /**
     * Returns an F score according to the specified alpha and beta. Note that the BLANC precision and
     * recall are calculated when the BLANC F score is calculated.
     *
     * @param alpha
     * @param beta
     * @return an F score according to the specified alpha and beta
     */
    public Double getFscore(double alpha, double beta) {
        calculateScores(alpha, beta);
        return fscore;
    }

    @Override
    public Double getFscore(double beta) {
        return getFscore(DEFAULT_ALPHA, beta);
    }

    public double getPrecisionCoreference() {
        return p_c;
    }

    public double getRecallCoreference() {
        return r_c;
    }

    public double getPrecisionNonCoreference() {
        return p_n;
    }

    public double getRecallNonCoreference() {
        return r_n;
    }

    public double getFscoreCoreference() {
        return f_c;
    }

    public double getFscoreNonCoreference() {
        return f_n;
    }

    public double getP_c() {
        return p_c;
    }

    public double getR_c() {
        return r_c;
    }

    public double getP_n() {
        return p_n;
    }

    public double getR_n() {
        return r_n;
    }

    public double getF_c() {
        return f_c;
    }

    public double getF_n() {
        return f_n;
    }

    public String getRoundedScores(int decimalPlace) {
        StringBuilder buf = new StringBuilder();

        buf.append("BLANC scores" + LINE_BREAK);
        buf.append("Positive link precision = " + round(p_c * 100, decimalPlace) + LINE_BREAK);
        buf.append("Positive link recall = " + round(r_c * 100, decimalPlace) + LINE_BREAK);
        buf.append("Positive link f1 = " + round(f_c * 100, decimalPlace) + LINE_BREAK);
        buf.append("Negative link precision = " + round(p_n * 100, decimalPlace) + LINE_BREAK);
        buf.append("Negative link recall = " + round(r_n * 100, decimalPlace) + LINE_BREAK);
        buf.append("Negative link f1 = " + round(f_n * 100, decimalPlace) + LINE_BREAK);
        buf.append("Average precision = " + round(getPrecision(), decimalPlace) + LINE_BREAK);
        buf.append("Average recall = " + round(getRecall(), decimalPlace) + LINE_BREAK);
        buf.append("Average f1 = " + round(getF1(), decimalPlace) + LINE_BREAK);

        return buf.toString();
    }

    /**
     * Prints scores rounded with the specified decimal place.
     *
     * @param decimalPlace
     */
    public void printRoundedScores(int decimalPlace) {
        System.out.println(getRoundedScores(decimalPlace));
    }

    private String round(double value, int decimalPlace) {
        return MathUtils.getRoundedDecimalNumber(value, decimalPlace, true);
    }

    /**
     * A simple tester.
     *
     * @param args
     */
    public static void main(String[] args) {
        BlancScore bs = new BlancScore(1006, 247, 221, 1032);
        // BlancScore bs = new BlancScore(279,637,314,17609);
        // BlancScore bs = new BlancScore(156,344,155,14668);

        bs.printRoundedScores(2);
        // double blancPrecision = bs.getPrecision();
        // double blancRecall = bs.getRecall();
        // double blancF1 = bs.getF1();

        // LogUtils.log("blancF1: " + blancF1);
        // LogUtils.log("blancPrecision: " + blancPrecision);
        // LogUtils.log("blancRecall: " + blancRecall);
    }

}
