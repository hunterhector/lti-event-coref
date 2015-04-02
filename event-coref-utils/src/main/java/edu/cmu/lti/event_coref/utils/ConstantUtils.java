package edu.cmu.lti.event_coref.utils;

/**
 * A utility class for handling enum constants.
 * 
 * @author Jun Araki
 */
public class ConstantUtils {

  /**
   * Checks whether the constant with the specified name is defined in the specified enum class.
   * 
   * @param <T>
   * @param enumClass
   * @param name
   * @return true if the constant with the specified name is defined in the specified enum class; false otherwise
   */
  public static <T extends Enum<T>> boolean isValidConstant(Class<T> enumClass, String name) {
    for (T constant : enumClass.getEnumConstants()) {
      if (constant.toString().equals(name)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the constant with the specified name in the specified enum class
   * 
   * @param <T>
   * @param enumClass
   * @param name
   * @return the constant with the specified name in the specified enum class
   */
  public static <T extends Enum<T>> T getConstant(Class<T> enumClass, String name) {
    for (T constant : enumClass.getEnumConstants()) {
      if (constant.toString().equals(name)) {
        return constant;
      }
    }

    return null;
  }

}
