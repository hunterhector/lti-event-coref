package edu.cmu.lti.utils.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for handling errors.
 * 
 * @author Jun Araki
 */
public class ErrorUtils {
    private static final Logger logger = LoggerFactory.getLogger(ErrorUtils.class);

  /**
   * Terminates if the specified condition is true, while logging an error message.
   * 
   * @param condition
   * @param errorMessage
   */
  public static void terminateIfTrue(boolean condition, String errorMessage) {
    if (condition) {
      logger.error(errorMessage);
      System.exit(1);
    }
  }

  /**
   * Terminates if the specified condition is false, while logging an error message.
   * 
   * @param condition
   * @param errorMessage
   */
  public static void terminateIfFalse(boolean condition, String errorMessage) {
    terminateIfTrue(!condition, errorMessage);
  }

  /**
   * Terminates if the specified object is null, while logging an error message.
   * 
   * @param o
   * @param errorMessage
   */
  public static void terminateIfNull(Object o, String errorMessage) {
    terminateIfTrue((o == null), errorMessage);
  }

  /**
   * Forces the system to terminate while printing our an error message.
   * 
   * @param errorMessage
   */
  public static void terminate(String errorMessage) {
    terminateIfFalse(false, errorMessage);
  }

  /**
   * Terminates if the specified string is null or empty, while logging an error message.
   * 
   * @param str
   * @param errorMessage
   */
  public static void checkNullOrEmptyString(String str, String errorMessage) {
    terminateIfTrue(StringUtils.isNullOrEmptyString(str), errorMessage);
  }

}
