package edu.cmu.lti.util.general;

import edu.cmu.lti.util.general.CollectionUtils;
import edu.cmu.lti.util.general.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A utility class for handling mathematical operations.
 * 
 * @author Jun Araki
 */
public class MathUtils {

  private static final String DECIMAL_POINT_REGEX = "\\.";

  private static final String DECIMAL_POINT = ".";

  private static final String PATTERN_CHAR = "#";

  private static final String MINUS_CHAR = "-";

  private static final String ZERO_CHAR = "0";

  /**
   * Returns the maximum of the numbers stored in the specified collection.
   * 
   * @param values
   * @return the maximum of the numbers stored in the specified collection
   */
  public static <T extends Number> Double getMax(Collection<T> values) {
    if (CollectionUtils.isNullOrEmptyCollection(values)) {
      return null;
    }

    if (CollectionUtils.areAllValuesNull(values)) {
      return null;
    }

    Double max = Double.NEGATIVE_INFINITY;
    for (T value : values) {
      if (value == null) {
        continue;
      }

      Double d = value.doubleValue();
      if (d > max) {
        max = d;
      }
    }
    return max;
  }

  /**
   * Returns the minimum of the numbers stored in the specified collection.
   * 
   * @param values
   * @return the minimum of the numbers stored in the specified collection
   */
  public static <T extends Number> Double getMin(Collection<T> values) {
    if (CollectionUtils.isNullOrEmptyCollection(values)) {
      return null;
    }

    if (CollectionUtils.areAllValuesNull(values)) {
      return null;
    }

    Double min = Double.POSITIVE_INFINITY;
    for (T value : values) {
      if (value == null) {
        continue;
      }

      Double d = value.doubleValue();
      if (d < min) {
        min = d;
      }
    }
    return min;
  }

  /**
   * Returns the sum of the numbers stored in the specified values. If a number in the values is
   * null, then it is ignored.
   * 
   * @param values
   * @return the sum of the numbers stored in the specified values
   */
  public static <T extends Number> Double getSum(Collection<T> values) {
    if (CollectionUtils.isNullOrEmptyCollection(values)) {
      return null;
    }

    if (CollectionUtils.areAllValuesNull(values)) {
      return null;
    }

    Double sum = 0.0;
    for (T value : values) {
      if (value == null) {
        continue;
      }

      sum += value.doubleValue();
    }
    return sum;
  }

  /**
   * Returns the average of the numbers stored in the specified values. If a number in the values is
   * null, then it is ignored.
   * 
   * @param values
   * @return the average of the numbers stored in the specified values
   */
  public static <T extends Number> Double getAverage(Collection<T> values) {
    if (CollectionUtils.isNullOrEmptyCollection(values)) {
      return null;
    }

    if (CollectionUtils.areAllValuesNull(values)) {
      return null;
    }

    Double sum = 0.0;
    Double count = 0.0;
    for (T value : values) {
      if (value == null) {
        continue;
      }

      sum += value.doubleValue();
      count++;
    }
    return (sum / count);
  }

  /**
   * Returns the variance of the numbers stored in the specified values. If a number in the values is
   * null, then it is ignored.
   * 
   * @param values
   * @return the average of the numbers stored in the specified values
   */
  public static <T extends Number> Double getVariance(Collection<T> values) {
    Double avg = getAverage(values);
    if (avg == null) {
      return null;
    }

    Double variance = 0.0;
    Double count = 0.0;
    for (T value : values) {
      if (value == null) {
        continue;
      }

      Double d = value.doubleValue();
      variance += (d - avg) * (d - avg);
      count++;
    }

    return (variance / count);
  }

  /**
   * Returns the standard deviation of the numbers stored in the specified values. If a number in the values is
   * null, then it is ignored.
   * 
   * @param values
   * @return the standard deviation of the numbers stored in the specified collection
   */
  public static <T extends Number> Double getStandardDeviation(Collection<T> values) {
    Double var = getVariance(values);
    if (var == null) {
      return null;
    }

    return Math.sqrt(var);
  }

  /**
   * Returns the harmonic mean of the specified p and r values. Those values must be positive real
   * numbers.
   * 
   * @param p
   * @param r
   * @param beta
   * @return the harmonic mean of the specified p and r values
   */
  public static Double getHarmonicMean(Double p, Double r, Double beta) {
    if (p == null || r == null) {
      return null;
    }
    if (p <= 0 || r <= 0) {
      return null;
    }

    return ((1.0 + beta * beta) * p * r) / (beta * beta * p + r);
  }

  /**
   * Returns the number of positive values in the specified collection of values.
   * 
   * @param <T>
   * @param values
   * @return the number of positive values in the specified collection of values
   */
  public static <T extends Number> int getNumberOfPositiveValues(Collection<T> values) {
    if (values == null || values.isEmpty()) {
      return 0;
    }

    int counter = 0;
    for (T value : values) {
      double d = value.doubleValue();
      if (d > 0) {
        counter++;
      }
    }
    return counter;
  }

  /**
   * Returns the number of negative values in the specified collection of values.
   * 
   * @param <T>
   * @param values
   * @return the number of negative values in the specified collection of values
   */
  public static <T extends Number> int getNumberOfNegativeValues(Collection<T> values) {
    if (values == null || values.isEmpty()) {
      return 0;
    }

    int counter = 0;
    for (T value : values) {
      double d = value.doubleValue();
      if (d < 0) {
        counter++;
      }
    }
    return counter;
  }

  /**
   * Returns the decimal number rounded with the specified decimal spaces.
   * 
   * @param number
   * @param numOfDecimalSpaces
   * @param keepExtraZeros
   * @return the decimal number rounded with the specified decimal spaces
   */
  public static String getRoundedDecimalNumber(Double number, int numOfDecimalSpaces,
          boolean keepExtraZeros) {
    if (number == null) {
      return null;
    }

    String numStr = StringUtils.convertDoubleToString(number);
    String[] parts = numStr.split(DECIMAL_POINT_REGEX);
    String integerPart = parts[0];

    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < integerPart.length(); i++) {
      if (i == 0 && integerPart.startsWith(MINUS_CHAR)) {
        buf.append(PATTERN_CHAR);
        continue;
      }

      if (keepExtraZeros) {
        buf.append(ZERO_CHAR);
      } else {
        buf.append(PATTERN_CHAR);
      }
    }
    if (numOfDecimalSpaces > 0) {
      buf.append(DECIMAL_POINT);
    }
    for (int i = 0; i < numOfDecimalSpaces; i++) {
      if (keepExtraZeros) {
        buf.append(ZERO_CHAR);
      } else {
        buf.append(PATTERN_CHAR);
      }
    }

    String pattern = buf.toString();
    DecimalFormat df = new DecimalFormat(pattern);
    return df.format(number);
  }

  /**
   * Returns the decimal number rounded with the specified decimal spaces. This method can specify a
   * null string which the method return when the specified number is null.
   * 
   * @param number
   * @param numOfDecimalSpaces
   * @param keepExtraZeros
   * @param nullString
   * @return
   */
  public static String getRoundedDecimalNumber(Double number, int numOfDecimalSpaces,
          boolean keepExtraZeros, String nullString) {
    if (number == null) {
      return nullString;
    }

    return getRoundedDecimalNumber(number, numOfDecimalSpaces, keepExtraZeros);
  }

  /**
   * Returns log of the specified value with the specified base. log_x y = log_z y / log_z x
   * 
   * @param base
   * @param value
   * @return log of the specified value with the specified base
   */
  public static double log(double base, double value) {
    return (Math.log(value) / Math.log(base));
  }

  /**
   * Returns nCk.
   * 
   * @param n
   * @param k
   * @return nCk
   */
  public static Double getCombination(int n, int k) {
    if (k > n) {
      return null;
    }

    double denominator = 1;
    double numerator = 1;
    for (int i = 0; i < k; i++) {
      numerator = numerator * (n - i);
      denominator = denominator * (k - i);
    }

    return (numerator / denominator);
  }

  // TODO: Move the code below to test cases.
  public static void main(String args[]) {
    List<Integer> integerList = new ArrayList<Integer>();
    integerList.add(1);
    integerList.add(2);
    integerList.add(6);

    List<Double> doubleList = new ArrayList<Double>();
    doubleList.add(3.5);
    doubleList.add(352.2);
    doubleList.add(1.0000005);

    double p = 0.8;
    double r = 0.6;
    double beta = 1.0;
    double fscore = getHarmonicMean(p, r, beta);
    String formattedFscore = getRoundedDecimalNumber(fscore, 1, true);

    double number = 3.0;
    String formattedNumber = getRoundedDecimalNumber(number, 2, true);

    try {
      System.out.println("Sum: " + getSum(integerList));
      System.out.println("Average: " + getAverage(integerList));
      System.out.println("Variance: " + getVariance(integerList));
      System.out.println("Standard deviation: " + getStandardDeviation(integerList));

      System.out.println("Sum: " + getSum(doubleList));
      System.out.println("Average: " + getAverage(doubleList));

      // System.out.println("Sum: " + getSum(integerList2));

      System.out.println("fscore: " + fscore);
      System.out.println("formattedFscore: " + formattedFscore);

      // Should print out "3.00"
      System.out.println("formattedNumber: " + formattedNumber);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
