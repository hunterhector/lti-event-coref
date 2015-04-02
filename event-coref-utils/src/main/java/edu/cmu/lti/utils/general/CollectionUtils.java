package edu.cmu.lti.utils.general;

import java.util.Collection;

/**
 * A utility class for handling collections.
 * 
 * @author Jun Araki
 */
public class CollectionUtils {

  /**
   * Tests whether the specified collection is null or empty.
   * 
   * @param <T>
   * @param collection
   * @return True if the specified collection is null or empty; false otherwise
   */
  public static <T> boolean isNullOrEmptyCollection(Collection<T> collection) {
    if (collection == null || collection.isEmpty()) {
      return true;
    }

    return false;
  }

  /**
   * Tests whether all the values of the specified collection are null.
   * 
   * @param <T>
   * @param collection
   * @return True if all the values of the specified collection are null; false otherwise
   */
  public static <T> boolean areAllValuesNull(Collection<T> collection) {
    for (T value : collection) {
      if (value != null) {
        return false;
      }
    }

    return true;
  }

}
